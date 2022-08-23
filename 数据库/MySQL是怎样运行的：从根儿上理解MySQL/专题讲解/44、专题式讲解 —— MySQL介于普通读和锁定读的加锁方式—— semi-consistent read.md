# MySQL介于普通读和锁定读的加锁方式—— semi-consistent read

标签： MySQL是怎样运行的

------

## 事前准备

为了故事的顺利发展，我们先建一个表，并向表中插入一些记录，下边是SQL语句：

```sql
CREATE TABLE hero (
    number INT,
    name VARCHAR(100),
    country varchar(100),
    PRIMARY KEY (number),
    KEY idx_name (name)
) Engine=InnoDB CHARSET=utf8;

INSERT INTO hero VALUES
    (1, 'l刘备', '蜀'),
    (3, 'z诸葛亮', '蜀'),
    (8, 'c曹操', '魏'),
    (15, 'x荀彧', '魏'),
    (20, 's孙权', '吴');
```

现在`hero`表中的记录情况就如下所示：

```sql
mysql> SELECT * FROM hero;
+--------+------------+---------+
| number | name       | country |
+--------+------------+---------+
|      1 | l刘备      | 蜀      |
|      3 | z诸葛亮    | 蜀      |
|      8 | c曹操      | 魏      |
|     15 | x荀彧      | 魏      |
|     20 | s孙权      | 吴      |
+--------+------------+---------+
5 rows in set (0.01 sec)
```

## 现象

在小册答疑群里有一位同学提了一个问题：说是在`READ COMMITTED`隔离级别下发生了一件百思不得其解的事儿。好的，首先构造环境，将当前会话默认的隔离级别设置成`READ COMMITTED`：

```scss
mysql> SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
Query OK, 0 rows affected (0.00 sec)
```

事务`T1`先执行：

```sql
# T1中，隔离级别为READ COMMITTED
mysql> BEGIN;
Query OK, 0 rows affected (0.00 sec)

mysql> SELECT * FROM hero WHERE country = '魏' FOR UPDATE;
+--------+---------+---------+
| number | name    | country |
+--------+---------+---------+
|      8 | c曹操   | 魏      |
|     15 | x荀彧   | 魏      |
+--------+---------+---------+
2 rows in set (0.01 sec)
```

`country`列并不是索引列，所以本条语句执行时肯定是使用扫描聚簇索引的全表扫描方式来执行，`EXPLAIN`语句也证明了我们的想法：

```sql
mysql> EXPLAIN SELECT * FROM hero WHERE country = '魏' FOR UPDATE;
+----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+-------------+
| id | select_type | table | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra       |
+----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+-------------+
|  1 | SIMPLE      | hero  | NULL       | ALL  | NULL          | NULL | NULL    | NULL |    5 |    20.00 | Using where |
+----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+-------------+
1 row in set, 1 warning (0.02 sec)
```

我们之前学过MySQL语句的加锁分析，知道在`READ COMMITTED`隔离级别下，如果采用全表扫描的方式执行查询语句时，InnoDB存储引擎将依次对每条记录加正经记录锁，在server层测试该记录是否符合WHERE条件，如果不符合则将加在该记录上的锁释放掉。本例中使用`FOR UPDATE`语句，肯定加的是X型正经记录锁。只有两条记录符合`WHERE`条件，所以最终其实只对这两条符合条件的记录加了`X型正经记录锁`（就是`number`列值为`8`和`15`的两条记录）。当然，我们可以使用`SHOW ENGINE INNODB STATUS`命令证明我们的分析：

