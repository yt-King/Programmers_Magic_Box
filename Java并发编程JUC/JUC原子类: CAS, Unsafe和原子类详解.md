# JUC原子类: CAS, Unsafe和原子类详解

## 带着BAT大厂的面试问题去理解

- 线程安全的实现方法有哪些?
- 什么是CAS?
- CAS使用示例，结合AtomicInteger给出示例?
- CAS会有哪些问题?
- 针对这这些问题，Java提供了哪几个解决的?
- AtomicInteger底层实现? CAS+volatile
- 请阐述你对Unsafe类的理解?
- 说说你对Java原子类的理解? 包含13个，4组分类，说说作用和使用场景。
- AtomicStampedReference是什么?
- AtomicStampedReference是怎么解决ABA的? 内部使用Pair来存储元素值及其版本号
- java中还有哪些类可以解决ABA的问题? AtomicMarkableReference

##  CAS

### 概念

​	CAS的全称是Compare-And-Swap，它是CPU并发原语，它的功能是判断内存某个位置的值是否为预期值，如果是则更改为新的值，这个过程是原子的。可以理解为CAS(V,O,N)，包含三个值分别为：**V 内存地址存放的实际值；O 预期的值（旧值）；N 更新的新值**。当V和O相同时，也就是说旧值和内存中实际的值相同表明该值没有被其他线程更改过，即该旧值O就是目前来说最新的值了，自然而然可以将新值N赋值给V。反之，V和O不相同，表明该值已经被其他线程改过了则该旧值O不是最新版本的值了，所以不能将新值N赋给V，返回V即可。

CAS 是一种用于在**多线程环境下实现同步功能的机**制，其也是无锁优化，或者叫自旋，还有自适应自旋。在jdk中，`CAS`加`volatile`关键字作为实现并发包的**基石**。没有CAS就不会有并发包。          

​	CAS并发原语体现在Java语言中就是sun.misc.Unsafe类的各个方法。调用UnSafe类中的CAS方法，JVM会帮我们实现出CAS汇编指令，这是一种完全依赖于硬件的功能，通过它实现了原子操作，再次强调，由于CAS是一种系统原语，原语属于操作系统用于范畴，是由若干条指令组成，用于完成某个功能的一个过程，并且原语的执行必须是连续的，在执行过程中不允许被中断，也就是说CAS是一条CPU的原子指令，不会造成所谓的数据不一致的问题，也就是说CAS是**线程安全**的。

### 代码使用

```java
/**
 * CASDemo
 * 比较并交换：compareAndSet
 */
public class CASDemo {
    public static void main(String[] args) {
        // 创建一个原子类
        AtomicInteger atomicInteger = new AtomicInteger(5);

        /**
         * 一个是期望值，一个是更新值，但期望值和原来的值相同时，才能够更改
         * 假设三秒前，我拿的是5，也就是expect为5，然后我需要更新成 2019
         */
        //CAS方法，企图更新成2019，这里有两个参数，一个是5，表示期望值，第二个就是我们要更新的值
        System.out.println(atomicInteger.compareAndSet(5, 2019) + "\t current data: " + atomicInteger.get());
	   //然后再次使用了一个方法，同样将值改成1024
        System.out.println(atomicInteger.compareAndSet(5, 1024) + "\t current data: " + atomicInteger.get());
    }
}
```

执行结果：![image-20220729105213519](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207291052583.png)

> 这是因为我们执行第一个的时候，期望值和原本值是满足的，因此修改成功，但是第二次后，主内存的值已经修改成了2019，不满足期望值，因此返回了false，本次写入失败

![image-20220729105217576](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207291052653.png)

这个就类似于SVN或者Git的版本号，如果没有人更改过，就能够正常提交，否者需要先将代码pull下来，合并代码后，然后提交

### CAS底层原理

CAS 主要分三步，读取-比较-修改。其中比较是在检测是否有冲突，如果检测到没有冲突后，其他线程还能修改这个值，那么 CAS 还是无法保证正确性。所以最关键的是要保证比较-修改这两步操作的原子性。

**CAS是靠硬件实现的，从而在硬件层面提升效率。实现方式是基于硬件平台的汇编指令，在intel的CPU中，使用的是cmpxchg指令。** 核心思想就是：**比较要更新变量的值V和预期值E（compare），相等才会将V的值设为新值N（swap）。**

在**多核**的情况下，这个指令也不能保证原子性，需要在前面加上  lock 指令。lock 指令可以保证一个 CPU 核心在操作期间**独占一片内存区域**。那么 这又是如何实现的呢？

