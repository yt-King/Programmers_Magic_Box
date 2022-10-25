# Netty核心功能——通道（Channel）

> 在我们的代码中业务处理逻辑则由 `ChannelPipeline` 中所定义的 `ChannelHandler` 完成的，`ChannelPipeline` 和 `ChannelHandler` 也是我们在平时应用开发的过程中打交道最多的组件。Netty 服务编排层的核心组件 `ChannelPipeline` 和 `ChannelHandler` 为用户提供了 I/O 事件的全部控制权。
>
> 理解所有这些组件之间的交互对于通过 Netty 构建模块化的、可重用的实现至关重要。

## 1、ChannelHandler 

### 1.1 Channel 的生命周期

Channel 的正常生命周期如下图所示。当这些状态发生改变时，将会生成对应的事件。 这些事件将会被转发给 `ChannelPipeline` 中的 `ChannelHandler`，随后就可以对它们做出响应。

![image-20221021111603223](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221021111603223.png)

- ChannelUnregistered：Channel 已经被创建，但还未注册到 EventLoop
- ChannelRegistered：Channel 已经被注册到了 EventLoop
- ChannelActive：Channel 处于活动状态（已经连接到它的远程节点）。它现在可以接收和发送数据了
- ChannelInactive：Channel 没有连接到远程节点

### 1.2 ChannelHandler 的生命周期

下图列出了 `interface ChannelHandler` 定义的生命周期操作，在 `ChannelHandler` 被添加到 `ChannelPipeline` 中或者被从 `ChannelPipeline` 中移除时会调用这些操作。这些 方法中的每一个都接受一个 `ChannelHandlerContext` 参数。

![image-20221021112300728](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221021112300728.png)

Netty 定义了下面两个重要的 ChannelHandler 子接口：

- ChannelInboundHandler——处理入站数据以及各种状态变化；
- ChannelOutboundHandler——处理出站数据并且允许拦截所有的操作

#### ChannelInboundHandler 接口

![image-20221021150741488](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221021150741488.png)

#### ChannelOutboundHandler 接口

![image-20221021150803022](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221021150803022.png)

### 1.3 ChannelHandler 适配器

> 可以使用 `ChannelInboundHandlerAdapter` 和 `ChannelOutboundHandlerAdapter` 类作为自己的 `ChannelHandler` 的起始点。这两个适配器分别提供了 `ChannelInboundHandler` 和 `ChannelOutboundHandler` 的基本实现。通过扩展抽象类 `ChannelHandlerAdapter`，它们 获得了它们共同的超接口 `ChannelHandler` 的方法。

![image-20221024101046663](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221024101046663.png)

## 2、ChannelPipeline 

> `ChannelPipeline` 作为 Netty 的核心编排组件，负责调度各种类型的 `ChannelHandler`，实际数据的加工处理操作则是由 `ChannelHandler` 完成的。每一个新创建的 `Channel` 都将会被分配一个新的 `ChannelPipeline`。这项关联是永久性 的；`Channel` 既不能附加另外一个 `ChannelPipeline`，也不能分离其当前的。在 `Netty` 组件 的生命周期中，这是一项固定的操作，不需要开发人员的任何干预。

`ChannelPipeline` 可以看作是 `ChannelHandler` 的容器载体，它是由一组 `ChannelHandler` 实例组成的，内部通过双向链表将不同的 `ChannelHandler` 链接在一起，如下图所示。当有 I/O 读写事件触发时，`ChannelPipeline` 会依次调用 `ChannelHandler` 列表对 `Channel` 的数据进行拦截和处理。

![image-20221024105114565](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221024105114565.png)

由上图可知，每个 `Channel` 会绑定一个 `ChannelPipeline`，每一个 `ChannelPipeline` 都包含多个 `ChannelHandlerContext`，所有 `ChannelHandlerContext` 之间组成了双向链表。又因为每个 `ChannelHandler` 都对应一个 `ChannelHandlerContext`，所以实际上 `ChannelPipeline` 维护的是它与 `ChannelHandlerContext` 的关系。*那么可能就会有疑问，为什么这里会多一层 `ChannelHandlerContext` 的封装呢？*