```css
mysql> SHOW ENGINE INNODB STATUS\G
... 省略了很多内容

------------
TRANSACTIONS
------------
Trx id counter 39764
Purge done for trx's n:o < 39763 undo n:o < 0 state: running but idle
History list length 36
Total number of lock structs in row lock hash table 1
LIST OF TRANSACTIONS FOR EACH SESSION:
---TRANSACTION 281479653009568, not started
0 lock struct(s), heap size 1160, 0 row lock(s)
---TRANSACTION 281479653012832, not started
0 lock struct(s), heap size 1160, 0 row lock(s)
---TRANSACTION 39763, ACTIVE 468 sec
2 lock struct(s), heap size 1160, 2 row lock(s)
MySQL thread id 19, OS thread handle 123145470611456, query id 586 localhost 127.0.0.1 root
TABLE LOCK table `xiaohaizi`.`hero` trx id 39763 lock mode IX
RECORD LOCKS space id 287 page no 3 n bits 72 index PRIMARY of table `xiaohaizi`.`hero` trx id 39763 lock_mode X locks rec but not gap
Record lock, heap no 4 PHYSICAL RECORD: n_fields 5; compact format; info bits 0
 0: len 4; hex 80000008; asc     ;;
 1: len 6; hex 000000009b4a; asc      J;;
 2: len 7; hex 80000001d3012a; asc       *;;
 3: len 7; hex 63e69bb9e6938d; asc c      ;;
 4: len 3; hex e9ad8f; asc    ;;

Record lock, heap no 5 PHYSICAL RECORD: n_fields 5; compact format; info bits 0
 0: len 4; hex 8000000f; asc     ;;
 1: len 6; hex 000000009b4a; asc      J;;
 2: len 7; hex 80000001d30137; asc       7;;
 3: len 7; hex 78e88d80e5bda7; asc x      ;;
 4: len 3; hex e9ad8f; asc    ;;

 ... 省略了很多内容
```

其中`id`为`39763`的事务就是指`T1`，可以看出它为`heap no`值为`4`和`5`的两条记录加了`X型正经记录锁`（lock_mode X locks rec but not gap）。

然后再开启一个隔离级别也为`READ COMMITTED`的事务`T2`，在其中执行：

```sql
# T2中，隔离级别为READ COMMITTED
mysql> BEGIN;
Query OK, 0 rows affected (0.00 sec)

mysql> SELECT * FROM hero WHERE country = '吴' FOR UPDATE;
(进入阻塞状态)
```

很显然，这条语句也会采用全表扫描的方式来执行，会依次去获取每一条聚簇索引记录的锁。不过因为`number`值为`8`的记录已经被`T1`加了`X型正经记录锁`，`T2`想得却得不到，只能眼巴巴的进行阻塞状态，此时的`SHOW ENGINE INNODB STATUS`也能证明我们的猜想（只截取了一部分）：

```sql
---TRANSACTION 39764, ACTIVE 34 sec fetching rows
mysql tables in use 1, locked 1
LOCK WAIT 3 lock struct(s), heap size 1160, 1 row lock(s)
MySQL thread id 20, OS thread handle 123145471168512, query id 590 localhost 127.0.0.1 root Sending data
SELECT * FROM hero WHERE country = '吴' FOR UPDATE
------- TRX HAS BEEN WAITING 34 SEC FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 287 page no 3 n bits 72 index PRIMARY of table `xiaohaizi`.`hero` trx id 39764 lock_mode X locks rec but not gap waiting
Record lock, heap no 4 PHYSICAL RECORD: n_fields 5; compact format; info bits 0
 0: len 4; hex 80000008; asc     ;;
 1: len 6; hex 000000009b4a; asc      J;;
 2: len 7; hex 80000001d3012a; asc       *;;
 3: len 7; hex 63e69bb9e6938d; asc c      ;;
 4: len 3; hex e9ad8f; asc    ;;
```

可以看到`T2`正在等待获取`heap no`为`4`的记录上的`X型正经记录锁`（lock_mode X locks rec but not gap waiting）。

以上是很正常的阻塞逻辑，我们都可以分析出来，不过如果在`T2`中执行下边的`UPDATE`语句：

```sql
# T2中，隔离级别为READ COMMITTED
mysql> BEGIN;
Query OK, 0 rows affected (0.00 sec)

mysql> UPDATE hero SET name = 'xxx' WHERE country = '吴';
Query OK, 1 row affected (0.02 sec)
Rows matched: 1  Changed: 1  Warnings: 0
```

WTF? 竟然没有阻塞，就这么随意地执行成功了？同样的`WHERE`条件，同样的执行计划，怎么`SELECT ... FOR UPDATE`和`UPDATE`语句的加锁情况不一样？

## 原因

哈哈，是的，的确不一样。其实MySQL支持3种类型的读语句：

