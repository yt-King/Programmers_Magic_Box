# 13｜Job/CronJob：为什么不直接用Pod来处理业务？

在上次的课里我们学习了 Kubernetes 的核心对象 Pod，用来编排一个或多个容器，让这些容器共享网络、存储等资源，总是共同调度，从而紧密协同工作。

因为 Pod 比容器更能够表示实际的应用，所以 Kubernetes 不会在容器层面来编排业务，而是把 Pod 作为在集群里调度运维的最小单位。

前面我们也看到了一张 Kubernetes 的资源对象关系图，以 Pod 为中心，延伸出了很多表示各种业务的其他资源对象。那么你会不会有这样的疑问：Pod 的功能已经足够完善了，为什么还要定义这些额外的对象呢？为什么不直接在 Pod 里添加功能，来处理业务需求呢？

这个问题体现了 Google 对大规模计算集群管理的深度思考，今天我就说说 Kubernetes 基于 Pod 的设计理念，先从最简单的两种对象——Job 和 CronJob 讲起。

## 为什么不直接使用 Pod

现在你应该知道，Kubernetes 使用的是 RESTful API，把集群中的各种业务都抽象为 HTTP 资源对象，那么在这个层次之上，我们就可以使用面向对象的方式来考虑问题。

如果你有一些编程方面的经验，就会知道面向对象编程（OOP），它把一切都视为高内聚的对象，强调对象之间互相通信来完成任务。

虽然面向对象的设计思想多用于软件开发，但它放到 Kubernetes 里却意外地合适。因为 Kubernetes 使用 YAML 来描述资源，把业务简化成了一个个的对象，内部有属性，外部有联系，也需要互相协作，只不过我们不需要编程，完全由 Kubernetes 自动处理（其实 Kubernetes 的 Go 语言内部实现就大量应用了面向对象）。

面向对象的设计有许多基本原则，其中有两条我认为比较恰当地描述了 Kubernetes 对象设计思路，一个是“**单一职责**”，另一个是“**组合优于继承**”。

“单一职责”的意思是对象应该只专注于做好一件事情，不要贪大求全，保持足够小的粒度才更方便复用和管理。

“组合优于继承”的意思是应该尽量让对象在运行时产生联系，保持松耦合，而不要用硬编码的方式固定对象的关系。

应用这两条原则，我们再来看 Kubernetes 的资源对象就会很清晰了。因为 Pod 已经是一个相对完善的对象，专门负责管理容器，那么我们就不应该再“画蛇添足”地盲目为它扩充功能，而是要保持它的独立性，容器之外的功能就需要定义其他的对象，把 Pod 作为它的一个成员“组合”进去。

这样每种 Kubernetes 对象就可以只关注自己的业务领域，只做自己最擅长的事情，其他的工作交给其他对象来处理，既不“缺位”也不“越位”，既有分工又有协作，从而以最小成本实现最大收益。

## 为什么要有 Job/CronJob

现在我们来看看 Kubernetes 里的两种新对象：Job 和 CronJob，它们就组合了 Pod，实现了对离线业务的处理。

上次课讲 Pod 的时候我们运行了两个 Pod：Nginx 和 busybox，它们分别代表了 Kubernetes 里的两大类业务。一类是像 Nginx 这样长时间运行的“**在线业务**”，另一类是像 busybox 这样短时间运行的“**离线业务**”。

“在线业务”类型的应用有很多，比如 Nginx、Node.js、MySQL、Redis 等等，一旦运行起来基本上不会停，也就是永远在线。

而“离线业务”类型的应用也并不少见，它们一般不直接服务于外部用户，只对内部用户有意义，比如日志分析、数据建模、视频转码等等，虽然计算量很大，但只会运行一段时间。“离线业务”的特点是**必定会退出**，不会无期限地运行下去，所以它的调度策略也就与“在线业务”存在很大的不同，需要考虑运行超时、状态检查、失败重试、获取计算结果等管理事项。

而这些业务特性与容器管理没有必然的联系，如果由 Pod 来实现就会承担不必要的义务，违反了“单一职责”，所以我们应该把这部分功能分离到另外一个对象上实现，让这个对象去控制 Pod 的运行，完成附加的工作。

“离线业务”也可以分为两种。一种是“**临时任务**”，跑完就完事了，下次有需求了说一声再重新安排；另一种是“**定时任务**”，可以按时按点周期运行，不需要过多干预。

对应到 Kubernetes 里，“临时任务”就是 API 对象 **Job**，“定时任务”就是 API 对象 **CronJob**，使用这两个对象你就能够在 Kubernetes 里调度管理任意的离线业务了。

由于 Job 和 CronJob 都属于离线业务，所以它们也比较相似。我们先学习通常只会运行一次的 Job 对象以及如何操作。

### 如何使用 YAML 描述 Job

Job 的 YAML“文件头”部分还是那几个必备字段，我就不再重复解释了，简单说一下：

- apiVersion 不是 v1，而是 batch/v1。
- kind 是 Job，这个和对象的名字是一致的。
- metadata 里仍然要有 name 标记名字，也可以用 labels 添加任意的标签。