其实这是一种比较常用的编程思想。`ChannelHandlerContext` 用于保存 `ChannelHandler` 上下文；`ChannelHandlerContext` 则包含了 `ChannelHandler` 生命周期的所有事件，如 `connect、bind、read、flush、write、close` 等。可以试想一下，如果没有 `ChannelHandlerContext` 的这层封装，那么我们在做 `ChannelHandler` 之间传递的时候，前置后置的通用逻辑就要在每个 `ChannelHandler` 里都实现一份。这样虽然能解决问题，但是代码结构的耦合，会非常不优雅。

根据网络数据的流向，`ChannelPipeline` 分为入站 `ChannelInboundHandler` 和出站 `ChannelOutboundHandler` 两种处理器。数据先由一系列 `InboundHandler` 处理后入站，然后再由相反方向的 `OutboundHandler` 处理完成后出站，如下图所示。我们经常使用的解码器 `Decoder` 就是入站操作，编码器 `Encoder` 就是出站操作。服务端接收到客户端数据需要先经过 `Decoder` 入站处理后，再通过 `Encoder` 出站通知客户端。

![image-20221025092916530](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221025092916530.png)

接下来我们分析一下 `ChannelPipeline` 双向链表的构造，`ChannelPipeline` 的双向链表分别维护了 `HeadContext` 和 `TailContext` 的头尾节点（这两个节点在 `Netty` 中已经默认实现了）。我们自定义的 `ChannelHandler` 会插入到 `Head` 和 `Tail` 之间，它们在 `ChannelPipeline` 中起到了非常重要的作用。首先来看下 `HeadContext` 和 `TailContext` 的继承关系，如下图所示：

![image-20221025093919245](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221025093919245.png)

1. `HeadContext` 既是 `Inbound` 处理器，也是 `Outbound` 处理器。因为它分别实现了 `ChannelInboundHandler` 和 `ChannelOutboundHandler`接口。网络数据写入操作的入口就是由 `HeadContext` 节点完成的。`HeadContext` 作为 `Pipeline` 的头结点负责读取数据并开始传递 `InBound` 事件，当数据处理完成后，数据会反方向经过 `Outbound` 处理器，最终传递到 `HeadContext`，所以 `HeadContext` 又是处理 `Outbound` 事件的最后一站。此外 `HeadContext` 在传递事件之前，还会执行一些前置操作。
2. `TailContext` 只实现了 `ChannelInboundHandler` 接口。它会在 `ChannelInboundHandler` 调用链路的最后一步执行，主要用于终止 `Inbound` 事件传播。`TailContext` 节点作为 `OutBound` 事件传播的第一站，仅仅是将 `OutBound` 事件传递给上一个节点。

## 3、ChannelHandlerContext

> `ChannelHandlerContext` 代表了 `ChannelHandler` 和 `ChannelPipeline` 之间的关 联，每当有 `ChannelHandler` 添加到 `ChannelPipeline` 中时，都会创建 `ChannelHandlerContext`。`ChannelHandlerContext` 的主要功能是管理它所关联的 `ChannelHandler` 和在 同一个 `ChannelPipeline` 中的其他 `ChannelHandler` 之间的交互。

`ChannelHandlerContext` 有很多的方法，其中一些方法也存在于 `Channel` 和 `ChannelPipeline` 本身上，但是有一点**重要的不同**：如果调用 `Channel` 或者 `ChannelPipeline` 上的这 些方法，它们将**沿着整个 `ChannelPipeline` 进行传播**。而调用位于 `ChannelHandlerContext` 上的相同方法，则将从当前所关联的 `ChannelHandler` 开始，并且**只会传播给位于该 `ChannelPipeline` 中的下一个能够处理该事件的 `ChannelHandler`**。

## 4、事件传播机制

接下来通过一个代码示例，一起体验下 ChannelPipeline 的事件传播机制：

