## 1. gRPC 客户端创建流程

### 1.1 背景

gRPC 是在 HTTP/2 之上实现的 RPC 框架，HTTP/2 是第 7 层（应用层）协议，它运行在 TCP（第 4 层 - 传输层）协议之上，相比于传统的 REST/JSON 机制有诸多的优点：

- 基于 HTTP/2 之上的二进制协议（Protobuf 序列化机制）；
- 一个连接上可以多路复用，并发处理多个请求和响应；
- 多种语言的类库实现；
- 服务定义文件和自动代码生成（.proto 文件和 Protobuf 编译工具）。

此外，gRPC 还提供了很多扩展点，用于对框架进行功能定制和扩展，例如，通过开放负载均衡接口可以无缝的与第三方组件进行集成对接（Zookeeper、域名解析服务、SLB 服务等）。

一个完整的 RPC 调用流程示例如下：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-01.png)

gRPC 的 RPC 调用与上述流程相似，下面我们一起学习下 gRPC 的客户端创建和服务调用流程。

### 1.2 业务代码示例

以 gRPC 入门级的 helloworld Demo 为例，客户端发起 RPC 调用的代码主要包括如下几部分：

- 根据 hostname 和 port 创建 ManagedChannelImpl；
- 根据 helloworld.proto 文件生成的 GreeterGrpc 创建客户端 Stub，用来发起 RPC 调用；
- 使用客户端 Stub（GreeterBlockingStub）发起 RPC 调用，获取响应。

相关示例代码如下所示（HelloWorldClient 类）：

```
HelloWorldClient(ManagedChannelBuilder<?> channelBuilder) {
    channel = channelBuilder.build();
    blockingStub = GreeterGrpc.newBlockingStub(channel);
    futureStub = GreeterGrpc.newFutureStub(channel);
    stub = GreeterGrpc.newStub(channel);
  }
  public void blockingGreet(String name) {
    logger.info("Will try to greet " + name + " ...");
    HelloRequest request = HelloRequest.newBuilder().setName(name).build();
    try {
      HelloReply response = blockingStub
              .sayHello(request);
...
```

### 1.3 RPC 调用流程

gRPC 的客户端调用主要包括基于 Netty 的 HTTP/2 客户端创建、客户端负载均衡、请求消息的发送和响应接收处理四个流程。

#### 1.3.1 客户端调用总体流程

gRPC 的客户端调用总体流程如下图所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-02.png)

gRPC 的客户端调用流程如下：

- 客户端 Stub(GreeterBlockingStub) 调用 sayHello(request)，发起 RPC 调用；
- 通过 DnsNameResolver 进行域名解析，获取服务端的地址信息（列表），随后使用默认的 LoadBalancer 策略，选择一个具体的 gRPC 服务端实例；
- 如果与路由选中的服务端之间没有可用的连接，则创建 NettyClientTransport 和 NettyClientHandler，发起 HTTP/2 连接；
- 对请求消息使用 PB（Protobuf）做序列化，通过 HTTP/2 Stream 发送给 gRPC 服务端；
- 接收到服务端响应之后，使用 PB（Protobuf）做反序列化；
- 回调 GrpcFuture 的 set(Response) 方法，唤醒阻塞的客户端调用线程，获取 RPC 响应。

需要指出的是，客户端同步阻塞 RPC 调用阻塞的是调用方线程（通常是业务线程），底层 Transport 的 I/O 线程（Netty 的 NioEventLoop）仍然是非阻塞的。

#### 1.3.2 ManagedChannel 创建流程

ManagedChannel 是对 Transport 层 SocketChannel 的抽象，Transport 层负责协议消息的序列化和反序列化，以及协议消息的发送和读取。

ManagedChannel 将处理后的请求和响应传递给与之相关联的 ClientCall 进行上层处理，同时，ManagedChannel 提供了对 Channel 的生命周期管理（链路创建、空闲、关闭等）。

