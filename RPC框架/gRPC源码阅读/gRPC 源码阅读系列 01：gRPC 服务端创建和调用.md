## 1. RPC 入门

### 1.1 RPC 框架原理

RPC 框架的目标就是让远程服务调用更加简单、透明，RPC 框架负责屏蔽底层的传输方式（TCP 或者 UDP）、序列化方式（XML/Json/ 二进制）和通信细节。服务调用者可以像调用本地接口一样调用远程的服务提供者，而不需要关心底层通信细节和调用过程。

RPC 框架的调用原理图如下所示：

![image-20221222161101744](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/image-20221222161101744.png)

### 1.2 业界主流的 RPC 框架

业界主流的 RPC 框架整体上分为三类：

- 支持多语言的 RPC 框架，比较成熟的有 Google 的 gRPC、Apache（Facebook）的 Thrift；
- 只支持特定语言的 RPC 框架，例如新浪微博的 Motan；
- 支持服务治理等服务化特性的分布式服务框架，其底层内核仍然是 RPC 框架, 例如阿里的 Dubbo。

随着微服务的发展，基于语言中立性原则构建微服务，逐渐成为一种主流模式，例如对于后端并发处理要求高的微服务，比较适合采用 Go 语言构建，而对于前端的 Web 界面，则更适合 Java 和 JavaScript。
因此，基于多语言的 RPC 框架来构建微服务，是一种比较好的技术选择。例如 Netflix，API 服务编排层和后端的微服务之间就采用 gRPC 进行通信。

### 1.3 gRPC 简介

gRPC 是一个高性能、开源和通用的 RPC 框架，面向服务端和移动端，基于 HTTP/2 设计。

#### 1.3.1 gRPC 概览

gRPC 是由 Google 开发并开源的一种语言 中立的 RPC 框架，当前支持 C、Java 和 Go 语言，其中 C 版本支持 C、C++、Node.js、C# 等。当前 Java 版本最新 Release 版为 1.5.0。

Git 地址如下：[grpc-java](https://github.com/grpc/grpc-java)

gRPC 的调用示例如下所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-01-02.png)

#### 1.3.2 gRPC 特点

- 语言中立，支持多种语言；
- 基于 IDL 文件定义服务，通过 proto3 工具生成指定语言的数据结构、服务端接口以及客户端 Stub；
- 通信协议基于标准的 HTTP/2 设计，支持双向流、消息头压缩、单 TCP 的多路复用、服务端推送等特性，这些特性使得 gRPC 在移动端设备上更加省电和节省网络流量；
- 序列化支持 PB（Protocol Buffer）和 JSON，PB 是一种语言无关的高性能序列化框架，基于 HTTP/2 + PB, 保障了 RPC 调用的高性能。

## 2. gRPC 服务端创建

以官方的 helloworld 为例，介绍 gRPC 服务端创建以及 service 调用流程（采用简单 RPC 模式）。

### 2.1 服务端创建业务代码

服务定义如下（helloworld.proto）：

```java
service Greeter {
  rpc SayHello (HelloRequest) returns (HelloReply) {}
}
message HelloRequest {
  string name = 1;
}
message HelloReply {
  string message = 1;
}
```

服务端创建代码如下（HelloWorldServer 类）：

```java
private void start() throws IOException {
    /* The port on which the server should run */
    int port = 50051;
    server = ServerBuilder.forPort(port)
        .addService(new GreeterImpl())
        .build()
        .start();
...
```

其中，服务端接口实现类（GreeterImpl）如下所示：

```java
static class GreeterImpl extends GreeterGrpc.GreeterImplBase {
    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
      HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();
    }
  }
```

### 2.2 服务端创建流程

gRPC 服务端创建采用 Build 模式，对底层服务绑定、transportServer 和 NettyServer 的创建和实例化做了封装和屏蔽，让服务调用者不用关心 RPC 调用细节，整体上分为三个过程：

- 创建 Netty HTTP/2 服务端；
- 将需要调用的服务端接口实现类注册到内部的 Registry 中，RPC 调用时，可以根据 RPC 请求消息中的服务定义信息查询到服务接口实现类；
- 创建 gRPC Server，它是 gRPC 服务端的抽象，聚合了各种 Listener，用于 RPC 消息的统一调度和处理。

下面我们看下 gRPC 服务端创建流程：

![image-20221222161129101](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/image-20221222161129101.png)

gRPC 服务端创建关键流程分析：

