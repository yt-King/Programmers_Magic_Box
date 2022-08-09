# JUC线程池 - FutureTask详解

## 带着BAT大厂的面试问题去理解FutureTask

- FutureTask用来解决什么问题的? 为什么会出现?
- FutureTask类结构关系怎么样的?
- FutureTask的线程安全是由什么保证的?
- FutureTask结果返回机制?
- FutureTask内部运行状态的转变?
- FutureTask通常会怎么用? 举例说明。

## 先弄明白Future机制

要弄明白Future是什么，先来看看**Runnable**接口和**Callable**接口有什么区别：

| 对比                        | Runnable           | Callable         |
| --------------------------- | ------------------ | ---------------- |
| 方法返回值                  | 有                 | 无               |
| 异常                        | 没办法处理受检异常 | 可以处理受检异常 |
| 在Thread类中使用            | 可以               | 不可以           |
| **在ExecutorService中使用** | 可以               | 可以             |

那么再来看看**ExecutorService**的类图：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208090950445.png" alt="image-20220809095059142" style="zoom:67%;" />

终于在**ExecutorService**的**submit**方法中看到了Future的身影， `submit()` 方法清一色的返回 `Future` 类型的返回值。

**Future** 又是一个接口，里面只有五个方法：

```java
// 取消任务
boolean cancel(boolean mayInterruptIfRunning);

// 获取任务执行结果
V get() throws InterruptedException, ExecutionException;

// 获取任务执行结果，带有超时时间限制
V get(long timeout, TimeUnit unit) throws InterruptedException,                             ExecutionException,  TimeoutException;

// 判断任务是否已经取消
boolean isCancelled();

// 判断任务是否已经结束
boolean isDone();
```

我们再结合JDK 1.8的doc中，对Future的描述：

> A Future represents the result of an asynchronous computation. Methods are provided to check if the computation is complete, to wait for its completion, and to retrieve the result of the computation.
>
> 大概意思就是Future是一个用于**异步计算**的接口。

所以**Future**的作用就是在线程池提交Callable任务后返回了一个**Future**对象，使用它可以知道Callable任务的状态和得到Callable返回的执行结果。Future提供了get()方法让我们可以等待Callable结束并获取它的执行结果。

来一个例子看一看怎么使用Future：

```java
class Task implements Callable<String> {
  public String call() throws Exception {
    return "我是返回值！！"; 
  } 
}
ExecutorService executor = Executors.newFixedThreadPool(4); 
// 定义任务
Callable<String> task = new Task(); 
// 提交任务并获得Future
Future<String> future = executor.submit(task); 
// 从Future获取异步执行返回的结果: 
String result = future.get(); // 如果异步任务已经完成，就直接获得结果。如果异步任务还没有完成，那么get()会阻塞，直到任务完成后才返回结果
```

## FutureTask登场

>除了用线程池的 submit 方法会返回一个 future 对象之外，同样还可以用 FutureTask 来获取 Future 类和任务的结果。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208091003757.png" alt="image-20220809100311620" style="zoom:67%;" />

通过上面的类图我们看到**FutureTask**实现了**RunnableFuture**接口，这是很神奇的一个接口，它分别实现了 `Runnable` 和 `Future` 接口，所以可以推断出 `FutureTask` 具有这两种接口的特性：

- 有 `Runnable` 特性，所以可以被线程执行
- 有 `Future` 特性，所以可以从中获取到执行结果

我们可以把 `Callable` 实例当作 `FutureTask` 构造函数的参数，生成 `FutureTask` 的对象，然后把这个对象当作一个 `Runnable` 对象，放到线程池中或另起线程去执行，最后还可以通过 `FutureTask` 获取任务执行的结果。

```java
public class FutureTaskDemo {
    public static void main(String[] args) {
        Task task = new Task();
        //将Task 实例传入到 FutureTask 的构造函数中去，创建了一个 FutureTask 实例
        FutureTask<Integer> futureTask = new FutureTask<>(task);
        //把这个实例当作一个 Runnable 放到 new Thread() 中去执行
        new Thread(futureTask).start();
        try {
            System.out.println("task运行结果："+futureTask.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
//创建实现Callable接口的task
class Task implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        System.out.println("子线程正在计算");
        int count = 0;
        for (int i = 0; i < 666; i++) {
            count += i;
        }
        return sum;
    }
}
```

## 源码分析

### 核心属性

