# 04｜创建容器镜像：如何编写正确、高效的Dockerfile

上一次的课程里我们一起学习了容器化的应用，也就是被打包成镜像的应用程序，然后再用各种 Docker 命令来运行、管理它们。

那么这又会带来一个疑问：这些镜像是怎么创建出来的？我们能不能够制作属于自己的镜像呢？

所以今天，我就来讲解镜像的内部机制，还有高效、正确地编写 Dockerfile 制作容器镜像的方法。

## 镜像的内部机制是什么

现在你应该知道，镜像就是一个打包文件，里面包含了应用程序还有它运行所依赖的环境，例如文件系统、环境变量、配置参数等等。

环境变量、配置参数这些东西还是比较简单的，随便用一个 manifest 清单就可以管理，真正麻烦的是文件系统。为了保证容器运行环境的一致性，镜像必须把应用程序所在操作系统的根目录，也就是 rootfs，都包含进来。

虽然这些文件里不包含系统内核（因为容器共享了宿主机的内核），但如果每个镜像都重复做这样的打包操作，仍然会导致大量的冗余。可以想象，如果有一千个镜像，都基于 Ubuntu 系统打包，那么这些镜像里就会重复一千次 Ubuntu 根目录，对磁盘存储、网络传输都是很大的浪费。

很自然的，我们就会想到，应该把重复的部分抽取出来，只存放一份 Ubuntu 根目录文件，然后让这一千个镜像以某种方式共享这部分数据。

这个思路，也正是容器镜像的一个重大创新点：分层，术语叫“**Layer**”。

容器镜像内部并不是一个平坦的结构，而是由许多的镜像层组成的，每层都是只读不可修改的一组文件，相同的层可以在镜像之间共享，然后多个层像搭积木一样堆叠起来，再使用一种叫“**Union FS 联合文件系统**”的技术把它们合并在一起，就形成了容器最终看到的文件系统（图片来源）。

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/c750a7795ff4787c6639dd42bf0a473f.png)

我来拿大家都熟悉的千层糕做一个形象的比喻吧。

千层糕也是由很多层叠加在一起的，从最上面可以看到每层里面镶嵌的葡萄干、核桃、杏仁、青丝等，每一层糕就相当于一个 Layer，干果就好比是 Layer 里的各个文件。但如果某两层的同一个位置都有干果，也就是有文件同名，那么我们就只能看到上层的文件，而下层的就被屏蔽了。

你可以用命令 docker inspect 来查看镜像的分层信息，比如 nginx:alpine 镜像：

docker inspect nginx:alpine

它的分层信息在“RootFS”部分：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/5yybd821a12ec1323f6ea8bb5a5c4ab7.png)

通过这张截图就可以看到，nginx:alpine 镜像里一共有 6 个 Layer。

相信你现在也就明白，之前在使用 docker pull、docker rmi 等命令操作镜像的时候，那些“奇怪”的输出信息是什么了，其实就是镜像里的各个 Layer。Docker 会检查是否有重复的层，如果本地已经存在就不会重复下载，如果层被其他镜像共享就不会删除，这样就可以节约磁盘和网络成本。

## Dockerfile 是什么

知道了容器镜像的内部结构和基本原理，我们就可以来学习如何自己动手制作容器镜像了，也就是自己打包应用。

在之前我们讲容器的时候，曾经说过容器就是“小板房”，镜像就是“样板间”。那么，要造出这个“样板间”，就必然要有一个“施工图纸”，由它来规定如何建造地基、铺设水电、开窗搭门等动作。这个“施工图纸”就是“**Dockerfile**”。

比起容器、镜像来说，Dockerfile 非常普通，它就是一个纯文本，里面记录了一系列的构建指令，比如选择基础镜像、拷贝文件、运行脚本等等，每个指令都会生成一个 Layer，而 Docker 顺序执行这个文件里的所有步骤，最后就会创建出一个新的镜像出来。

我们来看一个最简单的 Dockerfile 实例：

\# Dockerfile.busybox

FROM busybox                  # 选择基础镜像

CMD echo "hello world"        # 启动容器时默认运行的命令

这个文件里只有两条指令。

第一条指令是 FROM，所有的 Dockerfile 都要从它开始，表示选择构建使用的基础镜像，相当于“打地基”，这里我们使用的是 busybox。

第二条指令是 CMD，它指定 docker run 启动容器时默认运行的命令，这里我们使用了 echo 命令，输出“hello world”字符串。

现在有了 Dockerfile 这张“施工图纸”，我们就可以请出“施工队”了，用 docker build 命令来创建出镜像：

docker build -f Dockerfile.busybox .

