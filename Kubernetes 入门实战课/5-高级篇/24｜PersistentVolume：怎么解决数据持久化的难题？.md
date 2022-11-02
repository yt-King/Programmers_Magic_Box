# 24｜PersistentVolume：怎么解决数据持久化的难题？

经过了“初级篇”和“中级篇”的学习，相信你对 Kubernetes 的认识已经比较全面了，那么在接下来的“高级篇”里，我们再进一步，探索 Kubernetes 更深层次的知识点和更高级的应用技巧。

今天就先从 PersistentVolume 讲起。

早在第 14 讲介绍 ConfigMap/Secret 的时候，我们就遇到过 Kubernetes 里的 Volume 存储卷的概念，它使用字段 volumes 和 volumeMounts，相当于是给 Pod 挂载了一个“虚拟盘”，把配置信息以文件的形式注入进 Pod 供进程使用。

不过，那个时候的 Volume 只能存放较少的数据，离真正的“虚拟盘”还差得很远。

今天我们就一起来了解 Volume 的高级用法，看看 Kubernetes 管理存储资源的 API 对象 PersistentVolume、PersistentVolumeClaim、StorageClass，然后使用本地磁盘来创建实际可用的存储卷。

## 什么是 PersistentVolume

在刚完成的“中级篇”实战中（22 讲），我们在 Kubernetes 集群里搭建了 WordPress 网站，但其中存在一个很严重的问题：Pod 没有持久化功能，导致 MariaDB 无法“永久”存储数据。

因为 Pod 里的容器是由镜像产生的，而镜像文件本身是只读的，进程要读写磁盘只能用一个临时的存储空间，一旦 Pod 销毁，临时存储也就会立即回收释放，数据也就丢失了。

为了保证即使 Pod 销毁后重建数据依然存在，我们就需要找出一个解决方案，让 Pod 用上真正的“虚拟盘”。怎么办呢？

其实，Kubernetes 的 Volume 对数据存储已经给出了一个很好的抽象，它只是定义了有这么一个“存储卷”，而这个“存储卷”是什么类型、有多大容量、怎么存储，我们都可以自由发挥。Pod 不需要关心那些专业、复杂的细节，只要设置好 volumeMounts，就可以把 Volume 加载进容器里使用。

所以，Kubernetes 就顺着 Volume 的概念，延伸出了 **PersistentVolume** 对象，它专门用来表示持久存储设备，但隐藏了存储的底层实现，我们只需要知道它能安全可靠地保管数据就可以了（由于 PersistentVolume 这个词很长，一般都把它简称为 PV）。

那么，集群里的 PV 都从哪里来呢？

**作为存储的抽象，PV 实际上就是一些存储设备、文件系统**，比如 Ceph、GlusterFS、NFS，甚至是本地磁盘，管理它们已经超出了 Kubernetes 的能力范围，所以，一般会由系统管理员单独维护，然后再在 Kubernetes 里创建对应的 PV。

要注意的是，PV 属于集群的系统资源，是和 Node 平级的一种对象，Pod 对它没有管理权，只有使用权。

## 什么是 PersistentVolumeClaim/StorageClass

现在有了 PV，我们是不是可以直接在 Pod 里挂载使用了呢？

还不行。因为不同存储设备的差异实在是太大了：有的速度快，有的速度慢；有的可以共享读写，有的只能独占读写；有的容量小，只有几百 MB，有的容量大到 TB、PB 级别……

这么多种存储设备，只用一个 PV 对象来管理还是有点太勉强了，不符合“单一职责”的原则，让 Pod 直接去选择 PV 也很不灵活。于是 Kubernetes 就又增加了两个新对象，**PersistentVolumeClaim** 和 **StorageClass**，用的还是“中间层”的思想，把存储卷的分配管理过程再次细化。

我们看这两个新对象。

