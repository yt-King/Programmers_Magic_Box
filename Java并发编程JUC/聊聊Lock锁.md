# 聊聊Lock锁

![image-20220414102700078](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/old/4/202204141027306.png)

## 1.简介

lock是一个接口，他有很多种实现，常见的有可重入锁（ReentrantLock）和读写锁（Read、WriteLock），在JUC下的一个子包locks包里。Lock需要显式地获取和释放锁，虽然不如隐式获取锁的便捷，但有了锁获取与释放的**可操作性**、**可中断的获取锁及超时获取锁等同步特性。**

![image-20220411135241875](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/old/4/202204111352168.png)

### 1.1-方法简介

- void lock()：获取锁，若锁不可用，则当前线程将会阻塞，直到获得锁；

  > 这是最简单也是最普通的获取锁的方式，他不像synchronized一样在异常时自动释放锁，所以必须在finally块中释放确保发生异常时能够正常释放锁。需要注意的是该方法不能被中断，所以如果陷入死锁的状况lock()会一直等待下去。

- void lockInterruptibly() throws InterruptedException：获取可中断锁，当被中断或获取到锁才返回，若锁不可用，则当前线程被阻塞，直到获取锁或被中断；

  >与lock()不同的是lockInterruptibly()支持在等待锁的途中中断，或者可有说lockInterruptibly对Thread.interrupt方法的响应优先级更高，当lockInterruptibly在等待锁的途中调用中断方法这是lockInterruptibly不会获取锁而是抛出一个InterruptedException。而lock()则是优先获取锁才会响应中断，在lock等待的途中即使执行interrupt也没有用，必须等到lock获取锁以后才会响应中断

- boolean tryLock()：尝试获取锁，并立即返回；true：获取锁成功；false：获取锁失败；

- boolean tryLock(long time, TimeUnit unit) throws InterruptedException：尝试在指定的超时时间获取锁，当获取到锁时返回true；当超时或被中断时返回false；

- Condition newCondition()：返回一个和锁绑定的条件队列；在等待条件之前线程必须先获取当前锁，同时await()方法会原子地释放锁，并在返回之前重新获取到锁；

- void unlock()：释放锁；

ReentrantLock和ReadWriteLock是此接口的实现：

![image-20220411140005843](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/old/4/202204111400997.png)

## 2.Lock锁的意义

1. 对比于更加古老的synchronized锁，lock锁的操作更加的灵活，Lock提供了更丰富的锁操作
2. 通常来说锁的作用是提供多线程对共享资源的独占访问，**一次只能由一个线程获得锁，只有获得锁的线程拥有对共享资源的访问权限**，但是有些所可以做到对共享资源的并发访问，比如读写锁可以并发的读共享资源。

## 3.用法

下面的代码是一个基本的示例，声明一个Lock锁的实例对象，调用lock方法加锁，与synchronized自动解锁所不同的是Lock需要手动释放锁，正是如此是的lock锁有了很强大的灵活性。

```java
Lock lock = new ReentrantLock();
lock.lock();
try{
  
}finally {
  lock.unlock();
}
```

### 3.1-Condition 的用法

关键字 synchronized 与 wait()/notify()这两个方法一起使用可以实现等待/通知模式， **Lock 锁的 newContition()方法返回的 Condition 对象也可以实现等待/通知模式**。 用 notify()通知时，JVM 会随机唤醒某个等待的线程， 而使用 Condition 类可以进行选择性通知， Condition 比较常用的两个方法：

- **await()**：会使当前线程等待,同时会释放锁,当等到其他线程调用`signal()`方法时,此时这个沉睡线程会重新获得锁并继续执行代码（在哪里沉睡就在哪里唤醒）。
- **signal()**：用于唤醒一个等待的线程。

**需要注意的是**在调用 Condition 的 await()/signal()方法前，也需要线程持有相关 的 Lock 锁，调用 await()后线程会释放这个锁，在调用singal()方法后会从当前 Condition对象的等待队列中，唤醒一个线程，后被唤醒的线程开始尝试去获得锁， 一旦成功获得锁就继续往下执行。

```java
class Share {
//通过两个线程对number进行加减操作，一个线程当number == 0时 对number++，另外一个线程当number == 1时对number--
    private Integer number = 0;

    private ReentrantLock lock = new ReentrantLock();

    private Condition newCondition = lock.newCondition();

    // number++
    public void incr() {
        try {
            lock.lock(); // 加锁
            while (number != 0) {
                newCondition.await();//沉睡
            }
            number++;
            System.out.println(Thread.currentThread().getName() + "::" + number);
            newCondition.signal(); //唤醒另一个沉睡的线程 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    // number--
    public void decr() {
        try {
            lock.lock();
            while (number != 1) {
                newCondition.await();
            }
            number--;
            System.out.println(Thread.currentThread().getName() + "::" + number);
            newCondition.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}

public class LockDemo2 {
    public static void main(String[] args) {
        Share share = new Share();

        new Thread(()->{
            for (int i=0;i<=10;i++){
                share.incr();
            }
        },"AA").start();

        new Thread(()->{
            for (int i=0;i<=10;i++){
                share.decr();
            }
        },"BB").start();
        /**out:
         * AA::1
         * BB::0
         * AA::1
         * BB::0
         * .....
         */     
    }
}
```

## 4.ReentrantLock（重入锁）

### 1）概览

ReentrantLock实现了Lock接口，表示重入锁。是线程在获得锁之后，不需要阻塞就可以再次获取锁，然后直接关联一次计数器增加重入次数，这就意味着一个线程能够对一个临界资源重复加锁。以下是它与Synchronized的一些对比：

