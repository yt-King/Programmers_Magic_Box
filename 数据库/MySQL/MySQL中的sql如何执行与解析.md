# MySQL中的sql如何执行与解析

## MySQL 驱动

我们的系统在和 MySQL 数据库进行通信的时候是MySQL 驱动在底层帮我们做了对数据库的连接，只有建立了连接了，才能够有后面的交互。

![image-20220708112020316](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207081120365.png)

不过在实际使用中肯定是多用户的情况，一般web系统是部署在tomcat容器中，然后tomcat并发处理多个请求，这就导致每个请求都要建立连接，java 系统在通过 MySQL 驱动和 MySQL 数据库连接的时候是**基于 TCP/IP 协议**的，所以如果**每个请求**都是新建连接和销毁连0接，那这样势必会造成不必要的浪费和性能的下降，所以就用到了数据库连接池技术。

> **数据库连接池**：维护一定的连接数，方便系统获取连接，使用就去池子中获取，用完放回去就可以了，我们不需要关心连接的创建与销毁，也不需要关心线程池是怎么去维护这些连接的。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207081129357.png" alt="image-20220708112914290"  />

MySQL 的架构体系中也已经提供了这样的一个池子，也是数据库连池。双方都是通过数据库连接池来管理各个连接的，这样一方面线程之前不需要是争抢连接，更重要的是不需要反复的创建的销毁连接。

## 网络连接必须由线程来处理

网络中的连接都是由线程来处理的，所谓网络连接说白了就是**一次请求**，每次请求都会有相应的线程去处理的。也就是说对于 SQL 语句的请求在 MySQL 中是由一个个的**线程去处理**的。

## SQL 接口

MySQL 中处理请求的线程在获取到请求以后获取 SQL 语句去交给 SQL 接口去处理。

## 查询解析器

将 SQL 接口传递过来的 SQL 语句进行解析，翻译成 MySQL 自己能认识的语言，解析之后还要选择**最优的查询路径**

## MySQL 查询优化器

> 查询优化器内部具体怎么实现的我们不需要是关心，我需要知道的是  MySQL  会帮我去使用他自己认为的最好的方式去优化这条  SQL  语句，并生成一条条的执行计划，比如你创建了多个索引，MySQL 会依据成本最小原则来选择使用对应的索引，这里的成本主要包括两个方面, IO 成本和 CPU 成本

**IO 成本**: 即从磁盘把数据加载到内存的成本，默认情况下，读取数据页的 IO 成本是 1，MySQL 是以页的形式读取数据的，即当用到某个数据时，并不会只读取这个数据，而会把这个数据相邻的数据也一起读到内存中，这就是有名的程序局部性原理，所以 MySQL 每次会读取一整页，一页的成本就是 1。所以 IO 的成本主要和页的大小有关

**CPU 成本**：将数据读入内存后，还要检测数据是否满足条件和排序等 CPU 操作的成本，显然它与行数有关，默认情况下，检测记录的成本是 0.2。

MySQL 优化器 会计算 「IO 成本 + CPU」 成本最小的那个索引来执行，优化器执行选出最优索引等步骤后，会去调用存储引擎接口，开始去执行被 MySQL 解析过和优化过的 SQL 语句

## 存储引擎

查询优化器会调用存储引擎的接口，去执行  SQL，也就是说真正执行  SQL  的动作是在存储引擎中完成的。数据是被存放在内存或者是磁盘中的（存储引擎是一个非常重要的组件，后面会详细介绍）

## 执行器

执行器是一个非常重要的组件，因为前面那些组件的操作最终必须通过执行器去调用存储引擎接口才能被执行。执行器最终最根据一系列的执行计划去调用存储引擎的接口去完成  SQL  的执行

![image-20220708114452189](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207081144256.png)

当我们系统发出查询去交给 MySQL 的时候，MySQL 会按照上面介绍的一系列的流程最终通过执行器调用存储引擎去执行。在执行这个 SQL 的时候 SQL 语句对应的数据要么是在内存中，要么是在磁盘中，如果直接在磁盘中操作，那这样的随机IO读写的速度肯定让人无法接受的，所以每次在执行 SQL 的时候都会将其数据**加载到内存**中，这块内存就是 InnoDB 中一个非常重要的组件：**缓冲池 Buffer Pool**