PersistentVolumeClaim，简称 PVC，从名字上看比较好理解，就是用来向 Kubernetes 申请存储资源的。PVC 是给 Pod 使用的对象，它相当于是 Pod 的代理，代表 Pod 向系统申请 PV。一旦资源申请成功，Kubernetes 就会把 PV 和 PVC 关联在一起，这个动作叫做“**绑定**”（bind）。

但是，系统里的存储资源非常多，如果要 PVC 去直接遍历查找合适的 PV 也很麻烦，所以就要用到 StorageClass。

StorageClass 的作用有点像第 21 讲里的 IngressClass，它抽象了特定类型的存储系统（比如 Ceph、NFS），在 PVC 和 PV 之间充当“协调人”的角色，帮助 PVC 找到合适的 PV。也就是说它可以简化 Pod 挂载“虚拟盘”的过程，让 Pod 看不到 PV 的实现细节。

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/5e21d007a6152ec9594919300c2b6e22.jpg)

如果看到这里，你觉得还是差点理解也不要着急，我们找个生活中的例子来类比一下。毕竟和常用的 CPU、内存比起来，我们对存储系统的认识还是比较少的，所以 Kubernetes 里，PV、PVC 和 StorageClass 这三个新概念也不是特别好掌握。

看例子，假设你在公司里想要 10 张纸打印资料，于是你给前台打电话讲清楚了需求。

- “打电话”这个动作，就相当于 PVC，向 Kubernetes 申请存储资源。
- 前台里有各种牌子的办公用纸，大小、规格也不一样，这就相当于 StorageClass。
- 前台根据你的需要，挑选了一个品牌，再从库存里拿出一包 A4 纸，可能不止 10 张，但也能够满足要求，就在登记表上新添了一条记录，写上你在某天申领了办公用品。这个过程就是 PVC 到 PV 的绑定。
- 而最后到你手里的 A4 纸包，就是 PV 存储对象。

好，大概了解了这些 API 对象，我们接下来可以结合 YAML 描述和实际操作再慢慢体会。

## 如何使用 YAML 描述 PersistentVolume

Kubernetes 里有很多种类型的 PV，我们先看看最容易的本机存储“**HostPath**”，它和 Docker 里挂载本地目录的 -v 参数非常类似，可以用它来初步认识一下 PV 的用法。

因为 Pod 会在集群的任意节点上运行，所以首先，我们要作为系统管理员在每个节点上创建一个目录，它将会作为本地存储卷挂载到 Pod 里。

为了省事，我就在 /tmp 里建立名字是 host-10m-pv 的目录，表示一个只有 10MB 容量的存储设备。

有了存储，我们就可以使用 YAML 来描述这个 PV 对象了。

不过很遗憾，你不能用 kubectl create 直接创建 PV 对象，**只能用** **kubectl api-resources****、****kubectl explain** **查看 PV 的字段说明，手动编写 PV 的 YAML 描述文件**。

下面我给出一个 YAML 示例，你可以把它作为样板，编辑出自己的 PV：

```yml
apiVersion: v1

kind: PersistentVolume

metadata:

  name: host-10m-pv

spec:

  storageClassName: host-test

  accessModes:

  - ReadWriteOnce

  capacity:

    storage: 10Mi

  hostPath:

    path: /tmp/host-10m-pv/
```

PV 对象的文件头部分很简单，还是 API 对象的“老一套”，我就不再详细解释了，重点看它的 spec 部分，每个字段都很重要，描述了存储的详细信息。

“**storageClassName**”就是刚才说过的，对存储类型的抽象 StorageClass。这个 PV 是我们手动管理的，名字可以任意起，这里我写的是 host-test，你也可以把它改成 manual、hand-work 之类的词汇。

“**accessModes**”定义了存储设备的访问模式，简单来说就是虚拟盘的读写权限，和 Linux 的文件访问模式差不多，目前 Kubernetes 里有 3 种：

