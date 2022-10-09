# Netty基础

## 1、什么是Netty

> [Netty](https://netty.io/)——是一个异步事件驱动的网络应用程序框架。 Netty 的主要目的是构建基于 NIO（或者是 NIO.2）的高性能协议服务器，具有网络和业务逻辑组件的分离和松耦合。它可能实现广为人知的协议，例如 HTTP，或您自己的特定协议。

## 2、JDK 原生 NIO 程序的问题

**JDK 原生也有一套网络应用程序 API，但是存在一系列问题，主要如下：**

1）NIO 的类库和 API 繁杂，使用麻烦：你需要熟练掌握 Selector、ServerSocketChannel、SocketChannel、ByteBuffer 等。

2）需要具备其他的额外技能做铺垫：例如熟悉 Java 多线程编程，因为 NIO 编程涉及到 Reactor 模式，你必须对多线程和网路编程非常熟悉，才能编写出高质量的 NIO 程序。

3）可靠性能力补齐，开发工作量和难度都非常大：例如客户端面临断连重连、网络闪断、半包读写、失败缓存、网络拥塞和异常码流的处理等等。NIO 编程的特点是功能开发相对容易，但是可靠性能力补齐工作量和难度都非常大。

4）JDK NIO 的 Bug：例如臭名昭著的 Epoll Bug，它会导致 Selector 空轮询，最终导致 CPU 100%。

## 3、Netty 的特点

Netty 对 JDK 自带的 NIO 的 API 进行了封装，解决了上述问题。

**Netty的主要特点有：**

1）设计优雅：适用于各种传输类型的统一 API 阻塞和非阻塞 Socket；基于灵活且可扩展的事件模型，可以清晰地分离关注点；高度可定制的线程模型 - 单线程，一个或多个线程池；真正的无连接数据报套接字支持（自 3.1 起）。

2）使用方便：详细记录的 Javadoc，用户指南和示例；没有其他依赖项，JDK 5（Netty 3.x）或 6（Netty 4.x）就足够了。

3）高性能、吞吐量更高：延迟更低；减少资源消耗；最小化不必要的内存复制。

4）安全：完整的 SSL/TLS 和 StartTLS 支持。

5）社区活跃、不断更新：社区活跃，版本迭代周期短，发现的 Bug 可以被及时修复，同时，更多的新功能会被加入。

## 4、上手练习

你的第一个Telnet服务：[使用Netty创建Telnet服务](https://www.jianshu.com/p/5dcbc0456376)

示例仓库地址：https://github.com/yt-King/NettyDemo/tree/master

> 可能会遇到的问题：在命令行工具里可能会提示没有telnet命令，解决方法：[Windows系统开启telnet命令](https://help.aliyun.com/document_detail/40796.html)

## 5、第一个 Netty 应用

> 之前的练习只是简单的做了一个服务端，甚至连客户端都没有，接下来的这个是简易的但是是完整的一个例子。

[《Netty 实战(精髓)》：第一个 Netty 应用 ](https://waylau.com/essential-netty-in-action/GETTING%20STARTED/Your%20first%20Netty%20application.html)

示例仓库地址：https://github.com/yt-King/FirstNettyApplication

## 6、Netty基本构建模块

> Netty 是一个非阻塞框架。与阻塞 IO 相比，这会导致高吞吐量。了解非阻塞 IO 对于了解 Netty 的核心组件及其关系至关重要。

### BOOTSTRAP

Netty 应用程序通过设置 bootstrap（引导）类的开始，该类提供了一个 用于应用程序网络层配置的容器。有两种类型的引导：一种用于客户端（简单地称为 Bootstrap），而另一种 （ServerBootstrap）用于服务器。无论你的应用程序使用哪种协议或者处理哪种类型的数据， 唯一决定它使用哪种引导类的是它是作为一个客户端还是作为一个服务器。下表比较了这两种 类型的引导类：

| 类别                  | Bootstrap            | ServerBootstrap    |
| --------------------- | -------------------- | ------------------ |
| 网络编程中的作用      | 连接到远程主机和端口 | 绑定到一个本地端口 |
| EventLoopGroup 的数目 | 1                    | 2                  |

引导一个客户端只需要一个 EventLoopGroup，但是一个 ServerBootstrap 则需要两个（也可以是同一个实例）。为什么呢？

> 因为服务器需要两组不同的 Channel。第一组将只包含一个 ServerChannel，代表服务器自身的已绑定到某个本地端口的正在监听的套接字。而第二组将包含所有已创建的用来处理传入客户端连接（对于每个服务器已经接受的连接都有一个）的 Channel。下图说明了这个模 型，并且展示了为何需要两个不同的 EventLoopGroup：
>
> ![image-20221008161426106](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-9/image-20221008161426106.png)
>
> 与 ServerChannel 相关联的 EventLoopGroup 将分配一个负责为传入连接请求创建 Channel 的 EventLoop。一旦连接被接受，第二个 EventLoopGroup 就会给它的 Channel 分配一个 EventLoop

### CHANNEL

基本的 I/O 操作（bind()、connect()、read()和 write()）依赖于底层网络传输所提 供的原语。在基于 Java 的网络编程中，其基本的构造是 class Socket。Netty 的 Channel 接 口所提供的 API，大大地降低了直接使用 Socket 类的复杂性。此外，Channel 也是拥有许多 预定义的、专门化实现的广泛类层次结构的channel类。

### CHANNELHANDLER

ChannelHandler 支持很多协议，并且提供用于数据处理的容器。我们已经知道 ChannelHandler 由特定事件触发。 ChannelHandler 可用于几乎所有的动作，包括将一个对象转为字节（或相反），执行过程中抛出的异常处理。

常用的一个接口是 ChannelInboundHandler，这个类型接收到入站事件（包括接收到的数据）可以处理应用程序逻辑。当你需要提供响应时，你也可以从 ChannelInboundHandler 冲刷数据。一句话，业务逻辑经常存活于一个或者多个 ChannelInboundHandler。

### CHANNELPIPELINE

ChannelPipeline 提供了一个容器给 ChannelHandler 链并提供了一个API 用于管理沿着链入站和出站事件的流动。每个 Channel 都有自己的ChannelPipeline，当 Channel 创建时自动创建的。 ChannelHandler 是如何安装在 ChannelPipeline？ 主要是实现了ChannelHandler 的抽象 ChannelInitializer。ChannelInitializer子类 通过 ServerBootstrap 进行注册。当它的方法 initChannel() 被调用时，这个对象将安装自定义的 ChannelHandler 集到 pipeline。当这个操作完成时，ChannelInitializer 子类则 从 ChannelPipeline 自动删除自身。

### EVENTLOOP

EventLoop 定义了 Netty 的核心抽象，用于处理连接的生命周期中所发生的事件。

> 下图在高层次上说明了 Channel、EventLoop、Thread 以及 EventLoopGroup 之间的关系：
>
> ![image-20221008152944413](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-9/image-20221008152944413.png)
>
> - 一个 EventLoopGroup 包含一个或者多个 EventLoop；
> - 一个 EventLoop 在它的生命周期内只和一个 Thread 绑定；
> - 所有由 EventLoop 处理的 I/O 事件都将在它专有的 Thread 上被处理；
> - 一个 Channel 在它的生命周期内只注册于一个 EventLoop； 
> - 一个 EventLoop 可能会被分配给一个或多个 Channel。
>
> 在这种设计中，一个给定 Channel 的 I/O 操作都是由相同的 Thread 执行的，实际上**消除了对于同步的需要**

### CHANNELFUTURE

Netty 中所有的 I/O 操作都是异步的。因为一个操作可能不会 立即返回，所以我们需要一种用于在之后的某个时间点确定其结果的方法。为此，Netty 提供了 ChannelFuture 接口，其 addListener()方法注册了一个 ChannelFutureListener，以 便在某个操作完成时（无论是否成功）得到通知