# 09｜走近云原生：如何在本机搭建小巧完备的Kubernetes环境

在前面的“入门篇”里，我们学习了以 Docker 为代表的容器技术，做好了充分的准备，那么今天我们就来看看什么是容器编排、什么是 Kubernetes，还有应该怎么在自己的电脑上搭建出一个小巧完善的 Kubernetes 环境，一起走近云原生。

## 什么是容器编排

容器技术的核心概念是容器、镜像、仓库，使用这三大基本要素我们就可以轻松地完成应用的打包、分发工作，实现“一次开发，到处运行”的梦想。

不过，当我们熟练地掌握了容器技术，信心满满地要在服务器集群里大规模实施的时候，却会发现容器技术的创新只是解决了运维部署工作中一个很小的问题。现实生产环境的复杂程度实在是太高了，除了最基本的安装，还会有各式各样的需求，比如服务发现、负载均衡、状态监控、健康检查、扩容缩容、应用迁移、高可用等等。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/4790335b7fdd6a29d2cdda3yy3e337da.png" alt="img" style="zoom: 50%;" />

虽然容器技术开启了云原生时代，但它也只走出了一小步，再继续前进就无能为力了，因为这已经不再是隔离一两个进程的普通问题，而是要隔离数不清的进程，还有它们之间互相通信、互相协作的超级问题，困难程度可以说是指数级别的上升。

这些容器之上的管理、调度工作，就是这些年最流行的词汇：“**容器编排**”（Container Orchestration）。

容器编排这个词听起来好像挺高大上，但如果你理解了之后就会发现其实也并不神秘。像我们在上次课里使用 Docker 部署 WordPress 网站的时候，把 Nginx、WordPress、MariaDB 这三个容器理清次序、配好 IP 地址去运行，就是最初级的一种“容器编排”，只不过这是纯手工操作，比较原始、粗糙。

面对单机上的几个容器，“人肉”编排调度还可以应付，但如果规模上到几百台服务器、成千上万的容器，处理它们之间的复杂联系就必须要依靠计算机了，而目前计算机用来调度管理的“事实标准”，就是我们专栏的主角：Kubernetes。

## 什么是 Kubernetes

现在大家谈到容器都会说是 Docker，但其实早在 Docker 之前，Google 在公司内部就使用了类似的技术（cgroup 就是 Google 开发再提交给 Linux 内核的），只不过不叫容器。

作为世界上最大的搜索引擎，Google 拥有数量庞大的服务器集群，为了提高资源利用率和部署运维效率，它专门开发了一个集群应用管理系统，代号 Borg，在底层支持整个公司的运转。

2014 年，Google 内部系统要“升级换代”，从原来的 Borg 切换到 Omega，于是按照惯例，Google 会发表公开论文。

因为之前在发表 MapReduce、BigTable、GFS 时吃过亏（被 Yahoo 开发的 Hadoop 占领了市场），所以 Google 决定借着 Docker 的“东风”，在发论文的同时，把 C++ 开发的 Borg 系统用 Go 语言重写并开源，于是 Kubernetes 就这样诞生了。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/ebba08c9d360cb01a332d1720e97f1bc.png" alt="img" style="zoom:50%;" />

由于 Kubernetes 背后有 Borg 系统十多年生产环境经验的支持，技术底蕴深厚，理论水平也非常高，一经推出就引起了轰动。然后在 2015 年，Google 又联合 Linux 基金会成立了 CNCF（Cloud Native Computing Foundation，云原生基金会），并把 Kubernetes 捐献出来作为种子项目。

有了 Google 和 Linux 这两大家族的保驾护航，再加上宽容开放的社区，作为 CNCF 的“头把交椅”，Kubernetes 旗下很快就汇集了众多行业精英，仅用了两年的时间就打败了同期的竞争对手 Apache Mesos 和 Docker Swarm，成为了这个领域的唯一霸主。

那么，Kubernetes 到底能够为我们做什么呢？

简单来说，Kubernetes 就是一个**生产级别的容器编排平台和集群管理系统**，不仅能够创建、调度容器，还能够监控、管理服务器，它凝聚了 Google 等大公司和开源社区的集体智慧，从而让中小型公司也可以具备轻松运维海量计算节点——也就是“云计算”的能力。

## 什么是 minikube

Kubernetes 一般都运行在大规模的计算集群上，管理很严格，这就对我们个人来说造成了一定的障碍，没有实际操作环境怎么能够学好用好呢？

好在 Kubernetes 充分考虑到了这方面的需求，提供了一些快速搭建 Kubernetes 环境的工具，在官网（https://kubernetes.io/zh/docs/tasks/tools/）上推荐的有两个：**kind** 和 **minikube**，它们都可以在本机上运行完整的 Kubernetes 环境。

