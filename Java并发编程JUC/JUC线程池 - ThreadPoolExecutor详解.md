# JUC线程池 - ThreadPoolExecutor详解

## 带着BAT大厂的面试问题去理解

- 为什么要有线程池?
- Java是实现和管理线程池有哪些方式? 请简单举例如何使用。
- 为什么很多公司不允许使用Executors去创建线程池? 那么推荐怎么使用呢?
- ThreadPoolExecutor有哪些核心的配置参数? 请简要说明
- ThreadPoolExecutor可以创建哪是哪三种线程池呢?
- 当队列满了并且worker的数量达到maxSize的时候，会怎么样?
- 说说ThreadPoolExecutor有哪些RejectedExecutionHandler策略? 默认是什么策略?
- 简要说下线程池的任务执行机制? execute –> addWorker –>runworker (getTask)
- 线程池中任务是如何提交的?
- 线程池中任务是如何关闭的?
- 在配置线程池的时候需要考虑哪些配置因素?
- 如何监控线程池的状态?

## 为什么要有线程池

先来说说什么是池化技术：

> 池化技术指的是提前准备一些资源，在需要时可以重复使用这些预先准备的资源。 池化技术的优点主要有两个：提前准备和重复利用。因为创建一个类需要经历复杂且耗时的操作，因此我们应该尽量复用已有的类，以确保程序的高效运行，当然如果能够提前创建这些类就再好不过了，而这些功能的实现依靠的就是池化技术。

什么是线程池：

> 线程池是线程使用的一种模式，它将线程和任务的概念分离开，使用线程来执行任务，并提供统一的线程管理和任务管理的实现方法，避免了频繁创建和销毁线程所带来的性能开销。

线程池有什么优点：

>- 优点1：复用线程，降低资源消耗
>
>  线程在创建时要开辟虚拟机栈、本地方法栈、程序计数器等私有线程的内存空间，而销毁时又要回收这些私有空间资源，而线程池创建了线程之后就会放在线程池中，减少了系统资源的消耗
>
>- 优点2：提高响应速度
>
>  线程池是复用已有线程来执行任务的，而线程是在有任务时才新建的，所以相比于线程来说，线程池能够更快的响应任务和执行任务。
>
>- 优点3：管控线程数和任务数
>
>  控制最大并发数：线程池可以创建固定的线程数，从而避免了无限创建线程的问题；控制任务最大数：线程池可以控制最大任务数，当任务超过一定数量之后，就会采用拒绝策略来处理多出的任务，从而保证了系统可以健康的运行。
>
>- 优点4：更多增强功能
>  线程池相比于线程来说提供了更多的功能，比如定时执行和周期执行等功能。

线程池3 个核心部分：

>- **线程集合**：核心线程和工作线程
>- **阻塞队列**：用于待执行任务排队
>- **拒绝策略处理器**：阻塞队列满后，对任务处理进行
>
>![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208101024556.jpeg)

## **Executor框架结构图解**

从JDK1.5开始，为了把工作单元与执行机制分离开，Executor框架诞生了，他是一个用于统一创建与运行的接口。Executor框架实现的就是线程池的功能。

### 3大部分

（1）任务。也就是工作单元，包括被执行任务需要实现的接口：Runnable接口或者Callable接口；

（2）任务的执行。也就是把任务分派给多个线程的执行机制，包括Executor接口及继承自Executor接口的ExecutorService接口。

（3）异步计算的结果。包括Future接口及实现了Future接口的FutureTask类。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208101006196.png" alt="image-20220810100653021" style="zoom: 75%;" />

使用示意图：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208101008359.png" alt="image-20220810100844250" style="zoom:80%;" />

## 从一个例子入手

```java
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleThreadPool {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 10; i++) {
            Runnable worker = new WorkerThread(i);
            executor.execute(worker);
        }
        executor.shutdown(); 
        while (!executor.isTerminated()) { 
        }
        System.out.println("Finished all threads");
    }

}
class WorkerThread implements Runnable {

    private Integer count;

    public WorkerThread(Integer s){
        this.count=s;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+" Start. count = "+count);
        processCommand();
        System.out.println(Thread.currentThread().getName()+" End.");
    }

    private void processCommand() {
        try {
            Thread.sleep(new Random().nextInt(5000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
/**
pool-1-thread-1 Start. Command = 0
pool-1-thread-3 Start. Command = 2
pool-1-thread-2 Start. Command = 1
pool-1-thread-4 Start. Command = 3
pool-1-thread-5 Start. Command = 4
pool-1-thread-5 End.
pool-1-thread-5 Start. Command = 5
pool-1-thread-3 End.
pool-1-thread-3 Start. Command = 6
pool-1-thread-2 End.
pool-1-thread-2 Start. Command = 7
pool-1-thread-1 End.
pool-1-thread-1 Start. Command = 8
pool-1-thread-2 End.
pool-1-thread-2 Start. Command = 9
pool-1-thread-4 End.
pool-1-thread-5 End.
pool-1-thread-1 End.
pool-1-thread-3 End.
pool-1-thread-2 End.
Finished all threads
/
```

程序中我们创建了固定大小为五个工作线程的线程池。然后分配给线程池十个工作，因为线程池大小为五，它将启动五个工作线程先处理五个工作，其他的工作则处于等待状态，一旦有工作完成，空闲下来工作线程就会捡取等待队列里的其他工作进行执行。输出表明线程池中**至始至终**只有五个名为 "pool-1-thread-1" 到 "pool-1-thread-5" 的五个线程，这五个线程不随着工作的完成而消亡，会一直存在，并负责执行分配给线程池的任务，**直到线程池消亡。**

## ThreadPoolExecutor使用详解

其实java线程池的实现原理很简单，说白了就是一个线程集合workerSet和一个阻塞队列workQueue。当用户向线程池提交一个任务(也就是线程)时，线程池会先将任务放入workQueue中。workerSet中的线程会不断的从workQueue中获取线程然后执行。当workQueue中没有任务的时候，worker就会阻塞，直到队列中有任务了就取出来继续执行。

![image-20220810102659222](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208101026280.png)

### 参数

```java
public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              RejectedExecutionHandler handler)
```

