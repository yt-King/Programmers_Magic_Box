# 25｜PersistentVolume + NFS：怎么使用网络共享存储？

在上节课里我们看到了 Kubernetes 里的持久化存储对象 PersistentVolume、PersistentVolumeClaim、StorageClass，把它们联合起来就可以为 Pod 挂载一块“虚拟盘”，让 Pod 在其中任意读写数据。

不过当时我们使用的是 HostPath，存储卷只能在本机使用，而 Kubernetes 里的 Pod 经常会在集群里“漂移”，所以这种方式不是特别实用。

要想让存储卷真正能被 Pod 任意挂载，我们需要变更存储的方式，不能限定在本地磁盘，而是要改成**网络存储**，这样 Pod 无论在哪里运行，只要知道 IP 地址或者域名，就可以通过网络通信访问存储设备。

网络存储是一个非常热门的应用领域，有很多知名的产品，比如 AWS、Azure、Ceph，Kubernetes 还专门定义了 CSI（Container Storage Interface）规范，不过这些存储类型的安装、使用都比较复杂，在我们的实验环境里部署难度比较高。

所以今天的这次课里，我选择了相对来说比较简单的 NFS 系统（Network File System），以它为例讲解如何在 Kubernetes 里使用网络存储，以及静态存储卷和动态存储卷的概念。

## 如何安装 NFS 服务器

作为一个经典的网络存储系统，NFS 有着近 40 年的发展历史，基本上已经成为了各种 UNIX 系统的标准配置，Linux 自然也提供对它的支持。

NFS 采用的是 Client/Server 架构，需要选定一台主机作为 Server，安装 NFS 服务端；其他要使用存储的主机作为 Client，安装 NFS 客户端工具。

所以接下来，我们在自己的 Kubernetes 集群里再增添一台名字叫 Storage 的服务器，在上面安装 NFS，实现网络存储、共享网盘的功能。**不过这台 Storage 也只是一个逻辑概念，我们在实际安装部署的时候完全可以把它合并到集群里的某台主机里**，比如这里我就复用了第 17 讲里的 Console。

新的网络架构如下图所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/786e13af0e2f62f9cd73f5ab555a4507.jpg)

在 Ubuntu 系统里安装 NFS 服务端很容易，使用 apt 即可：

sudo apt -y install nfs-kernel-server

安装好之后，你需要给 NFS 指定一个存储位置，也就是网络共享目录。一般来说，应该建立一个专门的 /data 目录，这里为了简单起见，我就使用了**临时目录** **/tmp/nfs**：

```bash
mkdir -p /tmp/nfs
```

接下来你需要配置 NFS 访问共享目录，修改 /etc/exports，指定目录名、允许访问的网段，还有权限等参数。这些规则比较琐碎，和我们的 Kubernetes 课程关联不大，我就不详细解释了，你只要把下面这行加上就行，注意目录名和 IP 地址要改成和自己的环境一致：

```bash
/tmp/nfs 192.168.10.0/24(rw,sync,no_subtree_check,no_root_squash,insecure)
```

**改好之后，需要用** **exportfs -ra** **通知 NFS，让配置生效**，再用 exportfs -v 验证效果：

```bash
sudo exportfs -ra

sudo exportfs -v
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/0cd8889ee51c6d8a8947f6bd615d6bd1.png)

现在，你就可以使用 systemctl 来启动 NFS 服务器了：

```bash
sudo systemctl start  nfs-server

sudo systemctl enable nfs-server

sudo systemctl status nfs-server
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/29fb58f93f0e764ca8309ed9eff5175a.png)

你还可以使用命令 showmount 来检查 NFS 的网络挂载情况：