我说一下对这两个工具的个人看法，供你参考。

kind 基于 Docker，意思是“Kubernetes in Docker”。它功能少，用法简单，也因此运行速度快，容易上手。不过它缺少很多 Kubernetes 的标准功能，例如仪表盘、网络插件，也很难定制化，所以我认为它比较适合有经验的 Kubernetes 用户做快速开发测试，不太适合学习研究。

不选 kind 还有一个原因，它的名字与 Kubernetes YAML 配置里的字段 kind 重名，会对初学者造成误解，干扰学习。

再来看 minikube，从名字就能够看出来，它是一个“迷你”版本的 Kubernetes，自从 2016 年发布以来一直在积极地开发维护，紧跟 Kubernetes 的版本更新，同时也兼容较旧的版本（最多只到之前的 6 个小版本）。

minikube 最大特点就是“小而美”，可执行文件仅有不到 100MB，运行镜像也不过 1GB，但就在这么小的空间里却集成了 Kubernetes 的绝大多数功能特性，不仅有核心的容器编排功能，还有丰富的插件，例如 Dashboard、GPU、Ingress、Istio、Kong、Registry 等等，综合来看非常完善。

所以，我建议你在这个专栏里选择 minikube 来学习 Kubernetes。

## 如何搭建 minikube 环境

minikube 支持 Mac、Windows、Linux 这三种主流平台，你可以在它的官网（https://minikube.sigs.k8s.io）找到详细的安装说明，当然在我们这里就只用虚拟机里的 Linux 了。

minikube 的最新版本是 1.25.2，支持的 Kubernetes 版本是 1.23.3，所以我们就选定它作为我们初级篇的学习工具。

minikube 不包含在系统自带的 apt/yum 软件仓库里，我们只能自己去网上找安装包。不过因为它是用 Go 语言开发的，整体就是一个二进制文件，没有多余的依赖，所以安装过程也非常简单，只需要用 curl 或者 wget 下载就行。

minikube 的官网提供了各种系统的安装命令，通常就是下载、拷贝这两步，不过你需要注意一下本机电脑的硬件架构，Intel 芯片要选择带“**amd64**”后缀，Apple M1 芯片要选择“**arm64**”后缀，选错了就会因为 CPU 指令集不同而无法运行：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/d526aa920fba9bee9856177495a1c884.png)

我也把官网上 Linux 系统安装的命令抄在了这里，你可以直接拷贝后安装：

```bash
# Intel x86_64

curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64

# Apple arm64

curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-arm64

sudo install minikube /usr/local/bin/
```

安装完成之后，你可以执行命令 minikube version，看看它的版本号，验证是否安装成功：

```bash
minikube version
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/c01e21cb3835520fd148f063e67605ec.png" alt="img" style="zoom:50%;" />

不过 minikube 只能够搭建 Kubernetes 环境，要操作 Kubernetes，还需要另一个专门的客户端工具“**kubectl**”。

kubectl 的作用有点类似之前我们学习容器技术时候的工具“docker”，它也是一个命令行工具，作用也比较类似，同样是与 Kubernetes 后台服务通信，把我们的命令转发给 Kubernetes，实现容器和集群的管理功能。

kubectl 是一个与 Kubernetes、minikube 彼此独立的项目，所以不包含在 minikube 里，但 minikube 提供了安装它的简化方式，你只需执行下面的这条命令：

```bash
minikube kubectl
```

它就会把与当前 Kubernetes 版本匹配的 kubectl 下载下来，存放在内部目录（例如 .minikube/cache/linux/arm64/v1.23.3），然后我们就可以使用它来对 Kubernetes“发号施令”了。

所以，在 minikube 环境里，我们会用到两个客户端：minikube 管理 Kubernetes 集群环境，kubectl 操作实际的 Kubernetes 功能，和 Docker 比起来有点复杂。

我画了一个简单的 minikube 环境示意图，方便你理解它们的关系。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/22c4d6ef48a0cf009946ebbbc31b91e3.jpg" alt="img" style="zoom: 33%;" />

## 实际验证 minikube 环境

前面的工作都做完之后，我们就可以在本机上运行 minikube，创建 Kubernetes 实验环境了。

使用命令 minikube start 会从 Docker Hub 上拉取镜像，以当前最新版本的 Kubernetes 启动集群。不过为了保证实验环境的一致性，我们可以在后面再加上一个参数 --kubernetes-version，明确指定要使用 Kubernetes 版本。

这里我使用“1.23.3”，启动命令就是：

```bash
minikube start --kubernetes-version=v1.23.3
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/2db1bb67d11892a60b9204fc61e307d0.png)

