# Netty核心功能——传输 （Transport）

Netty 为它**所有的传输实现提供了一个通用 API**，这使得这种转换比直接使用 JDK 所能够达到的简单得多。所产生的代码不会被实现的细节所污染，我们也不需要在整个代码库上进行广泛的重构。简而言之，我们可以将时间花在其他更有成效的事情上。

## 从一个例子开始

先来比较一下不使用netty以及使用netty所写出来的nio程序对比：

```java
//只用 JDK API 来实现 NIO
public class PlainNioServer {
    public void serve(int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        ServerSocket ss = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        ss.bind(address);                                            //1、绑定服务器到制定端口
        Selector selector = Selector.open();                         //2、打开 selector 处理 channel
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);    //3、将ServerSocket 注册到Selector以接受连接
        final ByteBuffer msg = ByteBuffer.wrap("Hi!\r\n".getBytes());
        for (;;) {
            try {
                selector.select();//4、等待需要处理的新事件；阻塞将一直持续到下一个传入事件
            } catch (IOException ex) {
                ex.printStackTrace();
                // handle exception
                break;
            }
            Set<SelectionKey> readyKeys = selector.selectedKeys();    //5、获取所有接收事件的SelectionKey实例
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    if (key.isAcceptable()) {                //6、检查事件是否是一个新的已经就绪可以被接受的连接
                        ServerSocketChannel server =
                                (ServerSocketChannel)key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_WRITE |
                                SelectionKey.OP_READ, msg.duplicate());    //7、接受客户端，并将它注册到选择器
                        System.out.println(
                                "Accepted connection from " + client);
                    }
                    if (key.isWritable()) {                //8、检查套接字是否已经准备好写数据
                        SocketChannel client =
                                (SocketChannel)key.channel();
                        ByteBuffer buffer =
                                (ByteBuffer)key.attachment();
                        while (buffer.hasRemaining()) {
                            if (client.write(buffer) == 0) {        //9、将数据写到已连接的客户端
                                break;
                            }
                        }
                        client.close();                    //10、关闭连接
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {
                        // 在关闭时忽略
                    }
                }
            }
        }
    }
}
```

将其改写为使用netty实现：

```java
public class NettyNioServer {

    public void server(int port) throws Exception {
        final ByteBuf buf = Unpooled.unreleasableBuffer(
                Unpooled.copiedBuffer("Hi!\r\n", Charset.forName("UTF-8")));
        EventLoopGroup group = new NioEventLoopGroup();//1、为非阻塞模式使用NioEventLoopGroup
        try {
            ServerBootstrap b = new ServerBootstrap();    //2、创建 ServerBootstrap
            b.group(new NioEventLoopGroup(), new NioEventLoopGroup())
             .channel(NioServerSocketChannel.class)
             .localAddress(new InetSocketAddress(port))
             .childHandler(new ChannelInitializer<SocketChannel>() { //3、指定 ChannelInitializer，对于每个已接受的连接都调用它
                 @Override
                 public void initChannel(SocketChannel ch) 
                     throws Exception {
                     ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {//4、添加 ChannelInboundHandlerAdapter以接收和处理事件
                         @Override
                         public void channelActive(ChannelHandlerContext ctx) throws Exception {
                             ctx.writeAndFlush(buf.duplicate())//5将消息写到客户端，并添加ChannelFutureListener，以便消息写完就关闭连接
                                .addListener(ChannelFutureListener.CLOSE);
                         }
                     });
                 }
             });
            ChannelFuture f = b.bind().sync();                    //6、绑定服务器以接受连接
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();                    //7、释放所有的资源
        }
    }
}
```

使用netty的代码不仅更加的简洁，还有一个好处是如果想要将nio转变为oio，使用java原生api的话就需要重写一个程序，而是用netty的话只需要改变两行代码，分别是将`EventLoopGroup`类型改为`OioEventLoopGroup()`以及`ServerBootstrap`指定`channel`时将`OioServerSocketChannel.class`传入即可。因为 Netty 为每种传输的实现都暴露了相同的 API，所以无论选用哪一种传输的实现，我们的代码都几乎不受影响。

## 传输 API

传输 API 的核心是 Channel 接口，它被用于所有的 I/O 操作，它的层级结构图如下所示：

![image-20221008163617710](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-9/image-20221008163617710.png)

如图所示，每个 Channel 都将会被分配一个 ChannelPipeline 和 ChannelConfig。 ChannelConfig 包含了该 Channel 的所有配置设置，并且支持热更新。由于特定的传输可能具有独特的设置，所以它可能会实现一个 ChannelConfig 的子类型。

