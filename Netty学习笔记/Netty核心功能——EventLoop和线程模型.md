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

在 Netty 中 EventLoop 可以理解为 Reactor 线程模型的事件处理引擎，每个 EventLoop 线程都维护一个 Selector 选择器和任务队列 taskQueue。它主要负责处理 I/O 事件、普通任务和定时任务。

### 2.1 事件处理机制

![image-20221025180852013](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221025180852013.png)

结合 Netty 的整体架构，来看下 EventLoop 的事件流转图，以便更好地理解 Netty EventLoop 的设计原理。NioEventLoop 的事件处理机制采用的是**无锁串行化的设计思路**。

- **BossEventLoopGroup** 和 **WorkerEventLoopGroup** 包含一个或者多个 NioEventLoop。BossEventLoopGroup 负责监听客户端的 Accept 事件，当事件触发时，将事件注册至 WorkerEventLoopGroup 中的一个 NioEventLoop 上。每新建一个 Channel， 只选择一个 NioEventLoop 与其绑定。所以说 Channel 生命周期的所有事件处理都是**线程独立**的，不同的 NioEventLoop 线程之间不会发生任何交集。
- NioEventLoop 完成数据读取后，会调用绑定的 ChannelPipeline 进行事件传播，ChannelPipeline 也是**线程安全**的，数据会被传递到 ChannelPipeline 的第一个 ChannelHandler 中。数据处理完成后，将加工完成的数据再传递给下一个 ChannelHandler，整个过程是**串行化**执行，不会发生线程上下文切换的问题。

NioEventLoop 无锁串行化的设计不仅使系统吞吐量达到最大化，而且降低了用户开发业务逻辑的难度，不需要花太多精力关心线程安全问题。虽然单线程执行避免了线程切换，但是它的缺陷就是**不能执行时间过长的 I/O 操作**，一旦某个 I/O 事件发生阻塞，那么后续的所有 I/O 事件都无法执行，甚至造成事件积压。在使用 Netty 进行程序开发时，我们**一定要对 ChannelHandler 的实现逻辑有充分的风险意识**。

NioEventLoop 线程的可靠性至关重要，一旦 NioEventLoop 发生阻塞或者陷入空轮询，就会导致整个系统不可用。在 JDK 中， Epoll 的实现是存在漏洞的，即使 Selector 轮询的事件列表为空，NIO 线程一样可以被唤醒，导致 CPU 100% 占用。这就是臭名昭著的 JDK epoll 空轮询的 Bug。Netty 作为一个高性能、高可靠的网络框架，需要保证 I/O 线程的安全性。那么它是如何解决 JDK epoll 空轮询的 Bug 呢？实际上 Netty 并没有从根源上解决该问题，而是巧妙地规避了这个问题。

我们抛开其他细枝末节，直接定位到事件轮询 select() 方法中的最后一部分代码，一起看下 Netty 是如何解决 epoll 空轮询的 Bug。

```lua
long time = System.nanoTime();
if (time - TimeUnit.MILLISECONDS.toNanos(timeoutMillis) >= currentTimeNanos) {
    selectCnt = 1;
} else if (SELECTOR_AUTO_REBUILD_THRESHOLD > 0 &&
        selectCnt >= SELECTOR_AUTO_REBUILD_THRESHOLD) {
    selector = selectRebuildSelector(selectCnt);
    selectCnt = 1;
    break;
}
```

`Netty` 提供了一种检测机制判断线程是否可能陷入空轮询，具体的实现方式如下：

1. 每次执行 `Select` 操作之前记录当前时间 `currentTimeNanos`。
2. `time - TimeUnit.MILLISECONDS.toNanos(timeoutMillis) >= currentTimeNanos`，如果事件轮询的持续时间大于等于 `timeoutMillis`，那么说明是正常的，否则表明阻塞时间并未达到预期，可能触发了空轮询的 Bug。
3. `Netty` 引入了计数变量 `selectCnt`。在正常情况下，`selectCnt` 会重置，否则会对 `selectCnt` 自增计数。当 `selectCnt` 达到 `SELECTOR_AUTO_REBUILD_THRESHOLD`（默认512） 阈值时，会触发重建 `Selector` 对象。

`Netty` 采用这种方法巧妙地规避了 `JDK Bug`。异常的 `Selector` 中所有的 `SelectionKey` 会重新注册到新建的 `Selector` 上，重建完成之后异常的 `Selector` 就可以废弃了。

### 2.2 任务处理机制

`NioEventLoop` 不仅负责处理 I/O 事件，还要兼顾执行任务队列中的任务。任务队列遵循 FIFO 规则，可以保证任务执行的公平性。`NioEventLoop` 处理的任务类型基本可以分为三类。

1. **普通任务**：通过 `NioEventLoop` 的 `execute`() 方法向任务队列 `taskQueue` 中添加任务。例如 Netty 在写数据时会封装 `WriteAndFlushTask` 提交给 `taskQueue`。`taskQueue` 的实现类是多生产者单消费者队列 `MpscChunkedArrayQueue`，在多线程并发添加任务时，可以保证线程安全。
2. **定时任务**：通过调用 `NioEventLoop` 的 `schedule`() 方法向定时任务队列 `scheduledTaskQueue` 添加一个定时任务，用于周期性执行该任务。例如，心跳消息发送等。定时任务队列 `scheduledTaskQueue` 采用优先队列 `PriorityQueue` 实现。
3. **尾部队列**：`tailTasks` 相比于普通任务队列优先级较低，在每次执行完 `taskQueue` 中任务后会去获取尾部队列中任务执行。尾部任务并不常用，主要用于做一些收尾工作，例如统计事件循环的执行时间、监控信息上报等。