```java
serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

    @Override
    public void initChannel(SocketChannel ch) {
        ch.pipeline()
                .addLast(new SampleInBoundHandler("SampleInBoundHandlerA", false))
                .addLast(new SampleInBoundHandler("SampleInBoundHandlerB", false))
                .addLast(new SampleInBoundHandler("SampleInBoundHandlerC", true));

        ch.pipeline()
                .addLast(new SampleOutBoundHandler("SampleOutBoundHandlerA"))
                .addLast(new SampleOutBoundHandler("SampleOutBoundHandlerB"))
                .addLast(new SampleOutBoundHandler("SampleOutBoundHandlerC"));
    }
}

public class SampleInBoundHandler extends ChannelInboundHandlerAdapter {

    private final String name;
    private final boolean flush;
    public SampleInBoundHandler(String name, boolean flush) {
        this.name = name;
        this.flush = flush;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        System.out.println("InBoundHandler: " + name);

        if (flush) {
            ctx.channel().writeAndFlush(msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
public class SampleOutBoundHandler extends ChannelOutboundHandlerAdapter {

    private final String name;
    public SampleOutBoundHandler(String name) {
        this.name = name;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("OutBoundHandler: " + name);
        super.write(ctx, msg, promise);
    }
}
```

通过 `Pipeline` 的 `addLast` 方法分别添加了三个 `InboundHandler` 和 `OutboundHandler`，添加顺序都是 A -> B -> C，下图可以表示初始化后 `ChannelPipeline` 的内部结构：

![image-20221025095921072](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221025095921072.png)

当客户端向服务端发送请求时，会触发 `SampleInBoundHandler` 调用链的 `channelRead` 事件。经过 `SampleInBoundHandler` 调用链处理完成后，在 `SampleInBoundHandlerC` 中会调用 `writeAndFlush` 方法向客户端写回数据，此时会触发 `SampleOutBoundHandler` 调用链的 `write` 事件。

再`Inbound` 事件的传播方向为 `Head -> Tail`，而 `Outbound` 事件传播方向是 `Tail -> Head`，两者恰恰相反。所以最后的输出结果如下：

![image-20221025100134169](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221025100134169.png)

## 5、异常传播机制

`ChannelPipeline` 事件传播的实现采用了经典的责任链模式，调用链路环环相扣。接下来通过修改 `SampleInBoundHandler` 的实现来模拟业务逻辑异常：

```java
public class SampleInBoundHandler extends ChannelInboundHandlerAdapter {

    private final String name;
    private final boolean flush;

    public SampleInBoundHandler(String name, boolean flush) {
        this.name = name;
        this.flush = flush;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        System.out.println("InBoundHandler: " + name);

        if (flush) {
            ctx.channel().writeAndFlush(msg);
        } else {
            throw new RuntimeException("InBoundHandler: " + name);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("InBoundHandlerException: " + name);
        ctx.fireExceptionCaught(cause);
    }
}
```

在 `channelRead` 事件处理中，第一个 A 节点就会抛出 `RuntimeException`。同时我们重写了 `ChannelInboundHandlerAdapter` 中的 `exceptionCaught` 方法，只是在开头加上了控制台输出，方便观察异常传播的行为。下面看一下代码运行的控制台输出结果：

![image-20221025100409784](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221025100409784.png)

由输出结果可以看出 `ctx.fireExceptionCaugh` 会将异常按顺序从 `Head` 节点传播到 `Tail` 节点。如果用户没有对异常进行拦截处理，最后将由 `Tail` 节点统一处理，在 `TailContext` 源码中可以找到具体实现：

```java
protected void onUnhandledInboundException(Throwable cause) {
    try {
        logger.warn(
                "An exceptionCaught() event was fired, and it reached at the tail of the pipeline. " +
                        "It usually means the last handler in the pipeline did not handle the exception.",
                cause);
    } finally {
        ReferenceCountUtil.release(cause);
    }
}
```

虽然 `Netty` 中 `TailContext` 提供了兜底的异常处理逻辑，但是在很多场景下，并不能满足我们的需求。所以推荐用户对异常进行统一拦截，然后根据实际业务场景实现更加完善的异常处理机制。