>- `corePoolSize` 线程池中的核心线程数，最开始的时候，当提交一个任务时，线程池创建一个新线程执行任务，直到当前线程数等于corePoolSize, 即使有其他空闲线程能够执行新来的任务, 也会继续创建线程；如果当前线程数为corePoolSize，继续提交的任务被保存到阻塞队列中，等待被执行；如果执行了线程池的prestartAllCoreThreads()方法，线程池会提前创建并启动所有核心线程。
>
>- `maximumPoolSize` 线程池中允许的最大线程数。如果当前阻塞队列满了，且继续提交任务，则创建新的线程执行任务，前提是当前线程数小于maximumPoolSize；当阻塞队列是无界队列, 则maximumPoolSize则不起作用, 因为无法提交至核心线程池的线程会一直持续地放入workQueue.
>
>- `workQueue` 用来保存等待被执行的任务的阻塞队列. 在JDK中提供了如下阻塞队列: 
>
> - `ArrayBlockingQueue`: 基于数组结构的有界阻塞队列，创建时必须指定大小，按FIFO排序任务；
> - `LinkedBlockingQueue`: 基于链表结构的阻塞队列，是无界的，默认大小是 `Integer.MAX_VALUE`，按FIFO排序任务，吞吐量通常要高于ArrayBlockingQueue；
> - `SynchronousQueue`: 一个不存储元素的阻塞队列，每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态，可以把它看成容量为0的队列，吞吐量通常要高于LinkedBlockingQueue；
> - `PriorityBlockingQueue`: 具有优先级的无界阻塞队列；
>
> >`LinkedBlockingQueue`比`ArrayBlockingQueue`在插入删除节点性能方面更优，但是二者在`put()`, `take()`任务的时均需要加锁，`SynchronousQueue`使用无锁算法，根据节点的状态判断执行，而不需要用到锁，其核心是`Transfer.transfer()`
> >
> >关于阻塞队列的详细信息可以参考：[ThreadPoolExecutor最佳实践--如何选择队列](https://blog.csdn.net/Holmofy/article/details/81610481)
>
>- `keepAliveTime` 线程空闲时的存活时间，即当线程没有任务执行时，该线程继续存活的时间；默认情况下，该参数只在线程数大于corePoolSize时才有用, 超过这个时间的空闲线程将被终止；
>- `unit` keepAliveTime的单位
>- `threadFactory` 创建线程的工厂，通过自定义的线程工厂可以给每个新建的线程设置一个具有识别度的线程名。默认为DefaultThreadFactory
>- `handler` 线程池的饱和策略，当阻塞队列满了，且没有空闲的工作线程，如果继续提交任务，必须采取一种策略处理该任务，线程池提供了4种策略:
>  - `AbortPolicy`: 直接抛出异常，默认策略；
>   - `CallerRunsPolicy`: 用调用者所在的线程来执行任务；
>   - `DiscardOldestPolicy`: 丢弃阻塞队列中靠最前的任务，并执行当前任务；
>   - `DiscardPolicy`: 直接丢弃任务；
>
>当然也可以根据应用场景实现RejectedExecutionHandler接口，自定义饱和策略，如记录日志或持久化存储不能处理的任务。

### 三种类型

#### newFixedThreadPool（可重用固定线程数的线程池）

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>());
}
```

> FixedThreadPool 的 corePoolSize 和 maximumPoolSize 都被设置为创建 FixedThreadPool 时指定的参数 nThreads。

![image-20220810104451871](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208101044917.png)

1. 如果当前运行的线程数少于 `corePoolSize`，则创建新线程来执行任务
2. 在线程池完成预热之后（当前运行的线程数等于 `corePoolSize`），将任务加入 `LinkedBlockingQueue`
3. 线程执行完1中的任务后，会在循环中反复从 `LinkedBlockingQueue` 获取任务来执行
   `FixedThreadPool` 使用无界队列 `LinkedBlockingQueue` 作为线程池的工作队列（队列的容量为 `Integer.MAX_VALUE`）。使用无界队列作为工作队列会对线程池带来如下影响:

>1. 当线程池中的线程数达到 corePoolSize 后，新任务将在无界队列中等待，因此线程池中的线程数不会超过 corePoolSize
>2. 由于1，使用无界队列时 maximumPoolSize 将是一个无效参数
>3. 由于1和2，使用无界队列时 keepAliveTime 将是一个无效参数
>4. 由于使用无界队列，运行中的 FixedThreadPool 不会拒绝任务（不会调用 RejectedExecutionHandler.rejectedExecution 方法）

#### SingleThreadExecutor（使用单个 worker 线程的 Executor）

```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>()));
}
```

`SingleThreadExecutor` 的 `corePoolSize` 和 `maximumPoolSize` 被设置为 1。其他参数与 `FixedThreadPool` 相同。

![image-20220810123327595](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208101233644.png)

1. 如果当前运行的线程数少于 1，则创建一个新线程来执行任务
2. 在线程池完成预热之后（当前线程池中有一个运行的线程），将任务加入 LinkedBlockingQueue
3. 线程执行完 1 中的任务后，会在一个无限循环中反复从 LinkedBlockingQueue 获取任务来执行

#### CachedThreadPool（根据需要创建新线程的线程池）

```java
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}
```

`CachedThreadPool` 的 `corePoolSize` 被设置为 0，即 `corePool` 为空；`maximumPoolSize` 被设置为 `Integer.MAX_VALUE`，即 `maximumPoolSize` 是无界的。这里把 `keepAliveTime` 设置为60L，意味着 `CachedThreadPool` 中的空闲线程等待新任务的最长时间为60秒，空闲线程超过60秒后将会终止。
`CachedThreadPool` 使用没有容量的 `SynchronousQueue` 作为线程池的工作队列，但 `maximumPool` 是无界的。这意味着如果主线程提交任务的速度高于 `maximumPool` 中线程处理任务的速度时，`CachedThreadPool` 会不断创建新线程。极端情况下，`CachedThreadPool` 会因为创建过多线程而耗尽CPU和内存资源。

![image-20220810125249042](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208101252095.png)

1. 首先执行 `SynchronousQueue.offer(Runnable task)`。如果当前 `maximumPool` 中有空闲线程正在执行 `SynchronousQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS)`，那么主线程执行 `offer` 操作与空闲线程执行的 `poll` 操作配对成功，主线程把任务交给空闲线程执行，`execute()` 方法执行完成；否则执行下面的步骤2
2. 当初始 `maximumPool` 为空，或者 `maximumPool` 中当前没有空闲线程时，将没有线程执行 `SynchronousQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS)`。这种情况下，步骤1将失败。此时 `CachedThreadPool` 会创建一个新线程执行任务，`execut()` 方法执行完成
3. 在步骤2中新创建的线程将任务执行完成后，会执行 `SynchronousQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS)。`这个 `poll` 操作会让空闲线程最多在 `SynchronousQueue` 中等待60秒钟。如果60秒钟内主线程提交了一个新任务（主线程执行步骤1），那么这个空闲线程将执行主线程提交的新任务；否则，这个空闲线程将终止。由于空闲60秒的空闲线程会被终止，因此长时间保持空闲的 `CachedThreadPool` 不会使用任何的资源。

### 关闭线程池

遍历线程池中的所有线程，然后逐个调用线程的interrupt方法来中断线程.

#### 关闭方式 - shutdown

将线程池里的线程状态设置成SHUTDOWN状态（不再接受新任务，但可以继续执行队列中的任务）, 然后中断所有没有正在执行任务的线程.

#### 关闭方式 - shutdownNow

将线程池里的线程状态设置成STOP状态, 然后停止所有正在执行或暂停任务的线程. 只要调用这两个关闭方法中的任意一个, isShutDown() 返回true. 当所有任务都成功关闭了, isTerminated()返回true.

## ThreadPoolExecutor源码详解

### 成员变量说明

```java
// ctl存储线程池状态和线程数，integer共32位，那么用前三位表示线程池状态，后29位表示线程池数量。
// 初始化，状态为RUNNING，起始线程数是0。
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
// Integer 共有 32 位，最右边 29 位表示工作线程数，最左边 3 位表示线程池状态
// 线程池的状态用高 3 位表示，其中包括了符号位
private static final int COUNT_BITS = Integer.SIZE - 3;
// 000-111111111111111111111111111111
private static final int CAPACITY = (1 << COUNT_BITS) - 1;
// 以下这五种状态依次为从小到大
// 十进制值：-536870912
// 此状态表示线程池可以接受新任务
private static final int RUNNING = -1 << COUNT_BITS;
// 十进制值：0
// 此状态不再接受新任务，但可以继续执行队列中的任务
private static final int SHUTDOWN = 0 << COUNT_BITS;
// 十进制值：536870912
// 此状态全面拒绝，并中断正在处理的任务
private static final int STOP = 1 << COUNT_BITS;
// 十进制值：1073741824
// 此状态表示所有任务已经被终止
private static final int TIDYING = 2 << COUNT_BITS;
// 十进制值：1610612736
// 此状态表示已清理完现场
private static final int TERMINATED = 3 << COUNT_BITS;
// 获取当前线程池的状态(前3位)
private static int runStateOf(int c) { return c & ~CAPACITY; }
// 获取当前线程池中线程数(后29位)
private static int workerCountOf(int c){ return c & CAPACITY; }
// 更新状态和数量
private static int ctlOf(int rs, int wc) { return rs | wc; }
// 小于判断C是不是小于S,比如runStateLessThan(var,STOP),那var就只有可能是(RUNNING,SHUTDOWN)
private static boolean runStateLessThan(int c, int s) {
    return c < s;
}
// 是不是C >= S
private static boolean runStateAtLeast(int c, int s) {
    return c >= s;
}
// 判断状态是不是RUNNING
private static boolean isRunning(int c) {
    return c < SHUTDOWN;
}
// execute()方法提交的Runnable任务,如果当前没有获取到线程去执行任务,那么任务将放到这个阻塞队列中.
private final BlockingQueue<Runnable> workQueue;
// 这个锁用来保护下面的workers,访问workers必须获取这个锁.
private final ReentrantLock mainLock = new ReentrantLock();
/** 
设置包含池中的所有工作线程, 只有在持有主锁时才能访问.
---> 这里为什么不使用线程安全的数据结构的原因主要两个:
1. 有复合操作, 增加 worker 的同时还要更新 largestPoolSize.
2. 中断线程时,如果不加锁,就可能出现并发的中断线程,引起中断风暴.
*/
private final HashSet<Worker> workers = new HashSet<Worker>();
// 线程通信手段, 用于支持awaitTermination方法, awaitTermination的作用等待所有任务完成,并支持设置超时时间,返回值代表是不是超时.
private final Condition termination = mainLock.newCondition();
// 记录workers历史以来的最大值,每次获取之前都要获取主锁mainlock
// 每次增加worker的时候,都会判断当前workers.size()是否大于largestPoolSize,如果大于,则将当前最大值赋予largestPoolSize.
private int largestPoolSize;
// 计数所有已完成任务,每次获取之前都要获取主锁mainlock
// 每个worker都有一个自己的成员变量 completedTasks 来记录当前 worker 执行的任务次数, 当前线worker工作线程终止的时候, 才会将worker中的completedTasks的数量加入到 completedTaskCount 指标中.
private long completedTaskCount;
// 线程工厂,用于构造线程的时候加一些业务标识什么的.
private volatile ThreadFactory threadFactory;
// 拒绝策略,默认四种AbortPolicy、CallerRunsPolicy、DiscardPolicy、DiscardOldestPolicy,建议自己实现,增加监控指标.
private volatile RejectedExecutionHandler handler;
// 当线程池内线程数超过corePoolSize之后,空闲的线程多久之后进行销毁.
private volatile long keepAliveTime;
// 核心线程池空闲允许销毁的时间.
private volatile boolean allowCoreThreadTimeOut;
// 线程池核心线程池大小
private volatile int corePoolSize;
// 线程池可建立的最大线程数
private volatile int maximumPoolSize;
// 默认拒绝策略 AbortPolicy
private static final RejectedExecutionHandler defaultHandler = new AbortPolicy();
//  安全控制访问（主要用于shutdown和 shutdownNow方法）
private static final RuntimePermission shutdownPerm = new RuntimePermission("modifyThread");
// threadPoolExecutor初始化的时候,还会初始化AccessControlContext对象的acc的值。
// 在threadPoolExecutor初始化的时候赋值,acc对象是指当前调用上下文的快照，其中包括当前线程继承的AccessControlContext和任何有限的特权范围，使得可以在稍后的某个时间点(可能在另一个线程中)检查此上下文。
private final AccessControlContext acc;
```

重点分析一下线程池运行状态-runState：

|    运行状态    |                           状态描述                           |
| :------------: | :----------------------------------------------------------: |
|  **RUNNING**   |          接收新任务，并且也能处理阻塞队列中的任务。          |
|  **SHUTDOWN**  |      不接收新任务，但是却可以继续处理阻塞队列中的任务。      |
|    **STOP**    | 不接收新任务，同时也不处理队列任务，并且中断正在进行的任务。 |
|  **TIDYING**   | 所有任务都已终止，workercount(有效线程数)为0，线程转向 TIDYING 状态将会运行 terminated() 钩子方法。 |
| **TERMINATED** |           terminated() 方法调用完成后变成此状态。            |

生命周期状态流转如下图所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208101704223.png)

### 提交任务主流程分析

先看流程图：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208101401157.png)

#### ThreadPoolExecutor.execute()

```java
public void execute(Runnable command) {
    if (command == null)
        throw new NullPointerException();
    // 返回包含线程数及线程池状态的 Integer 类型数值
    int c = ctl.get();
    //  第一步，如果工作线程数小于核心线程数，则创建线程任务并执行
    if (workerCountOf(c) < corePoolSize) {
        if (addWorker(command, true)) {//#2
            return;
        }
        // 如果创建失败，防止外部已经在线程池中加入新任务，重新获取线程数及线程池状态
        //创建失败的原因在下面分析addWorker的时候有说到
        c = ctl.get();
    }
    // 第二步，超过核心线程数则将该任务提交到阻塞队列,只有线程池处于 RUNNING（线程池可以接受新任务） 状态，才执行置入队列的操作
    if (isRunning(c) && workQueue.offer(command)) {
        int recheck = ctl.get();//double check
        // 如果线程池不是 RUNNING 状态，则将刚加入队列的任务移除
        if (!isRunning(recheck) && remove(command)) {
            //执行拒绝策略
            reject(command);
        }
        // 如果之前的线程已被消费完，新建一个线程
        else if (workerCountOf(recheck) == 0) {
            addWorker(null, false);//#3
        }
    }
    // 第三步，核心池和队列都已经满了，尝试创建一个新线程
    else if (!addWorker(command, false)) {//#4
        // 如果 addWorker 返回的是 false，表示创建失败，就会唤醒拒绝策略
        reject(command);
    }
}
```

>分析：线程池的执行步骤及思路
>
>1. 如果线程池中的线程数少于corePoolSize，则尝试通过addWorker方法来新建一个线程执行提交的任务。 在执行addWorker方法时会原子地检查runState和workerCount，通过返回false来防止在不应该添加线程的情况下发出错误警报。因为存在多个线程并发的提交任务，所以需要在addWorker内部确保正确的创建线程。
>2. 如果线程池中的线程数超过corePoolSize则尝试将该任务提交到阻塞队列。
>3. 如果无法将任务添加到阻塞队列，则尝试新建线程处理这个任务，如果addWorker方法返回false则代表线程池已经饱和或者线程池已经关闭了，然后拒绝该任务。
>
>---
>
>为什么需要double check线程池的状态?
>
>在多线程环境下，线程池的状态时刻在变化，而ctl.get()是非原子操作，很有可能刚获取了线程池状态后线程池状态就改变了。倘若没有double check，万一线程池处于非running状态(在多线程环境下很有可能发生)，没有double check的情况下就不会将command移出，那么command永远不会执行。

#### ThreadPoolExecutor.addWorker(Runnable firstTask, boolean core)

返回前面的线程池的 execute() 方法的代码中，可以发现这个addWorker() 有三个地方在调用，分别在 #2，#3和#4。

- \#2：当工作线程数 < 核心线程数的时候，通过`addWorker(command, true)`添加核心线程执行command任务。
- \#3：double check的时候，如果发现线程池处于正常运行状态但是里面没有工作线程，则添加个空任务和一个普通线程，这样一个 task 为空的 worker 在线程执行的时候会去阻塞任务队列里拿任务，这样就相当于创建了一个新的线程，只是没有马上分配任务。
- \#4：队列已满的情况下，通过添加普通线程（非核心线程）去执行当前任务，如果失败了则执行拒绝策略。

```java
private boolean addWorker(Runnable firstTask, boolean core) {
    // 不需要任务预定义的语法标签，响应下文的 continue retry，快速退出多层嵌套循环
    // 此中写法类似于 goto 作用，目的是早实现多重循环时能够快速退出到任何一层
    retry:
    for (; ; ) {
        int c = ctl.get();// 获取当前线程池状态及线程数的容器
        int rs = runStateOf(c);// 获取当前运行状态
		// 逻辑判断成立可以分为以下几种情况直接返回 false，表示添加工作线程失败:
        // rs > shutdown：线程池状态处于 STOP，TIDYING，TERMINATED时，添加工作线程失败，不接受新任务。
        // rs >= shutdown && firstTask != null：线程池状态处于 SHUTDOWN，STOP，TIDYING，TERMINATED状态且worker的首个任务不为空时，添加工作线程失败，不接受新任务。
        //rs >= shutdown && workQueue.isEmppty：线程池状态处于 SHUTDOWN，STOP，TIDYING，TERMINATED状态且阻塞队列为空时，添加工作线程失败，不接受新任务。
        if (rs >= SHUTDOWN && !(rs == SHUTDOWN && firstTask == null && !workQueue.isEmpty())) {
        //把判断条件类比成rs >= shutdown && (rs != shutdown || firstTask != null || workQueue.isEmpty())更好理解
            return false;
        }

        for (; ; ) {
            int wc = workerCountOf(c);// 当前线程数
            // 如果超过最大允许线程数则不能再添加新的线程
            //工作线程数量是否超过可表示的最大容量（CAPACITY）
            //如果添加核心工作线程，是否超过最大核心线程容量（corePoolSize）
            //如果添加普通工作线程，是否超过线程池最大线程容量（maximumPoolSize）
            if (wc >= CAPACITY || wc >= (core ? corePoolSize : maximumPoolSize)) {
                return false;
            }
            // 将当前活动线程数加一(原子性操作)
            if (compareAndIncrementWorkerCount(c)) {
                // 表示直接跳出与 retry 相邻的这个循环体，也就是跳出外面那层的for循环，从开始创建工作线程继续
                break retry;
            }
            // 线程池状态和工作线程数是可变化的，需要经常提取这个最新值
            c = ctl.get();
            // 判断当前运行状态是不是改变了
            if (runStateOf(c) != rs)
                // 跳转至标签处，继续执行循环
                // 如果条件为假，则说明状态没变，线程池还处于运行状态，继续在 for (;;) 循环内执行，进行cas操作
                // 如果变了，就从外层循环重新执行，重新进行状态的检查
                continue retry;
        }
    }

    // 开始创建工作线程
    boolean workerStarted = false;
    boolean workerAdded = false;
    Worker w = null;
    //外面一层的try...catch...finally主要是为了判断工作线程是否启动成功，如果内层try...catch...finally代码执行成功，即 worker 添加进线程池成功，workerAdded 标志位置为true，则启动 worker 中的线程 t.start()，同时将标志位 workerStarted 置为 true，表示线程启动成功。
    try {
        // 利用 Worker 构造方法中的线程池工厂创建线程，并封装工作线程 Worker 对象
        w = new Worker(firstTask);
        // 这是 Worker 中的属性对象 thread
        final Thread t = w.thread;
        if (t != null) {
            // 在进行 ThreadPoolExecutor 的敏感操作时
            // 需要持有主锁，避免在添加和启动线程时被干扰
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            //里面一层的try...catch...finally，当Worker实例中的 Thread 线程不为空的时候，开启一个独占锁ReentrantLock mainLock，防止其他线程也来修改操作。
            try {
                //获取线程池运行状态，由于在调用shutdown方法或者shutdownNow方法时也会先获取锁，但是不能保证执行addWorker方法的线程先获得这把锁，所以添加workers前需要先判断当前线程池的状态
                int rs = runStateOf(ctl.get());
                // 当线程池处于 RUNNING 状态或者线程池处于 SHUTDOWN 状态但是当前线程的 firstTask 为空，满足以上条件时才能将 worker 实例添加进线程池
                if (rs < SHUTDOWN || (rs == SHUTDOWN && firstTask == null)) {
                    //因为worker的线程是通过ThreadFactory创建的，而调用线程的start方法先决条件是线程必须是未启动的（没有调用过start方法），所以如果ThreadFactory返回的是一个已启动的线程就需要抛出异常。
                    if (t.isAlive()) {
                        throw new IllegalThreadStateException();
                    }
                    workers.add(w);
                    int s = workers.size();
                    // 整个线程池在运行期间的最大并发任务个数
                    if (s > largestPoolSize) {
                        largestPoolSize = s;
                    }
                    //将标志位 workerAdded 设置为 true，表示添加工作线程成功。
                    workerAdded = true;
                }
            } finally {
                //无论成功与否，在 finally 中都必须执行 mainLock.unlock()来释放锁。
                mainLock.unlock();
            }
            if (workerAdded) {
                // t 是Worker中的属性对象 thread，相当于执行Worker的run方法
                t.start();
                workerStarted = true;
            }
        }
    } finally {
        if (!workerStarted) {
            // addWorkerFailed方法主要是用来将这个未启动成功的worker从worker集中清除以及重新调整当前线程池中的线程数。
            addWorkerFailed(w);
        }
    }
    return workerStarted;
}
```

>整体还不算复杂，核心就是根据传入的任务创建一个Worker对象，然后启动Worker。
>
>分析：根据当前线程池状态，检查是否可以添加新的任务线程，如果可以就创建并启动任务。
>
>- 一切正常会返回 true。
>-  返回 false 的情况：
>   * 1.线程池没有处于 RIUNNING 状态
>   * 2.线程工厂创建新的任务线程失败
>
>参数分析：
>
> * firstTask：外部启动线程池时需要构造的第一个线程，它是线程的母体
> * core：新增工作线程时的判断指标
>    - true：表示新增工作线程时，需要判断当前 RUNNING 状态的线程是否少于 corePoolSize(常驻核心线程数)
>     - false：表示新增工作线程时，需要判断当前 RUNNING 状态的线程是否少于 maximumPoolSize(线程池能够容纳同时执行的最大线程数)
>
>**通过源码，可以总结出创建 Worker 失败的原因：**
>
>1. **在添加时线程池被停止了**
>2. **添加核心线程池时，超过核心线程池数量**
>3. **添加非核心线程池时，超过最大线程池数量**
>4. **Worker 对象初始化未完成时就被使用，即 thread 对象还未完全创建**
>5. **当前线程正在被运行(防止出现重复运行，抛出异常)**
>6. **线程创建过多，导致 OOM**

#### addWorkerFailed(Worker w)

**清理未能成功启动的线程**

```java
private void addWorkerFailed(Worker w) {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        if (w != null)
            workers.remove(w);
        decrementWorkerCount();
        tryTerminate();
    } finally {
        mainLock.unlock();
    }
}
```

>addWorkerFailed的执行逻辑很简单，从worker集中移除这个worker，递减当前线程池中的线程数，然后尝试终止线程池。至于**为什么要尝试终止线程池是因为我们不能保证这个启动失败的线程是因为什么原因启动失败的，甚至可能这个未能启动成功的线程运行时执行了shutdown或者shutdownNow方法，所以很有必要去检查尝试终止线程池。**后面会详细分析tryTerminate方法。

#### Worker

>**将线程封装为 worker 主要是为了更好地管理线程的中断**

理解 Worker 非常关键，主要有以下几点：

1. Worker 很像是任务的代理，在线程池中，最小的执行单位就是 Worker，所以 Worker 实现了 Runnable 接口，实现了 run 方法；
2. 在 Worker 初始化时 `this.thread = getThreadFactory ().newThread (this)` 这行代码比较关键，它把当前 Worker 作为线程的构造器入参，我们在后续的实现中会发现这样的代码：`Thread t = w.thread;t.start ()`，此时的 w 是 Worker 的引用申明，此处 t.start 实际上执行的就是 Worker 的 run 方法；
3. Worker 本身也实现了 AQS，所以其本身也是一个锁，其在执行任务的时候，会锁住自己，任务执行完成之后，会释放自己。

```java
/**
 * 内部常量类，工作线程的核心类实现
 * 实现 Runnable 接口，并把本对象作为参数输入给 run() 方法中的 runWorker(this)
 * 所以内部属性线程 thread 在 start 的时候，即会调用 runWorker 方法
 * 继承 AQS 是为了实现独占锁这个功能
 */