ManagedChannel 提供了接口式的切面 ClientInterceptor，它可以拦截 RPC 客户端调用，注入扩展点，以及功能定制，方便框架的使用者对 gRPC 进行功能扩展。

ManagedChannel 的主要实现类 ManagedChannelImpl 创建流程如下：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-03.png)

流程关键技术点解读：

- 使用 builder 模式创建 ManagedChannelBuilder 实现类 NettyChannelBuilder，NettyChannelBuilder 提供了 buildTransportFactory 工厂方法创建 NettyTransportFactory，最终用于创建 NettyClientTransport；
- 初始化 HTTP/2 连接方式：采用 plaintext 协商模式还是默认的 TLS 模式，HTTP/2 的连接有两种模式，h2（基于 TLS 之上构建的 HTTP/2）和 h2c（直接在 TCP 之上构建的 HTTP/2）；
- 创建 NameResolver.Factory 工厂类，用于服务端 URI 的解析，gRPC 默认采用 DNS 域名解析方式。

ManagedChannel 实例构造完成之后，即可创建 ClientCall，发起 RPC 调用。

#### 1.3.3 ClientCall 创建流程

完成 ManagedChannelImpl 创建之后，由 ManagedChannelImpl 发起创建一个新的 ClientCall 实例。ClientCall 的用途是业务应用层的消息调度和处理，它的典型用法如下：

```
 call = channel.newCall(unaryMethod, callOptions);
 call.start(listener, headers);
 call.sendMessage(message);
 call.halfClose();
 call.request(1);
 // wait for listener.onMessage()
```

ClientCall 实例的创建流程如下所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-04.png)

流程关键技术点解读：

- ClientCallImpl 的主要构造参数是 MethodDescriptor 和 CallOptions，其中 MethodDescriptor 存放了需要调用 RPC 服务的接口名、方法名、服务调用的方式（例如 UNARY 类型）以及请求和响应的序列化和反序列化实现类。
  CallOptions 则存放了 RPC 调用的其它附加信息，例如超时时间、鉴权信息、消息长度限制和执行客户端调用的线程池等。
- 设置压缩和解压缩的注册类（CompressorRegistry 和 DecompressorRegistry），以便可以按照指定的压缩算法对 HTTP/2 消息做压缩和解压缩。

ClientCallImpl 实例创建完成之后，就可以调用 ClientTransport，创建 HTTP/2 Client，向 gRPC 服务端发起远程服务调用。

#### 1.3.4 基于 Netty 的 HTTP/2 Client 创建流程

gRPC 客户端底层基于 Netty4.1 的 HTTP/2 协议栈框架构建，以便可以使用 HTTP/2 协议来承载 RPC 消息，在满足标准化规范的前提下，提升通信性能。

gRPC HTTP/2 协议栈（客户端）的关键实现是 NettyClientTransport 和 NettyClientHandler，客户端初始化流程如下所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-05.png)

流程关键技术点解读：

- **NettyClientHandler 的创建：**级联创建 Netty 的 Http2FrameReader、Http2FrameWriter 和 Http2Connection，用于构建基于 Netty 的 gRPC HTTP/2 客户端协议栈。

- HTTP/2 Client 启动：

  仍然基于 Netty 的 Bootstrap 来初始化并启动客户端，但是有两个细节需要注意：

  - NettyClientHandler（实际被包装成 ProtocolNegotiator.Handler，用于 HTTP/2 的握手协商）创建之后，不是由传统的 ChannelInitializer 在初始化 Channel 时将 NettyClientHandler 加入到 pipeline 中，而是直接通过 Bootstrap 的 handler 方法直接加入到 pipeline 中，以便可以立即接收发送任务。
  - 客户端使用的 work 线程组并非通常意义的 EventLoopGroup，而是一个 EventLoop：即 HTTP/2 客户端使用的 work 线程并非一组线程（默认线程数为 CPU 内核 * 2），而是一个 EventLoop 线程。这个其实也很容易理解，一个 NioEventLoop 线程可以同时处理多个 HTTP/2 客户端连接，它是多路复用的，对于单个 HTTP/2 客户端，如果默认独占一个 work 线程组，将造成极大的资源浪费，同时也可能会导致句柄溢出（并发启动大量 HTTP/2 客户端）。