```bash
showmount -e 127.0.0.1
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/905ea675a49daef860d21b41a668d6d2.png)

## 如何安装 NFS 客户端

有了 NFS 服务器之后，为了让 Kubernetes 集群能够访问 NFS 存储服务，我们还需要在每个节点上都安装 NFS 客户端。

这项工作只需要一条 apt 命令，不需要额外的配置：

```bash
sudo apt -y install nfs-common
```

同样，在节点上可以用 showmount 检查 NFS 能否正常挂载，注意 IP 地址要写成 NFS 服务器的地址，我在这里就是“192.168.10.208”：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/7ed89f8468d6d4fa315a6d456f2eee9c.png)

现在让我们尝试手动挂载一下 NFS 网络存储，先创建一个目录 /tmp/test 作为挂载点：

```bash
mkdir -p /tmp/test
```

然后用命令 mount 把 NFS 服务器的共享目录挂载到刚才创建的本地目录上：

```bash
sudo mount -t nfs 192.168.10.208:/tmp/nfs /tmp/test
```

最后测试一下，我们在 /tmp/test 里随便创建一个文件，比如 x.yml：

```bash
touch /tmp/test/x.yml
```

再回到 NFS 服务器，检查共享目录 /tmp/nfs，应该会看到也出现了一个同样的文件 x.yml，这就说明 NFS 安装成功了。之后集群里的任意节点，只要通过 NFS 客户端，就能把数据写入 NFS 服务器，实现网络存储。

## 如何使用 NFS 存储卷

现在我们已经为 Kubernetes 配置好了 NFS 存储系统，就可以使用它来创建新的 PV 存储对象了。

先来手工分配一个存储卷，需要指定 storageClassName 是 nfs，而 accessModes 可以设置成 ReadWriteMany，这是由 NFS 的特性决定的，它**支持多个节点同时访问一个共享目录**。

因为这个存储卷是 NFS 系统，所以我们还需要在 YAML 里添加 nfs 字段，指定 NFS 服务器的 IP 地址和共享目录名。

这里我在 NFS 服务器的 /tmp/nfs 目录里又创建了一个新的目录 1g-pv，表示分配了 1GB 的可用存储空间，相应的，PV 里的 capacity 也要设置成同样的数值，也就是 1Gi。

把这些字段都整理好后，我们就得到了一个使用 NFS 网络存储的 YAML 描述文件：

```yml
apiVersion: v1

kind: PersistentVolume

metadata:

  name: nfs-1g-pv

spec:

  storageClassName: nfs

  accessModes:

    - ReadWriteMany

  capacity:

    storage: 1Gi

  nfs:

    path: /tmp/nfs/1g-pv

    server: 192.168.10.208
```

现在就可以用命令 kubectl apply 来创建 PV 对象，再用 kubectl get pv 查看它的状态：

```bash
kubectl apply -f nfs-static-pv.yml

kubectl get pv
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/3bb0be2483e92467d3cac14fbc635739.png)

**再次提醒你注意，****spec.nfs** **里的 IP 地址一定要正确，路径一定要存在（事先创建好）**，否则 Kubernetes 按照 PV 的描述会无法挂载 NFS 共享目录，PV 就会处于“pending”状态无法使用。

有了 PV，我们就可以定义申请存储的 PVC 对象了，它的内容和 PV 差不多，但不涉及 NFS 存储的细节，只需要用 resources.request 来表示希望要有多大的容量，这里我写成 1GB，和 PV 的容量相同：

```yml
apiVersion: v1

kind: PersistentVolumeClaim

metadata:

  name: nfs-static-pvc

spec:

  storageClassName: nfs

  accessModes:

    - ReadWriteMany

  resources:

    requests:

      storage: 1Gi
```

创建 PVC 对象之后，Kubernetes 就会根据 PVC 的描述，找到最合适的 PV，把它们“绑定”在一起，也就是存储分配成功：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/a7bbcc5dce117f9872cee3f08e6a6c8c.png)

我们再创建一个 Pod，把 PVC 挂载成它的一个 volume，具体的做法和上节课是一样的，用 persistentVolumeClaim 指定 PVC 的名字就可以了：

```yml
apiVersion: v1

kind: Pod

metadata:

  name: nfs-static-pod

spec:

  volumes:

  - name: nfs-pvc-vol

    persistentVolumeClaim:

      claimName: nfs-static-pvc

  containers:

    - name: nfs-pvc-test

      image: nginx:alpine

      ports:

      - containerPort: 80

      volumeMounts:

        - name: nfs-pvc-vol

          mountPath: /tmp
```

Pod、PVC、PV 和 NFS 存储的关系可以用下图来形象地表示，你可以对比一下 HostPath PV 的用法，看看有什么不同：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/2a21d16b028afdea4f525439bd8f06a7.jpg)

因为我们在 PV/PVC 里指定了 storageClassName 是 nfs，节点上也安装了 NFS 客户端，所以 Kubernetes 就会自动执行 NFS 挂载动作，把 NFS 的共享目录 /tmp/nfs/1g-pv 挂载到 Pod 里的 /tmp，完全不需要我们去手动管理。

最后还是测试一下，用 kubectl apply 创建 Pod 之后，我们用 kubectl exec 进入 Pod，再试着操作 NFS 共享目录：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/bbc244b6cd21b71f50807864718d8990.png)