- **NettyServer 实例创建：**gRPC 服务端创建，首先需要初始化 NettyServer，它是 gRPC 基于 Netty 4.1 HTTP/2 协议栈之上封装的 HTTP/2 服务端。NettyServer 实例由 NettyServerBuilder 的 buildTransportServer 方法构建，NettyServer 构建完成之后，监听指定的 Socket 地址，即可实现基于 HTTP/2 协议的请求消息接入。
- **绑定 IDL 定义的服务接口实现类：**gRPC 与其它一些 RPC 框架的差异点是服务接口实现类的调用并不是通过动态代理和反射机制，而是通过 proto 工具生成代码，在服务端启动时，将服务接口实现类实例注册到 gRPC 内部的服务注册中心上。请求消息接入之后，可以根据服务名和方法名，直接调用启动时注册的服务实例，而不需要通过反射的方式进行调用，性能更优。
- **gRPC 服务实例（ServerImpl）构建：**ServerImpl 负责整个 gRPC 服务端消息的调度和处理，创建 ServerImpl 实例过程中，会对服务端依赖的对象进行初始化，例如 Netty 的线程池资源、gRPC 的线程池、内部的服务注册类（InternalHandlerRegistry）等，ServerImpl 初始化完成之后，就可以调用 NettyServer 的 start 方法启动 HTTP/2 服务端，接收 gRPC 客户端的服务调用请求。

### 2.3 服务端 service 调用流程

gRPC 的客户端请求消息由 Netty Http2ConnectionHandler 接入，由 gRPC 负责将 PB 消息（或者 JSON）反序列化为 POJO 对象，然后通过服务定义查询到该消息对应的接口实例，发起本地 Java 接口调用，调用完成之后，将响应消息反序列化为 PB（或者 JSON），通过 HTTP2 Frame 发送给客户端。
流程并不复杂，但是细节却比较多，整个 service 调用可以划分为如下四个过程：

- gRPC 请求消息接入；
- gRPC 消息头和消息体处理；
- 内部的服务路由和调用；
- 响应消息发送。

#### 2.3.1 gRPC 请求消息接入

gRPC 的请求消息由 Netty HTTP/2 协议栈接入，通过 gRPC 注册的 Http2FrameListener，将解码成功之后的 HTTP Header 和 HTTP Body 发送到 gRPC 的 NettyServerHandler 中，实现基于 HTTP/2 的 RPC 请求消息接入。

gRPC 请求消息接入流程如下：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-01-04.png)

关键流程解读如下：

- Netty 4.1 提供了 HTTP/2 底层协议栈，通过 Http2ConnectionHandler 及其依赖的其它类库，实现了 HTTP/2 消息的统一接入和处理。
  通过注册 Http2FrameListener 监听器，可以回调接收 HTTP2 协议的消息头、消息体、优先级、Ping、SETTINGS 等。
  gRPC 通过 FrameListener 重载 Http2FrameListener 的 onDataRead、onHeadersRead 等方法，将 Netty 的 HTTP/2 消息转发到 gRPC 的 NettyServerHandler 中。
- Netty 的 HTTP/2 协议接入仍然是通过 ChannelHandler 的 CodeC 机制实现，它并不影响 NIO 线程模型。
  因此，理论上各种协议、以及同一个协议的多个服务端实例可以共用同一个 NIO 线程池（NioEventLoopGroup）, 也可以独占。
  在实践中独占模式普遍会存在线程资源占用过载问题，很容易出现句柄等资源泄漏。
  在 gRPC 中，为了避免该问题，默认采用共享池模式创建 NioEventLoopGroup，所有的 gRPC 服务端实例，都统一从 SharedResourceHolder 分配 NioEventLoopGroup 资源，实现 NioEventLoopGroup 的共享。

#### 2.3.2 gRPC 消息头和消息体处理

gRPC 消息头的处理入口是 NettyServerHandler 的 onHeadersRead()，处理流程如下所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-01-05.png)

处理流程如下：

- 对 HTTP Header 的 Content-Type 校验，此处必须是 “application/grpc”；

- 从 HTTP Header 的 URL 中提取接口和方法名，以 HelloWorldServer 为例，它的 method 为：”helloworld.Greeter/SayHello”；

- 将 Netty 的 HTTP Header 转换成 gRPC 内部的 Metadata，Metadata 内部维护了一个键值对的二维数组 namesAndValues，以及一系列的类型转换方法：

  ![image-20221222161353458](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/image-20221222161353458.png)

