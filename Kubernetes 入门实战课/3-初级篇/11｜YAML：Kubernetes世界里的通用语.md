# 11｜YAML：Kubernetes世界里的通用语

在上次课里，我们一起研究了 Kubernetes 的内部架构和组成，知道它分为控制面和数据面。控制面管理集群，数据面跑业务应用，节点内部又有 apiserver、etcd、scheduler、kubelet、kube-proxy 等组件，它们互相协作来维护整个集群的稳定运行。

这套独特的 Master/Node 架构是 Kubernetes 得以安身立命的根本，但仅依靠这套“内功心法”是不是就能够随意仗剑走天涯了呢？

显然不行。就像许多武侠、玄幻作品里的人物一样，Kubernetes 也需要一份“招式秘籍”才能把自己的“内功”完全发挥出来，只有内外兼修才能够达到笑傲江湖的境界。

而这份“招式秘籍”，就是 Kubernetes 世界里的标准工作语言 YAML，所以今天，我就来讲讲为什么要有 YAML、它是个什么样子、该怎么使用。

## 声明式与命令式是怎么回事

Kubernetes 使用的 YAML 语言有一个非常关键的特性，叫“声明式”（Declarative），对应的有另外一个词：“命令式”（Imperative）。

所以在详细了解 YAML 之前，我们得先来看看“**声明式**”与“**命令式**”这两种工作方式，它们在计算机世界里的关系有点像小说里的“剑宗”与“气宗”。

我们在入门篇里学习的 Docker 命令和 Dockerfile 就属于“命令式”，大多数编程语言也属于命令式，它的特点是交互性强，注重顺序和过程，你必须“告诉”计算机每步该做什么，所有的步骤都列清楚，这样程序才能够一步步走下去，最后完成任务，显得计算机有点“笨”。

“声明式”，在 Kubernetes 出现之前比较少见，它与“命令式”完全相反，不关心具体的过程，更注重结果。我们不需要“教”计算机该怎么做，只要告诉它一个目标状态，它自己就会想办法去完成任务，相比起来自动化、智能化程度更高。

这两个概念比较抽象，不太好理解，也是 Kubernetes 初学者经常遇到的障碍之一。Kubernetes 官网上特意以空调为例，解说“声明式”的原理，但我感觉还是没有说得太清楚，所以这里我就再以“打车”来形象地解释一下“命令式”和“声明式”的区别。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/781e4bcc7c8c89263cca2710488a8152.jpg" alt="img" style="zoom:33%;" />

假设你要打车去高铁站，但司机不熟悉路况，你就只好不厌其烦地告诉他该走哪条路、在哪个路口转向、在哪里进出主路、停哪个站口。虽然最后到达了目的地，但这一路上也费了很多口舌，发出了无数的“命令”。很显然，这段路程就属于“命令式”。

现在我们来换一种方式，同样是去高铁站，但司机经验丰富，他知道哪里有拥堵、哪条路的红绿灯多、哪段路有临时管控、哪里可以抄小道，此时你再多嘴无疑会干扰他的正常驾驶，所以，你只要给他一个“声明”：我要去高铁站，接下来就可以舒舒服服地躺在后座上休息，顺利到达目的地了。

在这个“打车”的例子里，Kubernetes 就是这样的一位熟练的司机，Master/Node 架构让它对整个集群的状态了如指掌，内部的众多组件和插件也能够自动监控管理应用。

这个时候我们再用“命令式”跟它打交道就不太合适了，因为它知道的信息比我们更多更全面，不需要我们这个外行去指导它这个内行，所以我们最好是做一个“**甩手掌柜**”，用“声明式”把任务的目标告诉它，比如使用哪个镜像、什么时候运行，让它自己去处理执行过程中的细节。

那么，该用什么方式去给 Kubernetes 发出一个“声明”呢？

容器技术里的 Shell 脚本和 Dockerfile 可以很好地描述“命令式”，但对于“声明式”就不太合适了，这个时候，我们需要使用专门的 YAML 语言。

## 什么是 YAML

YAML 语言创建于 2001 年，比 XML 晚了三年。XML 你应该知道吧，它是一种类似 HTML 的标签式语言，有很多繁文缛节。而 YAML 虽然在名字上模仿了 XML，但实质上与 XML 完全不同，更适合人类阅读，计算机解析起来也很容易。