- ReadWriteOnce：存储卷可读可写，但只能被一个节点上的 Pod 挂载。
- ReadOnlyMany：存储卷只读不可写，可以被任意节点上的 Pod 多次挂载。
- ReadWriteMany：存储卷可读可写，也可以被任意节点上的 Pod 多次挂载。

你要注意，这 3 种访问模式限制的对象是节点而不是 Pod，因为存储是系统级别的概念，不属于 Pod 里的进程。

显然，本地目录只能是在本机使用，所以这个 PV 使用了 ReadWriteOnce。

第三个字段“**capacity**”就很好理解了，表示存储设备的容量，这里我设置为 10MB。

再次提醒你注意，Kubernetes 里定义存储容量使用的是国际标准，我们日常习惯使用的 KB/MB/GB 的基数是 1024，要写成 Ki/Mi/Gi，一定要小心不要写错了，否则单位不一致实际容量就会对不上。

最后一个字段“**hostPath**”最简单，它指定了存储卷的本地路径，也就是我们在节点上创建的目录。

用这些字段把 PV 的类型、访问模式、容量、存储位置都描述清楚，一个存储设备就创建好了。

## 如何使用 YAML 描述 PersistentVolumeClaim

有了 PV，就表示集群里有了这么一个持久化存储可以供 Pod 使用，我们需要再定义 PVC 对象，向 Kubernetes 申请存储。

下面这份 YAML 就是一个 PVC，要求使用一个 5MB 的存储设备，访问模式是 ReadWriteOnce：

```yml
apiVersion: v1

kind: PersistentVolumeClaim

metadata:

  name: host-5m-pvc

spec:

  storageClassName: host-test

  accessModes:

    - ReadWriteOnce

  resources:

    requests:

      storage: 5Mi
```

PVC 的内容与 PV 很像，但它不表示实际的存储，而是一个“申请”或者“声明”，spec 里的字段描述的是对存储的“期望状态”。

所以 PVC 里的 storageClassName、accessModes 和 PV 是一样的，**但不会有字段** **capacity****，而是要用** **resources.request** **表示希望要有多大的容量**。

这样，Kubernetes 就会根据 PVC 里的描述，去找能够匹配 StorageClass 和容量的 PV，然后把 PV 和 PVC“绑定”在一起，实现存储的分配，和前面打电话要 A4 纸的过程差不多。

## 如何在 Kubernetes 里使用 PersistentVolume

现在我们已经准备好了 PV 和 PVC，就可以让 Pod 实现持久化存储了。

首先需要用 kubectl apply 创建 PV 对象：

```bash
kubectl apply -f host-path-pv.yml
```

然后用 kubectl get  查看它的状态：

```bash
kubectl get pv
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/5ca80e12c71d162f5707d37bf6009c37.png)

从截图里我们可以看到，这个 PV 的容量是 10MB，访问模式是 RWO（ReadWriteOnce），StorageClass 是我们自己定义的 host-test，状态显示的是 Available，也就是处于可用状态，可以随时分配给 Pod 使用。

接下来我们创建 PVC，申请存储资源：

```bash
kubectl apply -f host-path-pvc.yml

kubectl get pvc
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/fd6f1cb75f5d349860928594db29a11f.png)

一旦 PVC 对象创建成功，Kubernetes 就会立即通过 StorageClass、resources 等条件在集群里查找符合要求的 PV，如果找到合适的存储对象就会把它俩“绑定”在一起。

PVC 对象申请的是 5MB，但现在系统里只有一个 10MB 的 PV，没有更合适的对象，所以 Kubernetes 也只能把这个 PV 分配出去，多出的容量就算是“福利”了。

你会看到这两个对象的状态都是 Bound，也就是说存储申请成功，PVC 的实际容量就是 PV 的容量 10MB，而不是最初申请的容量 5MB。

那么，如果我们把 PVC 的申请容量改大一些会怎么样呢？比如改成 100MB：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/25241a47c63cf629b88590ba1773710c.png)