Sending build context to Docker daemon   7.68kB

Step 1/2 : FROM busybox

 ---> d38589532d97

Step 2/2 : CMD echo "hello world"

 ---> Running in c5a762edd1c8

Removing intermediate container c5a762edd1c8

 ---> b61882f42db7

Successfully built b61882f42db7

你需要特别注意命令的格式，用 -f 参数指定 Dockerfile 文件名，后面必须跟一个文件路径，叫做“**构建上下文**”（build’s context），这里只是一个简单的点号，表示当前路径的意思。

接下来，你就会看到 Docker 会逐行地读取并执行 Dockerfile 里的指令，依次创建镜像层，再生成完整的镜像。

新的镜像暂时还没有名字（用 docker images 会看到是 <none>），但我们可以直接使用“IMAGE ID”来查看或者运行：

docker inspect b61

docker run b61

## 怎样编写正确、高效的 Dockerfile

大概了解了 Dockerfile 之后，我再来讲讲编写 Dockerfile 的一些常用指令和最佳实践，帮你在今后的工作中把它写好、用好。

首先因为构建镜像的第一条指令必须是 FROM，所以基础镜像的选择非常关键。如果关注的是镜像的安全和大小，那么一般会选择 Alpine；如果关注的是应用的运行稳定性，那么可能会选择 Ubuntu、Debian、CentOS。

FROM alpine:3.15                # 选择Alpine镜像

FROM ubuntu:bionic              # 选择Ubuntu镜像

我们在本机上开发测试时会产生一些源码、配置等文件，需要打包进镜像里，这时可以使用 COPY 命令，它的用法和 Linux 的 cp 差不多，不过拷贝的源文件必须是“**构建上下文**”路径里的，不能随意指定文件。也就是说，如果要从本机向镜像拷贝文件，就必须把这些文件放到一个专门的目录，然后在 docker build 里指定“构建上下文”到这个目录才行。

这里有两个 COPY 命令示例，你可以看一下：

COPY ./a.txt  /tmp/a.txt    # 把构建上下文里的a.txt拷贝到镜像的/tmp目录

COPY /etc/hosts  /tmp       # 错误！不能使用构建上下文之外的文件

接下来要说的就是 Dockerfile 里最重要的一个指令 RUN ，它可以执行任意的 Shell 命令，比如更新系统、安装应用、下载文件、创建目录、编译程序等等，实现任意的镜像构建步骤，非常灵活。

RUN 通常会是 Dockerfile 里最复杂的指令，会包含很多的 Shell 命令，但 Dockerfile 里一条指令只能是一行，所以有的 RUN 指令会在每行的末尾使用续行符 \，命令之间也会用 && 来连接，这样保证在逻辑上是一行，就像下面这样：

RUN apt-get update \

​    && apt-get install -y \

​        build-essential \

​        curl \

​        make \

​        unzip \

​    && cd /tmp \

​    && curl -fSL xxx.tar.gz -o xxx.tar.gz\

​    && tar xzf xxx.tar.gz \

​    && cd xxx \

​    && ./config \

​    && make \

​    && make clean

有的时候在 Dockerfile 里写这种超长的 RUN 指令很不美观，而且一旦写错了，每次调试都要重新构建也很麻烦，所以你可以采用一种变通的技巧：**把这些 Shell 命令集中到一个脚本文件里，用** **COPY** **命令拷贝进去再用** **RUN** **来执行**：

COPY setup.sh  /tmp/                # 拷贝脚本到/tmp目录

RUN cd /tmp && chmod +x setup.sh \  # 添加执行权限

​    && ./setup.sh && rm setup.sh    # 运行脚本然后再删除

RUN 指令实际上就是 Shell 编程，如果你对它有所了解，就应该知道它有变量的概念，可以实现参数化运行，这在 Dockerfile 里也可以做到，需要使用两个指令 ARG 和 ENV。

**它们区别在于** **ARG** **创建的变量只在镜像构建过程中可见，容器运行时不可见，而** **ENV** **创建的变量不仅能够在构建镜像的过程中使用，在容器运行时也能够以环境变量的形式被应用程序使用。**

下面是一个简单的例子，使用 ARG 定义了基础镜像的名字（可以用在“FROM”指令里），使用 ENV 定义了两个环境变量：

ARG IMAGE_BASE="node"

ARG IMAGE_TAG="alpine"

ENV PATH=$PATH:/tmp

ENV DEBUG=OFF

还有一个重要的指令是 EXPOSE，它用来声明容器对外服务的端口号，对现在基于 Node.js、Tomcat、Nginx、Go 等开发的微服务系统来说非常有用：

EXPOSE 443           # 默认是tcp协议