- **WriteQueue 创建：**Netty 的 NioSocketChannel 初始化并向 Selector 注册之后（发起 HTTP 连接之前），立即由 NettyClientHandler 创建 WriteQueue，用于接收并处理 gRPC 内部的各种 Command，例如链路关闭指令、发送 Frame 指令、发送 Ping 指令等。

HTTP/2 Client 创建完成之后，即可由客户端根据协商策略发起 HTTP/2 连接。如果连接创建成功，后续即可复用该 HTTP/2 连接，进行 RPC 调用。

#### 1.3.5 HTTP/2 连接创建流程

HTTP/2 在 TCP 连接之初通过协商的方式进行通信，只有协商成功，才能进行后续的业务层数据发送和接收。

HTTP/2 的版本标识分为两类：

- 基于 TLS 之上构架的 HTTP/2, 即 HTTPS，使用 h2 表示（ALPN）：0x68 与 0x32；
- 直接在 TCP 之上构建的 HTTP/2, 即 HTTP，使用 h2c 表示。

HTTP/2 连接创建，分为两种：通过协商升级协议方式和直接连接方式。

假如不知道服务端是否支持 HTTP/2，可以先使用 HTTP/1.1 进行协商，客户端发送协商请求消息（只含消息头），报文示例如下：

```
GET / HTTP/1.1
Host: 127.0.0.1
Connection: Upgrade, HTTP2-Settings
Upgrade: h2c
HTTP2-Settings: <base64url encoding of HTTP/2 SETTINGS payload>
```

服务端接收到协商请求之后，如果不支持 HTTP/2，则直接按照 HTTP/1.1 响应返回，双方通过 HTTP/1.1 进行通信，报文示例如下：

```
HTTP/1.1 200 OK
Content-Length: 28
Content-Type: text/css

body...
```

如果服务端支持 HTTP/2, 则协商成功，返回 101 结果码，通知客户端一起升级到 HTTP/2 进行通信，示例报文如下：

```
HTTP/1.1 101 Switching Protocols
Connection: Upgrade
Upgrade: h2c

[ HTTP/2 connection...
```

101 响应之后，服务需要发送 SETTINGS 帧作为连接序言，客户端接收到 101 响应之后，也必须发送一个序言作为回应，示例如下：

```
PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n
SETTINGS 帧
```

客户端序言发送完成之后，可以不需要等待服务端的 SETTINGS 帧，而直接发送业务请求 Frame。

假如客户端和服务端已经约定使用 HTTP/2, 则可以免去 101 协商和切换流程，直接发起 HTTP/2 连接，具体流程如下所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-06.png)

几个关键点：

- 如果已经明确知道服务端支持 HTTP/2，则可免去通过 HTTP/1.1 101 协议切换方式进行升级，TCP 连接建立之后即可发送序言，否则只能在接收到服务端 101 响应之后发送序言；
- 针对一个连接，服务端第一个要发送的帧必须是 SETTINGS 帧，连接序言所包含的 SETTINGS 帧可以为空；
- 客户端可以在发送完序言之后发送应用帧数据，不用等待来自服务器端的序言 SETTINGS 帧。

gRPC 支持三种 Protocol Negotiator 策略：

- **PlaintextNegotiator：**明确服务端支持 HTTP/2，采用 HTTP 直接连接的方式与服务端建立 HTTP/2 连接，省去 101 协议切换过程；
- **PlaintextUpgradeNegotiator：**不清楚服务端是否支持 HTTP/2，采用 HTTP/1.1 协商模式切换升级到 HTTP/2；
- **TlsNegotiator：**在 TLS 之上构建 HTTP/2，协商采用 ALPN 扩展协议，以 “h2” 作为协议标识符。

