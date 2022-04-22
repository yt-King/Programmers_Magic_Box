# 聊聊Lock锁

![image-20220414102700078](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/4/202204141027306.png)

## 1.简介

lock是一个接口，他有很多种实现，常见的有可重入锁（ReentrantLock）和读写锁（Read、WriteLock），在JUC下的一个子包locks包里。Lock需要显式地获取和释放锁，虽然不如隐式获取锁的便捷，但有了锁获取与释放的可操作性、可中断的获取锁及超时获取锁等同步特性。

![image-20220411135241875](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/4/202204111352168.png)

### 1.1-方法简介

- void lock()：获取锁，若锁不可用，则当前线程将会阻塞，直到获得锁；

  > 这是最简单也是最普通的获取锁的方式，他不像synchronized一样在异常时自动释放锁，所以必须在finally块中释放确保发生异常时能够正常释放锁。需要注意的是该方法不能被中断，所以如果陷入死锁的状况lock（）会一直等待下去。

- void lockInterruptibly() throws InterruptedException：获取可中断锁，当被中断或获取到锁才返回，若锁不可用，则当前线程被阻塞，直到获取锁或被中断；

  >与lock()不同的是lockInterruptibly()支持在等待锁的途中中断，或者可有说lockInterruptibly对Thread.interrupt方法的响应优先级更高，当lockInterruptibly在等待锁的途中调用中断方法这是lockInterruptibly不会获取锁而是抛出一个InterruptedException。而lock()则是优先获取锁才会响应中断，在lock等待的途中即使执行interrupt也没有用，必须等到lock获取锁以后才会响应中断

- boolean tryLock()：尝试获取锁，并立即返回；true：获取锁成功；false：获取锁失败；

- boolean tryLock(long time, TimeUnit unit) throws InterruptedException：尝试在指定的超时时间获取锁，当获取到锁时返回true；当超时或被中断时返回false；

- Condition newCondition()：返回一个和锁绑定的条件队列；在等待条件之前线程必须先获取当前锁，同时await()方法会原子地释放锁，并在返回之前重新获取到锁；

- void unlock()：释放锁；

ReentrantLock和ReadWriteLock是此接口的实现：

![image-20220411140005843](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/4/202204111400997.png)

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

![image-20220418091647330](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/4/202204180916528.png)

AQS 中会将竞争共享资源失败的线程及其状态信息封装到一个node中加入到一个变体的 CLH 队列中，接下来会不断自旋（cas）尝试获取锁，条件是当前节点是头结点的直接后继才会尝试。失败一定次数后则阻塞自己等待被唤醒。而持有锁的线程释放锁时会唤醒后继的节点中的线程。

![image-20220418092533237](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/4/202204180925289.png)

### 3）什么是CLH队列

参考：https://juejin.cn/post/6896278031317663751

CLH：Craig、Landin and Hagersten 队列，是 **单向链表实现的队列**。申请线程只在本地变量上自旋，**它不断轮询前驱的状态**，如果发现 **前驱节点释放了锁就结束自旋**

![image-20220418092907692](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/4/202204180929770.png)

CLH 队列具有以下特点：

1. CLH 队列是一个单向链表，有着 FIFO 先进先出的队列特性
2. 通过 tail 尾节点（原子引用）来构建队列，总是指向最后一个节点
3. 未获得锁节点会进行自旋，而不是切换线程状态
4. 并发高时性能较差，因为会有大量的为获得锁的线程不断轮询前驱节点的状态，会造成一定的资源浪费

AQS 中的队列是 CLH 变体的虚拟双向队列，通过将每条请求共享资源的线程封装成一个节点来实现锁的分配

![image-20220418092941448](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/4/202204180929541.png)

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

![image-20220419160915290](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/4/202204191609071.png)

### 5）AQS的两种队列

AQS 总共有两种队列，从node的构造方式中也可以看出，一种是用于同步队列，代表的是正常的获取释放锁的队列；另外一种是条件队列，代表的是每个 ConditionObject 对应的队列。两者都是FIFO（先进先出）队列。

![image-20220419161335679](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/4/202204191613016.png)

#### 同步队列

同步队列是一个双向列表，其内的节点有两种，一种是独占队列的节点，一种是共享队列的节点，两者的区别是独占的节点的nextWaiter 指向null，共享锁的nextWaiter 指向一个静态的SHARED 节点。两种队列都包括head节点和tail节点。head节点是一个空的头节点，主要用作后续的调度。

![image-20220419210611913](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/4/202204192106008.png)

#### 条件队列

条件队列是单链，它没有空的头节点，每个节点都有对应的线程。条件队列头节点和尾节点的指针分别是 firstWaiter 和 lastWaiter 。

![image-20220419211214815](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/4/202204192112873.png)

### 6）Condition接口

上面说到了条件队列，条件等待和条件唤醒功能一般都是 ReentrantLock 与 AQS 的内部类 配合实现的。一个 ReentrantLock 可以创建多个 ConditionObject 实例，每个实例对应一个条件队列，以保证每个实例都有自己的等待唤醒逻辑，不会相互影响。条件队列里的线程对应的节点被唤醒时会被放到 ReentrantLock 的同步队列里，让同步队列去完成唤醒和重新尝试获取锁的工作。可以理解为条件队列是依赖同步队列的，它们协同才能完成条件等待和条件唤醒功能。

而在AQS中ConditionObject 是通过实现Condition接口来完成的，类似Object的wait()、wait(long timeout)、notify()以及notifyAll()的方法结合synchronized内置锁可以实现可以实现等待/通知模式，Condition接口定义了await()、awaitNanos(long)、signal()、signalAll()等方法，配合对象锁实例实现等待/通知功能。

![image-20220419213817740](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/4/202204192138853.png)

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

![image-20220422094202664](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/4/202204220942907.png)

接下来来看一下addWaiter的源码

#### addWaiter(Node mode)

```java
    private Node addWaiter(Node mode) {//mode = Node.EXCLUSIVE = null
        Node node = new Node(Thread.currentThread(), mode);
        // Try the fast path of enq; backup to full enq on failure
        Node pred = tail;
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
                    tail = head;
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

![image-20220421164553026](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/4/202204211645138.png)

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
                    setHead(node);//获取锁成功后把当前节点设置为头节点
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

![image-20220422114524482](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/4/202204221145696.png)

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
            /* 判断前面节点状态为 SIGNAL ，返回 true
             * This node has already set status asking a release
             * to signal it, so it can safely park.
             */
            return true;
        if (ws > 0) {
            /* 如果前面的节点状态为取消（CANCEL值为1）,就一直向前查找，直到找到状态不为取消的节点，把它放在这个节点后面
             * Predecessor was cancelled. Skip over predecessors and
             * indicate retry.
             */
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            /* 如果前面节点不是取消也不是 SIGNAL 状态，将其设置为 SIGNAL 状态
             * waitStatus must be 0 or PROPAGATE.  Indicate that we
             * need a signal, but don't park yet.  Caller will need to
             * retry to make sure it cannot acquire before parking.
             */
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }
```

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

方法开始尝试释放锁，若失败直接返回，如果释放锁成功，那么就会接着判断头节点是否为空和头节点 waitStatus 是否不为 0 ，因为在唤醒头节点的后继之前会通过CAS尝试将头节点状态置0的操作。如果头节点的状态为 0 了，说明正在释放后继节点，这时候也就不再需要释放了，直接返回 true。判断状态之后就是unparkSuccessor方法：

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
