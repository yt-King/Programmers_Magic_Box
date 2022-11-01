# 18｜Deployment：让应用永不宕机

在上一节课里，我们使用 kubeadm 搭建了一个由两个节点组成的小型 Kubernetes 集群，比起单机的 minikube，它更接近真实环境，在这里面做实验我们今后也更容易过渡到生产系统。

有了这个 Kubernetes 环境，接下来我们就在“初级篇”里学习的 Pod 知识基础上，深入研究一些由 Pod 衍生出来的其他 API 对象。

今天要看的 API 对象名字叫“**Deployment**”，顾名思义，它是专门用来部署应用程序的，能够让应用永不宕机，多用来发布无状态的应用，是 Kubernetes 里最常用也是最有用的一个对象。

## 为什么要有 Deployment

在第 13 讲里，我们学习了 API 对象 Job 和 CronJob，它们代表了生产环境中的离线业务，通过对 Pod 的包装，向 Pod 添加控制字段，实现了基于 Pod 运行临时任务和定时任务的功能。

那么，除了“离线业务”，另一大类业务——也就是“在线业务”，在 Kubernetes 里应该如何处理呢？

我们先看看用 Pod 是否就足够了。因为它在 YAML 里使用“**containers**”就可以任意编排容器，而且还有一个“**restartPolicy**”字段，默认值就是 Always，可以监控 Pod 里容器的状态，一旦发生异常，就会自动重启容器。

不过，“restartPolicy”只能保证容器正常工作。不知你有没有想到，如果容器之外的 Pod 出错了该怎么办呢？比如说，有人不小心用 kubectl delete 误删了 Pod，或者 Pod 运行的节点发生了断电故障，那么 Pod 就会在集群里彻底消失，对容器的控制也就无从谈起了。

还有我们也都知道，在线业务远不是单纯启动一个 Pod 这么简单，还有多实例、高可用、版本更新等许多复杂的操作。比如最简单的多实例需求，为了提高系统的服务能力，应对突发的流量和压力，我们需要创建多个应用的副本，还要即时监控它们的状态。如果还是只使用 Pod，那就会又走回手工管理的老路，没有利用好 Kubernetes 自动化运维的优势。

其实，解决的办法也很简单，因为 Kubernetes 已经给我们提供了处理这种问题的思路，就是“单一职责”和“对象组合”。既然 Pod 管理不了自己，那么我们就再创建一个新的对象，由它来管理 Pod，采用和 Job/CronJob 一样的形式——“对象套对象”。

这个用来管理 Pod，实现在线业务应用的新 API 对象，就是 Deployment。

## 如何使用 YAML 描述 Deployment

我们先用命令 kubectl api-resources 来看看 Deployment 的基本信息：

```bash
kubectl api-resources

NAME         SHORTNAMES   APIVERSION   NAMESPACED   KIND

deployments  deploy       apps/v1      true        Deployment
```

从它的输出信息里可以知道，Deployment 的简称是“**deploy**”，它的 apiVersion 是“**apps/v1**”，kind 是“**Deployment**”。

所以，依据前面学习 Pod、Job 的经验，你就应该知道 Deployment 的 YAML 文件头该怎么写了：

```yml
apiVersion: apps/v1

kind: Deployment

metadata:

  name: xxx-dep
```

当然了，我们还是可以使用命令 kubectl create 来创建 Deployment 的 YAML 样板，免去反复手工输入的麻烦。

创建 Deployment 样板的方式和 Job 也差不多，先指定类型是 **Deployment**（简写 **deploy**），然后是它的名字，再用 --image 参数指定镜像名字。

比如下面的这条命令，我就创建了一个名字叫 ngx-dep 的对象，使用的镜像是 nginx:alpine：

```bash
export out="--dry-run=client -o yaml"

kubectl create deploy ngx-dep --image=nginx:alpine $out
```

得到的 Deployment 样板大概是下面的这个样子：

```yml
apiVersion: apps/v1

kind: Deployment

metadata:

  labels:

    app: ngx-dep

  name: ngx-dep

  

spec:

  replicas: 2

  selector:

    matchLabels:

      app: ngx-dep

      

  template:

    metadata:

      labels:

        app: ngx-dep

    spec:

      containers:

      - image: nginx:alpine

        name: nginx
```

把它和 Job/CronJob 对比一下，你会发现有相似也有不同。相似的地方是都有“**spec**”“**template**”字段，“template”字段里也是一个 Pod；不同的地方在于它的“spec”部分多了 replicas、selector 这两个新字段，聪明的你应该会猜到，这或许就会是 Deployment 特殊能力的根本。

没错，这两个新字段就是 Deployment 实现多实例、高可用等功能的关键所在。

## Deployment 的关键字段

先看 replicas 字段。它的含义比较简单明了，就是“副本数量”的意思，也就是说，指定要在 Kubernetes 集群里运行多少个 Pod 实例。

