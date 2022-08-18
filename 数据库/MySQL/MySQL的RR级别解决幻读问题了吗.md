# MySQL的RR级别解决幻读问题了吗

>先说结论：解决了，但又没有完全解决，取决于你如何看待幻读这个概念。

## 什么是幻读

那么首先就来看看什么是幻读，MySQL[官方文档](https://dev.mysql.com/doc/refman/8.0/en/innodb-next-key-locking.html)定义如下：

>当同一个查询在不同时间生成不同的行集时，事务中就会出现所谓的幻读问题。例如，如果执行了两次 SELECT，但第二次返回了第一次没有返回的行，则该行是“幻影”行。

但是产生“幻影”行这个结果的操作不是唯一的，如果我们定义幻读是专指**“新插入的行”**，那可以明确地说RR级别下的`next_lock key`已经解决了幻读问题；但是如果我们定义幻读包含**“所有写操作”**，那么也可以说幻读问题没有完全被解决。这也就是我开头所说的，取决于你看待幻读这个概念的观点。接下来通过几个例子来说明问题。

（tips：如果对于MVCC机制没有了解的同学建议先看一下[深入理解MySQL底层事务隔离级别的实现原理](https://cloud.tencent.com/developer/article/1892952)这篇文章，学习一下MVCC机制）

## 准备工作

版本说明：MySQL 5.7 ；隔离级别：RR

新建一个表t1：

![image-20220817151318986](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208171513036.png)

## 例1

![image-20220817154611530](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208171546594.png)

如上图开启一个事务1，并查询全表，然后另开一个连接,在新的事务2中插入一条数据：

![image-20220817155136803](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208171551870.png)

最后在事务1中再去查询发现数据并没有变：

![image-20220817155231536](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208171552607.png)

>在事务1中，两次查询条件是相同的，查处的结果也是相同的，因为默认的查询语句是不加锁的（RR隔离级别），所以这是靠MVCC机制+undo版本连+readview实现的

## 例2

照着例1的例子继续下去，不过这次的事务1中查询后更了`for update`(加锁读)。

![image-20220817155647583](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208171556645.png)

继续同例1中一样在事务2中插入一条新纪录：

![image-20220817155831681](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208171558737.png)

可以看到事务2被阻塞了，因为当前的查询对全表加了间隙锁，所以在插入是会被阻塞。

## 例3（重点）

一开始还是照常开启事务1，查询操作：

![image-20220817160911496](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208171609552.png)

然后开启事务2，插入操作：

![image-20220817161033866](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208171610927.png)

这时我们在事务1中进行update操作，在查询一遍：

![image-20220817161239684](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208171612740.png)

这时候我们就可以看到幻读居然产生了。

> 这个案例非常值得分析，首先查询语句没加锁，所以事务2不会加锁，所以事务2记录正常插入，那为什么事务1里面在事务2提交成功后进行了更新操作在查询就跟之前不一样了呢？在同一个事务中两次查询条件相同的查询得出了不同的结果，这确确实实是出现了幻读，但是这是为什么呢？

原因如下：**RR级别下ReadView的生成时机是在事务中的第一次查询，事务结束前该ReadView复用。但是如果事务中进行了当前读的操作，比如例3事务一中的update，后续再查询就会重新生成ReadView。**也就是说`update`操作产生了当前读，那当前读肯定可以读到事务2已经提了的数据，然后全部更新后再去读就又会产生一个`readview`，很明显之前的`update`操作对于这个`readview`是可见的，所以数据的**条数**就跟之前的不一样了。

## 总结

借鉴MySQL45讲中对幻读的定义：幻读是专指“新插入的行”。

![image-20220817162106688](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208171621765.png)