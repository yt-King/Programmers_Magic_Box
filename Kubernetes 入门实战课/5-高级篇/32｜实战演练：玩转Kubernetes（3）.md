# 32｜实战演练：玩转Kubernetes（3）

到今天，我们的“高级篇”课程也要结束了。比起前面的“初级篇”“中级篇”来说，这里的知识点比较多，难度也要高一些。如果你能够一篇不漏地学习下来，相信一定对 Kubernetes 有更深层次的认识和理解。

今天的这节课还是来对前面的知识做回顾与总结，提炼出文章里的学习要点和重点，你也可以顺便检验一下自己的掌握程度，试试在不回看课程的情况下，自己能不能流畅说出关联的操作细节。

复习之后，我们就来进行最后一次实战演练了。首先会继续改进贯穿课程始终的 WordPress 网站，把 MariaDB 改成 StatefulSet，加上 NFS 持久化存储；然后我们会在 Kubernetes 集群里安装 Dashboard，综合实践 Ingress、namespace 的用法。

## 要点回顾一：API 对象

“高级篇”可以分成三个部分，第一部分讲的是 PersistentVolume、StatefulSet 等 API 对象。

（24 讲）**PersistentVolume 简称 PV，是 Kubernetes 对持久化存储的抽象**，代表了 LocalDisk、NFS、Ceph 等存储设备，和 CPU、内存一样，属于集群的公共资源。

因为不同存储设备之间的差异很大，为了更好地描述 PV 特征，就出现了 StorageClass，它的作用是分类存储设备，让我们更容易去选择 PV 对象。

PV 一般由系统管理员来创建，我们如果要使用 PV 就要用 PVC（PersistentVolumeClaim）去申请，说清楚需求的容量、访问模式等参数，然后 Kubernetes 就会查找最合适的 PV 分配给我们使用。

（25 讲）手动创建 PV 的工作量很大，麻烦而且容易出错，所以就有了“动态存储卷”的概念，需要**在 StorageClass 里绑定一个 Provisioner 对象，由它来代替人工，根据 PVC 自动创建出符合要求的 PV**。

有了 PV 和 PVC，我们就可以在 Pod 里用“persistentVolumeClaim”来引用 PVC，创建出可供容器使用的 Volume，然后在容器里用“volumeMounts”把它挂载到某个路径上，这样容器就可以读写 PV，实现数据的持久化存储了。

（26 讲）持久化存储的一个重要应用领域就是保存应用的状态数据，**管理有状态的应用，就要使用新的对象 StatefulSet**，可以认为它是管理无状态应用对象 Deployment 的一个特例。

StatefulSet 对象的 YAML 描述和 Deployment 非常像，“spec”里只是多了一个“serviceName”字段，但它部署应用的方式却与 Deployment 差距很大。

Deployment 创建的 Pod 是随机的名字，而 StatefulSet 会对 Pod 顺序编号、顺序创建，保证应用有一个确定的启动先后次序，这样就可以实现主从、主备等关系。

在使用 Service 为 StatefulSet 创建服务的时候，它也会为每个 Pod 单独创建域名，同样也是顺序编号，保证 Pod 有稳定的网络标识，外部用户就可以用这个域名来准确地访问到某个具体的 Pod。

StatefulSet 还使用“volumeClaimTemplates”字段来定义持久化存储，里面其实就是一个 PVC，每个 Pod 可以用这个模板来生成自己的 PVC 去申请 PV，实现存储卷与 Pod 的独立绑定。

通过**启动顺序、稳定域名和存储模板**这三个关键能力，StatefulSet 就可以很好地处理 Redis、MySQL 等有状态应用了。

## 要点回顾二：应用管理

“高级篇”第二部分讲的是应用管理，包括滚动更新、资源配额和健康检查等内容。

（27 讲）在 Kubernetes 里部署好应用后，我们还需要对它做持续的运维管理，其中一项任务是版本的更新和回退。