> ChannelHandler 的典型用途包括：
>
> - 将数据从一种格式转换为另一种格式；
> - 提供异常的通知；
> - 提供 Channel 变为活动的或者非活动的通知；
> - 提供当 Channel 注册到 EventLoop 或者从 EventLoop 注销时的通知；
> - 提供有关用户自定义事件的通知。

## 内置的传输方式

Netty 内置了一些可开箱即用的传输方式。因为这些传输方式并不支持每一种协议，所以我们必须选择一个和自己的应用程序所使用的协议相匹配的传输方式。Netty 提供的传输方式如下表所示：

| 名称     | 包                          | 描述                                                         |
| -------- | --------------------------- | ------------------------------------------------------------ |
| NIO      | io.netty.channel.socket.nio | 使用 java.nio.channels 包作为基础——基于选择器的方式          |
| Epoll    | io.netty.channel.epoll      | 由 JNI 驱动的 epoll()和非阻塞 IO。这个传输支持 只有在Linux上可用的多种特性，如SO_REUSEPORT， 比 NIO 传输更快，而且是完全非阻塞的（**这个是 Netty 特有的实现，更加适配 Netty 现有的线程模型，具有更高的性能以及更低的垃圾回收压力**） |
| OIO      | io.netty.channel.socket.oio | 使用 java.net 包作为基础——使用阻塞流                         |
| Local    | io.netty.channel.local      | 可以在 VM 内部通过管道进行通信的本地传输                     |
| Embedded | io.netty.channel.embedded   | Embedded 传输，允许使用 ChannelHandler 而又不需要一个真正的基于网络的传输。这在测试你的 ChannelHandler 实现时非常有用 |

### 1、NIO—基于选择器的IO

NIO 提供了一个所有 I/O 操作的全异步的实现，相对于传统的IO，其突出的有点就是快速和简单，在网络传输中优势明显。NIO主要有三大核心部分：Channel(通道)，Buffer(缓冲区), Selector(选择器)。传统IO基于字节流和字符流进行操作，而NIO基于Channel和Buffer(缓冲区)进行操作，数据总是从通道读取到缓冲区中，或者从缓冲区写入到通道中。Selector(选择器)用于监听多个通道的事件（比如：连接打开，数据到达）。因此，单个线程可以监听多个数据通道。

![image-20221008175804107](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-9/image-20221008175804107.png)

对于所有 Netty 的传输实现都共有的用户级别 API 完全地隐藏了这些 NIO 的内部细节。下图就展示了netty的处理流程：

![image-20221009221903353](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202210092219471.png)

---

