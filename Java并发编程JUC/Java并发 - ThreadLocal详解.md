#  Java 并发 - ThreadLocal详解

## 带着BAT大厂的面试问题去理解

- 什么是ThreadLocal? 用来解决什么问题的?
- 说说你对ThreadLocal的理解
- ThreadLocal是如何实现线程隔离的?
- 为什么ThreadLocal会造成内存泄露? 如何解决
- 还有哪些使用ThreadLocal的应用场景?

## ThreadLocal简介

>官网解释：该类提供了线程局部 (thread-local) 变量。这些变量不同于它们的普通对应物，因为访问某个变量(通过其 get 或 set 方法)的每个线程都有自己的局部变量，它独立于变量的初始化副本。ThreadLocal 实例通常是类中的 private static 字段，它们希望将状态与某一个线程(例如，用户 ID 或事务 ID)相关联。

**总结而言**：ThreadLocal是一个将在多线程中为每一个线程创建单独的变量副本的类; 当使用ThreadLocal来维护变量时, ThreadLocal会为每个线程创建单独的变量副本, 避免因多线程操作共享变量而导致的数据不一致的情况。

## ThreadLocal理解

> `ThreadLocal`，即线程本地变量。如果你创建了一个`ThreadLocal`变量，**那么访问这个变量的每个线程都会有这个变量的一个本地拷贝**，多个线程操作这个变量的时候，实际是在操作自己本地内存里面的变量，从而起到**线程隔离**的作用，避免了并发场景下的线程安全问题。

提到ThreadLocal被提到应用最多的是session管理和数据库链接管理，这里以数据访问为例帮助你理解ThreadLocal： 

- 如下数据库管理类在单线程使用是没有任何问题的

```java
class ConnectionManager {
    private static Connection connect = null;

    public static Connection openConnection() {
        if (connect == null) {
            connect = DriverManager.getConnection();
        }
        return connect;
    }

    public static void closeConnection() {
        if (connect != null)
            connect.close();
    }
}
```

很显然，在多线程中使用会存在线程安全问题：第一，这里面的2个方法都没有进行同步，很可能在openConnection方法中会多次创建connect；第二，由于connect是共享变量，那么必然在调用connect的地方需要使用到同步来保障线程安全，因为很可能一个线程在使用connect进行数据库操作，而另外一个线程调用closeConnection关闭链接。

- 为了解决上述线程安全的问题，第一考虑：互斥同步

你可能会说，将这段代码的两个方法进行同步处理，并且在调用connect的地方需要进行同步处理，比如用Synchronized或者ReentrantLock互斥锁。

- 这里再抛出一个问题：这地方到底需不需要将connect变量进行共享?

事实上，是不需要的。假如每个线程中都有一个connect变量，各个线程之间对connect变量的访问实际上是没有依赖关系的，即一个线程不需要关心其他线程是否对这个connect进行了修改的。即改后的代码可以这样：

```java
class ConnectionManager {
    private Connection connect = null;

    public Connection openConnection() {
        if (connect == null) {
            connect = DriverManager.getConnection();
        }
        return connect;
    }

    public void closeConnection() {
        if (connect != null)
            connect.close();
    }
}

class Dao {
    public void insert() {
        ConnectionManager connectionManager = new ConnectionManager();
        Connection connection = connectionManager.openConnection();

        // 使用connection进行操作

        connectionManager.closeConnection();
    }
}
```

这样处理确实也没有任何问题，由于每次都是在方法内部创建的连接，那么线程之间自然不存在线程安全问题。但是这样会有一个致命的影响：导致服务器压力非常大，并且严重影响程序执行性能。由于在方法中需要频繁地开启和关闭数据库连接，这样不仅严重影响程序执行效率，还可能导致服务器压力巨大。

- 这时候ThreadLocal登场了

