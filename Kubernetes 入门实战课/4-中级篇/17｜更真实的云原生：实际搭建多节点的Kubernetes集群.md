# 17｜更真实的云原生：实际搭建多节点的Kubernetes集群

到今天，你学习这个专栏的进度就已经过半了，在前面的“入门篇”我们了解了 Docker 和容器技术，在“初级篇”我们掌握了 Kubernetes 的基本对象、原理和操作方法，一路走下来收获很多。

现在你应该对 Kubernetes 和容器编排有了一些初步的认识，那么接下来，让我们继续深入研究 Kubernetes 的其他 API 对象，也就是那些在 Docker 中不存在的但对云计算、集群管理至关重要的概念。

不过在那之前，我们还需要有一个比 minikube 更真实的 Kubernetes 环境，它应该是一个多节点的 Kubernetes 集群，这样更贴近现实中的生产系统，能够让我们尽快地拥有实际的集群使用经验。

所以在今天的这节课里，我们就来暂时忘掉 minikube，改用 kubeadm（https://kubernetes.io/zh/docs/reference/setup-tools/kubeadm/）搭建出一个新的 Kubernetes 集群，一起来看看更真实的云原生环境。

## 什么是 kubeadm

前面的几节课里我们使用的都是 minikube，它非常简单易用，不需要什么配置工作，就能够在单机环境里创建出一个功能完善的 Kubernetes 集群，给学习、开发、测试都带来了极大的便利。

不过 minikube 还是太“迷你”了，方便的同时也隐藏了很多细节，离真正生产环境里的计算集群有一些差距，毕竟许多需求、任务只有在多节点的大集群里才能够遇到，相比起来，minikube 真的只能算是一个“玩具”。

那么，多节点的 Kubernetes 集群是怎么从无到有地创建出来的呢？

第 10 讲说过 Kubernetes 是很多模块构成的，而实现核心功能的组件像 apiserver、etcd、scheduler 等本质上都是可执行文件，所以也可以采用和其他系统差不多的方式，使用 Shell 脚本或者 Ansible 等工具打包发布到服务器上。

不过 Kubernetes 里的这些组件的配置和相互关系实在是太复杂了，用 Shell、Ansible 来部署的难度很高，需要具有相当专业的运维管理知识才能配置、搭建好集群，而且即使这样，搭建的过程也非常麻烦。

为了简化 Kubernetes 的部署工作，让它能够更“接地气”，社区里就出现了一个专门用来在集群中安装 Kubernetes 的工具，名字就叫“**kubeadm**”，意思就是“Kubernetes 管理员”。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/f27c7938cba21215621ac33635d63288.jpg" alt="img" style="zoom:33%;" />

kubeadm，原理和 minikube 类似，也是用容器和镜像来封装 Kubernetes 的各种组件，但它的目标不是单机部署，而是要能够轻松地在集群环境里部署 Kubernetes，并且让这个集群接近甚至达到生产级质量。

而在保持这个高水准的同时，kubeadm 还具有了和 minikube 一样的易用性，只要很少的几条命令，如 init、join、upgrade、reset 就能够完成 Kubernetes 集群的管理维护工作，这让它不仅适用于集群管理员，也适用于开发、测试人员。

## 实验环境的架构是什么样的

在使用 kubeadm 搭建实验环境之前，我们先来看看集群的架构设计，也就是说要准备好集群所需的硬件设施。

这里我画了一张系统架构图，图里一共有 3 台主机，当然它们都是使用虚拟机软件 VirtualBox/VMWare 虚拟出来的，下面我来详细说明一下：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/yyf5db64d398b4d5dyyd5e8e23ece53e.jpg)

所谓的多节点集群，要求服务器应该有两台或者更多，为了简化我们只取最小值，所以这个 Kubernetes 集群就只有两台主机，一台是 Master 节点，另一台是 Worker 节点。当然，在完全掌握了 kubeadm 的用法之后，你可以在这个集群里添加更多的节点。

Master 节点需要运行 apiserver、etcd、scheduler、controller-manager 等组件，管理整个集群，所以对配置要求比较高，至少是 2 核 CPU、4GB 的内存。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/d19a8ceafd4db10a5yy35c623384ba3c.png" alt="img" style="zoom:50%;" />

而 Worker 节点没有管理工作，只运行业务应用，所以配置可以低一些，为了节省资源我给它分配了 1 核 CPU 和 1GB 的内存，可以说是低到不能再低了。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/eeee60b6e29d7b6c4c74f913ac663ef3.png" alt="img" style="zoom:50%;" />

基于模拟生产环境的考虑，在 Kubernetes 集群之外还需要有一台起辅助作用的服务器。