- 创建 NettyServerStream 对象，它持有了 Sink 和 TransportState 类，负责将消息封装成 GrpcFrameCommand，与底层 Netty 进行交互，实现协议消息的处理；

- 创建 NettyServerStream 之后，会触发 ServerTransportListener 的 streamCreated 方法，在该方法中，主要完成了消息上下文和 gRPC 业务监听器的创建；

- gRPC 上下文创建：CancellableContext 创建之后，支持超时取消，如果 gRPC 客户端请求消息在 Http Header 中携带了“grpc-timeout”，系统在创建 CancellableContext 的同时会启动一个延时定时任务，延时周期为超时时间，一旦该定时器成功执行，就会调用 CancellableContext.CancellationListener 的 cancel 方法，发送 CancelServerStreamCommand 指令；

- JumpToApplicationThreadServerStreamListener 的创建：它是 ServerImpl 的内部类，从命名上基本可以看出它的用途，即从 ServerStream 跳转到应用线程中进行服务调用，gRPC 服务端的接口调用主要通过 JumpToApplicationThreadServerStreamListener 的 messageRead 和 halfClosed 方法完成；

- 将 NettyServerStream 的 TransportState 缓存到 Netty 的 Http2Stream 中，当处理请求消息体时，可以根据 streamId 获取到 Http2Stream，进而根据“streamKey”还原 NettyServerStream 的 TransportState，进行后续处理。

gRPC 消息体的处理入口是 NettyServerHandler 的 onDataRead()，处理流程如下所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-01-07.png)

消息体处理比较简单，下面就关键技术点进行讲解：

- 因为 Netty HTTP/2 协议 Http2FrameListener 分别提供了 onDataRead 和 onHeadersRead 回调方法，所以 gRPC NettyServerHandler 在处理完消息头之后需要缓存上下文，以便后续处理消息体时使用；

- onDataRead 和 onHeadersRead 方法都是由 Netty 的 NIO 线程负责调度，但是在执行 onDataRead 的过程中发生了线程切换，如下所示（ServerTransportListenerImpl 类）：

  ```
  wrappedExecutor.execute(new ContextRunnable(context) {
         @Override
         public void runInContext() {
           ServerStreamListener listener = NOOP_LISTENER;
           try {
             ServerMethodDefinition<?, ?> method = registry.lookupMethod(methodName);
             if (method == null) {
               method = fallbackRegistry.lookupMethod(methodName, stream.getAuthority());
             }
  ```

  因此，实际上它们是并行 + 交叉串行实行的，后续章节介绍线程模型时会介绍切换原则。

#### 2.3.3 内部的服务路由和调用

内部的服务路由和调用，主要包括如下几个步骤：

- 将请求消息体反序列为 Java 的 POJO 对象，即 IDL 中定义的请求参数对象；
- 根据请求消息头中的方法名到注册中心查询到对应的服务定义信息；
- 通过 Java 本地接口调用方式，调用服务端启动时注册的 IDL 接口实现类。

具体流程如下所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-01-08.png)

中间的交互流程比较复杂，涉及的类较多，但是关键步骤主要有三个：

- **解码：**对 HTTP/2 Body 进行应用层解码，转换成服务端接口的请求参数，解码的关键就是调用 requestMarshaller.parse(input)，将 PB 码流转换成 Java 对象；
- **路由：**根据 URL 中的方法名从内部服务注册中心查询到对应的服务实例，路由的关键是调用 registry.lookupMethod(methodName) 获取到 ServerMethodDefinition 对象；
- **调用：**调用服务端接口实现类的指定方法，实现 RPC 调用，与一些 RPC 框架不同的是，此处调用是 Java 本地接口调用，非反射调用，性能更优，它的实现关键是 UnaryRequestMethod.invoke(request, responseObserver) 方法。

#### 2.3.4 响应消息发送

响应消息的发送由 StreamObserver 的 onNext 触发，流程如下所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-01-09.png)

响应消息的发送原理如下：

- 分别发送 gRPC HTTP/2 响应消息头和消息体，由 NettyServerStream 的 Sink 将响应消息封装成 SendResponseHeadersCommand 和 SendGrpcFrameCommand，加入到 WriteQueue 中；
- WriteQueue 通过 Netty 的 NioEventLoop 线程进行消息处理，NioEventLoop 将 SendResponseHeadersCommand 和 SendGrpcFrameCommand 写入到 Netty 的 Channel 中，进而触发 DefaultChannelPipeline 的
  write(Object msg, ChannelPromise promise) 操作；