版本更新很简单，只要编写一个新的 YAML（Deployment、DaemonSet、StatefulSet），再用 kubectl apply 应用就可以了。Kubernetes 采用的是**“滚动更新”策略，实际上是两个同步进行的“扩容”和“缩容”动作**，这样在更新的过程中始终会有 Pod 处于可用状态，能够平稳地对外提供服务。

应用的更新历史可以用命令 kubectl rollout history 查看，如果有什么意外，就可以用 kubectl rollout undo 来回退。这两个命令相当于给我们的更新流程上了一个保险，可以放心大胆操作，失败就用“S/L 大法”。

（28 讲）为了让 Pod 里的容器能够稳定运行，我们可以采用**资源配额**和**检查探针**这两种手段。

资源配额能够限制容器申请的 CPU 和内存数量，不至于过多或者过少，保持在一个合理的程度，更有利于 Kubernetes 调度。

检查探针是 Kubernetes 内置的应用监控工具，有 Startup、Liveness、Readiness 三种，分别探测启动、存活、就绪状态，探测的方式也有 exec、tcpSocket、httpGet 三种。组合运用这些就可以灵活地检查容器的状态，Kubernetes 发现不可用就会重启容器，让应用在总体上处于健康水平。

## 要点回顾三：集群管理

“高级篇”第三部分讲的是集群管理，有名字空间、系统监控和网络通信等知识点。

（29 讲）Kubernetes 的集群里虽然有很多计算资源，但毕竟是有限的，除了要给 Pod 加上资源配额，我们也要为集群加上资源配额，方法就是用名字空间，把整体的资源池切分成多个小块，按需分配给不同的用户使用。

名字空间的资源配额使用的是“ResourceQuota”，除了基本的 CPU 和内存，它还能够限制存储容量和各种 API 对象的数量，这样就可以避免多用户互相挤占，更高效地利用集群资源。

（30 讲）系统监控是集群管理的另一个重要方面，Kubernetes 提供了 Metrics Server 和 Prometheus 两个工具：

- **Metrics Server** 专门用来收集 Kubernetes 核心资源指标，可以用 kubectl top 来查看集群的状态，它也是水平自动伸缩对象 HorizontalPodAutoscaler 的前提条件。
- **Prometheus**，继 Kubernetes 之后的第二个 CNCF 毕业项目，是云原生监控领域的“事实标准”，在集群里部署之后就可以用 Grafana 可视化监控各种指标，还可以集成自动报警等功能。

（31 讲）对于底层的基础网络设施，Kubernetes 定义了平坦的网络模型“IP-per-pod”，实现它就要符合 CNI 标准。常用的网络插件有 Flannel、Calico、Cilium 等，Flannel 使用 Overlay 模式，性能较低，Calico 使用 Route 模式，性能较高。

现在，“高级篇”的众多知识要点我们都完整地过了一遍，你是否已经都理解、掌握了它们呢？

## 搭建 WordPress 网站

接下来我们就来在第 22 讲的基础上继续优化 WordPress 网站，其中的关键是让数据库 MariaDB 实现数据持久化。

网站的整体架构图变化不大，前面的 Nginx、WordPress 还是原样，只需要修改 MariaDB：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/7cd3726d03ae12172b9073d1abf9fe1b.jpg)

因为 MariaDB 由 Deployment 改成了 StatefulSet，所以我们要修改 YAML，添加“serviceName”“volumeClaimTemplates”这两个字段，定义网络标识和 NFS 动态存储卷，然后在容器部分用“volumeMounts”挂载到容器里的数据目录“/var/lib/mysql”。

修改后的 YAML 就是这个样子：