它的名字叫 Console，意思是控制台，我们要在上面安装命令行工具 kubectl，所有对 Kubernetes 集群的管理命令都是从这台主机发出去的。这也比较符合实际情况，因为安全的原因，集群里的主机部署好之后应该尽量少直接登录上去操作。

要提醒你的是，Console 这台主机只是逻辑上的概念，不一定要是独立，你在实际安装部署的时候完全可以复用之前 minikube 的虚拟机，或者直接使用 Master/Worker 节点作为控制台。

这 3 台主机共同组成了我们的实验环境，所以在配置的时候要注意它们的网络选项，必须是在同一个网段，你可以再回顾一下**课前准备**，保证它们使用的是同一个“Host-Only”（VirtualBox）或者“自定”（VMWare Fusion）网络。

## 安装前的准备工作

不过有了架构图里的这些主机之后，我们还不能立即开始使用 kubeadm 安装 Kubernetes，因为 Kubernetes 对系统有一些特殊要求，我们必须还要在 Master 和 Worker 节点上做一些准备。

这些工作的详细信息你都可以在 Kubernetes 的官网上找到，但它们分散在不同的文档里，比较凌乱，所以我把它们整合到了这里，包括改主机名、改 Docker 配置、改网络设置、改交换分区这四步。

第一，由于 Kubernetes 使用主机名来区分集群里的节点，所以每个节点的 hostname 必须不能重名。你需要修改“**/etc/hostname**”这个文件，把它改成容易辨识的名字，比如 Master 节点就叫 master，Worker 节点就叫 worker：

```bash
sudo vi /etc/hostname
```

第二，虽然 Kubernetes 目前支持多种容器运行时，但 Docker 还是最方便最易用的一种，所以我们仍然继续使用 Docker 作为 Kubernetes 的底层支持，使用 apt 安装 Docker Engine（可参考第 1 讲）。

安装完成后需要你再对 Docker 的配置做一点修改，在“**/etc/docker/daemon.json**”里把 cgroup 的驱动程序改成 systemd ，然后重启 Docker 的守护进程，具体的操作我列在了下面：

```bash
cat <<EOF | sudo tee /etc/docker/daemon.json

{

  "exec-opts": ["native.cgroupdriver=systemd"],

  "log-driver": "json-file",

  "log-opts": {

    "max-size": "100m"

  },

  "storage-driver": "overlay2"

}

EOF

sudo systemctl enable docker

sudo systemctl daemon-reload

sudo systemctl restart docker
```

第三，为了让 Kubernetes 能够检查、转发网络流量，你需要修改 iptables 的配置，启用“br_netfilter”模块：

```bash
cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf

br_netfilter

EOF

cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf

net.bridge.bridge-nf-call-ip6tables = 1

net.bridge.bridge-nf-call-iptables = 1

net.ipv4.ip_forward=1 # better than modify /etc/sysctl.conf

EOF

sudo sysctl --system
```

第四，你需要修改“**/etc/fstab**”，关闭 Linux 的 swap 分区，提升 Kubernetes 的性能：

```bash
sudo swapoff -a

sudo sed -ri '/\sswap\s/s/^#?/#/' /etc/fstab
```

完成之后，最好记得重启一下系统，然后给虚拟机拍个快照做备份，避免后续的操作失误导致重复劳动。

## 安装 kubeadm

好，现在我们就要安装 kubeadm 了，在 Master 节点和 Worker 节点上都要做这一步。

kubeadm 可以直接从 Google 自己的软件仓库下载安装，但国内的网络不稳定，很难下载成功，需要改用其他的软件源，这里我选择了国内的某云厂商：

```bash
sudo apt install -y apt-transport-https ca-certificates curl

curl https://mirrors.aliyun.com/kubernetes/apt/doc/apt-key.gpg | sudo apt-key add -

cat <<EOF | sudo tee /etc/apt/sources.list.d/kubernetes.list

deb https://mirrors.aliyun.com/kubernetes/apt/ kubernetes-xenial main

EOF

sudo apt update
```

更新了软件仓库，我们就可以用 apt install 获取 kubeadm、kubelet 和 kubectl 这三个安装必备工具了。apt 默认会下载最新版本，但我们也可以指定版本号，比如使用和 minikube 相同的“1.23.3”：

```bash
sudo apt install -y kubeadm=1.23.3-00 kubelet=1.23.3-00 kubectl=1.23.3-00
```

安装完成之后，你可以用 kubeadm version、kubectl version 来验证版本是否正确：