- 响应消息通过 ChannelPipeline 职责链进行调度，触发 NettyServerHandler 的 sendResponseHeaders 和 sendGrpcFrame 方法，调用 Http2ConnectionEncoder 的 writeHeaders 和 writeData 方法，将响应消息通过 Netty 的 HTTP/2 协议栈发送给客户端。

需要指出的是，请求消息的接收、服务调用以及响应消息发送，多次发生 NIO 线程和应用线程之间的互相切换，以及并行处理。因此上述流程中的一些步骤，并不是严格按照图示中的顺序执行的，后续线程模型章节，会做分析和介绍。

## 3. 源码分析

### 3.1 主要类和功能交互流程

#### 3.1.1 gRPC 请求消息头处理

![img](http://mininft.xyz/assets/images/post/20181013/grpc-01-10.png)

gRPC 请求消息头处理涉及的主要类库如下：

- **NettyServerHandler：**gRPC Netty Server 的 ChannelHandler 实现，负责 HTTP/2 请求消息和响应消息的处理；
- **SerializingExecutor：**应用调用线程池，负责 RPC 请求消息的解码、响应消息编码以及服务接口的调用等；
- **MessageDeframer：**负责请求 Framer 的解析，主要用于处理 HTTP/2 Header 和 Body 的读取；
- **ServerCallHandler：**真正的服务接口处理类，提供 onMessage(ReqT request) 和 onHalfClose() 方法，用于服务接口的调用。

#### 3.1.2 gRPC 请求消息体处理和服务调用

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-01-11.png)

#### 3.1.3 gRPC 响应消息处理

![image-20221222161224509](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/image-20221222161224509.png)

需要说明的是，响应消息的发送由调用服务端接口的应用线程执行，在本示例中，由 SerializingExecutor 进行调用。
当请求消息头被封装成 SendResponseHeadersCommand 并被插入到 WriteQueue 之后，后续操作由 Netty 的 NIO 线程 NioEventLoop 负责处理。
应用线程继续发送响应消息体，将其封装成 SendGrpcFrameCommand 并插入到 WriteQueue 队列中，由 Netty 的 NIO 线程 NioEventLoop 处理。响应消息的发送严格按照顺序：即先消息头，后消息体。

### 3.2 源码分析

了解 gRPC 服务端消息接入和 service 调用流程之后，针对主要的流程和类库，进行源码分析，以加深对 gRPC 服务端工作原理的了解。

#### 3.2.1 Netty 服务端创建

基于 Netty 的 HTTP/2 协议栈，构建 gRPC 服务端，Netty HTTP/2 协议栈初始化代码如下所示（创建 NettyServerHandler，NettyServerHandler 类）：

```
 frameWriter = new WriteMonitoringFrameWriter(frameWriter, keepAliveEnforcer);
    Http2ConnectionEncoder encoder = new DefaultHttp2ConnectionEncoder(connection, frameWriter);
    Http2ConnectionDecoder decoder = new FixedHttp2ConnectionDecoder(connection, encoder,
        frameReader);
    Http2Settings settings = new Http2Settings();
    settings.initialWindowSize(flowControlWindow);
    settings.maxConcurrentStreams(maxStreams);
    settings.maxHeaderListSize(maxHeaderListSize);
    return new NettyServerHandler(
        transportListener, streamTracerFactories, decoder, encoder, settings, maxMessageSize,
        keepAliveTimeInNanos, keepAliveTimeoutInNanos,
        maxConnectionAgeInNanos, maxConnectionAgeGraceInNanos,
        keepAliveEnforcer);
```

创建 gRPC FrameListener，作为 Http2FrameListener，监听 HTTP/2 消息的读取，回调到 NettyServerHandler 中（NettyServerHandler 类）：

```
decoder().frameListener(new FrameListener());
```

将 NettyServerHandler 添加到 Netty 的 ChannelPipeline 中，接收和发送 HTTP/2 消息（NettyServerTransport 类）：

```
ChannelHandler negotiationHandler = protocolNegotiator.newHandler(grpcHandler);
    channel.pipeline().addLast(negotiationHandler);
```

gRPC 服务端请求和响应消息统一由 NettyServerHandler 拦截处理，相关方法如下：

![image-20221222161232445](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/image-20221222161232445.png)