```java
/**
 * 用来保存 FutureTask 内部的任务执行状态
 *
 * NEW -> COMPLETING -> NORMAL
 * NEW -> COMPLETING -> EXCEPTIONAL
 * NEW -> CANCELLED
 * NEW -> INTERRUPTING -> INTERRUPTED
 */
private volatile int state;
/**
 * 表示是个新的任务或者还没被执行完的任务。
 * 这是初始状态。
 */
private static final int NEW          = 0;
/**
 * 大于 1 即是完成状态。
 * 所有值大于 COMPLETING 的状态都表示任务已经执行完成(任务正常执行完成，任务执行异常或者任务被取消)。
 *
 * 任务已经执行完成或者执行任务的时候发生异常，但是任务执行结果或者异常原因还没有保存到 outcome 字段的时候，状
 * 态会从 NEW 变更到 COMPLETING。
 * 但是这个状态会时间会比较短，属于中间状态。
 */
private static final int COMPLETING   = 1;
/**
 * 任务已经执行完成并且任务执行结果已经保存到 outcome 字段，状态会从 COMPLETING 转换到 NORMAL。
 * 这是一个最终态。
 */
private static final int NORMAL       = 2;
/**
 * 任务执行发生异常并且异常原因已经保存到 outcome 字段中后，状态会从 COMPLETING 转换到 EXCEPTIONAL。
 * 这是一个最终态。
 */
private static final int EXCEPTIONAL  = 3;
/**
 * 任务还没开始执行或者已经开始执行但是还没有执行完成的时候，用户调用了 cancel(false) 方法取消任务且不中断任务执行线程，
 * 这个时候状态会从 NEW 转化为 CANCELLED 状态。
 * 这是一个最终态。
 */
private static final int CANCELLED    = 4;
/**
 * 任务还没开始执行或者已经执行但是还没有执行完成的时候，用户调用了 cancel(true) 方法取消任务并且要中断任务执行线程
 * 但是还没有中断任务执行线程之前，状态会从 NEW 转化为 INTERRUPTING。
 *
 * 这是一个中间状态。
 */
private static final int INTERRUPTING = 5;
/**
 * 调用 interrupt() 中断任务执行线程之后状态会从 INTERRUPTING 转换到 INTERRUPTED。
 * 这是一个最终态。
 */
private static final int INTERRUPTED  = 6;
/**
 * 用来保存底层的调用，在被执行完成以后会指向 null,接着会初始化 state 字段为 NEW。
 */
private Callable<V> callable;
/**
 * 用来保存任务执行结果，如果发生异常，则用来保存异常原因
 * */
private Object outcome; 
/**
 * 运行callable的线程
 * */
private volatile Thread runner;
/**
 * 使用Treiber栈保存等待线程
 * */
private volatile WaitNode waiters;
```

使用关系如下：

- 任务本尊：`callable`
- 任务的执行者：`runner`
- 任务的结果：`outcome`
- 获取任务的结果：`state` + `outcome` + `waiters`
- 中断或者取消任务：`state` + `runner` + `waiters`

各个状态之间的可能转换关系如下图所示:

![image-20220809102440230](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208091024324.png)

###  构造函数

```java
/**
 * 这个构造函数会把传入的Callable变量保存在this.callable字段中，该字段定义为private Callable<V> callable;
 * 用来保存底层的调用，在被执行完成以后会指向null,接着会初始化state字段为NEW。
 */
public FutureTask(Callable<V> callable) {
    if (callable == null) {
        throw new NullPointerException();
    }
    this.callable = callable;
    this.state = NEW;       // ensure visibility of callable
}
/**
 * 把传入的 Runnable 封装成一个 Callable 对象保存在 callable 字段中，同时如果任务执行成功的话就会返回传入的 result。
 * 这种情况下如果不需要返回值的话可以传入一个 null。
 */
public FutureTask(Runnable runnable, V result) {
    this.callable = Executors.callable(runnable, result);
    this.state = NEW;       // ensure visibility of callable
}
```

再来看下`Executors.callable()`这个方法，这个方法的功能是把Runnable转换成Callable，代码如下:

```java
public static <T> Callable<T> callable(Runnable task, T result) {
    if (task == null)
        throw new NullPointerException();
    //这里采用的是适配器模式，调用RunnableAdapter<T>(task, result)方法来适配
    return new RunnableAdapter<T>(task, result);
}
```