EXPOSE 53/udp        # 可以指定udp协议

讲了这些 Dockerfile 指令之后，我还要特别强调一下，因为每个指令都会生成一个镜像层，所以 Dockerfile 里最好不要滥用指令，尽量精简合并，否则太多的层会导致镜像臃肿不堪。

## docker build 是怎么工作的

Dockerfile 必须要经过 docker build 才能生效，所以我们再来看看 docker build 的详细用法。

刚才在构建镜像的时候，你是否对“构建上下文”这个词感到有些困惑呢？它到底是什么含义呢？

我觉得用 Docker 的官方架构图来理解会比较清楚（注意图中与“docker build”关联的虚线）。

因为命令行“docker”是一个简单的客户端，真正的镜像构建工作是由服务器端的“Docker daemon”来完成的，所以“docker”客户端就只能把“构建上下文”目录打包上传（显示信息 Sending build context to Docker daemon ），这样服务器才能够获取本地的这些文件。

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/c8116066bdbf295a7c9fc25b87755dfe.jpg)

明白了这一点，你就会知道，“构建上下文”其实与 Dockerfile 并没有直接的关系，它其实指定了要打包进镜像的一些依赖文件。而 COPY 命令也只能使用基于“构建上下文”的相对路径，因为“Docker daemon”看不到本地环境，只能看到打包上传的那些文件。

但这个机制也会导致一些麻烦，如果目录里有的文件（例如 readme/.git/.svn 等）不需要拷贝进镜像，docker 也会一股脑地打包上传，效率很低。

为了避免这种问题，你可以在“构建上下文”目录里再建立一个 .dockerignore 文件，语法与 .gitignore 类似，排除那些不需要的文件。

下面是一个简单的示例，表示不打包上传后缀是“swp”“sh”的文件：

\# docker ignore

*.swp

*.sh

另外关于 Dockerfile，一般应该在命令行里使用 -f 来显式指定。但如果省略这个参数，docker build 就会在当前目录下找名字是 Dockerfile 的文件。所以，如果只有一个构建目标的话，文件直接叫“Dockerfile”是最省事的。

现在我们使用 docker build 应该就没什么难点了，不过构建出来的镜像只有“IMAGE ID”没有名字，不是很方便。

为此你可以加上一个 -t 参数，也就是指定镜像的标签（tag），这样 Docker 就会在构建完成后自动给镜像添加名字。当然，名字必须要符合上节课里的命名规范，用 : 分隔名字和标签，如果不提供标签默认就是“latest”。

## 小结

好了，今天我们一起学习了容器镜像的内部结构，重点理解**容器镜像是由多个只读的 Layer 构成的，同一个 Layer 可以被不同的镜像共享**，减少了存储和传输的成本。

如何编写 Dockerfile 内容稍微多一点，我再简单做个小结：

1. 创建镜像需要编写 Dockerfile，写清楚创建镜像的步骤，每个指令都会生成一个 Layer。
2. Dockerfile 里，第一个指令必须是 FROM，用来选择基础镜像，常用的有 Alpine、Ubuntu 等。其他常用的指令有：COPY、RUN、EXPOSE，分别是拷贝文件，运行 Shell 命令，声明服务端口号。
3. docker build 需要用 -f 来指定 Dockerfile，如果不指定就使用当前目录下名字是“Dockerfile”的文件。
4. docker build 需要指定“构建上下文”，其中的文件会打包上传到 Docker daemon，所以尽量不要在“构建上下文”中存放多余的文件。
5. 创建镜像的时候应当尽量使用 -t 参数，为镜像起一个有意义的名字，方便管理。

今天讲了不少，但关于创建镜像还有很多高级技巧等待你去探索，比如使用缓存、多阶段构建等等，你可以再参考 Docker 官方文档（https://docs.docker.com/engine/reference/builder/），或者一些知名应用的镜像（如 Nginx、Redis、Node.js 等）进一步学习。

## 课下作业

最后是课下作业时间，这里有一个完整的 Dockerfile 示例，你可以尝试着去解释一下它的含义，然后再自己构建一下：

```bash
# Dockerfile

# docker build -t ngx-app .

# docker build -t ngx-app:1.0 .

ARG IMAGE_BASE="nginx"

ARG IMAGE_TAG="1.21-alpine"

FROM ${IMAGE_BASE}:${IMAGE_TAG}

COPY ./default.conf /etc/nginx/conf.d/

RUN cd /usr/share/nginx/html \
    && echo "hello nginx" > a.txt

EXPOSE 8081 8082 8083
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/1705133103a8aaf6c7fed770afa6dc24.jpg" alt="img" style="zoom:33%;" />