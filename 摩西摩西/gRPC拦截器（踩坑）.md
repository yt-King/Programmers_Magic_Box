# gRPC拦截器（踩坑）

> 需求：目前中台的各种服务都通过`k8s`部署在各个容器中，现在想要实现灰度版本切换，方案是在`commom_grpc`包中使用`grpc`的两端拦截器添加`metadata`信息用于代理。大致流程如下：
>
> 1. `k8s`在`metadata`中写入目标版本信息给网关
> 2. 网关把metadata转发出去调用某个服务
> 3. 在转发的时候 `k8s` 会拦截，判断走哪个版本的服务
>
> grpc的两端服务器的作用就是解决在`grpc`服务里调用其他`grpc`服务无法传递版本信息的问题，首先`grpc`作为服务端响应请求，这是服务端拦截器会在响应请求前拦截请求并获取`metadata`中的`roleid`（版本控制信息）存入`threadlocal`中，这是如果服务端需要调用其他`grpc`服务，那么此刻这个服务端就变成了客户端，客户端拦截器会在请求发出之前拦截请求，将之前保存在`threallocal`的`roleid`（版本控制信息）在存入`metadata`中发给服务端，这样发送途中`k8s`就可以知晓版本控制信息判断走那个服务。

## 服务端拦截器

### 1、作用时机？

请求被具体的Handler相应前。

### 2、可以做什么？

编辑元数据

```java
@GrpcGlobalServerInterceptor
public class GrpcServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(serverCallHandler.startCall(serverCall, metadata)) {
            @Override
            public void onHalfClose() {
                try {
                    //将metadata信息保存在ThreadLocal在客户端拦截器中传递下去
                    String roleId = metadata.get(Metadata.Key.of("roleid", Metadata.ASCII_STRING_MARSHALLER));
                    if (StrUtil.isNotBlank(roleId)) {
                        ThreadLocalUtil.put(roleId);
                    }
                    super.onHalfClose();
                } catch (Exception e) {
                    e.printStackTrace();
                    // 通过元数据传输错误信息
                    metadata.put(Metadata.Key.of("message-bin", Metadata.BINARY_BYTE_MARSHALLER), e.getMessage().getBytes());

                    if (e instanceof ServiceException) {
                        serverCall.close(Status.INTERNAL.withDescription(((ServiceException) e).getCode() + ":" + e.getMessage()), metadata);
                    } else {
                        serverCall.close(Status.UNKNOWN.withDescription(e.getMessage()), metadata);
                    }
                }
            }
        };
    }
}
```

## 客户端拦截器

### 1、作用时机？

请求被分发出去之前。

### 2、可以做什么？

添加请求头数据、以便代理转发使用

```java
@GrpcGlobalClientInterceptor
public class GrpcClientInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                //获取保存在ThreadLocal中的元数据
                String roleId = ThreadLocalUtil.get();
                if (StrUtil.isNotBlank(roleId)) {
                    headers.put(Metadata.Key.of("roleid", Metadata.ASCII_STRING_MARSHALLER), roleId);
                    ThreadLocalUtil.remove();
                }
                super.start(responseListener, headers);
            }
        };
    }
}
```

### 3、踩坑经历

错误的代码示范：

```java
return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {

    @Override
    public void start(Listener<RespT> responseListener, Metadata headers) {
        super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
            @Override
            public void onHeaders(Metadata headers) {
                headers.put(Metadata.Key.of("test-bin", Metadata.ASCII_STRING_MARSHALLER), "test");
                headers = (Metadata) ThreadLocalUtil.get("metadata");
                System.out.println(System.currentTimeMillis() + "/client.metadata.toString() = " + headers.toString());
                super.onHeaders(headers);
            }
        }, headers);
    }
};
```

在使用该错误代码时会发生一个奇怪的现象：我的服务端接收请求后服务端拦截器正常响应，然后当服务端要去调别的grpc服务时应该是先走自己的客户端拦截器，然后再被我们请求的那个服务的服务端拦截器拦截，但测试后这两个顺序却反了过来，我们请求的服务的服务端拦截器先拦截了请求，然后才是自己的客户端拦截器拦截请求，这时候就算把版本信息写到`metadata`里也没用了，因为请求的那个服务端已经响应完了自己才把信息写到`metadata`里。经过分析发现问题出在`new responseListener`中，他是一个响应监听器，说明他是**后置**的，只有我请求的那个服务端响应了他才会去执行`onHeaders`方法，这时候已经晚了。

解决的办法就是不去`new`这个`responseListener`，在`start()`方法里就去设置`metadata`的信息，这样的话就可以不用等响应直接设置好版本信息在发送请求，然后才是被请求的服务端拦截器拦截，这时候就可以正常的在`metadata`中拿到版本信息了。

## 问题记录

1. -bin是干什么用的以及-bin的乱码问题

   >注意 HTTP2 并不允许随意使用字节序列来作为报头值，所以二进制的报头值必须使用 Base64 来编码，[参见链接](https://tools.ietf.org/html/rfc4648#section-4)。 实现必须接受填充的和非填充的值，并且发出非填充的值。应用以“-bin”结尾的名称来定义二进制报头。运行时库在报头被发送和接收时，用这个后缀来检测二进制报头并且正确地在报头被发送和接收时进行 Base64 编码和解码。参考：[HTTP2 协议上的 gRPC](https://www.cnblogs.com/panpanwelcome/p/13492559.html)
   >
   >乱码问题：在调试的时候输出元数据信息发现已`-bin`结尾的`key`的值输出来是乱码，应该是因为上面说到的 `Base64` 编码的问题，因为打断点看他的`byte`数组值是正确的
   >
   >![image-20220915163629217](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-9/image-20220915163629217.png)
   >
   >将其通过`utf-8`格式输出也是对的：
   >
   >![image-20220915163724925](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-9/image-20220915163724925.png)

3. `(Metadata.Key.of("roleId-bin", Metadata.ASCII_STRING_MARSHALLER)`报错：

   > ASCII header is named roleId-bin.  Only binary headers may end with -bin，以-bin结尾的key只能使用binary headers。

