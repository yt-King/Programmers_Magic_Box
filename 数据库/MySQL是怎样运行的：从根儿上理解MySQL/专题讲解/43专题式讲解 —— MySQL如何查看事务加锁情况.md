# MySQL如何查看事务加锁情况

标签： MySQL是怎样运行的

------

> 小贴士：
>
> 本篇文章算是回答一些同学的提问，以MySQL 5.7为例。

我们都知道`MySQL`的`InnoDB`存储引擎是支持事务功能的，不过在很多同学的潜意识中，只有把若干语句包含在`BEGIN`/`START TRANSACTION`、`COMMIT`中间，才算是开启一个事务，就像这样：

```ini
BEGIN;
语句1;
语句2;
...
语句n;
COMMIT;
```

其实不是这样的，每个涉及到使用`InnoDB`作为存储引擎的表的语句，都会在事务中执行。我们稍后详细看一下。

## 自动提交与手动提交

设计MySQL的大叔提供了一个称之为`autocommit`的系统变量，如下所示：

```sql
mysql> SHOW VARIABLES LIKE 'autocommit';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| autocommit    | ON    |
+---------------+-------+
1 row in set (0.15 sec)
```

该系统变量含义如下：

- 当`autocommit`的值为`ON`时，意味着开启自动提交，则每个`SQL`语句将形成一个独立的事务，当该语句执行完成时，对应的事务也就提交了。

  有同学有疑问，如果在`autocommit`的值为`ON`时，我写的一条SQL语句中既没有增加/修改/删除记录，也没有对记录加锁，比方说这样的一条利用MVCC进行读取的SELECT语句：

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
  |     22 | g关羽      | 蜀      |
  |     30 | d典韦      | 魏      |
  +--------+------------+---------+
  7 rows in set (0.02 sec)
  
  mysql>
  ```

  那这条语句也相当与在事务中执行的么？是的，其实区别语句是否在事务中执行的依据其实是代码中是否调用了`trx_start_low`这个函数，有感兴趣的同学可以去看看代码哈～

- 当`autocommit`的值为`OFF`时，意味着禁用自动提交，我们写的若干个增删改查语句都算作是一个事务中的语句，直到我们手动的使用`COMMIT`语句来提交这个事务。

如果我们当前会话的系统变量`autocommit`的值为`ON`，意味着开启了自动提交。此时如果我们想把若干个语句放到一个事务中执行，那就需要显式地写出`BEGIN`或者`START TRANSACTION`语句来禁用自动提交。

## 查看事务加锁的几种方法

有同学会使用`information_schema`数据库下的一些表来观察当前数据库中的一些事务和锁的情况，诸如：`innodb_locks`、`innodb_lock_wait`。但是千万要记住：在使用它们之前一定先得知道它们是干啥的，然后再去使用。不要在连这些表是干啥的情况下就去贸然使用，最后还能得出一个结论～。下边我们看看这几个表都是干嘛使的：

- `innodb_locks`表：该表中会记录一些锁信息：

  - 如果一个事务想要获取某个锁但未获取到，该锁信息将被记录。
  - 如果一个事务因为获取到了某个锁，但是这个锁阻塞了别的事务的话，该锁信息会被记录。

  这里需要大家注意的是：只有当系统中发生了某个事务因为获取不到锁而阻塞的情况才会向该表中写入记录。如果未发生阻塞，比方说我们在`T1`中执行：

  ```sql
  # T1
  mysql> BEGIN;
  Query OK, 0 rows affected (0.00 sec)
  
  mysql> SELECT * FROM hero WHERE number = 1 for update;
  +--------+---------+---------+
  | number | name    | country |
  +--------+---------+---------+
  |      1 | l刘备   | 蜀      |
  +--------+---------+---------+
  1 row in set (0.01 sec)
  ```

  很显然`T1`已经获取到了`hero`表中`number`值为`1`的聚簇索引记录的`X型正经记录锁`（number列为主键），但是由于并未发生阻塞，该锁的信息并不会记录在`innodb_locks`表中：

  ```sql
  mysql> SELECT * FROM innodb_locks;
  Empty set, 1 warning (0.02 sec)
  ```

- `innodb_lock_wait`：表明当前系统中因为等待哪些锁而让事务进入阻塞状态。

  比方说接着上边的例子，我们在`事务T2`中接着执行：

  ```ini
  # T2
  mysql> BEGIN;
  Query OK, 0 rows affected (0.00 sec)
  
  mysql> SELECT * FROM hero WHERE number = 1 lock in share mode;
  # 进入阻塞状态
  ```

  因为获取不到`hero`表中`number`值为`1`的聚簇索引记录的`S型正经记录锁`，所以事务`T2`进入阻塞状态，那么在`innodb_lock_wait`表中会留下一条记录：

  ```sql
  mysql> select * from innodb_lock_wait;
  +-------------------+-------------------------+-----------------+------------------+
  | requesting_trx_id | requested_lock_id       | blocking_trx_id | blocking_lock_id |
  +-------------------+-------------------------+-----------------+------------------+
  | 281479631936736   | 281479631936736:272:3:2 | 38938           | 38938:272:3:2    |
  +-------------------+-------------------------+-----------------+------------------+
  1 row in set, 1 warning (0.01 sec)
  ```

  因为此时`T2`发生了阻塞，在`innodb_locks`表中也可以体现出来：

  ```sql
  mysql> SELECT * FROM innodb_locks;
  +-------------------------+-----------------+-----------+-----------+--------------------+------------+------------+-----------+----------+-----------+
  | lock_id                 | lock_trx_id     | lock_mode | lock_type | lock_table         | lock_index | lock_space | lock_page | lock_rec | lock_data |
  +-------------------------+-----------------+-----------+-----------+--------------------+------------+------------+-----------+----------+-----------+
  | 281479631936736:272:3:2 | 281479631936736 | S         | RECORD    | `xiaohaizi`.`hero` | PRIMARY    |        272 |         3 |        2 | 1         |
  | 38938:272:3:2           | 38938           | X         | RECORD    | `xiaohaizi`.`hero` | PRIMARY    |        272 |         3 |        2 | 1         |
  +-------------------------+-----------------+-----------+-----------+--------------------+------------+------------+-----------+----------+-----------+
  2 rows in set, 1 warning (0.02 sec)
  ```

不过我们看到，在查询`innodb_locks`和`innodb_lock_wait`表的时候都伴随着一个`warning`，我们看一下系统在警告神马：

```markdown
mysql> SHOW WARNINGS\G
*************************** 1. row ***************************
  Level: Warning
   Code: 1681