|            | ReentrantLock                  | Synchronized     |
| ---------- | ------------------------------ | ---------------- |
| 锁实现机制 | AQS实现                        | 监视器模式实现   |
| 灵活性     | 支持响应中断、超时、尝试获取锁 | 不灵活           |
| 释放形式   | 必须显示调用unlock()进行解锁   | 自动释放监视器   |
| 锁类型     | 必须显示调用unlock()进行解锁   | 自动释放监视器   |
| 条件队列   | 可关联多个条件队列             | 关联一个条件队列 |
| 可重入     | 支持                           | 支持             |

### 2）什么是AQS

可以看到ReentrantLock的实现是依靠AQS来实现的，那么什么是AQS呢？

AQS全称`AbstractQueuedSynchronizer`，即抽象的队列同步器，是一种用来构建锁和同步器的框架。他的核心思想就是实现同步，当一个共享资源被请求时是处于空闲状态的，那么AQS就会将当前请求资源的线程设置为状态有效的线程，并且给请求到的资源加锁。而那些请求已经加了锁的资源而失败的线程或者说在刚刚争用资源时失败了的线程，则会通过一套线程阻塞等待以及被唤醒时锁分配的机制来进行管理。在AQS中是通过一个变体的 CLH 队列来实现的。

![image-20220418091647330](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/old/4/202204180916528.png)

AQS 中会将竞争共享资源失败的线程及其状态信息封装到一个node中加入到一个变体的 CLH 队列中，接下来会不断自旋（cas）尝试获取锁，条件是当前节点是头结点的直接后继才会尝试。失败一定次数后则阻塞自己等待被唤醒。而持有锁的线程释放锁时会唤醒后继的节点中的线程。

![image-20220418092533237](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/old/4/202204180925289.png)

### 3）什么是CLH队列

参考：https://juejin.cn/post/6896278031317663751

CLH：Craig、Landin and Hagersten 队列，是 **单向链表实现的队列**。申请线程只在本地变量上自旋，**它不断轮询前驱的状态**，如果发现 **前驱节点释放了锁就结束自旋**

![image-20220418092907692](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/old/4/202204180929770.png)

CLH 队列具有以下特点：

1. CLH 队列是一个单向链表，有着 FIFO 先进先出的队列特性
2. 通过 tail 尾节点（原子引用）来构建队列，总是指向最后一个节点
3. 未获得锁节点会进行自旋，而不是切换线程状态
4. 并发高时性能较差，因为会有大量的为获得锁的线程不断轮询前驱节点的状态，会造成一定的资源浪费

AQS 中的队列是 CLH 变体的虚拟双向队列，通过将每条请求共享资源的线程封装成一个节点来实现锁的分配

![image-20220418092941448](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/old/4/202204180929541.png)

相比于 CLH 队列而言，AQS 中的 CLH 变体等待队列拥有以下特性

1. AQS 中队列是个双向链表，也具有 FIFO 先进先出的特性
2. 通过 Head、Tail 头尾两个节点来组成队列结构，通过 volatile 修饰保证可见性
3. Head 指向节点为已获得锁的节点，是一个虚拟节点，节点本身不持有具体线程
4. 获取不到同步状态，会将节点进行自旋获取锁，自旋一定次数失败后会将线程阻塞，相对于 CLH 队列性能较好，并发高时性能不会有太大的影响

### 4）AQS中node的组成

node是用来存放线程及其附带的一些信息用的，一些主要的属性如下

>int waitStatus ：节点状态 
>
>volatile Node prev ：当前节点中的线程的前驱节点 
>
>volatile Node next ：当前节点中的线程的后继节点 
>
>volatile Thread thread：当前节点中的线程 
>
>Node nextWaiter：在同步队列里用来标识节点是独占锁节点还是共享锁节点，在条件队列里代表条件条件队列的下一个节点

同时waitStatus涉及到4个可选的状态：

```java
 		/** 表示线程已取消 */
        static final int CANCELLED =  1;
        /** 表示线程等待唤醒 */
        static final int SIGNAL    = -1;
        /** 表示线程等待获取同步锁 */
        static final int CONDITION = -2;
        /** 表示共享模式下无条件传播 */
        static final int PROPAGATE = -3;
```

>**CANCELLED**：代表取消状态，该线程节点已释放（超时、中断），已取消的节点不会再阻塞
>
>**SIGNAL**：代表通知状态，这个状态下的节点如果被唤醒，就有义务去唤醒它的后继节点。这也就是为什么一个节点的线程阻塞之前必须保证前一个节点是 SIGNAL 状态，因为这样才能保证前一个节点可以去唤醒他的后继节点。
>
>**CONDITION** ：代表条件等待状态，条件等待队列里每一个节点都是这个状态，它的节点被移到同步队列之后状态会修改为 0。
>
>**PROPAGATE**：代表传播状态，在一些地方用于修复 bug 和提高性能，减少不必要的循环。
>
>**ps:** 如果 waiterStatus 的值为 **0**，有两种情况：1、节点状态值没有被更新过（同步队列里最后一个节点的状态）；2、在唤醒线程之前头节点状态会被被修改为 0。
>
>**tips：** 负值表示结点处于有效等待状态，而正值表示结点已被取消。所以源码中很多地方用>0、<0来判断结点的状态是否正常。

![image-20220419160915290](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/old/4/202204191609071.png)

### 5）AQS的两种队列

AQS 总共有两种队列，从node的构造方式中也可以看出，一种是用于同步队列，代表的是正常的获取释放锁的队列；另外一种是条件队列，代表的是每个 ConditionObject 对应的队列。两者都是FIFO（先进先出）队列。

![image-20220419161335679](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/old/4/202204191613016.png)

#### 同步队列

同步队列是一个双向列表，其内的节点有两种，一种是独占队列的节点，一种是共享队列的节点，两者的区别是独占的节点的nextWaiter 指向null，共享锁的nextWaiter 指向一个静态的SHARED 节点。两种队列都包括head节点和tail节点。head节点是一个空的头节点，主要用作后续的调度。

