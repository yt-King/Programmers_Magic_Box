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

对比ByteBuffer的缺点：

- `ByteBuffer`长度固定，一旦分配完成，它的容量不能动态扩展和收缩，当需要编码的POJO对象大于`ByteBuffer`的容量时，会发生索引越界异常；
- `ByteBuffer`只有一个标识位置的指针`position`，读写的时候需要手工调用`flip()`和`rewind()`等，使用者必须小心谨慎地处理这些API，否则很容易导致程序处理失败；
- `ByteBuffer`的API功能有限，一些高级和实用的特性它不支持，需要使用者自己编程实现。

## 2、ByteBuf 类——Netty 的数据容器

所有的网络通信都涉及字节序列的移动，所以高效易用的数据结构明显是必不可少的。所以理解Netty 的 ByteBuf 是如何满足这些需求的很重要。

### 2.1 工作原理

`ByteBuf`工作机制:`ByteBuf`维护了两个不同的索引，一个用于读取，一个用于写入。`readerIndex`和`writerIndex`的初始值都是0，当从`ByteBuf`中读取数据时，它的`readerIndex`将会被递增(它不会超过`writerIndex`)，当向`ByteBuf`写入数据时，它的`writerIndex`会递增。

![image-20221017224234800](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/202210172242866.png)

`ByteBuf`的几个特点：

1. 名称以`readXXX`或者`writeXXX`开头的`ByteBuf`方法，会推进对应的索引，而以`setXXX`或`getXXX`开头的操作不会。
2. 在读取之后，`0～readerIndex`的就被视为`discard`的，调用`discardReadBytes`方法，可以释放这部分空间，它的作用类似`ByteBuffer`的`compact()`方法。
3. `readerIndex`和`writerIndex`之间的数据是可读取的，等价于`ByteBuffer`的`position`和`limit`之间的数据。`writerIndex`和`capacity`之间的空间是可写的，等价于`ByteBuffer`的`limit`和`capacity`之间的可用空间。

### 2.2 ByteBuf的三种类型

#### 堆缓冲区

> 最常用的 ByteBuf 模式是将数据存储在 JVM 的堆空间中。这种模式被称为支撑数组（backing array），它能在没有使用池化的情况下提供快速的分配和释放

`优点`：由于数据存储在JVM的堆中可以快速创建和快速释放，并且提供了数组的直接快速访问的方法。

`缺点`：每次读写数据都要先将数据拷贝到直接缓冲区（相关阅读：[Java NIO 直接缓冲区和非直接缓冲区对比](http://www.codebaoku.com/it-java/it-java-230692.html)）再进行传递。

#### 直接缓冲区

> Direct Buffer在堆之外直接分配内存，直接缓冲区不会占用堆的容量。

`优点`：在使用Socket传递数据时性能很好，由于数据直接在内存中，不存在从JVM拷贝数据到直接缓冲区的过程，性能好。

`缺点`：因为Direct Buffer是直接在内存中，所以分配内存空间和释放内存比堆缓冲区更复杂和慢。

#### 复合缓冲区

>复合缓冲区是 Netty 特有的缓冲区。本质上类似于提供一个或多个 `ByteBuf` 的组合视图，可以根据需要添加和删除不同类型的 `ByteBuf`。
>
>![image-20221017234320763](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/202210172343831.png)

`优点`：提供了一种访问方式让使用者自由地组合多个`ByteBuf`，避免了复制和分配新的缓冲区。

`缺点`：不支持访问其支撑数组。因此如果要访问，需要先将内容复制到堆内存中，再进行访问。

https://juejin.cn/post/6995058704714301476

https://juejin.cn/post/6994593304922816549
