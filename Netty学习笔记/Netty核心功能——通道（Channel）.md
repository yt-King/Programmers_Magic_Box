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

根据网络数据的流向，`ChannelPipeline` 分为入站 `ChannelInboundHandler` 和出站 `ChannelOutboundHandler` 两种处理器。在客户端与服务端通信的过程中，数据从客户端发向服务端的过程叫出站，反之称为入站。数据先由一系列 `InboundHandler` 处理后入站，然后再由相反方向的 `OutboundHandler` 处理完成后出站，如下图所示。我们经常使用的解码器 `Decoder` 就是入站操作，编码器 `Encoder` 就是出站操作。服务端接收到客户端数据需要先经过 `Decoder` 入站处理后，再通过 `Encoder` 出站通知客户端。