YAML 的官网（https://yaml.org/）有对语言规范的完整介绍，所以我就不在这里列举语言的细节了，只讲一些与 Kubernetes 相关的要点，帮助你快速掌握。

你需要知道，**YAML 是 JSON 的超集**，支持整数、浮点数、布尔、字符串、数组和对象等数据类型。也就是说，任何合法的 JSON 文档也都是 YAML 文档，如果你了解 JSON，那么学习 YAML 会容易很多。

但和 JSON 比起来，YAML 的语法更简单，形式也更清晰紧凑，比如：

- 使用空白与缩进表示层次（有点类似 Python），可以不使用花括号和方括号。
- 可以使用 # 书写注释，比起 JSON 是很大的改进。
- 对象（字典）的格式与 JSON 基本相同，但 Key 不需要使用双引号。
- 数组（列表）是使用 - 开头的清单形式（有点类似 MarkDown）。
- 表示对象的 : 和表示数组的 - 后面都必须要有空格。
- 可以使用 --- 在一个文件里分隔多个 YAML 对象。

下面我们来看几个 YAML 的简单示例。

首先是数组，它使用 - 列出了三种操作系统：

```yml
# YAML数组(列表)

OS:
  - linux
  - macOS
  - Windows
```

这段 YAML 对应的 JSON 如下：

```json
{
  "OS": ["linux", "macOS", "Windows"]
}
```

对比可以看到 YAML 形式上很简单，没有闭合花括号、方括号的麻烦，每个元素后面也不需要逗号。

再来看一个 YAML 对象，声明了 1 个 Master 节点，3 个 Worker 节点：

```yml
# YAML对象(字典)
Kubernetes:
  master: 1
  worker: 3
```

它等价的 JSON 如下：

```json
{
  "Kubernetes": {
    "master": 1,
    "worker": 3
  }
}
```

注意到了吗 YAML 里的 Key 都不需要使用双引号，看起来更舒服。

把 YAML 的数组、对象组合起来，我们就可以描述出任意的 Kubernetes 资源对象，第三个例子略微复杂点，你可以自己尝试着解释一下：

```yml
# 复杂的例子，组合数组和对象

Kubernetes:

  master:
    - apiserver: running
    - etcd: running

  node:
    - kubelet: running
    - kube-proxy: down
    - container-runtime: [docker, containerd, cri-o]
```

关于 YAML 语言的其他知识点我就不再一一细说了，都整理在了这张图里，你可以参考YAML 官网，在今后的课程中慢慢体会。

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/d4f3d4cc27a8a4a70d4898b41efebf04.jpg)

## 什么是 API 对象

学到这里还不够，因为 YAML 语言只相当于“语法”，要与 Kubernetes 对话，我们还必须有足够的“词汇”来表示“语义”。

那么应该声明 Kubernetes 里的哪些东西，才能够让 Kubernetes 明白我们的意思呢？

作为一个集群操作系统，Kubernetes 归纳总结了 Google 多年的经验，在理论层面抽象出了很多个概念，用来描述系统的管理运维工作，这些概念就叫做“**API 对象**”。说到这个名字，你也许会联想到上次课里讲到的 Kubernetes 组件 **apiserver**。没错，它正是来源于此。

因为 apiserver 是 Kubernetes 系统的唯一入口，外部用户和内部组件都必须和它通信，而它采用了 HTTP 协议的 URL 资源理念，API 风格也用 RESTful 的 GET/POST/DELETE 等等，所以，这些概念很自然地就被称为是“API 对象”了。

那都有哪些 API 对象呢？

你可以使用 kubectl api-resources 来查看当前 Kubernetes 版本支持的所有对象：

```bash
kubectl api-resources
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/b259e8bfbd0d15b796228d92ede42a91.png)

在输出的“NAME”一栏，就是对象的名字，比如 ConfigMap、Pod、Service 等等，第二栏“SHORTNAMES”则是这种资源的简写，在我们使用 kubectl 命令的时候很有用，可以少敲几次键盘，比如 Pod 可以简写成 po，Service 可以简写成 svc。

在使用 kubectl 命令的时候，你还可以加上一个参数 --v=9，它会显示出详细的命令执行过程，清楚地看到发出的 HTTP 请求，比如：

```bash
kubectl get pod --v=9
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/3fe4823f6ba10600e63c197487e84931.png)