>接下来简单讲解一下NIO的相关知识：
>
>> 首先要明确的第一点就是——java中的NIO（JDK 1.4引入）和IO模型中的NIO**并不是一个东西**。前者是New-IO，后者是Non-blocking-IO，在这个概念下我们继续往下看。
>
>先来了解一下什么是IO，I/O 在程序中指的 Input/Output，也就是数据的输入和输出。程序的 I/O 操作依赖于底层的 I/O 操作，基本上都会用上底层的 read/write 操作，但 read/write 操作并不是直接写入或读取物理设备，而是面向缓冲区操作，调用操作系统的 read，是把数据从内核缓冲区复制到进程缓冲区；而 write 系统调用，是把数据从进程缓冲区复制到内核缓冲区。如下图：
>
>![image-20221009222734251](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202210092227321.png)
>
>缓存区的设置主要是为了减少频繁地与设备的物理交换。因为外部设备的直接读写，涉及操作系统的中断。发生系统中断时，需要保存之前的进程数据和状态等信息，而结束中断之后，还需要恢复之前的进程数据和状态等信息。为了减少这种底层系统的时间损耗、性能损耗，于是出现了内存缓冲区。
>
>再来了解一下了解两组名词：**阻塞和非阻塞**、**同步和异步**：
>
>- **阻塞**：阻塞意味着调用方在**结果出现之前，不会做任何其他事情**。
>- **非阻塞**：非阻塞意味着调用方**在结果出现之前，同时在做其他的事情**。被调用方会立即返回一个值，调用方拿到后可以做其他事情也可以选择不做。
>
>- **同步**：**必须等待被调用方处理完请求返回结果**。
>- **异步**：**被调用方处理请求通过调用方注册的回调接口返回结果**。
>
>有了这些前置的知识就可以来看看Unix 中的四种 I/O 模型：
>
>#### 同步阻塞 I/O (Blocking IO)
>
>在阻塞式 IO 模型中，应用程序从 IO 系统调用开始，直到系统调用返回，在这段时间内，调用进程是阻塞的。如下图：
>
>![image-20221009225135272](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202210092251379.png)
>
>优点：实现简单，而且阻塞期间线程挂起，用户线程基本不会占用资源
>
>缺点：在高并发的场景下，需要大量的线程来维护每一个阻塞的任务，内存和线程切换的开销都非常大。
>
>#### 同步非阻塞 I/O (Non-Blocking IO)
>
>在这种模式下，如果调用时没有数据，系统会立即返回一个调用失败的消息。然后调用方过段时间再查询，如果有数据，则变成阻塞方式，进行数据复制。如下图：
>
>![image-20221009225452142](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202210092254265.png)
>
>优点：每次的调用都可以立即得到反馈，调用方不会阻塞，实时性比较好
>
>缺点：调用方需要不断的轮询，这个会占用 CPU 资源，并且效率低下。
>
>#### I/O 多路复用 (IO Multiplexing)
>
>为了避免非阻塞 IO 模型上轮询的问题，系统设计了 select/epoll，在这个模式下，一个进程可以监控多个文件描述符，也就是多个数据的就绪状态。目前支持 IO 多路复用的系统调用有 select，epoll 等，select 基本在所有的系统都支持，epoll 是 Linux 2.6 内核提出的，是 select 的一种增强版本。流程如下：
>
>![image-20221009225850390](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/202210092258500.png)
>
>IO多路复用模型的基本原理就是select/epoll系统调用，单个线程不断的轮询select/epoll系统调用所负责的成百上千的socket连接，当某个或者某些socket网络连接有数据到达了，就返回这些可以读写的连接。
>
>优点：通过一次select/epoll系统调用，就查询到到可以读写的一个甚至是成百上千的网络连接。
>
>缺点：IO 多路复用本质上还是属于同步 IO，也就是数据就绪后，还是需要阻塞等待数据复制完成。
>
>#### 异步 I/O (Asynchronous IO)
>
>被调用方完成所有的操作后，再通知调用方。调用方得到通知后可以直接进行后续操作，而不需进行等待，如下图：
>
>![image-20221009230047691](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/202210092300806.png)
>
>优点：调用方永远不会阻塞。
>
>缺点：因为被调用方完成了大部分工作，所以需要被调用方支持。目前 Windows 下有 IOCP 实现了异步 IO，Linux 下在 2.6 引入，但是还不完善，并且底层依旧采用 epoll，和 IO 多路复用相比，性能上没有明显的优势。
>
>现在再来看java中的NIO，其实他**主要是基于 IO 多路复用这个 IO 模型**。