有了这个字段，就相当于为 Kubernetes 明确了应用部署的“期望状态”，Deployment 对象就可以扮演运维监控人员的角色，自动地在集群里调整 Pod 的数量。

比如，Deployment 对象刚创建出来的时候，Pod 数量肯定是 0，那么它就会根据 YAML 文件里的 Pod 模板，逐个创建出要求数量的 Pod。

接下来 Kubernetes 还会持续地监控 Pod 的运行状态，万一有 Pod 发生意外消失了，数量不满足“期望状态”，它就会通过 apiserver、scheduler 等核心组件去选择新的节点，创建出新的 Pod，直至数量与“期望状态”一致。

这里面的工作流程很复杂，但对于我们这些外部用户来说，设置起来却是非常简单，只需要一个 replicas 字段就搞定了，不需要再用人工监控管理，整个过程完全自动化。

下面我们再来看另一个关键字段 selector，它的作用是“筛选”出要被 Deployment 管理的 Pod 对象，下属字段“**matchLabels**”定义了 Pod 对象应该携带的 label，它必须和“template”里 Pod 定义的“labels”完全相同，否则 Deployment 就会找不到要控制的 Pod 对象，apiserver 也会告诉你 YAML 格式校验错误无法创建。

这个 selector 字段的用法初看起来好像是有点多余，为了保证 Deployment 成功创建，我们必须在 YAML 里把 label 重复写两次：一次是在“**selector.matchLabels**”，另一次是在“**template.matadata**”。像在这里，你就要在这两个地方连续写 app: ngx-dep ：

```yml
...

spec:

  replicas: 2

  selector:

    matchLabels:

      app: ngx-dep

      

  template:

    metadata:

      labels:

        app: ngx-dep

    ...
```

你也许会产生疑问：为什么要这么麻烦？为什么不能像 Job 对象一样，直接用“template”里定义好的 Pod 就行了呢？

这是因为在线业务和离线业务的应用场景差异很大。离线业务中的 Pod 基本上是一次性的，只与这个业务有关，紧紧地绑定在 Job 对象里，一般不会被其他对象所使用。

而在线业务就要复杂得多了，因为 Pod 永远在线，除了要在 Deployment 里部署运行，还可能会被其他的 API 对象引用来管理，比如负责负载均衡的 Service 对象。

所以 Deployment 和 Pod 实际上是一种松散的组合关系，Deployment 实际上并不“持有”Pod 对象，它只是帮助 Pod 对象能够有足够的副本数量运行，仅此而已。如果像 Job 那样，把 Pod 在模板里“写死”，那么其他的对象再想要去管理这些 Pod 就无能为力了。

好明白了这一点，那我们该用什么方式来描述 Deployment 和 Pod 的组合关系呢？

Kubernetes 采用的是这种“贴标签”的方式，通过在 API 对象的“metadata”元信息里加各种标签（labels），我们就可以使用类似关系数据库里查询语句的方式，筛选出具有特定标识的那些对象。**通过标签这种设计，Kubernetes 就解除了 Deployment 和模板里 Pod 的强绑定，把组合关系变成了“弱引用”**。

虽然话是这么说，但对于很多 Kubernetes 的初学者来说，理解 Deployment 里的 spec 定义还是一个难点。

所以我还是画了一张图，用不同的颜色来区分 Deployment YAML 里的字段，并且用虚线特别标记了 matchLabels 和 labels 之间的联系，希望能够帮助你理解 Deployment 与被它管理的 Pod 的组合关系。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/1f1fdcd112a07cce85757e27fbcc1bb0.jpg" alt="img" style="zoom:50%;" />

## 如何使用 kubectl 操作 Deployment

把 Deployment 的 YAML 写好之后，我们就可以用 kubectl apply 来创建对象了：

```bash
kubectl apply -f deploy.yml
```

要查看 Deployment 的状态，仍然是用 kubectl get 命令：

```bash
kubectl get deploy
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/a5b3f8a4c6ac5560dc9dfyybfb257872.png" alt="img" style="zoom:50%;" />

它显示的信息都很重要：

- READY 表示运行的 Pod 数量，前面的数字是当前数量，后面的数字是期望数量，所以“2/2”的意思就是要求有两个 Pod 运行，现在已经启动了两个 Pod。
- UP-TO-DATE 指的是当前已经更新到最新状态的 Pod 数量。因为如果要部署的 Pod 数量很多或者 Pod 启动比较慢，Deployment 完全生效需要一个过程，UP-TO-DATE 就表示现在有多少个 Pod 已经完成了部署，达成了模板里的“期望状态”。
- AVAILABLE 要比 READY、UP-TO-DATE 更进一步，不仅要求已经运行，还必须是健康状态，能够正常对外提供服务，它才是我们最关心的 Deployment 指标。
- 最后一个 AGE 就简单了，表示 Deployment 从创建到现在所经过的时间，也就是运行的时间。

因为 Deployment 管理的是 Pod，我们最终用的也是 Pod，所以还需要用 kubectl get pod 命令来看看 Pod 的状态：

```bash
kubectl get pod
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/4e47298ab0fa443e2c8936ac8ed9e5cb.png" alt="img" style="zoom:50%;" />