关于适配器的介绍可以看一下我的这篇文章：[聊聊设计模式——适配器](https://juejin.cn/post/7129042891896274980)

```java
static final class RunnableAdapter<T> implements Callable<T> {
    //实现了Callable接口，在call()实现中调用Runnable.run()方法，然后把传入的result作为任务的结果返回。
    final Runnable task;
    final T result;
    RunnableAdapter(Runnable task, T result) {
        this.task = task;
        this.result = result;
    }
    public T call() {
        task.run();
        return result;
    }
}
```

看到这里我们发现`FutureTask`初始化后不论在构造函数中传入的是`Callable`还是`Runnable`，最后都会成为`Callable`，但是`FutureTask` 实现的是 `Runnable` 接口，也就是说他只能重写`run`方法，那么问题来了：

>- FutureTask 是怎样在 run() 方法中获取返回值的？
>- 它将返回值放到哪里了？
>- get() 方法又是怎样拿到这个返回值的呢？

### 核心方法 - run()

```java
/**
 * 1.判断当前任务的 state 是否等于 NEW，如果不为 NEW 则说明任务或者已经执行过，或者已经被取消，直接返回。
 * 2.如果状态为 NEW 则接着会通过 unsafe 类把任务执行线程引用 CAS 的保存在 runner 字段中，如果保存失败，则直接返回。
 * 3.执行任务
 * 4.如果任务执行发生异常，则调用 setException() 方法保存异常信息。否则执行 set() 设置结果和状态值。
 * 5.任务如果是被中断的，执行 handlePossibleCancellationInterrupt() 处理状态和中断响应。
 */
public void run() {
    // 条件一：state != NEW 条件成立，说明当前task已经被执行过了或者被cancel了，
    // 总之非NEW状态的任务，线程就不处理了，直接return;结束!
    // 条件二：!UNSAFE.compareAndSwapObject(this, runnerOffset,null, Thread.currentThread())
    // 条件成立：cas失败，当前任务被其它线程抢占了
    if (state != NEW || !UNSAFE.compareAndSwapObject(this, runnerOffset, null, Thread.currentThread())) {
        return;
    }
    try {
        Callable<V> c = callable;
        if (c != null && state == NEW) {
            V result;
            boolean ran;
            try {
                // 执行任务，计算逻辑
                result = c.call();
                ran = true;
            } catch (Throwable ex) {
                result = null;
                ran = false;
                // 任务异常
                setException(ex);
            }
            if (ran) {
                // 任务正常执行完毕
                set(result);
            }
        }
    } finally {
        // runner must be non-null until state is settled to
        // prevent concurrent calls to run()
        runner = null;
        // state must be re-read after nulling runner to prevent
        // leaked interrupts
        int s = state;
        // 如果任务被中断，执行中断处理
        if (s >= INTERRUPTING) {
            handlePossibleCancellationInterrupt(s);
        }
    }
}
```

可以看到`run`方法并没有返回值，而是通过`set`函数将运行结果（正常运行返回或者异常信息返回）保存，`setException` 和 `set` 方法非常相似，都是将异常或者结果保存在 `Object` 类型的 `outcome` 变量中，由于要考虑线程安全，所以要通过 CAS方式设置 `state` 变量的值，既然是在 CAS 成功后 更改 `outcome` 的值，这也就是 `state` 没有被 `volatile` 修饰的原因所在。更改 `outcome` 的值完成后还要再通过`lazySet`的方式{UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL)}将`state`的值转为最终态。

#### set()

```java
/**
 * 1.首先会 CAS 的把当前的状态从 NEW 变更为 COMPLETING 状态。
 * 2.把任务执行结果保存在 outcome 字段中。
 * 3.lazySet的方式把当前任务状态从 COMPLETING 变更为 NORMAL。
 * 4.调用 finishCompletion()。
 */
protected void set(V v) {
    if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
        outcome = v;
        UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // final state
        finishCompletion();
    }
}

/**
 * 1.首先会 CAS 的把当前的状态从 NEW 变更为 COMPLETING（中间状态）状态。
 * 2.把异常原因保存在 outcome 字段中，outcome 字段用来保存任务执行结果或者异常原因。
 * 3.lazySet的方式把当前任务状态从 COMPLETING 变更为 EXCEPTIONAL。
 * 4.调用 finishCompletion()。
 */
protected void setException(Throwable t) {
    if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
        outcome = t;
        UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL); // final state
        finishCompletion();
    }
}
```

#### finishCompletion()

>不管是任务执行异常还是任务正常执行完毕，或者取消任务，最后都会调用 finishCompletion() 方法，该方法会唤醒等待队列中的线程

```java
/**
 * 依次遍历 waiters 链表，唤醒节点中的线程，然后把 callable 置空。
 * 被唤醒的线程会各自从 awaitDone() 方法中的 LockSupport.park() 阻塞中返回，
 * 然后会进行新一轮的循环。在新一轮的循环中会返回执行结果（或者更确切的说是返回任务的状态）。
 */
private void finishCompletion() {
    // assert state > COMPLETING;
    for (WaitNode q; (q = waiters) != null;) {
        if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
            for (;;) {
                Thread t = q.thread;
                if (t != null) {
                    q.thread = null;
                    LockSupport.unpark(t);
                }
                WaitNode next = q.next;
                if (next == null) {
                    break;
                }
                q.next = null; // unlink to help gc
                q = next;
            }
            break;
        }
    }
    done();//是一个空方法，它是提供给子类覆写的，以实现一些任务执行结束前的额外操作。
    callable = null;        // 任务结束了设为null
}
```

这个方法事实上完成了一个“善后”工作。我们先来看看if条件语句中的CAS操作：

```java
UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)
```

该方法是将`waiters`属性的值由原值设置为null, 我们知道，`waiters`属性指向了Treiber栈的栈顶节点，可以说是代表了整个Treiber栈，将该值设为null的目的就是清空整个栈。如果设置不成功，则if语句块不会被执行，又进行下一轮for循环，而下一轮for循环的判断条件又是`waiters!=null` ，由此我们知道，虽然最外层的for循环乍一看好像是什么遍历节点的操作，其实只是为了确保waiters属性被成功设置成null，本质上相当于一个自旋操作。

将waiters属性设置成null以后，接下了` for (;;)`死循环才是真正的遍历节点，可以看出，循环内部就是一个普通的遍历链表的操作，我们前面讲属性的时候说过，Treiber栈里面存放的WaitNode代表了当前等待任务执行结束的线程，能进到这个循环说明任务已经执行完了，这个循环的作用也正是遍历链表中所有等待的线程，并唤醒他们。

#### handlePossibleCancellationInterrupt()

run方法的最后还有一个finally块：