![image-20220419210611913](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/old/4/202204192106008.png)

#### 条件队列

条件队列是单链，它没有空的头节点，每个节点都有对应的线程。条件队列头节点和尾节点的指针分别是 firstWaiter 和 lastWaiter 。

![image-20220419211214815](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/old/4/202204192112873.png)

### 6）Condition接口

上面说到了条件队列，条件等待和条件唤醒功能一般都是 ReentrantLock 与 AQS 的内部类 配合实现的。一个 ReentrantLock 可以创建多个 ConditionObject 实例，每个实例对应一个条件队列，以保证每个实例都有自己的等待唤醒逻辑，不会相互影响。条件队列里的线程对应的节点被唤醒时会被放到 ReentrantLock 的同步队列里，让同步队列去完成唤醒和重新尝试获取锁的工作。可以理解为**条件队列是依赖同步队列的，它们协同才能完成条件等待和条件唤醒功能。**

而在AQS中ConditionObject 是通过实现Condition接口来完成的，类似Object的wait()、wait(long timeout)、notify()以及notifyAll()的方法结合synchronized内置锁可以实现可以实现等待/通知模式，Condition接口定义了await()、awaitNanos(long)、signal()、signalAll()等方法，配合对象锁实例实现等待/通知功能。

![image-20220419213817740](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/old/4/202204192138853.png)

## 5.AQS源码分析

### 1）独占锁

**独占模式**，即只允许一个线程获取同步状态，当这个线程还没有释放同步状态时，其他线程是获取不了的，只能加入到同步队列，进行等待。

首先调用的是acquire方法，两种结果：1. 成功，则方法结束返回，2. 失败，先调用addWaiter()然后在调用acquireQueued()方法

#### acquire(int arg)

```java
//以独占模式获取，忽略中断。通过调用至少一次 tryAcquire 来实现，成功返回。否则线程排队，可能重复阻塞和解除阻塞，调用 tryAcquire 直到成功。此方法可用于实现方法 Lock.lock。
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))// 这里 Node.EXCLUSIVE 的值是 null,Node.EXCLUSIVE互斥模式、Node.SHARED共享模式
        selfInterrupt();
}
```

tryAcquire方法，由具体的锁来实现的，这个方法主要是尝试获取锁，获取成功就不会再执行其他代码了。

#### tryAcquire(int arg)

```java
    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }
```

获取锁失败的情况下则将进行入队操作，即addWaiter(Node.EXCLUSIVE),这里的Node.EXCLUSIVE是空，用于构造nextWaiter，这是在独占锁的模式下，共享锁的话则使用Node.SHARED。之前说到过同步队列中的节点有两种，一种是共享模式，队列中的每个节点都指向一个静态的SHARED 节点，即下图中的SHARED，而独占队列每个节点都指向的是空，即EXCLUSIVE。

![image-20220422094202664](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/old/4/202204220942907.png)

接下来来看一下addWaiter的源码

#### addWaiter(Node mode)

```java
    private Node addWaiter(Node mode) {//mode = Node.EXCLUSIVE = null
        Node node = new Node(Thread.currentThread(), mode);
        // Try the fast path of enq; backup to full enq on failure
        Node pred = this.tail;
        if (pred != null) {// 如果尾节点不为空，就把节点放在尾节点后面并设置为新的尾节点
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {// 尝试把节点设置为新的尾节点
                pred.next = node;
                return node;
            }
        }
        enq(node);
        return node;
    }
```

设置失败的话会进入一个方法`enq()`。如果当前没有尾节点，则会直接进入到`enq()`方法，**用于完成对同步队列的头结点初始化工作以及CAS操作失败的重试**

#### enq(final Node node)

```java
private Node enq(final Node node) {
        for (;;) {
            Node t = tail;
            if (t == null) {  // 如果尾节点为空，那么队列也为空，新建一个头节点，让 head 和 tail 都指向它
                if (compareAndSetHead(new Node()))
                    this.tail = this.head;
            } else {// 如果有尾节点，把传入的节点放入队尾
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }
```

该方法先判断是否有尾节点，没有的话则说明需要初始化（因为没有尾节点也就意味着没有头节点），通过CAS新增一个空的头节点，然后尾指针也指向这个节点。如果有尾节点的话就直接将这个节点加在尾节点后面，然后通过CAS将尾指针指向新的尾节点。

![image-20220421164553026](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/old/4/202204211645138.png)

`addWaiter()`方法结束后，接下来就是方法`acquireQueued()`，用于已在队列中的线程以独占且不间断模式获取state状态，直到获取锁后返回

#### acquireQueued(final Node node, int arg)

```java
 final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();// 核验并获取前一个节点，如果前一个节点不存在，直接抛异常
                if (p == head && tryAcquire(arg)) {// 如果前一个节点就是头节点，让这个节点的线程尝试获取锁
                    setHead(node);//获取锁成功后把当前节点设置为头节点,j就是变成一个空节点，因为已经获取到锁了
                    p.next = null; // 将之前头节点的 next 指针置空，后面 GC 时会回收p
                    failed = false;
                    return interrupted;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&// 若没有获取锁则判断是否应该阻塞当前线程（核心是判断并修正前面节点的 waitStatus）
                    parkAndCheckInterrupt())// 阻塞当前线程、返回并清除中断标记
                    interrupted = true;
            }
        } finally {
            //将当前节点设置为取消状态
            if (failed)
                cancelAcquire(node);
        }
    }
```

acquireQueued()方法的流程大致如下：

![image-20220422114524482](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/old/4/202204221145696.png)

获取锁的代码已经讲完了，接下来看看前节点不是头节点时判断是否需要阻塞的方法shouldParkAfterFailedAcquire（），先来回顾一下节点的waitStatus的作用