```yml
apiVersion: apps/v1

kind: StatefulSet

metadata:

  labels:

    app: maria-sts

  name: maria-sts

spec:

  \# headless svc

  serviceName: maria-svc

  \# pvc

  volumeClaimTemplates:

  - metadata:

      name: maria-100m-pvc

    spec:

      storageClassName: nfs-client

      accessModes:

        - ReadWriteMany

      resources:

        requests:

          storage: 100Mi

  replicas: 1

  selector:

    matchLabels:

      app: maria-sts

  template:

    metadata:

      labels:

        app: maria-sts

    spec:

      containers:

      - image: mariadb:10

        name: mariadb

        imagePullPolicy: IfNotPresent

        ports:

        - containerPort: 3306

        envFrom:

        - prefix: 'MARIADB_'

          configMapRef:

            name: maria-cm

        volumeMounts:

        - name: maria-100m-pvc

          mountPath: /var/lib/mysql
```

改完 MariaDB，我们还要再对 WordPress 做一点小修改。

还记得吗？StatefulSet 管理的每个 Pod 都有自己的域名，所以要把 WordPress 的环境变量改成 MariaDB 的新名字，也就是“**maria-sts-0.maria-svc**”：

```yml
apiVersion: v1

kind: ConfigMap

metadata:

  name: wp-cm

data:

  HOST: 'maria-sts-0.maria-svc'  #注意这里

  USER: 'wp'

  PASSWORD: '123'

  NAME: 'db'
```

改完这两个 YAML，我们就可以逐个创建 MariaDB、WordPress、Ingress 等对象了。

和之前一样，访问 NodePort 的“30088”端口，或者是用 Ingress Controller 的“wp.test”域名，都可以进入 WordPress 网站：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/fc3b52f96f138f01b23e3a7487730746.png)

StatefulSet 的持久化存储是否生效了呢？

你可以把这些对象都删除后重新创建，再进入网站，看看是否原来的数据依然存在。或者更简单一点，直接查看 NFS 的存储目录，应该可以看到 MariaDB 生成的一些数据库文件：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/428886b77e4797dc7ded5a43yyc0b218.png)

这两种方式都能够证明，我们的 MariaDB 使用 StatefulSet 部署后数据已经保存在了磁盘上，不会因为对象的销毁而丢失。

到这里，第一个小实践你就已经完成了，给自己鼓鼓劲，我们一起来做第二个实践，在 Kubernetes 集群里安装 Dashboard。

## 部署 Dashboard

在“初级篇”的实战演练课里（第 15 讲），我简单介绍了 Kubernetes 的图形管理界面，也就是 Dashboard，不知道你是否还有印象。当时 Dashboard 是直接内置在 minikube 里的，不需要安装，一个命令启动，就能在浏览器里直观地管理 Kubernetes 集群了，非常方便。

那现在我们用 kubeadm 部署了实际的多节点集群，能否也用上 Dashboard 呢？接下来我就带你来一起动手，从零开始安装 Dashboard。

首先，你应该先去 Dashboard 的项目网站（https://github.com/kubernetes/dashboard），看一下它的说明文档，了解一下它的基本情况。

它的安装很简单，只需要一个 YAML 文件，可以直接下载：

wget https://raw.githubusercontent.com/kubernetes/dashboard/v2.6.0/aio/deploy/recommended.yaml

这个 YAML 里包含了很多对象，虽然文件比较大，但现在的你应该基本都能够看懂了，要点有这么几个：

- 所有的对象都属于“kubernetes-dashboard”名字空间。
- Dashboard 使用 Deployment 部署了一个实例，端口号是 8443。
- 容器启用了 Liveness 探针，使用 HTTPS 方式检查存活状态。
- Service 对象使用的是 443 端口，它映射了 Dashboard 的 8443 端口。

使用命令 kubectl apply 就可以轻松部署 Dashboard 了：

kubectl apply -f dashboard.yaml

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/c56f8936e187047a2b7d100f7ae0f779.png)

## 部署 Ingress/Ingress Controller

不过，为了给我们的实战增加一点难度，我们可以在前面配一个 Ingress 入口，用反向代理的方式来访问它。

由于 Dashboard 默认使用的是加密的 HTTPS 协议，拒绝明文 HTTP 访问，所以我们要先生成证书，让 Ingress 也走 HTTPS 协议。