```java
finally {
    // runner must be non-null until state is settled to
    // prevent concurrent calls to run()
    runner = null;
    // state must be re-read after nulling runner to prevent
    // leaked interrupts
    int s = state;
    if (s >= INTERRUPTING)
        handlePossibleCancellationInterrupt(s);
}
```

>在finally块中，我们将runner属性置为null，并且检查有没有遗漏的中断，如果发现`s >= INTERRUPTING`, 说明执行任务的线程有可能被其他线程中断了，因为`s >= INTERRUPTING` 只有两种可能，state状态为`INTERRUPTING`和`INTERRUPTED`。

关于任务取消的操作，后面讲cancel方法的时候会提到，现在我们来看看`handlePossibleCancellationInterrupt`方法干了点啥：

```java
private void handlePossibleCancellationInterrupt(int s) {
    // It is possible for our interrupter to stall before getting a
    // chance to interrupt us.  Let's spin-wait patiently.
    if (s == INTERRUPTING)
        while (state == INTERRUPTING)
            Thread.yield(); // wait out pending interrupt
}
```

可见该方法是一个自旋操作，如果当前的state状态是`INTERRUPTING`，我们在原地自旋，直到state状态转换成终止态。

> 我们来总结一下，run方法重点做了以下几件事：
>
> 1. 将runner属性设置成当前正在执行run方法的线程
> 2. 调用callable成员变量的call方法来执行任务
> 3. 设置执行结果outcome, 如果执行成功, 则outcome保存的就是执行结果；如果执行过程中发生了异常, 则outcome中保存的就是异常，设置结果之前，先将state状态设为中间态
> 4. 对outcome的赋值完成后，设置state状态为终止态(`NORMAL`或者`EXCEPTIONAL`)
> 5. 唤醒Treiber栈中所有等待的线程
> 6. 善后清理(waiters, callable，runner设为null)
> 7. 检查是否有遗漏的中断，如果有，等待中断状态完成。

### 核心方法 - get()

```java
/**
 * 任务发起线程可以调用 get() 方法来获取任务执行结果，如果此时任务已经执行完毕则会直接返回任务结果，
 * 如果任务还没执行完毕，则调用方会阻塞直到任务执行结束返回结果为止。
 *  1.判断任务当前的 state <= COMPLETING 是否成立。COMPLETING 状态是任务是否执行完成的临界状态。
 *  2.如果成立，表明任务还没有结束（这里的结束包括任务正常执行完毕，任务执行异常，任务被取消），则会调用 awaitDone() 进行阻塞等待。
 *  3.如果不成立表明任务已经结束，调用 report() 返回结果。
 */
public V get() throws InterruptedException, ExecutionException {
    int s = state;
    if (s <= COMPLETING) {
        s = awaitDone(false, 0L);
    }
    return report(s);
}

/**
 * 带超时等待的获取任务结果
 */
public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    if (unit == null) {
        throw new NullPointerException();
    }
    int s = state;
    // 如果 awaitDone() 超时返回之后任务还没结束，则抛出异常
    if (s <= COMPLETING && (s = awaitDone(true, unit.toNanos(timeout))) <= COMPLETING) {
        throw new TimeoutException();
    }
    return report(s);
}
```

#### report(int s)

report方法非常简单，它根据当前state状态，返回正常执行的结果，或者抛出指定的异常。

```java
//返回执行结果或抛出异常
private V report(int s) throws ExecutionException {
    Object x = outcome;
    // 任务正常执行完成，返回任务执行结果
    if (s == NORMAL) {
        return (V) x;
    }
    // 任务被取消，抛出 CancellationException 异常
    if (s >= CANCELLED) {
        throw new CancellationException();
    }
    // 其他状态，抛出执行异常 ExecutionException
    throw new ExecutionException((Throwable)x);
}
```

> 说明（重要！！）：FutureTask中会涉及到**两类线程**，一类是执行任务的线程，它只有一个，`FutureTask`的`run`方法就由该线程来执行；一类是获取任务执行结果的线程，它可以有多个，这些线程可以并发执行，每一个线程都是独立的，都可以调用`get`方法来获取任务的执行结果。如果任务还没有执行完，则这些线程就需要进入`Treiber栈`中挂起，直到任务执行结束，或者等待的线程自身被中断。

理清了这一点后，我们再来详细看看`awaitDone`方法。可以看出，该方法的大框架是一个自旋操作。

#### 最核心方法 - awaitDone(boolean timed, long nanos)