- 普通读（也称一致性读，英文名：Consistent Read）。

  这个就是指普通的SELECT语句，在末尾不加`FOR UPDATE`或者`LOCK IN SHARE MODE`的SELECT语句。普通读的执行方式是生成`ReadView`直接利用MVCC机制来进行读取，并不会对记录进行加锁。

  > 小贴士：
  >
  > 对于SERIALIZABLE隔离级别来说，如果autocommit系统变量被设置为OFF，那普通读的语句会转变为锁定读，和在普通的SELECT语句后边加LOCK IN SHARE MODE达成的效果一样。

- 锁定读（英文名：Locking Read）。

  这个就是事务在读取记录之前，需要先获取该记录对应的锁。当然，获取什么类型的锁取决于当前事务的隔离级别、语句的执行计划、查询条件等因素，具体可参见：

- 半一致性读（英文名：Semi-Consistent Read）。

  这是一种夹在普通读和锁定读之间的一种读取方式。它只在`READ COMMITTED`隔离级别下（或者在开启了innodb_locks_unsafe_for_binlog系统变量的情况下）使用`UPDATE`语句时才会使用。具体的含义就是当`UPDATE`语句读取已经被其他事务加了锁的记录时，`InnoDB`会将该记录的最新提交的版本读出来，然后判断该版本是否与`UPDATE`语句中的`WHERE`条件相匹配，如果不匹配则不对该记录加锁，从而跳到下一条记录；如果匹配则再次读取该记录并对其进行加锁。这样子处理只是为了让`UPDATE`语句尽量少被别的语句阻塞。

  > 小贴士：
  >
  > 半一致性读只适用于对聚簇索引记录加锁的情况，并不适用于对二级索引记录加锁的情况。

很显然，我们上边所唠叨的例子中是因为事务`T2`执行`UPDATE`语句时使用了半一致性读，判断`number`列值为`8`和`15`这两条记录的最新提交版本的`country`列值均不为`UPDATE`语句中`WHERE`条件中的`'吴'`，所以直接就跳过它们，不对它们加锁了。

本知识点容易被忽略，各位同学在工作过程中分析的时候别忘记考虑一下`Semi-Consistent Read`喔，码字不易，有帮助帮着转发喔，么么哒～

# 死锁分析

标签： MySQL是怎样运行的

------

如果我们的业务处在一个非常初级的阶段，并发程度比较低，那么我们可以几年都遇不到一次死锁问题的发生，反之，我们业务的并发程度非常高，那么时不时爆出的死锁问题肯定让我们非常挠头。不过在死锁问题发生时，很多没有经验的同学的第一反应就是成为一直鸵鸟：这玩意儿很高深，我也看不懂，听天由命吧，又不是一直发生。其实如果大家认真研读了我们之前写了3篇关于`MySQL`中语句加锁分析的文章，加上本篇关于死锁日志的分析，那么解决死锁问题应该也不是那么摸不着头脑的事情了。

## 准备工作

为了故事的顺利发展，我们需要建一个表：

```sql
CREATE TABLE hero (
    id INT,
    name VARCHAR(100),
    country varchar(100),
    PRIMARY KEY (id),
    KEY idx_name (name)
) Engine=InnoDB CHARSET=utf8;
```

我们为hero表的id列创建了聚簇索引，为name列创建了一个二级索引。这个hero表主要是为了存储三国时的一些英雄，我们向表中插入一些记录：

```sql
INSERT INTO hero VALUES
    (1, 'l刘备', '蜀'),
    (3, 'z诸葛亮', '蜀'),
    (8, 'c曹操', '魏'),
    (15, 'x荀彧', '魏'),
    (20, 's孙权', '吴');
```

现在表中的数据就是这样的：

```sql
mysql> SELECT * FROM hero;
+----+------------+---------+
| id | name       | country |
+----+------------+---------+
|  1 | l刘备      | 蜀      |
|  3 | z诸葛亮    | 蜀      |
|  8 | c曹操      | 魏      |
| 15 | x荀彧      | 魏      |
| 20 | s孙权      | 吴      |
+----+------------+---------+
5 rows in set (0.00 sec)
```

准备工作就做完了。

## 创建死锁情景

我们先创建一个发生死锁的情景，在`Session A`和`Session B`中分别执行两个事务，具体情况如下：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208232007992.webp)