从截图里可以看到，kubectl 客户端等价于调用了 curl，向 8443 端口发送了 HTTP GET 请求，URL 是 /api/v1/namespaces/default/pods。

目前的 Kubernetes 1.23 版本有 50 多种 API 对象，全面地描述了集群的节点、应用、配置、服务、账号等等信息，apiserver 会把它们都存储在数据库 etcd 里，然后 kubelet、scheduler、controller-manager 等组件通过 apiserver 来操作它们，就在 API 对象这个抽象层次实现了对整个集群的管理。

## 如何描述 API 对象

现在我们就来看看如何以 YAML 语言，使用“声明式”在 Kubernetes 里描述并创建 API 对象。

之前我们运行 Nginx 的命令你还记得吗？使用的是 kubectl run，和 Docker 一样是“命令式”的：

```bash
kubectl run ngx --image=nginx:alpine
```

我们来把它改写成“声明式”的 YAML，说清楚我们想要的 Nginx 应用是个什么样子，也就是“目标状态”，让 Kubernetes 自己去决定如何拉取镜像运行：

```yml
apiVersion: v1
kind: Pod
metadata:
  name: ngx-pod
  labels:
    env: demo
    owner: chrono
spec:
  containers:
  - image: nginx:alpine
    name: ngx
    ports:
    - containerPort: 80
```

有了刚才 YAML 语言知识“打底”，相信你基本上能够把它看明白，知道它是一个 Pod，要使用 nginx:alpine 镜像创建一个容器，开放端口 80，而其他的部分，就是 Kubernetes 对 API 对象强制的格式要求了。

因为 API 对象采用标准的 HTTP 协议，为了方便理解，我们可以借鉴一下 HTTP 的报文格式，把 API 对象的描述分成“header”和“body”两部分。

“header”包含的是 API 对象的基本信息，有三个字段：**apiVersion**、**kind**、**metadata**。

apiVersion 表示操作这种资源的 API 版本号，由于 Kubernetes 的迭代速度很快，不同的版本创建的对象会有差异，为了区分这些版本就需要使用 apiVersion 这个字段，比如 v1、v1alpha1、v1beta1 等等。

kind 表示资源对象的类型，这个应该很好理解，比如 Pod、Node、Job、Service 等等。

metadata 这个字段顾名思义，表示的是资源的一些“元信息”，也就是用来标记对象，方便 Kubernetes 管理的一些信息。

```yml
apiVersion: v1
kind: Pod
metadata:
  name: ngx-pod
  labels:
    env: demo
    owner: chrono
```

比如在这个 YAML 示例里就有两个“元信息”，一个是 name，给 Pod 起了个名字叫 ngx-pod，另一个是 labels，给 Pod“贴”上了一些便于查找的标签，分别是 env 和 owner。

**apiVersion**、**kind**、**metadata** 都被 kubectl 用于生成 HTTP 请求发给 apiserver，你可以用 --v=9 参数在请求的 URL 里看到它们，比如：

```
https://192.168.49.2:8443/api/v1/namespaces/default/pods/ngx-pod
```

和 HTTP 协议一样，“header”里的 apiVersion、kind、metadata 这三个字段是任何对象都必须有的，而“body”部分则会与对象特定相关，每种对象会有不同的规格定义，在 YAML 里就表现为 spec 字段（即 specification），表示我们对对象的“期望状态”（desired status）。

还是来看这个 Pod，它的 spec 里就是一个 containers 数组，里面的每个元素又是一个对象，指定了名字、镜像、端口等信息：

```yml
spec:
  containers:
  - image: nginx:alpine
    name: ngx
    ports:
    - containerPort: 80
```

现在把这些字段综合起来，我们就能够看出，这份 YAML 文档完整地描述了一个类型是 Pod 的 API 对象，要求使用 v1 版本的 API 接口去管理，其他更具体的名称、标签、状态等细节都记录在了 metadata 和 spec 字段等里。

使用 kubectl apply、kubectl delete，再加上参数 -f，你就可以使用这个 YAML 文件，创建或者删除对象了：

```bash
kubectl apply -f ngx-pod.yml

kubectl delete -f ngx-pod.yml
```

Kubernetes 收到这份“声明式”的数据，再根据 HTTP 请求里的 POST/DELETE 等方法，就会自动操作这个资源对象，至于对象在哪个节点上、怎么创建、怎么删除完全不用我们操心。

## 如何编写 YAML