Message: 'INFORMATION_SCHEMA.INNODB_LOCKS' is deprecated and will be removed in a future release.
1 row in set (0.01 sec)
```

其实是因为`innodb_locks`和`innodb_lock_wait`在我目前使用的版本（MySQL 5.7.21）中被标记为过时的，并且提示在未来的版本中可能被移除，其实也就是不鼓励我们使用这两个表来获取相关的锁信息。

另外，我们还可以使用`SHOW ENGINE INNODB STATUS`这个命令来查看当前系统中每个事务都加了哪些锁：

```markdown
mysql> SHOW ENGINE INNODB STATUS\G
...此处省略很多信息
------------
TRANSACTIONS
------------
Trx id counter 38944
Purge done for trx's n:o < 38452 undo n:o < 0 state: running but idle
History list length 262
Total number of lock structs in row lock hash table 2
LIST OF TRANSACTIONS FOR EACH SESSION:
---TRANSACTION 281479631937824, not started
0 lock struct(s), heap size 1160, 0 row lock(s)
---TRANSACTION 38938, ACTIVE 1875 sec
2 lock struct(s), heap size 1160, 1 row lock(s)
MySQL thread id 29, OS thread handle 123145576628224, query id 690 localhost 127.0.0.1 root
Trx read view will not see trx with id >= 38938, sees < 38938

...此处省略很多信息
```

由于输出的内容太多，为方便起见，我们只保留了关于`TRANSACTIONS`的相关信息，这里列出了每个事务获取锁的情况。如果我们想看到更详细的加锁情况，可以开启`innodb_status_output_locks`：

```ini
mysql> SET GLOBAL innodb_status_output_locks = ON;
Query OK, 0 rows affected (0.01 sec)
```

那每个锁的详细情况就被列出来了：

```yaml
mysql> SHOW ENGINE INNODB STATUS\G
...此处省略很多信息

------------
TRANSACTIONS
------------
Trx id counter 38945
Purge done for trx's n:o < 38452 undo n:o < 0 state: running but idle
History list length 262
Total number of lock structs in row lock hash table 2
LIST OF TRANSACTIONS FOR EACH SESSION:
---TRANSACTION 281479631937824, not started
0 lock struct(s), heap size 1160, 0 row lock(s)
---TRANSACTION 38938, ACTIVE 2122 sec
2 lock struct(s), heap size 1160, 1 row lock(s)
MySQL thread id 29, OS thread handle 123145576628224, query id 690 localhost 127.0.0.1 root
Trx read view will not see trx with id >= 38938, sees < 38938
TABLE LOCK table `xiaohaizi`.`hero` trx id 38938 lock mode IX
RECORD LOCKS space id 272 page no 3 n bits 80 index PRIMARY of table `xiaohaizi`.`hero` trx id 38938 lock_mode X locks rec but not gap
Record lock, heap no 2 PHYSICAL RECORD: n_fields 5; compact format; info bits 0
 0: len 4; hex 80000001; asc     ;;
 1: len 6; hex 000000009624; asc      $;;
 2: len 7; hex 80000001d00110; asc        ;;
 3: len 7; hex 6ce58898e5a487; asc l      ;;
 4: len 3; hex e89c80; asc    ;;

...此处省略很多信息
```

当然，我们现在并不准备唠叨输出内容中的每个字段都是啥意思，之后有时间再撰文描述吧～