>**CANCELLED**：代表取消状态，该线程节点已释放（超时、中断），已取消的节点不会再阻塞
>
>**SIGNAL**：代表通知状态，这个状态下的节点如果被唤醒，就有义务去唤醒它的后继节点。这也就是为什么一个节点的线程阻塞之前必须保证前一个节点是 SIGNAL 状态，因为这样才能保证前一个节点可以去唤醒他的后继节点。
>
>**CONDITION** ：代表条件等待状态，条件等待队列里每一个节点都是这个状态，它的节点被移到同步队列之后状态会修改为 0。
>
>**PROPAGATE**：代表传播状态，在一些地方用于修复 bug 和提高性能，减少不必要的循环。
>
>**ps:** 如果 waiterStatus 的值为 **0**，有两种情况：1、节点状态值没有被更新过（同步队列里最后一个节点的状态）；2、在唤醒线程之前头节点状态会被被修改为 0。
>
>**tips：** 负值表示结点处于有效等待状态，而正值表示结点已被取消。所以源码中很多地方用>0、<0来判断结点的状态是否正常。

#### shouldParkAfterFailedAcquire(Node pred, Node node)

```java
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL)
            /* 如果已经告诉前驱拿完号后通知自己一下，就可以安心等待了
             * This node has already set status asking a release
             * to signal it, so it can safely park.
             */
            return true;
        if (ws > 0) {
            /* 如果前面的节点状态为取消（CANCEL值为1）,就一直向前查找，直到找到状态不为取消的节点，把它放在这个节点后面
             * 注意：那些放弃的结点，由于被自己「加塞」到它们前边，它们相当于形成一个无引用链，稍后就会被 GC 回收
             * Predecessor was cancelled. Skip over predecessors and
             * indicate retry.
             */
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            /* 如果前驱正常，那就把前驱的状态设置成 SIGNAL，告诉它拿完号后通知自己一下。
             * 有可能失败，前驱说不定刚刚释放完
             * waitStatus must be 0 or PROPAGATE.  Indicate that we
             * need a signal, but don't park yet.  Caller will need to
             * retry to make sure it cannot acquire before parking.
             */
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }
```

>阻塞判断`shouldParkAfterFailedAcquire`，什么情况下应该阻塞线程？什么情况下应该再给一次抢锁的机会？3种情况：
>
>1. 判断node的前驱节点`waitStatus=SIGNAL`（这里和CLH锁自旋检测前驱状态一样），node的线程就放心阻塞，因为会在下次某个线程释放锁后，被node前驱唤醒。
>2. node的前驱节点`waitStatus>0`，`waitStatus`只有`CANCELLED>0`，所以node前驱节点被取消了，剔除取消节点，给node链接一个正常的前驱，然后再自旋一次。
>3. node的前驱节点`waitStatus=0`或者`waitStatus=PROPAGATE`（`waitStatus=PROPAGATE`是共享锁传播的情况暂时不考虑），此时将node前驱节点`waitStatus`设置为`SIGNAL`，然后再给一次自旋的机会。

除了第一种状态，后面两种状态都会返回 false，后面经过循环再次进去这个方法。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208021323613.png" alt="img" style="zoom:50%;" />

当shouldParkAfterFailedAcquire返回 true 时，就会进入下一个方法parkAndCheckInterrupt()：

#### parkAndCheckInterrupt()

```java
    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this); // 阻塞当前线程，通过LockSupport类调用 Unsafe 这个类的 park() 方法进行阻塞
        return Thread.interrupted();// 返回并清除当前线程中断状态
    }
```

到此加锁的过程就结束了，接下来时解锁的部分-> release()

#### release(int arg)

```java
    public final boolean release(int arg) {
        if (tryRelease(arg)) {// 尝试释放锁，如果成功则唤醒后继节点的线程
            //tryRelease()跟tryAcquire()一样实现都是由具体的锁来实现的。
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);// 唤醒后面节点中第一个非取消状态节点的线程
            return true;
        }
        return false;
    }
```

在方法的开始做了一个判断：

1、如果当前线程释放锁失败，就直接返回了；

2、如果释放锁成功，那么就会接着判断头节点是否为空和头节点 waitStatus 是否不为 0 。

这里判断头节点状态是一个比较重要的点。为什么头节点的状态一定不能为 0 呢？从后面要讲到源码可以知道，在唤醒头节点的后继之前会做一个将头节点状态置为 0 的操作（虽然这个操作不一定成功）。如果头节点的状态为 0 了，说明正在释放后继节点，这时候也就不再需要释放了，直接返回 true。

头节点状态判断之后，就会进入到释放后继节点这一步，也就是`unparkSuccessor()`方法：

#### unparkSuccessor(Node node)

```java
    private void unparkSuccessor(Node node) {
        /*
         * If status is negative (i.e., possibly needing signal) try
         * to clear in anticipation of signalling.  It is OK if this
         * fails or if status is changed by waiting thread.
         */
        int ws = node.waitStatus;// 获取头节点（head）的状态
        if (ws < 0)
            compareAndSetWaitStatus(node, ws, 0); //通过CAS操作尝试将头节点状态置0

        /*
         * Thread to unpark is held in successor, which is normally
         * just the next node.  But if cancelled or apparently null,
         * traverse backwards from tail to find the actual
         * non-cancelled successor.
         */
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {// 如果后面这个节点状态为取消，那么就找到一个位置最靠前的非取消状态的节点
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)
                if (t.waitStatus <= 0)
                    s = t;
        }
        if (s != null)
            LockSupport.unpark(s.thread);// 唤醒符合条件的后继节点，通过LockSupport类调用 Unsafe 这个类的 unpark() 方法进行唤醒
    }
```

#### 总结：