简单起见，我直接用 Linux 里的命令行工具“openssl”来生成一个自签名的证书（如果你有条件，也可以考虑找 CA 网站申请免费证书）：

```bash
openssl req -x509 -days 365 -out k8s.test.crt -keyout k8s.test.key \

  -newkey rsa:2048 -nodes -sha256 \

    -subj '/CN=k8s.test' -extensions EXT -config <( \

       printf "[dn]\nCN=k8s.test\n[req]\ndistinguished_name = dn\n[EXT]\nsubjectAltName=DNS:k8s.test\nkeyUsage=digitalSignature\nextendedKeyUsage=serverAuth")
```

openssl 的命令比较长，我简单解释一下：它生成的是一个 X509 格式的证书，有效期 365 天，私钥是 RSA2048 位，摘要算法是 SHA256，签发的网站是“k8s.test”。

运行命令行后会生成两个文件，一个是证书“k8s.test.crt”，另一个是私钥“k8s.test.key”，我们需要把这两个文件存入 Kubernetes 里供 Ingress 使用。

因为这两个文件属于机密信息，存储的方式当然就是用 Secret 了。你仍然可以用命令 kubectl create secret 来自动创建 YAML，不过类型不是“generic”，而是“tls”，同时还要用 -n 指定名字空间，用 --cert、--key 指定文件：

```bash
export out="--dry-run=client -o yaml"

kubectl create secret tls dash-tls -n kubernetes-dashboard --cert=k8s.test.crt --key=k8s.test.key $out > cert.yml
```

出来的 YAML 大概是这个样子：

```yml
apiVersion: v1

kind: Secret

metadata:

  name: dash-tls

  namespace: kubernetes-dashboard

type: kubernetes.io/tls

data:

  tls.crt: LS0tLS1CRUdJTiBDRVJU...

  tls.key: LS0tLS1CRUdJTiBQUklW...
```

创建这个 Secret 对象之后，你可以再用 kubectl describe 来检查它的状态：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/2615d5c6c3yy704cc63c5bf6df5b87cc.png)

接下来我们就来编写 Ingress Class 和 Ingress 对象，为了保持名字空间的整齐，也把它放在“kubernetes-dashboard”名字空间里。

Ingress Class 对象很简单，名字是“dash-ink”，指定 Controller 还是我们之前用的 Nginx 官方的 Ingress Controller：

```yml
apiVersion: networking.k8s.io/v1

kind: IngressClass

metadata:

  name: dash-ink

  namespace: kubernetes-dashboard

spec:

  controller: nginx.org/ingress-controller
```

Ingress 对象可以用 kubectl create 命令自动生成，如果你有点忘记的话，可以回头参考一下(第 21 讲)：

```bash
kubectl create ing dash-ing --rule="k8s.test/=kubernetes-dashboard:443" --class=dash-ink -n kubernetes-dashboard $out
```

但这次因为是 HTTPS 协议，所以我们要在 Ingress 里多加一点东西，一个是“**annotations**”字段，指定后端目标是 HTTPS 服务，另一个是“**tls**”字段，指定域名和证书，也就是刚才创建的 Secret：

```yml
apiVersion: networking.k8s.io/v1

kind: Ingress

metadata:

  name: dash-ing

  namespace: kubernetes-dashboard

  annotations:

    nginx.org/ssl-services: "kubernetes-dashboard"

spec:

  ingressClassName: dash-ink

  tls:

    - hosts:

      - k8s.test

      secretName: dash-tls

  rules:

  - host: k8s.test

    http:

      paths:

      - path: /

        pathType: Prefix

        backend:

          service:

            name: kubernetes-dashboard

            port:

              number: 443
```

最后一个对象，就是 Ingress Controller 了，还是拿现成的模板修改，记得要把“args”里的 Ingress Class 改成我们自己的“dash-ink”：

```yml
apiVersion: apps/v1

kind: Deployment

metadata:

  name: dash-kic-dep

  namespace: nginx-ingress

spec:

  ...

        args:

          - -ingress-class=dash-ink
```