```bash
kubeadm version

kubectl version --client
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/72d79f46d9132af0dca110d982eff1c9.png" alt="img" style="zoom:50%;" />

另外按照 Kubernetes 官网的要求，我们最好再使用命令 apt-mark hold ，锁定这三个软件的版本，避免意外升级导致版本错误：

```bash
sudo apt-mark hold kubeadm kubelet kubectl
```

## 下载 Kubernetes 组件镜像

前面我说过，kubeadm 把 apiserver、etcd、scheduler 等组件都打包成了镜像，以容器的方式启动 Kubernetes，但这些镜像不是放在 Docker Hub 上，而是放在 Google 自己的镜像仓库网站 gcr.io，而它在国内的访问很困难，直接拉取镜像几乎是不可能的。

所以我们需要采取一些变通措施，提前把镜像下载到本地。

使用命令 kubeadm config images list 可以查看安装 Kubernetes 所需的镜像列表，参数 --kubernetes-version 可以指定版本号：

```bash
kubeadm config images list --kubernetes-version v1.23.3

k8s.gcr.io/kube-apiserver:v1.23.3

k8s.gcr.io/kube-controller-manager:v1.23.3

k8s.gcr.io/kube-scheduler:v1.23.3

k8s.gcr.io/kube-proxy:v1.23.3

k8s.gcr.io/pause:3.6

k8s.gcr.io/etcd:3.5.1-0

k8s.gcr.io/coredns/coredns:v1.8.6
```

知道了镜像的名字和标签就好办了，我们有两种方法可以比较容易地获取这些镜像。

第一种方法是利用 minikube。因为 minikube 本身也打包了 Kubernetes 的组件镜像，所以完全可以从它的节点里把这些镜像导出之后再拷贝过来。

具体做法也很简单，先启动 minikube，然后 minikube ssh 登录进虚拟节点，用 docker save -o 命令把相应版本的镜像都保存下来，再用 minikube cp 拷贝到本地，剩下的事情就不用我多说了：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/6609a62525bbf5d77eb7331f9835244f.png" alt="img" style="zoom:50%;" />

这种方法安全可靠，不过操作上麻烦了些，所以就有了第二种方法，从国内的镜像网站下载然后再用 docker tag 改名，能够使用 Shell 编程实现自动化：

```shell
repo=registry.aliyuncs.com/google_containers