下面我们以 PlaintextNegotiator 为例，了解下基于 Netty 的 HTTP/2 连接创建流程：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-07.png)

#### 1.3.6 负载均衡策略

总体上看，RPC 的负载均衡策略有两大类：

- 服务端负载均衡（例如代理模式、外部负载均衡服务）
- 客户端负载均衡（内置负载均衡策略和算法，客户端实现）

外部负载均衡模式如下所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-08.png)

以代理 LB 模式为例：RPC 客户端向负载均衡代理发送请求，负载均衡代理按照指定的路由策略，将请求消息转发到后端可用的服务实例上。负载均衡代理负责维护后端可用的服务列表，如果发现某个服务不可用，则将其剔除出路由表。

代理 LB 模式的优点是客户端不需要实现负载均衡策略算法，也不需要维护后端的服务列表信息，不直接跟后端的服务进行通信，在做网络安全边界隔离时，非常实用。例如通过 Nginx 做 L7 层负载均衡，将互联网前端的流量安全的接入到后端服务中。

代理 LB 模式通常支持 L4（Transport）和 L7（Application) 层负载均衡，两者各有优缺点，可以根据 RPC 的协议特点灵活选择。L4/L7 层负载均衡对应场景如下：

- **L4 层：**对时延要求苛刻、资源损耗少、RPC 本身采用私有 TCP 协议；
- **L7 层：**有会话状态的连接、HTTP 协议簇（例如 Restful）。

客户端负载均衡策略由客户端内置负载均衡能力，通过静态配置、域名解析服务（例如 DNS 服务）、订阅发布（例如 Zookeeper 服务注册中心）等方式获取 RPC 服务端地址列表，并将地址列表缓存到客户端内存中。

每次 RPC 调用时，根据客户端配置的负载均衡策略由负载均衡算法从缓存的服务地址列表中选择一个服务实例，发起 RPC 调用。

客户端负载均衡策略工作原理示例如下：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-09.png)

gRPC 默认采用客户端负载均衡策略，同时提供了扩展机制，使用者通过自定义实现 NameResolver 和 LoadBalancer，即可覆盖 gRPC 默认的负载均衡策略，实现自定义路由策略的扩展。

gRPC 提供的负载均衡策略实现类如下所示：

- **PickFirstBalancer：**无负载均衡能力，即使有多个服务端地址可用，也只选择第一个地址；
- **RoundRobinLoadBalancer：**“RoundRobin” 负载均衡策略。

gRPC 负载均衡流程如下所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-10.png)

流程关键技术点解读：

- 负载均衡功能模块的输入是客户端指定的 hostName、需要调用的接口名和方法名等参数，输出是执行负载均衡算法后获得的 NettyClientTransport，通过 NettyClientTransport 可以创建基于 Netty HTTP/2 的 gRPC 客户端，发起 RPC 调用；
- gRPC 系统默认提供的是 DnsNameResolver，它通过 InetAddress.getAllByName(host) 获取指定 host 的 IP 地址列表（本地 DNS 服务），对于扩展者而言，可以继承 NameResolver 实现自定义的地址解析服务，例如使用 Zookeeper 替换 DnsNameResolver，把 Zookeeper 作为动态的服务地址配置中心，它的伪代码示例如下：

**第一步：**继承 NameResolver，实现 start(Listener listener) 方法：

```
void start(Listener listener)
{
 // 获取 ZooKeeper 地址，并连接
 // 创建 Watcher，并实现 process(WatchedEvent event)，监听地址变更
 // 根据接口名和方法名，调用 getChildren 方法，获取发布该服务的地址列表
// 将地址列表加到 List 中
// 调用 NameResolver.Listener.onAddresses(), 通知地址解析完成
```