要让我们在外面能够访问 Ingress Controller，还要为它再定义一个 Service，类型是“NodePort”，端口指定是“30443”：

```yml
apiVersion: v1

kind: Service

metadata:

  name: dash-kic-svc

  namespace: nginx-ingress

spec:

  ports:

  - port: 443

    protocol: TCP

    targetPort: 443

    nodePort: 30443

  selector:

    app: dash-kic-dep

  type: NodePort
```

把上面的 Secret、Ingress Class、Ingress、Ingress Controller、Service 都创建好之后，我们再来确认一下它们的运行状态：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/4062d4e5c8c57f74a480ee21ca3717b2.png)

因为这些对象比较多，处于不同的名字空间，关联有点复杂，我画了一个简单的示意图，你可以看一下：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/b720648a0fefab28fa940b7cd6afb350.jpg)

## 访问 Dashboard

到这里，Dashboard 的部署工作就基本完成了。为了能正常访问，我们还要为它创建一个用户，才能登录进 Dashboard。

Dashboard 的网站上有一个简单示例（https://github.com/kubernetes/dashboard/blob/master/docs/user/access-control/creating-sample-user.md），我们直接拿来用就行：

```yml
apiVersion: v1

kind: ServiceAccount

metadata:

  name: admin-user

  namespace: kubernetes-dashboard

---

apiVersion: rbac.authorization.k8s.io/v1

kind: ClusterRoleBinding

metadata:

  name: admin-user

roleRef:

  apiGroup: rbac.authorization.k8s.io

  kind: ClusterRole

  name: cluster-admin

subjects:

- kind: ServiceAccount

  name: admin-user

  namespace: kubernetes-dashboard
```

这个 YAML 创建了一个 Dashboard 的管理员账号，名字叫“admin-user”，使用的是 Kubernetes 的 RBAC 机制，就不展开细讲了。

这个账号不能用简单的“用户名 + 密码”的方式登录，需要用到一个 Token，可以用 kubectl get secret、kubectl describe secret 查到：

```bash
kubectl get secret -n kubernetes-dashboard

kubectl describe secrets -n kubernetes-dashboard admin-user-token-xxxx
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/0ffd4627b0efa2ba5774bf5c65faa1yy.png)

Token 是一个很长的字符串，把它拷贝存好，再为它的测试域名“k8s.test”加上域名解析（修改 /etc/hosts），然后我们就可以在浏览器里输入网址“https://k8s.test:30443”访问 Dashboard 了：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/c83cd71ab4d6696f5b837ea20056ff5d.png)

下面的两张截图就是我查看集群里“kube-system”名字空间的情况，由于我们之前安装了 Metrics Server，所以 Dashboard 也能够以图形的方式显示 CPU 和内存状态，有那么一点 Prometheus + Grafana 的意思：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/3ca6e156150a6a06477bb2eb07e00cd9.png)

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/fae2168c30677d2370e8e71c3d98f1b2.png)

## 小结

好了，今天我们一起回顾了“高级篇”里的要点，下面的这张思维导图就是对这些知识点的全面总结，你可以再认真研究一下：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/4a9bb79b2e54096yyf5c5799837dd930.jpg)

今天我们有两个实战项目。首先是 WordPress，把后端的存储服务 MariaDB 改造成了 StatefulSet，挂载了 NFS 网盘，这样就实现了一个功能比较完善的网站，达到了基本可用的程度。

接着我们又在 Kubernetes 里安装了 Dashboard，主要部署在名字空间“kubernetes-dashboard”。Dashboard 自身的安装很简单，但我们又为它在前面搭建了一个反向代理，配上了安全证书，进一步实践了 Ingress 的用法。

不过这两个项目还没有完全覆盖“高级篇”的内容，你可以再接着改进它们，比如加上健康检查、资源配额、自动水平伸缩等，多动手来巩固所学的知识。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/aa71ca9df15c8141f3368cce8b41dc9f.jpg" alt="img" style="zoom:33%;" />