```java
/**
 * 当调用 get() 获取任务结果但是任务还没执行完成的时候，调用线程会调用 awaitDone() 方法进行阻塞等待
 *
 *  1.判断调用 get() 的线程是否被其他线程中断，如果是的话则在等待队列中删除对应节点然后抛出 InterruptedException 异常。
 *  2.取任务当前状态，如果当前任务状态大于 COMPLETING 则表示任务执行完成，则把 thread 字段置 null 并返回结果。
 *  3.如果任务处于 COMPLETING 状态，则表示任务已经处理完成（正常执行完成或者执行出现异常），
 *      但是执行结果或者异常原因还没有保存到 outcome 字段中。这个时候调用线程让出执行权让其他线程优先执行。
 *  4.如果等待节点为空，则构造一个等待节点 WaitNode。
 *  5.如果第四步中新建的节点还没入队列，则 CAS 的把该节点加入 waiters 队列的首节点。
 *  6.阻塞等待。
 */
private int awaitDone(boolean timed, long nanos) throws InterruptedException {
    // 计算等待截止时间
    final long deadline = timed ? System.nanoTime() + nanos : 0L;
    WaitNode q = null;
    boolean queued = false;
    for (;;) {
        // 判断阻塞线程是否被中断，如果被中断则在等待队列中删除该节点并抛出 InterruptedException 异常
        if (Thread.interrupted()) {
            removeWaiter(q);
            throw new InterruptedException();
        }
        // 获取当前状态，如果状态大于 COMPLETING
        // 说明任务已经结束（要么正常结束，要么异常结束，要么被取消）
        // 则把 thread 显示置空，并返回结果
        int s = state;
        if (s > COMPLETING) {
            if (q != null)
                q.thread = null;
            return s;
        }
        // 如果状态处于中间状态 COMPLETING
        // 表示任务已经结束但是任务执行线程还没来得及给 outcome 赋值
        // 这个时候让出执行权让其他线程优先执行
        else if (s == COMPLETING) { // cannot time out yet
            Thread.yield();
        }
        // 如果等待节点为空，则构造一个等待节点
        else if (q == null) {
            q = new WaitNode();
        }
        // 如果还没有入队列，则把当前节点加入 waiters 首节点并替换原来 waiters
        else if (!queued) {
            queued = UNSAFE.compareAndSwapObject(this, waitersOffset, q.next = waiters, q);
        }
        else if (timed) {
            // 如果需要等待特定时间，则先计算要等待的时间
            // 如果已经超时，则删除对应节点并返回对应的状态
            nanos = deadline - System.nanoTime();
            if (nanos <= 0L) {
                removeWaiter(q);
                return state;
            }
            // 阻塞等待特定时间
            LockSupport.parkNanos(this, nanos);
        }
        else {
            // 阻塞等待直到被其他线程唤醒
            LockSupport.park(this);
        }
    }
}
```

对于前面的几种情况比如说`s > COMPLETING`表示任务已经结束直接返回即可，`s == COMPLETING`表示任务已经结束但是任务执行线程还没来得及给 `outcome` 赋值，让出执行权即可。等到`s >COMPLETING`的时候也就可以返回了。

分析完`s > COMPLETING`和`s == COMPLETING`的情况就只剩下`s<COMPLETING`的情况，也就是说这时候`state`的值为0，也就是初始状态表示是个新的任务或者还没被执行完的任务。这个时候一般会经历三次for循环：

1. 第一轮：执行的逻辑是 `q == null`, 这时候会新建一个节点 q, 第一轮循环结束。

