# Netty

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

## 上手练习

你的第一个Telnet服务：[使用Netty创建Telnet服务](https://www.jianshu.com/p/5dcbc0456376)

示例仓库地址：https://github.com/yt-King/NettyDemo/tree/master

> 可能会遇到的问题：在命令行工具里可能会提示没有telnet命令，解决方法：[Windows系统开启telnet命令](https://help.aliyun.com/document_detail/40796.html)

## 第一个 Netty 应用

[《Netty 实战(精髓)》：第一个 Netty 应用 ](https://waylau.com/essential-netty-in-action/GETTING%20STARTED/Your%20first%20Netty%20application.html)

## Netty基本构建模块

### BOOTSTRAP

Netty 应用程序通过设置 bootstrap（引导）类的开始，该类提供了一个 用于应用程序网络层配置的容器。

### CHANNEL

底层网络传输 API 必须提供给应用 I/O操作的接口，如读，写，连接，绑定等等。对于我们来说，这是结构几乎总是会成为一个“socket”。 Netty 中的接口 Channel 定义了与 socket 丰富交互的操作集：bind, close, config, connect, isActive, isOpen, isWritable, read, write 等等。 Netty 提供大量的 Channel 实现来专门使用。这些包括 AbstractChannel，AbstractNioByteChannel，AbstractNioChannel，EmbeddedChannel， LocalServerChannel，NioSocketChannel 等等。

> Netty 的设计保证程序处理事件不会有同步：
>
> ![image-20221008143921075](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-9/image-20221008143921075.png)
>
> 该图显示，一个 EventLoopGroup 具有一个或多个 EventLoop。
>
> 当创建一个 Channel，Netty 通过 一个单独的 EventLoop 实例来注册该 Channel的使用寿命。这就是为什么你的应用程序不需要同步 Netty 的 I/O操作;所有 Channel 的 I/O 始终用相同的线程来执行。

### CHANNELHANDLER

ChannelHandler 支持很多协议，并且提供用于数据处理的容器。我们已经知道 ChannelHandler 由特定事件触发。 ChannelHandler 可用于几乎所有的动作，包括将一个对象转为字节（或相反），执行过程中抛出的异常处理。

常用的一个接口是 ChannelInboundHandler，这个类型接收到入站事件（包括接收到的数据）可以处理应用程序逻辑。当你需要提供响应时，你也可以从 ChannelInboundHandler 冲刷数据。一句话，业务逻辑经常存活于一个或者多个 ChannelInboundHandler。

### CHANNELPIPELINE

ChannelPipeline 提供了一个容器给 ChannelHandler 链并提供了一个API 用于管理沿着链入站和出站事件的流动。每个 Channel 都有自己的ChannelPipeline，当 Channel 创建时自动创建的。 ChannelHandler 是如何安装在 ChannelPipeline？ 主要是实现了ChannelHandler 的抽象 ChannelInitializer。ChannelInitializer子类 通过 ServerBootstrap 进行注册。当它的方法 initChannel() 被调用时，这个对象将安装自定义的 ChannelHandler 集到 pipeline。当这个操作完成时，ChannelInitializer 子类则 从 ChannelPipeline 自动删除自身。

### EVENTLOOP

EventLoop 用于处理 Channel 的 I/O 操作。一个单一的 EventLoop通常会处理多个 Channel 事件。一个 EventLoopGroup 可以含有多于一个的 EventLoop 和 提供了一种迭代用于检索清单中的下一个。

### CHANNELFUTURE

Netty 所有的 I/O 操作都是异步。因为一个操作可能无法立即返回，我们需要有一种方法在以后确定它的结果。出于这个目的，Netty 提供了接口 ChannelFuture,它的 addListener 方法注册了一个 ChannelFutureListener ，当操作完成时，可以被通知（不管成功与否）。

## 核心功能