**第二步：**创建 ManagedChannelBuilder 时，指定 Target 的地址为 Zookeeper 服务端地址，同时设置 nameResolver 为 Zookeeper NameResolver, 示例代码如下所示：

```
this(ManagedChannelBuilder.forTarget(zookeeperAddr)
        .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
        .nameResolverFactory(new ZookeeperNameResolverProvider())
        .usePlaintext(false));
```

- LoadBalancer 负责从 nameResolver 中解析获得的服务端 URL 中按照指定路由策略，选择一个目标服务端地址，并创建 ClientTransport。同样，可以通过覆盖 handleResolvedAddressGroups 实现自定义负载均衡策略。

通过 LoadBalancer + NameResolver，可以实现灵活的负载均衡策略扩展。例如基于 Zookeeper、etcd 的分布式配置服务中心方案。

#### 1.3.7 RPC 请求消息发送流程

gRPC 默认基于 Netty HTTP/2 + PB 进行 RPC 调用，请求消息发送流程如下所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-11.png)

流程关键技术点解读：

- ClientCallImpl 的 sendMessage 调用，主要完成了请求对象的序列化（基于 PB）、HTTP/2 Frame 的初始化；
- ClientCallImpl 的 halfClose 调用将客户端准备就绪的请求 Frame 封装成自定义的 SendGrpcFrameCommand，写入到 WriteQueue 中；
- WriteQueue 执行 flush() 将 SendGrpcFrameCommand 写入到 Netty 的 Channel 中，调用 Channel 的 write 方法，被 NettyClientHandler 拦截到，由 NettyClientHandler 负责具体的发送操作；
- NettyClientHandler 调用 Http2ConnectionEncoder 的 writeData 方法，将 Frame 写入到 HTTP/2 Stream 中，完成请求消息的发送。

#### 1.3.8 RPC 响应接收和处理流程

gRPC 客户端响应消息的接收入口是 NettyClientHandler，它的处理流程如下所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-12.png)

流程关键技术点解读：

- NettyClientHandler 的 onHeadersRead(int streamId, Http2Headers headers, boolean endStream) 方法会被调用两次，根据 endStream 判断是否是 Stream 结尾；
- 请求和响应的关联：根据 streamId 可以关联同一个 HTTP/2 Stream，将 NettyClientStream 缓存到 Stream 中，客户端就可以在接收到响应消息头或消息体时还原出 NettyClientStream，进行后续处理；
- RPC 客户端调用线程的阻塞和唤醒使用到了 GrpcFuture 的 wait 和 notify 机制，来实现客户端调用线程的同步阻塞和唤醒；
- 客户端和服务端的 HTTP/2 Header 和 Data Frame 解析共用同一个方法，即 MessageDeframer 的 deliver()。

## 2. 客户端源码分析

gRPC 客户端调用原理并不复杂，但是代码却相对比较繁杂。下面围绕关键的类库，对主要功能点进行源码分析。

### 2.1 NettyClientTransport 功能和源码分析

NettyClientTransport 的主要功能如下：

- 通过 start(Listener transportListener) 创建 HTTP/2 Client，并连接 gRPC 服务端；
- 通过 newStream(MethodDescriptor method, Metadata headers, CallOptions callOptions) 创建 ClientStream；
- 通过 shutdown() 关闭底层的 HTTP/2 连接。

以启动 HTTP/2 客户端为例进行讲解（NettyClientTransport 类）：

```
EventLoop eventLoop = group.next();
    if (keepAliveTimeNanos != KEEPALIVE_TIME_NANOS_DISABLED) {
      keepAliveManager = new KeepAliveManager(
          new ClientKeepAlivePinger(this), eventLoop, keepAliveTimeNanos, keepAliveTimeoutNanos,
          keepAliveWithoutCalls);
    }
    handler = NettyClientHandler.newHandler(lifecycleManager, keepAliveManager, flowControlWindow,
        maxHeaderListSize, Ticker.systemTicker(), tooManyPingsRunnable);
    HandlerSettings.setAutoWindow(handler);
    negotiationHandler = negotiator.newHandler(handler);
```