那么这种情况下使用ThreadLocal是再适合不过的了，因为ThreadLocal在每个线程中对该变量会创建一个副本，即每个线程内部都会有一个该变量，且在线程内部任何地方都可以使用，线程之间互不影响，这样一来就不存在线程安全问题，也不会严重影响程序执行性能。下面就是网上出现最多的例子：

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
	//如果我们希望通过某个类将状态(例如用户ID、事务ID)与线程关联起来，那么通常在这个类中定义private static类型的ThreadLocal实例。
    private static final ThreadLocal<Connection> dbConnectionLocal = new ThreadLocal<Connection>() {
        @Override
        protected Connection initialValue() {
            try {
                return DriverManager.getConnection("", "", "");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    public Connection getConnection() {
        return dbConnectionLocal.get();
    }
}
```

> 但是要注意，虽然ThreadLocal能够解决上面说的问题，但是由于在每个线程中都创建了副本，所以要考虑它对资源的消耗，比如内存的占用会比不使用ThreadLocal要大。

## ThreadLocal原理

> 主要是用到了Thread对象中的一个ThreadLocalMap类型的变量threadLocals, 负责存储当前线程的关于Connection的对象, dbConnectionLocal(以上述例子中为例) 这个变量为Key, 以新建的Connection对象为Value; 这样的话, 线程第一次读取的时候如果不存在就会调用ThreadLocal的initialValue方法创建一个Connection对象并且返回，具体关于为线程分配变量副本的代码如下:

```java
public T get() {
    Thread t = Thread.currentThread();
    ThreadLocalMap threadLocals = getMap(t);
    if (threadLocals != null) {
        ThreadLocalMap.Entry e = threadLocals.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result;
        }
    }
    return setInitialValue();
}
```

- 首先获取当前线程对象t, 然后从线程t中获取到ThreadLocalMap类型的成员变量threadLocals
- 如果当前线程的threadLocals已经初始化(即不为null) 并且存在以当前ThreadLocal对象为Key的值, 则直接返回当前线程要获取的对象(本例中为Connection);
- 如果当前线程的threadLocals已经初始化(即不为null)但是不存在以当前ThreadLocal对象为Key的的对象, 那么重新创建一个Connection对象, 并且添加到当前线程的threadLocals Map中,并返回
- 如果当前线程的threadLocals属性还没有被初始化, 则重新创建一个ThreadLocalMap对象, 并且创建一个Connection对象并添加到ThreadLocalMap对象中并返回。

```java
private T setInitialValue() {
    T value = initialValue();
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
    return value;
}
```

- 首先调用我们上面写的重载过后的initialValue方法, 产生一个Connection对象
- 继续查看当前线程的threadLocals是不是空的, 如果ThreadLocalMap已被初始化, 那么直接将产生的对象添加到ThreadLocalMap中, 如果没有初始化, 则创建并添加对象到其中

---

同时, `ThreadLocal`还提供了直接操作Thread对象中的`threadLocals`的方法：

```java
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
}
//这样我们也可以不实现initialValue, 将初始化工作放到DBConnectionFactory的getConnection方法中:
public Connection getConnection() {
    Connection connection = dbConnectionLocal.get();
    if (connection == null) {
        try {
            connection = DriverManager.getConnection("", "", "");
            dbConnectionLocal.set(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    return connection;
}
```

看过代码后就可以知道为什么ThreadLocal能够实现变量的多线程隔离了; 其实就是**用了Map的数据结构给当前线程缓存了**, 要使用的时候就**从本线程的threadLocals对象中获取就可以了**, key就是当前线程。

## ThreadLocalMap对象是什么

先来看一看ThreadLocal的数据结构：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208071653023.png)

>`Thread`类有一个类型为`ThreadLocal.ThreadLocalMap`的实例变量`threadLocals`，也就是说每个线程有一个自己的`ThreadLocalMap`。
>
>`ThreadLocalMap`有自己的独立实现，可以简单地将它的`key`视作`ThreadLocal`，`value`为代码中放入的值（实际上`key`并不是`ThreadLocal`本身，而是它的一个**弱引用**）。
>
>每个线程在往`ThreadLocal`里放值的时候，都会往自己的`ThreadLocalMap`里存，读也是以`ThreadLocal`作为引用，在自己的`map`里找对应的`key`，从而实现了**线程隔离**。
>
>`ThreadLocalMap`有点类似`HashMap`的结构，只是`HashMap`是由**数组+链表**实现的，而`ThreadLocalMap`中并没有**链表**结构。
>
>我们还要注意`Entry`， 它的`key`是`ThreadLocal<?> k` ，继承自`WeakReference`， 也就是我们常说的弱引用类型。

本质上来讲, ThreadLocalMap它就是一个Map, 但是这个ThreadLocalMap与我们平时见到的Map有点不一样

- 它没有实现Map接口
- 它没有public的方法, 最多有一个default的构造方法, 因为这个ThreadLocalMap的方法仅仅在ThreadLocal类中调用, 属于静态内部类
- ThreadLocalMap的Entry实现继承了WeakReference<ThreadLocal<?>>
- 该方法仅仅用了一个Entry数组来存储Key, Value; Entry并不是链表形式, 而是每个bucket里面仅仅放一个Entry

### ThreadLocalMap **的四个属性**

- Entry[] table
- INITIAL_CAPACITY
- size
- threshold

```java
// 源码
static class ThreadLocalMap {
    
    static class Entry extends WeakReference<ThreadLocal<?>> {
        /** The value associated with this ThreadLocal. */
        Object value;

        Entry(ThreadLocal<?> k, Object v) {
            super(k);
            value = v;
        }
    }

    //初始容量默认为16，必须是2的幂
    private static final int INITIAL_CAPACITY = 16;

    // table每次resized，容量都得是2的幂
    private Entry[] table;

    // 当前table中的存放的元素数量
    private int size = 0;

    // 扩容阀值
    private int threshold; // Default to 0
	
    /**
     * 接下来还有 set()、get()、扩容方法、expungeStaleEntry()、cleanSomeSlots()等重要方法就不贴源码了
	 * ......
     */
} 
```

### **Hash 算法**

`ThreadLocalMap` 实现了自己的`hash` 算法来解决散列表数组冲突。

```java
public class ThreadLocal<T> {
    private final int threadLocalHashCode = nextHashCode();

    private static AtomicInteger nextHashCode = new AtomicInteger();

    private static final int HASH_INCREMENT = 0x61c88647;

    private static int nextHashCode() {
        return nextHashCode.getAndAdd(HASH_INCREMENT);
    }

    static class ThreadLocalMap {
        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            table = new Entry[INITIAL_CAPACITY];
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);

            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            setThreshold(INITIAL_CAPACITY);
        }
    }
}
```

这里最关键的就是`threadLocalHashCode`值的计算，`ThreadLocal`中有一个属性为`HASH_INCREMENT = 0x61c88647`，每当创建一个`ThreadLocal`对象，这个`ThreadLocal.nextHashCode` 这个值就会增长 `0x61c88647` 。这个值很特殊，它是**斐波那契数** 也叫 **黄金分割数**。`hash`增量为 这个数字，带来的好处就是 `hash` **分布非常均匀**。

> 总结：`ThreadLocalMap` 的`hash` 算法很简单，就是使用`斐波那契数的倍数` 和`(len -1)` 按位与（这个结果其实就是`斐波那契数的倍数` 对`capacity` 取模）的结果作为当前 key 在散列表中的数组下标。

### **Hash 冲突**

> **`HashMap` 如何解决 hash 冲突**：`HashMap` 解决冲突是使用链地址法，在数组上构造链表结构，将冲突的数据放在链表上，且每个数组元素也就是链表的长度超过某个数量后会将链表转换为红黑树。
>
> `ThreadLocalMap` 使用的是线性探测的开放地址法去解决 hash 冲突。 当当前 `key` 存在 hash 冲突，会线性地往后探测直到找到为 null 的位置存入对象，或者找到 `key` 相同的位置覆盖更新原来的对象。在这过程中若发现不为空但 `key` 为 null 的桶（`key` 过期的 `Entry` 数据）则启动**探测式清理操作**。

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208071657632.png)

如上图所示，如果我们插入一个`value=27`的数据，通过 `hash` 计算后应该落入槽位 4 中，而槽位 4 已经有了 `Entry` 数据。

此时就会线性向后查找，一直找到 `Entry` 为 `null` 的槽位才会停止查找，将当前元素放入此槽位中。这里还画了一个`Entry`中的`key`为`null`的数据（**Entry=2 的灰色块数据**），因为`key`值是**弱引用**类型，在`set`过程中，如果遇到了`key`过期的`Entry`数据，就会进行一轮上面说到的**探测式清理操作**，具体操作方式后面会讲到。

### ThreadLocalMap.set() **原理详解**

在介绍`ThreadLocal`原理时展示了`ThreadLocal`直接操作Thread对象中的`threadLocals`的方法：**ThreadLocal.set()**。`set()` 方法的代码如下：

```java
// ThreadLocal.ThreadLocalMap.set()方法
private void set(ThreadLocal<?> key, Object value) {
    // 通过 key 计算出当前 key 在散列表对应的位置——i
    Entry[] tab = table;
    int len = tab.length;
    int i = key.threadLocalHashCode & (len-1);
    
    // 从 i 开始向后遍历，查找找到为空的位置（也就是得到 tab[i]），注意：通过nextIndex()方法，在遍历完散列数组的最后位置后，遍历的下一个位置是 index=0
    /** 
     * private static int nextIndex(int i, int len) {
     *     return ((i + 1 < len) ? i + 1 : 0);
     * }
	*/
    for (Entry e = tab[i];e != null;e = tab[i = nextIndex(i, len)]) {
        ThreadLocal<?> k = e.get();
		// 遇到key相同，直接更新覆盖，返回
        if (k == key) {
            e.value = value;
            return;
        }
		// 遍历到到key=null（过期元素），执行replaceStaleEntry()，返回
        if (k == null) {
            replaceStaleEntry(key, value, i);
            return;
        }
    }
    // 在 空位置 存放数据
    tab[i] = new Entry(key, value);
    // size++
    int sz = ++size;
    // 调用boolean cleanSomeSlots()进行启发式清理过期元素
    // 若未清理到任何数据且size超过阈值threshold（len*2/3）则rehash()，rehash()中会先进行探测式清理过期元素，若此时size>=len/2(threshold-threshold/4)则扩容
    if (!cleanSomeSlots(i, sz) && sz >= threshold)
        rehash();
}
```

接下来看一看`ThreadLocalMap`的 set() 方法，往`ThreadLocalMap`中`set`数据（**新增**或者**更新**数据）分为好几种情况，针对不同的情况我们画图来说明。

#### 第一种情况

通过`hash`计算后的槽位对应的`Entry`数据为空：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208080955100.png)

这里直接将数据放到该槽位即可。

#### **第二种情况**

槽位数据不为空，`key`值与当前`ThreadLocal`通过`hash`计算获取的`key`值一致：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208080956472.png)

这里直接更新该槽位的数据。

#### **第三种情况**

槽位数据不为空，往后遍历过程中，在找到`Entry`为`null`的槽位之前，没有遇到`key`过期的`Entry`：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208081002368.png)

遍历散列数组，线性往后查找，如果找到`Entry`为`null`的槽位，则将数据放入该槽位中，或者往后遍历过程中，遇到了**key 值相等**的数据，直接更新即可。

#### **第四种情况** 

槽位数据不为空，往后遍历过程中，在找到`Entry`为`null`的槽位之前，遇到`key`过期的`Entry`，如下图，往后遍历过程中，遇到了`index=7`的槽位数据`Entry`的`key=null`，这是就要执行`replaceStaleEntry(key, value, i)`方法，方法代码如下：

```java
// ThreadLocal.ThreadLocalMap.replaceStaleEntry()
private void replaceStaleEntry(ThreadLocal<?> key, Object value,int staleSlot) {
    Entry[] tab = table;
    int len = tab.length;
    Entry e;
	// 从staleSlot向前遍历直到遇到Entry=null，期间遇到key=null时更新slotToExpunge
    int slotToExpunge = staleSlot;
    for (int i = prevIndex(staleSlot, len);(e = tab[i]) != null;i = prevIndex(i, len)){
        if (e.get() == null){
			slotToExpunge = i;
        }
    }
	// 从staleSlot向后遍历，直到Entry=null停止
    for (int i = nextIndex(staleSlot, len);(e = tab[i]) != null;i = nextIndex(i, len)) {
        ThreadLocal<?> k = e.get();
		// 遇到key=key
        if (k == key) {
            // 更新该位置Entry并将该位置和staleSlot的Entry交换
            e.value = value;
            tab[i] = tab[staleSlot];
            tab[staleSlot] = e;
			// 若此时slotToExpunge=staleSlot，说明向前遍历时没有发现过期元素以及向后遍历也没发现过期元素，此时修改探测式清理过期元素的起始下标为i（也就是从i作为起始下标开始探测式清理）
            if (slotToExpunge == staleSlot){
                slotToExpunge = i;
            }
            // cleanSomeSlots()为启发式清理，expungeStaleEntry()为探测式清理
            cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
            return;
        }
		// 若遇到key=null 且 slotToExpunge=staleSlot，说明向前遍历未遇到过期元素但向后遍历遇到了过期元素，此时修改探测式清理过期元素的起始下标为i
        if (k == null && slotToExpunge == staleSlot){
         	slotToExpunge = i;   
        }
    }
	// 从staleSlot向后遍历过程中遇到了Entry=null，此时直接将数据更新到staleSlot位置
    tab[staleSlot].value = null;
    tab[staleSlot] = new Entry(key, value);
	// 若slotToExpunge!=staleSlot，说明向前遍历或者向后遍历过程中有遇到过期元素，此时slotToExpunge为向前遍历中“最远”的或者向后遍历中遇到的“最远”的key为null的下标，启动探测式清理后启动启发式清理。
    if (slotToExpunge != staleSlot)
        cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
}
```

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208081003614.png)

初始化探测式清理过期数据扫描的开始位置：`slotToExpunge = staleSlot = 7`

以当前`staleSlot`开始 向前迭代查找，找其他过期的数据，然后更新过期数据起始扫描下标`slotToExpunge`。`for`循环迭代，直到碰到`Entry`为`null`结束。

如果找到了过期的数据，继续向前迭代，直到遇到`Entry=null`的槽位才停止迭代，如下图所示，**slotToExpunge 被更新为 0**：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208081055294.png)

以当前节点(`index=7`)向前迭代，检测是否有过期的`Entry`数据，如果有则更新`slotToExpunge`值。碰到`null`则结束探测。以上图为例`slotToExpunge`被更新为 0（0的前一个节点是15，15位null）。

上面向前迭代的操作是为了更新探测清理过期数据的起始下标`slotToExpunge`的值，这个值在后面清理的时候会用到，它是用来记录清理开始的下标位置，在调用清理的函数时会将这个参数传递给清理函数。

接着开始以`staleSlot`位置(`index=7`)向后迭代，直到Entry=null停止，这里又分为两种情况

##### 1.**在Entry=null之前找到了相同 key 值的 Entry 数据**

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208081056145.png)

先更新key值相同的`Entry`中的数据，然后将当前位置和`Entry`位置互换（即上图中下标为8的位置先将值更新为27，然后和下标为7也就是当前位置互换位置）。然后开始进行过期`Entry`的清理工作，如下图所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208081252664.png)

##### 2.**在Entry=null之前没有找到相同 key 值的 Entry 数据**

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208081252254.png)

创建新的`Entry`，替换`table[stableSlot]`位置：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208081253337.png)

#### 清理函数

接下来就是清理工作，清理函数分为两种，expungeStaleEntry()-**探测式清理**；cleanSomeSlots()-**启发式清理**。方法的一般调用形式如下

```java
cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
```

所以我们先来看看探测式清理，其中函数的入参就是我们在上面讲到的`slotToExpunge`（记录清理开始的下标）：

```java
private int expungeStaleEntry(int staleSlot) {
    Entry[] tab = table;
    int len = tab.length;
	// 传入的staleSlot位置上的数据一定是过期数据，将staleSlot位置的置空
    tab[staleSlot].value = null;
    tab[staleSlot] = null;
    size--;
	// for循环是向后遍历，直到遇到 Entry=null
    Entry e;
    int i;
    for (i = nextIndex(staleSlot, len);(e = tab[i]) != null;i = nextIndex(i, len)) {
        ThreadLocal<?> k = e.get();
        // 若当前遍历的 key 为 null则将 Entry置空
        if (k == null) {
            e.value = null;
            tab[i] = null;
            size--;
        //若当前遍历的 key 不为null，将其rehash并将key的原本位置Entry置空，再将key的Entry放入rehash后的位置以及其后面位置的第一个为null的位置
        } else {
            int h = k.threadLocalHashCode & (len - 1);
            if (h != i) {
                tab[i] = null;
                while (tab[h] != null)
                    h = nextIndex(h, len);
                tab[h] = e;
            }
        }
    }
    // 返回i，也就是探测式清理向后遍历中遇到的第一个为null的位置
    return i;
}
```

从一个示例图来看：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208081344099.png)

我们假设`expungeStaleEntry(3)` 来调用此方法，如上图所示，我们可以看到`ThreadLocalMap`中`table`的数据情况，接着执行清理操作：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208081344817.png)

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208081344988.png)

执行完第二步后，index=4 的元素挪到 index=3 的槽位中。

继续往后迭代检查，碰到正常数据，计算该数据位置是否偏移，如果被偏移，则重新计算`slot`位置，目的是让正常数据尽可能存放在正确位置或离正确位置更近的位置（rehash）

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208081345651.png)

> 这里探测式清理并不能全部清除数组中的过期元素，而是从传入的下标清理到第一个 `Entry==null` 为止。部分清除。
>
> 其余的部分，需要通过 **启发式清理**。以上的流程图引用自[javaGuide](https://javaguide.cn/java/concurrent/threadlocal.html#threadlocalmap%E8%BF%87%E6%9C%9F-key-%E7%9A%84%E6%8E%A2%E6%B5%8B%E5%BC%8F%E6%B8%85%E7%90%86%E6%B5%81%E7%A8%8B)。

---

接下来再来看看启发式清理函数：

```java
private boolean cleanSomeSlots(int i, int n) {
    boolean removed = false;
    Entry[] tab = table;
    int len = tab.length;
    do {
        i = nextIndex(i, len);
        Entry e = tab[i];
        if (e != null && e.get() == null) {
            n = len;
            removed = true;
            i = expungeStaleEntry(i);
        }
    } while ( (n >>>= 1) != 0);
    return removed;
}
```

启发式清理需要接收两个参数：

1. i = 探测式清理后返回的数字下标，这里至少保证了Hash冲突的下标至探测式清理后返回的下标这个区间无过期的Entry。

2. n = 数组总长度

   >n 的用途：主要用于**扫描控制（scan control），从 while 中是通过 n 来进行条件判断的说明 n 就是用来控制扫描趟数（循环次数）的**。在扫描过程中，如果没有遇到脏 entry 就整个扫描过程持续 log2(n)次，log2(n)的得来是因为`n >>>= 1`，每次 n 右移一位相当于 n 除以 2。如果在扫描过程中遇到脏 entry 的话就会令 n 为当前 hash 表的长度（`n=len`），再扫描 log2(n)趟，注意此时 n 增加无非就是多增加了循环次数从而通过 nextIndex 往后搜索的范围扩大

- 根据源码可以看出，启动式清理会从传入的下标 `i` 处，向后遍历。
- 如果发现过期的Entry则再次触发探测式清理，并重置 `n`。这个n是用来控制 `do while` 循环的跳出条件。
- 如果遍历过程中，连续 `m` 次没有发现过期的Entry，就可以认为数组中已经没有过期Entry了。
- 这个 `m` 的计算是 `n >>>= 1` ，你也可以理解成是数组长度的2的几次幂。

**例如：数组长度是16，那么2^4=16，也就是连续4次没有过期Entry，即 `m = logn/log2(n为数组长度)`**

### 扩容机制

> 在`ThreadLocalMap.set()`方法的最后，如果执行完启发式清理工作后，未清理到任何数据，且当前散列数组中`Entry`的数量已经达到了列表的扩容阈值`(len*2/3)`，就开始执行`rehash()`逻辑

```java
private void rehash() {
    // 该方法为从下标0出发，找到第一个 key=null 的位置j，以j为起始开始探测式清理
    expungeStaleEntries();
    // 阈值 threshold=len*2/3
	// 当前size超过或等于阈值的3/4时执行扩充
    if (size >= threshold - threshold / 4)
        resize();
}

private void expungeStaleEntries() {
    Entry[] tab = table;
    int len = tab.length;
    for (int j = 0; j < len; j++) {
        Entry e = tab[j];
        if (e != null && e.get() == null)
            expungeStaleEntry(j);
    }
}
```

> rehash()`的阈值是`size >= threshold，当前size超过或等于threshold的3/4时执行扩充（resize），**注意两者区别**

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208081411511.png)

```java
private void resize() {
    Entry[] oldTab = table;
    int oldLen = oldTab.length;
    int newLen = oldLen * 2;//扩容是 tab 直接扩容为原来的 2 倍的
    Entry[] newTab = new Entry[newLen];
    int count = 0;
	//遍历旧的散列表，重新计算每个元素的 hash 位置放到新的 tab 数组中，遇到 hash 冲突则往后寻找最近的 entry=null 的位置存放
    for (int j = 0; j < oldLen; ++j) {
        Entry e = oldTab[j];
        if (e != null) {
            ThreadLocal<?> k = e.get();
            if (k == null) {
                e.value = null;
            } else {
                int h = k.threadLocalHashCode & (newLen - 1);
                while (newTab[h] != null)
                    h = nextIndex(h, newLen);
                newTab[h] = e;
                count++;
            }
        }
    }
    //最后重新计算 tab 执行扩容的阈值
    setThreshold(newLen);
    size = count;
    table = newTab;
}
```

### ThreadLocalMap.get() **详解**

介绍完`ThreadLocalMap`的`set`函数，`get`函数就相对简单很多了。使用 `get()` 操作获取数据有 2 种情况：

**第一种情况：** 通过查找`key`值计算出散列表中`slot`位置，然后该`slot`位置中的`Entry.key`和查找的`key`一致，则直接返回

**第二种情况：** `slot`位置中的`Entry.key`和要查找的`key`不一致，则从该位置向后遍历，遇到 `key=null` 就启动探测式清理然后继续遍历，直到遍历到 `key=传入的key` 的位置，最后将位置上的 `Entry` 返回；或者位置上的 `Entry` 为空，返回 null。

代码如下：

```java
private Entry getEntry(ThreadLocal<?> key) {
    int i = key.threadLocalHashCode & (table.length - 1);
    Entry e = table[i];
    if (e != null && e.get() == key)
        // 第一种情况
        return e;
    else
        // 第二种情况
        return getEntryAfterMiss(key, i, e);
}

private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
    Entry[] tab = table;
    int len = tab.length;

    while (e != null) {
        ThreadLocal<?> k = e.get();
        // 遍历到key=传入的key，返回该Entry
        if (k == key)
            return e;
        if (k == null)
            // 遍历中遇到 key=null，启动探测式清理
            expungeStaleEntry(i);
        else
            i = nextIndex(i, len);
        e = tab[i];
    }
    // 遍历中遇到了null
    return null;
}
```

## ThreadLocal为啥要用弱引用

先来看一张很经典的图：

![image-20220808145941738](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208081459823.png)

由于`ThreadLocal`的弱引用会造成一个非常经典的问题，那就是**内存泄漏**

>ThreadLocalMap使用ThreadLocal的弱引用作为key，如果一个ThreadLocal没有外部强引用来引用它，那么系统GC的时候，这个ThreadLocal势必会被回收，这样一来，ThreadLocalMap中就会出现key为null的Entry，就没有办法访问这些key为null的Entry的value，如果当前线程再迟迟不结束的话，这些key为null的Entry的value就会一直存在一条强引用链：**Current Thread Ref -> Current Thread -> Map -> Entry ->value**永远无法回收，造成内存泄漏。

既然如此那`ThreadLocal`为啥还要使用弱引用呢？

1. ThreadLocalMap对于内存泄漏的防护措施(**ThreadLocalMap的set、get、remove**)

   > 在调用 ThreadLocal 的 get()，set() 和 remove() 的时候都会清除当前线程 ThreadLocalMap 中所有 key 为 null的value。这样可以降低内存泄漏发生的概率。所以我们在使用ThreadLocal 的时候，每次用完 ThreadLocal 都调用remove()方法，清除数据，防止内存泄漏。

2. 强引用vs.弱引用

- key 使用强引用：也就是把图中的虚线变成了实现，这时候即使`ThreadLocalRef`置为null断掉与堆中`ThreadLocal`的连线，通过可达性分析`ThreadLocal`跟value一样也会一直存在一条强引用链：**Current Thread Ref -> Current Thread -> Map -> Entry ->key**永远无法回收，造成内存泄漏。
- key 使用弱引用：引用的ThreadLocal的对象被回收了，由于ThreadLocalMap持有ThreadLocal的弱引用，即使没有手动删除，ThreadLocal也会被回收。value在下一次ThreadLocalMap调用set,get，remove的时候会被清除。

ThreadLocalMap的生命周期跟Thread一样长，如果都没有手动删除对应key，都会导致内存泄漏,但是弱引用的话进行set、get、remove方法时，会清除key为null的value，比较方便一点。

> 因此，ThreadLocal内存泄漏的根源是：由于ThreadLocalMap的生命周期跟Thread一样长，如果没有手动删除对应key就会导致内存泄漏，而**不是因为弱引用。**所以推荐每次使用完`ThreadLocal`，都调用它的`remove()`方法，清除数据。

## 扩展

java中的threadlocal，是绑定在线程上的。你在一个线程中set的值，在另外一个线程是拿不到的。如果在threadlocal的平行线程中，创建了新的子线程，那么这里面的值是无法传递、共享的。这就是透传问题。->[解决方案](https://mp.weixin.qq.com/s/8hc1dNlJy_zfDiyb8G3fUw)