最后再提一下零拷贝这个概念：
零拷贝（zero-copy）是一种目前只有在使用 NIO 和 Epoll 传输时才可使用的特性。它使你可以快速高效地将数据从文件系统移动到网络接口，而不需要将其从内核空间复制到用户空间，其在像 FTP 或者HTTP 这样的协议中可以显著地提升性能。但是，并不是所有的操作系统都支持这一特性。特别地，它对于实现了数据加密或者压缩的文件系统是不可用的——只能传输文件的原始内容。反过来说，传输已被加密的文件则不是问题。（具体实现原理：[Linux零拷贝原理](https://juejin.cn/post/6887469050515947528)）

---

### 2、Epoll—用于 Linux 的本地非阻塞传输

之前所说的Netty 的 NIO 传输基于 Java 提供的异步/非阻塞网络编程的通用抽象。虽然这保证了 Netty 的非阻塞 API 可以在任何平台上使用，但它也包含了相应的限制，因为 JDK为了在所有系统上提供相同的功能，必须做出妥协。

Linux作为高性能网络编程的平台，其重要性与日俱增，这催生了大量先进特性的开发，其中包括epoll——**一个高度可扩展的I/O事件通知特性**。这个API自Linux内核版本 2.5.44 被引入，提供了比旧的POSIX `select`和`poll`系统调用 更好的性能。

Netty为Linux提供了一组NIO API，使用一种和它本身的设计更加一致的方式使用epoll，并且以一种更加轻量的方式使用中断。如果我们的应用程序是运行在Linux系统上的，那么有必要考虑利用这个版本的传输方式；因为在高负载下它的性能要优于JDK的NIO实现。

---

>来看看IO多路复用中select、poll、epoll之间的区别：
>
>先来理解一个概念——**文件描述符**
>
>> 文件描述符（File descriptor）是一个用于表述指向文件的引用的抽象化概念。在形式上是一个非负整数。实际上可以把它理解为一个索引值，指向内核为每一个进程所维护的该进程打开文件的记录表。当程序打开一个现有文件或者创建一个新文件时，内核向进程返回一个文件描述符。在程序设计中，一些涉及底层的程序编写往往会围绕着文件描述符展开。
>
>I/O 多路复用机制指内核一旦发现进程指定的一个或者多个IO条件准备读取，它就通知该进程，就是说通过一种机制，可以监视多个描述符，一旦某个描述符就绪（一般是读就绪或写就绪），能够通知程序进行相应的读写操作。这种机制的使用需要 select 、 poll 、 epoll 来配合。 
>
>#### select ==> 时间复杂度O(n)
>
>select是第一个实现IO多路复用 ，是三者当中最底层的，它的事件的轮训机制是基于比特位的。每次查询都要遍历整个事件列表，所以select具有O(n)的无差别轮询复杂度，同时处理的流越多，无差别轮询时间就越长。
>
>#### poll ==> 时间复杂度O(n)
>
>poll本质上和select没有区别，改进了select最大数量限制。它将用户传入的数组拷贝到内核空间，然后查询每个fd对应的设备状态，如果设备就绪则在设备等待队列中加入一项并继续遍历，如果遍历完所有fd后没有发现就绪设备，则挂起当前进程，直到设备就绪或者主动超时，被唤醒后它又要再次遍历fd。这个过程经历了多次无谓的遍历。
>
>#### epoll ==> 时间复杂度O(1)
>
>epoll使用一个文件描述符管理多个描述符，将用户关系的文件描述符的事件存放到内核的一个事件表中，这样在用户空间和内核空间的拷贝只需要一次。
>
>详细介绍：[IO多路复用之select、poll、epoll ](https://www.cnblogs.com/yungyu16/p/13066744.html)&&[详解IO多路复用（select、poll、epoll）](https://juejin.cn/post/7050773195745411085)
>

---

### 3、OIO—旧的阻塞 I/O

一般不怎么会用到，但是你可以不用，不过他不能没有。例如，你可能需要移植使用了一些进行阻塞调用的库（如JDBC ）的遗留代码，而将逻辑转 换为非阻塞的可能也是不切实际的，这时候OIO就派上用场了，可以在短期内使用Netty的OIO传输进行过度，然后再将代码移植到纯粹的异步传输上。

这时候你可能会想，Netty是如何能够使用和用于异步传输相同的API来支持OIO的呢。 答案就是，Netty利用了SO_TIMEOUT这个Socket标志，它指定了等待一个I/O操作完成的最大毫秒 数。如果操作在指定的时间间隔内没有完成，则将会抛出一个SocketTimeout Exception。Netty 将捕获这个异常并继续处理循环。在EventLoop下一次运行时，它将再次尝试。这实际上也是类似于Netty这样的异步框架能够支持OIO的唯一方式。

![image-20221011154355299](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221011154355299.png)

### 4、用于 JVM 内部通信的 Local 传输

Netty 提供了一个 Local 传输，用于在同一个 JVM 中运行的客户端和服务器程序之间的异步 通信。同样，这个传输也支持对于所有 Netty 传输实现都共同的 API。

在这个传输中，和服务器 Channel 相关联的 SocketAddress 并没有绑定物理网络地址； 相反，只要服务器还在运行，它就会被存储在注册表里，并在 Channel 关闭时注销。因为这个 传输并不接受真正的网络流量，所以它并不能够和其他传输实现进行互操作。因此，客户端希望 连接到（在同一个 JVM 中）使用了这个传输的服务器端时也必须使用它。除了这个限制，它的使用方式和其他的传输一模一样。

### 5、Embedded 传输

Netty 提供了一种额外的传输，使得你可以将一组 ChannelHandler 作为帮助器类嵌入到其他的 ChannelHandler 内部。通过这种方式，你将可以扩展一个 ChannelHandler 的功能， 而又不需要修改其内部代码。 

## 总结

本篇文章主要分析了Netty的几种传输方式，通常情况下都是使用的NIO或者Epoll（在linux上）。但是也要了解其他的几种传输方式，因为也许将来的某个需求会让你用上其余的几种传输方式。