## Buffer Pool

`Buffer Pool` （缓冲池）是 InnoDB 存储引擎中非常重要的**内存结构**，顾名思义，缓冲池其实就是类似  Redis  一样的作用，起到一个缓存的作用，因为我们都知道 MySQL 的数据**最终是存储在磁盘中**的，如果没有这个 Buffer Pool  那么我们每次的数据库请求都会磁盘中查找，这样必然会存在 IO 操作，这肯定是无法接受的。但是有了 Buffer Pool 就是我们第一次在查询的时候会将查询的结果存到  Buffer Pool 中，这样后面再有请求的时候就会**先从缓冲池中去查询**，如果没有再去磁盘中查找，然后在放到  Buffer Pool 中。

![image-20220708143518132](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207081435194.png)

按照上面的那幅图，一条修改SQL 语句的执行步骤大致是这样子的

- innodb 存储引擎会在缓冲池中查找这条数据是否存在
- 发现不存在，那么就会去磁盘中加载，并将其存放在缓冲池中
- 该条记录会被加上一个独占锁

## undo 日志文件：记录数据被修改前的样子

undo 顾名思义，就是没有做，没发生的意思。undo log  就是没有发生事情（原本事情是什么）的一些日志

在准备更新一条语句的时候，该条语句已经被加载到 Buffer pool 中了，实际上这里还有这样的操作，就是在将该条语句加载到 Buffer Pool 中的时候同时会往 undo 日志文件中插入一条日志，也就是将这条记录的**原来的值记录下来**。

**这样做的目的是什么**？

Innodb 存储引擎的最大特点就是支持事务，如果本次更新失败，也就是事务提交失败，那么该事务中的所有的操作都必须回滚到执行前的样子，也就是说当事务失败的时候，也不会对原始数据有影响：

![image-20220708145227522](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207081452594.png)

## redo 日志文件：记录数据被修改后的样子

除了从磁盘中加载文件和将操作前的记录保存到 undo 日志文件中，其他的操作是在内存中完成的，内存中的数据的特点就是：断电丢失。如果此时 MySQL 所在的服务器宕机了，那么 Buffer Pool 中的数据会全部丢失的。这个时候 redo 日志文件就需要来大显神通了

**画外音：redo 日志文件是 InnoDB 特有的，他是存储引擎级别的，不是 MySQL 级别的**

redo 记录的是数据修改之后的值，不管事务是否提交都会记录下来，例如，此时将要做的是update students set stuName='小强' where id=1; 那么这条操作就会被记录到 redo log buffer 中（因为MySQL 为了提高效率，所以将这些操作都先放在内存中去完成，然后会在某个时机将其持久化到磁盘中）。

---

到此为止 MySQL 的执行器调用存储引擎将一条 SQL 加载到缓冲池和记录日志的流程如下：

- 准备更新一条 SQL 语句
- MySQL（innodb）会先去缓冲池（BufferPool）中去查找这条数据，没找到就会去磁盘中查找，如果查找到就会将这条数据加载到缓冲池（BufferPool）中
- 在加载到 Buffer Pool 的同时，会将这条数据的原始记录保存到 undo 日志文件中
- innodb 会在 Buffer Pool 中执行更新操作
- 更新后的数据会记录在 redo log buffer 中

上面说的步骤都是在正常情况下的操作，但是程序的设计和优化并不仅是为了这些正常情况而去做的，也是为了**那些临界区和极端情况下出现的问题去优化**设计的。

比如遇到MySQL宕机，那么 MySQL 会认为本次事务是失败的，所以数据依旧是更新前的样子，并不会有任何的影响。然后将  redo Log Buffer 中的数据持久化到磁盘中，就是将 redo log buffer 中的数据写入到 redo log 磁盘文件中，一般情况下，redo log Buffer 数据写入磁盘的策略是立即刷入磁盘。

