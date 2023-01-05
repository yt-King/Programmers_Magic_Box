# 02 纵览全局：把握 Netty 整体架构脉络

## Netty 整体结构

Netty 是一个设计非常用心的**网络基础组件**，Netty 的模块设计具备较高的**通用性和可扩展性**，一共分为三个模块：

![Drawing 0.png](https://learn.lianglianglee.com/专栏/Netty 核心原理剖析与 RPC 实践-完/assets/CgqCHl-NO7eATPMMAAH8t8KvehQ985.png)

### 1. Core 核心层

Core 核心层是 Netty 最精华的内容，它提供了底层网络通信的通用抽象和实现，包括可扩展的事件模型、通用的通信 API、支持零拷贝的 ByteBuf 等。

### 2. Protocol Support 协议支持层

协议支持层基本上覆盖了主流协议的编解码实现，如 HTTP、SSL、Protobuf、压缩、大文件传输、WebSocket、文本、二进制等主流协议，此外 Netty 还支持自定义应用层协议。Netty 丰富的协议支持降低了用户的开发成本，基于 Netty 我们可以快速开发 HTTP、WebSocket 等服务。

### 3. Transport Service 传输服务层

传输服务层提供了网络传输能力的定义和实现方法。它支持 Socket、HTTP 隧道、虚拟机管道等传输方式。Netty 对 TCP、UDP 等数据传输做了抽象和封装，用户可以更聚焦在业务逻辑实现上，而不必关系底层数据传输的细节。

## Netty 逻辑架构

Netty 的逻辑处理架构为典型网络分层架构设计，共分为网络通信层、事件调度层、服务编排层，每一层各司其职。图中包含了 Netty 每一层所用到的核心组件。

<img src="https://learn.lianglianglee.com/专栏/Netty 核心原理剖析与 RPC 实践-完/assets/Ciqc1F-NO9KAUOtaAAE1S5uRlDE275.png" alt="Drawing 1.png" style="zoom: 67%;" />