我们分析一下：

- 从第③步中可以看出，`Session A`中的事务先对`hero`表聚簇索引的`id`值为1的记录加了一个`X型正经记录锁`。

- 从第④步中可以看出，`Session B`中的事务对`hero`表聚簇索引的`id`值为3的记录加了一个`X型正经记录锁`。

- 从第⑤步中可以看出，`Session A`中的事务接着想对`hero`表聚簇索引的`id`值为3的记录也加了一个`X型正经记录锁`，但是与第④步中`Session B`中的事务加的锁冲突，所以`Session A`进入阻塞状态，等待获取锁。

- 从第⑥步中可以看出，`Session B`中的事务想对`hero`表聚簇索引的`id`值为1的记录加了一个`X型正经记录锁`，但是与第③步中`Session A`中的事务加的锁冲突，而此时`Session A`和`Session B`中的事务循环等待对方持有的锁，死锁发生，被`MySQL`服务器的死锁检测机制检测到了，所以选择了一个事务进行回滚，并向客户端发送一条消息：

  ```vbnet
  ERROR 1213 (40001): Deadlock found when trying to get lock; try restarting transaction
  ```

以上是我们从语句加了什么锁的角度出发来进行死锁情况分析的，但是实际应用中我们可能压根儿不知道到底是哪几条语句产生了死锁，我们需要根据`MySQL`在死锁发生时产生的死锁日志来逆向定位一下到底是什么语句产生了死锁，从而再优化我们的业务。

### 查看死锁日志

设计`InnoDB`的大叔给我们提供了`SHOW ENGINE INNODB STATUS`命令来查看关于InnoDB存储引擎的一些状态信息，其中就包括了系统最近一次发生死锁时的加锁情况。在上边例子中的死锁发生时，我们运行一下这个命令：

```perl
mysql> SHOW ENGINE INNODB STATUS\G
...省略了好多其他信息
------------------------
LATEST DETECTED DEADLOCK
------------------------
2019-06-20 13:39:19 0x70000697e000
*** (1) TRANSACTION:
TRANSACTION 30477, ACTIVE 10 sec starting index read
mysql tables in use 1, locked 1
LOCK WAIT 3 lock struct(s), heap size 1160, 2 row lock(s)
MySQL thread id 2, OS thread handle 123145412648960, query id 46 localhost 127.0.0.1 root statistics
select * from hero where id = 3 for update
*** (1) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 171 page no 3 n bits 72 index PRIMARY of table `dahaizi`.`hero` trx id 30477 lock_mode X locks rec but not gap waiting
Record lock, heap no 3 PHYSICAL RECORD: n_fields 5; compact format; info bits 0
 0: len 4; hex 80000003; asc     ;;
 1: len 6; hex 000000007517; asc     u ;;
 2: len 7; hex 80000001d0011d; asc        ;;
 3: len 10; hex 7ae8afb8e8919be4baae; asc z         ;;
 4: len 3; hex e89c80; asc    ;;

*** (2) TRANSACTION:
TRANSACTION 30478, ACTIVE 8 sec starting index read
mysql tables in use 1, locked 1
3 lock struct(s), heap size 1160, 2 row lock(s)
MySQL thread id 3, OS thread handle 123145412927488, query id 47 localhost 127.0.0.1 root statistics
select * from hero where id = 1 for update
*** (2) HOLDS THE LOCK(S):
RECORD LOCKS space id 171 page no 3 n bits 72 index PRIMARY of table `dahaizi`.`hero` trx id 30478 lock_mode X locks rec but not gap
Record lock, heap no 3 PHYSICAL RECORD: n_fields 5; compact format; info bits 0
 0: len 4; hex 80000003; asc     ;;
 1: len 6; hex 000000007517; asc     u ;;
 2: len 7; hex 80000001d0011d; asc        ;;
 3: len 10; hex 7ae8afb8e8919be4baae; asc z         ;;
 4: len 3; hex e89c80; asc    ;;

*** (2) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 171 page no 3 n bits 72 index PRIMARY of table `dahaizi`.`hero` trx id 30478 lock_mode X locks rec but not gap waiting
Record lock, heap no 2 PHYSICAL RECORD: n_fields 5; compact format; info bits 0
 0: len 4; hex 80000001; asc     ;;
 1: len 6; hex 000000007517; asc     u ;;
 2: len 7; hex 80000001d00110; asc        ;;
 3: len 7; hex 6ce58898e5a487; asc l      ;;
 4: len 3; hex e89c80; asc    ;;

*** WE ROLL BACK TRANSACTION (2)
------------
...省略了好多其他信息
```