![image-20220708150710084](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207081507135.png)

如果 redo log Buffer 刷入磁盘后，数据库服务器宕机了，那我们更新的数据怎么办？此时数据是在内存中，数据岂不是丢失了？不，这次数据就不会丢失了，因为 redo log buffer 中的数据已经被写入到磁盘了，已经被持久化了，就算数据库宕机了，在下次重启的时候 MySQL 也会将 redo 日志文件内容恢复到 Buffer Pool 中（这边的理解是和  Redis  的持久化机制是差不多的，在  Redis  启动的时候会检查 rdb 或者是 aof 或者是两者都检查，根据持久化的文件来将数据恢复到内存中），到此为止**大体流程**如下：

- 准备更新一条 SQL 语句
- MySQL（innodb）会先去缓冲池（BufferPool）中去查找这条数据，没找到就会去磁盘中查找，如果查找到就会将这条数据加载到缓冲池（BufferPool）中
- 在加载到 Buffer Pool 的同时，会将这条数据的原始记录保存到 undo 日志文件中
- innodb 会在 Buffer Pool 中执行更新操作
- 更新后的数据会记录在 redo log buffer 中
- MySQL 提交事务的时候，会将 redo log buffer 中的数据写入到 redo 日志文件中 刷磁盘可以通过 innodb_flush_log_at_trx_commit 参数来设置
- - 值为 0 表示不刷入磁盘
  - 值为 1 表示立即刷入磁盘
  - 值为 2 表示先刷到 os cache
- myslq 重启的时候会将 redo 日志恢复到缓冲池中

## bin log 日志文件：记录整个操作过程

> bin log属于是  MySQL  级别的日志，redo log记录的东西是偏向于物理性质的，如：“对什么数据，做了什么修改”。bin log是偏向于逻辑性质的，类似于：“对 students 表中的 id 为 1 的记录做了更新操作” 两者的主要特点总结如下:

| 性质     | redo Log                                                     | bin Log                                                      |
| -------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 文件大小 | redo log 的大小是固定的（配置中也可以设置，一般默认的就足够了） | bin log 可通过配置参数max_bin log_size设置每个bin log文件的大小（但是一般不建议修改）。 |
| 实现方式 | redo log是InnoDB引擎层实现的（也就是说是 Innodb  存储引擎独有的） | bin log是  MySQL  层实现的，所有引擎都可以使用 bin log日志   |
| 记录方式 | redo log 采用循环写的方式记录，当写到结尾时，会回到开头循环写日志。 | bin log 通过追加的方式记录，当文件大小大于给定值后，后续的日志会记录到新的文件上 |
| 使用场景 | redo log适用于崩溃恢复(crash-safe)（这一点其实非常类似与 Redis 的持久化特征） | bin log 适用于主从复制和数据恢复                             |

### **bin log文件是如何刷入磁盘的**?

bin log 的刷盘是有相关的策略的，策略可以通过sync_bin log来修改，默认为 0，表示先写入 os cache，也就是说在提交事务的时候，数据不会直接到磁盘中，这样如果宕机bin log数据仍然会丢失。所以建议将sync_bin log设置为 1 表示直接将数据写入到磁盘文件中。

刷入 bin log 有以下几种模式

- **STATMENT**

基于 SQL 语句的复制(statement-based replication, SBR)，每一条会修改数据的 SQL 语句会记录到 bin log 中

【优点】：不需要记录每一行的变化，减少了 bin log 日志量，节约了 IO , 从而提高了性能

【缺点】：在某些情况下会导致主从数据不一致，比如执行sysdate()、sleep()等

- **ROW**

基于行的复制(row-based replication, RBR)，不记录每条SQL语句的上下文信息，仅需记录哪条数据被修改了

【优点】：不会出现某些特定情况下的存储过程、或 function、或 trigger 的调用和触发无法被正确复制的问题

【缺点】：会产生大量的日志，尤其是 alter table 的时候会让日志暴涨

- **MIXED**