NettyServerHandler 是 gRPC 应用侧和底层协议栈的桥接类，负责将原生的 HTTP/2 消息调度到 gRPC 应用侧，同时将应用侧的消息发送到协议栈。

#### 3.2.2 服务实例创建和绑定

gRPC 服务端启动时，需要将调用的接口实现类实例注册到内部的服务注册中心，用于后续的接口调用，关键代码如下（InternalHandlerRegistry 类）：

```
Builder addService(ServerServiceDefinition service) {
      services.put(service.getServiceDescriptor().getName(), service);
      return this;
    }
```

服务接口绑定时，由 Proto3 工具生成代码，重载 bindService() 方法（GreeterImplBase 类）：

```
@java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_SAY_HELLO,
            asyncUnaryCall(
              new MethodHandlers<
                io.grpc.examples.helloworld.HelloRequest,
                io.grpc.examples.helloworld.HelloReply>(
                  this, METHODID_SAY_HELLO)))
          .build();
    }
```

#### 3.2.3 service 调用

##### gRPC 消息的接收

gRPC 消息的接入由 Netty HTTP/2 协议栈回调 gRPC 的 FrameListener，进而调用 NettyServerHandler 的 onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers) 和 onDataRead(int streamId, ByteBuf data, int padding, boolean endOfStream)，代码如下所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-01-14.png)

消息头和消息体的处理，主要由 MessageDeframer 的 deliver 方法完成，相关代码如下（MessageDeframer 类）：

```
if (inDelivery) {
     return;
   }
   inDelivery = true;
   try {
          while (pendingDeliveries > 0 && readRequiredBytes()) {
       switch (state) {
         case HEADER:
           processHeader();
           break;
         case BODY:
           processBody();
           pendingDeliveries--;
           break;
         default:
           throw new AssertionError("Invalid state: " + state);
```

gRPC 请求消息（PB）的解码由 PrototypeMarshaller 负责，代码如下 (ProtoLiteUtils 类)：

```
public T parse(InputStream stream) {
       if (stream instanceof ProtoInputStream) {
         ProtoInputStream protoStream = (ProtoInputStream) stream;
         if (protoStream.parser() == parser) {
           try {
             T message = (T) ((ProtoInputStream) stream).message();
...
```

##### gRPC 响应消息发送

响应消息分为两部分发送：响应消息头和消息体，分别被封装成不同的 WriteQueue.AbstractQueuedCommand，插入到 WriteQueue 中。

消息头封装代码（NettyServerStream 类）：

```
public void writeHeaders(Metadata headers) {
     writeQueue.enqueue(new SendResponseHeadersCommand(transportState(),
         Utils.convertServerHeaders(headers), false),
         true);
   }
```

消息体封装代码（NettyServerStream 类）：

```
ByteBuf bytebuf = ((NettyWritableBuffer) frame).bytebuf();
     final int numBytes = bytebuf.readableBytes();
     onSendingBytes(numBytes);
     writeQueue.enqueue(
         new SendGrpcFrameCommand(transportState(), bytebuf, false),
         channel.newPromise().addListener(new ChannelFutureListener() {
           @Override
           public void operationComplete(ChannelFuture future) throws Exception {
             transportState().onSentBytes(numBytes);
           }
         }), flush);
```

Netty 的 NioEventLoop 将响应消息发送到 ChannelPipeline，最终被 NettyServerHandler 拦截并处理。

响应消息头处理代码如下（NettyServerHandler 类）：

```
private void sendResponseHeaders(ChannelHandlerContext ctx, SendResponseHeadersCommand cmd,
     ChannelPromise promise) throws Http2Exception {
   int streamId = cmd.stream().id();
   Http2Stream stream = connection().stream(streamId);
   if (stream == null) {
     resetStream(ctx, streamId, Http2Error.CANCEL.code(), promise);
     return;
   }
   if (cmd.endOfStream()) {
     closeStreamWhenDone(promise, streamId);
   }
   encoder().writeHeaders(ctx, streamId, cmd.headers(), 0, cmd.endOfStream(), promise);
 }
```

响应消息体处理代码如下（NettyServerHandler 类）：

```
private void sendGrpcFrame(ChannelHandlerContext ctx, SendGrpcFrameCommand cmd,
     ChannelPromise promise) throws Http2Exception {
   if (cmd.endStream()) {
     closeStreamWhenDone(promise, cmd.streamId());
   }
   encoder().writeData(ctx, cmd.streamId(), cmd.content(), 0, cmd.endStream(), promise);
 }
```