private final class Worker extends AbstractQueuedSynchronizer implements Runnable {

    private static final long serialVersionUID = 6138294804551838833L;

    final Thread thread;
    Runnable firstTask;
    volatile long completedTasks;

    /**
     * firstTask用它来初始化时传入的第一个任务，这个任务可以有也可以为null。如果这个值是非空的，那么线程就会在启动初期立即执行
     * 这个任务；如果这个值是null，那么就需要创建一个线程去执行任务列表（workQueue）中的任务，也就是非核心线程的创建。
     */
    Worker(Runnable firstTask) {
        // setState 是 AQS(AbstractQueuedSynchronizer) 的方法
        // 初始化为 -1，这样在线程运行前（调用runWorker）禁止中断，在 interruptIfStarted() 方法中会判断 getState()>=0
        setState(-1);
        this.firstTask = firstTask;
        //根据线程池的 threadFactory 创建一个线程，将 worker 本身传给线程（因为 worker 实现了 Runnable 接口）
        this.thread = getThreadFactory().newThread(this);
    }
    //....省略部分方法
    // 1 代表被锁住了，0 代表未锁
    protected boolean isHeldExclusively() {
        return getState() != 0;
    }

    // 尝试获取锁
    protected boolean tryAcquire(int unused) {
        // 从这里可以看出它是一个独占锁，因为当获取锁后，cas 设置 state 不可能成功，这里我们也能明白上文中将 state 设置为 -1 的作用，这种情况下永远不可能获取得锁，而 worker 要被中断首先必须获取锁
        if (compareAndSetState(0, 1)) {
            setExclusiveOwnerThread(Thread.currentThread());
            return true;
        }
        return false;
    }
    // 尝试释放锁
    protected boolean tryRelease(int unused) {
        setExclusiveOwnerThread(null);
        setState(0);
        return true;
    }  
    // 中断线程，这个方法会被 shutdowNow 调用，从中可以看出 shutdownNow 要中断线程不需要获取锁，也就是说如果线程正在运行，照样会给你中断掉，所以一般来说我们不用 shutdowNow 来中断线程，太粗暴了，中断时线程很可能在执行任务，影响任务执行
    void interruptIfStarted() {
        Thread t;
        // 中断也是有条件的，必须是 state >= 0 且 t != null 且线程未被中断
        // 如果 state == -1 ，不执行中断，再次明白了为啥上文中 setState(-1) 的意义
        if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
            try {
                t.interrupt();
            } catch (SecurityException ignore) {
            }
        }
    }
}
```

#### Woker.run()

在上面的`addWorker`方法中看到如果线程添加成功就启动`t.start()`，相当于执行Worker 的 run() 方法：

```java
public void run() {
    runWorker(this);
}
```

#### runWorker(Worker w)

run() 方法中又调用了runWorker() 方法：

```java
/**
 * Worker 对象作为输入参数，Worker 内部属性线程调用 start 的时候就会执行此方法。
 */
