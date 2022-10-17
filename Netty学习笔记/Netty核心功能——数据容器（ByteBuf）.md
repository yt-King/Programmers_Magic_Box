# Netty核心功能——数据容器（ByteBuf）

>网络数据的基本单位总是字节，Java NIO 提供了 ByteBuffer 作为它的字节容器，但是这个类使用起来过于复杂，而且也有些繁琐。
>Netty 的 ByteBuffer 替代品是 ByteBuf，一个强大的实现，既解决了 JDK API 的局限性，又为网络应用程序的开发者提供了更好的 API。

## 1、简介

Netty 的数据处理 API 通过两个组件暴露——`abstract class ByteBuf` 和 `interface  ByteBufHolder`，下面是一些 ByteBuf API 的优点：

- 它可以被用户自定义的缓冲区类型扩展；
- 通过内置的复合缓冲区类型实现了透明的零拷贝；
- 容量可以按需增长（类似于 JDK 的 StringBuilder）；
- 在读和写这两种模式之间切换不需要调用 ByteBuffer 的 flip()方法；
- 读和写使用了不同的索引；
- 支持方法的链式调用；
- 支持引用计数；
- 支持池化。

## 2、ByteBuf 类——Netty 的数据容器

所有的网络通信都涉及字节序列的移动，所以高效易用的数据结构明显是必不可少的。所以理解Netty 的 ByteBuf 是如何满足这些需求的很重要。

### 2.1工作原理