退出 Pod，再看一下 NFS 服务器的 /tmp/nfs/1g-pv 目录，你就会发现 Pod 里创建的文件确实写入了共享目录：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/87cdc722da478db6f938db4d424be0d0.png)

而且更好的是，因为 NFS 是一个网络服务，不会受 Pod 调度位置的影响，所以只要网络通畅，这个 PV 对象就会一直可用，数据也就实现了真正的持久化存储。

## 如何部署 NFS Provisoner

现在有了 NFS 这样的网络存储系统，你是不是认为 Kubernetes 里的数据持久化问题就已经解决了呢？

对于这个问题，我觉得可以套用一句现在的流行语：“解决了，但没有完全解决。”

说它“解决了”，是因为网络存储系统确实能够让集群里的 Pod 任意访问，数据在 Pod 销毁后仍然存在，新创建的 Pod 可以再次挂载，然后读取之前写入的数据，整个过程完全是自动化的。

说它“没有完全解决”，是因为 **PV 还是需要人工管理**，必须要由系统管理员手动维护各种存储设备，再根据开发需求逐个创建 PV，而且 PV 的大小也很难精确控制，容易出现空间不足或者空间浪费的情况。

在我们的这个实验环境里，只有很少的 PV 需求，管理员可以很快分配 PV 存储卷，但是在一个大集群里，每天可能会有几百几千个应用需要 PV 存储，如果仍然用人力来管理分配存储，管理员很可能会忙得焦头烂额，导致分配存储的工作大量积压。

那么能不能让创建 PV 的工作也实现自动化呢？或者说，让计算机来代替人类来分配存储卷呢？

这个在 Kubernetes 里就是“**动态存储卷**”的概念，它可以用 StorageClass 绑定一个 Provisioner 对象，而这个 Provisioner 就是一个能够自动管理存储、创建 PV 的应用，代替了原来系统管理员的手工劳动。

有了“动态存储卷”的概念，前面我们讲的手工创建的 PV 就可以称为“静态存储卷”。

目前，Kubernetes 里每类存储设备都有相应的 Provisioner 对象，对于 NFS 来说，它的 Provisioner 就是“NFS subdir external provisioner”，你可以在 GitHub 上找到这个项目（https://github.com/kubernetes-sigs/nfs-subdir-external-provisioner）。

NFS Provisioner 也是以 Pod 的形式运行在 Kubernetes 里的，**在 GitHub 的** **deploy** **目录里是部署它所需的 YAML 文件，一共有三个，分别是 rbac.yaml、class.yaml 和 deployment.yaml**。

不过这三个文件只是示例，想在我们的集群里真正运行起来还要修改其中的两个文件。

第一个要修改的是 rbac.yaml，它使用的是默认的 default 名字空间，应该把它改成其他的名字空间，避免与普通应用混在一起，你可以用“查找替换”的方式把它统一改成 kube-system。

第二个要修改的是 deployment.yaml，它要修改的地方比较多。首先要把名字空间改成和 rbac.yaml 一样，比如是 kube-system，然后重点要修改 volumes 和 env 里的 IP 地址和共享目录名，必须和集群里的 NFS 服务器配置一样。

按照我们当前的环境设置，就应该把 IP 地址改成 192.168.10.208，目录名改成 /tmp/nfs：

```yml
spec:

  template:

    spec:

      serviceAccountName: nfs-client-provisioner

      containers:

      ...

          env:

            - name: PROVISIONER_NAME

              value: k8s-sigs.io/nfs-subdir-external-provisioner

            - name: NFS_SERVER

              value: 192.168.10.208        #改IP地址

            - name: NFS_PATH

              value: /tmp/nfs              #改共享目录名

      volumes:

        - name: nfs-client-root

          nfs:

            server: 192.168.10.208         #改IP地址

            Path: /tmp/nfs                 #改共享目录名
```

还有一件麻烦事，deployment.yaml 的镜像仓库用的是 gcr.io，拉取很困难，而国内的镜像网站上偏偏还没有它，为了让实验能够顺利进行，我不得不“曲线救国”，把它的镜像转存到了 Docker Hub 上。

所以你还需要把镜像的名字由原来的“k8s.gcr.io/sig-storage/nfs-subdir-external-provisioner:v4.0.2”改成“chronolaw/nfs-subdir-external-provisioner:v4.0.2”，其实也就是变动一下镜像的用户名而已。

把这两个 YAML 修改好之后，我们就可以在 Kubernetes 里创建 NFS Provisioner 了：

```bash
kubectl apply -f rbac.yaml

kubectl apply -f class.yaml

kubectl apply -f deployment.yaml
```