我们只关心最近发生的死锁信息，所以就把以`LATEST DETECTED DEADLOCK`这一部分给单独提出来分析一下。下边我们就逐行看一下这个输出的死锁日志都是什么意思：

- 首先看第一句：

  ```yaml
  2019-06-20 13:39:19 0x70000697e000
  ```

  这句话的意思就是死锁发生的时间是：2019-06-20 13:39:19，后边的一串十六进制`0x70000697e000`表示的操作系统为当前session分配的线程的线程id。

- 然后是关于死锁发生时第一个事务的有关信息：

  ```python
  *** (1) TRANSACTION:
  
  # 为事务分配的id为30477，事务处于ACTIVE状态已经10秒了，事务现在正在做的操作就是：“starting index read”
  TRANSACTION 30477, ACTIVE 10 sec starting index read
  
  # 此事务使用了1个表，为1个表上了锁（此处不是说为该表加了表锁，只要不是进行一致性读的表，都需要加锁，具体怎么加锁请看加锁语句分析或者小册章节）
  mysql tables in use 1, locked 1
  
  # 此事务处于LOCK WAIT状态，拥有3个锁结构（2个行锁结构，1个表级别X型意向锁结构，锁结构在小册中重点介绍过），heap size是为了存储锁结构而申请的内存大小（我们可以忽略），其中有2个行锁的结构
  LOCK WAIT 3 lock struct(s), heap size 1160, 2 row lock(s)
  
  # 本事务所在线程的id是2（MySQL自己命名的线程id），该线程在操作系统级别的id就是那一长串数字，当前查询的id为46（MySQL内部使用，可以忽略），还有用户名主机信息
  MySQL thread id 2, OS thread handle 123145412648960, query id 46 localhost 127.0.0.1 root statistics
  
  # 本事务发生阻塞的语句
  select * from hero where id = 3 for update
  
  # 本事务当前在等待获取的锁：
  *** (1) WAITING FOR THIS LOCK TO BE GRANTED:
  
  # 等待获取的表空间ID为151，页号为3，也就是表hero的PRIMAY索引中的某条记录的锁（n_bits是为了存储本页面的锁信息而分配的一串内存空间，小册中有详细介绍），该锁的类型是X型正经记录锁（rec but not gap）
  RECORD LOCKS space id 171 page no 3 n bits 72 index PRIMARY of table `dahaizi`.`hero` trx id 30477 lock_mode X locks rec but not gap waiting
  
  # 该记录在页面中的heap_no为2，具体的记录信息如下：
  Record lock, heap no 3 PHYSICAL RECORD: n_fields 5; compact format; info bits 0
  
  # 这是主键值
  0: len 4; hex 80000003; asc     ;;
  
  # 这是trx_id隐藏列
  1: len 6; hex 000000007517; asc     u ;;
  
  # 这是roll_pointer隐藏列
  2: len 7; hex 80000001d0011d; asc        ;;
  
  # 这是name列
  3: len 10; hex 7ae8afb8e8919be4baae; asc z         ;;
  
  # 这是country列
  4: len 3; hex e89c80; asc    ;;
  ```

  从这个信息中可以看出，`Session A`中的事务为2条记录生成了锁结构，但是其中有一条记录上的`X型正经记录锁`（rec but not gap）并没有获取到，没有获取到锁的这条记录的位置是：表空间ID为151，页号为3，heap_no为2。当然，设计`InnoDB`的大叔还贴心的给出了这条记录的详细情况，它的主键值为`80000003`，这其实是InnoDB内部存储使用的格式，其实就代表数字`3`，也就是该事务在等待获取`hero`表聚簇索引主键值为`3`的那条记录的`X型`正经记录锁。