final void runWorker(Worker w) {
    Thread wt = Thread.currentThread();
    Runnable task = w.firstTask;
    w.firstTask = null;
    w.unlock(); // allow interrupts
    boolean completedAbruptly = true;
    try {
        while (task != null || (task = getTask()) != null) {
            w.lock();
            if ((runStateAtLeast(ctl.get(), STOP) ||
                    (Thread.interrupted() &&
                            runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                wt.interrupt();
            try {
                beforeExecute(wt, task);
                Throwable thrown = null;
                try {
                    task.run();
                } catch (RuntimeException x) {
                    thrown = x;
                    throw x;
                } catch (Error x) {
                    thrown = x;
                    throw x;
                } catch (Throwable x) {
                    thrown = x;
                    throw new Error(x);
                } finally {
                    afterExecute(task, thrown);
                }
            } finally {
                task = null;
                w.completedTasks++;
                w.unlock();
            }
        }
        completedAbruptly = false;
    } finally {
        processWorkerExit(w, completedAbruptly);
    }
}
```

里面包含了三块`try...catch...finally`代码，为了便于理解将其拆分，先看第一块：

```java
Thread wt = Thread.currentThread();
Runnable task = w.firstTask;
w.firstTask = null;
// Worker继承了AQS，所以执行unlock实际上最终
// 执行的就是Worker的tryRelease的方法，而
// tryRelease始终返回true，方法内部仅仅时将AQS的state设置为0，
// 代表此刻线程才真正开始运行任务，
// 也就是说执行到这一行代码前都是不允许中断当前线程的，
// 注意这段代码执行完毕后AQS的state为0
w.unlock(); // allow interrupts
boolean completedAbruptly = true;
try {
    // 循环的判断任务（firstTask或从队列中获取的task）是否为空
    while (task != null || (task = getTask()) != null) {
        // Worker加锁，本质是AQS获取资源并且尝试CAS更新state由0更变为1
        w.lock();
        // 如果线程池状态为 >= STOP（即 STOP，TIDYING，TERMINATED ）时，则线程应该中断
        // 如果线程池状态 < STOP, 线程不应该中断,如果中断了（Thread.interrupted() 返回 true，并清除标志位）,再次判断线程池状态（防止在清除标志位时执行了 shutdownNow() 这样的方法）,如果此时线程池为 STOP，执行线程中断
        if ((runStateAtLeast(ctl.get(), STOP) ||
             (Thread.interrupted() &&
              runStateAtLeast(ctl.get(), STOP))) &&
            !wt.isInterrupted())
            wt.interrupt();
            
            //此处省略了第二个try...catch...finally
    }
    // 走到这里说明某一次getTask()返回为null，线程正常退出
    completedAbruptly = false;
} finally {
     // 如果执行到这只有两种可能，一种是执行过程中异常中断了，一种是队列里没有任务了，从这里可以看出线程没有核心线程与非核心线程之分，哪个任务异常了或者正常退出了都会执行此方法，此方法会根据情况将线程数-1
    processWorkerExit(w, completedAbruptly);
}
```

第二个`try...catch...finally`

```java
try {
   beforeExecute(wt, task);
   Throwable thrown = null;
    
    //此处省略了第三个try...catch...finally
    
} finally {
    task = null;
    w.completedTasks++;
    w.unlock();
}
```

第三个`try...catch...finally`

```java
try {
    // 运行任务
    task.run();
} catch (RuntimeException x) {
    thrown = x; throw x;
} catch (Error x) {
    thrown = x; throw x;
} catch (Throwable x) {
    thrown = x; throw new Error(x);
} finally {
    afterExecute(task, thrown);
}
```

上面的代码中可以看到有`beforeExecute`、`afterExecute`和`terminaerd`三个函数，它们都是钩子函数，可以分别在子类中重写它们用来扩展ThreadPoolExecutor，例如添加日志、计时、监视或者统计信息收集的功能。

- beforeExecute()：线程执行之前调用
- afterExecute()：线程执行之后调用
- terminaerd()：线程池退出时候调用

这样拆分完之后发现，其实主要注意两个点就行了，分别是`getTask()`和`task.run()`，`task.run()`就是运行任务，那我们继续来看下`getTask()`是如何获取任务的。

#### getTask()

要注意的是调用getTask方法的地方是一个while死循环, 只要getTask有返回值,那么就不会退出循环。退出循环就说明改销毁超过核心线程数的那部分线程了。

```java
private Runnable getTask() {
    boolean timedOut = false; //最后获取任务是不是超时了

    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);//获取当前的运行状态

        //1.线程池状态是STOP，TIDYING，TERMINATED
        //2.线程池shutdown并且队列是空的.
        //满足以上两个条件之一则工作线程数wc减去1，然后直接返回null
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
            decrementWorkerCount();
            return null;
        }

        int wc = workerCountOf(c);

        // 是否开启超时机制，相当于当前这个worker线程是否需要被淘汰的标记
        // 如果核心线程数允许超时,则timed为true,开启超时机制.
        // 如果核心线程数不允许超时,那么就看当前总线程数是不是>核心线程数,如果大于,则timed为true,当前woker开启超时机制.
        boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
        //allowCoreThreadTimeOut它的默认值为false,默认的情况下核心线程数量会一直保持，即使这些线程是空闲的它也是会一直存在的，而当设置为 true 时，线程池中 corePoolSize 线程空闲时间达到 keepAliveTime 也将销毁关闭。
        

        //1.工作线程数 > 最大线程数maximumPoolSize 或者timed == true && timedOut == true
        //2.工作线程数 > 1 或者队列为空 
        //同时满足以上两个条件则通过CAS把线程数减去1，同时返回null。CAS把线程数减去1失败会进入下一轮循环做重试
        if ((wc > maximumPoolSize || (timed && timedOut))
            && (wc > 1 || workQueue.isEmpty())) {
            if (compareAndDecrementWorkerCount(c))
                return null;
            continue;
        }

        // 走到这里代表当前这个worker线程需要从队列中获取任务
        try {
            //timed代表当前这个worker是否需要在空闲指定的时间内被淘汰
            // 如果timed为true，通过poll()方法做超时拉取，keepAliveTime时间内没有等待到有效的任务，则返回null
            // 如果timed为false，通过take()做阻塞拉取，会阻塞到有下一个有效的任务时候再返回（一般不会是null）
            Runnable r = timed ?
                workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                workQueue.take();
            // 注意take方法时肯定不会返回null的，这是Queue的特性，所以返回只要返回null就代表
            // 当前这个worker线程肯定是非核心线程并且超时了。那么下一次for循环极大概率是需要淘汰这个非核心线程的
            if (r != null)
                return r;
            // 代表这个worker线程是一个非核心超时的线程
            timedOut = true;
            // 如果当前worker线程在阻塞期间抛出InterruptedException
            // 仅仅忽略这次异常，执行下一次for循环重试获取任务
        } catch (InterruptedException retry) {
            timedOut = false;
        }
    }
}
```

#### processWorkerExit

再来看看`runWorker`方法中线程最后执行的`processWorkerExit`方法：

- 任务异常退出, 则在加个worker回来, 当前任务是丢了的
- 任务不是异常退出:
  1. 如何核心线程允许超时,当任务队列中还有任务,那么就必须保证线程池中有一个worker,没有就在这个方法里面执行addWorker.
  2. 如果核心线程不允许超时,就得保证当前线程池中线程数量>=核心线程数,如果当前线程池中线程数量<核心线程数,依然要增加一个worker,执行addWorker.

```java