使用命令 kubectl get，再加上名字空间限定 -n kube-system，就可以看到 NFS Provisioner 在 Kubernetes 里运行起来了。

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/35758cbe60ddf264bcf59d703fd4986d.png)

## 如何使用 NFS 动态存储卷

比起静态存储卷，动态存储卷的用法简单了很多。因为有了 Provisioner，我们就不再需要手工定义 PV 对象了，只需要在 PVC 里指定 StorageClass 对象，它再关联到 Provisioner。

我们来看一下 NFS 默认的 StorageClass 定义：

```yml
apiVersion: storage.k8s.io/v1

kind: StorageClass

metadata:

  name: nfs-client

provisioner: k8s-sigs.io/nfs-subdir-external-provisioner 

parameters:

  archiveOnDelete: "false"
```

YAML 里的关键字段是 provisioner，它指定了应该使用哪个 Provisioner。另一个字段 parameters 是调节 Provisioner 运行的参数，需要参考文档来确定具体值，在这里的 archiveOnDelete: "false" 就是自动回收存储空间。

理解了 StorageClass 的 YAML 之后，你也可以不使用默认的 StorageClass，而是根据自己的需求，任意定制具有不同存储特性的 StorageClass，比如添加字段 onDelete: "retain" 暂时保留分配的存储，之后再手动删除：

```yml
apiVersion: storage.k8s.io/v1

kind: StorageClass

metadata:

  name: nfs-client-retained

provisioner: k8s-sigs.io/nfs-subdir-external-provisioner

parameters:

  onDelete: "retain"
```

接下来我们定义一个 PVC，向系统申请 10MB 的存储空间，使用的 StorageClass 是默认的 nfs-client：

```yml
apiVersion: v1

kind: PersistentVolumeClaim

metadata:

  name: nfs-dyn-10m-pvc

spec:

  storageClassName: nfs-client

  accessModes:

    - ReadWriteMany

  resources:

    requests:

      storage: 10Mi
```

写好了 PVC，我们还是在 Pod 里用 volumes 和 volumeMounts 挂载，然后 Kubernetes 就会自动找到 NFS Provisioner，在 NFS 的共享目录上创建出合适的 PV 对象：

```yml
apiVersion: v1

kind: Pod

metadata:

  name: nfs-dyn-pod

spec:

  volumes:

  - name: nfs-dyn-10m-vol

    persistentVolumeClaim:

      claimName: nfs-dyn-10m-pvc

  containers:

    - name: nfs-dyn-test

      image: nginx:alpine

      ports:

      - containerPort: 80

      volumeMounts:

        - name: nfs-dyn-10m-vol

          mountPath: /tmp
```

使用 kubectl apply 创建好 PVC 和 Pod，让我们来查看一下集群里的 PV 状态：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/570d73409db1edc757yy10e6aba56ebb.png)

从截图你可以看到，虽然我们没有直接定义 PV 对象，但由于有 NFS Provisioner，它就自动创建一个 PV，大小刚好是在 PVC 里申请的 10MB。

如果你这个时候再去 NFS 服务器上查看共享目录，也会发现多出了一个目录，名字与这个自动创建的 PV 一样，但加上了名字空间和 PVC 的前缀：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/a9b6942b824bc9f7841850ee15yy68ea.png)

我还是把 Pod、PVC、StorageClass 和 Provisioner 的关系画成了一张图，你可以清楚地看出来这些对象的关联关系，还有 Pod 是如何最终找到存储设备的：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/e3905990be6fb8739fb51a4ab9856f1e.jpg)

## 小结

好了，今天的这节课里我们继续学习 PV/PVC，引入了网络存储系统，以 NFS 为例研究了静态存储卷和动态存储卷的用法，其中的核心对象是 StorageClass 和 Provisioner。

我再小结一下今天的要点：

1. 在 Kubernetes 集群里，网络存储系统更适合数据持久化，NFS 是最容易使用的一种网络存储系统，要事先安装好服务端和客户端。
2. 可以编写 PV 手工定义 NFS 静态存储卷，要指定 NFS 服务器的 IP 地址和共享目录名。
3. 使用 NFS 动态存储卷必须要部署相应的 Provisioner，在 YAML 里正确配置 NFS 服务器。
4. 动态存储卷不需要手工定义 PV，而是要定义 StorageClass，由关联的 Provisioner 自动创建 PV 完成绑定。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/2022f24dcc6a3d76214bbc59c3bd2aff.jpg" alt="img" style="zoom:33%;" />