- 然后是关于死锁发生时第二个事务的有关信息：

  其中的大部分信息我们都已经介绍过了，我们就挑重要的说：

  ```python
  *** (2) TRANSACTION:
  TRANSACTION 30478, ACTIVE 8 sec starting index read
  mysql tables in use 1, locked 1
  3 lock struct(s), heap size 1160, 2 row lock(s)
  MySQL thread id 3, OS thread handle 123145412927488, query id 47 localhost 127.0.0.1 root statistics
  select * from hero where id = 1 for update
  
  # 表示该事务获取到的锁信息
  *** (2) HOLDS THE LOCK(S):
  RECORD LOCKS space id 171 page no 3 n bits 72 index PRIMARY of table `dahaizi`.`hero` trx id 30478 lock_mode X locks rec but not gap
  Record lock, heap no 3 PHYSICAL RECORD: n_fields 5; compact format; info bits 0
  
  # 主键值为3
  0: len 4; hex 80000003; asc     ;;
  1: len 6; hex 000000007517; asc     u ;;
  2: len 7; hex 80000001d0011d; asc        ;;
  3: len 10; hex 7ae8afb8e8919be4baae; asc z         ;;
  4: len 3; hex e89c80; asc    ;;
  
  # 表示该事务等待获取的锁信息
  *** (2) WAITING FOR THIS LOCK TO BE GRANTED:
  RECORD LOCKS space id 171 page no 3 n bits 72 index PRIMARY of table `dahaizi`.`hero` trx id 30478 lock_mode X locks rec but not gap waiting
  Record lock, heap no 2 PHYSICAL RECORD: n_fields 5; compact format; info bits 0
  
  # 主键值为1
  0: len 4; hex 80000001; asc     ;;
  1: len 6; hex 000000007517; asc     u ;;
  2: len 7; hex 80000001d00110; asc        ;;
  3: len 7; hex 6ce58898e5a487; asc l      ;;
  4: len 3; hex e89c80; asc    ;;
  ```

  从上边的输出可以看出来，`Session B`中的事务获取了`hero`表聚簇索引主键值为`3`的记录的`X型正经记录锁`，等待获取`hero`表聚簇索引主键值为`1`的记录的`X型正经记录锁`（隐含的意思就是这个`hero`表聚簇索引主键值为`1`的记录的`X型正经记录锁`已经被`SESSION A`中的事务获取到了）。

- 看最后一部分：

  ```diff
  *** WE ROLL BACK TRANSACTION (2)
  ```

  最终InnoDB存储引擎决定回滚第2个食物，也就是`Session B`中的那个事务。

## 思索分析的思路

1. 查看死锁日志时，首先看一下发生死锁的事务等待获取锁的语句都是啥。

   本例中，发现`SESSION A`发生阻塞的语句是：

   ```sql
   select * from hero where id = 3 for update
   ```

   `SESSION B`发生阻塞的语句是：

   ```sql
   select * from hero where id = 1 for update
   ```

   然后切记：到自己的业务代码中找出这两条语句所在事务的其他语句。

2. 找到发生死锁的事务中所有的语句之后，对照着事务获取到的锁和正在等待的锁的信息来分析死锁发生过程。

   从死锁日志中可以看出来，`SESSION A`获取了`hero`表聚簇索引`id`值为1的记录的`X型正经记录锁`（这其实是从`SESSION B`正在等待的锁中获取的），查看`SESSION A`中的语句，发现是下边这个语句造成的（对照着语句加锁分析那三篇文章）:

   ```sql
   select * from hero where id = 1 for update;
   ```

   还有`SESSION B`获取了`hero`表聚簇索引`id`值为3的记录的`X型正经记录锁`，查看`SESSION B`中的语句，发现是下边这个语句造成的（对照着语句加锁分析那三篇文章）:

   ```sql
   select * from hero where id = 3 for update;
   ```

   然后看`SESSION A`正在等待`hero`表聚簇索引`id`值为3的记录的`X型正经记录锁`，这个是由于下边这个语句造成的：

   ```sql
   select * from hero where id = 3 for update;
   ```

   然后看`SESSION B`正在等待`hero`表聚簇索引`id`值为1的记录的`X型正经记录锁`，这个是由于下边这个语句造成的：

   ```sql
   select * from hero where id = 1 for update;
   ```

   然后整个死锁形成过程就根据死锁日志给还原出来了。