**在获取同步状态时，AQS维护一个同步队列，获取同步状态失败的线程会加入到队列中进行自旋；移除队列（或停止自旋）的条件是前驱节点是头结点并且成功获得了同步状态。在释放同步状态时，同步器会调用unparkSuccessor()方法唤醒后继节点。**

### 2）共享锁

了解了独占锁的原理之后接下来再来看一下共享锁的源码

#### acquireShared()

```java
public final void acquireShared(int arg) {
	if (tryAcquireShared(arg) < 0)
		doAcquireShared(arg);
}
```

这个方法就两行：第一行判断尝试获取锁的返回值是否小于0，这里的返回值是指当前信号量减去传入的信号量的结果，小于0就代表当前信号量不足，获取锁失败，这时候就需要 AQS 接管了；第二行是执行阻塞和唤醒后获取锁的方法。

#### doAcquireShared()

```java
private void doAcquireShared(int arg) {
	final Node node = addWaiter(Node.SHARED); // 1、共享节点入队
	boolean failed = true;
	try {
		boolean interrupted = false;
		for (;;) {
			final Node p = node.predecessor();
			if (p == head) {
        int r = tryAcquireShared(arg); // 2、尝试获取共享锁（相当于尝试扣减信号量）
        if (r >= 0) {
          setHeadAndPropagate(node, r); // 3、设置头节点并且做一些判断，符合条件会唤醒下一个节点
          p.next = null; // help GC
          if (interrupted)
            selfInterrupt();
          failed = false;
          return;
        }
      }
      if (shouldParkAfterFailedAcquire(p, node)
        parkAndCheckInterrupt()) // 线程会阻塞在这个位置，被唤醒后再继续循环
        interrupted = true;
    }
  } finally {
    if (failed)
      cancelAcquire(node);
  }
}
```

这个方法大部分代码与前面讲的 `acquireQueued()` 方法是相同的。这里着重讲不同的地方。

首先是标记的第 1 处 `final Node node = addWaiter(Node.SHARED);` 这里参数是传的静态常量 SHARED ，这个值会赋给新生成节点的 nextWaiter 。正如前面说的，通过 nextWaiter 的值我们就能判断这个节点是独占锁的节点还是共享锁的节点。

然后是标记为 2 的这行代码 `int r = tryAcquireShared(arg);` 这代表尝试获取锁之后的值，如果剩下的信号量不为负，那就代表获取锁成功了，就会进入到标识为 3 的这个方法。

下面我们来看看标记为 3 的 `setHeadAndPropagate(node, r)` 方法的源码：

```java
JAVA
private void setHeadAndPropagate(Node node, int propagate) {
  Node h = head; // Record old head for check below
  setHead(node); // 1
  if (propagate > 0 || h == null || h.waitStatus < 0 ||  // 2
      (h = head) == null || h.waitStatus < 0) {
    Node s = node.next;
    if (s == null || s.isShared())
      doReleaseShared();
  }
}
```

这个方法代码也不多，主要是两块内容：第一个是 `setHead(node)` 方法，这个方法让第二个节点变成头节点，置空之前头节点的部分指针；第二块内容做了大量的判断，然后如果符合条件会执行 `doReleaseShared();`，这个方法也是后面重点要讲的唤醒共享锁同步队列线程的方法。

这里详细讲一下第二块内容做的这些判断：

- `propagate > 0` ：propagate 是传入的参数，代表获取锁成功之后剩余的信号量，如果为正，说明其他线程也可能获取到锁，就会执行后面的唤醒逻辑；
- `h == null`：之前的头节点是空，这里代表异常情况，也需要唤醒线程避免后面的线程都不会被唤醒的情况出现；
- `h.waitStatus < 0`：这里代表保存旧的头节点和设置新的头节点的间隙又有新的节点将会或已经被阴塞了，这个情况也需要执行唤醒让线程重新尝试获取锁；
- `(h = head) == null `：这里代表新的头节点异常，与旧头节点异常一样需要做唤醒操作；
- `h.waitStatus < 0`：这个代表设置新节点成功到做这个判断的间隙又有新节点将会或已经被阻塞了，同样需要唤醒；
- `s == null`：这个代表队列只有头节点或者发生异常，统一做唤醒操作，主要还是处理异常情况；
- `s.isShared()`：这个判断代表只要是共享节点并且满足唤醒条件都会执行唤醒。

这个方法里实现了链式唤醒：当一个线程被唤醒并获取到锁，如果满足条件就会去唤醒其他线程来尝试获取锁，这种唤醒能一直传递下去，使得共享锁获取锁的效率大大提升。

#### releaseShared()

接着讲另一个重要的方法`releaseShared()`，下面是源码：

```java
JAVA
public final boolean releaseShared(int arg) {
  if (tryReleaseShared(arg)) {
    doReleaseShared();
    return true;
  }
  return false;
}
```

这个方法除了返回值，核心代码也只有两行：第一行代表尝试释放锁，释放失败就直接返回了，释放成功就会执行唤醒后继节点线程操作；第二行就是具体的唤醒线程的方法；

下面是 `doReleaseShared()` 方法的源码：

```java
JAVA
private void doReleaseShared() {
  for (;;) {
    Node h = head;
    if (h != null && h != tail) {
      int ws = h.waitStatus;
      if (ws == Node.SIGNAL) {
        if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
          continue;            // loop to recheck cases
        unparkSuccessor(h);
      }
      else if (ws == 0 &&
               !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
        continue;                // loop on failed CAS
    }
    if (h == head)               // loop if head changed
      break;
  }
}
```

在这个方法的循环里对头节点做了大量的判断，头节点的状态满足条件才会执行唤醒操作，我们挨个来看看这些判断的作用：