根据启动时配置的 HTTP/2 协商策略，以 NettyClientHandler 为参数创建 ProtocolNegotiator.Handler。

创建 Bootstrap，并设置 EventLoopGroup，需要指出的是，此处并没有使用 EventLoopGroup，而是它的一种实现类 EventLoop，原因在前文中已经说明，相关代码示例如下（NettyClientTransport 类）：

```
Bootstrap b = new Bootstrap();
    b.group(eventLoop);
    b.channel(channelType);
    if (NioSocketChannel.class.isAssignableFrom(channelType)) {
      b.option(SO_KEEPALIVE, true);
    }
```

创建 WriteQueue 并设置到 NettyClientHandler 中，用于接收内部的各种 QueuedCommand，初始化完成之后，发起 HTTP/2 连接，代码如下（NettyClientTransport 类）：

```
handler.startWriteQueue(channel);
    channel.connect(address).addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
          ChannelHandlerContext ctx = future.channel().pipeline().context(handler);
          if (ctx != null) {
            ctx.fireExceptionCaught(future.cause());
          }
          future.channel().pipeline().fireExceptionCaught(future.cause());
        }
```

### 2.2 NettyClientHandler 功能和源码分析

NettyClientHandler 继承自 Netty 的 Http2ConnectionHandler，是 gRPC 接收和发送 HTTP/2 消息的关键实现类，也是 gRPC 和 Netty 的交互桥梁，它的主要功能如下所示：

- 发送各种协议消息给 gRPC 服务端；
- 接收 gRPC 服务端返回的应答消息头、消息体和其它协议消息；
- 处理 HTTP/2 协议相关的指令，例如 StreamError、ConnectionError 等。

协议消息的发送：无论是业务请求消息，还是协议指令消息，都统一封装成 QueuedCommand，由 NettyClientHandler 拦截并处理，相关代码如下所示（NettyClientHandler 类）：

```
public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
          throws Exception {
    if (msg instanceof CreateStreamCommand) {
      createStream((CreateStreamCommand) msg, promise);
    } else if (msg instanceof SendGrpcFrameCommand) {
      sendGrpcFrame(ctx, (SendGrpcFrameCommand) msg, promise);
    } else if (msg instanceof CancelClientStreamCommand) {
      cancelStream(ctx, (CancelClientStreamCommand) msg, promise);
    } else if (msg instanceof SendPingCommand) {
      sendPingFrame(ctx, (SendPingCommand) msg, promise);
    } else if (msg instanceof GracefulCloseCommand) {
      gracefulClose(ctx, (GracefulCloseCommand) msg, promise);
    } else if (msg instanceof ForcefulCloseCommand) {
      forcefulClose(ctx, (ForcefulCloseCommand) msg, promise);
    } else if (msg == NOOP_MESSAGE) {
      ctx.write(Unpooled.EMPTY_BUFFER, promise);
    } else {
      throw new AssertionError("Write called for unexpected type: " + msg.getClass().getName());
    }
```

协议消息的接收：NettyClientHandler 通过向 Http2ConnectionDecoder 注册 FrameListener 来监听 RPC 响应消息和协议指令消息，相关接口如下：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-13.png)

FrameListener 回调 NettyClientHandler 的相关方法，实现协议消息的接收和处理：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-14.png)

需要指出的是，NettyClientHandler 并没有实现所有的回调接口，对于需要特殊处理的几个方法进行了重载，例如 onDataRead 和 onHeadersRead。

### 2.3 ProtocolNegotiator 功能和源码分析

ProtocolNegotiator 用于 HTTP/2 连接创建的协商，gRPC 支持三种策略并有三个实现子类：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-15.png)