在处理器中，一般有两种方式来实现上述效果：总线锁和缓存锁。在多核处理器的结构中，CPU 核心并不能直接访问内存，而是统一通过一条总线访问。总线锁就是锁住这条总线，使其他核心无法访问内存。这种方式代价太大了，会导致其他核心停止工作。而缓存锁并不锁定总线，只是锁定某部分内存区域。当一个 CPU 核心将内存区域的数据读取到自己的缓存区后，它会锁定缓存对应的内存区域。锁住期间，其他核心无法操作这块内存区域。

CAS 就是通过这种方式实现比较和交换操作的原子性的。**值得注意的是， CAS 只是保证了操作的原子性，并不保证变量的可见性，因此变量需要加上 volatile 关键字。**

#### atomicInteger.getAndIncrement()源码

![image-20220729163546017](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207291635104.png)

从这里能够看到，底层又调用了一个unsafe类的getAndAddInt方法

Unsafe是CAS的核心类，由于Java方法无法直接访问底层系统，需要通过本地（Native）方法来访问，Unsafe相当于一个后门，基于该类可以直接操作特定的内存数据，通过**本地调用`C/C++`语言来实现`CAS`操作**。Unsafe类存在sun.misc包中，其内部方法操作可以像C的指针一样直接操作内存，因为Java中的CAS操作的执行依赖于Unsafe类的方法。

![image-20220729163549457](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207291635549.png)

```
注意Unsafe类的所有方法都是native修饰的，也就是说unsafe类中的方法都直接调用操作系统底层资源执行相应的任务
```

为什么Atomic修饰的包装类，能够保证原子性，依靠的就是底层的unsafe类

```java
//Unsafe类
//获取内存地址为obj+offset的变量值, 并将该变量值加上delta
public final int getAndAddInt(Object obj, long offset, int delta) {
    int v;
    do {
    	//通过对象和偏移量获取变量的值
    	//由于volatile的修饰, 所有线程看到的v都是一样的
        v= this.getIntVolatile(obj, offset);
    /*
	while中的compareAndSwapInt()方法尝试修改v的值,具体地, 该方法也会通过obj和offset获取变量的值
	如果这个值和v不一样, 说明其他线程修改了obj+offset地址处的值, 此时compareAndSwapInt()返回false, 继续循环
	如果这个值和v一样, 说明没有其他线程修改obj+offset地址处的值, 此时可以将obj+offset地址处的值改为v+delta, compareAndSwapInt()返回true, 退出循环
	Unsafe类中的compareAndSwapInt()方法是原子操作, 所以compareAndSwapInt()修改obj+offset地址处的值的时候不会被其他线程中断
	*/
    } while(!this.compareAndSwapInt(obj, offset, v, v + delta));
    return v;
}
```

这里没有用synchronized，而用CAS，这样提高了并发性，也能够实现一致性，是因为每个线程进来后，进入的do while循环，然后不断的获取内存中的值，判断是否为最新，然后在进行更新操作。

假设线程A和线程B同时执行getAndInt操作（分别跑在不同的CPU上）

>1. AtomicInteger里面的value原始值为3，即主内存中AtomicInteger的 value 为3，根据JMM模型，线程A和线程B各自持有一份价值为3的副本，分别存储在各自的工作内存
>2. 线程A通过getIntVolatile(var1 , var2) 拿到value值3，这是线程A被挂起（该线程失去CPU执行权）
>3. 线程B也通过getIntVolatile(var1, var2)方法获取到value值也是3，此时刚好线程B没有被挂起，并执行了compareAndSwapInt方法，比较内存的值也是3，成功修改内存值为4，线程B打完收工，一切OK
>4. 这是线程A恢复，执行CAS方法，比较发现自己手里的数字3和主内存中的数字4不一致，说明该值已经被其它线程抢先一步修改过了，那么A线程本次修改失败，只能够重新读取后在来一遍了，也就是在执行do while
>5. 线程A重新获取value值，因为变量value被volatile修饰，所以其它线程对它的修改，线程A总能够看到，线程A继续执行compareAndSwapInt进行比较替换，直到成功。

Unsafe类 + CAS思想： 也就是自旋，自我旋转

### CAS缺点

CAS不加锁，保证一次性，但是需要多次比较