\##### 服务接口实例调用： 经过一系列预处理，最终由 ServerCalls 的 ServerCallHandler 调用服务接口实例，代码如下（ServerCalls 类）：

```
 return new EmptyServerCallListener<ReqT>() {
         ReqT request;
         @Override
         public void onMessage(ReqT request) {
           this.request = request;
         }
         @Override
         public void onHalfClose() {
           if (request != null) {
             method.invoke(request, responseObserver);
             responseObserver.freeze();
             if (call.isReady()) {
               onReady();
             }
```

最终的服务实现类调用如下（GreeterGrpc 类）：

```
public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
     switch (methodId) {
       case METHODID_SAY_HELLO:
         serviceImpl.sayHello((io.grpc.examples.helloworld.HelloRequest) request,
 (io.grpc.stub.StreamObserver<io.grpc.examples.helloworld.HelloReply>) responseObserver);
         break;
       default:
         throw new AssertionError();
     }
```

### 3.3 服务端线程模型

gRPC 的线程由 Netty 线程 + gRPC 应用线程组成，它们之间的交互和切换比较复杂，下面做下详细介绍。

#### 3.3.1 Netty Server 线程模型

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-01-15.png)

它的工作流程总结如下：

- 从主线程池（bossGroup）中随机选择一个 Reactor 线程作为 Acceptor 线程，用于绑定监听端口，接收客户端连接；
- Acceptor 线程接收客户端连接请求之后创建新的 SocketChannel，将其注册到主线程池（bossGroup）的其它 Reactor 线程上，由其负责接入认证、握手等操作；
- 步骤 2 完成之后，应用层的链路正式建立，将 SocketChannel 从主线程池的 Reactor 线程的多路复用器上摘除，重新注册到 Sub 线程池（workerGroup）的线程上，用于处理 I/O 的读写操作。

Netty Server 使用的 NIO 线程实现是 NioEventLoop，它的职责如下：

- 作为服务端 Acceptor 线程，负责处理客户端的请求接入；
- 作为客户端 Connecor 线程，负责注册监听连接操作位，用于判断异步连接结果；
- 作为 I/O 线程，监听网络读操作位，负责从 SocketChannel 中读取报文；
- 作为 I/O 线程，负责向 SocketChannel 写入报文发送给对方，如果发生写半包，会自动注册监听写事件，用于后续继续发送半包数据，直到数据全部发送完成；
- 作为定时任务线程，可以执行定时任务，例如链路空闲检测和发送心跳消息等；
- 作为线程执行器可以执行普通的任务 Task（Runnable）。

#### 3.3.2 gRPC service 线程模型

gRPC 服务端调度线程为 SerializingExecutor，它实现了 Executor 和 Runnable 接口，通过外部传入的 Executor 对象，调度和处理 Runnable，同时内部又维护了一个任务队列 ConcurrentLinkedQueue，通过 run 方法循环处理队列中存放的 Runnable 对象，代码示例如下：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-01-16.png)

#### 3.3.3 线程调度和切换策略

Netty Server I/O 线程的职责：

- gRPC 请求消息的读取、响应消息的发送
- HTTP/2 协议消息的编码和解码
- NettyServerHandler 的调度

gRPC service 线程的职责：

- 将 gRPC 请求消息（PB 码流）反序列化为接口的请求参数对象
- 将接口响应对象序列化为 PB 码流
- gRPC 服务端接口实现类调用

gRPC 的线程模型遵循 Netty 的线程分工原则，即：协议层消息的接收和编解码由 Netty 的 I/O(NioEventLoop) 线程负责；后续应用层的处理由应用线程负责，防止由于应用处理耗时而阻塞 Netty 的 I/O 线程。

基于上述分工原则，在 gRPC 请求消息的接入和响应发送过程中，系统不断的在 Netty I/O 线程和 gRPC 应用线程之间进行切换。明白了分工原则，也就能够理解为什么要做频繁的线程切换。

gRPC 线程模型存在的一个缺点，就是在一次 RPC 调用过程中，做了多次 I/O 线程到应用线程之间的切换，频繁切换会导致性能下降，这也是为什么 gRPC 性能比一些基于私有协议构建的 RPC 框架性能低的一个原因。尽管 gRPC 的性能已经比较优异，但是仍有一定的优化空间。