如果记不住这些也不要紧，你还可以使用命令 kubectl explain job 来看它的字段说明。不过想要生成 YAML 样板文件的话不能使用 kubectl run，因为 kubectl run 只能创建 Pod，要创建 Pod 以外的其他 API 对象，需要使用命令 kubectl create，再加上对象的类型名。

比如用 busybox 创建一个“echo-job”，命令就是这样的：

```bash
export out="--dry-run=client -o yaml"              # 定义Shell变量

kubectl create job echo-job --image=busybox $out
```

会生成一个基本的 YAML 文件，保存之后做点修改，就有了一个 Job 对象：

```yml
apiVersion: batch/v1
kind: Job
metadata:
  name: echo-job
spec:
  template:
    spec:
      restartPolicy: OnFailure
      containers:
      - image: busybox
        name: echo-job
        imagePullPolicy: IfNotPresent
        command: ["/bin/echo"]
        args: ["hello", "world"]
```

你会注意到 Job 的描述与 Pod 很像，但又有些不一样，主要的区别就在“spec”字段里，多了一个 template 字段，然后又是一个“spec”，显得有点怪。

如果你理解了刚才说的面向对象设计思想，就会明白这种做法的道理。它其实就是在 Job 对象里应用了组合模式，template 字段定义了一个“**应用模板**”，里面嵌入了一个 Pod，这样 Job 就可以从这个模板来创建出 Pod。

而这个 Pod 因为受 Job 的管理控制，不直接和 apiserver 打交道，也就没必要重复 apiVersion 等“头字段”，只需要定义好关键的 spec，描述清楚容器相关的信息就可以了，可以说是一个“无头”的 Pod 对象。

为了辅助你理解，我把 Job 对象重新组织了一下，用不同的颜色来区分字段，这样你就能够很容易看出来，其实这个“echo-job”里并没有太多额外的功能，只是把 Pod 做了个简单的包装：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/9b780905a824d2103d4ayyc79267ae28.jpg" alt="img" style="zoom: 33%;" />

总的来说，这里的 Pod 工作非常简单，在 containers 里写好名字和镜像，command 执行 /bin/echo，输出“hello world”。

不过，因为 Job 业务的特殊性，所以我们还要在 spec 里多加一个字段 restartPolicy，确定 Pod 运行失败时的策略，OnFailure 是失败原地重启容器，而 Never 则是不重启容器，让 Job 去重新调度生成一个新的 Pod。

### 如何在 Kubernetes 里操作 Job

现在让我们来创建 Job 对象，运行这个简单的离线作业，用的命令还是 kubectl apply：

```bash
kubectl apply -f job.yml
```

创建之后 Kubernetes 就会从 YAML 的模板定义中提取 Pod，在 Job 的控制下运行 Pod，你可以用 kubectl get job、kubectl get pod 来分别查看 Job 和 Pod 的状态：

```![](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/33ac80cb9f5dd91d1affc133e56efc55.png)
kubectl get job

kubectl get pod
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/33ac80cb9f5dd91d1affc133e56efc55.png" alt="img" style="zoom:50%;" />

可以看到，因为 Pod 被 Job 管理，它就不会反复重启报错了，而是会显示为 Completed 表示任务完成，而 Job 里也会列出运行成功的作业数量，这里只有一个作业，所以就是 1/1。

你还可以看到，Pod 被自动关联了一个名字，用的是 Job 的名字（echo-job）再加上一个随机字符串（pb5gh），这当然也是 Job 管理的“功劳”，免去了我们手工定义的麻烦，这样我们就可以使用命令 kubectl logs 来获取 Pod 的运行结果：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/81224cedf0acf209b746a1162d09b3b5.png" alt="img" style="zoom:50%;" />

到这里，你可能会觉得，经过了 Job、Pod 对容器的两次封装，虽然从概念上很清晰，但好像并没有带来什么实际的好处，和直接跑容器也差不了多少。

其实 Kubernetes 的这套 YAML 描述对象的框架提供了非常多的灵活性，可以在 Job 级别、Pod 级别添加任意的字段来定制业务，这种优势是简单的容器技术无法相比的。

这里我列出几个控制离线作业的重要字段，其他更详细的信息可以参考 Job 文档：

- **activeDeadlineSeconds**，设置 Pod 运行的超时时间。
- **backoffLimit**，设置 Pod 的失败重试次数。
- **completions**，Job 完成需要运行多少个 Pod，默认是 1 个。
- **parallelism**，它与 completions 相关，表示允许并发运行的 Pod 数量，避免过多占用资源。

要注意这 4 个字段并不在 template 字段下，而是在 spec 字段下，所以它们是属于 Job 级别的，用来控制模板里的 Pod 对象。

下面我再创建一个 Job 对象，名字叫“sleep-job”，它随机睡眠一段时间再退出，模拟运行时间较长的作业（比如 MapReduce）。Job 的参数设置成 15 秒超时，最多重试 2 次，总共需要运行完 4 个 Pod，但同一时刻最多并发 2 个 Pod：

```yml
apiVersion: batch/v1
kind: Job
metadata:
  name: sleep-job