- 循环时间长，开销大（因为执行的是do while，如果比较不成功一直在循环，最差的情况，就是某个线程一直取到的值和预期值都不一样，这样就会无限循环）
- 只能保证一个共享变量的原子操作
  - 当对一个共享变量执行操作时，我们可以通过循环CAS的方式来保证原子操作
  - 但是对于多个共享变量操作时，循环CAS就无法保证操作的原子性，这个时候只能用锁来保证原子性，有一个解决方案是利用对象整合多个共享变量，即一个类中的成员变量就是这几个共享变量。然后将这个对象做CAS操作就可以保证其原子性。atomic中提供了AtomicReference来保证引用对象之间的原子性。
- 引出来ABA问题

### ABA问题

在多线程场景下`CAS`会出现`ABA`问题，例如有2个线程同时对同一个值(初始值为A)进行CAS操作，这两个线程如下

> 1. 线程1，期望值为A，欲更新的值为B
> 2. 线程2，期望值为A，欲更新的值为B

线程`1`抢先获得CPU时间片，而线程`2`因为其他原因阻塞了，线程`1`取值与期望的A值比较，发现相等然后将值更新为B，然后这个时候**出现了线程`3`，期望值为B，欲更新的值为A**，线程3取值与期望的值B比较，发现相等则将值更新为A，此时线程`2`从阻塞中恢复，并且获得了CPU时间片，这时候线程`2`取值与期望的值A比较，发现相等则将值更新为B，虽然线程`2`也完成了操作，但是线程`2`并不知道值已经经过了`A->B->A`的变化过程。

#### `ABA`问题带来的危害：

 小明在提款机，提取了50元，因为提款机问题，有两个线程，同时把余额从100变为50

>  线程1（提款机）：获取当前值100，期望更新为50， 线程2（提款机）：获取当前值100，期望更新为50， 线程1成功执行，线程2某种原因block了，这时，某人给小明汇款50 线程3（默认）：获取当前值50，期望更新为100， 这时候线程3成功执行，余额变为100， 线程2从Block中恢复，获取到的也是100，compare之后，继续更新余额为50！！！

 此时可以看到，实际余额应该为**100**（100-50+50），但是实际上变为了**50**（100-50+50-50）这就是ABA问题带来的危害。

#### ABA问题的解决办法(AtomicStampedReference)

可以通过增加一个版本号来解决

```java
/**
 * ABA问题的解决，AtomicStampedReference
 */
public class ABADemo {

    /**
     * 普通的原子引用包装类
     */
    static AtomicReference<Integer> atomicReference = new AtomicReference<>(100);

    // 传递两个值，一个是初始值，一个是初始版本号
    static AtomicStampedReference<Integer> atomicStampedReference = new AtomicStampedReference<>(100, 1);

    public static void main(String[] args) {

        System.out.println("============以下是ABA问题的产生==========");

        new Thread(() -> {
            // 把100 改成 101 然后在改成100，也就是ABA
            atomicReference.compareAndSet(100, 101);
            atomicReference.compareAndSet(101, 100);
        }, "t1").start();

        new Thread(() -> {
            try {
                // 睡眠一秒，保证t1线程，完成了ABA操作
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 把100 改成 101 然后在改成100，也就是ABA
            System.out.println(atomicReference.compareAndSet(100, 2019) + "\t" + atomicReference.get());

        }, "t2").start();

        System.out.println("============以下是ABA问题的解决==========");

        new Thread(() -> {

            // 获取版本号
            int stamp = atomicStampedReference.getStamp();
            System.out.println(Thread.currentThread().getName() + "\t 第一次版本号" + stamp);

            // 暂停t3一秒钟
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 传入4个值，期望值，更新值，期望版本号，更新版本号
            atomicStampedReference.compareAndSet(100, 101, atomicStampedReference.getStamp(), atomicStampedReference.getStamp()+1);

            System.out.println(Thread.currentThread().getName() + "\t 第二次版本号" + atomicStampedReference.getStamp());

            atomicStampedReference.compareAndSet(101, 100, atomicStampedReference.getStamp(), atomicStampedReference.getStamp()+1);

            System.out.println(Thread.currentThread().getName() + "\t 第三次版本号" + atomicStampedReference.getStamp());

        }, "t3").start();

        new Thread(() -> {

            // 获取版本号
            int stamp = atomicStampedReference.getStamp();
            System.out.println(Thread.currentThread().getName() + "\t 第一次版本号" + stamp);

            // 暂停t4 3秒钟，保证t3线程也进行一次ABA问题
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            boolean result = atomicStampedReference.compareAndSet(100, 2019, stamp, stamp+1);

            System.out.println(Thread.currentThread().getName() + "\t 修改成功否：" + result + "\t 当前最新实际版本号：" + atomicStampedReference.getStamp());

            System.out.println(Thread.currentThread().getName() + "\t 当前实际最新值" + atomicStampedReference.getReference());


        }, "t4").start();

    }
}

```