基于 STATMENT 和 ROW 两种模式的**混合复制**( mixed-based replication, MBR )，一般的复制使用 STATEMENT 模式保存 bin log ，对于 STATEMENT 模式无法复制的操作使用 ROW 模式保存 bin log

> 那既然bin log也是日志文件，那它是在什么记录数据的呢？

其实 MySQL 在提交事务的时候，不仅仅会将 redo log buffer  中的数据写入到redo log 文件中，同时也会将本次修改的数据记录到 bin log文件中，同时会将本次修改的bin log文件名和修改的内容在bin log中的位置记录到redo log中，最后还会在redo log最后写入 commit 标记，这样就表示本次事务被成功的提交了。

![image-20220708151346367](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207081513462.png)

如果在数据被写入到bin log文件的时候，刚写完，数据库宕机了，**数据会丢失吗？**

首先可以确定的是，只要redo log最后没有 commit 标记，说明本次的事务**一定是失败**的。但是数据**没有丢失**，因为已经被记录到redo log的磁盘文件中了。在 MySQL 重启的时候，就会将 redo log 中的数据恢复（加载）到Buffer Pool中。

好了，到目前为止，一个更新操作基本介绍得差不多，但是是不是感觉少了哪件事情还没有做？因为这个时候被更新记录仅仅是在内存中执行的，哪怕是宕机又恢复了也仅仅是将更新后的记录加载到Buffer Pool中，这个时候 MySQL 数据库中的这条记录依旧是旧值，也就是说内存中的数据在我们看来依旧是脏数据，那这个时候怎么办呢？

其实 MySQL 会有一个后台线程，它会在某个时机将我们Buffer Pool中的脏数据刷到 MySQL 数据库中，这样就将内存和数据库的数据保持统一了。

![image-20220708151802831](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207081518906.png)

## 执行流程总结

- Buffer Pool 是 MySQL 的一个非常重要的组件，因为针对数据库的增删改操作都是在 Buffer Pool 中完成的
- Undo log 记录的是数据操作前的样子
- redo log 记录的是数据被操作后的样子（redo log 是 Innodb 存储引擎特有）
- bin log 记录的是整个操作记录（这个对于主从复制具有非常重要的意义）

从准备更新一条数据到事务的提交的流程描述

- 首先执行器根据 MySQL 的执行计划来查询数据，先是从缓存池中查询数据，如果没有就会去数据库中查询，如果查询到了就将其放到缓存池中
- 在数据被缓存到缓存池的同时，会写入 undo log 日志文件
- 更新的动作是在 BufferPool 中完成的，同时会将更新后的数据添加到 redo log buffer 中
- 完成以后就可以提交事务，在提交的同时会做以下三件事
  - 将redo log buffer中的数据刷入到 redo log 文件中
  - 将本次操作记录写入到 bin log文件中
  - 将 bin log 文件名字和更新内容在 bin log 中的位置记录到redo log中，同时在 redo log 最后添加 commit 标记

至此表示整个更新事务**已经完成**，接下来看一看sql是如何被解析的。

## SQL解析现状与场景