（它的启动过程使用了比较活泼的表情符号，可能是想表现得平易近人吧，如果不喜欢也可以调整设置关闭它。）

现在 Kubernetes 集群就已经在我们本地运行了，你可以使用 minikube status、minikube node list这两个命令来查看集群的状态：

```bash
minikube status

minikube node list
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/827df6e7b3b5836e093c887f753d4938.png" alt="img" style="zoom:50%;" />

从截图里可以看到，Kubernetes 集群里现在只有一个节点，名字就叫“minikube”，类型是“Control Plane”，里面有 host、kubelet、apiserver 三个服务，IP 地址是 192.168.49.2。

你还可以用命令 minikube ssh 登录到这个节点上，虽然它是虚拟的，但用起来和实机也没什么区别：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/dd04a831f618d70ba77bdaecdb108d03.png)

有了集群，接下来我们就可以使用 kubectl 来操作一下，初步体会 Kubernetes 这个容器编排系统，最简单的命令当然就是查看版本：

```bash
kubectl version
```

不过这条命令还不能直接用，因为使用 minikube 自带的 kubectl 有一点形式上的限制，要在前面加上 minikube 的前缀，后面再有个 --，像这样：

```bash
minikube kubectl -- version 
```

为了避免这个不大不小的麻烦，我建议你使用 Linux 的“**alias**”功能，为它创建一个别名，写到当前用户目录下的 .bashrc 里，也就是这样：

```bash
alias kubectl="minikube kubectl --"
```

另外，kubectl 还提供了命令自动补全的功能，你还应该再加上“**kubectl completion**”：

```bash
source <(kubectl completion bash)
```

现在，我们就可以愉快地使用 kubectl 了：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/abdc85efa4d3b25d779faec7c80b5ff9.png)

下面我们**在 Kubernetes 里运行一个 Nginx 应用，命令与 Docker 一样，也是** **run****，不过形式上有点区别，需要用** **--image** **指定镜像**，然后 Kubernetes 会自动拉取并运行：

```bash
kubectl run ngx --image=nginx:alpine
```

这里涉及 Kubernetes 里的一个非常重要的概念：**Pod**，你可以暂时把它理解成是“穿了马甲”的容器，查看 Pod 列表需要使用命令 kubectl get pod，它的效果类似 docker ps：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/2abb91592d0ff740134a5d7665cb7c95.png" alt="img" style="zoom:50%;" />

命令执行之后可以看到，在 Kubernetes 集群里就有了一个名字叫 ngx 的 Pod 正在运行，表示我们的这个单节点 minikube 环境已经搭建成功。

## 小结

好了，今天我们先了解了容器编排概念和 Kubernetes 的历史，然后在 Linux 虚拟机上安装了 minikube 和 kubectl，运行了一个简单但完整的 Kubernetes 集群，实现了与云原生的“第一次亲密接触”。

那什么是云原生呢？这在 CNCF 上有明确的定义，不过我觉得太学术化了，我也不想机械重复，就讲讲我自己的通俗理解吧。

所谓的“云”，现在就指的是 Kubernetes，那么“云原生”的意思就是应用的开发、部署、运维等一系列工作都要向 Kubernetes 看齐，使用容器、微服务、声明式 API 等技术，保证应用的整个生命周期都能够在 Kubernetes 环境里顺利实施，不需要附加额外的条件。

换句话说，“云原生”就是 Kubernetes 里的“原住民”，而不是从其他环境迁过来的“移民”。

最后照例小结一下今天的内容：

1. 容器技术只解决了应用的打包、安装问题，面对复杂的生产环境就束手无策了，解决之道就是容器编排，它能够组织管理各个应用容器之间的关系，让它们顺利地协同运行。
2. Kubernetes 源自 Google 内部的 Borg 系统，也是当前容器编排领域的事实标准。minikube 可以在本机搭建 Kubernetes 环境，功能很完善，适合学习研究。
3. 操作 Kubernetes 需要使用命令行工具 kubectl，只有通过它才能与 Kubernetes 集群交互。
4. kubectl 的用法与 docker 类似，也可以拉取镜像运行，但操作的不是简单的容器，而是 Pod。

另外还要说一下 Kubernetes 的官网（https://kubernetes.io/zh/），里面有非常详细的文档，包括概念解释、入门教程、参考手册等等，最难得的是它有全中文版本，我们阅读起来完全不会有语言障碍，希望你有时间多上去看看，及时获取官方第一手知识。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/90a478eeb6ae8a6ccd988fedc3ab4096.jpg" alt="img" style="zoom:33%;" />