运行结果为：![image-20220731135939945](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207311359043.png)

#### java中还有哪些类可以解决ABA的问题?

AtomicMarkableReference，它不是维护一个版本号，而是维护一个boolean类型的标记，看标记值有无修改

## UnSafe类详解

> Unsafe是位于sun.misc包下的一个类，主要提供一些用于**执行低级别、不安全操作的方法**，如直接访问系统内存资源、自主管理内存资源等，这些方法在提升Java运行效率、增强Java语言底层资源操作能力方面起到了很大的作用。但由于Unsafe类使Java语言拥有了类似C语言指针一样**操作内存空间的能力**，这无疑也增加了程序发生相关指针问题的风险。在程序中过度、不正确使用Unsafe类会使得程序出错的概率变大，使得Java这种安全的语言变得不再“安全”，因此对Unsafe的使用**一定要慎重**。

这个类尽管里面的方法都是 public 的，但是并没有办法使用它们，JDK API 文档也没有提供任何关于这个类的方法的解释。总而言之，对于 Unsafe 类的使用都是受限制的，只有授信的代码才能获得该类的实例，当然 JDK 库里面的类是可以随意使用的。

![image-20220731141006174](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207311410311.png)

### Unsafe与CAS

反编译出来的代码：

```java
public final int getAndAddInt(Object paramObject, long paramLong, int paramInt)
  {
    int i;
    do
      i = getIntVolatile(paramObject, paramLong);
    while (!compareAndSwapInt(paramObject, paramLong, i, i + paramInt));
    return i;
  }

  public final long getAndAddLong(Object paramObject, long paramLong1, long paramLong2)
  {
    long l;
    do
      l = getLongVolatile(paramObject, paramLong1);
    while (!compareAndSwapLong(paramObject, paramLong1, l, l + paramLong2));
    return l;
  }

  public final int getAndSetInt(Object paramObject, long paramLong, int paramInt)
  {
    int i;
    do
      i = getIntVolatile(paramObject, paramLong);
    while (!compareAndSwapInt(paramObject, paramLong, i, paramInt));
    return i;
  }

  public final long getAndSetLong(Object paramObject, long paramLong1, long paramLong2)
  {
    long l;
    do
      l = getLongVolatile(paramObject, paramLong1);
    while (!compareAndSwapLong(paramObject, paramLong1, l, paramLong2));
    return l;
  }

  public final Object getAndSetObject(Object paramObject1, long paramLong, Object paramObject2)
  {
    Object localObject;
    do
      localObject = getObjectVolatile(paramObject1, paramLong);
    while (!compareAndSwapObject(paramObject1, paramLong, localObject, paramObject2));
    return localObject;
  }
```

从源码中发现，内部使用自旋的方式进行CAS更新(while循环进行CAS更新，如果更新失败，则循环再次重试)。

又从Unsafe类中发现，原子操作其实只支持下面三个方法。

```java
public final native boolean compareAndSwapObject(Object paramObject1, long paramLong, Object paramObject2, Object paramObject3);

public final native boolean compareAndSwapInt(Object paramObject, long paramLong, int paramInt1, int paramInt2);

public final native boolean compareAndSwapLong(Object paramObject, long paramLong1, long paramLong2, long paramLong3);
```

我们发现Unsafe只提供了3种CAS方法：compareAndSwapObject、compareAndSwapInt和compareAndSwapLong。都是native方法。

### Unsafe底层

不妨再看看Unsafe的`compareAndSwap*`方法来实现CAS操作，它是一个本地方法，实现位于`unsafe.cpp`中。

```c
UNSAFE_ENTRY(jboolean, Unsafe_CompareAndSwapInt(JNIEnv *env, jobject unsafe, jobject obj, jlong offset, jint e, jint x))
  UnsafeWrapper("Unsafe_CompareAndSwapInt");
  oop p = JNIHandles::resolve(obj);
  jint* addr = (jint *) index_oop_from_field_offset_long(p, offset);
  return (jint)(Atomic::cmpxchg(x, addr, e)) == e;
UNSAFE_END
```