从截图里你可以看到，被 Deployment 管理的 Pod 自动带上了名字，命名的规则是 Deployment 的名字加上两串随机数（其实是 Pod 模板的 Hash 值）。

好，到现在对象创建成功，Deployment 和 Pod 的状态也都没问题，可以正常服务，我们是时候检验一下 Deployment 部署的效果了，看看是否如前面所说的，Deployment 部署的应用真的可以做到“永不宕机”？

来尝试一下吧，让我们用 kubectl delete 删除一个 Pod，模拟一下 Pod 发生故障的情景：

```bash
kubectl delete pod ngx-dep-6796688696-jm6tt
```

然后再查看 Pod 的状态：

```bash
kubectl get pod
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/4467538713d83434bf6ff983acde1c80.png" alt="img" style="zoom:50%;" />

你就会“惊喜”地发现，被删除的 Pod 确实是消失了，但 Kubernetes 在 Deployment 的管理之下，很快又创建出了一个新的 Pod，保证了应用实例的数量始终是我们在 YAML 里定义的数量。

这就证明，Deployment 确实实现了它预定的目标，能够让应用“永远在线”“永不宕机”。

**在 Deployment 部署成功之后，你还可以随时调整 Pod 的数量，实现所谓的“应用伸缩”**。这项工作在 Kubernetes 出现之前对于运维来说是一件很困难的事情，而现在由于有了 Deployment 就变得轻而易举了。

kubectl scale 是专门用于实现“扩容”和“缩容”的命令，你只要用参数 --replicas 指定需要的副本数量，Kubernetes 就会自动增加或者删除 Pod，让最终的 Pod 数量达到“期望状态”。

比如下面的这条命令，就把 Nginx 应用扩容到了 5 个：

```bash
kubectl scale --replicas=5 deploy ngx-dep
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/843cc2d702b4e4034bb3a2f2f988fdc4.png" alt="img" style="zoom:50%;" />

但要注意， kubectl scale 是命令式操作，扩容和缩容只是临时的措施，如果应用需要长时间保持一个确定的 Pod 数量，最好还是编辑 Deployment 的 YAML 文件，改动“replicas”，再以声明式的 kubectl apply 修改对象的状态。

因为 Deployment 使用了 selector 字段，这里我就顺便提一下 Kubernetes 里 labels 字段的使用方法吧。

之前我们通过 labels 为对象“贴”了各种“标签”，在使用 kubectl get 命令的时候，加上参数 -l，使用 ==、!=、in、notin 的表达式，就能够很容易地用“标签”筛选、过滤出所要查找的对象（有点类似社交媒体的 #tag 功能），效果和 Deployment 里的 selector 字段是一样的。

看两个例子，第一条命令找出“app”标签是 nginx 的所有 Pod，第二条命令找出“app”标签是 ngx、nginx、ngx-dep 的所有 Pod：

```bash
kubectl get pod -l app=nginx

kubectl get pod -l 'app in (ngx, nginx, ngx-dep)'
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/b07ba6a3a9207a5a998c237a6ef49d26.png" alt="img" style="zoom:50%;" />

## 小结

好了，今天我们学习了 Kubernetes 里的一个重要的对象：Deployment，它表示的是在线业务，和 Job/CronJob 的结构类似，也包装了 Pod 对象，通过添加额外的控制功能实现了应用永不宕机，你也可以再对比一下第 13 讲来加深对它的理解。

我再简单小结一下今天的内容：

1. Pod 只能管理容器，不能管理自身，所以就出现了 Deployment，由它来管理 Pod。
2. Deployment 里有三个关键字段，其中的 template 和 Job 一样，定义了要运行的 Pod 模板。
3. replicas 字段定义了 Pod 的“期望数量”，Kubernetes 会自动维护 Pod 数量到正常水平。
4. selector 字段定义了基于 labels 筛选 Pod 的规则，它必须与 template 里 Pod 的 labels 一致。
5. 创建 Deployment 使用命令 kubectl apply，应用的扩容、缩容使用命令 kubectl scale。

学了 Deployment 这个 API 对象，我们今后就不应该再使用“裸 Pod”了。即使我们只运行一个 Pod，也要以 Deployment 的方式来创建它，虽然它的 replicas 字段值是 1，但 Deployment 会保证应用永远在线。

另外，作为 Kubernetes 里最常用的对象，Deployment 的本事还不止这些，它还支持滚动更新、版本回退，自动伸缩等高级功能，这些在“高级篇”里我们再详细学习。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/22a054fac2709bbcaabe209aa6fff47f.jpg" alt="img" style="zoom:33%;" />