- `ws == Node.SIGNAL`：从前面的源码可以知道，一个节点阻塞前它前面的节点的 waiteStatus 必须为 SIGNAL ，如果在做唤醒操作这个值就会变，做这个判断主要是确保当前队列没有其他线程在做唤醒操作；
- `!compareAndSetWaitStatus(h, Node.SIGNAL, 0)`：尝试将头节点 waiteStatus 值设置为 0，代表这个 FIFO 队列正在做唤醒操作，注意与独占锁不一样，这里要确保这个值是设置成功的；
- `ws == 0 &&!compareAndSetWaitStatus(h, 0, Node.PROPAGATE)`：这个判断主要是为了确保前面获取到的头节点 waiteStatus 的值与实时获取的头节点 waiteStatus 值相同。什么样的情况下前面做一个判断的间隙这里头节点的状态就变了呢？那就是有新节点入队放在头节点后面并准备阻塞或者已经阻塞了，由于是否阻塞有不确定性，这里就会重新循环获取最新的状态，避免同时做阻塞和唤醒的动作。

### 3）条件等待和条件通知

条件等待和条件通知功能主要由 AQS 内部类 ConditionObject 的两个重要的方法： `await()` 和 `signal()` 来实现。

#### await()

`await()` 方法正如字面意思一样，就是等待。它与 Object 对象的 `wait()` 方法不同的是，Object 的 `wait()` 方法调用后，任何对象调用 `notify` 都能唤醒它，而 `await()` 方法调用后，只有调用 `await()` 方法的实例调用的 `notify()` 方法才能唤醒它，因此 `await()` 是一个条件等待方法。

方法的源码如下：

```java
JAVA
public final void await() throws InterruptedException {
  if (Thread.interrupted())
    throw new InterruptedException();
  Node node = addConditionWaiter(); // 生成一个新的节点添加到条件队列
  int savedState = fullyRelease(node); // 调用同步队列释放锁的方法
  int interruptMode = 0;
  while (!isOnSyncQueue(node)) { // 节点是否已经转移到了同步队列中
    LockSupport.park(this); // 没有被转移就阻塞线程
    if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) // 根据线程中断状态设置 interruptMode 
      break;
  }
  if (acquireQueued(node, savedState) && interruptMode != THROW_IE) // 调用同步队列阻塞和尝试获取锁的方法
    interruptMode = REINTERRUPT;
  if (node.nextWaiter != null) // clean up if cancelled
    unlinkCancelledWaiters(); // 把节点等待状态（waitStatus）不为 CONDITION 的节点移除
  if (interruptMode != 0)
    reportInterruptAfterWait(interruptMode); // 根据不同的中断模式决定是抛出中断异常还是重新标记中断
}
```

需要特别注意的是这个方法是支持中断的，而且方法中很多判断和方法都是与中断有关的，具体哪些地方什么情况会抛出中断异常这里不详细说，这个不是本文的重点。

首先讲讲 `addConditionWaiter()` 这个方法，源码如下：

```java
JAVA
private Node addConditionWaiter() {
  Node t = lastWaiter;
  // If lastWaiter is cancelled, clean out.
  if (t != null && t.waitStatus != Node.CONDITION) {
    unlinkCancelledWaiters(); // 把所有节点等待状态不为 CONDITION 的节点移除
    t = lastWaiter;
  }
  Node node = new Node(Thread.currentThread(), Node.CONDITION); // 新建一个条件队列的节点
  if (t == null)
    firstWaiter = node;
  else
    t.nextWaiter = node;
  lastWaiter = node;
  return node;
}
```

这个方法做了两件事：

1、如果队列不为空而且最后一个节点等待状态异常，就做一个全队列扫描，去掉异常的节点；

2、把节点入队，这里要做一个判断：如果队列为空，把新节点作为头节点，如果队列非空，把新节点放在队尾。

接着是 `isOnSyncQueue()` 方法，源码如下：

```java
JAVA
final boolean isOnSyncQueue(Node node) {
  if (node.waitStatus == Node.CONDITION || node.prev == null)
    return false;
  if (node.next != null) // If has successor, it must be on queue
    return true;
  return findNodeFromTail(node);
}
```

这个方法主要是用来判断当前线程的节点是否已经在同步队列了，这个方法涉及三个判断：

1、如果节点等待状态是 CONDITION 或者节点的 prev 指针为空（节点在同步队列这个指针才有值），那么一定不是在同步队列；

2、如果节点的 next 指针不为空，那么一定在同步队列；

3、遍历同步队列，看队列中有没有节点与这个节点相同。

#### signal()

`signal()` 方法是与 `await()` 方法对应的，一个负责通知，一个负责等待。

下面是 `signal` 方法的源码：

```java
JAVA
public final void signal() {
  if (!isHeldExclusively())
    throw new IllegalMonitorStateException();
  Node first = firstWaiter;
  if (first != null)
    doSignal(first);
}
```

`isHeldExclusively()` 这个方法返回的是该线程是否正在独占资源，如果不是的话会抛出异常。

整个 `signal()` 方法的重点里面调用的 `doSignal()` 方法，传入的参数是头节点。

下面是 `doSignal()` 的源码：

```java
JAVA
private void doSignal(Node first) {
  do {
    if ( (firstWaiter = first.nextWaiter) == null) // 判断头节点的下一个节点是否为 null
      lastWaiter = null; // 如果队列只有头节点，将 lastWaiter 指针置为 null
    first.nextWaiter = null; // 迁移节点前先将节点的 nextWaiter 置为 null
  } while (!transferForSignal(first) && // 把头节点迁移到同步队列中去
           (first = firstWaiter) != null); // 没有迁移成功就重新获取头节点判断不为空继续循环
}
```

这个方法也没有太复杂的内容，具体可以看看上面的注释，这里详细讲讲 `transferForSignal()` 。

方法源码如下：

