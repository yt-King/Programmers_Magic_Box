# HashMap

## 1.哈希表简介

[面试官：哈希表都不知道，你是怎么看懂HashMap的？](https://juejin.cn/post/6876105622274703368)

## 2.底层实现

### 2.1-JDK1.8之前

JDK1.8 之前 `HashMap` 底层是 **数组和链表** 结合在一起使用也就是 **链表散列**。**HashMap 通过 key 的 hashCode 经过扰动函数处理过后得到 hash 值，然后通过 (n - 1) & hash 判断当前元素存放的位置（这里的 n 指的是数组的长度），如果当前位置存在元素的话，就判断该元素与要存入的元素的 hash 值以及 key 是否相同，如果相同的话，直接覆盖，不相同就通过拉链法解决冲突。**

**所谓扰动函数指的就是 HashMap 的 hash 方法。使用 hash 方法也就是扰动函数是为了防止一些实现比较差的 hashCode() 方法 换句话说使用扰动函数之后可以减少碰撞。**

### 2.2-JDK1.8之后

相比于之前的版本， JDK1.8 之后在解决哈希冲突时有了较大的变化，当链表长度大于阈值（默认为 8）（将链表转换成红黑树前会判断，如果当前数组的长度小于 64，那么会选择先进行数组扩容，每次扩容后，容量为原来的 2 倍，并进行数据迁移。而不是转换为红黑树）时，将链表转化为红黑树，以减少搜索时间。 

![image-20220807152015513](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208071520618.png)

>TreeMap、TreeSet 以及 JDK1.8 之后的 HashMap 底层都用到了红黑树。红黑树就是为了解决二叉查找树的缺陷，因为二叉查找树在某些情况下会退化成一个线性结构。

### 2.3-基本操作

`HashMap`提供两个重要的基本操作，`put(K, V)`和`get(K)`。

- 当调用`put`操作时，`HashMap`计算键值K的哈希值，然后将其对应到`HashMap`的某一个桶(`bucket`)上；此时找到以这个桶为头结点的一个单链表，然后顺序遍历该单链表找到某个节点的`Entry`中的`Key`是**等于**给定的参数K；若找到，则将其的`old V`替换为参数指定的`V`；否则直接在链表尾部插入一个新的`Entry`节点。
- 对于`get(K)`操作类似于`put`操作，`HashMap`通过计算键的哈希值，先找到对应的桶，然后遍历桶存放的单链表通过比照`Entry`的键来找到对应的值。

### 2.4 HashMap扩容

在使用`HashMap`的过程中，我们经常会遇到这样一个带参数的构造方法。

```java
public HashMap(int initialCapacity, float loadFactor) ;
```

- 第一个参数：初始容量，指明初始的桶的个数；相当于桶数组的大小。
- 第二个参数：装载因子，是一个0-1之间的系数，根据它来确定需要扩容的阈值，默认值是0.75。

**当`map`中包含的`Entry`的数量大于等于`threshold = loadFactor * capacity`的时候，且新建的`Entry`刚好落在一个非空的桶上，此刻触发扩容机制，将其容量扩大为2倍。**

当`size`大于等于`threshold`的时候，并不一定会触发扩容机制，但是会很可能就触发扩容机制，只要有一个新建的`Entry`出现哈希冲突，则立刻`resize`。

## 3.特点

- HashMap 是一个[散列表](https://so.csdn.net/so/search?q=散列表&spm=1001.2101.3001.7020)，它存储的内容是键值对(key-value)映射。
- HashMap 实现了 Map 接口，根据键的 HashCode 值存储数据，具有很快的访问速度，最多允许一条记录的键为 null，不支持线程同步。
- HashMap 是无序的，即不会记录插入的顺序。
- HashMap 继承于AbstractMap，实现了 Map、Cloneable、java.io.[Serializable](https://so.csdn.net/so/search?q=Serializable&spm=1001.2101.3001.7020) 接口。

## 4.HashMap 的长度为什么是 2 的幂次方

为了能让 HashMap 存取高效，尽量较少碰撞，也就是要尽量把数据分配均匀。我们上面也讲到了过了，Hash 值的范围值-2147483648 到 2147483647，前后加起来大概 40 亿的映射空间，只要哈希函数映射得比较均匀松散，一般应用是很难出现碰撞的。但问题是一个 40 亿长度的数组，内存是放不下的。所以这个散列值是不能直接拿来用的。用之前还要先做对数组的长度取模运算，得到的余数才能用来要存放的位置也就是对应的数组下标。这个数组下标的计算方法是“ `(n - 1) & hash`”。（n 代表数组长度）。这也就解释了 HashMap 的长度为什么是 2 的幂次方。

**这个算法应该如何设计呢？**

我们首先可能会想到采用%取余的操作来实现。但是，重点来了：**“取余(%)操作中如果除数是 2 的幂次则等价于与其除数减一的与(&)操作（也就是说 hash%length==hash&(length-1)的前提是 length 是 2 的 n 次方!!）。”** 并且 **采用二进制位操作 &，相对于%能够提高运算效率，这就解释了 HashMap 的长度为什么是 2 的幂次方。**

## 5.HashMap 多线程操作导致死循环问题

主要原因在于并发下的 Rehash 会造成元素之间会形成一个循环链表。不过，jdk 1.8 后解决了这个问题，但是还是不建议在多线程下使用 HashMap,因为多线程下使用 HashMap 还是会存在其他问题比如数据丢失。并发环境下推荐使用 ConcurrentHashMap 。

详情请查看：https://coolshell.cn/articles/9606.html

## 6.HashMap 有哪几种常见的遍历方式?

[HashMap 的 7 种遍历方式与性能分析！](https://mp.weixin.qq.com/s/zQBN3UvJDhRTKP6SzcZFKw)

## 7.JDK8中的HashMap什么时候将链表转化为红黑树？为什么要使用红黑树？

1. 这个题很容易答错， 大部分答案就是： **当链表中的元素个数大于8时就会把链表转化为红黑树**。 但是其实还有另外一个限制：  当发现链表中的元素个数大于8之后， 还会判断一下当前数组的长度， **如果数组长度小于64时， 此时并不会转化为红黑树， 而是进行扩容**。 只有当链表中的元素个数大于8， 并且数组的长度大于 等于64时才会将链表转为红黑树。 上面扩容的原因是， 如果数组长度还比较小， 就先利用扩容来缩小链表的长度。 
2. 当元素个数小于一个阈值时， 链表整体的插入查询效率要高于红黑树， 当元素个数大于此阈值时， 链表整 体的插入查询效率要低于红黑树。 此阈值在HashMap中为8 

## 8.JDK8中HashMap的put方法的实现过程？

1.根据key生成hashcode（经过扰动函数）

2.判断当前HashMap对象中的数组是否为空， 如果为空则初始化该数组

3.根据逻辑**与运算**，算出hashcode基于当前数组对应的数组下标i

4.判断数组的第i个位置的元素 （tab[i]） 是否为空

- 如果空，则将key，value封装为Node对象赋值给tab[i]

- 如果不为空：

  - 如果put方法传入进来的key等于tab[i] .key， 那么证明存在相同的key

  - 如果不等于tab[i] .key， 则：
    - 如果tab[i]的类型是TreeNode， 则表示数组的第i位置上是一颗红黑树， 那么将key和 value插入到红黑树中， 并且在插入之前会判断在红黑树中是否存在相同的key
    - 如果tab[i]的类型不是TreeNode， 则表示数组的第i位置上是一个链表， 那么遍历链表寻  找是否存在相同的key， 并且在遍历的过程中会对链表中的结点数进行计数， 当遍历到最后一个结点时， 会将key ,value封装为Node插入到链表的尾部， 同时判断在插入新结点之 前的链表结点个数是不是大于等于8，如果是，则将链表改为红黑树。

  -  如果上述步骤中发现存在相同的key，则根据onlyIfAbsent标记来判断是否需要更新value值，然后返回oldValue

5.modCount++

6.HashMap的元素个数size加1

7.如果size大于扩容的阈值， 则进行扩容 

## 9.JDK8中HashMap的get方法的实现过程

![image-20220817101800978](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208171018027.png)

## 10.HashMap中变量modCount的真实作用是什么

[面试官：HashMap中变量modCount的真实作用是什么](https://zhuanlan.zhihu.com/p/430069424)

[谈谈fail-fast与fail-safe是什么以及工作机制](https://zhuanlan.zhihu.com/p/37476508)

[hashmap遍历时用map.remove方法为什么会报错？](https://juejin.cn/post/6844903812092674056)

[java中foreach实现原理](https://juejin.cn/post/6844904003600384013)

## ConcurrentHashMap

![image-20220807152021143](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208071520214.png)

**在 JDK1.7 的时候，`ConcurrentHashMap`（分段锁）** 对整个桶数组进行了分割分段(`Segment`)，每一把锁只锁容器其中一部分数据，多线程访问容器里不同数据段的数据，就不会存在锁竞争，提高并发访问率。

首先将数据分为一段一段的存储，然后给每一段数据配一把锁，当一个线程占用锁访问其中一个段数据时，其他段的数据也能被其他线程访问。

**`ConcurrentHashMap` 是由 `Segment` 数组结构和 `HashEntry` 数组结构组成**。

Segment 实现了 `ReentrantLock`,所以 `Segment` 是一种可重入锁，扮演锁的角色。`HashEntry` 用于存储键值对数据。

一个 `ConcurrentHashMap` 里包含一个 `Segment` 数组。`Segment` 的结构和 `HashMap` 类似，是一种数组和链表结构，一个 `Segment` 包含一个 `HashEntry` 数组，每个 `HashEntry` 是一个链表结构的元素，每个 `Segment` 守护着一个 `HashEntry` 数组里的元素，当对 `HashEntry` 数组的数据进行修改时，必须首先获得对应的 `Segment` 的锁。

![image-20220807152024650](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208071520709.png)

JDK1.8 的 `ConcurrentHashMap` 不再是 **Segment 数组 + HashEntry 数组 + 链表**，而是 **Node 数组 + 链表 / 红黑树**。不过，Node 只能用于链表的情况，红黑树的情况需要使用 **`TreeNode`**。当冲突链表达到一定长度时，链表会转换成红黑树。

 `ConcurrentHashMap` 取消了 `Segment` 分段锁，采用 CAS 和 `synchronized` 来保证并发安全。数据结构跟 HashMap1.8 的结构类似，数组+链表/红黑二叉树。Java 8 在链表长度超过一定阈值（8）时将链表（寻址时间复杂度为 O(N)）转换为红黑树（寻址时间复杂度为 O(log(N))）

`synchronized` 只锁定当前链表或红黑二叉树的首节点，这样只要 hash 不冲突，就不会产生并发，效率又提升 N 倍