你会看到 PVC 会一直处于 Pending 状态，这意味着 Kubernetes 在系统里没有找到符合要求的存储，无法分配资源，只能等有满足要求的 PV 才能完成绑定。

## 如何为 Pod 挂载 PersistentVolume

PV 和 PVC 绑定好了，有了持久化存储，现在我们就可以为 Pod 挂载存储卷。用法和第 14 讲里差不多，先要在 spec.volumes 定义存储卷，然后在 containers.volumeMounts 挂载进容器。

不过因为我们用的是 PVC，所以**要在** **volumes** **里用字段** **persistentVolumeClaim** **指定 PVC 的名字**。

下面就是 Pod 的 YAML 描述文件，把存储卷挂载到了 Nginx 容器的 /tmp 目录：

```yml
apiVersion: v1

kind: Pod

metadata:

  name: host-pvc-pod

spec:

  volumes:

  - name: host-pvc-vol

    persistentVolumeClaim:

      claimName: host-5m-pvc

  containers:

    - name: ngx-pvc-pod

      image: nginx:alpine

      ports:

      - containerPort: 80

      volumeMounts:

      - name: host-pvc-vol

        mountPath: /tmp
```

我把 Pod 和 PVC/PV 的关系画成了图（省略了字段 accessModes），你可以从图里看出它们是如何联系起来的：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/a4d709808a0ef729604c884c50748bd8.jpg)

现在我们创建这个 Pod，查看它的状态：

```bash
kubectl apply -f host-path-pod.yml

kubectl get pod -o wide
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/d4a2771c2c32597a4e5e2e60823c159d.png)

它被 Kubernetes 调到了 worker 节点上，那么 PV 是否确实挂载成功了呢？让我们用 kubectl exec 进入容器，执行一些命令看看：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/c42a618688eee98555cda33c5c1d6824.png)

容器的 /tmp 目录里生成了一个 a.txt 的文件，根据 PV 的定义，它就应该落在 worker 节点的磁盘上，所以我们就登录 worker 节点检查一下：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/9dc40b80e2e4edb2d9449e2d43b02ac4.png)

你会看到确实在 worker 节点的本地目录有一个 a.txt 的文件，再对一下时间，就可以确认是刚才在 Pod 里生成的文件。

因为 Pod 产生的数据已经通过 PV 存在了磁盘上，所以如果 Pod 删除后再重新创建，挂载存储卷时会依然使用这个目录，数据保持不变，也就实现了持久化存储。

不过还有一点小问题，因为这个 PV 是 HostPath 类型，只在本节点存储，如果 Pod 重建时被调度到了其他节点上，那么即使加载了本地目录，也不会是之前的存储位置，持久化功能也就失效了。

所以，HostPath 类型的 PV 一般用来做测试，或者是用于 DaemonSet 这样与节点关系比较密切的应用，我们下节课再讲实现真正任意的数据持久化。

## 小结

好了，今天我们一起学习了 Kubernetes 里应对持久化存储的解决方案，一共有三个 API 对象，分别是 PersistentVolume、PersistentVolumeClaim、StorageClass。它们管理的是集群里的存储资源，简单来说就是磁盘，Pod 必须通过它们才能够实现数据持久化。

再小结一下今天的主要内容：

1. PersistentVolume 简称为 PV，是 Kubernetes 对存储设备的抽象，由系统管理员维护，需要描述清楚存储设备的类型、访问模式、容量等信息。
2. PersistentVolumeClaim 简称为 PVC，代表 Pod 向系统申请存储资源，它声明对存储的要求，Kubernetes 会查找最合适的 PV 然后绑定。
3. StorageClass 抽象特定类型的存储系统，归类分组 PV 对象，用来简化 PV/PVC 的绑定过程。
4. HostPath 是最简单的一种 PV，数据存储在节点本地，速度快但不能跟随 Pod 迁移。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/25a1d7b3e9841c886781afb44b351341.jpg" alt="img" style="zoom:33%;" />