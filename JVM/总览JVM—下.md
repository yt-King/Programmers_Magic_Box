# 1、概述篇

## 1.1、大厂面试题

![image-20210221103714852](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224150350-1731711792.png)

![image-20210221103814442](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224149994-1753226700.png)

---

## 1.2、背景说明

![image-20210221103924021](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224149644-958941858.png)

---

## 1.3、调优概述

![image-20210221110924434](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224149360-2047048203.png)

---

## 1.4、性能优化的步骤

（括号内的描述会感觉很low，冒号后面的就感觉很高级。）

![image-20210221111257730](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224149034-1931798243.png)

### 性能监控

#### 理解

![image-20210221111424105](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224148672-642407735.png)

一种以非强行或者非入侵方式**收集或查看**应用运营性能数据的活动。

监控通常是指一种在生产、质量评估或者开发环境下实施的带有**预防或主动性**的活动。

当应用相关干系人提出性能问题却没有提供足够多的线索时，首先我们需要进行性能监控，随后是性能分析。

#### 监控哪些方面：

- GC 频繁
- cpu load 过高
- OOM
- 内存泄露
- 死锁
- 程序响应时间较长

### 性能分析

#### 理解

![image-20210221111433465](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224148394-1058032558.png)

#### 从哪些方面分析

- 打印 GC 日志，通过 GCviewer 或者 [http://gceasy.io](http://gceasy.io/) 来分析异常信息
- 灵活运用命令行工具、jstack、jmap、jinfo 等
- dump 出堆文件，使用内存分析工具分析文件
- 使用阿里 Arthas、jconsole、JVisualVM 来实时查看 JVM 状态
- jstack 查看堆栈信息

### 性能调优

#### 理解

![image-20210221111442443](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224148125-980852703.png)

一种为改善应用响应性或吞吐量而更改参数、源代码、属性配置的活动，性能调优是在性能监控、性能分析之后的活动。

#### 如何调优

- 适当增加内存，根据业务背景选择垃圾回收器
- 优化代码，控制内存使用
- 增加机器，分散节点压力
- 合理设置线程池线程数量
- 使用中间件提高程序效率，比如缓存、消息队列等
- 其他……

---

## 1.5、性能评价/测试指标

### 概述

![image-20220426111210542](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224147857-932585187.png)

### 停顿时间（或响应时间）

提交请求和返回该请求的响应之间使用的时间，一般比较关注平均响应时间。常用操作的响应时间列表：

| 操作                                | 响应时间 |
| :---------------------------------- | :------- |
| 打开一个站点                        | 几秒     |
| 数据库查询一条记录（有索引）        | 十几毫秒 |
| 机械磁盘一次寻址定位                | 4 毫秒   |
| 从机械磁盘顺序读取 1M 数据          | 2 毫秒   |
| 从 SSD 磁盘顺序读取 1M 数据         | 0.3 毫秒 |
| 从远程分布式换成 Redis 读取一个数据 | 0.5 毫秒 |
| 从内存读取 1M 数据                  | 十几微妙 |
| Java 程序本地方法调用               | 几微妙   |
| 网络传输 2Kb 数据                   | 1 微妙   |

在垃圾回收环节中：

- 暂停时间：执行垃圾收集时，程序的工作线程被暂停的时间。
- -XX:MaxGCPauseMillis

### 吞吐量

![image-20210221111708356](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224147588-1848362938.png)

- 对单位时间内完成的工作量（请求）的量度
- 在 GC 中：运行用户代码的事件占总运行时间的比例（总运行时间：程序的运行时间+内存回收的时间）
- 吞吐量为 1-1/(1+n)，其中-XX::GCTimeRatio=n

### 并发数

![image-20210221123937618](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224147326-713920919.png)

- 同一时刻，对服务器有实际交互的请求数

### 内存占用

- Java 堆区所占的内存大小

### 相互间的关系

以高速公路通行状况为例

![image-20210221123959568](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224147051-970274545.png)

- 吞吐量：每天通过高速公路收费站的车辆的数据
- 并发数：高速公路上正在行驶的车辆的数目
- 响应时间：车速

# 2、JVM监控及诊断工具 - 命令行篇

---

## 2.1、概述

![image-20210221160812224](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224146708-72534046.png)

性能诊断是软件工程师在日常工作中需要经常面对和解决的问题，在用户体验至上的今天，解决好应用的性能问题能带来非常大的收益。

Java 作为最流行的编程语言之一，其应用性能诊断一直受到业界广泛关注。可能造成 Java 应用出现性能问题的因素非常多，例如线程控制、磁盘读写、数据库访问、网络 I/O、垃圾收集等。想要定位这些问题，一款优秀的性能诊断工具必不可少。

体会 1：使用数据说明问题，使用知识分析问题，使用工具处理问题。

体会 2：无监控、不调优！

### 2.1.1. 简单命令行工具

在我们刚接触 java 学习的时候，大家肯定最先了解的两个命令就是 javac，java，那么除此之外，还有没有其他的命令可以供我们使用呢？

我们进入到安装 jdk 的 bin 目录，发现还有一系列辅助工具。这些辅助工具用来获取目标 JVM 不同方面、不同层次的信息，帮助开发人员很好地解决 Java 应用程序的一些疑难杂症。

![image-20220426112512715](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224146396-505958701.png)

Window系统下：

![image-20210221161138186](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224146073-1786377423.png)

工具源码

![image-20210221161022590](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224145730-104421951.png)

![image-20210221161055745](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224145367-689844395.png)

![image-20210221161112447](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224145114-315883263.png)

访问网站

![image-20210221161246900](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224144841-2022290801.png)

![image-20210221161310578](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224144528-630703956.png)

---

## 2.2、jps：查看正在运行的Java进程

### 2.2.1. 基本情况

**jps(Java Process Status)**

显示指定系统内所有的 HotSpot 虚拟机进程（查看虚拟机进程信息），可用于查询正在运行的虚拟机进程。

说明：对于本地虚拟机进程来说，进程的本地虚拟机 ID 与操作系统的进程 ID 是一致的，是唯一的。

### 2.2.2. 测试

![image-20210221161406491](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224144283-233512088.png)

运行程序，别让jvm结束就行

![image-20210221161538007](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224143741-335569151.png)

![image-20210221161911354](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224143459-172601295.png)

每次jps都是起了一个新的进程

![image-20210221162052876](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224143170-1523625789.png)

### 2.2.3. 基本语法

![image-20210221162354878](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224142958-484846949.png)

基本使用语法为：**`jps [options] [hostid]`**

我们还可以通过追加参数，来打印额外的信息。

**options 参数**

- -q：仅仅显示 LVMID（local virtual machine id），即本地虚拟机唯一 id。不显示主类的名称等
- -l：输出应用程序主类的全类名 或 如果进程执行的是 jar 包，则输出 jar 完整路径
- -m：输出虚拟机进程启动时传递给主类 main()的参数
- -v：列出虚拟机进程启动时的 JVM 参数。比如：-Xms20m -Xmx50m 是启动程序指定的 jvm 参数。

说明：以上参数可以综合使用。

补充：如果某 Java 进程关闭了默认开启的 UsePerfData 参数（即使用参数-XX：-UsePerfData），那么 jps 命令（以及下面介绍的 jstat）将无法探知该 Java 进程。

**hostid 参数**

RMI 注册表中注册的主机名。如果想要远程监控主机上的 java 程序，需要安装 jstatd。

对于具有更严格的安全实践的网络场所而言，可能使用一个自定义的策略文件来显示对特定的可信主机或网络的访问，尽管这种技术容易受到 IP 地址欺诈攻击。

如果安全问题无法使用一个定制的策略文件来处理，那么最安全的操作是不运行 jstatd 服务器，而是在本地使用 jstat 和 jps 工具。

具体参数演示：

-q只显示进程

![image-20210221162507474](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224142736-1728243214.png)

-l包含全类名

![image-20210221162539067](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224142503-2039950711.png)

-m

![image-20210221162630672](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224142227-729749695.png)

![image-20210221162641372](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224141906-1843468139.png)

换行

![image-20210221162709321](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224141417-1306179761.png)

程序传递参数，然后运行

![image-20210221162745967](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224140990-2019470905.png)

-m参数

![image-20210221162813706](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224140706-2118309711.png)

看到了传递的参数，相关jar包也可能看到

![image-20210221162831036](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224140448-1040919097.png)

jvm参数，然后启动

![image-20210221162922476](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224140192-1265753207.png)

-v

![image-20210221162945888](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224139842-334977865.png)

![image-20210221162956227](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224139503-760033943.png)

![image-20210221163006913](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224139229-789550424.png)

可以综合使用（或者jps -lm）

![image-20210221163036269](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224138862-1031993200.png)

mlvV参数可以写在一起，例如（jps -mlvV）；参数q是比较独立的，如果要想和其他参数一起发挥作用，应该分开写（例如 jps -l -q）

![image-20210221163143607](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224138595-1488426150.png)

![image-20220426122520161](https://img2022.cnblogs.com/blog/2402456/202205/2402456-20220501224138350-1155680968.png)

![image-20210221163339202](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224137986-2140906002.png)

jvm参数，关闭，运行

![image-20210221163243136](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224137679-1205810328.png)

jps，就没有那个程序了

![image-20210221163302727](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224137428-1752323318.png)

---

## 2.3、jstat：查看JVM统计信息

（这个很重要）

![image-20220426151311111](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224137060-1433114351.png)

### 基本情况

**`jstat（JVM Statistics Monitoring Tool）：用于监视虚拟机各种运行状态信息的命令行工具`**。它可以显示本地或者远程虚拟机进程中的类装载、内存、垃圾收集、JIT 编译等运行数据。

在没有 GUI 图形界面，只提供了纯文本控制台环境的服务器上，它将是运行期定位虚拟机性能问题的首选工具。**`常用于检测垃圾回收问题以及内存泄漏问题`**。

官方文档：https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jstat.html

![image-20210221163641917](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224136717-209127910.png)

jstat和jstat -help是一样的

![image-20220426151746279](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224136353-661188443.png)

### 基本语法

#### 概述

![image-20220426151943848](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224136074-171490894.png)

其中vmid是进程id号，也就是jps之后看到的前面的号码，如下：

![image-20220426152018391](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224135824-327818772.png)

**预览图**

![image-20210221163906605](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224135423-1757970873.png)

#### 相关参数

##### option 参数

![image-20220426155320944](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224134996-1906574699.png)

选项 option 可以由以下值构成。

**类装载相关的**：

- -class：显示 ClassLoader 的相关信息：类的装载、卸载数量、总空间、类装载所消耗的时间等

**垃圾回收相关的**：

- -gc：显示与 GC 相关的堆信息。包括 Eden 区、两个 Survivor 区、老年代、永久代等的容量、已用空间、GC 时间合计等信息。
- -gccapacity：显示内容与-gc 基本相同，但输出主要关注 Java 堆各个区域使用到的最大、最小空间。
- -gcutil：显示内容与-gc 基本相同，但输出主要关注已使用空间占总空间的百分比。
- -gccause：与-gcutil 功能一样，但是会额外输出导致最后一次或当前正在发生的 GC 产生的原因。
- -gcnew：显示新生代 GC 状况
- -gcnewcapacity：显示内容与-gcnew 基本相同，输出主要关注使用到的最大、最小空间
- -geold：显示老年代 GC 状况
- -gcoldcapacity：显示内容与-gcold 基本相同，输出主要关注使用到的最大、最小空间
- -gcpermcapacity：显示永久代使用到的最大、最小空间。

**JIT 相关的**：

- -compiler：显示 JIT 编译器编译过的方法、耗时等信息
- -printcompilation：输出已经被 JIT 编译的方法

**小试牛刀**

启动

![image-20210221164323351](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224134535-1508621081.png)

![image-20210221164348133](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224134251-390006195.png)

**类装载相关的**

```
class
```

![image-20210221164601686](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224133998-1626163925.png)

这就是类装载的功能，通过-class来体现的

![image-20210221164645585](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224133707-1284659270.png)

![image-20210221164736161](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224133411-643391880.png)

**JIT相关的**

![image-20220426155402375](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224133146-480331326.png)

```
compiler
```

![image-20210221165540178](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224132921-2045890255.png)

翻译结果：编译的有91，失败的多少，耗时，失败的类型、方法等

```
printcompilation
```

![image-20210221165644639](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224132656-1608849971.png)

翻译结果：append方法被编译过了

**垃圾回收相关的**

![image-20220426160044800](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224132368-1466964906.png)

![image-20220426160131614](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224132078-1434700345.png)

```
gc
```

![image-20210221165813363](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224131837-1250957091.png)

参数具体的含义

![image-20210221165856010](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224131523-261377423.png)

GCT是总的gc时间，是YGCT+FGCT的时间。

##### option参数除外的其他参数

![image-20220426160646144](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224131213-97331501.png)

`interval`，每隔一秒打印一次

![image-20210221164751308](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224130988-1920284105.png)

`count`，打印10次就结束

![image-20210221164855075](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224130733-1588261290.png)

`t`，进程启动后，经历的时间，单位是秒

![image-20210221165058577](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224130470-908619710.png)

![image-20210221164941238](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224130167-293473950.png)

![image-20210221165127429](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224129881-661087188.png)

![image-20210221165140000](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224129627-1649524008.png)

`h`，每隔多少行数据就打印一次表头（-h3，是每隔3条数据就打印一次表头）

![image-20210221165221471](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224129280-974501801.png)

##### 例子

![image-20210221170039086](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224128914-1592793760.png)

jvm参数:-Xms60m -Xmx60m -XX:SurvivorRatio=8

![image-20210221170120459](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224128634-1076863512.png)

![image-20210221170139959](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224128405-1816117406.png)

![image-20210221170200280](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224128100-1799733104.png)

60M空间，newRatio=2，即新生代=20M（SurvivorRatio=8，所以S0、S1、Eden分别是2M、2M、16M），老年代=40M。上图YGC一次。

gcutil是显示占比的情况，FGC发生了，FGC后还是内存不够就会OOM

![image-20210221170738363](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224127727-272289373.png)

OOM了监控也就终止了

![image-20210221170748295](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224127379-551869906.png)

![image-20210221171005790](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224127017-1748451055.png)

发生GC的原因

![image-20210221171026639](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224126739-1624079910.png)

生产环境是不能用GUI界面的工具的，只能用jstat这些命令行工具监控。

![image-20210221171159361](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224126503-1731227669.png)

![image-20210221171206405](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224126224-1954967936.png)

截取两行，然后获取到启动的时间时间段假设为10，GCT总时间在这期间花费了假设0.04，然后0.04 / 10这个比例超过20%就是压力较大。

如下图：

![image-20220426172915008](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224125710-259121410.png)

![image-20210221171942291](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224125383-856486387.png)

![image-20210221173015583](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224125040-584323416.png)

---

## 2.4、jinfo：实时查看和修改JVM配置参数

概览图：

![image-20220427004538264](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224124733-411953470.png)

### 基本情况

![image-20210221222610706](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224124396-1185844172.png)

![image-20210221223056257](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224124095-1937543939.png)

![image-20210221222919548](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224123796-1798251495.png)

![image-20210221222911736](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224123513-1468446615.png)

jps可以看到设置过的参数，没有设置过就要用jinfo

![image-20210221222956944](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224123222-2072220374.png)

### 基本语法

![image-20210221223128019](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224122917-1788813550.png)

#### 查看

`jinfo -sysprops 进程id`：可以查看由System.getProperties()取得的参数

![image-20220427004937533](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224122613-141095883.png)

`jinfo -flags 进程id`：查看曾经赋过值的一些参数

举例：

![image-20220427005418943](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224122342-871111943.png)

![image-20210221223545429](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224122019-561846550.png)

`jinfo -flag 参数名称 进程id`：查看某个java进程的具体参数信息

#### 修改

![image-20220427005538799](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224121706-178427779.png)

Linux上运行 java -XX:+PrintFlagsFinal -version | grep manageable

Window系统运行 java -XX:+PrintFlagsFinal -version | findstr manageable

![image-20220427005552440](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224121349-636158742.png)

##### 针对boolean类型

```
jinfo -flag [+|-]参数名称 进程id
```

PID可以通过jps命令查看，如果使用+号，那就可以让该参数起作用，否则使用-号就让该参数不起作用，具体例子如下：

![image-20220427005733795](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224121073-1874211552.png)

##### 针对非boolean类型

```
jinfo -flag 参数名称=参数值 进程id
```

PID可以通过jps命令查看，如果使用+号，那就可以让该参数起作用，否则使用-号就让该参数不起作用，具体例子如下：

![image-20220427005832761](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224120845-824022840.png)

注：程序退出之后，这个修改值就会失效，即不支持持久化

![image-20210221225328680](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224120605-2052646600.png)

### 拓展

![image-20220427005931652](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224120328-346122379.png)

java -XX:PrintFlagsFinal

![image-20220427010145797](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224120088-162481305.png)

值前面添加冒号:的是修改之后的值，没有添加的都是没有发生改变的初始值

---

## 2.5、jmap：导出内存映像文件&内存使用情况

### 概览图

![image-20210222223824451](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224119723-1244390719.png)

### 基本情况

![image-20210222223901826](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224119385-1450268164.png)

### 基本语法

![image-20210222224631152](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224119045-1622496364.png)

```
-dump
```

![image-20210222230317389](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224118745-1244913269.png)

`-heap`：输出整个堆空间的详细信息，包括GC的使用、堆配置信息，以及内存的使用信息等

`-histo`：输出堆中对象的同级信息，包括类、实例数量和合计容量；特别的：-histo:live只统计堆中的存活对象

#### 使用1：导出内存映像文件

##### 概述

![image-20220427012746630](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224118517-1269936332.png)

![image-20220427012827635](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224118169-551281754.png)

说明：

1、(自动方式)通常在写Heap Dump文件前会触发一次Full GC，所以heap dump文件里保存的都是Full GC后留下的对象信息。而手动不会在Full GC之后生成Dump

2、由于生成dump文件比较耗时，因此大家需要耐心等待，尤其是大内存镜像生成dump文件则需要耗费更长的时间来完成。

3、使用手动方式生成dump文件，一般指令执行之后就会生成，不用等到快出现OOM的时候

4、使用自动方式生成dump文件，当出现OOM之前先生成dump文件

5、如果使用手动方式，生成堆中存活对象的dump文件是比较小的，便于传输和分析

##### 手动方式

![image-20220427013507996](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224117890-505151266.png)

**说明：**
<filename.hprof>中的filename是文件名称，而.hprof是后缀名，<***>代表该值可以省略<>，当然后面的是进程id，需要通过jps查询出来
format=b表示生成的是标准的dump文件，用来进行格式限定

具体例子如下：
生成堆中所有对象的快照：

```shell
jmap -dump:format=b,file=d:\1.hprof 11696
```

生成堆中存活对象的快照：

```shell
jmap -dump:format=live,b,file=d:\1.hprof 11696
```

其中file=后面的是生成的dump文件地址，最后的11696是进程id，可以通过jps查看

例子：

![image-20210223074147888](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224117626-761304345.png)

连续生成3个（防止程序结束了）。dump是堆转储，format=b是标准的格式

![image-20210223074232643](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224117334-1264308916.png)

生成堆中存活对象的快照：

![image-20210223074244160](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224117081-380230230.png)

文件，是二进制的。1、2、3文件是越来越大，4（活着的对象）一般都是比1、2、3小的（生产环境dump出活着的对象就行，导致内存不足也是因为 活着的对象无法回收），但是我们这里变大是因为不断的产生新的对象。

![image-20210223074257429](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224116805-840721635.png)

##### 自动方式

自动方式，如果发生OOM,会自动dump,生成dump文件

![image-20210223074831157](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224116513-707700991.png)

复制比如的参数

![image-20210223075021746](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224116223-1928894382.png)

![image-20210223075102450](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224115937-1273546975.png)

出现OOM前就dump出来了

![image-20210223075119756](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224115684-165286017.png)

#### 使用2：显示堆内存相关信息

![image-20220427014358221](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224115440-1088257216.png)

```
jmap -heap 进程id
```

1、输出的是时间点上的堆信息，而jstat后面可以添加参数，可以指定时间动态观察数据改变情况，而图形化界面工具，例如jvisualvm等，它们可以用图表的方式动态展示出相关信息，更加直观明了

```
jmap -histo 进程id
```

输出堆中对象的同级信息，包括类、实例数量和合计容量，也是这一时刻的内存中的对象信息

例子如下：

```shell
jmap -heap 3540 > a.txt
jmap -histo 3540 > b.txt
```

a.txt文件

![image-20210223075353514](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224115075-1418786027.png)

![image-20210223075444099](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224114634-1363302945.png)

b.txt文件

![image-20210223080032900](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224114193-118277145.png)

跟可视化 的工具看到类似

![image-20210223080152638](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224113661-1524677288.png)

#### 使用3：其他作用（了解）

![image-20220427015041500](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224113310-2093115135.png)

这两个指令仅linux/solaris平台有效，所以无法在windows操作平台上演示，并且使用比较小众，不在多说

### 小结

![image-20210223080316314](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224113034-1691170043.png)

---

## 2.6、jhat：JDK自带堆分析工具（了解）

### 概览图

![image-20210223080549758](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224112686-679839334.png)

jhat命令在jdk9及其之后就被移除了，官方建议使用jvisualvm代替jhat，所以该指令只需简单了解一下即可

### 基本情况

jhat就是分析二进制dump文件工具。 VisualVM比jhat功能强大，一个是可视化的，一个是命令行的。（查看已经dump出来的文件不需要在命令行执行，即不需要在Linux下执行，所以可视化更方便。jhat不会在生产环境执行的）

![image-20210223080618448](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224112375-1321332202.png)

![image-20210223080711954](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224111975-1048675937.png)

会启动一个微型的服务

![image-20210223080920152](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224111690-379755305.png)

分析文件CPU会升高

![image-20210223080912677](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224111449-66028684.png)

访问

![image-20210223080949936](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224110895-1519098988.png)

堆空间的直方图

![image-20210223081026818](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224110586-682869693.png)

跟前面看到的JProfiler看到的类似

![image-20210223081034774](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224110176-2131931115.png)

OQL查询

![image-20210223081202163](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224109797-453584659.png)

![image-20210223081211054](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224109503-2078280774.png)

长度大于100，快速定位大文件，跟hibernate的HQL语法很像

![image-20210223081226276](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224109105-291174578.png)

![image-20210223081247287](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224108782-911405138.png)

### 基本语法

![image-20210223081426626](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224108512-2082687867.png)

![image-20210223081529816](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224108263-1216304371.png)

![image-20210223081454173](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224107993-1046618366.png)

![image-20210223081512946](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224107663-837984454.png)

![image-20210223081542720](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224107389-1171114499.png)

jhat就简单介绍，后面用VisualVM。

---

## 2.7、jstack：打印JVM中线程快照

jstack是打印线程的，jhat是打印堆空间的。

![image-20220427093135379](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224107122-1755660464.png)

### 基本情况

![image-20220427093407298](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224106799-663167450.png)

![image-20220427093413909](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224106459-45206437.png)

### 基本语法

![image-20220427093806346](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224106159-1079066860.png)

![image-20210223081804997](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224105866-802812514.png)

![image-20210223081815957](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224105551-580576250.png)

例子：

```java
package com.atguigu.jstack;
 
import java.util.Map;
import java.util.Set;
 
/**
 * 演示线程的死锁问题
 *
 * @author shkstart
 * @create 下午 3:20
 */
public class ThreadDeadLock {
 
    public static void main(String[] args) {
 
        StringBuilder s1 = new StringBuilder();
        StringBuilder s2 = new StringBuilder();
 
        new Thread(){
            @Override
            public void run() {
 
                synchronized (s1){
 
                    s1.append("a");
                    s2.append("1");
 
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
 
                    synchronized (s2){
                        s1.append("b");
                        s2.append("2");
 
                        System.out.println(s1);
                        System.out.println(s2);
                    }
 
                }
 
            }
        }.start();
 
 
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (s2){
 
                    s1.append("c");
                    s2.append("3");
 
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
 
                    synchronized (s1){
                        s1.append("d");
                        s2.append("4");
 
                        System.out.println(s1);
                        System.out.println(s2);
                    }
                }
            }
        }).start();
    }
 
 
}
 
```

运行，实际代码量会很大，很难一眼看出来死锁，长时间没有执行结果，就可以用工具看看是不是死锁了

![image-20210223082109200](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224105307-1497395685.png)

![image-20210223082215514](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224105008-2146714075.png)

其他线程都是正常的Runnable

![image-20210223082305255](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224104690-221410681.png)

守护线程，即垃圾回收线程，是正常的

![image-20210223082733894](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224104380-1987270912.png)

线程1的锁被线程0持有了，反过来也是，就找到死锁

![image-20210223082452225](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224104055-1211936654.png)

```typescript
package com.atguigu.jstack;
 
/**
 * 演示线程：TIMED_WAITING
 *
 * @author shkstart
 * @create 15:28
 */
public class TreadSleepTest {
    public static void main(String[] args) {
        System.out.println("hello - 1");
        try {
            Thread.sleep(1000 * 60 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
 
        System.out.println("hello - 2");
    }
}
 
```

运行

![image-20210223082536154](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224103733-558298943.png)

![image-20210223082549245](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224103509-1321083960.png)

等待，因为在睡眠

![image-20210223082822348](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224103299-55154368.png)

```java
package com.atguigu.jstack;
 
/**
 * 演示线程的同步
 *
 * @author shkstart
 * @create 15:31
 */
public class ThreadSyncTest {
    public static void main(String[] args) {
        Number number = new Number();
        Thread t1 = new Thread(number);
        Thread t2 = new Thread(number);
 
        t1.setName("线程1");
        t2.setName("线程2");
 
        t1.start();
        t2.start();
    }
}
 
class Number implements Runnable {
    private int number = 1;
 
    @Override
    public void run() {
        while (true) {
            synchronized (this) {
 
                if (number <= 100) {
 
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
 
                    System.out.println(Thread.currentThread().getName() + ":" + number);
                    number++;
 
                } else {
                    break;
                }
            }
        }
    }
 
}
```

![image-20210223201610101](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224103040-1747573176.png)

线程2在阻塞，线程1在睡眠，这就是线程的监控

![image-20210223201629412](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224102704-1115284821.png)

加-l参数，多一些锁的附加信息

![image-20220427095029578](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224102422-591362237.png)

![image-20220427094921830](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224102182-934010220.png)

补充

```java
package com.atguigu.jstack;
 
import java.util.Map;
import java.util.Set;
 
/**
 * @author shkstart
 * @create 15:51
 */
public class AllStackTrace {
    public static void main(String[] args) {
        Map<Thread, StackTraceElement[]> all = Thread.getAllStackTraces();
        Set<Map.Entry<Thread, StackTraceElement[]>> entries = all.entrySet();
        for(Map.Entry<Thread, StackTraceElement[]> en : entries){
            Thread t = en.getKey();
            StackTraceElement[] v = en.getValue();
            System.out.println("【Thread name is :" + t.getName() + "】");
            for(StackTraceElement s : v){
                System.out.println("\t" + s.toString());
            }
        }
    }
}
 
```

![image-20210223202053556](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224101776-1875329602.png)

![image-20210223202057023](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224101375-112729431.png)

复制这代码放到ThreadDeadLock

![image-20210223202257642](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224100946-1684111391.png)

```java
package com.atguigu.jstack;
 
import java.util.Map;
import java.util.Set;
 
/**
 * 演示线程的死锁问题
 *
 * @author shkstart
 * @create 下午 3:20
 */
public class ThreadDeadLock {
 
    public static void main(String[] args) {
 
        StringBuilder s1 = new StringBuilder();
        StringBuilder s2 = new StringBuilder();
 
        new Thread(){
            @Override
            public void run() {
 
                synchronized (s1){
 
                    s1.append("a");
                    s2.append("1");
 
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
 
                    synchronized (s2){
                        s1.append("b");
                        s2.append("2");
 
                        System.out.println(s1);
                        System.out.println(s2);
                    }
 
                }
 
            }
        }.start();
 
 
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (s2){
 
                    s1.append("c");
                    s2.append("3");
 
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
 
                    synchronized (s1){
                        s1.append("d");
                        s2.append("4");
 
                        System.out.println(s1);
                        System.out.println(s2);
                    }
                }
            }
        }).start();
 
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
 
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<Thread, StackTraceElement[]> all = Thread.getAllStackTraces();//追踪当前进程中的所有的线程
                Set<Map.Entry<Thread, StackTraceElement[]>> entries = all.entrySet();
                for(Map.Entry<Thread, StackTraceElement[]> en : entries){
                    Thread t = en.getKey();
                    StackTraceElement[] v = en.getValue();
                    System.out.println("【Thread name is :" + t.getName() + "】");
                    for(StackTraceElement s : v){
                        System.out.println("\t" + s.toString());
                    }
                }
            }
        }).start();
    }
 
 
}
 
```

运行

![image-20220427095855504](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224100512-64587131.png)

![image-20210223202523462](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224100210-132042193.png)

![image-20210223202628874](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224059951-296638508.png)

![image-20210223202652804](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224059601-365781905.png)

用java代码也能大概看出来是死锁，但不是很直白。

---

## 2.8、jcmd：多功能命令行

![image-20210223210153403](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224059255-1809583806.png)

一个顶多个，真的很强

### 概览图

![image-20220427104558313](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224058942-715903108.png)

### 基本情况

![image-20210223210108336](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224058649-1018151346.png)

类似于jps

![image-20210223210300389](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224058178-886671453.png)

![image-20210223210323672](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224057819-645757492.png)

`jcmd -l`：列出所有的JVM进程

![image-20210223210350522](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224057466-1763606842.png)

![image-20220427105455011](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224057067-2097404079.png)

`jcmd 进程号 help`：针对指定的进程，列出支持的所有具体命令

![image-20220427105747424](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224056787-463072525.png)

```
jcmd 进程号 具体命令
```

根据命令来替换之前的那些操作：
Thread.print 可以替换 jstack指令
GC.class_histogram 可以替换 jmap中的-histo操作
GC.heap_dump 可以替换 jmap中的-dump操作
GC.run 可以查看GC的执行情况
VM.uptime 可以查看程序的总执行时间，可以替换jstat指令中的-t操作
VM.system_properties 可以替换 jinfo -sysprops 进程id
VM.flags 可以获取JVM的配置参数信息

相当于jstat的操作

![image-20210223210621694](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224056429-199614344.png)

相当于jmap -histo的操作

![image-20210223210735019](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224055967-1913986968.png)

相当于jmap -dump

![image-20210223210812136](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224055643-92308212.png)

进行的执行时间（相当于jstat -t ）

![image-20210223210946913](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224055327-807002528.png)

相当于jinfo -sysprops pid的操作

![image-20210223211056270](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224055052-1376346707.png)

![image-20210223211105541](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224054669-482343502.png)

JVM参数

![image-20210223211220744](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224054096-480417145.png)

---

## 2.9、jstatd：远程主机信息收集

![image-20210223211312617](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501224053566-133957874.png)

# 3、JVM监控及诊断工具 - GUI篇

---

## 3.1、工具概述

![image-20220427140712082](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230247786-1095164871.png)

![image-20220427140729807](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230247474-461104681.png)

![image-20220427140958298](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230247109-1293696983.png)

jvisualvm是自带的，Visual VM是独立下载的，都是同一个。

JMC本来是Jrocket VM，后来被Oracle收购了。

![image-20210223214640944](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230246821-1079341705.png)

---

## 3.2、jConsole

### 概览图

![image-20210223215236029](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230246464-220864153.png)

### 基本概述

![image-20210223215301814](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230246095-180375109.png)

![image-20210223215358352](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230245658-1882581226.png)

### 启动

方式一：在jdk安装目录中找到jconsole.exe，双击该可执行文件就可以

方式二：打开DOS窗口，直接输入jconsole就可以了

```java
package com.atguigu.jconsole;
 
import java.util.ArrayList;
import java.util.Random;
 
/**
 * -Xms600m -Xmx600m -XX:SurvivorRatio=8
 * @create 2020  17:51
 */
public class HeapInstanceTest {
    byte[] buffer = new byte[new Random().nextInt(1024 * 100)];
 
    public static void main(String[] args) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<HeapInstanceTest> list = new ArrayList<HeapInstanceTest>();
        while (true) {
            list.add(new HeapInstanceTest());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
 
```

运行

![image-20210223215514852](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230245320-107669214.png)

运行jconsole（进程显示就是通过jps实现的）

![image-20210223215547400](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230245026-1136725682.png)

### 三种连接方式

![image-20210223215639950](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230244739-786788050.png)

#### Local

使用JConsole连接一个正在本地系统运行的JVM，并且`执行程序的用户和运行JConsole的用户需要是同一个用户`。JConsole使用文件系统的授权通过RMI连接器链接到平台的MBean的服务器上。这种从本地连接的监控能力只有Sun的JDK具有。

#### Remote

使用下面的URL通过RMI连接器连接到一个JMX代理，service:jmx:rmi:///jndi/rmi://hostName:portNum/jmxrmi。JConsole为建立连接，需要在环境变量中设置mx.remote.credentials来指定用户名和密码，从而进行授权。

#### Advanced

使用一个特殊的URL连接JMX代理。一般情况使用自己定制的连接器而不是RMI提供的连接器来连接JMX代理，或者是一个使用JDK1.4的实现了JMX和JMX Rmote的应用

### 主要作用

1、概览

![image-20220427142659013](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230244443-1957861287.png)

2、内存

![image-20220427142745984](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230244143-763395944.png)

![image-20210223215834784](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230243834-1174489813.png)

这些区域可以点击，折线会跟着变化

![image-20210223215959166](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230243592-676181913.png)

![image-20210223220041181](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230243264-209686926.png)

可以强制GC

![image-20210223220100112](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230242922-1319705190.png)

如果OOM了

![image-20210223220309042](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230242599-1626363692.png)

看到满了

![image-20210223220303446](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230242284-988901296.png)

3、根据线程检测死锁

![image-20220427142807736](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230241910-1268714929.png)

```java
package com.atguigu.jconsole;
 
import java.util.Map;
import java.util.Set;
 
/**
 * 演示线程的死锁问题
 *
 * @author shkstart
 * @create 下午 3:20
 */
public class ThreadDeadLock {
 
    public static void main(String[] args) {
 
        StringBuilder s1 = new StringBuilder();
        StringBuilder s2 = new StringBuilder();
 
        new Thread(){
            @Override
            public void run() {
 
                synchronized (s1){
 
                    s1.append("a");
                    s2.append("1");
 
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
 
                    synchronized (s2){
                        s1.append("b");
                        s2.append("2");
 
                        System.out.println(s1);
                        System.out.println(s2);
                    }
 
                }
 
            }
        }.start();
 
 
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (s2){
 
                    s1.append("c");
                    s2.append("3");
 
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
 
                    synchronized (s1){
                        s1.append("d");
                        s2.append("4");
 
                        System.out.println(s1);
                        System.out.println(s2);
                    }
                }
            }
        }).start();
 
    }
}
 
```

![image-20210223220442565](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230241670-1743039642.png)

![image-20210223220422906](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230241374-270065625.png)

监测死锁

![image-20210223220522735](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230241074-381135098.png)

告诉我们死锁了

![image-20210223220540394](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230240754-1470114551.png)

![image-20210223220552988](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230240512-1700354695.png)

4、类

![image-20220427143000825](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230240242-1728781231.png)

5、VM 概要

![image-20220427143029138](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230239945-993485838.png)

---

## 3.3、Visual VM

### 概览图

![image-20210223220754750](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230239563-1663468858.png)

### 基本概述

多合一就是多个工具集合一起

![image-20210223220803152](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230239175-328328220.png)

![image-20210223221205102](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230238672-1838265264.png)

使用：
在jdk安装目录中找到jvisualvm.exe，然后双击执行即可
打开DOS窗口，输入jvisualvm就可以打开该软件

启动整体界面

![image-20220427144216049](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230238325-795318071.png)

### 插件安装

![image-20220427144600974](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230237948-1610132537.png)

第一种，Visual VM本身作为一种插件，在它的内部也可以安装其他的插件，例如安装Visual GC

![image-20210223221349490](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230237321-2099237087.png)

![image-20210223221410522](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230236697-209472668.png)

之前就讲过这个Visual GC

![image-20210223221456641](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230236250-2127266383.png)

第一种还可以在工具内安装，前面是GitHub下载安装

![image-20210223221534385](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230235862-2040965367.png)

![image-20210223221610571](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230235530-1508487933.png)

要装上visual gc

![image-20210223221736020](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230235102-1149771897.png)

第二种是在IDEA上安装Visual VM这个插件

![image-20220427145056339](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230234811-29716057.png)

![image-20210223221905602](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230234437-981603414.png)

![image-20210223222046565](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230234088-716421104.png)

需要配置，不然点击图标无法正确打开（两个路径）

![image-20210223221927668](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230233770-1143487412.png)

点击直接使用

![image-20210223222252479](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230233327-504891062.png)

### 连接方式

![image-20210223222317949](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230232929-1056735076.png)

远程连接可以可视化查看生产环境的JVM，后面案例中讲（一开始以为生成环境只能用命令行呢）

### 主要功能

![image-20220427145506487](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230232676-1959285827.png)

#### 1.生成/读取堆内存快照

一、生成堆内存快照
1、方式1：

![image-20220427151343081](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230232433-1836774731.png)

2、方式2：

![image-20220427151413503](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230232182-676107610.png)

注意：
生成堆内存快照如下图：

![image-20220427151436881](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230231947-643583603.png)

这些快照存储在内存中，当线程停止的时候快照就会丢失，如果还想利用，可以将快照进行另存为操作，如下图：

![image-20220427151446577](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230231724-1518534442.png)

二、装入堆内存快照

![image-20220427151455619](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230231406-978593323.png)

#### 2.查看JVM参数和系统属性

#### 3.查看运行中的虚拟机进程

#### 4.生成/读取线程快照

一、生成线程快照
1、方式1：

![image-20220427151618029](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230231127-1558275075.png)

2、方式2：

注意：
生成线程快照如下图：

![image-20220427151635626](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230230914-386844398.png)

这些快照存储在内存中，当线程停止的时候快照就会丢失，如果还想利用，可以将快照进行另存为操作，如下图：

![image-20220427151650301](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230230650-1878485133.png)

二、装入线程快照

![image-20220427151705994](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230230352-1500985226.png)

#### 5.程序资源的实时监控

#### 6.其他功能

JMX代理连接

远程环境监控

CPU分析和内存分析

例子

```java
package com.atguigu.jvisualvm;
 
import java.util.ArrayList;
import java.util.Random;
 
/**
 * -Xms600m -Xmx600m -XX:SurvivorRatio=8
 * @author shkstart  shkstart@126.com
 * @create 2020  21:12
 */
public class OOMTest {
    public static void main(String[] args) {
        ArrayList<Picture> list = new ArrayList<>();
        while(true){
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            list.add(new Picture(new Random().nextInt(100 * 50)));
        }
    }
}
 
class Picture{
    private byte[] pixels;
 
    public Picture(int length) {
        this.pixels = new byte[length];
    }
}
package com.atguigu.jvisualvm;

import java.util.ArrayList;
import java.util.Random;

/**
 * -Xms600m -Xmx600m -XX:SurvivorRatio=8
 * @author shkstart  shkstart@126.com
 * @create 2020  17:51
 */
public class HeapInstanceTest {
    byte[] buffer = new byte[new Random().nextInt(1024 * 100)];

    public static void main(String[] args) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<HeapInstanceTest> list = new ArrayList<HeapInstanceTest>();
        while (true) {
            list.add(new HeapInstanceTest());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```

两个例子都运行，然后监控

概述

![image-20220427162659948](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230230090-605033420.png)

jinfo命令可以查看或者jps也可以

![image-20220427162800687](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230229832-1999032685.png)

jinfo命令 -sysprops PID

![image-20220427162834482](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230229569-2075366088.png)

![image-20220427162919339](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230229276-1655580904.png)

可以执行垃圾回收

![image-20220427162950991](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230229005-1214628889.png)

![image-20220427163018476](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230228737-327339758.png)

没有颜色的线程就是终止了（之前是运行的）。

![image-20220427163051002](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230228468-778255546.png)

还有不同颜色表示不同的线程状态

![image-20220427163128519](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230228206-276434763.png)

![image-20220427163325251](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230227974-266342950.png)

![image-20220427163358229](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230227736-1495690596.png)

需要安装插件

![image-20220427163722266](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230227478-822865009.png)

![image-20220427163420484](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230227219-2006583480.png)

HeapInstanceTest已经OOM了

![image-20220427164001196](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230226972-1149748909.png)

因为新生代想放老年代，老年代满了

![image-20220427163930754](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230226718-668066628.png)

dump的方式

![image-20220427164105869](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230226469-2075167015.png)

![image-20220427164135624](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230226217-873074113.png)

或者

![image-20220427164234359](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230225968-154333110.png)

![image-20220427164254569](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230225700-298712691.png)

dump出来只是一个临时快照，要保存就需要另存为

![image-20210223224019689](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230225422-1261377979.png)

![image-20210223224057513](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230225073-1387488053.png)

删除只是删除临时快照，另存为的还在

![image-20210223224142938](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230224773-458851483.png)

查看dump文件（dump文件一般都很大，随随便便就几百M）

![image-20220427165227245](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230224496-430664980.png)

![image-20220427165403451](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230224216-850741409.png)

双击具体的类会跳转到实例数

![image-20220427165450098](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230223924-976372462.png)

![image-20210223224438002](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230223563-120856819.png)

OQL

![image-20220427165759173](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230223228-375495855.png)

![image-20210223224545735](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230222887-1395236069.png)

![image-20210223224559846](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230222416-1952178080.png)

比较

![image-20210223224631098](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230222021-1519505404.png)

当前同一个目录下的另一个

![image-20210223224655951](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230221698-888285814.png)

![image-20210223224725242](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230221191-1530421727.png)

![image-20210223224841566](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230220577-770393765.png)

![image-20210223224858969](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230220123-1729481567.png)

详细的数据信息

![image-20210223224913102](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230219706-1337377966.png)

线程快照文件

![image-20210223225012208](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230219341-1327519189.png)

两线程都处于监控状态，检测到死锁

![image-20220427170917736](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230219043-271504718.png)

![image-20220427171111894](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230218789-1853621182.png)

也可以右键

![image-20210223225312204](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230218489-1694131836.png)

查看

![image-20220427171339920](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230218230-1205023678.png)

装入

![image-20210223225440077](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230217876-1572271872.png)

抽样器

![image-20210223225510025](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230217594-1213024989.png)

热点 - 方法 就是占用CPU时间比较长的方法（这个功能可以呀）

![image-20220427171702897](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230217303-891877132.png)

![image-20220427171824987](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230217055-1359428041.png)

线程多还可以过滤

![image-20220427171935891](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230216742-729440439.png)

因为sleep，所以占用长

![image-20210223225737353](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230216421-2071163543.png)

所以main方法占用CPU比较长

![image-20220427172154107](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230216064-735979787.png)

可以快照一下保存当前状态

![image-20210223230012213](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230215715-411881948.png)

![image-20210223230032481](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230215382-1195667332.png)

CPU占有率高，就可以这样采样，看看是什么原因造成的。

内存采样

![image-20220427172410781](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230215121-1340965770.png)

![image-20220427172525584](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230214863-385111588.png)

OOM或者Full GC次数多就可以查看哪个线程的数据占用内存多

也可以快照保存

![image-20220427172629258](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230214629-908498507.png)

---

## 3.4、eclipse MAT

MAT主要就是专门分析dump文件，更擅长dump文件分析。

### 概览图

![image-20210224072311669](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230214301-1914777111.png)

### 基本概述

![image-20210224072353720](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230213919-122090083.png)

![image-20210224072436878](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230213472-1918777462.png)

![image-20210224072545262](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230213069-1411115432.png)

```java
package com.atguigu.mat;
 
import java.util.ArrayList;
import java.util.Random;
 
/**
 * -Xms600m -Xmx600m -XX:SurvivorRatio=8
 * @author shkstart  shkstart@126.com
 * @create 2020  21:12
 */
public class OOMTest {
    public static void main(String[] args) {
        ArrayList<Picture> list = new ArrayList<>();
        while(true){
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            list.add(new Picture(new Random().nextInt(100 * 50)));
        }
    }
}
 
class Picture{
    private byte[] pixels;
 
    public Picture(int length) {
        this.pixels = new byte[length];
    }
}
```

参数，运行

![image-20210224073609532](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230212733-1316017057.png)

![image-20210224073737594](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230212502-1003354339.png)

![image-20210224073744213](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230212271-2112696993.png)

![image-20210224073805284](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230212028-456239140.png)

![image-20210224073813464](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230211742-897002841.png)

![image-20210224073908896](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230211408-784927389.png)

点finish

![image-20210224073940680](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230210962-51347328.png)

![image-20210224073951924](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230210587-583612486.png)

多个一个压缩包

![image-20210224074011026](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230210252-1302163255.png)

也可以根据正常运行的线程生成dump文件

![image-20210224074044008](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230209939-1189969344.png)

类似于jps的操作

![image-20220428112116566](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230209645-1551270214.png)

![image-20210224074201672](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230209235-1946745306.png)

![image-20210224074242146](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230208440-1972832619.png)

打开上面刚生成的dump文件

![image-20210224074302099](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230207925-1534931016.png)

![image-20210224074618284](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230207425-2065954047.png)

点击finish

![image-20210224074651261](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230206974-1459962988.png)

![image-20210224074852124](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230206487-1149789471.png)

![image-20210224075023302](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230206043-1037787291.png)

![image-20210224075104907](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230205660-248060414.png)

小图标

![image-20210224075219099](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230205345-422333655.png)![image-20210224080146485](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230205008-1927585023.png)

![image-20210224075647520](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230204754-322031923.png)

![image-20210224075702111](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230204396-149114556.png)

分别点击

![image-20210224075824001](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230203894-605338036.png)

![image-20220428112647187](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230203597-349711957.png)

![image-20220428112716857](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230203287-1581619861.png)

![image-20220428112744630](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230202997-1081723936.png)

![image-20210224080523685](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230202666-368377458.png)

通过图形列举最大的对象

![image-20210224080539276](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230202294-1388218955.png)

![image-20210224080447092](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230201956-973667318.png)

例子中的图很单一，但实际项目中，图很丰富的，例如：

![image-20210224080311252](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230201633-2091176792.png)

![image-20210224080333903](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230201304-1349572839.png)

![image-20210224080341918](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230200957-1043254591.png)

分析先从大的内存分析，因为泄露的可能性更大。

重复类（被多个加载器加载的类）

![image-20210224080700716](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230200652-1704887572.png)

这个报告就是一开始运行生成的那个

![image-20210224080808764](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230200217-156160827.png)

![image-20210224080804535](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230159816-702943363.png)

![image-20210224080944641](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230159405-2011464586.png)

![image-20210224081109231](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230158996-1105624723.png)

Objects就是对象的个数；shallow heap就是浅堆，即一个对象消耗内存的大小，不包括其引用的对象的对象大小；深堆就是该对象被回收，相关的内存能回收多少。大概理解一下。

![image-20210224081143626](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230158482-345755851.png)

比如，id占大小，name、history引用占用大小，都是浅堆，但是name、history指向的对象的大小不算在浅堆中

![image-20210224081419433](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230158096-1481738201.png)

点击一下，左边就有相关信息

![image-20210224081751887](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230157696-866164567.png)

分组（这个有用）

![image-20210224082217495](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230157286-433906592.png)

![image-20210224082226204](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230156794-314268091.png)

默认分组

![image-20210224082250376](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230156440-757353692.png)

排序

![image-20210224082318786](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230155965-1047376675.png)

正则搜索（这个有用）

![image-20210224082401816](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230155606-1099721766.png)

怀疑一个对象就可以跟踪GC Roots，排除虚引用、弱引用、软引用，即显示强引用

![image-20210224082450747](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230155122-821476915.png)

看看谁引用，引用是否合理

![image-20210224082601432](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230154602-2049007659.png)

比较

![image-20210224082721415](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230154143-1697389726.png)

![image-20210224082730144](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230153699-1770402205.png)

看看哪些对象增加的快，排序

![image-20210224082750149](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230153251-1574995902.png)

![image-20210224082756202](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230152875-2002897027.png)

### 获取堆dump文件

#### dump文件内存

![image-20210224072616638](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230152465-678306117.png)

#### 两点说明

![image-20210224072922170](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230151998-923933919.png)

#### 获取dump文件

![image-20220427202545062](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230151604-332646798.png)

![image-20220427202554570](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230151272-349252607.png)

### 分析堆dump文件

#### `histogram`：展示了各个类的实例数目以及这些实例的Shallow heap或者Retained heap的总和

l功能类似 **jmap -histo pid**

图标：

![image-20220427202902853](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230150980-1305024936.png)

具体内容：

![image-20220427202914172](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230150729-1583030839.png)

#### `thread overview`

作用：

1、查看系统中的Java线程

2、查看局部变量的信息

图标

![image-20220427203102873](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230150501-1356009872.png)

具体信息：

![image-20220427203112654](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230150194-987140882.png)

local就是局部变量

![image-20210224220415094](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230149943-80696248.png)

里面数组的名字叫elementData，数组内就是一个个的Picture

![image-20210224220602306](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230149593-370524741.png)

展开，sleep

![image-20210224220259879](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230149288-1677854014.png)

![image-20210224220317089](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230148979-923066315.png)

内存泄露疑点报告

![image-20210224220659350](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230148643-1157003284.png)

![image-20210224220814051](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230148260-776935355.png)

a的生命周期很长，且大小不大（才38.5MB），但是a引用了b，且b很大(可能1000MB)，b本来出了括号就应该回收（生命周期不应该那么长），由于a引用了，导致b无法及时被回收，这样也认为内存泄露，这就是广义的内存泄露。内存泄露有狭义和广义之分。

![image-20210224220915307](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230147712-1865904451.png)

Object数组就是他

![image-20210224221007939](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230146785-642471335.png)

后面详细讲。

#### 获得对象互相引用的关系

outgoing就是引用了谁，incoming就是谁引用了我。

**`with outgoing references`**

图示：

![image-20220427203449322](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230146451-967806182.png)

结果：

![image-20220427203514277](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230146172-835859278.png)

**`with incoming references`**

图示：

![image-20220427203641066](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230145946-1637339522.png)

结果：

只有数组引用了（假如还有一个静态变量引用了Picture，这时候就可以分析这个引用是否应该存在，要么释放引用，要么改成弱引用等）

![image-20220427203651542](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230145719-296220528.png)

对照一下代码

![image-20210224221927376](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230145492-2069207047.png)

#### 浅堆与深堆

##### shallow heap

![image-20220427203743161](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230145160-2039131842.png)

通过Mat工具分析

![image-20210224222245504](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230144801-87068785.png)

##### retained heap

![image-20210224223058822](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230144422-1627394908.png)

注意：
当前深堆大小 = 当前对象的浅堆大小 + 对象中所包含对象的深堆大小

##### 补充：对象实际大小

![image-20220427204316520](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230144031-1129429603.png)

##### 练习

![image-20220427204726327](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230143694-971347803.png)

![image-20210224224058643](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230143318-157195007.png)

![image-20220427204741115](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230142990-1053114540.png)

B深堆大小和实际对象大小都是BCD

##### 案例分析：StudentTrace

```java
/**
 * 有一个学生浏览网页的记录程序，它将记录 每个学生访问过的网站地址。
 * 它由三个部分组成：Student、WebPage和StudentTrace三个类
 *
 *  -XX:+HeapDumpBeforeFullGC -XX:HeapDumpPath=c:\code\student.hprof
 * @create 16:11
 */
public class StudentTrace {
    static List<WebPage> webpages = new ArrayList<WebPage>();
 
    public static void createWebPages() {
        for (int i = 0; i < 100; i++) {
            WebPage wp = new WebPage();
            wp.setUrl("http://www." + Integer.toString(i) + ".com");
            wp.setContent(Integer.toString(i));
            webpages.add(wp);
        }
    }
 
    public static void main(String[] args) {
        createWebPages();//创建了100个网页
        //创建3个学生对象
        Student st3 = new Student(3, "Tom");
        Student st5 = new Student(5, "Jerry");
        Student st7 = new Student(7, "Lily");
 
        for (int i = 0; i < webpages.size(); i++) {
            if (i % st3.getId() == 0)
                st3.visit(webpages.get(i));
            if (i % st5.getId() == 0)
                st5.visit(webpages.get(i));
            if (i % st7.getId() == 0)
                st7.visit(webpages.get(i));
        }
        webpages.clear();
        System.gc();
    }
}
 
class Student {
    private int id;
    private String name;
    private List<WebPage> history = new ArrayList<>();
 
    public Student(int id, String name) {
        super();
        this.id = id;
        this.name = name;
    }
 
    public int getId() {
        return id;
    }
 
    public void setId(int id) {
        this.id = id;
    }
 
    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
 
    public List<WebPage> getHistory() {
        return history;
    }
 
    public void setHistory(List<WebPage> history) {
        this.history = history;
    }
 
    public void visit(WebPage wp) {
        if (wp != null) {
            history.add(wp);
        }
    }
}
 
 
class WebPage {
    private String url;
    private String content;
 
    public String getUrl() {
        return url;
    }
 
    public void setUrl(String url) {
        this.url = url;
    }
 
    public String getContent() {
        return content;
    }
 
    public void setContent(String content) {
        this.content = content;
    }
}
 
```

运行（参数：生成dump文件）

![image-20220428154246117](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230142737-1360530552.png)

![image-20220428154404375](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230142472-11606553.png)

![image-20210224225320733](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230142218-1205229469.png)

![image-20220428154443901](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230141983-999317457.png)

![image-20220428154546538](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230141746-87757023.png)

![image-20220428154617766](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230141487-1058016856.png)

![image-20220428155219058](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230140980-1969245481.png)

![image-20220428155338116](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230140716-1915715115.png)

24 = int 4字节 + String引用 4字节 + history引用 4字节 + 对象头 8字节 = 20字节 + 8字节对齐

![image-20210224225537951](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230140433-1971361416.png)

![image-20220428155545373](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230140161-268786033.png)

回收能回收多少就看深堆大小的内存

![image-20220428155659899](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230139926-1646777027.png)

三个student的深度大小不同是由于3整除的多一些，7整除少一些

![image-20210224225920154](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230139640-372181548.png)

深堆的计算过程

![image-20220428172922148](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230139328-697709490.png)

2280字节的大小只能该学生访问的所有网页，有些能被7整除的数也能被其他数整除，该学生访问的所有网页的深堆就没有2280字节那么大了。（实际是1288，即elementData的深堆大小）

该学生Lily被回收了，他的WebPage是不是就会被回收，就看incoming，看看是否有其他人都访问该WebPage

![image-20210225074114650](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230138952-1847858156.png)

发现有3个学生，所以Lily回收，该WebPage不会被回收

![image-20210225074259457](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230138540-333938835.png)

右键看下incoming

![image-20210225074415626](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230138002-254401799.png)

只有Lily，所以Lily回收了，该WebPage就会被回收

![image-20210225074435429](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230137479-951361047.png)

##### 支配树

![image-20210225074537672](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230137054-1015721598.png)

引用图：

![image-20220427205952302](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230136667-1636962604.png)

访问E要经过C，C就是E的支配者

![image-20210225074951254](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230136367-45572734.png)

D不是H的支配者

![image-20210225075050964](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230136030-1706863771.png)

根据对象引用图画出支配树（C是DEFG的支配者）

![image-20210225075716343](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230135691-1373581882.png)

画出支配树之后，就可以知道要是回收C，C所支配的都要回收

注意：
跟随我一起来理解如何从“对象引用图---》支配树”，首先需要理解支配者（如果要到达对象B，毕竟经过对象A，那么对象A就是对象B的支配者，可以想到支配者大于等于1），然后需要理解直接支配者（在支配者中距离对象B最近的对象A就是对象B的直接支配者，你要明白直接支配者不一定就是对象B的上一级，然后直接支配者只有一个），然后还需要理解支配树是怎么画的，其实支配树中的对象与对象之间的关系就是直接支配关系，也就是上一级是下一级的直接支配者，只要按照这样的方式来作图，肯定能从“对象引用图---》支配树”

在Eclipse MAT工具中如何查看支配树：

![image-20210225080231578](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230135377-629249660.png)

![image-20210225080239390](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230135062-1644503658.png)

![image-20210225080247280](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230134649-584078646.png)

正则搜索

![image-20210225080309225](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230134057-1433191674.png)

主线程的三个学生

![image-20210225080339748](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230133579-568592906.png)

注意区别

![image-20210225080450636](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230133047-1495798855.png)

Lily只有8个，这就是支配树

![image-20210225080523144](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230132379-196411145.png)

前面说错了，之前说Size是dump文件的大小，其实是堆空间的大小。

### 案例：Tomcat堆溢出分析

![image-20220427210902366](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230131979-836982074.png)

![image-20210225080842033](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230131712-1308438607.png)

点击最大的空间，右键，看看都引用了谁

![image-20210225081325249](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230131370-674704703.png)

![image-20210225081349598](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230131051-911086177.png)

![image-20210225081403648](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230130644-573556288.png)

看下具体的session，怀疑Tomcat收到大量的session导致的

![image-20210225081455264](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230130175-1493444546.png)

OQL查询

![image-20210225081613382](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230129656-775335512.png)

![image-20210225081633284](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230129205-1858264032.png)

验证了有大量的session（9千多个），下一步就要验证session是不是都是短时间内被创建的

随便选一个session，属性信息有创建时间、结束时间，做差就是存活时间，下图是相差1毫秒

![image-20210225081825154](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230128704-1039526085.png)

OQL查询，列出来所有的创建时间，两次排序 （拿到最早的创建时间和最后一次的创建时间，做差就能得到全部创建session的总时间段）

![image-20210225081954909](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230128246-357186523.png)

每个案例的思路不一样的，但是分析的技术都是一样的。

---

## 3.5、补充1：再谈内存泄露

![image-20220428200828889](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230127842-690466109.png)

### 内存泄露的理解与分析

![image-20210225082755097](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230127541-1016303849.png)

两个都是 是，就不是内存泄露。 是 - 否，就是内存泄露； 否 - 否，第一个否，第二个问题就不用问了。

![image-20210225203842546](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230127241-1054356533.png)

![image-20210225204004810](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230126936-1593506916.png)

![image-20210225204039765](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230126401-1806278513.png)

![image-20210225204208283](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230125996-1480055564.png)

![image-20210225204256725](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230125694-1056646816.png)

偶然发生，比如资源关闭问题。

隐式泄露，就是广义的内存泄露。

### Java中内存泄露的8种情况

![image-20220428201240205](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230125432-770719605.png)

有些面试会要求写出内存泄露的例子，写出内存泄露不是为了让内存泄露，而是为了熟悉或者证明遇到过或解决过内存泄露的问题。

![image-20210225204659905](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230125075-1878970483.png)

![image-20210225204710816](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230124774-897028592.png)

![image-20210225204828224](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230124460-621919476.png)

![image-20210225204946059](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230124018-201831599.png)

![image-20210225205030463](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230123576-1702734691.png)

msg放到方法内部定义就行，作用域不用那么大，定义在外面需要回收UsingRandom对象才会回收msg。

![image-20210225205220528](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230123192-144275162.png)

如果必须要定义在外面，最后不用了要设置为null，这个方法我之前有试过。

![image-20210225205324798](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230122893-2087326512.png)

![image-20210225213309594](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230122456-270525436.png)

```java
package com.atguigu.memoryleak;
 
import java.util.HashSet;
 
/**
 * 演示内存泄漏
 * @author shkstart
 * @create 14:47
 */
public class ChangeHashCode1 {
    public static void main(String[] args) {
        HashSet<Point> hs = new HashSet<Point>();
        Point cc = new Point();
        cc.setX(10);//hashCode = 41
        hs.add(cc);
 
        cc.setX(20);//hashCode = 51  此行为导致了内存的泄漏
 
        System.out.println("hs.remove = " + hs.remove(cc));//false
        hs.add(cc);
        System.out.println("hs.size = " + hs.size());//size = 2
 
        System.out.println(hs);
    }
 
}
 
class Point {
    int x;
 
    public int getX() {
        return x;
    }
 
    public void setX(int x) {
        this.x = x;
    }
 
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        return result;
    }
 
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Point other = (Point) obj;
        if (x != other.x) return false;
        return true;
    }
 
    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                '}';
    }
}
```

![image-20210225213757498](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230121677-1631053454.png)

打印出来两个相同的对象（本来是无法存入两个相同的对象的，但是中途修改了hash值，就存进去了，是否相同是根据hash值来的）

![image-20210225213844226](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230121356-889598812.png)

![image-20210225214016131](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230121076-1335606099.png)

```java
package com.atguigu.memoryleak;
 
import java.util.HashSet;
 
/**
 * 演示内存泄漏
 *
 * @author shkstart
 * @create 14:43
 */
public class ChangeHashCode {
    public static void main(String[] args) {
        HashSet set = new HashSet();
        Person p1 = new Person(1001, "AA");
        Person p2 = new Person(1002, "BB");
 
        set.add(p1);
        set.add(p2);
 
        p1.name = "CC";//导致了内存的泄漏
        set.remove(p1); //删除失败
 
        System.out.println(set);
 
        set.add(new Person(1001, "CC"));
        System.out.println(set);
 
        set.add(new Person(1001, "AA"));
        System.out.println(set);
 
    }
}
 
class Person {
    int id;
    String name;
 
    public Person(int id, String name) {
        this.id = id;
        this.name = name;
    }
 
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
 
        Person person = (Person) o;
 
        if (id != person.id) return false;
        return name != null ? name.equals(person.name) : person.name == null;
    }
 
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
 
    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
```

hash是根据name值来的

运行

![image-20210225214336380](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230120867-129630671.png)

打印

![image-20210225214106696](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230120637-2141702654.png)

注释掉就能删除了

![image-20210225214150082](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230120344-247334825.png)

运行

![image-20210225214352671](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230120067-1191268895.png)

![image-20210225214419122](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230119757-1989143775.png)

分析

![image-20210225215944627](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230119412-1357723533.png)

Map判断对象是否一样跟重写的hashcode、equals方法有关系。

结论：

![image-20210225220111512](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230119068-499465290.png)

![image-20210225220207001](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230118757-1713900615.png)

```java
package com.atguigu.memoryleak;
 
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
 
/**
 * 演示内存泄漏
 *
 * @author shkstart
 * @create 14:53
 */
public class MapTest {
    static Map wMap = new WeakHashMap();
    static Map map = new HashMap();
 
    public static void main(String[] args) {
        init();
        testWeakHashMap();
        testHashMap();
    }
 
    public static void init() {
        String ref1 = new String("obejct1");
        String ref2 = new String("obejct2");
        String ref3 = new String("obejct3");
        String ref4 = new String("obejct4");
        wMap.put(ref1, "cacheObject1");
        wMap.put(ref2, "cacheObject2");
        map.put(ref3, "cacheObject3");
        map.put(ref4, "cacheObject4");
        System.out.println("String引用ref1，ref2，ref3，ref4 消失");
 
    }
 
    public static void testWeakHashMap() {
 
        System.out.println("WeakHashMap GC之前");
        for (Object o : wMap.entrySet()) {
            System.out.println(o);
        }
        try {
            System.gc();
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("WeakHashMap GC之后");
        for (Object o : wMap.entrySet()) {
            System.out.println(o);
        }
    }
 
    public static void testHashMap() {
        System.out.println("HashMap GC之前");
        for (Object o : map.entrySet()) {
            System.out.println(o);
        }
        try {
            System.gc();
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("HashMap GC之后");
        for (Object o : map.entrySet()) {
            System.out.println(o);
        }
    }
 
}
/**
 * 执行结果：
 * String引用ref1，ref2，ref3，ref4 消失
 * WeakHashMap GC之前
 * obejct2=cacheObject2
 * obejct1=cacheObject1
 * WeakHashMap GC之后
 * HashMap GC之前
 * obejct4=cacheObject4
 * obejct3=cacheObject3
 * Disconnected from the target VM, address: '127.0.0.1:51628', transport: 'socket'
 * HashMap GC之后
 * obejct4=cacheObject4
 * obejct3=cacheObject3
 **/
 
```

WeakHashMap在执行GC后就清理掉了，HashMap在执行GC之后，没有被清理掉。

![image-20210225220459819](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230118344-2041914287.png)

![image-20210225220535263](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230118047-830104667.png)

### 内存泄露案例分析

![image-20210225220555132](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230117761-1876004693.png)

隐秘的泄露问题

```java
package com.atguigu.memoryleak;
 
import java.util.Arrays;
import java.util.EmptyStackException;
 
/**
 * @author shkstart
 * @create 15:05
 */
public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
 
    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }
 
    public void push(Object e) { //入栈
        ensureCapacity(); // 容量是否足够，否则就扩容
        elements[size++] = e;
    }
    //存在内存泄漏
    public Object pop() { //出栈
        if (size == 0)
            throw new EmptyStackException();
        return elements[--size]; // 只是移动了游标，但是对象引用还在，就无法被回收
    }
 
    // 正确的写法
//    public Object pop() {
//        if (size == 0)
//            throw new EmptyStackException();
//        Object result = elements[--size];
//        elements[size] = null;
//        return result;
//    }
 
    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}
 
```

![image-20210225221111962](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230117506-1953087235.png)

![image-20210225220849907](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230117204-1871450452.png)

![image-20210225220900408](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230116924-573728067.png)

![image-20210225221035879](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230116671-649393910.png)

移动端的例子

![image-20210225221153518](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230116342-1387194445.png)

安卓端一个Activity就是一个页面，在TestActivity页面当点击返回的时候，TestActivity就可以被回收了，但是由于新的线程持有了TestActivity类的静态变量的key这个锁，而这个新的线程一直继续，所以就会导致TestActivity无法被回收。

对dump文件分析

![image-20210225221619381](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230115982-1207594897.png)

![image-20210225221656776](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230115532-415404972.png)

![image-20210225221722415](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230115142-1452763055.png)

---

## 3.6、补充2：支持使用OQL语言查询对象信息（了解）

![image-20210225225318031](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230114854-549687207.png)

![image-20210225225328370](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230114578-1917193552.png)

![image-20210225225421374](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230114304-1408066775.png)

![image-20210225225429181](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230114049-796951795.png)

按F1

![image-20210225225524507](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230113730-524027045.png)

![image-20210225225624251](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230113297-1374162823.png)

语法

![image-20210225225641129](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230113009-1418937826.png)

这就是关于MAT的使用

![image-20210225225653774](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230112614-224797014.png)

![image-20210225225709952](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230112375-2009340867.png)

![image-20210225225739158](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230112102-630426695.png)

文档

![image-20210225225809973](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230111701-566147233.png)

![image-20210225225751842](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230111218-160530095.png)

英文的重要。

![image-20210225225933789](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230110797-678337691.png)

![image-20210225230002220](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230110435-453616330.png)

写好OQL，F5就能查询

![image-20210225230102629](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230110080-101179025.png)

![image-20210225230203755](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230109693-803766412.png)

结果集以对象的方式呈现

![image-20210226072116694](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230109331-1436126581.png)

从Student中得到保留集

![image-20210226072146793](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230108930-1317717951.png)

![image-20210226072226874](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230108566-1483114303.png)

使用地址查找是唯一的，如果类名，可能有多个

![image-20210226072245655](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230108197-1645901465.png)

![image-20210226072341818](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230107796-690456635.png)

长度大于10的数组

![image-20210226072452883](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230107326-1584687285.png)

![image-20210226072502309](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230106903-582931545.png)

![image-20210226072513058](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230106466-1820961877.png)

![image-20210226072539925](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230106084-457357057.png)

![image-20210226072554885](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230105671-1034308580.png)

![image-20210226072603203](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230105187-1623486130.png)

---

## 3.7、JProfiler

### 概览图

![image-20210226072732373](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230104865-577763466.png)

### 基本概述

#### 介绍

![image-20210226072657876](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230104541-1595127957.png)

![image-20210226072903100](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230104107-2034714996.png)

#### 特点

![image-20210226072913613](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230103686-507454093.png)

界面

![image-20210226072959741](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230103354-997501641.png)

#### 主要功能

![image-20220429101047986](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230103069-1887522374.png)

### 安装与配置

#### 下载与安装

![image-20210226073352435](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230102761-520322398.png)

![image-20210226073529773](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230102230-1690165243.png)

#### JProfiler中配置IDEA

1、IDE Integrations

![image-20220429101302529](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230101819-2745287.png)

2、选择合适的IDE版本

![image-20220429101316978](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230101576-444032303.png)

3、开始集成

![image-20220429101329244](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230101325-272178252.png)

4、正式集成

![image-20220429101342571](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230101068-219478501.png)

5、集成成功

![image-20220429101401171](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230100831-1272280270.png)

6、点击OK即可

#### IDEA集成JProfiler

![image-20220429101633979](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230100571-735073162.png)

离线

![image-20210226074138994](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230100258-392801193.png)

![image-20210226074154194](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230100005-1306686152.png)

![image-20210226074215228](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230059649-1685203615.png)

配置

![image-20210226074235535](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230059352-1437704505.png)

点击左边的就会运行

![image-20210226074256875](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230059017-1552130759.png)

选中要运行的程序，然后运行JProfiler

![image-20210226074443267](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230058665-798302396.png)

OK就行

![image-20220429101918923](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230058269-1604216304.png)

![image-20220429101939099](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230057962-1120101339.png)

![image-20210226074517324](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230057602-40685184.png)

手动启动OOMTest

![image-20210226074649823](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230057272-287036691.png)

打开独立的JProfiler

![image-20210226074710853](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230056911-643945911.png)

![image-20210226074852332](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230056621-1804890137.png)

点击分析已经保存，就可以选择之前保存的

![image-20210226074807364](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230056279-2019424997.png)

也可以

![image-20210226075046011](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230055991-1384733184.png)

选择jvm进程就会进入

![image-20210226075115680](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230055558-2108365853.png)

![image-20210226075220570](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230055173-1605423754.png)

### 具体使用

#### 数据采集方式

![image-20220429102846644](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230054880-1433656491.png)

![image-20210226075327521](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230054539-793201291.png)

![image-20210226075257409](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230054063-1082829519.png)

内存泄露分析用Sampling足够了，正在运行的Java程序，推荐用Sampling模式。

![image-20210226075657498](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230053705-960100211.png)

![image-20210226075712421](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230053216-1939441041.png)

![image-20210226075900057](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230052814-600194600.png)

整体视图

![image-20210226075928250](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230052264-1496880578.png)

绿色是空闲的，蓝色是占用的

![image-20210226075953410](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230051858-1942775190.png)

可以GC

![image-20210226080020511](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230051477-482231144.png)

内存区域

![image-20210226080042273](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230050933-233232620.png)

对象、数组内存活动的表，这里没有数据 点击一下

![image-20220429103545074](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230050606-135163248.png)

![image-20220429103623990](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230050304-290966669.png)

![image-20220429103729856](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230050017-819525708.png)

类的个数，绿色、蓝色看示意图

![image-20220429103803932](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230049721-661937294.png)

![image-20220429103840133](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230049457-1549042968.png)

![image-20220429103900010](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230049167-1880540776.png)

![image-20220429104353810](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230048853-1787638051.png)

![image-20220429104443289](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230048594-1368370505.png)

源代码

![image-20210226080543459](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230048334-1753616864.png)

#### 遥感监测

![image-20220429105431543](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230048044-712700069.png)

其中Telemetries就是遥感监测的意思

#### 内存视图 Live Memory

![image-20210226081244121](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230047631-483106116.png)

这功能主要是分析这三个问题

![image-20220429105751294](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230047265-1772096487.png)

这俩都比较大，就可能是第一个问题；个数不多，但Size大，就可能是第二个问题；

![image-20220429112049231](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230046984-1970202131.png)

![image-20220429104701275](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230046678-393328052.png)

![image-20210226080835374](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230046369-486957931.png)

直方图（Size是浅堆）

![image-20220429104832873](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230046033-1689308267.png)

可以标记当前时间点，然对比一下数量是多了还是少了

![image-20220429104959482](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230045738-1259177614.png)

![image-20220429105039723](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230045466-794084121.png)

包的形式

![image-20220429105111899](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230045093-902706796.png)

更新可以手动点击更新

![image-20220429105242272](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230044733-1449536824.png)

手动启动（因为这个启动会导致jvm性能降低）（分析内存泄露可以开启）

![image-20220429111937887](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230044470-180481923.png)

![image-20220429112144565](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230044166-1676941824.png)

一般GC后，内存变化是折线图，如果把折线图的最低点连接起来像增长的线性图，就可能是内存泄露了，因为线性图说明GC没有回收到什么垃圾。这是从Memory观察到的，如果怀疑是内存泄露就可以开启Recorded Objects

![image-20220429155149934](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230043907-1351763221.png)

![image-20220429112438201](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230043609-1924230723.png)

![image-20220429112549181](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230043339-1690182246.png)

![image-20220429112629296](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230043075-181880620.png)

活着的对象、垃圾收集的对象、两者都有的![image-20220429112726575](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230042841-522390948.png)

这都是存活的

![image-20220429112811228](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230042556-2033697849.png)

可以进行垃圾收集的对象

![image-20220429112848541](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230042292-693748041.png)

没有对象被回收

![image-20210226082405366](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230041807-145811556.png)

GC

![image-20210226082443501](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230041434-442980820.png)

再看一下

![image-20210226082513916](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230040862-47315863.png)

就有数据了，Picture是没有进行过回收的

![image-20210226082524841](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230040440-1342199737.png)

这时候就可以定位到Picture没有被回收，就可以怀疑是Picture内存泄露

追踪

![image-20220429160038624](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230040072-2036450666.png)

#### 堆遍历 heap walker

如果通过内存视图 Live Memory已经分析出哪个类的对象不能进行垃圾回收，并且有可能导致内存溢出，如果想进一步分析，我们可以在该对象上点击右键，选择Show Selection In Heap Walker，如下图：

![image-20220429164326702](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230039809-1976179051.png)

![image-20220429155944479](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230039553-204101279.png)

![image-20210226082726286](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230039218-617510113.png)

![image-20220429160221511](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230038895-1779982551.png)

![image-20210226213144735](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230038607-1880382682.png)

进行溯源，操作如下：

![image-20220429164434077](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230038097-2082605364.png)

![image-20220429160334672](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230037818-1449083519.png)

![image-20220429160404987](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230037480-71008998.png)

![image-20220429160442282](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230037205-1376687079.png)

![image-20220429160504287](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230036851-1395140684.png)

最后定位到是main方法引用了（这里比较简单）

![image-20220429160637685](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230036571-747972563.png)

还可以通过图表查看

![image-20220429160731787](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230036067-531595213.png)

![image-20220429160852354](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230035807-1554789182.png)

![image-20220429160929210](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230035447-952531963.png)

![image-20220429161016126](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230035142-869414176.png)

![image-20220429161041499](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230034720-1858263163.png)

GC无法回收Picture，这就可以看到是谁引用了，到底是不是内存泄露问题就看下代码就行。

![image-20220429161203469](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230034433-1432960320.png)

关掉后会提示是否保存session，点击Processd![image-20220429161245314](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230034085-387532516.png)

![image-20210226214122684](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230033820-467846362.png)

![image-20210226214148665](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230033511-1456102606.png)

重新打开

![image-20210226214214457](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230033105-235143713.png)

选择就行

![image-20220429161632645](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230032777-1264224551.png)

重新打开 OOMTest

![image-20220429163318011](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230032533-2139617721.png)

可以生成堆转储快照（点击图标）

![image-20220429163433037](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230032276-69123298.png)

![image-20220429163658289](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230032028-1710890733.png)

这就是离线版堆存储dump文件

![image-20220429163824682](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230031757-363528916.png)

生成heap当前的快照

![image-20220429163910713](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230031508-1513947256.png)

![image-20210226214800953](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230031225-2010731393.png)

![image-20210226214817773](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230030962-1045192562.png)

![image-20220429164013157](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230030630-2077517812.png)

这就是用JProfiler导出快照。

#### cpu视图 cpu views

![image-20220429171814804](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230030298-2055235587.png)

CPU默认不会追踪，需要追踪就手动点开，因为追踪会影响性能

![image-20210226214959548](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230029918-1501917433.png)

用官方例子

![image-20220429172236068](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230029642-2077135155.png)

![image-20220429172406581](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230029355-677876634.png)

点击开启

![image-20220429172458981](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230029071-883081286.png)

具体使用：

1、记录方法统计信息

![image-20220429171830971](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230028802-1375107744.png)

![image-20220429171837638](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230028548-631285780.png)

2、方法统计

![image-20220429172027768](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230028249-1925224487.png)

3、具体分析

![image-20220429172043964](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230027945-1903481483.png)

因为一个方法的执行时间越长，那就是说对CPU的占用就越多，JProfiler就通过方法的执行时间来刻画对CPU的使用情况。

各个线程

![image-20220429173441027](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230027655-178855368.png)

线程状态

![image-20220429173523931](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230027379-566528360.png)

注：对CPU的监控比内存的少。

#### 线程视图 threads

![image-20220429173911971](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230027069-934109445.png)

![image-20220429174300082](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230026730-1971098077.png)

具体使用：
1、查看线程运行情况

![image-20220429173929621](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230026452-182454853.png)

活的死的线程都有

![image-20220429174426216](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230026129-1233649008.png)

2、新建线程dump文件

![image-20220429175018071](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230025839-1804128285.png)

运行死锁的程序

![image-20210226221405795](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230025537-1155431084.png)

![image-20220429175538013](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230025198-1602438043.png)

![image-20220429175738785](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230024948-156735588.png)

![image-20220429175835016](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230024720-1057852767.png)

#### 监视器&锁 Monitors&locks

![image-20210226222422094](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230024406-779774205.png)

### 案例分析

#### 案例1

```java
package com.atguigu.jprofiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * 功能演示测试
 * @author shkstart
 * @create 12:19
 */
public class JProfilerTest {
    public static void main(String[] args) {
        while (true){
            ArrayList list = new ArrayList();
            for (int i = 0; i < 500; i++) {
                Data data = new Data();
                list.add(data);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
class Data{
    private int size = 10;
    private byte[] buffer = new byte[1024 * 1024];//1mb
    private String info = "hello,atguigu";
}
```

点击执行

![image-20220429184539872](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230024034-523725116.png)

蓝色部分曲折，说明有进行GC

![image-20220429184720089](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230023744-1334956835.png)

![image-20220429184753279](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230023466-1511137827.png)

每循环一次，list就释放了

![image-20210227135123652](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230023127-178753110.png)

正常的程序就是这样子的内存有增加，又有GC回收，这就比较良性的，如果GC后一直有趋于线性增加就可能会内存泄露了。

![image-20220429185423998](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230022834-356001781.png)

![image-20220429184929331](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230022519-619135633.png)

正常的程序就是这样子的内存有增加，又有GC回收，这就比较良性的，如果GC后一直有趋于线性增加就可能会内存泄露了。

![image-20220429185525859](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230022257-574007545.png)

#### 案例2

```java
public class MemoryLeak {
 
    public static void main(String[] args) {
        while (true) {
            ArrayList beanList = new ArrayList();
            for (int i = 0; i < 500; i++) {
                Bean data = new Bean();
                data.list.add(new byte[1024 * 10]);//10kb
                beanList.add(data);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
 
}
 
class Bean {
    int size = 10;
    String info = "hello,atguigu";
    //     ArrayList list = new ArrayList();
    static ArrayList list = new ArrayList();
}
```

我们通过JProfiler来看一下，如下：

![image-20220429185944580](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230022019-1408354786.png)

你可以看到内存一个劲的往上涨，但是就是没有下降的趋势，说明这肯定有问题，过不了多久就会出现OOM，我们来到Live memory中，先标记看一下到底是哪些对象在进行内存增长，等一下看看会不会触发垃圾回收，如果不触发的话，我们自己来触发垃圾回收，之后观察哪些对象没有被回收掉，如下

![image-20220429190015602](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230021743-509279752.png)

我上面点击了Mark Current，发现有些对象在持续增长，然后点击了一下Run GC，结果如下所示：

![image-20220429190051966](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230021475-775378206.png)

可以看出byte[]没有被回收，说明它是有问题的，我们点击Show Selection In Heap Walker，如下：

![image-20220429190117136](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230021198-1249458170.png)

然后看一下该对象被谁引用，如下：

![image-20220429190140343](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230020890-652097883.png)

结果如下：

![image-20220429190152796](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230020506-1882485806.png)

可以看出byte[]来自于Bean类是的list中，并且这个list是ArrayList类型的静态集合，所以找到了：static ArrayList list = new ArrayList();
发现list是静态的，这不妥，因为我们的目的是while结束之后Bean对象被回收，并且Bena对象中的所有字段都被回收，但是list是静态的，那就是类的，众所周知，类变量随类而生，随类而灭，因此每次我们往list中添加值，都是往同一个list中添加值，这会造成list不断增大，并且不能回收，所以最终会导致OOM

![image-20210227141514271](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501230020067-622110649.png)

---

## 3.8、Arthas

### 基本概述

#### 背景

![image-20220430101355598](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231916459-573197408.png)

![image-20220430101403391](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231916062-680244364.png)

![image-20220430101533259](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231915728-1358545873.png)

#### 概述

![image-20220430101554737](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231915298-1921399876.png)

#### 基于哪些工具开发而来

![image-20220430101619062](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231914928-43093833.png)

#### 官方使用文档

https://arthas.aliyun.com/doc/quick-start.html

### 安装与使用

#### 安装

![image-20220430101707769](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231914526-1828274996.png)

![image-20220430101713043](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231914122-485749736.png)

![image-20220430101718741](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231913742-1146278340.png)

#### 工程目录

![image-20220430101744581](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231913422-526482725.png)

#### 启动

![image-20220430101804323](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231912986-2034030806.png)

#### 查看日志

cat ~/logs/arthas/arthas.log

#### 查看帮助

java -jar arthas-boot.jar -h

#### web console

![image-20220430101929483](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231912699-596742228.png)

#### 退出

![image-20220430101947154](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231912336-923240440.png)

### 相关诊断指令

#### 基础指令

![image-20220430102028020](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231912031-496245529.png)

#### jvm相关

![image-20220430102040403](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231911649-166469137.png)

**dashboard**

链接：https://arthas.aliyun.com/doc/dashboard.html
作用：当前系统的实时数据面板

**thread**

链接：https://arthas.aliyun.com/doc/thread.html
作用：查看当前线程信息，查看线程的堆栈

**JVM**

![image-20220430102204740](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231911221-579379743.png)

**其他**

![image-20220430102225219](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231910837-823431693.png)

#### class/classloader相关

![image-20220430102334313](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231910491-725301178.png)

**sc**

![image-20220430102351305](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231910096-464816274.png)

**sm**

![image-20220430102408458](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231909713-1360213789.png)

**jad**

![image-20220430102426578](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231909330-2052713322.png)

![image-20220430102430604](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231908950-1574897859.png)

**mc、redefine**

![image-20220430102449699](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231908509-152733857.png)

**classloader**

![image-20220430102502363](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231908175-94539737.png)

#### monitor/watch/trace相关

**monitor**

![image-20220430102532963](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231907757-2125330692.png)

**watch**

![image-20220430102547830](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231907359-1042142147.png)

![image-20220430102551878](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231907014-176242118.png)

**stack**

![image-20220430102633375](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231906711-632042987.png)

**trace**

**tt**

#### 其他

![image-20220430102727157](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231906303-1493179314.png)

**profiler/火焰图**

profiler：https://arthas.aliyun.com/doc/profiler.html

**options**

options：https://arthas.aliyun.com/doc/options.html

---

## 3.9、Java Mission Control

### 概览图

![image-20210228150940020](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231906006-859247598.png)

### 历史

![image-20210228151400294](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231905562-1075348924.png)

### 概述

![image-20210228151726473](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231905129-1471504239.png)

### 启动

![image-20220430121159368](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231904779-1995537093.png)

![image-20220430121141185](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231904411-892265946.png)

![image-20220430121309546](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231904128-471216912.png)

![image-20220430121613745](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231903912-1693425087.png)

![image-20220430121853500](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231903607-774289889.png)

### 功能：实时监控JVM运行时的状态

![image-20220430121942623](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231903246-495210518.png)

![image-20220430122017073](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231902866-1971810834.png)

添加要监控的

![image-20210228152008188](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231902500-1871372762.png)

![image-20210228152023068](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231902073-2133381142.png)

![image-20210228152038343](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231901634-2118679932.png)

![image-20210228152123154](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231901103-1779242421.png)

![image-20210228152201970](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231900671-1907668536.png)

CPU等过高，可以触发报警

![image-20210228152239139](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231900227-1285551890.png)

### Java Flight Recorder

#### 概述

![image-20220430122753712](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231859873-820398798.png)

#### 事件类型

![image-20220430122416050](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231859490-1390938898.png)

#### 启动方式

##### 方式1（了解）：使用-XX:StartFlightRecording=参数

![image-20220430124820470](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231859091-695652476.png)

##### 方式2（了解）：使用jcmd的JFR.*子命令

![image-20220430124901807](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231858690-1904107891.png)

##### 方式3：通过JMC的JFR插件

添加参数：

```shell
-XX:+UnlockCommercialFeatures
-XX:+FlightRecorder
```

![image-20220430124557786](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231858444-1294197818.png)

![image-20220430124240401](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231858183-1605894314.png)

![image-20210228152605506](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231857903-1510192346.png)

![image-20210228152649608](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231857513-716983024.png)

![image-20210228152722563](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231857171-1835142372.png)

![image-20220430124048084](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231856686-659568694.png)

![image-20220430125447994](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231856373-1517194573.png)

#### Java Flight Recorder 取样分析

![image-20220430125245875](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231856034-1254055243.png)

![image-20220430125708926](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231855708-676826616.png)

![image-20220430125806949](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231855317-859282055.png)

![image-20220430125841775](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231854984-1422887909.png)

哪些方法调用的多

![image-20220430130005514](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231854672-824734556.png)

---

## 3.10、其他工具

![image-20220430130152643](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231854439-2080146332.png)

#### Flame Graphs（火焰图）

![image-20220430130414315](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231854063-540161354.png)

![image-20220430130524853](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231853760-1984444663.png)

#### Tprofiler

![image-20210228154328655](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231853235-1189764471.png)

![image-20220430130622600](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231852883-1847617597.png)

#### Btrace

![image-20210228154531385](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231852445-52711375.png)

#### YourKit

#### JProbe

#### Spring Insight

# 4、JVM运行时参数

![image-20220430131003677](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231852125-91009470.png)

---

## 01-JVM参数选项

![image-20210228154816396](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231851678-1386050363.png)

后两个是非标准的

![image-20210228155053783](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231851371-669301419.png)

### 类型一：标准参数选项

![image-20220430180458205](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231851097-535215111.png)

#### 特点

`以-开头`，比较稳定，后续版本基本不会变化

#### 各种选项

直接在DOS窗口中运行java或者java -help可以看到所有的标准选项

```shell
C:\Users\Administrator>java
用法: java [-options] class [args...]
           (执行类)
   或  java [-options] -jar jarfile [args...]
           (执行 jar 文件)
其中选项包括:
    -d32          使用 32 位数据模型 (如果可用)
    -d64          使用 64 位数据模型 (如果可用)
    -server       选择 "server" VM
                  默认 VM 是 server.
 
    -cp <目录和 zip/jar 文件的类搜索路径>
    -classpath <目录和 zip/jar 文件的类搜索路径>
                  用 ; 分隔的目录, JAR 档案
                  和 ZIP 档案列表, 用于搜索类文件。
    -D<名称>=<值>
                  设置系统属性
    -verbose:[class|gc|jni]
                  启用详细输出
    -version      输出产品版本并退出
    -version:<值>
                  警告: 此功能已过时, 将在
                  未来发行版中删除。
                  需要指定的版本才能运行
    -showversion  输出产品版本并继续
    -jre-restrict-search | -no-jre-restrict-search
                  警告: 此功能已过时, 将在
                  未来发行版中删除。
                  在版本搜索中包括/排除用户专用 JRE
    -? -help      输出此帮助消息
    -X            输出非标准选项的帮助
    -ea[:<packagename>...|:<classname>]
    -enableassertions[:<packagename>...|:<classname>]
                  按指定的粒度启用断言
    -da[:<packagename>...|:<classname>]
    -disableassertions[:<packagename>...|:<classname>]
                  禁用具有指定粒度的断言
    -esa | -enablesystemassertions
                  启用系统断言
    -dsa | -disablesystemassertions
                  禁用系统断言
    -agentlib:<libname>[=<选项>]
                  加载本机代理库 <libname>, 例如 -agentlib:hprof
                  另请参阅 -agentlib:jdwp=help 和 -agentlib:hprof=help
    -agentpath:<pathname>[=<选项>]
                  按完整路径名加载本机代理库
    -javaagent:<jarpath>[=<选项>]
                  加载 Java 编程语言代理, 请参阅 java.lang.instrument
    -splash:<imagepath>
                  使用指定的图像显示启动屏幕
```

#### 补充内容：-server与-client

![image-20210228155401625](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231850778-1197842439.png)

对于以上第2点，我们可以打开DOS窗口，输入java -version就可以看到64位机器上用的server模式，如下所示：

![image-20220430180823891](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231850484-1913683836.png)

### 类型二：-X参数选项

#### 特点

![image-20220430181125040](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231850279-1546595098.png)

#### 各种选项

直接在DOS窗口中运行java -X命令可以看到所有的X选项

```shell
C:\Users\Administrator>java -X
    -Xmixed           混合模式执行 (默认)
    -Xint             仅解释模式执行
    -Xbootclasspath:<用 ; 分隔的目录和 zip/jar 文件>
                      设置搜索路径以引导类和资源
    -Xbootclasspath/a:<用 ; 分隔的目录和 zip/jar 文件>
                      附加在引导类路径末尾
    -Xbootclasspath/p:<用 ; 分隔的目录和 zip/jar 文件>
                      置于引导类路径之前
    -Xdiag            显示附加诊断消息
    -Xnoclassgc       禁用类垃圾收集
    -Xincgc           启用增量垃圾收集
    -Xloggc:<file>    将 GC 状态记录在文件中 (带时间戳)
    -Xbatch           禁用后台编译
    -Xms<size>        设置初始 Java 堆大小
    -Xmx<size>        设置最大 Java 堆大小
    -Xss<size>        设置 Java 线程堆栈大小
    -Xprof            输出 cpu 配置文件数据
    -Xfuture          启用最严格的检查, 预期将来的默认值
    -Xrs              减少 Java/VM 对操作系统信号的使用 (请参阅文档)
    -Xcheck:jni       对 JNI 函数执行其他检查
    -Xshare:off       不尝试使用共享类数据
    -Xshare:auto      在可能的情况下使用共享类数据 (默认)
    -Xshare:on        要求使用共享类数据, 否则将失败。
    -XshowSettings    显示所有设置并继续
    -XshowSettings:all
                      显示所有设置并继续
    -XshowSettings:vm 显示所有与 vm 相关的设置并继续
    -XshowSettings:properties
                      显示所有属性设置并继续
    -XshowSettings:locale
                      显示所有与区域设置相关的设置并继续
 
-X 选项是非标准选项, 如有更改, 恕不另行通知。
```

![image-20220430181349568](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231849900-1953795325.png)

#### JVM的JIT编译模式相关的选项

![image-20220430181625205](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231849540-507854197.png)

JDK默认使用-Xmixed模式，证明如下：

![image-20220430181719944](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231849277-1231482147.png)

特别地，-Xmx -Xms -Xss属于XX参数？

![image-20220430181815009](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231849033-688890556.png)

### 类型三：-XX参数选项

#### 特点

![image-20220430182233672](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231848758-385660788.png)

#### 作用:用于开发和调试JVM

#### 分类

##### Boolean类型格式

![image-20220430182551540](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231848357-797117550.png)

##### 非Boolean类型格式（key-value类型）

![image-20220430182816577](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231848024-201078477.png)

特别地

![image-20220430182937338](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231847716-578780676.png)

---

## 02-添加JVM参数选项

![image-20210228161224427](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231847432-471317536.png)

### Eclipse

1、在空白处单击右键，选择Run As，在选择Run Configurations……

![image-20220430184113367](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231847022-1061377161.png)

2、设置虚拟机参数

![image-20220430184145173](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231846656-622555493.png)

### IDEA

1、Edit Configurations…

![image-20220430184246215](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231846308-678119582.png)

2、设置虚拟机参数

![image-20220430184257938](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231846028-630651672.png)

### 运行jar包

这是在java -jar demo.jar中的java -jar之间添加了虚拟机配置信息

```shell
java -Xms50m -Xmx50m -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -jar demo.jar
```

### 通过Tomcat运行war包

Linux系统下可以在tomcat/bin/catalina.sh中添加类似如下配置：

```shell
JAVA_OPTS="-Xms512M -Xmx1024M"
```

Windows系统下载catalina.bat中添加类似如下配置：

```shell
set "JAVA_OPTS=-Xms512M -Xmx1024M"
```

### 程序运行过程中

![image-20220430184628063](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231845583-340297535.png)

使用`jinfo -flag <name>=<value> <pid>`设置非Boolean类型参数

使用`jinfo -flag [+|-]<name> <pid>`设置Boolean类型参数

---

## 03-常用的JVM参数选项

![image-20220430185017985](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231845170-263312160.png)

### 打印设置的XX选项及值

![image-20220430185329194](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231844714-1194375777.png)

### 堆、栈、方法区等内存大小设置

![image-20210228161911086](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231844381-1755930666.png)

#### 栈

-Xss128k，等价于-XX:ThreadStackSize，设置每个线程的栈大小为128k

#### 堆内存

`-XX:SurvivorRatio=8`：设置年轻代中Eden区与一个Survivor区的比值，默认为8

只有显示使用Eden区和Survivor区的比例，才会让比例生效，否则比例都会自动设置，至于其中的原因，请看下面的-XX:+UseAdaptiveSizePolicy中的解释，最后推荐使用默认打开的-XX:+UseAdaptiveSizePolicy设置，并且不显示设置-XX:SurvivorRatio

`-XX:+UseAdaptiveSizePolicy`：自动选择各区大小比例，默认开启

![image-20220430185929801](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231844028-746547802.png)

`-XX:NewRatio=2`：设置老年代与年轻代（包括1个Eden区和2个Survivor区）的比值，默认为2

根据实际情况进行设置，主要根据对象生命周期来进行分配，如果对象生命周期很长，那么让老年代大一点，否则让新生代大一点

#### 方法区

![image-20220430190151046](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231843770-1938359747.png)

#### 直接内存

`-XX:MaxDirectMemorySize`：指定DirectMemory容量，若未指定，则默认与Java堆最大值一样

### OutOfMemory相关的选项

![image-20220430190807199](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231843339-86690775.png)

### 垃圾收集器相关选项

![image-20210228161931796](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231843019-315601403.png)

![image-20220430191002103](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231842686-449143442.png)

![image-20220430191009482](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231842389-1863075068.png)

#### 查看默认的垃圾回收器

![image-20220430191107895](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231842116-1830436290.png)

以上两种方式都可以查看默认使用的垃圾回收器，第一种方式更加准备，但是需要程序的支持；第二种方式需要去尝试，如果使用了，返回的值中有+号，否则就是-号

![image-20210228170007694](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231841813-462742077.png)

运行结果

![image-20210228170021099](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231841475-869418845.png)

#### Serial回收器

![image-20210228170102390](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231841187-78067115.png)

#### Parnew回收器

![image-20220430191300696](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231840898-443772213.png)

![image-20220430191312193](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231840666-598384296.png)

根据下图可知，该回收器最终将会没有搭档，那就相当于被遗弃了

![image-20220430191354652](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231840341-2108134400.png)

#### Parallel回收器

![image-20220430191442840](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231839942-183810156.png)

注意：
`Parallel回收器主打吞吐量，而CMS和G1主打低延迟`，如果主打吞吐量，那么就不应该限制最大停顿时间，所以-XX:MaxGCPauseMills不应该设置
-XX:MaxGCPauseMills中的调整堆大小通过默认开启的-XX:+UseAdaptiveSizePolicy来实现
-XX:GCTimeRatio用来衡量吞吐量，并且和-XX:MaxGCPauseMills矛盾，因此不会同时使用

#### CMS回收器

![image-20210228170544531](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231839360-372528521.png)

![image-20210228170908355](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231838830-1814532995.png)

![image-20210228170931525](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231838362-1627865064.png)

#### G1回收器

![image-20210228171045020](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231837949-1837761798.png)

如果使用G1垃圾收集器，不建议设置-Xmn和-XX:NewRatio，毕竟可能影响G1的自动调节

![image-20210228171127456](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231837371-1690144612.png)

#### 怎么选择垃圾收集器

![image-20210228171210965](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231836810-104641211.png)

### GC日志相关选项

![image-20210228161943328](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231836393-1836428992.png)

#### 常用参数

```java
package com.atguigu.java;
 
import java.util.ArrayList;
 
/**
 * -Xms60m -Xmx60m -XX:SurvivorRatio=8
 * @author shkstart
 * @create 14:27
 */
public class GCLogTest {
    public static void main(String[] args) {
        ArrayList<byte[]> list = new ArrayList<>();
 
        for (int i = 0; i < 500; i++) {
            byte[] arr = new byte[1024 * 100];//100KB
            list.add(arr);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```

`-verbose:gc`：输出日志信息，默认输出的标准输出；可以独立使用

![image-20220430232441142](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231836067-1089803718.png)

`-XX:+PrintGC`：等同于-verbose:gc，表示打开简化的日志；可以独立使用

![image-20220430232543818](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231835688-1169103385.png)

`-XX:+PrintGCDetails`：在发生垃圾回收时打印内存回收详细的日志，并在进程退出时输出当前内存各区域的分配情况；可以独立使用

![image-20220430232706436](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231835331-1944722235.png)

`-XX:+PrintGCTimeStamps`：程序启动到GC发生的时间秒数；不可以独立使用，需要配合-XX:+PrintGCDetails使用

![image-20220430232805180](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231834658-1068891876.png)

`-XX:+PrintGCDateStamps`：输出GC发生时的时间戳（以日期的形式，例如：2013-05-04T21:53:59.234+0800）；不可以独立使用，可以配合-XX:+PrintGCDetails使用

![image-20220430232854295](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231834199-1200630014.png)

`-XX:+PrintHeapAtGC`：每一次GC前和GC后，都打印堆信息；可以独立使用

![image-20220430232945025](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231833512-484579266.png)

`-XIoggc:<file>`：把GC日志写入到一个文件中去，而不是打印到标准输出中

![image-20220430233022382](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231833089-263994286.png)

GC参数对应的GC日志输出

```shell
###############-verbose:gc 或 -XX:+PrintGC ####################
[GC (Allocation Failure)  16303K->14194K(59392K), 0.0040576 secs]
[GC (Allocation Failure)  30519K->30520K(59392K), 0.0037994 secs]
[Full GC (Ergonomics)  30520K->30298K(59392K), 0.0160108 secs]
[Full GC (Ergonomics)  46642K->46301K(59392K), 0.0061445 secs]

########################## -XX:+PrintGCDetails ###########################
[GC (Allocation Failure) [PSYoungGen: 16303K->2016K(18432K)] 16303K->14198K(59392K), 0.0040885 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC (Allocation Failure) [PSYoungGen: 18341K->2024K(18432K)] 30523K->30524K(59392K), 0.0042607 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[Full GC (Ergonomics) [PSYoungGen: 2024K->0K(18432K)] [ParOldGen: 28500K->30298K(40960K)] 30524K->30298K(59392K), [Metaspace: 3993K->3993K(1056768K)], 0.0354285 secs] [Times: user=0.08 sys=0.00, real=0.04 secs] 
[Full GC (Ergonomics) [PSYoungGen: 16344K->5500K(18432K)] [ParOldGen: 30298K->40800K(40960K)] 46642K->46301K(59392K), [Metaspace: 3993K->3993K(1056768K)], 0.0064641 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
 
Heap
 PSYoungGen      total 18432K, used 10379K [0x00000000fec00000, 0x0000000100000000, 0x0000000100000000)
  eden space 16384K, 63% used [0x00000000fec00000,0x00000000ff622d10,0x00000000ffc00000)
  from space 2048K, 0% used [0x00000000ffe00000,0x00000000ffe00000,0x0000000100000000)
  to   space 2048K, 0% used [0x00000000ffc00000,0x00000000ffc00000,0x00000000ffe00000)
 ParOldGen       total 40960K, used 40800K [0x00000000fc400000, 0x00000000fec00000, 0x00000000fec00000)
  object space 40960K, 99% used [0x00000000fc400000,0x00000000febd82d8,0x00000000fec00000)
 Metaspace       used 4000K, capacity 4568K, committed 4864K, reserved 1056768K
  class space    used 447K, capacity 460K, committed 512K, reserved 1048576K

 
##########################-XX:+PrintGCTimeStamps -XX:+PrintGCDetails#########################################
 
4.364: [GC (Allocation Failure) [PSYoungGen: 16303K->2040K(18432K)] 16303K->14270K(59392K), 0.0033008 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
9.614: [GC (Allocation Failure) [PSYoungGen: 18365K->1948K(18432K)] 30595K->30588K(59392K), 0.0043835 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
9.619: [Full GC (Ergonomics) [PSYoungGen: 1948K->0K(18432K)] [ParOldGen: 28640K->30298K(40960K)] 30588K->30298K(59392K), [Metaspace: 3996K->3996K(1056768K)], 0.0111210 secs] [Times: user=0.16 sys=0.00, real=0.02 secs] 
14.934: [Full GC (Ergonomics) [PSYoungGen: 16344K->5500K(18432K)] [ParOldGen: 30298K->40800K(40960K)] 46642K->46301K(59392K), [Metaspace: 3996K->3996K(1056768K)], 0.0117331 secs] [Times: user=0.16 sys=0.00, real=0.01 secs] 
Heap
 PSYoungGen      total 18432K, used 10379K [0x00000000fec00000, 0x0000000100000000, 0x0000000100000000)
  eden space 16384K, 63% used [0x00000000fec00000,0x00000000ff622d10,0x00000000ffc00000)
  from space 2048K, 0% used [0x00000000ffe00000,0x00000000ffe00000,0x0000000100000000)
  to   space 2048K, 0% used [0x00000000ffc00000,0x00000000ffc00000,0x00000000ffe00000)
 ParOldGen       total 40960K, used 40800K [0x00000000fc400000, 0x00000000fec00000, 0x00000000fec00000)
  object space 40960K, 99% used [0x00000000fc400000,0x00000000febd8350,0x00000000fec00000)
 Metaspace       used 4003K, capacity 4568K, committed 4864K, reserved 1056768K
  class space    used 447K, capacity 460K, committed 512K, reserved 1048576K

 
 
###########################-XX:+PrintGCDateStamps -XX:+PrintGCDetails#################################
2021-01-30T15:01:12.596+0800: [GC (Allocation Failure) [PSYoungGen: 16303K->2020K(18432K)] 16303K->14154K(59392K), 0.0051686 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
2021-01-30T15:01:17.856+0800: [GC (Allocation Failure) [PSYoungGen: 18345K->2036K(18432K)] 30479K->30480K(59392K), 0.0037044 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2021-01-30T15:01:17.856+0800: [Full GC (Ergonomics) [PSYoungGen: 2036K->0K(18432K)] [ParOldGen: 28444K->30303K(40960K)] 30480K->30303K(59392K), [Metaspace: 3993K->3993K(1056768K)], 0.0161352 secs] [Times: user=0.02 sys=0.00, real=0.02 secs] 
2021-01-30T15:01:23.406+0800: [Full GC (Ergonomics) [PSYoungGen: 16316K->6000K(18432K)] [ParOldGen: 30303K->40600K(40960K)] 46619K->46601K(59392K), [Metaspace: 3993K->3993K(1056768K)], 0.0113074 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
Heap
 PSYoungGen      total 18432K, used 10909K [0x00000000fec00000, 0x0000000100000000, 0x0000000100000000)
  eden space 16384K, 66% used [0x00000000fec00000,0x00000000ff6a7530,0x00000000ffc00000)
  from space 2048K, 0% used [0x00000000ffe00000,0x00000000ffe00000,0x0000000100000000)
  to   space 2048K, 0% used [0x00000000ffc00000,0x00000000ffc00000,0x00000000ffe00000)
 ParOldGen       total 40960K, used 40600K [0x00000000fc400000, 0x00000000fec00000, 0x00000000fec00000)
  object space 40960K, 99% used [0x00000000fc400000,0x00000000feba63c0,0x00000000fec00000)
 Metaspace       used 4000K, capacity 4568K, committed 4864K, reserved 1056768K
  class space    used 447K, capacity 460K, committed 512K, reserved 1048576K

 
 
############################# -XX:+PrintHeapAtGC ###################################
{Heap before GC invocations=1 (full 0):
 PSYoungGen      total 18432K, used 16303K [0x00000000fec00000, 0x0000000100000000, 0x0000000100000000)
  eden space 16384K, 99% used [0x00000000fec00000,0x00000000ffbebca0,0x00000000ffc00000)
  from space 2048K, 0% used [0x00000000ffe00000,0x00000000ffe00000,0x0000000100000000)
  to   space 2048K, 0% used [0x00000000ffc00000,0x00000000ffc00000,0x00000000ffe00000)
 ParOldGen       total 40960K, used 0K [0x00000000fc400000, 0x00000000fec00000, 0x00000000fec00000)
  object space 40960K, 0% used [0x00000000fc400000,0x00000000fc400000,0x00000000fec00000)
 Metaspace       used 3996K, capacity 4568K, committed 4864K, reserved 1056768K
  class space    used 446K, capacity 460K, committed 512K, reserved 1048576K
Heap after GC invocations=1 (full 0):
 PSYoungGen      total 18432K, used 2016K [0x00000000fec00000, 0x0000000100000000, 0x0000000100000000)
  eden space 16384K, 0% used [0x00000000fec00000,0x00000000fec00000,0x00000000ffc00000)
  from space 2048K, 98% used [0x00000000ffc00000,0x00000000ffdf8100,0x00000000ffe00000)
  to   space 2048K, 0% used [0x00000000ffe00000,0x00000000ffe00000,0x0000000100000000)
 ParOldGen       total 40960K, used 12129K [0x00000000fc400000, 0x00000000fec00000, 0x00000000fec00000)
  object space 40960K, 29% used [0x00000000fc400000,0x00000000fcfd8740,0x00000000fec00000)
 Metaspace       used 3996K, capacity 4568K, committed 4864K, reserved 1056768K
  class space    used 446K, capacity 460K, committed 512K, reserved 1048576K
}
{Heap before GC invocations=2 (full 0):
 PSYoungGen      total 18432K, used 18341K [0x00000000fec00000, 0x0000000100000000, 0x0000000100000000)
  eden space 16384K, 99% used [0x00000000fec00000,0x00000000ffbf1538,0x00000000ffc00000)
  from space 2048K, 98% used [0x00000000ffc00000,0x00000000ffdf8100,0x00000000ffe00000)
  to   space 2048K, 0% used [0x00000000ffe00000,0x00000000ffe00000,0x0000000100000000)
 ParOldGen       total 40960K, used 12129K [0x00000000fc400000, 0x00000000fec00000, 0x00000000fec00000)
  object space 40960K, 29% used [0x00000000fc400000,0x00000000fcfd8740,0x00000000fec00000)
 Metaspace       used 3998K, capacity 4568K, committed 4864K, reserved 1056768K
  class space    used 446K, capacity 460K, committed 512K, reserved 1048576K
Heap after GC invocations=2 (full 0):
 PSYoungGen      total 18432K, used 1996K [0x00000000fec00000, 0x0000000100000000, 0x0000000100000000)
  eden space 16384K, 0% used [0x00000000fec00000,0x00000000fec00000,0x00000000ffc00000)
  from space 2048K, 97% used [0x00000000ffe00000,0x00000000ffff3100,0x0000000100000000)
  to   space 2048K, 0% used [0x00000000ffc00000,0x00000000ffc00000,0x00000000ffe00000)
 ParOldGen       total 40960K, used 28540K [0x00000000fc400000, 0x00000000fec00000, 0x00000000fec00000)
  object space 40960K, 69% used [0x00000000fc400000,0x00000000fdfdf180,0x00000000fec00000)
 Metaspace       used 3998K, capacity 4568K, committed 4864K, reserved 1056768K
  class space    used 446K, capacity 460K, committed 512K, reserved 1048576K
}
{Heap before GC invocations=3 (full 1):
 PSYoungGen      total 18432K, used 1996K [0x00000000fec00000, 0x0000000100000000, 0x0000000100000000)
  eden space 16384K, 0% used [0x00000000fec00000,0x00000000fec00000,0x00000000ffc00000)
  from space 2048K, 97% used [0x00000000ffe00000,0x00000000ffff3100,0x0000000100000000)
  to   space 2048K, 0% used [0x00000000ffc00000,0x00000000ffc00000,0x00000000ffe00000)
 ParOldGen       total 40960K, used 28540K [0x00000000fc400000, 0x00000000fec00000, 0x00000000fec00000)
  object space 40960K, 69% used [0x00000000fc400000,0x00000000fdfdf180,0x00000000fec00000)
 Metaspace       used 3998K, capacity 4568K, committed 4864K, reserved 1056768K
  class space    used 446K, capacity 460K, committed 512K, reserved 1048576K
Heap after GC invocations=3 (full 1):
 PSYoungGen      total 18432K, used 0K [0x00000000fec00000, 0x0000000100000000, 0x0000000100000000)
  eden space 16384K, 0% used [0x00000000fec00000,0x00000000fec00000,0x00000000ffc00000)
  from space 2048K, 0% used [0x00000000ffe00000,0x00000000ffe00000,0x0000000100000000)
  to   space 2048K, 0% used [0x00000000ffc00000,0x00000000ffc00000,0x00000000ffe00000)
 ParOldGen       total 40960K, used 30303K [0x00000000fc400000, 0x00000000fec00000, 0x00000000fec00000)
  object space 40960K, 73% used [0x00000000fc400000,0x00000000fe197cb0,0x00000000fec00000)
 Metaspace       used 3998K, capacity 4568K, committed 4864K, reserved 1056768K
  class space    used 446K, capacity 460K, committed 512K, reserved 1048576K
}
{Heap before GC invocations=4 (full 2):
 PSYoungGen      total 18432K, used 16316K [0x00000000fec00000, 0x0000000100000000, 0x0000000100000000)
  eden space 16384K, 99% used [0x00000000fec00000,0x00000000ffbef198,0x00000000ffc00000)
  from space 2048K, 0% used [0x00000000ffe00000,0x00000000ffe00000,0x0000000100000000)
  to   space 2048K, 0% used [0x00000000ffc00000,0x00000000ffc00000,0x00000000ffe00000)
 ParOldGen       total 40960K, used 30303K [0x00000000fc400000, 0x00000000fec00000, 0x00000000fec00000)
  object space 40960K, 73% used [0x00000000fc400000,0x00000000fe197cb0,0x00000000fec00000)
 Metaspace       used 3998K, capacity 4568K, committed 4864K, reserved 1056768K
  class space    used 446K, capacity 460K, committed 512K, reserved 1048576K
Heap after GC invocations=4 (full 2):
 PSYoungGen      total 18432K, used 6000K [0x00000000fec00000, 0x0000000100000000, 0x0000000100000000)
  eden space 16384K, 36% used [0x00000000fec00000,0x00000000ff1dc3c0,0x00000000ffc00000)
  from space 2048K, 0% used [0x00000000ffe00000,0x00000000ffe00000,0x0000000100000000)
  to   space 2048K, 0% used [0x00000000ffc00000,0x00000000ffc00000,0x00000000ffe00000)
 ParOldGen       total 40960K, used 40600K [0x00000000fc400000, 0x00000000fec00000, 0x00000000fec00000)
  object space 40960K, 99% used [0x00000000fc400000,0x00000000feba63d8,0x00000000fec00000)
 Metaspace       used 3998K, capacity 4568K, committed 4864K, reserved 1056768K
  class space    used 446K, capacity 460K, committed 512K, reserved 1048576K
}
```

#### 其他参数

![image-20220430233103949](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231832716-224673678.png)

### 其他参数

![image-20220430233213287](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231832494-1842920164.png)

---

## 04-通过Java代码获取JVM参数

![image-20220430233302021](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231832160-880359489.png)

```java
/**
 *
 * 监控我们的应用服务器的堆内存使用情况，设置一些阈值进行报警等处理
 *
 * @author shkstart
 * @create 15:23
 */
public class MemoryMonitor {
    public static void main(String[] args) {
        MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();
        MemoryUsage usage = memorymbean.getHeapMemoryUsage();
        System.out.println("INIT HEAP: " + usage.getInit() / 1024 / 1024 + "m");
        System.out.println("MAX HEAP: " + usage.getMax() / 1024 / 1024 + "m");
        System.out.println("USE HEAP: " + usage.getUsed() / 1024 / 1024 + "m");
        System.out.println("\nFull Information:");
        System.out.println("Heap Memory Usage: " + memorymbean.getHeapMemoryUsage());
        System.out.println("Non-Heap Memory Usage: " + memorymbean.getNonHeapMemoryUsage());
 
        System.out.println("=======================通过java来获取相关系统状态============================ ");
        System.out.println("当前堆内存大小totalMemory " + (int) Runtime.getRuntime().totalMemory() / 1024 / 1024 + "m");// 当前堆内存大小
        System.out.println("空闲堆内存大小freeMemory " + (int) Runtime.getRuntime().freeMemory() / 1024 / 1024 + "m");// 空闲堆内存大小
        System.out.println("最大可用总堆内存maxMemory " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "m");// 最大可用总堆内存大小
 
    }
}
```

# 5、分析GC日志

![image-20220501123238180](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231831875-969141082.png)

---

## 5.1、GC日志参数

![image-20210228201901181](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231831409-1752194565.png)

---

## 5.2、GC日志格式

![image-20220501124035205](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231831051-220047363.png)

### 复习：GC分类s

![image-20210228202300725](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231830398-462085779.png)

1、**新生代收集**：当Eden区满的时候就会进行新生代收集，所以新生代收集和S0区域和S1区域无关

2、**老年代收集和新生代收集的关系**：进行老年代收集之前会先进行一次年轻代的垃圾收集，原因如下：一个比较大的对象无法放入新生代，那它自然会往老年代去放，如果老年代也放不下，那会先进行一次新生代的垃圾收集，之后尝试往新生代放，如果还是放不下，才会进行老年代的垃圾收集，之后在往老年代去放，这是一个过程，我来说明一下为什么需要往老年代放，但是放不下，而进行新生代垃圾收集的原因，这是因为新生代垃圾收集比老年代垃圾收集更加简单，这样做可以节省性能

3、**进行垃圾收集的时候，堆包含新生代、老年代、元空间/永久代**：可以看出Heap后面包含着新生代、老年代、元空间，但是我们设置堆空间大小的时候设置的只是新生代、老年代而已，元空间是分开设置的

4、**`哪些情况会触发Full GC`**：老年代空间不足、方法区空间不足、显示调用System.gc()、Minior GC进入老年代的数据的平均大小 大于 老年代的可用内存、大对象直接进入老年代，而老年代的可用空间不足

![image-20210228202856941](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231830032-777633019.png)

![image-20220501124428897](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231829054-1596180424.png)

### 不同GC分类的GC细节

```java
/**
 *  -XX:+PrintCommandLineFlags
 *
 *  -XX:+UseSerialGC:表明新生代使用Serial GC ，同时老年代使用Serial Old GC
 *
 *  -XX:+UseParNewGC：标明新生代使用ParNew GC
 *
 *  -XX:+UseParallelGC:表明新生代使用Parallel GC
 *  -XX:+UseParallelOldGC : 表明老年代使用 Parallel Old GC
 *  说明：二者可以相互激活
 *
 *  -XX:+UseConcMarkSweepGC：表明老年代使用CMS GC。同时，年轻代会触发对ParNew 的使用
 * @author shkstart
 * @create 17:19
 */
public class GCUseTest {
    public static void main(String[] args) {
        ArrayList<byte[]> list = new ArrayList<>();
 
        while(true){
            byte[] arr = new byte[1024 * 10];//10kb
            list.add(arr);
//            try {
//                Thread.sleep(5);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    }
}
```

#### 老年代使用CMS GC

**GC设置方法**：参数中使用-XX:+UseConcMarkSweepGC，说明老年代使用CMS GC，同时年轻代也会触发对ParNew的使用，因此添加该参数之后，新生代使用ParNew GC，而老年代使用CMS GC，整体是并发垃圾收集，主打低延迟

![image-20220501124657607](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231828714-231468715.png)

打印出来的GC细节：

![image-20220501124707342](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231827413-2136498147.png)

#### 新生代使用Serial GC

**GC设置方法**：参数中使用-XX:+UseSerialGC，说明新生代使用Serial GC，同时老年代也会触发对Serial Old GC的使用，因此添加该参数之后，新生代使用Serial GC，而老年代使用Serial Old GC，整体是串行垃圾收集

![image-20220501124745704](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231827052-1858866693.png)

打印出来的GC细节：

![image-20220501124802034](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231826610-67408916.png)

DefNew代表新生代使用Serial GC，然后Tenured代表老年代使用Serial Old GC

### GC日志分类

#### MinorGC

![image-20210228202924476](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231826135-379159093.png)

![image-20210228203012381](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231825745-1646304122.png)

#### FullGC

![image-20210228203030198](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231825419-555137237.png)

![image-20210228203038526](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231825063-309395934.png)

### GC日志结构剖析

![image-20220501125431610](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231824807-1550677889.png)

#### 垃圾收集器

![image-20210228203123033](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231824428-1818802275.png)

#### GC前后情况

![image-20210228203212974](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231824046-2011024815.png)

#### GC时间

![image-20220501125551171](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231823639-892613450.png)

### Minor GC 日志解析

![image-20210228202059395](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231823277-1768104165.png)

上图是对下图的一条GC日志展开来说

![image-20220501125808654](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231822894-1329756446.png)

### Full GC 日志解析

![image-20210228202112942](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231822469-585336176.png)

上图是对下图的一条GC日志展开来说

![image-20210228203743605](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231821578-1522259065.png)

---

## 5.3、GC日志分析工具

![image-20220501130109859](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231821248-863305893.png)

![image-20210228204231264](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231820861-1863840032.png)

加上jvm参数后，运行

```java
package com.atguigu.java;
 
import com.sun.xml.internal.ws.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
 
/**
 * java.lang.OutOfMemoryError: Metaspace异常演示：
 *
 * -Xms60m -Xmx60m -XX:MetaspaceSize=10m -XX:MaxMetaspaceSize=10m -XX:SurvivorRatio=8 -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC -Xloggc:d:/Metaspace00M.log
 *
 * @author shkstart
 * @create 13:25
 */
public class MetaspaceOOM extends ClassLoader {
    public static void main(String[] args) {
        int j = 0;
        try {
            MetaspaceOOM test = new MetaspaceOOM();
            for (int i = 0; i < 10000; i++) {
                //创建ClassWriter对象，用于生成类的二进制字节码
                ClassWriter classWriter = new ClassWriter(0);
                //指明版本号，修饰符，类名，包名，父类，接口
                classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Class" + i, null, "java/lang/Object", null);
                //返回byte[]
                byte[] code = classWriter.toByteArray();
                //类的加载
                test.defineClass("Class" + i, code, 0, code.length);//Class对象
                j++;
            }
        } finally {
            System.out.println(j);
        }
    }
}
 
```

![image-20210228204439590](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231820505-1769498770.png)

生成文件

![image-20210228204456778](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231820236-2076244973.png)

加JVM参数，运行

```java
package com.atguigu.java;
 
import java.util.ArrayList;
 
/**
 * 测试生成详细的日志文件
 *
 * -Xms60m -Xmx60m -XX:SurvivorRatio=8 -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC -Xloggc:d:/GCLogTest.log
 *
 * @author shkstart
 * @create 14:27
 */
public class GCLogTest {
    public static void main(String[] args) {
        ArrayList<byte[]> list = new ArrayList<>();
 
        for (int i = 0; i < 5000; i++) {
            byte[] arr = new byte[1024 * 50];//50KB
            list.add(arr);
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
 
    }
}
```

![image-20210228204823031](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231819970-204739388.png)

![image-20210228204832432](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231819733-1157708407.png)

### GCEasy

![image-20210228204733025](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231819370-1999929515.png)

点击选择文件

![image-20210228204740340](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231818937-2102163363.png)

![image-20210228204804927](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231818603-595632122.png)

![image-20210228204811911](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231818272-1598339305.png)

![image-20210228204903329](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231817926-60621021.png)

![image-20210228205101646](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231817485-224318401.png)

![image-20210228205133972](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231817209-1098619944.png)

40M已经是顶峰了，再分配就OOM了

![image-20210228205208811](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231816916-862146016.png)

Java8默认用Parallel GC，重点关注吞吐量

![image-20210228205507787](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231816538-275859463.png)

对比其他GC，可以改G1、CMS+ParNew，看看这俩是不是主打低延迟，但吞吐量会差一点。

GC之后

![image-20210228205734098](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231816190-1849176177.png)

GC之前

![image-20210228205743184](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231815828-1735594563.png)

GC时间

![image-20210228205759968](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231815431-1660883410.png)

年轻代，GC后占空间减少

![image-20210228205828684](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231815117-1286696905.png)

老年代，一直涨

![image-20210228205907108](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231814718-40018910.png)

![image-20210228210011415](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231814385-118960497.png)

![image-20210228210021439](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231813953-2133093544.png)

### GCViewer

#### 基本概述

![image-20210228210057983](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231813571-1126749261.png)

#### 安装

![image-20210228210116059](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231813215-1517592125.png)

#### 双击运行

![image-20210228210124518](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231812952-1735161177.png)

![image-20210228210130444](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231812688-1268235165.png)

![image-20210228210139825](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231812394-1446100574.png)

![image-20210228210148506](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231812016-973654869.png)

![image-20210228210200455](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231811518-1784129583.png)

![image-20210228210207723](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231810817-1780596078.png)

![image-20210228210231512](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231810311-1697355473.png)

![image-20210228210245172](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231809715-1461895161.png)

### 其他工具（了解）

![image-20220501130822287](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501231809113-1217690912.png)