```java
JAVA
final boolean transferForSignal(Node node) {
  if (!compareAndSetWaitStatus(node, Node.CONDITION, 0)) // 将节点 waitStatus 通过 CAS 更新为 0
    return false; // 更新失败说明节点等待状态已经变了，返回 false 重新获取头节点然后重试
  Node p = enq(node); // 将节点放入同步队列中
  int ws = p.waitStatus;
  if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL)) // 判断前一个节点等待状态，如果状态是正常的赋值为 SIGNAL
    LockSupport.unpark(node.thread); // 前面节点状态为取消或就会唤醒当前节点，避免后面没办法被唤醒
  return true;
}
```

整个迁移队列变化如下图所示：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208021441298.png" alt="img" style="zoom:67%;" />

## 6.ReentrantLock详解

### 类的继承关系

ReentrantLock实现了Lock接口，Lock接口中定义了lock与unlock相关操作，并且还存在newCondition方法，表示生成一个条件。

### 类的内部类

> ReentrantLock总共有三个内部类，并且三个内部类是紧密相关的，下面先看三个类的关系。

![image-20220802155739118](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208021557197.png)

说明: ReentrantLock类内部总共存在Sync、NonfairSync、FairSync三个类，NonfairSync与FairSync类继承自Sync类，Sync类继承自`AbstractQueuedSynchronizer`抽象类。 

#### 1）Sync类

存在如下方法和作用如下：

| 方法              | 作用                                                         |
| ----------------- | ------------------------------------------------------------ |
| lock              | abstract，并未实现，留给子类具体实现                         |
| nonfairTryAcquire | 非公平方式获取                                               |
| tryRelease        | 试图在共享模式下获取对象状态，此方法应该查询是否允许它在共享模式下获取对象状态，如果允许，则获取它 |
| isHeldExclusively | 判断资源是否被当前线程占有                                   |
| newCondition      | 新生一个条件                                                 |
| getOwner          | 返回资源的占用线程                                           |
| getHoldCount      | 返回状态                                                     |
| isLocked          | 资源是否被占用                                               |
| readObject        | 自定义反序列化逻辑                                           |

#### 2）NonfairSync类

NonfairSync类继承了Sync类，表示采用非公平策略获取锁，其实现了Sync类中抽象的lock方法，源码如下:

```java
// 非公平锁
static final class NonfairSync extends Sync {
    // 版本号
    private static final long serialVersionUID = 7316153563782823691L;

    // 获得锁
    final void lock() {
        if (compareAndSetState(0, 1)) // 比较并设置状态成功，状态0表示锁没有被占用
            // 把当前线程设置独占了锁
            setExclusiveOwnerThread(Thread.currentThread());
        else // 锁已经被占用，或者set失败
            // 以独占模式获取对象，忽略中断
            acquire(1); 
    }

    protected final boolean tryAcquire(int acquires) {
        return nonfairTryAcquire(acquires);
    }
}
```

> 说明: 从lock方法的源码可知，每一次都尝试获取锁，而并不会按照公平等待的原则进行等待，让等待时间最久的线程获得锁。

####  3）FairSyn类

FairSync类也继承了Sync类，表示采用公平策略获取锁，其实现了Sync类中的抽象lock方法，源码如下:

```java
// 公平锁
static final class FairSync extends Sync {
    // 版本序列化
    private static final long serialVersionUID = -3000897897090466540L;

    final void lock() {
        // 以独占模式获取对象，忽略中断
        acquire(1);
    }

    /**
        * Fair version of tryAcquire.  Don't grant access unless
        * recursive call or no waiters or is first.
        */
    // 尝试公平获取锁
    protected final boolean tryAcquire(int acquires) {
        // 获取当前线程
        final Thread current = Thread.currentThread();
        // 获取状态
        int c = getState();
        if (c == 0) { // 状态为0
            if (!hasQueuedPredecessors() &&
                compareAndSetState(0, acquires)) { // 不存在已经等待更久的线程并且比较并且设置状态成功
                // 设置当前线程独占
                setExclusiveOwnerThread(current);
                return true;
            }
        }
        else if (current == getExclusiveOwnerThread()) { // 状态不为0，即资源已经被线程占据
            // 下一个状态
            int nextc = c + acquires;
            if (nextc < 0) // 超过了int的表示范围
                throw new Error("Maximum lock count exceeded");
            // 设置状态
            setState(nextc);
            return true;
        }
        return false;
    }
}
```

> 说明: 跟踪lock方法的源码可知，当资源空闲时，它总是会先判断sync队列(AbstractQueuedSynchronizer中的数据结构)是否有等待时间更长的线程，如果存在，则将该线程加入到等待队列的尾部，实现了公平获取原则。其中，FairSync类的lock的方法调用如下，只给出了主要的方法。

![image-20220802161154996](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208021611067.png)

可以看出只要资源被其他线程占用，该线程就会添加到sync queue中的尾部，而不会先尝试获取资源。这也是和Nonfair最大的区别，Nonfair每一次都会尝试去获取资源，如果此时该资源恰好被释放，则会被当前线程获取，这就造成了不公平的现象，当获取不成功，再加入队列尾部。

### 类的属性

ReentrantLock类的`sync`非常重要，对ReentrantLock类的操作大部分都直接转化为对`Sync`和`AbstractQueuedSynchronizer`类的操作。

```java
public class ReentrantLock implements Lock, java.io.Serializable {
    // 序列号
    private static final long serialVersionUID = 7373984872572414699L;    
    // 同步队列
    private final Sync sync;
}
```

### 类的构造函数

```java
public ReentrantLock() {
    // 默认非公平策略
    sync = new NonfairSync();
}

//可以传递参数确定采用公平策略或者是非公平策略，参数为true表示公平策略，否则，采用非公平策略:
public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}
```

### 核心函数分析