gRPC 的 ProtocolNegotiator 实现类完全遵循 HTTP/2 相关规范，以 PlaintextUpgradeNegotiator 为例，通过设置 Http2ClientUpgradeCodec，用于 101 协商和协议升级，相关代码如下所示（PlaintextUpgradeNegotiator 类）：

```
public Handler newHandler(GrpcHttp2ConnectionHandler handler) {
      Http2ClientUpgradeCodec upgradeCodec = new Http2ClientUpgradeCodec(handler);
      HttpClientCodec httpClientCodec = new HttpClientCodec();
      final HttpClientUpgradeHandler upgrader =
          new HttpClientUpgradeHandler(httpClientCodec, upgradeCodec, 1000);
      return new BufferingHttp2UpgradeHandler(upgrader);
    }
```

### 2.4 LoadBalancer 功能和源码分析

LoadBalancer 负责客户端负载均衡，它是个抽象类，gRPC 框架的使用者可以通过继承的方式进行扩展。

gRPC 当前已经支持 PickFirstBalancer 和 RoundRobinLoadBalancer 两种负载均衡策略，未来不排除会提供更多的策略。

以 RoundRobinLoadBalancer 为例，它的工作原理如下：根据 PickSubchannelArgs 来选择一个 Subchannel（RoundRobinLoadBalancerFactory 类）：

```
public PickResult pickSubchannel(PickSubchannelArgs args) {
      if (size > 0) {
        return PickResult.withSubchannel(nextSubchannel());
      }
      if (status != null) {
        return PickResult.withError(status);
      }
      return PickResult.withNoResult();
    }
```

再看下 Subchannel 的选择算法（Picker 类）：

```
private Subchannel nextSubchannel() {
      if (size == 0) {
        throw new NoSuchElementException();
      }
      synchronized (this) {
        Subchannel val = list.get(index);
        index++;
        if (index >= size) {
          index = 0;
        }
        return val;
      }
    }
```

即通过顺序的方式从服务端列表中获取一个 Subchannel。
如果用户需要定制负载均衡策略，则可以在 RPC 调用时，使用如下代码（HelloWorldClient 类）：

```
this(ManagedChannelBuilder.forAddress(host, port).loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance()).nameResolverFactory(new ZkNameResolverProvider()) .usePlaintext(true));
```

### 2.5 ClientCalls 功能和源码分析

ClientCalls 提供了各种 RPC 调用方式，包括同步、异步、Streaming 和 Unary 方式等，相关方法如下所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/grpc-02-16.png)

下面一起看下 RPC 请求消息的发送和应答接收相关代码。

#### 2.5.1 RPC 请求调用源码分析

请求调用主要有两步：请求 Frame 构造和 Frame 发送，请求 Frame 构造代码如下所示（ClientCallImpl 类）：

```
public void sendMessage(ReqT message) {
    Preconditions.checkState(stream != null, "Not started");
    Preconditions.checkState(!cancelCalled, "call was cancelled");
    Preconditions.checkState(!halfCloseCalled, "call was half-closed");
    try {
      InputStream messageIs = method.streamRequest(message);
      stream.writeMessage(messageIs);
...
```

使用 PB 对请求消息做序列化，生成 InputStream，构造请求 Frame：

```
private int writeUncompressed(InputStream message, int messageLength) throws IOException {
    if (messageLength != -1) {
      statsTraceCtx.outboundWireSize(messageLength);
      return writeKnownLengthUncompressed(message, messageLength);
    }
    BufferChainOutputStream bufferChain = new BufferChainOutputStream();
    int written = writeToOutputStream(message, bufferChain);
    if (maxOutboundMessageSize >= 0 && written > maxOutboundMessageSize) {
      throw Status.INTERNAL
          .withDescription(
              String.format("message too large %d > %d", written , maxOutboundMessageSize))
          .asRuntimeException();
    }
    writeBufferChain(bufferChain, false);
    return written;
}
```

