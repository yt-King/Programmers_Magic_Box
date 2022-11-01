# 15｜实战演练：玩转Kubernetes（1）

经过两个星期的学习，到今天我们的“初级篇”也快要结束了。

和之前的“入门篇”一样，在这次课里，我也会对前面学过的知识做一个比较全面的回顾，毕竟 Kubernetes 领域里有很多新名词、新术语、新架构，知识点多且杂，这样的总结复习就更有必要。

接下来我还是先简要列举一下“初级篇”里讲到的 Kubernetes 要点，然后再综合运用这些知识，演示一个实战项目——还是搭建 WordPress 网站，不过这次不是在 Docker 里，而是在 Kubernetes 集群里。

## Kubernetes 技术要点回顾

容器技术开启了云原生的大潮，但成熟的容器技术，到生产环境的应用部署的时候，却显得“步履维艰”。因为容器只是针对单个进程的隔离和封装，而实际的应用场景却是要求许多的应用进程互相协同工作，其中的各种关系和需求非常复杂，在容器这个技术层次很难掌控。

为了解决这个问题，**容器编排**（Container Orchestration）就出现了，它可以说是以前的运维工作在云原生世界的落地实践，本质上还是在集群里调度管理应用程序，只不过管理的主体由人变成了计算机，管理的目标由原生进程变成了容器和镜像。

而现在，容器编排领域的王者就是——Kubernetes。

Kubernetes 源自 Borg 系统，它凝聚了 Google 的内部经验和 CNCF 的社区智慧，所以战胜了竞争对手 Apache Mesos 和 Docker Swarm，成为了容器编排领域的事实标准，也成为了云原生时代的基础操作系统，学习云原生就必须要掌握 Kubernetes。

（10 讲）Kubernetes 的 **Master/Node 架构**是它具有自动化运维能力的关键，也对我们的学习至关重要，这里我再用另一张参考架构图来简略说明一下它的运行机制：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/f429ca7114eebf140632409f3fbcbb05.png" alt="img" style="zoom:50%;" />

Kubernetes 把集群里的计算资源定义为节点（Node），其中又划分成控制面和数据面两类。

- 控制面是 Master 节点，负责管理集群和运维监控应用，里面的核心组件是 **apiserver、etcd、scheduler、controller-manager**。
- 数据面是 Worker 节点，受 Master 节点的管控，里面的核心组件是 **kubelet、kube-proxy、container-runtime**。

此外，Kubernetes 还支持插件机制，能够灵活扩展各项功能，常用的插件有 DNS 和 Dashboard。

为了更好地管理集群和业务应用，Kubernetes 从现实世界中抽象出了许多概念，称为“**API 对象**”，描述这些对象就需要使用 **YAML** 语言。

YAML 是 JSON 的超集，但语法更简洁，表现能力更强，更重要的是它以“**声明式**”来表述对象的状态，不涉及具体的操作细节，这样 Kubernetes 就能够依靠存储在 etcd 里集群的状态信息，不断地“调控”对象，直至实际状态与期望状态相同，这个过程就是 Kubernetes 的自动化运维管理（**11 讲**）。

Kubernetes 里有很多的 API 对象，其中最核心的对象是“**Pod**”，它捆绑了一组存在密切协作关系的容器，容器之间共享网络和存储，在集群里必须一起调度一起运行。通过 Pod 这个概念，Kubernetes 就简化了对容器的管理工作，其他的所有任务都是通过对 Pod 这个最小单位的再包装来实现的（**12 讲**）。