>通过分析ReentrantLock的源码，可知对其操作都转化为对Sync对象的操作，由于Sync继承了AQS，所以基本上都可以转化为对AQS的操作。如将ReentrantLock的lock函数转化为对Sync的lock函数的调用，而具体会根据采用的策略(如公平策略或者非公平策略)的不同而调用到Sync的不同子类。

#### NonfairSync部分

接下来主要看一看一个完整的流程，以非公平实现NonfairSync为例，因为默认实现的就是非公平锁，所以当我们new了一个lock锁出来后调用.lock()方法走的就是NonfairSync重写的的lock。

```java
final void lock() {
    // 在 lock 的时候先直接 CAS 修改一次 state 变量（尝试获取锁），成功就返回，不成功再排队，从而达到不排队直接抢占的目的
    // 相对于公平锁，它是一开始就走 AQS 的流程排队获取锁。如果前面有调用过其 lock 方法，则排在队列中前面，也就更有机会更早的获	取锁，从而达到「公平」的目的。
    if (compareAndSetState(0, 1)) {
    	setExclusiveOwnerThread(Thread.currentThread());
    } else {
    	acquire(1);
    }
}
```

当我们没有抢占到锁的时候就会走acquire(1)这个方法，也就是我在第五节AQS源码分析中讲到的独占锁部分，这里不再赘述，接下来分析一下这个参数1的作用，这个参数在后续代码中都是在`tryAcquire(arg)`这个函数中用到的，上面也讲到这个函数都是交由具体的锁去具体实现的，来看一下`ReentrantLock`中的实现：

```java
//这是NonfairSync的实现方式
protected final boolean tryAcquire(int acquires) {
	return nonfairTryAcquire(acquires);
}

final boolean nonfairTryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        if (compareAndSetState(0, acquires)) {//要么是在这里用这个1，代表去拿到锁
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        int nextc = c + acquires;//要么在这里用，表示重入次数加1
        if (nextc < 0)
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}
```

可以看到最后这个参数1就是和States挂钩的。在释放锁的时候作用也类似：

```java
protected final boolean tryRelease(int releases) {
    int c = getState() - releases;
    // 如果释放的线程和获取锁的线程不是同一个，抛出非法监视器状态异常
    if (Thread.currentThread() != getExclusiveOwnerThread()) {
    	throw new IllegalMonitorStateException();
    }
    boolean free = false;
    if (c == 0) {
    // 因为是重入锁的关系，不是每次释放锁 c 都等于 0，知道最后一次释放锁的时候，才通知 AQS 不需要再记录哪个线程正在获取锁
   		free = true;
    	setExclusiveOwnerThread(null);
    }
    setState(c);
    return free;
}
```

#### FairSync部分

说完了非公平锁，接下来再来看看公平锁的tryAcquire()实现：

```java
protected final boolean tryAcquire(int acquires) {
    // 获取当前线程
    final Thread current = Thread.currentThread();
    // 获取父类 AQS 中的标志位
    int c = getState();
    if (c == 0) {
        // 如果队列中没有其他线程，说明没有线程正在占有锁
        // 同时修改一下状态位，如果通过 CAS 操作将状态改为更新成功则代表当前线程获取锁
        if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) {
            // 将当前线程设置到 AQS 的一个变量中，说明这个线程拿走了锁。
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    // 如果 state 不为 0 意味着锁已经被拿走了，但是因为 ReentrantLock 是可重入锁，是可以重复 lock、unlock 的
    // 所以这里还要再判断一次当前获取锁的线程是不是当前请求锁的线程
    else if (current == getExclusiveOwnerThread()) {
        // 如果还是当前获取锁的线程，对 state 字段进行累加操作
        int nextc = c + acquires;
        if (nextc < 0) {
            throw new Error("Maximum lock count exceeded");
        }
        setState(nextc);
        return true;
    }
    // 如果如果获取锁，tryAcquire 返回 true，反之返回 false。
    return false;
}
```

主要是`hasQueuedPredecessors()`这个函数——用来判断有没有别的线程排在了当前线程的前面。可以参考这篇文章，写的十分详细：[AQS深入理解 hasQueuedPredecessors源码分析 JDK8](https://blog.csdn.net/anlian523/article/details/106173860)

## 示例分析（公平锁）

```java
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class MyThread extends Thread {
    private Lock lock;
    public MyThread(String name, Lock lock) {
        super(name);
        this.lock = lock;
    }
    
    public void run () {
        lock.lock();
        try {
            System.out.println(Thread.currentThread() + " running");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            lock.unlock();
        }
    }
}

public class AbstractQueuedSynchonizerDemo {
    public static void main(String[] args) throws InterruptedException {
        Lock lock = new ReentrantLock(true);
        
        MyThread t1 = new MyThread("t1", lock);        
        MyThread t2 = new MyThread("t2", lock);
        MyThread t3 = new MyThread("t3", lock);
        t1.start();
        t2.start();    
        t3.start();
    }
}
/**运行结果(某一次):
Thread[t1,5,main] running
Thread[t2,5,main] running
Thread[t3,5,main] running
*/
```

该示例使用的是公平策略，由结果可知，可能会存在如下一种时序。

![image-20220802161715507](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208021617584.png)

> 首先，t1线程的lock操作 -> t2线程的lock操作 -> t3线程的lock操作 -> t1线程的unlock操作 -> t2线程的unlock操作 -> t3线程的unlock操作。根据这个时序图来进一步分析源码的工作流程。

- t1线程执行lock.lock，下图给出了方法调用中的主要方法。

  ![image-20220803111143018](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208031111104.png)

  说明: 由调用流程可知，t1线程成功获取了资源，可以继续执行。

- t2线程执行lock.lock，下图给出了方法调用中的主要方法。

  ![image-20220803111234925](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208031112008.png)

  说明: 由上图可知，最后的结果是t2线程会被禁止，因为调用了LockSupport.park。

   