Frame 发送代码如下所示：

```
public void writeFrame(WritableBuffer frame, boolean endOfStream, boolean flush) {
      ByteBuf bytebuf = frame == null ? EMPTY_BUFFER : ((NettyWritableBuffer) frame).bytebuf();
      final int numBytes = bytebuf.readableBytes();
      if (numBytes > 0) {
        onSendingBytes(numBytes);
        writeQueue.enqueue(
            new SendGrpcFrameCommand(transportState(), bytebuf, endOfStream),
            channel.newPromise().addListener(new ChannelFutureListener() {
              @Override
              public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                  transportState().onSentBytes(numBytes);
                }
              }
            }), flush);
```

NettyClientHandler 接收到发送事件之后，调用 Http2ConnectionEncoder 将 Frame 写入 Netty HTTP/2 协议栈（NettyClientHandler 类）：

```
private void sendGrpcFrame(ChannelHandlerContext ctx, SendGrpcFrameCommand cmd,
      ChannelPromise promise) {
    encoder().writeData(ctx, cmd.streamId(), cmd.content(), 0, cmd.endStream(), promise);
  }
```

#### 2.5.2 RPC 响应接收和处理源码分析

响应消息的接收入口是 NettyClientHandler，包括 HTTP/2 Header 和 HTTP/2 DATA Frame 两部分，代码如下（NettyClientHandler 类）：

```
private void onHeadersRead(int streamId, Http2Headers headers, boolean endStream) {
    NettyClientStream.TransportState stream = clientStream(requireHttp2Stream(streamId));
    stream.transportHeadersReceived(headers, endStream);
    if (keepAliveManager != null) {
      keepAliveManager.onDataReceived();
    }
  }
```

如果参数 endStream 为 True，说明 Stream 已经结束，调用 transportTrailersReceived，通知 Listener close，代码如下所示（AbstractClientStream2 类）：

```
if (stopDelivery || isDeframerStalled()) {
        deliveryStalledTask = null;
        closeListener(status, trailers);
      } else {
        deliveryStalledTask = new Runnable() {
          @Override
          public void run() {
            closeListener(status, trailers);
          }
        };
      }
```

读取到 HTTP/2 DATA Frame 之后，调用 MessageDeframer 的 deliver 对 Frame 进行解析，代码如下（MessageDeframer 类）：

```
private void deliver() {
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
...
```

将 Frame 转换成 InputStream 之后，通知 ClientStreamListenerImpl，调用 messageRead(final InputStream message)，将 InputStream 反序列化为响应对象，相关代码如下所示（ClientStreamListenerImpl 类）：

```
public void messageRead(final InputStream message) {
      class MessageRead extends ContextRunnable {
        MessageRead() {
          super(context);
        }
        @Override
        public final void runInContext() {
          try {
            if (closed) {
              return;
            }
            try {
              observer.onMessage(method.parseResponse(message));
            } finally {
              message.close();
            }
```

当接收到 endOfStream 之后，通知 ClientStreamListenerImpl，调用它的 close 方法，如下所示（ClientStreamListenerImpl 类）：

```
private void close(Status status, Metadata trailers) {
      closed = true;
      cancelListenersShouldBeRemoved = true;
      try {
        closeObserver(observer, status, trailers);
      } finally {
        removeContextListenerAndCancelDeadlineFuture();
      }
    }
```

最终调用 UnaryStreamToFuture 的 onClose 方法，set 响应对象，唤醒阻塞的调用方线程，完成 RPC 调用，代码如下（UnaryStreamToFuture 类）：

```
public void onClose(Status status, Metadata trailers) {
      if (status.isOk()) {
        if (value == null) {
          responseFuture.setException(
              Status.INTERNAL.withDescription("No value received for unary call")
                  .asRuntimeException(trailers));
        }
        responseFuture.set(value);
      } else {
        responseFuture.setException(status.asRuntimeException(trailers));
      }
```