spec:
  activeDeadlineSeconds: 15
  backoffLimit: 2
  completions: 4
  parallelism: 2
  template:
    spec:
      restartPolicy: OnFailure
      containers:
      - image: busybox
        name: echo-job
        imagePullPolicy: IfNotPresent
        command:
          - sh
          - -c
          - sleep $(($RANDOM % 10 + 1)) && echo done
```

使用 kubectl apply 创建 Job 之后，我们可以用 kubectl get pod -w 来实时观察 Pod 的状态，看到 Pod 不断被排队、创建、运行的过程：

```bash
kubectl apply -f sleep-job.yml

kubectl get pod -w
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/7d413a0c38065de2063a99e7df2b7eb7.png)

等到 4 个 Pod 都运行完毕，我们再用 kubectl get 来看看 Job 和 Pod 的状态：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/58b99356c811bd377acfa4cb921d2446.png)

就会看到 Job 的完成数量如同我们预期的是 4，而 4 个 Pod 也都是完成状态。

显然，“声明式”的 Job 对象让离线业务的描述变得非常直观，简单的几个字段就可以很好地控制作业的并行度和完成数量，不需要我们去人工监控干预，Kubernetes 把这些都自动化实现了。

## 如何使用 YAML 描述 CronJob

学习了“临时任务”的 Job 对象之后，再学习“定时任务”的 CronJob 对象也就比较容易了，我就直接使用命令 kubectl create 来创建 CronJob 的样板。

要注意两点。第一，因为 CronJob 的名字有点长，所以 Kubernetes 提供了简写 cj，这个简写也可以使用命令 kubectl api-resources 看到；第二，CronJob 需要定时运行，所以我们在命令行里还需要指定参数 --schedule。

```bash
export out="--dry-run=client -o yaml"              # 定义Shell变量

kubectl create cj echo-cj --image=busybox --schedule="" $out
```

然后我们编辑这个 YAML 样板，生成 CronJob 对象：

```yml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: echo-cj
spec:
  schedule: '*/1 * * * *'
  jobTemplate:
    spec:
      template:
        spec:
          restartPolicy: OnFailure
          containers:
          - image: busybox
            name: echo-cj
            imagePullPolicy: IfNotPresent
            command: ["/bin/echo"]
            args: ["hello", "world"]
```

我们还是重点关注它的 spec 字段，你会发现它居然连续有三个 spec 嵌套层次：

- 第一个 spec 是 CronJob 自己的对象规格声明
- 第二个 spec 从属于“jobTemplate”，它定义了一个 Job 对象。
- 第三个 spec 从属于“template”，它定义了 Job 里运行的 Pod。

所以，CronJob 其实是又组合了 Job 而生成的新对象，我还是画了一张图，方便你理解它的“套娃”结构：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/yy352c661ae37dd116dd12c61932b43c.jpg" alt="img" style="zoom: 33%;" />

除了定义 Job 对象的“**jobTemplate**”字段之外，CronJob 还有一个新字段就是“**schedule**”，用来定义任务周期运行的规则。它使用的是标准的 Cron 语法，指定分钟、小时、天、月、周，和 Linux 上的 crontab 是一样的。像在这里我就指定每分钟运行一次，格式具体的含义你可以课后参考 Kubernetes 官网文档。

除了名字不同，CronJob 和 Job 的用法几乎是一样的，使用 kubectl apply 创建 CronJob，使用 kubectl get cj、kubectl get pod 来查看状态：

```bash
kubectl apply -f cronjob.yml

kubectl get cj

kubectl get pod
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/b00fdd8541372fb7a4de00de5ac6342c.png" alt="img" style="zoom:50%;" />

## 小结

好了，今天我们以面向对象思想分析了一下 Kubernetes 里的资源对象设计，它强调“职责单一”和“对象组合”，简单来说就是“对象套对象”。

通过这种嵌套方式，Kubernetes 里的这些 API 对象就形成了一个“控制链”：

CronJob 使用定时规则控制 Job，Job 使用并发数量控制 Pod，Pod 再定义参数控制容器，容器再隔离控制进程，进程最终实现业务功能，层层递进的形式有点像设计模式里的 Decorator（装饰模式），链条里的每个环节都各司其职，在 Kubernetes 的统一指挥下完成任务。

小结一下今天的内容：

1. Pod 是 Kubernetes 的最小调度单元，但为了保持它的独立性，不应该向它添加多余的功能。
2. Kubernetes 为离线业务提供了 Job 和 CronJob 两种 API 对象，分别处理“临时任务”和“定时任务”。
3. Job 的关键字段是 spec.template，里面定义了用来运行业务的 Pod 模板，其他的重要字段有 completions、parallelism 等
4. CronJob 的关键字段是 spec.jobTemplate 和 spec.schedule，分别定义了 Job 模板和定时运行的规则。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/597caae147ec2a1852151878fc47ed7f.jpg" alt="img" style="zoom:33%;" />