除了核心的 Pod 对象，基于“单一职责”和“对象组合”这两个基本原则，我们又学习了 4 个比较简单的 API 对象，分别是 **Job/CronJob** 和 **ConfigMap**/**Secret**。

Job/CronJob 对应的是离线作业，它们逐层包装了 Pod，添加了作业控制和定时规则（**13 讲**）。

ConfigMap/Secret 对应的是配置信息，需要以环境变量或者存储卷的形式注入进 Pod，然后进程才能在运行时使用（**14 讲**）。

和 Docker 类似，Kubernetes 也提供一个客户端工具，名字叫“**kubectl**”，它直接与 Master 节点的 apiserver 通信，把 YAML 文件发送给 RESTful 接口，从而触发 Kubernetes 的对象管理工作流程。

kubectl 的命令很多，查看自带文档可以用 api-resources、explain ，查看对象状态可以用 get、describe、logs ，操作对象可以用 run、apply、exec、delete 等等（**09 讲**）。

使用 YAML 描述 API 对象也有固定的格式，必须写的“头字段”是“**apiVersion**”“**kind**”“**metadata**”，它们表示对象的版本、种类和名字等元信息。实体对象如 Pod、Job、CronJob 会再有“**spec**”字段描述对象的期望状态，最基本的就是容器信息，非实体对象如 ConfigMap、Secret 使用的是“**data**”字段，记录一些静态的字符串信息。

好了，“初级篇”里的 Kubernetes 知识要点我们就基本总结完了，如果你发现哪部分不太清楚，可以课后再多复习一下前面的课程加以巩固。

## WordPress 网站基本架构

下面我们就在 Kubernetes 集群里再搭建出一个 WordPress 网站，用的镜像还是“入门篇”里的那三个应用：WordPress、MariaDB、Nginx，不过当时我们是直接以容器的形式来使用它们，现在要改成 Pod 的形式，让它们运行在 Kubernetes 里。

我还是画了一张简单的架构图，来说明这个系统的内部逻辑关系：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/3d9d09078f1200a84c63a7cea2f40bcc.jpg" alt="img" style="zoom:50%;" />

从这张图中你可以看到，网站的大体架构是没有变化的，毕竟应用还是那三个，它们的调用依赖关系也必然没有变化。

那么 Kubernetes 系统和 Docker 系统的区别又在哪里呢？

关键就在**对应用的封装**和**网络环境**这两点上。

现在 WordPress、MariaDB 这两个应用被封装成了 Pod（由于它们都是在线业务，所以 Job/CronJob 在这里派不上用场），运行所需的环境变量也都被改写成 ConfigMap，统一用“声明式”来管理，比起 Shell 脚本更容易阅读和版本化管理。

另外，Kubernetes 集群在内部维护了一个自己的专用网络，这个网络和外界隔离，要用特殊的“端口转发”方式来传递数据，还需要在集群之外用 Nginx 反向代理这个地址，这样才能实现内外沟通，对比 Docker 的直接端口映射，这里略微麻烦了一些。

## WordPress 网站搭建步骤

了解基本架构之后，接下来我们就逐步搭建这个网站系统，总共需要 4 步。

**第一步**当然是要编排 MariaDB 对象，它的具体运行需求可以参考“入门篇”的实战演练课，这里我就不再重复了。

MariaDB 需要 4 个环境变量，比如数据库名、用户名、密码等，在 Docker 里我们是在命令行里使用参数 --env，而在 Kubernetes 里我们就应该使用 ConfigMap，为此需要定义一个 maria-cm 对象：

```yml
apiVersion: v1
kind: ConfigMap
metadata:
  name: maria-cm
data:
  DATABASE: 'db'
  USER: 'wp'
  PASSWORD: '123'
  ROOT_PASSWORD: '123'
```

然后我们定义 Pod 对象 maria-pod，把配置信息注入 Pod，让 MariaDB 运行时从环境变量读取这些信息：

```yml
apiVersion: v1
kind: Pod
metadata:
  name: maria-pod
  labels:
    app: wordpress
    role: database
spec:
  containers:
  - image: mariadb:10
    name: maria
    imagePullPolicy: IfNotPresent
    ports:
    - containerPort: 3306
    envFrom:
    - prefix: 'MARIADB_'
      configMapRef:
        name: maria-cm
```

注意这里我们使用了一个新的字段“**envFrom**”，这是因为 ConfigMap 里的信息比较多，如果用 env.valueFrom 一个个地写会非常麻烦，容易出错，而 envFrom 可以一次性地把 ConfigMap 里的字段全导入进 Pod，并且能够指定变量名的前缀（即这里的 MARIADB_），非常方便。

使用 kubectl apply 创建这个对象之后，可以用 kubectl get pod 查看它的状态，如果想要获取 IP 地址需要加上参数 -o wide ：

```bash
kubectl apply -f mariadb-pod.yml

kubectl get pod -o wide
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/3fb0242f97c782f79ecf8ba845c81798.png)

现在数据库就成功地在 Kubernetes 集群里跑起来了，IP 地址是“172.17.0.2”，注意这个地址和 Docker 的不同，是 Kubernetes 里的私有网段。

接着是**第二步**，编排 WordPress 对象，还是先用 ConfigMap 定义它的环境变量：

```yml
apiVersion: v1
kind: ConfigMap
metadata:
  name: wp-cm
data:
  HOST: '172.17.0.2'
  USER: 'wp'
  PASSWORD: '123'
  NAME: 'db'
```

在这个 ConfigMap 里要注意的是“HOST”字段，它必须是 MariaDB Pod 的 IP 地址，如果不写正确 WordPress 会无法正常连接数据库。

然后我们再编写 WordPress 的 YAML 文件，为了简化环境变量的设置同样使用了 envFrom：

```yml
apiVersion: v1
kind: Pod
metadata:
  name: wp-pod
  labels:
    app: wordpress
    role: website
spec:
  containers:
  - image: wordpress:5
    name: wp-pod
    imagePullPolicy: IfNotPresent
    ports:
    - containerPort: 80
    envFrom:
    - prefix: 'WORDPRESS_DB_'
      configMapRef:
        name: wp-cm
```

接着还是用 kubectl apply 创建对象，kubectl get pod 查看它的状态：

```bash
kubectl apply -f wp-pod.yml

kubectl get pod -o wide
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/d5e8c09e70e90179d651bf3c28abc0de.png" alt="img" style="zoom:50%;" />

**第三步**是为 WordPress Pod 映射端口号，让它在集群外可见。

因为 Pod 都是运行在 Kubernetes 内部的私有网段里的，外界无法直接访问，想要对外暴露服务，需要使用一个专门的 kubectl port-forward 命令，它专门负责把本机的端口映射到在目标对象的端口号，有点类似 Docker 的参数 -p，经常用于 Kubernetes 的临时调试和测试。

下面我就把本地的“8080”映射到 WordPress Pod 的“80”，kubectl 会把这个端口的所有数据都转发给集群内部的 Pod：

```bash
kubectl port-forward wp-pod 8080:80 &
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/d445d205ae6f8c966200ffa9ba7f29be.png" alt="img" style="zoom:50%;" />

注意在命令的末尾我使用了一个 & 符号，让端口转发工作在后台进行，这样就不会阻碍我们后续的操作。

如果想关闭端口转发，需要敲命令 fg ，它会把后台的任务带回到前台，然后就可以简单地用“Ctrl + C”来停止转发了。

**第四步**是创建反向代理的 Nginx，让我们的网站对外提供服务。

这是因为 WordPress 网站使用了 URL 重定向，直接使用“8080”会导致跳转故障，所以为了让网站正常工作，我们还应该在 Kubernetes 之外启动 Nginx 反向代理，保证外界看到的仍然是“80”端口号。（这里的细节和我们的课程关系不大，感兴趣的同学可以留言提问讨论）

Nginx 的配置文件和第 7 讲基本一样，只是目标地址变成了“127.0.0.1:8080”，它就是我们在第三步里用 kubectl port-forward 命令创建的本地地址：

```json
server {
  listen 80;
  default_type text/html;
  location / {
      proxy_http_version 1.1;
      proxy_set_header Host $host;
      proxy_pass http://127.0.0.1:8080;
  }
}
```

然后我们用 docker run -v 命令加载这个配置文件，以容器的方式启动这个 Nginx 代理：

```bash
docker run -d --rm \
    --net=host \
    -v /tmp/proxy.conf:/etc/nginx/conf.d/default.conf \
    nginx:alpine
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/9f2b16fb58dbe0a358e26042565f9851.png)

有了 Nginx 的反向代理之后，我们就可以打开浏览器，输入本机的“127.0.0.1”或者是虚拟机的 IP 地址（我这里仍然是“http://192.168.10.208”），看到 WordPress 的界面：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/735552be9cf6d45ac41a001252ayyef4.png" alt="img" style="zoom:50%;" />

你也可以在 Kubernetes 里使用命令 kubectl logs 查看 WordPress、MariaDB 等 Pod 的运行日志，来验证它们是否已经正确地响应了请求：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/8498c598e6f3142d490218601acdbc62.png)

## 使用 Dashboard 管理 Kubernetes

到这里 WordPress 网站就搭建成功了，我们的主要任务也算是完成了，不过我还想再带你看看 Kubernetes 的图形管理界面，也就是 Dashboard，看看不用命令行该怎么管理 Kubernetes。

启动 Dashboard 的命令你还记得吗，在第 10 节课里讲插件的时候曾经说过，需要用 minikube，命令是：

```bash
minikube dashboard
```

它会自动打开浏览器界面，显示出当前 Kubernetes 集群里的工作负载：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/536eeb176a7737c9ed815c10af0fcf59.png" alt="img" style="zoom:50%;" />

点击任意一个 Pod 的名字，就会进入管理界面，可以看到 Pod 的详细信息，而右上角有 4 个很重要的功能，分别可以查看日志、进入 Pod 内部、编辑 Pod 和删除 Pod，相当于执行 logs、exec、edit、delete 命令，但要比命令行要直观友好的多：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/d5e5131bfb1d6aae2f026177bf283628.png" alt="img" style="zoom:50%;" />

比如说，我点击了第二个按钮，就会在浏览器里开启一个 Shell 窗口，直接就是 Pod 的内部 Linux 环境，在里面可以输入任意的命令，无论是查看状态还是调试都很方便：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/466c67a48616c946505242d0796ed74c.png" alt="img" style="zoom:50%;" />

ConfigMap/Secret 等对象也可以在这里任意查看或编辑：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/defyybc05ed793b7966e1f6b68018022.png" alt="img" style="zoom:50%;" />

Dashboard 里的可操作的地方还有很多，这里我只是一个非常简单的介绍。虽然你也许已经习惯了使用键盘和命令行，但偶尔换一换口味，改用鼠标和图形界面来管理 Kubernetes 也是件挺有意思的事情，有机会不妨尝试一下。

## 小结

好了，作为“初级篇”的最后一节课，今天我们回顾了一下 Kubernetes 的知识要点，我还是画一份详细的思维导图，帮助你课后随时复习总结。

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/87a1d338340c8ca771a97d0fyy4b611f.jpg)

这节课里我们使用 Kubernetes 搭建了 WordPress 网站，和第 7 讲里的 Docker 比较起来，我们应用了容器编排技术，以“声明式”的 YAML 来描述应用的状态和它们之间的关系，而不会列出详细的操作步骤，这就降低了我们的心智负担——调度、创建、监控等杂事都交给 Kubernetes 处理，我们只需“坐享其成”。

虽然我们朝着云原生的方向迈出了一大步，不过现在我们的容器编排还不够完善，Pod 的 IP 地址还必须手工查找填写，缺少自动的服务发现机制，另外对外暴露服务的方式还很原始，必须要依赖集群外部力量的帮助。

所以，我们的学习之旅还将继续，在接下来的“中级篇”里，会开始研究更多的 API 对象，来解决这里遇到的问题。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/3c3036bc56bb9ec14598342e56c11bea.jpg" alt="img" style="zoom:33%;" />