可以看到它通过 `Atomic::cmpxchg` 来实现比较和替换操作。其中参数x是即将更新的值，参数e是原内存的值。

如果是多处理器，为cmpxchg指令添加lock前缀。反之，就省略lock前缀(单处理器会不需要lock前缀提供的**内存屏障**效果)。这里的lock前缀就是使用了处理器的总线锁(**最新的处理器都使用缓存锁代替总线锁来提高性能**)。

> cmpxchg(void* ptr, int old, int new)，如果ptr和old的值一样，则把new写到ptr内存，否则返回ptr的值，整个操作是原子的。在Intel平台下，会用lock cmpxchg来实现，使用lock触发缓存锁，这样另一个线程想访问ptr的内存，就会被block住。

### Unsafe其它功能

Unsafe 提供了硬件级别的操作，比如说获取某个属性在内存中的位置，比如说修改对象的字段值，即使它是私有的。不过 Java 本身就是为了屏蔽底层的差异，对于一般的开发而言也很少会有这样的需求。

> 更多相关功能，推荐看这篇文章：来自美团技术团队：[Java魔法类：Unsafe应用解析](https://tech.meituan.com/2019/02/14/talk-about-java-magic-class-unsafe.html)

## AtomicInteger

### 使用举例

以 AtomicInteger 为例，常用 API：

```java
public final int get()：获取当前的值
public final int getAndSet(int newValue)：获取当前的值，并设置新的值
public final int getAndIncrement()：获取当前的值，并自增
public final int getAndDecrement()：获取当前的值，并自减
public final int getAndAdd(int delta)：获取当前的值，并加上预期的值
void lazySet(int newValue): 最终会设置成newValue,使用lazySet设置值后，可能导致其他线程在之后的一小段时间内还是可以读到旧的值。
```

### 源码解析

```java
public class AtomicInteger extends Number implements java.io.Serializable {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;
    static {
        try {
            //用于获取value字段相对当前对象的“起始地址”的偏移量
            valueOffset = unsafe.objectFieldOffset(AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    private volatile int value;

    //返回当前值
    public final int get() {
        return value;
    }

    //递增加detla
    public final int getAndAdd(int delta) {
        //三个参数，1、当前的实例 2、value实例变量的偏移量 3、当前value要加上的数(value+delta)。
        return unsafe.getAndAddInt(this, valueOffset, delta);
    }

    //递增加1
    public final int incrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
    }
...
}
```

我们可以看到 AtomicInteger 底层用的是volatile的变量和CAS来进行更改数据的。

- volatile保证线程的可见性，多线程并发时，一个线程修改数据，可以保证其它线程立马看到修改后的值
- CAS 保证数据更新的原子性。

## 延伸到所有原子类

> JDK中提供了13个原子操作类。

### 原子更新基本类型

使用原子的方式更新基本类型，Atomic包提供了以下3个类。

- `AtomicBoolean`: 原子更新布尔类型。
- `AtomicInteger`: 原子更新整型。
- `AtomicLong`: 原子更新长整型。

以上3个类提供的方法几乎一模一样，可以参考上面`AtomicInteger`中的相关方法

### 原子更新数组

通过原子的方式更新数组里的某个元素，Atomic包提供了以下的3个类：

- `AtomicIntegerArray`: 原子更新整型数组里的元素。

- `AtomicLongArray`: 原子更新长整型数组里的元素。

- `AtomicReferenceArray`: 原子更新引用类型数组里的元素。 　 

  这三个类的最常用的方法是如下两个方法：

  - get(int index)：获取索引为index的元素值。
  - compareAndSet(int i,E expect,E update): 如果当前值等于预期值，则以原子方式将数组位置i的元素设置为update值。

### 原子更新引用类型

Atomic包提供了以下三个类：

- `AtomicReference`: 原子更新引用类型。
- `AtomicStampedReference`: 原子更新引用类型, 内部使用Pair来存储元素值及其版本号。
- `AtomicMarkableReferce`: 原子更新带有标记位的引用类型。

这三个类提供的方法都差不多，首先构造一个引用对象，然后把引用对象set进`Atomic`类，然后调用`compareAndSet`等一些方法去进行原子操作，原理都是基于Unsafe实现，但`AtomicReferenceFieldUpdater`略有不同，更新的字段**必须用volatile修饰。**

```java
import java.util.concurrent.atomic.AtomicReference;

public class AtomicReferenceTest {
    
    public static void main(String[] args){

        // 创建两个Person对象，它们的id分别是101和102。
        Person p1 = new Person(101);
        Person p2 = new Person(102);
        // 新建AtomicReference对象，初始化它的值为p1对象
        AtomicReference ar = new AtomicReference(p1);
        // 通过CAS设置ar。如果ar的值为p1的话，则将其设置为p2。
        ar.compareAndSet(p1, p2);

        Person p3 = (Person)ar.get();
        System.out.println("p3 is "+p3);
        System.out.println("p3.equals(p1)="+p3.equals(p1));
    }
}

class Person {
    volatile long id;
    public Person(long id) {
        this.id = id;
    }
    public String toString() {
        return "id:"+id;
    }
}
//输出结果
//p3 is id:102
//p3.equals(p1)=false
```

结果说明：

- 新建AtomicReference对象ar时，将它初始化为p1。
- 紧接着，通过CAS函数对它进行设置。如果ar的值为p1的话，则将其设置为p2。
- 最后，获取ar对应的对象，并打印结果。p3.equals(p1)的结果为false，这是因为Person并没有覆盖equals()方法，而是采用继承自Object.java的equals()方法；而Object.java中的equals()实际上是调用"=="去比较两个对象，即比较两个对象的地址是否相等。

### 原子更新字段类

Atomic包提供了四个类进行原子字段更新：

- `AtomicIntegerFieldUpdater`: 原子更新整型的字段的更新器。
- `AtomicLongFieldUpdater`: 原子更新长整型字段的更新器。
- `AtomicStampedFieldUpdater`: 原子更新带有版本号的引用类型。
- `AtomicReferenceFieldUpdater`: 上面已经说过此处不在赘述。

这四个类的使用方式都差不多，是基于反射的原子更新字段的值。要想原子地更新字段类需要两步:

- 第一步，因为原子更新字段类都是抽象类，每次使用的时候必须使用静态方法newUpdater()创建一个更新器，并且需要设置想要更新的类和属性。
- 第二步，更新类的字段必须使用public volatile修饰。

```java
public class TestAtomicIntegerFieldUpdater {

    public static void main(String[] args){
        TestAtomicIntegerFieldUpdater tIA = new TestAtomicIntegerFieldUpdater();
        tIA.doIt();
    }

    public AtomicIntegerFieldUpdater<DataDemo> updater(String name){
        return AtomicIntegerFieldUpdater.newUpdater(DataDemo.class,name);

    }

    public void doIt(){
        DataDemo data = new DataDemo();
        System.out.println("publicVar = "+updater("publicVar").getAndAdd(data, 2));
        /*
            * 由于在DataDemo类中属性value2/value3,在TestAtomicIntegerFieldUpdater中不能访问
            * */
        //System.out.println("protectedVar = "+updater("protectedVar").getAndAdd(data,2));
        //System.out.println("privateVar = "+updater("privateVar").getAndAdd(data,2));

        //System.out.println("staticVar = "+updater("staticVar").getAndIncrement(data));//报java.lang.IllegalArgumentException
        /*
            * 下面报异常：must be integer
            * */
        //System.out.println("integerVar = "+updater("integerVar").getAndIncrement(data));
        //System.out.println("longVar = "+updater("longVar").getAndIncrement(data));
    }

}

class DataDemo{
    public volatile int publicVar=3;
    protected volatile int protectedVar=4;
    private volatile  int privateVar=5;

    public volatile static int staticVar = 10;
    //public  final int finalVar = 11;

    public volatile Integer integerVar = 19;
    public volatile Long longVar = 18L;

}
```

对于AtomicIntegerFieldUpdater 的使用稍微有一些限制和约束，约束如下：

- 字段必须是volatile类型的，在线程之间共享变量时保证立即可见.eg:volatile int value = 3
- 字段的描述类型(修饰符public/protected/default/private)是与调用者与操作对象字段的关系一致。也就是说调用者能够直接操作对象字段，那么就可以反射进行原子操作。但是对于父类的字段，子类是不能直接操作的，尽管子类可以访问父类的字段。
- 只能是实例变量，不能是类变量，也就是说不能加static关键字。
- 只能是可修改变量，不能使final变量，因为final的语义就是不可修改。实际上final的语义和volatile是有冲突的，这两个关键字不能同时存在。
- 对于AtomicIntegerFieldUpdater和AtomicLongFieldUpdater只能修改int/long类型的字段，不能修改其包装类型(Integer/Long)。如果要修改包装类型就需要使用AtomicReferenceFieldUpdater。
