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