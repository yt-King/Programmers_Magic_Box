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

## 内置的传输

Netty 内置了一些可开箱即用的传输。因为这些传输并不支持每一种协议，所以我们必须选择一个和自己的应用程序所使用的协议相匹配的传输。Netty 提供的传输类别如下表所示：

| 名称     | 包                          | 描述                                                         |
| -------- | --------------------------- | ------------------------------------------------------------ |
| NIO      | io.netty.channel.socket.nio | 使用 java.nio.channels 包作为基础——基于选择器的方式          |
| Epoll    | io.netty.channel.epoll      | 由 JNI 驱动的 epoll()和非阻塞 IO。这个传输支持 只有在Linux上可用的多种特性，如SO_REUSEPORT， 比 NIO 传输更快，而且是完全非阻塞的（**这个是 Netty 特有的实现，更加适配 Netty 现有的线程模型，具有更高的性能以及更低的垃圾回收压力**） |
| OIO      | io.netty.channel.socket.oio | 使用 java.net 包作为基础——使用阻塞流                         |
| Local    | io.netty.channel.local      | 可以在 VM 内部通过管道进行通信的本地传输                     |
| Embedded | io.netty.channel.embedded   | Embedded 传输，允许使用 ChannelHandler 而又不需要一个真正的基于网络的传输。这在测试你的 ChannelHandler 实现时非常有用 |

### 1、NIO——非阻塞 I/O

相关链接：[Java NIO 底层原理](https://cloud.tencent.com/developer/article/1739347)

NIO 提供了一个所有 I/O 操作的全异步的实现，相对于传统的IO，其突出的有点就是快速和简单，在网络传输中优势明显。NIO主要有三大核心部分：Channel(通道)，Buffer(缓冲区), Selector(选择器)。传统IO基于字节流和字符流进行操作，而NIO基于Channel和Buffer(缓冲区)进行操作，数据总是从通道读取到缓冲区中，或者从缓冲区写入到通道中。Selector(选择器)用于监听多个通道的事件（比如：连接打开，数据到达）。因此，单个线程可以监听多个数据通道。

![image-20221008175804107](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-9/image-20221008175804107.png)