private void processWorkerExit(Worker w, boolean completedAbruptly) {
    //如果completedAbruptly为true，则说明线程执行时出现异常，需要将workerCount数量减一
    //如果completedAbruptly为false，说明在getTask方法中已经对workerCount进行减一，这里不用再减  
    if (completedAbruptly) 
        decrementWorkerCount();

    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        completedTaskCount += w.completedTasks;
        // 加锁确保线程安全地移除 worker 
        workers.remove(w);
    } finally {
        mainLock.unlock();
    }

    //尝试关闭线程池,但如果是正常运行状态,就不会关闭,这个是否关闭的临界条件在分析tryTerminate单独会说
    tryTerminate();

    int c = ctl.get();

    //这个地方比较绕,要好好看哈.
    // completedAbruptly=true代表异常结束的,具体为啥可以看runWorker中的代码,没有异常的话会走completedAbruptly=false的.
    //前提当前线程池的状态是SHUGTDOWN或者RUNNING,如果不是这两个状态,说明线程已经停止了,啥都不要干了.
    //如果任务是异常结束的,就增加worker
    //注: 别问我为啥上面要删除worker,还要再加,不删是不是不用加了。明确下那个任务已经退出getTask那块的死循环了,永远回不去了,回不去就说明没有任务可以执行了，只能先删掉然后再新增worker.
    if (runStateLessThan(c, STOP)) {
        if (!completedAbruptly) {
            int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
            if (min == 0 && ! workQueue.isEmpty())
                min = 1;
            if (workerCountOf(c) >= min)
                return; // replacement not needed
        }
        addWorker(null, false);
    }
}
```

#### tryTerminate

```java
final void tryTerminate() {
    for (;;) {
        int c = ctl.get();
        // 如果当前线程池状态处于运行状态或者线程池至少已经处于
        // TIDYING状态了（代表已经被终止了）或者线程池处于
        // SHUTDOWN状态（调用了shuntdown方法）但是任务还没有处理完的话
        // 这些情况下时还不需要终止线程池
        if (isRunning(c) ||
            runStateAtLeast(c, TIDYING) ||
            (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty()))
            return;
        // 不是以上三种情况代表目前为止是有资格终止线程池的，
        // 如果此刻线程池中的线程数超过0个的话，则尝试终止一个空闲的线程，
        // interruptIdleWorkers会将第一个阻塞的线程唤醒，接下来这个线程就会跳出runWorker
        // 方法中的while循环最后执行processWorkerExit方法，则processWorkerExit方法会继续
        // 调用tryTerminate方法来确保传递终止信号
        if (workerCountOf(c) != 0) {
            interruptIdleWorkers(ONLY_ONE);
            return;
        }
		
        // 执行到这里代表线程池中的状态必定是处于SHUTDOWN或者STOP状态之一
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // cas尝试将当前线程池状态设置为TIDYING将线程池中的线程数设置为0，
            // 成功的话则调用钩子方法terminated，因为存在并发调用tryTerminate方法
            // 所以只需要确保一个线程调用成功即可
            if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                try {
                    terminated();
                } finally {
                    // 最终将线程池状态设置为TERMINATED将线程池中的线程数设置为0
                    ctl.set(ctlOf(TERMINATED, 0));
                    // 线程池成功被终止后唤醒那些调用awaitTermination方法等待线程池终止的线程
                    termination.signalAll();
                }
                return;
            }
        } finally {
            mainLock.unlock();
        }
        // else retry on failed CAS
    }
}
```

在上面分析中我们只是分析了tryTerminate方法中断线程池的流程，tryTerminate方法只会在以下两种情况下尝试中断当前线程池

1. 调用了shutdown方法之后并且线程池中的任务都被执行完成之后
2. 调用了shutdownNow方法之后

> 但是为什么需要在processWorkerExit与addWorkerFailed中调用这个tryTerminate方法呢？
>
> 作者在这个方法的注释如下
>
> ```ABAP
> This method must be called following any action that might make termination possible -- reducing worker count or removing tasks from the queue during shutdown.
> ```
>
> tryTerminate方法必须在任何可能导致终止的操作中之后被调用，例如减少线程池的worker数量或者当关闭线程池从队列中移除任务的时候。

上述的分析中涉及到AQS的相关知识，可以看看我的这篇文章 [万字长文带你玩转ReentrantLock](https://juejin.cn/post/7127820316797042724)，其中有讲解到AQS的知识。

### shutdown

shutdown方法的作用是尝试唤醒那些由于拿不到任务而阻塞在队列中或者还未正式运行的worker以便这些线程能够从队列中将任务都尽快的执行完。底层是通过设置线程的中断标志为true，这样的话基于AQS或者LockSupport来实现阻塞线程的同步类就不会阻塞线程，然后这些被设置中断标记的worker就能够持续不断的从阻塞队列中获取任务并执行任务直到任务队列为空最终线程池中的所有worker都将退出。

```java
public void shutdown() {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        checkShutdownAccess();
        advanceRunState(SHUTDOWN);
        interruptIdleWorkers();//调用 interruptIdleWorkers(false)
        onShutdown(); // hook for ScheduledThreadPoolExecutor
    } finally {
        mainLock.unlock();
    }
    tryTerminate();
}
```

执行shutdown方法时必须获取主锁，然后调用checkShutdownAccess方法检查权限（不是重点）接下来advanceRunState方法修改线程池状态为SHUTDOWN

####  advanceRunState

```java
private void advanceRunState(int targetState) {
    for (;;) {
        int c = ctl.get();
        if (runStateAtLeast(c, targetState) ||
            ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))))
            break;
    }
}
```

自旋判断当前线程池的状态是否至少为传入的targetState，此时传入的是SHUTDOWN，如果当前线程池已经处于SHUTDOWN的话就break跳出循环即可，否则需要通过cas尝试将当前线程池的ctl重新修改为方法传入的targetState与当前线程池的线程数，cas失败代表有其它线程正在修改ctl那么只需要在下次自旋时重新判断当前线程池的状态或者重新修改ctl即可。

将线程池状态修改为SHUTDOWN之后接着调用interruptIdleWorkers方法**唤醒那些不是正在执行任务的worker（空闲的worker）**

#### interruptIdleWorkers

```java
private void interruptIdleWorkers(boolean onlyOne) {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        for (Worker w : workers) {
            Thread t = w.thread;
            if (!t.isInterrupted() && w.tryLock()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                } finally {
                    w.unlock();
                }
            }
            if (onlyOne)
                break;
        }
    } finally {
        mainLock.unlock();
    }
}
```

interruptIdleWorkers方法的执行逻辑很简单，只要当前线程不是出于中断状态，然后调用worker的tryLock方法尝试获取worker的锁，还记得runWorker方法吗

```java
while (task != null || (task = getTask()) != null) {
      w.lock();
}
```

worker在拿到任务的第一步就是给自己上锁代表worker当前正在执行任务，执行`w.lock();`这段代码后AQS的state就变成了1，而tryLock方法调用的是tryAcquire方法

```java
protected boolean tryAcquire(int unused) {
    if (compareAndSetState(0, 1)) {
        setExclusiveOwnerThread(Thread.currentThread());
        return true;
    }
    return false;
}
```

很显然如果worker正在执行任务，这个tryAcquire方法必定时返回false的，所以interruptIdleWorkers方法会将那些不是正在执行任务的worker或者还未开始执行任务的worker（还没执行到runWorker中的while循环中）都设置为中断状态，这样的话这些被中断的线程在下次执行getTask方法调用队列的take获取poll方法拿任务的时候就不会被阻塞了，不懂原理的可以去看LockSupport和AQS相关的知识点。阻塞队列都是基于这两个东西实现的。接下来这些永远不会被阻塞的worker就会不停的消耗队列中的任务然后退出，接下来那些刚刚由于真正执行任务而没有设置中断标记的worker执行下一次getTask的时候会发现线程池的状态已经为SHUTDOWN了慢慢的在队列中的任务都被执行完毕之后就会进入getTask方法的这段退出代码中

```java
if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
    decrementWorkerCount();
    return null;
}
```

最终线程池中的所有worker都将退出，任务都被执行完毕。

### shutdownNow

shutdownNow方法的作用是中断当前线程池中所有已启动的worker线程，这样那些所有正在运行的线程在执行完任务后重新执行getTask方法时就会直接退出，我们看一下shutdownNow方法的代码

```java
public List<Runnable> shutdownNow() {
    List<Runnable> tasks;
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        checkShutdownAccess();
        advanceRunState(STOP);
        interruptWorkers();
        tasks = drainQueue();
    } finally {
        mainLock.unlock();
    }
    tryTerminate();
    return tasks;
}
```

#### interruptWorkers

```java
private void interruptWorkers() {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        for (Worker w : workers)
            w.interruptIfStarted();
    } finally {
        mainLock.unlock();
    }
}
```

执行逻辑很简单，遍历线程池中所有的worker调用interruptIfStarted方法

```java
void interruptIfStarted() {
    Thread t;
    if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
        try {
            t.interrupt();
        } catch (SecurityException ignore) {
        }
    }
}
```

这个中断方法也很简单，只要当前worker的AQS状态大于等于0（已启动）并且它自己本身没有被中断的话就将这个worker线程的中断标记设置为true。接下来那些正在运行的worker或者拿不到任务的worker在下一次执行getTask方法的时候退出

```java
private Runnable getTask() {
    boolean timedOut = false; // Did the last poll() time out?

    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);

        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
            decrementWorkerCount();
            return null;
        }
        //......省略部分代码
    }
}
```

在上面的getTask执行一开始会先判断当前线程池的状态，如果是STOP的话这个worker线程就会退出。同时如果当前线程池中存在拿不到任务而阻塞的线程，由于所有的worker的线程都被设置了中断标记，这些线程都会被唤醒或者抛出InterruptedException（LockSupport和AQS的原因）从而重新执行上面的自旋最终退出。

接下来我们继续看一下是如何将那些未完成的任务重现保存起来的。

###  drainQueue方法

```java
private List<Runnable> drainQueue() {
    BlockingQueue<Runnable> q = workQueue;
    ArrayList<Runnable> taskList = new ArrayList<Runnable>();
    q.drainTo(taskList);
    if (!q.isEmpty()) {
        for (Runnable r : q.toArray(new Runnable[0])) {
            if (q.remove(r))
                taskList.add(r);
        }
    }
    return taskList;
}
```

drainQueue方法的执行逻辑也很简单，通过BlockingQueue自带的drainTo方法将内部剩余的所有任务都转移到另一个集合中，但是有一些特殊的队列例如DelayQueue是无法立即通过drainTo方法将任务进行转移的，因此可能需要通过remove方法尝试将这些任务强制删除然后转移到新队列中。

### awaitTermination方法

awaitTermination方法的作用时阻塞当前线程直到当前线程池被关闭或者在指定的时间后超时。

```java
public boolean awaitTermination(long timeout, TimeUnit unit)
    throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        for (;;) {
            if (runStateAtLeast(ctl.get(), TERMINATED))
                return true;
            if (nanos <= 0)
                return false;
            nanos = termination.awaitNanos(nanos);
        }
    } finally {
        mainLock.unlock();
    }
}
```

整个执行逻辑很简单，获取mainLock，判断当前线程池是否已经终止了以及传递的超时时间是否为0，否则的话就通过mainLock的termination这个Condition使当前线程阻塞指定的时间。注意`termination.awaitNanos(nanos);`方法返回的值代表的是**方法传入的超时时间减去线程阻塞的耗时时间**，也就是说整个awaitTermination如果返回true的话代表当前线程池能够在指定的超时时间内被终止，否则的话则代表经过指定的超时时间线程池还未被终止。

## 总结

ThreadPoolExecutor的主体部分已经分析完了，其中的难点在于execute方法，从任务被提交的那一刻开始就有可能存在其它线程并发的往线程池中提交任务甚至是关闭线程池、动态改变线程池的核心参数等等操作，因此需要在关键的地方通过CAS对当前线程池的核心线程数与最大线程数和线程池的状态进行判断，避免错误的提交和执行任务。看完源码后可以看一看美团的[Java线程池实现原理及其在美团业务中的实践](https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html)这篇文章从设计的角度去看线程池，再回过头看看一看源码应该又会有不同的体验。

## 相关问题

### 为什么线程池不允许使用Executors去创建? 

线程池不允许使用Executors去创建，而是通过ThreadPoolExecutor的方式，这样的处理方式让写的同学更加明确线程池的运行规则，规避资源耗尽的风险。 说明：Executors各个方法的弊端：

- newFixedThreadPool和newSingleThreadExecutor: 主要问题是堆积的请求处理队列可能会耗费非常大的内存，甚至OOM。
- newCachedThreadPool和newScheduledThreadPool: 主要问题是线程数最大数是Integer.MAX_VALUE，可能会创建数量非常多的线程，甚至OOM。

### Tomcat 的线程池和 JDK 的线程池实现有啥区别, Dubbo 中有类似 Tomcat 的线程池实现吗?

[tomcat和dubbo对于JDK线程池的修改](https://blog.csdn.net/evasnowind/article/details/108080463)

### 线程池里的线程真的有核心线程和非核心线程之分?

事实上在ThreadPoolExecutor中，核心线程与非核心线程没有什么区别，它们只是在数量上有区别而已，很有可能一个执行任务过快的核心线程会由于另一个非核心线程执行一个耗时的任务导致在指定的keepAliveTime之后这个核心线程被移除，从而使刚刚那个非核心线程转换为核心线程。核心与非核心线程的超时机制以及这两种线程间的转换完全是在内部的getTask方法中进行判断的。

### 线程池被 shutdown 后，还能产生新的线程?

### 线程把任务丢给线程池后肯定就马上返回了?

### 线程池里的线程异常后会再次新增线程吗，如何捕获这些线程抛出的异常?

### 线程池的大小如何设置，如何**动态设置**线程池的参数？

在上文总结中推荐的美团的那篇文章中有。

### 线程池的状态机画一下？

### 使用线程池应该避免哪些问题，能否简单说下线程池的最佳实践？

### 如何优雅关闭线程池？

### 如何对线程池进行监控？