2. 第二轮for循环，执行的逻辑是 `!queue`，这个时候会把第一轮循环中生成的节点的 next 指针指向waiters，然后CAS的把节点q 替换waiters, 也就是把新生成的节点添加到waiters 中的首节点。如果替换成功，queued=true，第二轮循环结束。替换失败的话可能下一次再来CAS替换也可能进别的分支。

   >在这里介绍一下这个**WaitNode**节点，可以把它理解为一个由单向链表构成的等待队列，我觉得有点类似于简化版的AQS队列，关于AQS的相关知识可以看看 [万字长文带你玩转ReentrantLock](https://juejin.cn/post/7127820316797042724)，里面详细分析了AQS，WaitNode源码如下：
   >
   >```java
   >static final class WaitNode {
   >    volatile Thread thread;
   >    volatile WaitNode next;
   >    WaitNode() { thread = Thread.currentThread(); }
   >}
   >```
   >
   >FutureTask中的这个单向链表是当做**栈**来使用的，确切来说是当做Treiber栈来使用的，不了解Treiber栈是个啥的可以简单的把它当做是一个线程安全的栈，它使用CAS来完成入栈出栈操作(想进一步了解的话可以看[这篇文章](https://segmentfault.com/a/1190000012463330))。为啥要使用一个线程安全的栈呢，因为同一时刻可能有多个线程都在获取任务的执行结果，如果任务还在执行过程中，则这些线程就要被包装成`WaitNode`扔到Treiber栈的栈顶，即完成入栈操作，这样就有可能出现多个线程同时入栈的情况，因此需要使用CAS操作保证入栈的线程安全，对于出栈的情况也是同理。结构图如下:
   >
   >![Treiber stack](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208091553253.png)
   >
   >由于FutureTask中的队列本质上是一个Treiber栈，那么使用这个队列就只需要一个指向栈顶节点的指针就行了，在FutureTask中，就是`waiters`属性：
   >
   >```java
   >private volatile WaitNode waiters;
   >```
   >
   >事实上，它就是整个单向链表的头节点。

3. 第三轮for循环，进行阻塞等待。要么阻塞特定时间，要么一直阻塞知道被其他线程唤醒（根据get是否带超时等待来调用对应的代码块）。

#### **那为什么会有任务执行线程还没来得及给 `outcome` 赋值这个问题呢？**

或者说为什么要用`putOrderedInt`方法在给`outcome` 赋值后改变`state`的值。

还是要把目光看向`set`函数，我们通过cas操作将`state`赋值为`COMPLETING`然后再给`outcome`赋值后，是使用`putOrderedInt`函数也就是`lazySet`的方式把当前任务状态从 `COMPLETING` 变更为 `NORMAL`。

```java
protected void set(V v) {
    if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
        outcome = v;
        UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // final state
        finishCompletion();
    }
}
```

>putOrderedInt保证写不被重排序，但不强制将store buffer里的数据刷新到内存，也就是说别的线程读state时可能不会立马就能看到state的更新，存在一定延迟性。如果正巧一个线程putOrderedInt完，另一个线程立马要读这个state，没读到更新，主动yield，这样会浪费一点cpu，并发生线程切换的资源消耗。FutureTask的作者应该是认为，这种低概率下产生的成本消耗要优于每次写都要强刷store buffer的固定成本消耗。这就是state被定义为volatile类型变量，却没有选择直接使用state=value的赋值形式的原因吧。

>另一种解释：在当前线程执行run方法的同时，有可能其他线程取消了任务的执行，此时其他线程就可能对state状态进行改写，这也就是我们在设置终止状态的时候用`putOrderedInt`方法，而没有用CAS操作的原因——我们无法确信在设置state前是处于`COMPLETING`中间态还是`INTERRUPTING`中间态。

#### awaitDone方法和get方法都没有加锁，在多个线程同时执行get方法的时候会有线程安全问题吗？

>`awaitDone`方法和get方法都没有加锁，这在多个线程同时执行get方法的时候会不会产生线程安全问题呢？通过查看方法内部的参数我们知道，整个方法内部用的大多数是局部变量，因此不会产生线程安全问题，对于全局的共享变量`waiters`的修改时，也使用了CAS操作，保证了线程安全，而state变量本身是volatile的，保证了读取时的可见性，因此整个方法调用虽然没有加锁，它仍然是线程安全的。

---

以上对于`awaitDone()`的讲解都是以`state`的状态为出发点与讲解的，并没有详细讲解如果线程被中断了的情况，接下来我们就完整的走一遍`get`方法，先从无参的`get`方式开始：

### 无参版本get()方法流程

```java
public V get() throws InterruptedException, ExecutionException {
    int s = state;
    if (s <= COMPLETING)
        s = awaitDone(false, 0L);
    return report(s);
}
```

一个比较完整的流程图如下：

![image-20220809223649524](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208092236691.png)

首先一开始，我们先检测当前线程是否被中断了，这是因为get方法是阻塞式的，如果等待的任务还没有执行完，则调用get方法的线程会被扔到Treiber栈中挂起等待，直到任务执行完毕。但是，如果任务迟迟没有执行完毕，则我们也有可能直接中断在Treiber栈中的线程，以停止等待。当检测到线程被中断后，我们调用了removeWaiter:

```java
private void removeWaiter(WaitNode node) {
    if (node != null) {
        ...
    }
}
```

`removeWaiter`的作用是将参数中的node从等待队列（即Treiber栈）中移除。如果此时线程还没有进入Treiber栈，则 q=null，那么removeWaiter(q)啥也不干。在这之后，我们就直接抛出了`InterruptedException`异常。

接下来就是我在上面分析过的流程，假设当前的任务未完成，所以会走到最后的else语句块中被`LockSupport.park(this)`挂起。那么这个挂起的线程什么时候会被唤醒呢？有两种情况：

1. 任务执行完毕了，在`finishCompletion`方法中会唤醒所有在Treiber栈中等待的线程
2. 等待的线程自身因为被中断等原因而被唤醒。

接下来再来看线程被唤醒以后的情况，此时，线程将回到`for(;;)`循环的开头，继续下一轮循环：

> 首先自然还是检测中断，所不同的是，此时q已经不为null了，因此在有中断发生的情况下，在抛出中断之前，多了一步removeWaiter(q)操作，该操作是将当前线程从等待的Treiber栈中移除，相比入栈操作，这个出栈操作要复杂一点，这取决于节点是否位于栈顶。下面我们来仔细分析这个出栈操作：

```java
private void removeWaiter(WaitNode node) {
    if (node != null) {
        node.thread = null;
        retry:
        for (;;) {          // restart on removeWaiter race
            for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
                s = q.next;
                if (q.thread != null)
                    pred = q;
                else if (pred != null) {
                    pred.next = s;
                    if (pred.thread == null) // check for race
                        continue retry;
                }
                else if (!UNSAFE.compareAndSwapObject(this, waitersOffset, q, s))
                    continue retry;
            }
            break;
        }
    }
}
```

首先，我们把要出栈的WaitNode的thread属性设置为null, 这相当于一个**标记**，是我们后面在waiters链表中定位该节点的依据（通过判断是否为空来定位）。

(1) 要移除的节点就在栈顶

我们先来看看该节点就位于栈顶的情况，这说明在该节点入栈后，并没有别的线程再入栈了。由于一开始我们就将该节点的thread属性设为了null，因此，前面的`q.thread != null` 和 `pred != null`都不满足，我们直接进入到最后一个else if 分支：

```java
else if (!UNSAFE.compareAndSwapObject(this, waitersOffset, q, s))
    continue retry;
```

这一段是栈顶节点出栈的操作，和入栈类似，采用了CAS比较，将栈顶元素设置成原栈顶节点的下一个节点。

值得注意的是，当CAS操作不成功时，程序会回到retry处重来，**但即使CAS操作成功了，程序依旧会遍历完整个链表**，找寻node.thread == null 的节点，并将它们一并从链表中剔除。

(2) 要移除的节点不在栈顶

当要移除的节点不在栈顶时，我们会一直遍历整个链表，直到找到`q.thread == null`的节点，找到之后，我们将进入

```java
else if (pred != null) {
    pred.next = s;
    if (pred.thread == null) // check for race
        continue retry;
}
```

这是**因为节点不在栈顶，则其必然是有前驱节点pred的**，这时，我们只是简单的让前驱节点指向当前节点的下一个节点，从而将目标节点从链表中剔除。

注意，**后面多加的那个if判断是很有必要的**，因为`removeWaiter`方法并没有加锁，所以可能有多个线程在同时执行，WaitNode的两个成员变量`thread`和`next`都被设置成volatile，这保证了它们的可见性，如果我们在这时发现了`pred.thread == null`，那就意味着它已经被另一个线程标记了，将在另一个线程中被拿出`waiters`链表，而我们当前目标节点的原后继节点现在是接在这个pred节点上的，因此，如果pred已经被其他线程标记为要拿出去的节点，我们现在这个线程再继续往后遍历就没有什么意义了，所以这时就调到retry处，从头再遍历。

如果pred节点没有被其他线程标记，那我们就接着往下遍历，直到整个链表遍历完。

至此，将节点从waiters链表中移除的removeWaiter操作我们就分析完了，我们总结一下该方法：

> 在该方法中，会传入一个需要移除的节点，我们会将这个节点的`thread`属性设置成null，以标记该节点。然后无论如何，我们会遍历整个链表，清除那些被标记的节点（只是简单的将节点从链表中剔除）。如果要清除的节点就位于栈顶，则还需要注意重新设置`waiters`的值，指向新的栈顶节点。所以可以看出，虽说removeWaiter方法传入了需要剔除的节点，但是事实上它可能剔除的不止是传入的节点，而是所有已经被标记了的节点，这样不仅清除操作容易了些（不需要专门去定位传入的node在哪里），而且提升了效率（可以同时清除所有已经被标记的节点）。

如果线程不是因为中断被唤醒，那说明是`finishCompletion`唤醒的，意味着任务已经结束了，此时会再次获取当前的`state`状态。最后，等awaitDone函数返回后，get方法返回了`report(s)`，至此结束。

---

### 有参版本get(long timeout, TimeUnit unit)方法流程

```java
public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    if (unit == null)
        throw new NullPointerException();
    int s = state;
    if (s <= COMPLETING && (s = awaitDone(true, unit.toNanos(timeout))) <= COMPLETING)
        throw new TimeoutException();
    return report(s);
}
```

它和上面不带超时时间的get方法很类似，如果指定的超时时间到了，则直接返回，如果返回时，任务还没有进入终止状态，则直接抛出`TimeoutException`异常，否则就像`get()`方法一样，正常的返回执行结果。不同的只是在`awaitDone`方法中多了超时检测：

```java
else if (timed) {
    nanos = deadline - System.nanoTime();
    // 如果需要等待特定时间，则先计算要等待的时间
    // 如果已经超时，则删除对应节点并返回对应的状态
    if (nanos <= 0L) {
        removeWaiter(q);
        return state;
    }
    // 阻塞等待特定时间
    LockSupport.parkNanos(this, nanos);
}
```

---

如果程序运行正常，通常调用 get() 方法，会将当前线程挂起，那谁来唤醒呢？自然是是之前讲到的`run`方法里面设置返回结果（`set`方法）/异常的方法(`setException`方法) 两个方法中都会调用 `finishCompletion` 方法。

### 核心方法 - cancel(boolean mayInterruptIfRunning)

```java
/**
 * 1.判断任务当前执行状态，如果任务状态不为 NEW，则说明任务或者已经执行完成，或者执行异常，不能被取消，直接返回 false 表示执行失败。
 * 2.判断需要中断任务执行线程
 *  -- 把任务状态从 NEW 转化到 INTERRUPTING。这是个中间状态。
 *  -- 中断任务执行线程。
 *  -- 修改任务状态为INTERRUPTED。
 * 3.如果不需要中断任务执行线程，直接把任务状态从 NEW 转化为 CANCELLED。如果转化失败则返回 false 表示取消失败。
 * 4.调用 finishCompletion()。
 */
public boolean cancel(boolean mayInterruptIfRunning) {
    // 如果任务已经结束，则直接返回 false
    if (!(state == NEW && UNSAFE.compareAndSwapInt(this, stateOffset, NEW, mayInterruptIfRunning ? INTERRUPTING : CANCELLED))) {
        return false;
    }
    try {
        // // 如果需要中断任务执行线程
        if (mayInterruptIfRunning) {
            try {
                Thread t = runner;
                // 中断任务执行线程
                if (t != null) {
                    t.interrupt();
                }
            } finally {
                // 修改状态为 INTERRUPTED
                UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
            }
        }
    } finally {
        finishCompletion();
    }
    return true;
}
```

关于cancel方法可以参考一下[Java多线程（3）：取消正在运行的任务](https://segmentfault.com/a/1190000007961347)这篇文章。

>关于cancel方法，这里要补充说几点：
>首先有以下三种情况之一的，cancel操作一定是失败的：
>
>1. 任务已经执行完成了
>2. 任务已经被取消过了
>3. 任务因为某种原因不能被取消
>
>其它情况下，cancel操作将返回true。值得注意的是，**cancel操作返回true并不代表任务真的就是被取消了**，这取决于发动cancel状态时，任务所处的状态：
>
>1. 如果发起cancel时任务还没有开始运行，则随后任务就不会被执行；
>2. 如果发起cancel时任务已经在运行了，则这时就需要看`mayInterruptIfRunning`参数了：
>   - 如果`mayInterruptIfRunning` 为true, 则当前在执行的任务会被中断
>   - 如果`mayInterruptIfRunning` 为false, 则可以允许正在执行的任务继续运行，直到它执行完

我们来看看FutureTask是怎么实现cancel方法的这几个规范的:

首先，对于“任务已经执行完成了或者任务已经被取消过了，则cancel操作一定是失败的(返回false)”这两条，是通过简单的判断state值是否为`NEW`实现的，因为我们前面说过了，只要state不为NEW，说明任务已经执行完毕了。从代码中可以看出，只要state不为NEW，则直接返回false。

如果state还是NEW状态，我们再往下看：

```java
UNSAFE.compareAndSwapInt(this, stateOffset, NEW, mayInterruptIfRunning ? INTERRUPTING : CANCELLED)
```

这一段是根据`mayInterruptIfRunning`的值将state的状态由`NEW`设置成`INTERRUPTING`或者`CANCELLED`，当这一操作也成功之后，就可以执行后面的try语句了。

```java
    try {
        // // 如果需要中断任务执行线程
        if (mayInterruptIfRunning) {
            try {
                Thread t = runner;
                // 中断任务执行线程
                if (t != null) {
                    t.interrupt();
                }
            } finally {
                // 修改状态为 INTERRUPTED
                UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
            }
        }
    }
```

从中可以得出cancel方法实际上完成以下两种状态转换之一:

1. `NEW -> CANCELLED `(对应于`mayInterruptIfRunning=false`)
2. `NEW -> INTERRUPTING -> INTERRUPTED` (对应于`mayInterruptIfRunning=true`)

**对于第一条路径**(对应于`mayInterruptIfRunning=false`)，虽说cancel方法最终返回了true，但它只是简单的把state状态设为CANCELLED，不会走try语句块中的if方法，所以并不会中断线程的执行。**但是这样带来的后果是，任务即使执行完毕了，也无法设置任务的执行结果**，因为前面分析run方法的时候我们知道，设置任务结果有一个中间态，而这个中间态的设置，是以当前state状态为NEW为前提的。

**对于第二条路径**(对应于`mayInterruptIfRunning=true`)，则会中断执行任务的线程，我们在倒回上面的run方法看看：

>虽然第二条路径中断了当前正在执行的线程，但是，响不响应这个中断是由执行任务的线程自己决定的，更具体的说，这取决于`c.call()`方法内部是否对中断进行了响应，是否将中断异常抛出。
>
>那call方法中是怎么处理中断的呢？从上面的代码中可以看出，catch语句处理了所有的`Throwable`的异常，这自然也包括了中断异常。
>
>然而，值得一提的是，即使这里进入了`catch (Throwable ex){}`代码块，`setException(ex)`的操作一定是失败的，因为在我们取消任务执行的线程中，我们已经先把state状态设为`INTERRUPTING`了，而`setException(ex)`的操作要求设置前线程的状态为`NEW`。所以这里响应**cancel方法所造成的中断**最大的意义**不是为了对中断进行处理，而是简单的停止任务线程的执行，节省CPU资源。**
>
>那既然这个`setException(ex)`的操作一定是失败的，那放在这里有什么用呢？事实上，这个`setException(ex)`是用来处理任务自己在正常执行过程中产生的异常的，在我们没有主动去cancel任务时，任务的state状态在执行过程中就会始终是`NEW`，如果任务此时自己发生了异常，则这个异常就会被`setException(ex)`方法成功的记录到`outcome`中。
>
>反正无论如何，run方法最终都会进入finally块，而这时候它会发现`s >= INTERRUPTING`，如果检测发现`s = INTERRUPTING`，说明cancel方法还没有执行到中断当前线程的地方，那就等待它将state状态设置成`INTERRUPTED`。到这里，对cancel方法的分析就和上面对run方法中的`handlePossibleCancellationInterrupt()`自旋等待`state`状态转变的分析对接上了。
>
>cancel方法到这里就分析完了，如果你一条条的去对照Future接口对于cancel方法的规范，它每一条都是实现了的，而它实现的核心机理，就是对state的当前状态的判断和设置。**由此可见，state属性是贯穿整个FutureTask的最核心的属性。**
