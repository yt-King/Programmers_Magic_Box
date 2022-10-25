# Netty核心功能——EventLoop和线程模型

> Netty 高性能的奥秘在于其 **Reactor 线程模型。** EventLoop 是 Netty Reactor 线程模型的核心处理引擎，那么它是如何高效地实现事件循环和任务处理机制的呢？

## 1、浅谈Reactor 线程模型

> 网络框架的设计离不开 I/O 线程模型，线程模型的优劣直接决定了系统的吞吐量、可扩展性、安全性等。目前主流的网络框架几乎都采用了 I/O 多路复用的方案。Reactor 模式作为其中的事件分发器，负责将读写事件分发给对应的读写事件处理者。大名鼎鼎的 Java 并发包作者 Doug Lea，在 ***Scalable I/O in Java*** 一文中阐述了服务端开发中 I/O 模型的演进过程。Netty 中**三种 Reactor 线程模型**也来源于这篇经典文章。
>
> 翻译版：[【译】Scalable IO in Java 可伸缩Java IO](https://www.jianshu.com/p/6a6fbf62e2e4)
>
> 原文地址：[Scalable I/O in Java](https://gee.cs.oswego.edu/dl/cpjslides/nio.pdf)

### 1.1 单线程模型

![image-20221025144859994](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221025144859994.png)

上图描述了 `Reactor` 的单线程模型结构，在 `Reactor` 单线程模型中，所有 I/O 操作（包括连接建立、数据读写、事件分发等），都是由**一个线程完成**的。它的处理流程如下：

1. Reactor对象通过select监控连接事件，收到事件后通过dispatch进行转发。
2. 如果是连接建立的事件，则由acceptor接受连接，并创建handler处理后续事件。
3. 如果不是建立连接事件，则Reactor会分发调用Handler来响应。
4. handler会完成read->业务处理->send的完整业务流程。

单线程模型逻辑简单，缺陷也十分明显：

1. 一个线程支持处理的连接数非常有限，CPU 很容易打满，性能方面有明显瓶颈；
2. 当多个事件被同时触发时，只要有一个事件没有处理完，其他后面的事件就无法执行，这就会造成消息积压及请求超时；
3. 线程在处理 I/O 事件时，Select 无法同时处理连接建立、事件分发等操作；
4. 如果 I/O 线程一直处于满负荷状态，很可能造成服务端节点不可用。

### 1.2 多线程模型

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221025145821306.png" alt="image-20221025145821306" style="zoom:70%;" />

由于单线程模型有性能方面的瓶颈，多线程模型作为解决方案就应运而生了。Reactor 多线程模型将业务逻辑交给多个线程进行处理。除此之外，多线程模型其他的操作与单线程模型是类似的，它的处理流程如下：

1. Reactor对象通过Select监控客户端请求事件，收到事件后通过dispatch进行分发。
2. 如果是建立连接请求事件，则由acceptor通过accept处理连接请求，然后创建一个Handler对象处理连接完成后续的各种事件。
3. 如果不是建立连接事件，则Reactor会分发调用连接对应的Handler来响应。
4. Handler只负责响应事件，不做具体业务处理，通过Read读取数据后，会分发给后面的Worker线程池进行业务处理。
5. Worker线程池会分配独立的线程完成真正的业务处理，如何将响应结果发给Handler进行处理。
6. Handler收到响应结果后通过send将响应结果返回给Client。

但是这个模型也存在如下问题：

1. 多线程数据共享和访问比较复杂。如果子线程完成业务处理后，把结果传递给主线程Reactor进行发送，就会涉及共享数据的互斥和保护机制。
2. Reactor承担所有事件的监听和响应，只在主线程中运行，可能会存在性能问题。例如并发百万客户端连接，或者服务端需要对客户端握手进行安全认证，但是认证本身非常损耗性能。

### 1.3 主从多线程模型

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221025150049001.png" alt="image-20221025150049001" style="zoom:70%;" />

主从多线程模型由多个 Reactor 线程组成，每个 Reactor 线程都有独立的 Selector 对象。MainReactor 仅负责处理客户端连接的 Accept 事件，连接建立成功后将新创建的连接对象注册至 SubReactor。再由 SubReactor 分配线程池中的 I/O 线程与其连接绑定，它将负责连接生命周期内所有的 I/O 事件。它的处理流程如下：

1. 从主线程池中随机选择一个Reactor线程作为acceptor线程，用于绑定监听端口，接收客户端连接
2. acceptor线程接收客户端连接请求之后创建新的SocketChannel，将其注册到主线程池的其它Reactor线程上，由其负责接入认证、IP黑白名单过滤、握手等操作
3. 步骤2完成之后，业务层的链路正式建立，将SocketChannel从主线程池的Reactor线程的多路复用器上摘除，重新注册到Sub线程池的线程上，并创建一个Handler用于处理各种连接事件
4. 当有新的事件发生时，SubReactor会调用连接对应的Handler进行响应
5. Handler通过Read读取数据后，会分发给后面的Worker线程池进行业务处理
6. Worker线程池会分配独立的线程完成真正的业务处理，如何将响应结果发给Handler进行处理
7. Handler收到响应结果后通过Send将响应结果返回给Client

## 2、EventLoop 接口

> EventLoop 这个概念其实并不是 Netty 独有的，它是一种**事件等待和处理的程序模型**，可以解决多线程资源消耗高的问题。例如 Node.js 就采用了 EventLoop 的运行机制，不仅占用资源低，而且能够支撑了大规模的流量访问。

下图展示了 EventLoop 通用的运行模式。每当事件发生时，应用程序都会将产生的事件放入事件队列当中，然后 EventLoop 会轮询从队列中取出事件执行或者将事件分发给相应的事件监听者执行。事件执行的方式通常分为**立即执行、延后执行、定期执行**几种。

![image-20221025152801874](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221025152801874.png)