SQL解析是一项复杂的技术，一般都是由数据库厂商来掌握，当然也有公司专门提供[SQL解析的API ](https://www.sqlparser.com/)。由于这几年MySQL数据库中间件的兴起，需要支持读写分离、分库分表等功能，就必须从SQL中抽出表名、库名以及相关字段的值。因此像Java语言编写的Druid，C语言编写的MaxScale，Go语言编写的Kingshard等，都会对SQL进行部分解析。而真正把SQL解析技术用于数据库维护的产品较少，主要有如下几个:

- 美团开源的[SQLAdvisor  (opens new window)](https://github.com/Meituan-Dianping/SQLAdvisor)。它基于MySQL原生态词法解析，结合分析SQL中的where条件、聚合条件、多表Join关系给出索引优化建议。
- 去哪儿开源的Inception（原仓库已经删除，闭源了）。侧重于根据内置的规则，对SQL进行审核。
- 阿里的[Cloud DBA  (opens new window)](https://developer.aliyun.com/article/218442)。根据官方文档介绍，其也是提供SQL优化建议和改写。

上述产品都有非常合适的应用场景，在业界也被广泛使用。但是SQL解析的应用场景远远没有被充分发掘，比如：

- 基于表粒度的慢查询报表。比如，一个Schema中包含了属于不同业务线的数据表，那么从业务线的角度来说，其希望提供表粒度的慢查询报表。
- 生成SQL特征。将SQL语句中的值替换成问号，方便SQL归类。虽然可以使用正则表达式实现相同的功能，但是其Bug较多，可以参考pt-query-digest。比如pt-query-digest中，会把遇到的数字都替换成“?”，导致无法区别不同数字后缀的表。
- 高危操作确认与规避。比如，DBA不小心Drop数据表，而此类操作，目前还无有效的工具进行回滚，尤其是大表，其后果将是灾难性的。
- SQL合法性判断。为了安全、审计、控制等方面的原因，美团不会让研发人员直接操作数据库，而是提供RDS服务。尤其是对于数据变更，需要研发人员的上级主管进行业务上的审批。如果研发人员，写了一条语法错误的SQL，而RDS无法判断该SQL是否合法，就会造成不必要的沟通成本。

因此为了让所有有需要的业务都能方便的使用SQL解析功能，我们认为应该具有如下特性。

- 直接暴露SQL解析接口，使用尽量简单。比如，输入SQL，则输出表名、特征和优化建议。
- 接口的使用不依赖于特定的语言，否则维护和使用的代价太高。比如，以HTTP等方式提供服务。

## SQL解析的原理

SQL解析与优化是属于编译器范畴，和C等其他语言的解析没有本质的区别。其中分为，词法分析、语法和语义分析、优化、执行代码生成。对应到MySQL的部分，如下图：

![image-20220708154849576](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207081548631.png)

### 词法分析

SQL解析由词法分析和语法/语义分析两个部分组成。词法分析主要是把输入转化成一个个Token。其中**Token**中包含**Keyword**（也称symbol）和非**Keyword**。例如，SQL语句 select username from userinfo，在分析之后，会得到4个Token，其中有2个Keyword，分别为select和from：

| 关键字 | 非关键字 | 关键字 | 非关键字 |
| ------ | -------- | ------ | -------- |
| select | username | from   | userinfo |

通常情况下，词法分析可以使用`Flex`来生成，但是MySQL并未使用该工具，而是手写了词法分析部分（据说是为了效率和灵活性，参考此文）。具体代码在sql/lex.h和sql/sql_lex.cc文件中。

MySQL中的Keyword定义在sql/lex.h中，如下为部分Keyword：

```sql
{ "&&",               SYM(AND_AND_SYM)},
{ "<",                SYM(LT)},
{ "<=",               SYM(LE)},
{ "<>",               SYM(NE)},
{ "!=",               SYM(NE)},
{ "=",                SYM(EQ)},
{ ">",                SYM(GT_SYM)},
{ ">=",               SYM(GE)},
{ "<<",               SYM(SHIFT_LEFT)},
{ ">>",               SYM(SHIFT_RIGHT)},
{ "<=>",              SYM(EQUAL_SYM)},
{ "ACCESSIBLE",       SYM(ACCESSIBLE_SYM)},
{ "ACTION",           SYM(ACTION)},
{ "ADD",              SYM(ADD)},
{ "AFTER",            SYM(AFTER_SYM)},
{ "AGAINST",          SYM(AGAINST)},
{ "AGGREGATE",        SYM(AGGREGATE_SYM)},
{ "ALL",              SYM(ALL)},
```

词法分析的核心代码在sql/sql_lex.c文件中的，MySQLLex→lex_one_Token，有兴趣的同学可以下载源码研究。

### 语法分析

语法分析就是生成语法树的过程。这是整个解析过程中最精华，最复杂的部分，不过这部分MySQL使用了Bison来完成。即使如此，如何设计合适的数据结构以及相关算法，去存储和遍历所有的信息，也是值得在这里研究的。

#### 语法分析树

SQL语句：

```sql
select username, ismale from userinfo where age > 20 and level > 5 and 1 = 1
```

会生成如下语法树。

![image-20220711090543605](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207110905724.png)

#### MySQL语法分析树生成过程

全部的源码在sql/sql_yacc.yy中，在MySQL5.6中有17K行左右代码。这里列出涉及到SQL：

```sql
select username, ismale from userinfo where age > 20 and level > 5 and 1 = 1 
```

解析过程的部分代码摘录出来。其实有了Bison之后，SQL解析的难度也没有想象的那么大。特别是这里给出了解析的脉络之后。

```sql
select /*select语句入口*/:
 
          select_init
 
          {
 
            LEX *lex= Lex;
 
            lex->sql_command= SQLCOM_SELECT;
 
          }
 
        ;
 
select_init:
          SELECT_SYM /*select 关键字*/ select_init2
 
        | '(' select_paren ')' union_opt
 
        ;
 
select_init2:
          select_part2
          {
            LEX *lex= Lex;
            SELECT_LEX * sel= lex->current_select;
            if (lex->current_select->set_braces(0))
            {
              my_parse_error(ER(ER_SYNTAX_ERROR));
              MYSQL_YYABORT;
            }
            if (sel->linkage == UNION_TYPE &&
                sel->master_unit()->first_select()->braces)
            {
              my_parse_error(ER(ER_SYNTAX_ERROR));
              MYSQL_YYABORT;
            }
          }
          union_clause
        ;
select_part2:
          {
            LEX *lex= Lex;
            SELECT_LEX *sel= lex->current_select;
            if (sel->linkage != UNION_TYPE)
              mysql_init_select(lex);
            lex->current_select->parsing_place= SELECT_LIST;
          }
  
          select_options select_item_list /*解析列名*/
          {
            Select->parsing_place= NO_MATTER;
          }
          select_into select_lock_type
        ;
 
select_into:
          opt_order_clause opt_limit_clause {}
        | into
        | select_from /*from 字句*/
        | into select_from
        | select_from into
        ;
select_from:
          FROM join_table_list /*解析表名*/ where_clause /*where字句*/ group_clause having_clause
          opt_order_clause opt_limit_clause procedure_analyse_clause
          {
            Select->context.table_list=
              Select->context.first_name_resolution_table=
                Select->table_list.first;
          }
        | FROM DUAL_SYM where_clause opt_limit_clause
          /* oracle compatibility: oracle always requires FROM clause,
             and DUAL is system table without fields.
             Is "SELECT 1 FROM DUAL" any better than "SELECT 1" ?
          Hmmm :) */
        ;
 
where_clause:
          /* empty */  { Select->where= 0; }
        | WHERE
          {
            Select->parsing_place= IN_WHERE;
          }
          expr /*各种表达式*/
          {
            SELECT_LEX *select= Select;
            select->where= $3;
            select->parsing_place= NO_MATTER;
            if ($3)
              $3->top_level_item();
          }
        ;
  
/* all possible expressions */
expr:
           | expr and expr %prec AND_SYM
          {
            /* See comments in rule expr: expr or expr */
            Item_cond_and *item1;
            Item_cond_and *item3;
            if (is_cond_and($1))
            {
              item1= (Item_cond_and*) $1;
              if (is_cond_and($3))
              {
                item3= (Item_cond_and*) $3;
                /*
                  (X1 AND X2) AND (Y1 AND Y2) ==> AND (X1, X2, Y1, Y2)
                */
                item3->add_at_head(item1->argument_list());
                $$ = $3;
              }
              else
              {
                /*
                  (X1 AND X2) AND Y ==> AND (X1, X2, Y)
                */
                item1->add($3);
                $$ = $1;
              }
            }
            else if (is_cond_and($3))
            {
              item3= (Item_cond_and*) $3;
              /*
                X AND (Y1 AND Y2) ==> AND (X, Y1, Y2)
              */
              item3->add_at_head($1);
              $$ = $3;
            }
            else
            {
              /* X AND Y */
              $$ = new (YYTHD->mem_root) Item_cond_and($1, $3);
              if ($$ == NULL)
                MYSQL_YYABORT;
            }
          }
```

在大家浏览上述代码的过程，会发现Bison中嵌入了C++的代码。通过C++代码，把解析到的信息存储到相关对象中。例如表信息会存储到TABLE_LIST中，order_list存储order by子句里的信息，where字句存储在Item中。有了这些信息，再辅助以相应的算法就可以对SQL进行更进一步的处理了。

#### 核心数据结构及其关系

在SQL解析中，最核心的结构是SELECT_LEX，其定义在sql/sql_lex.h中。下面仅列出与上述例子相关的部分。

![image-20220711092127108](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207110921184.png)

上面图示中，列名username、ismale存储在item_list中，表名存储在table_list中，条件存储在where中。其中以where条件中的Item层次结构最深，表达也较为复杂，如下图所示。

![image-20220711092403190](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207110924274.png)

## SQL解析的应用

为了更深入的了解SQL解析器，这里给出2个应用SQL解析的例子。

### 无用条件去除

无用条件去除属于优化器的逻辑优化范畴，可以仅仅根据SQL本身以及表结构即可完成，这里通过图来分析以下四种情况。

a）1=1 and (m > 3 and n > 4)

![image-20220711092708491](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207110927587.png)

b）1=2 and (m > 3 and n > 4)

![image-20220711092822838](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207110928924.png)

c）1=1 or (m > 3 and n > 4)

![image-20220711092843726](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207110928804.png)

d）1=2 or (m > 3 and n > 4)

![image-20220711092910959](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-7/202207110929078.png)

### SQL特征生成

为了确保数据库，这一系统基础组件稳定、高效运行，业界有很多辅助系统。比如慢查询系统、中间件系统。这些系统采集、收到SQL之后，需要对SQL进行归类，以便统计信息或者应用相关策略。归类时，通常需要获取SQL特征。比如SQL:

```sql
select username, ismale from userinfo where age > 20 and level > 5；
```

SQL特征为：

```sql
select username, ismale from userinfo where age > ? and level > ? 
```

业界著名的慢查询分析工具pt-query-digest，通过正则表达式实现这个功能但是这类处理办法Bug较多。接下来就介绍如何使用SQL解析，完成SQL特征的生成。

SQL特征生成分两部分组成。

- a) 生成Token数组
- b) 根据Token数组，生成SQL特征

首先回顾在词法解析章节，我们介绍了SQL中的关键字，并且每个关键字都有一个16位的整数对应，而非关键字统一用ident表示，其也对应了一个16位整数。如下表：

| 标识 | select | from | where | >    | ?    | and  | ident |
| ---- | ------ | ---- | ----- | ---- | ---- | ---- | ----- |
| 整数 | 728    | 448  | 878   | 463  | 893  | 272  | 476   |

将一个SQL转换成特征的过程：

| 原SQL   | select | username           | from | userinfo           | where | age                | >    | 20   |
| ------- | ------ | ------------------ | ---- | ------------------ | ----- | ------------------ | ---- | ---- |
| SQL特征 | select | ident:length:value | from | ident:length:value | where | ident:length:value | >    | ?    |

在SQL解析过程中，可以很方便的完成Token数组的生成。而一旦完成Token数组的生成，就可以很简单的完成SQL特征的生成。SQL特征被广泛用于各个系统中，比如pt-query-digest需要根据特征对SQL归类，然而其基于正则表达式的实现有诸多bug。下面列举几个已知Bug：

| 原始SQL                                                 | pt-query-digest生成的特征                          | SQL解析器生成的特征                         |
| ------------------------------------------------------- | -------------------------------------------------- | ------------------------------------------- |
| select * from email_template2 where id = 1              | select * from mail_template？ where id = ？        | select * from email_template2 where id = ？ |
| REPLACE INTO a VALUES(‘INSERT INTO foo VALUES (1),(2)’) | replace into a values(\‘insert into foo values(?+) | replace into a values （?）                 |

因此可以看出SQL解析的优势是很明显的