讲到这里，相信你对如何使用 YAML 与 Kubernetes 沟通应该大概了解了，不过疑问也会随之而来：这么多 API 对象，我们怎么知道该用什么 apiVersion、什么 kind？metadata、spec 里又该写哪些字段呢？还有，YAML 看起来简单，写起来却比较麻烦，缩进对齐很容易搞错，有没有什么简单的方法呢？

这些问题最权威的答案无疑是 Kubernetes 的官方参考文档（https://kubernetes.io/docs/reference/kubernetes-api/），API 对象的所有字段都可以在里面找到。不过官方文档内容太多太细，查阅起来有些费劲，所以下面我就介绍几个简单实用的小技巧。

第一个技巧其实前面已经说过了，就是 kubectl api-resources 命令，它会显示出资源对象相应的 API 版本和类型，比如 Pod 的版本是“v1”，Ingress 的版本是“networking.k8s.io/v1”，照着它写绝对不会错。

第二个技巧，是命令 kubectl explain，它相当于是 Kubernetes 自带的 API 文档，会给出对象字段的详细说明，这样我们就不必去网上查找了。比如想要看 Pod 里的字段该怎么写，就可以这样：

```bash
kubectl explain pod

kubectl explain pod.metadata

kubectl explain pod.spec

kubectl explain pod.spec.containers
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/53cf783195be896e7632c1fc6bd24185.png)

使用前两个技巧编写 YAML 就基本上没有难度了。

不过我们还可以让 kubectl 为我们“代劳”，生成一份“文档样板”，免去我们打字和对齐格式的工作。**这第三个技巧就是 kubectl 的两个特殊参数** **--dry-run=client** **和** **-o yaml****，前者是空运行，后者是生成 YAML 格式，结合起来使用就会让 kubectl 不会有实际的创建动作，而只生成 YAML 文件。**

例如，想要生成一个 Pod 的 YAML 样板示例，可以在 kubectl run 后面加上这两个参数：

```bash
kubectl run ngx --image=nginx:alpine --dry-run=client -o yaml
```

就会生成一个绝对正确的 YAML 文件：

```yml
apiVersion: v1
kind: Pod
metadata:
  creationTimestamp: null
  labels:
    run: ngx
  name: ngx
spec:
  containers:
  - image: nginx:alpine
    name: ngx
    resources: {}
  dnsPolicy: ClusterFirst
  restartPolicy: Always
status: {}
```

接下来你要做的，就是查阅对象的说明文档，添加或者删除字段来定制这个 YAML 了。

这个小技巧还可以再进化一下，把这段参数定义成 Shell 变量（名字任意，比如$do/$go，这里用的是$out），用起来会更省事，比如：

```bash
export out="--dry-run=client -o yaml"

kubectl run ngx --image=nginx:alpine $out
```

今后除了一些特殊情况，我们都不会再使用 kubectl run 这样的命令去直接创建 Pod，而是会编写 YAML，用“声明式”来描述对象，再用 kubectl apply 去发布 YAML 来创建对象。

## 小结

好了，今天就到这里，我们一起学习了“声明式”和“命令式”的区别、YAML 语言的语法、如何用 YAML 来描述 API 对象，还有一些编写 YAML 文件的技巧。

Kubernetes 采用 YAML 作为工作语言是它有别与其他系统的一大特色，声明式的语言能够更准确更清晰地描述系统状态，避免引入繁琐的操作步骤扰乱系统，与 Kubernetes 高度自动化的内部结构相得益彰，而且纯文本形式的 YAML 也很容易版本化，适合 CI/CD。

再小结一下今天的内容要点：

YAML 是 JSON 的超集，支持数组和对象，能够描述复杂的状态，可读性也很好。

1. Kubernetes 把集群里的一切资源都定义为 API 对象，通过 RESTful 接口来管理。描述 API 对象需要使用 YAML 语言，必须的字段是 **apiVersion、kind、metadata**。
2. 命令 kubectl api-resources 可以查看对象的 apiVersion 和 kind，命令 kubectl explain 可以查看对象字段的说明文档。
3. 命令 kubectl apply、kubectl delete 发送 HTTP 请求，管理 API 对象。
4. 使用参数 --dry-run=client -o yaml 可以生成对象的 YAML 模板，简化编写工作。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/13dc437dda840dda4850fb72237b8e36.jpg" alt="img" style="zoom:33%;" />