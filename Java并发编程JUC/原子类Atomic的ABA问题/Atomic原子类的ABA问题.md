# Atomic原子类的ABA问题

## 1.原子类

### 1.1-原子更新基本类型

从JDK1.5开始，Java在java.util.concurrent.atomic包下引入了一些Atomic相关的原子操作类，这些类避免使用加锁来实现同步，从而更加方便、高效的实现原子操作。atomic包下的所有类如下图所示：

![image-20220128204107658](Atomic%E5%8E%9F%E5%AD%90%E7%B1%BB%E7%9A%84ABA%E9%97%AE%E9%A2%98.images/image-20220128204107658.png)

Atomic包下所有的原子类都只适用于单个元素，即只能保证一个基本数据类型、对象、或者数组的原子性。根据使用范围，可以将这些类分为四种类型，分别为原子**更新基本类型**、**原子更新数组**、**原子更新引用**、**原子更新属性**。

atomic包下原子更新基本数据类型包括AtomicInteger、AtomicLong、AtomicBoolean三个类，分别提供了原子更新整数类型、原子更新长整数类型和原子更新布尔类型的功能。这里，我们以AtomicInteger为例来学习如何使用。

AtomicInteger中提供了很多方法供我们调用，如：

```java
// 获取当前值，然后自加，相当于i++
getAndIncrement()
// 获取当前值，然后自减，相当于i--
getAndDecrement()
// 自加1后并返回，相当于++i
incrementAndGet()
// 自减1后并返回，相当于--i
decrementAndGet()
// 获取当前值，并加上预期值
getAndAdd(int delta)
// 获取当前值，并设置新值
int getAndSet(int newValue)

// 这些方法都是原子操作，在多线程下也能够保证原子性
```

### 1.2-2.原子更新引用类型

基本类型的原子类只能更新一个变量，如果需要原子更新多个变量，则需要使用引用类型原子类。引用类型的原子类包括AtomicReference、AtomicStampedReference、AtomicMarkableReference三个。

- **AtomicReference** 引用原子类
- **AtomicStampedReference** 原子更新带有版本号的引用类型。该类将整数值与引用关联起来，可用于解决原子的更新数据和数据的版本号，可以解决使用 CAS 进行原子更新时可能出现的 ABA 问题。
- **AtomicMarkableReference** 原子更新带有标记的引用类型。该类将 boolean 标记与引用关联起来。

### 1.3-原子更新数组

这里原子更新数组并不是对数组本身的原子操作，而是对数组中的元素。主要包括3个类：AtomicIntegerArray、AtomicLongArray及AtomicReferenceArray，分别表示原子更新整数数组的元素、原子更新长整数数组的元素以及原子更新引用类型数组的元素。我们以AtomicIntegerArray为例来看：

```java
public class AtomicIntegerArray implements java.io.Serializable {
    // final类型的int数组
    private final int[] array;
    // 获取数组中第i个元素
    public final int get(int i) {
        return (int)AA.getVolatile(array, i);
    }   
    // 设置数组中第i个元素
    public final void set(int i, int newValue) {
        AA.setVolatile(array, i, newValue);
    }
    // CAS更改第i个元素
    public final boolean compareAndSet(int i, int expectedValue, int newValue) {
        return AA.compareAndSet(array, i, expectedValue, newValue);
    }
    // 获取第i个元素，并加1
    public final int getAndIncrement(int i) {
        return (int)AA.getAndAdd(array, i, 1);
    }
    // 获取第i个元素并减1
    public final int getAndDecrement(int i) {
        return (int)AA.getAndAdd(array, i, -1);
    }   
    // 对数组第i个元素加1后再获取
    public final int incrementAndGet(int i) {
        return (int)AA.getAndAdd(array, i, 1) + 1;
    }  
    // 对数组第i个元素减1后再获取
    public final int decrementAndGet(int i) {
        return (int)AA.getAndAdd(array, i, -1) - 1;
    }    
    // ... 省略
}    
```

可以看到，在AtomicIntegerArray内部维护了一个final修饰的int数组，且类中所有的操作都是针对数组元素的操作。同时，这些方法都是原子操作，可以保证多线程下数据的安全性。

### 1.4-原子更新对象属性

如果只选择更新某个对象中的某个字段，可以使用更新对象字段的原子类。包括三个类，AtomicIntegerFieldUpdater、AtomicLongFieldUpdater以及AtomicReferenceFieldUpdater。需要注意的是这些类的使用需要满足以下条件才可。

- 被操作的字段不能是static类型；
- 被操纵的字段不能是final类型；
- 被操作的字段必须是volatile修饰的；
- 属性必须对于当前的Updater所在区域是可见的。

下面以AtomicIntegerFieldUpdater为例，结合前例中的Book类来更新Book的价格，注意将price用volatile修饰。

```Java
public class Book {
    public String name;

    public volatile int price;

    public Book(String name, int price) {
        this.name = name;
        this.price = price;
    }
}

AtomicIntegerFieldUpdater<Book> updater = AtomicIntegerFieldUpdater.newUpdater(Book.class, "price");
Book book = new Book("三国演义", 42);
updater.set(book, 50);
System.out.println( "更新后的价格是" + updater.get(book));

```

输出结果如下：

> 更新后的价格是50

实例化一个Book，价格为42，通过AtomicIntegerFieldUpdater可以将价格修改为50。

## 2.ABA问题

在多线程场景下`CAS`会出现`ABA`问题，例如有2个线程同时对同一个值(初始值为A)进行CAS操作，这两个线程如下

> 1. 线程1，期望值为A，欲更新的值为B
> 2. 线程2，期望值为A，欲更新的值为B

线程`1`抢先获得CPU时间片，而线程`2`因为其他原因阻塞了，线程`1`取值与期望的A值比较，发现相等然后将值更新为B，然后这个时候**出现了线程`3`，期望值为B，欲更新的值为A**，线程3取值与期望的值B比较，发现相等则将值更新为A，此时线程`2`从阻塞中恢复，并且获得了CPU时间片，这时候线程`2`取值与期望的值A比较，发现相等则将值更新为B，虽然线程`2`也完成了操作，但是线程`2`并不知道值已经经过了`A->B->A`的变化过程。

### 1.1-`ABA`问题带来的危害：

 小明在提款机，提取了50元，因为提款机问题，有两个线程，同时把余额从100变为50

>  线程1（提款机）：获取当前值100，期望更新为50， 线程2（提款机）：获取当前值100，期望更新为50， 线程1成功执行，线程2某种原因block了，这时，某人给小明汇款50 线程3（默认）：获取当前值50，期望更新为100， 这时候线程3成功执行，余额变为100， 线程2从Block中恢复，获取到的也是100，compare之后，继续更新余额为50！！！

 此时可以看到，实际余额应该为**100**（100-50+50），但是实际上变为了**50**（100-50+50-50）这就是ABA问题带来的危害。

## 3.ABA问题的解决办法

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

运行结果为：![image-20220128212033796](Atomic%E5%8E%9F%E5%AD%90%E7%B1%BB%E7%9A%84ABA%E9%97%AE%E9%A2%98.images/image-20220128212033796.png)

## 4.LongAdder（CAS机制优化）

[比AtomicLong更优秀的LongAdder确定不来了解一下吗？](https://juejin.cn/post/6844904159402000397#heading-14)