for name in `kubeadm config images list --kubernetes-version v1.23.3`; do

    src_name=${name#k8s.gcr.io/}

    src_name=${src_name#coredns/}

    docker pull $repo/$src_name

    docker tag $repo/$src_name $name

    docker rmi $repo/$src_name

done
```

第二种方法速度快，但也有隐患，万一网站不提供服务，或者改动了镜像就比较危险了。

所以你可以把这两种方法结合起来，先用脚本从国内镜像仓库下载，然后再用 minikube 里的镜像做对比，只要 IMAGE ID 是一样就说明镜像是正确的。

这张截图就是 Kubernetes 1.23.3 的镜像列表（amd64/arm64），你在安装时可以参考：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/11d9d4c91b08d95e82e75406a4d3aa5c.png" alt="img" style="zoom: 50%;" />



<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/528d9913620015f594988e648eeac66c.png" alt="img" style="zoom:50%;" />

## 安装 Master 节点

准备工作都做好了，现在就可以开始正式安装 Kubernetes 了，我们先从 Master 节点开始。

kubeadm 的用法非常简单，只需要一个命令 kubeadm init 就可以把组件在 Master 节点上运行起来，不过它还有很多参数用来调整集群的配置，你可以用 -h 查看。这里我只说一下我们实验环境用到的 3 个参数：

- **--pod-network-cidr**，设置集群里 Pod 的 IP 地址段。
- **--apiserver-advertise-address**，设置 apiserver 的 IP 地址，对于多网卡服务器来说很重要（比如 VirtualBox 虚拟机就用了两块网卡），可以指定 apiserver 在哪个网卡上对外提供服务。
- **--kubernetes-version**，指定 Kubernetes 的版本号。

下面的这个安装命令里，我指定了 Pod 的地址段是“10.10.0.0/16”，apiserver 的服务地址是“192.168.10.210”，Kubernetes 的版本号是“1.23.3”：

```bash
sudo kubeadm init \

    --pod-network-cidr=10.10.0.0/16 \

    --apiserver-advertise-address=192.168.10.210 \

    --kubernetes-version=v1.23.3
```

因为我们已经提前把镜像下载到了本地，所以 kubeadm 的安装过程很快就完成了，它还会提示出接下来要做的工作：

```bash
To start using your cluster, you need to run the following as a regular user:

  mkdir -p $HOME/.kube

  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config

  sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

意思是要在本地建立一个“**.kube**”目录，然后拷贝 kubectl 的配置文件，你只要原样拷贝粘贴就行。

另外还有一个很重要的“**kubeadm join**”提示，其他节点要加入集群必须要用指令里的 token 和 ca 证书，所以这条命令务必拷贝后保存好：

```bash
Then you can join any number of worker nodes by running the following on each as root:

kubeadm join 192.168.10.210:6443 --token tv9mkx.tw7it9vphe158e74 \

  --discovery-token-ca-cert-hash sha256:e8721b8630d5b562e23c010c70559a6d3084f629abad6a2920e87855f8fb96f3
```

安装完成后，你就可以使用 kubectl version、kubectl get node 来检查 Kubernetes 的版本和集群的节点状态了：

```bash
kubectl version

kubectl get node
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/c63ce96bfyy0e1bc2927d575a66ee209.png" alt="img" style="zoom:50%;" />

你会注意到 Master 节点的状态是“NotReady”，这是由于还缺少网络插件，集群的内部网络还没有正常运作。

## 安装 Flannel 网络插件

Kubernetes 定义了 CNI 标准，有很多网络插件，这里我选择最常用的 **Flannel**，可以在它的 GitHub 仓库里（https://github.com/flannel-io/flannel/）找到相关文档。

它安装也很简单，只需要使用项目的“**kube-flannel.yml**”在 Kubernetes 里部署一下就好了。不过因为它应用了 Kubernetes 的网段地址，你需要修改文件里的“**net-conf.json**”字段，把 Network 改成刚才 kubeadm 的参数 --pod-network-cidr 设置的地址段。

比如在这里，就要修改成“10.10.0.0/16”：

```json
  net-conf.json: |
    {
      "Network": "10.10.0.0/16",
      "Backend": {
        "Type": "vxlan"
      }
    }
```

改好后，你就可以用 kubectl apply 来安装 Flannel 网络了：

```bash
kubectl apply -f kube-flannel.yml
```

稍等一小会，等镜像拉取下来并运行之后，你就可以执行 kubectl get node 来看节点状态：

```bash
kubectl get node
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/6a3c852abe5b193a6997b154163ed67a.png" alt="img" style="zoom:50%;" />

这时你应该能够看到 Master 节点的状态是“Ready”，表明节点网络也工作正常了。

## 安装 Worker 节点

如果你成功安装了 Master 节点，那么 Worker 节点的安装就简单多了，只需要用之前拷贝的那条 kubeadm join 命令就可以了，记得要用 sudo 来执行：

```bash
sudo \

kubeadm join 192.168.10.210:6443 --token tv9mkx.tw7it9vphe158e74 \

  --discovery-token-ca-cert-hash sha256:e8721b8630d5b562e23c010c70559a6d3084f629abad6a2920e87855f8fb96f3
```

它会连接 Master 节点，然后拉取镜像，安装网络插件，最后把节点加入集群。

当然，这个过程中同样也会遇到拉取镜像的问题，你可以如法炮制，提前把镜像下载到 Worker 节点本地，这样安装过程中就不会再有障碍了。

Worker 节点安装完毕后，执行 kubectl get node ，就会看到两个节点都是“Ready”状态：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/f756ece9e81af80a7204243f15777026.png)

现在让我们用 kubectl run ，运行 Nginx 来测试一下：

```bash
kubectl run ngx --image=nginx:alpine

kubectl get pod -o wide
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/73651e5f178e2daf6eaf7ac262e230e9.png)

会看到 Pod 运行在 Worker 节点上，IP 地址是“10.10.1.2”，表明我们的 Kubernetes 集群部署成功。

## 小结

好了，把 Master 节点和 Worker 节点都安装好，我们今天的任务就算是基本完成了。

后面 Console 节点的部署工作更加简单，它只需要安装一个 kubectl，然后复制“config”文件就行，你可以直接在 Master 节点上用“scp”远程拷贝，例如：

```bash
scp `which kubectl` chrono@192.168.10.208:~/

scp ~/.kube/config chrono@192.168.10.208:~/.kube
```

今天的过程多一些，要点我列在了下面：

1. kubeadm 是一个方便易用的 Kubernetes 工具，能够部署生产级别的 Kubernetes 集群。
2. 安装 Kubernetes 之前需要修改主机的配置，包括主机名、Docker 配置、网络设置、交换分区等。
3. Kubernetes 的组件镜像存放在 gcr.io，国内下载比较麻烦，可以考虑从 minikube 或者国内镜像网站获取。
4. 安装 Master 节点需要使用命令 kubeadm init，安装 Worker 节点需要使用命令 kubeadm join，还要部署 Flannel 等网络插件才能让集群正常工作。

因为这些操作都是各种 Linux 命令，全手动敲下来确实很繁琐，所以我把这些步骤都做成了 Shell 脚本放在了 GitHub 上（https://github.com/chronolaw/k8s_study/tree/master/admin），你可以下载后直接运行。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/d3d76937e5f4eb6545a07b96bc731e41.jpg" alt="img" style="zoom:33%;" />