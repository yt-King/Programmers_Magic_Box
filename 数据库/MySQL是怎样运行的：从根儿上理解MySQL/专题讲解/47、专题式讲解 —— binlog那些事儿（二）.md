# binlog那些事儿（二）

标签： MySQL是怎样运行的

------

读完上一篇文章，我们应该知道：

- binlog日志用于主从复制以及数据恢复。
- 启动选项`--log-bin[=basename]`可以控制MySQL服务器是否生成binlog，并且控制binlog日志文件所在路径以及文件名称。
- 为了记录binlog，MySQL服务器在文件系统上创建了一系列存储真实binlog数据的文件（这些文件都以数字编号），以及binlog索引文件。
- binlog日志文件中记载了数据库发生更改的若干事件。
- 使用SHOW BINLOG EVENTS语句可以查看某个binlog日志文件中存储的各种事件。
- mysqlbinlog实用工具可以用文本形式查看某个binlog日志文件所记载各种事件。

掌握了上述内容之后，我们可以继续展开了。

## binlog日志版本

binlog是自MySQL 3.23.14版本开始诞生的，到现在为止，共经历了4个版本：

- v1
- v2
- v3
- v4

其中的v4版本从MySQL 5.0就开始使用，直到今天。

所以本文着重介绍v4版本的binlog格式，其他版本就不关注了。

## binlog日志文件结构概览

废话少说，先看一下一个binlog日志文件的基本格式：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208232008060.webp)

从上图中可以看出：

- 每个binlog日志文件的前4个字节是固定的，即：`0xfe626963`。

> 小贴士：
>
> 0xfe626963中的0x626963的ascii码是'bin'，0xfe626963也被称作魔数（magic number），如果一个文件不以0xfe626963开头，那这个文件肯定不算是一个binlog日志。很多软件都会在磁盘文件的某个地方添加一个类似的魔数来表明该文件是本软件处理的文件格式，比方说Intel处理器的BIOS会将磁盘上的第一个扇区加载到内存中，这个扇区的最后两个字节必须为魔数0x55aa，Java的class文件字节码的开头四个字节为魔数0xCAFEBABE。

- 每个binlog日志文件都是由若干事件构成的。
- 每个binlog日志文件所存储的第1个事件都是一个称作`格式描述事件`（format description event）的特殊事件，我们稍后详细唠叨一下这个特殊事件。

其中，每个事件都可以被分成`event header`和`event data`两个部分，我们以上图的`事件2`为例展示一下：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208232008064.webp)

其中：

- event header部分描述了该事件是什么类型、什么时候生成的、由哪个服务器生成的等信息。
- event data部分描述了该事件所特有的一些信息，比方说在插入一条记录时，需要将这条记录的内容记录在event data中。

### event header结构

每个事件都会包括一个通用的event header，我们看一下这个event header的结构：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208232008057.webp)

event header中包含了如下几部分内容：

- timestamp（4字节）：产生该事件时的时间戳。
- typecode（1字节）：该事件的类型，事件的类型在枚举结构`Log_event_type`中列举出来（上一篇文章或者本文后续部分都有提到这个结构）。比方说`格式描述事件`的typecode就是15。
- server_id（4字节）：产生该事件的主机的server_id。
- event_length（4字节）：该事件总大小（包括event header + event data）。
- next_position（4字节）：下一个事件的位置。
- flags（2字节）：该事件的一些附加属性（称作flags）。
- extra_headers（不确定大小）：目前这个字段尚未使用（也就是占用的大小为0），可能在将来的版本中使用，大家目前忽略这个字段就好了。

### event data

event data由2部分组成，分别是：

- 固定大小部分
- 可变大小部分

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208232008059.webp)

不过并不是所有事件都有这两个部分，**有的事件可以仅有其中的一个部分或者两个部分都没有**。

上一篇文章中唠叨过，MySQL中支持几十种binlog事件，不同事件具有不同的event data部分。

我们先看一下binlog的事件类型有多少（上一篇文章中引用MySQL internal文档中的内容，有点陈旧，所以这次直接从MySQL5.7.22的源码中获取Log_event_type结构）：

```ini
enum Log_event_type
{
  /**
    Every time you update this enum (when you add a type), you have to
    fix Format_description_event::Format_description_event().
  */
  UNKNOWN_EVENT= 0,
  START_EVENT_V3= 1,
  QUERY_EVENT= 2,
  STOP_EVENT= 3,
  ROTATE_EVENT= 4,
  INTVAR_EVENT= 5,
  LOAD_EVENT= 6,
  SLAVE_EVENT= 7,
  CREATE_FILE_EVENT= 8,
  APPEND_BLOCK_EVENT= 9,
  EXEC_LOAD_EVENT= 10,
  DELETE_FILE_EVENT= 11,
  /**
    NEW_LOAD_EVENT is like LOAD_EVENT except that it has a longer
    sql_ex, allowing multibyte TERMINATED BY etc; both types share the
    same class (Load_event)
  */
  NEW_LOAD_EVENT= 12,
  RAND_EVENT= 13,
  USER_VAR_EVENT= 14,
  FORMAT_DESCRIPTION_EVENT= 15,
  XID_EVENT= 16,
  BEGIN_LOAD_QUERY_EVENT= 17,
  EXECUTE_LOAD_QUERY_EVENT= 18,

  TABLE_MAP_EVENT = 19,

  /**
    The PRE_GA event numbers were used for 5.1.0 to 5.1.15 and are
    therefore obsolete.
   */
  PRE_GA_WRITE_ROWS_EVENT = 20,
  PRE_GA_UPDATE_ROWS_EVENT = 21,
  PRE_GA_DELETE_ROWS_EVENT = 22,

  /**
    The V1 event numbers are used from 5.1.16 until mysql-trunk-xx
  */
  WRITE_ROWS_EVENT_V1 = 23,
  UPDATE_ROWS_EVENT_V1 = 24,
  DELETE_ROWS_EVENT_V1 = 25,

  /**
    Something out of the ordinary happened on the master
   */
  INCIDENT_EVENT= 26,

  /**
    Heartbeat event to be send by master at its idle time
    to ensure master's online status to slave
  */
  HEARTBEAT_LOG_EVENT= 27,

  /**
    In some situations, it is necessary to send over ignorable
    data to the slave: data that a slave can handle in case there
    is code for handling it, but which can be ignored if it is not
    recognized.
  */
  IGNORABLE_LOG_EVENT= 28,
  ROWS_QUERY_LOG_EVENT= 29,

  /** Version 2 of the Row events */
  WRITE_ROWS_EVENT = 30,
  UPDATE_ROWS_EVENT = 31,
  DELETE_ROWS_EVENT = 32,

  GTID_LOG_EVENT= 33,
  ANONYMOUS_GTID_LOG_EVENT= 34,

  PREVIOUS_GTIDS_LOG_EVENT= 35,

  TRANSACTION_CONTEXT_EVENT= 36,

  VIEW_CHANGE_EVENT= 37,

  /* Prepared XA transaction terminal event similar to Xid */
  XA_PREPARE_LOG_EVENT= 38,
  /**
    Add new events here - right above this comment!
    Existing events (except ENUM_END_EVENT) should never change their numbers
  */
  ENUM_END_EVENT /* end marker */
};
```

可见在MySQL 5.7.22这个版本中，共支持38种不同的binlog事件类型。把每一种事件格式都唠叨清楚要花费很多篇幅，并且没有多大的必要，我们下边只举一个具体的例子进行描述。

### 举一个具体的例子——格式描述事件

每个binlog日志文件都以`格式描述事件`作为第一个事件，它对应的Log_event_type就是FORMAT_DESCRIPTION_EVENT。我们看一下这种事件的结构：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208232008066.webp)

从图中我们可以知道，`格式描述事件`共占用119字节，是由event header和event data两部分构成的，其中event header是各个事件都有的部分，我们上边详细唠叨过event header中各个字段的含义，这里就不赘述了。另外，在event data部分，`格式描述事件`的event data中只有`固定长度部分`，没有`可变长度部分`，其中的各个字段含义如下：

- `binlog_version`：使用的binlog版本。
- `server_version`：产生此事件的MySQL服务器的版本。
- `create_timestamp`：产生此事件时的时间戳，该字段的值和event header中timestamp中的值一样。
- `header_length`：此事件的event header占用的存储空间大小。
- `post-header length`：使用1个字节来表示每个事件的event data部分占用的存储空间大小（不包括校验和相关字段），当前我使用的MySQL版本为5.7.22，共包含38种不同的事件，`post-header length`字段就占用了38个字节。
- `checksum_alg`：表示计算事件校验和的算法（该字段为1时表示采用CRC32算法）。
- `checksum`：表示本事件的校验和。

唠叨了很多，大家真正打开一个binlog日志文件来看一下：

```yaml
魔数：      FE62696E 

timestamp:     8AB5A861  
typecode:     0F          
server_id:     03000000 
event_length:     77000000 
next_postion:     7B000000 
flags:      0000     
binlog_version:    0400   
server_version:    352E37 2E32312D 6C6F6700 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000000 000000  
create_timestamp:    8AB5A861 
header_length:     13       
post-header length(共38种): 380D0008 00120004 04040412 00005F00 041A0800 00000808 08020000 000A0A0A 2A2A0012 3400
checksum_alg：    01 
checksum：      5B7108A3
```

> 小贴士：
>
> 其他事件的event data部分大家可以参考一下MySQL internal文档。另外，也可以使用mysqlbinlog，配合--hexdump启动选项来直接分析binlog的二进制格式。

## 基于语句（Statement）和基于行(Row)的binlog

同一条SQL语句，随着启动选项`binlog-format`的不同，可能生成不同类型的binlog事件：

- 当以启动选项`--binlog-format=STATEMENT`启动MySQL服务器时，生成的binlog称作`基于语句的日志`。此时只会将一条SQL语句将会被完整的记录到binlog中，而不管该语句影响了多少记录。
- 当以启动选项`--binlog-format=ROW`启动MySQL服务器时，生成的binlog称作`基于行的日志`。此时会将该语句所改动的记录的全部信息都记录上。
- 当以启动选项`--binlog-format=MIXED`启动MySQL服务器时，生成的binlog称作`基于行的日志`。此时在通常情况下采用`基于语句的日志`，在某些特殊情况下会自动转为`基于行的日志`（这些具体情况请参考：[dev.mysql.com/doc/refman/…](https://link.juejin.cn/?target=https%3A%2F%2Fdev.mysql.com%2Fdoc%2Frefman%2F8.0%2Fen%2Fbinary-log-mixed.html%EF%BC%89%E3%80%82)

> 小贴士：
>
> 我们也可以通过修改会话级别的binlog_format系统变量的形式来修改只针对本客户端执行语句生成的binlog日志的格式。

### 基于语句的binlog

假如服务器启动时添加了`--binlog-format=STATEMENT`启动选项，我们执行如下语句：

```ini
UPDATE s1 SET common_field = 'xx' WHERE id > 9990;
```

然后使用mysqlbinlog实用工具查看一下相应的binlog内容：

```ini
mysqlbinlog --verbose xiaohaizi-bin.000007
...这里省略了很多内容
# at 308
#211207 21:00:27 server id 3  end_log_pos 440 CRC32 0x713f80ae  Query thread_id=2 exec_time=0 error_code=0
use `xiaohaizi`/*!*/;
SET TIMESTAMP=1638882027/*!*/;
update s1 set common_field= 'xx' where id > 9990
/*!*/;
...这里省略了很多内容
```

可见，`基于语句的binlog`只将更新语句是什么记录下来了。

### 基于行的binlog

假如服务器启动时添加了`--binlog-format=ROW`启动选项，我们执行如下语句：

```ini
UPDATE s1 SET common_field = 'xxx' WHERE id > 9990;
```

然后使用mysqlbinlog实用工具查看一下相应的binlog内容：

```shell
mysqlbinlog --verbose xiaohaizi-bin.000008
...这里省略了很多内容
### UPDATE `xiaohaizi`.`s1`
### WHERE
###   @1=9991
###   @2='7cgwfh14w6nql61pvult6ok0ccwe'
###   @3='799105223'
###   @4='c'
###   @5='gjjiwstjysv1lgx'
###   @6='zg1hsvqrtyw2pgxgg'
###   @7='y244x02'
###   @8='xx'
### SET
###   @1=9991
###   @2='7cgwfh14w6nql61pvult6ok0ccwe'
###   @3='799105223'
###   @4='c'
###   @5='gjjiwstjysv1lgx'
###   @6='zg1hsvqrtyw2pgxgg'
###   @7='y244x02'
###   @8='xxx'
### UPDATE `xiaohaizi`.`s1`
### WHERE
###   @1=9992
###   @2='2sfq3oftc'
###   @3='815047282'
###   @4='ub'
###   @5='73hw14kbaaoa'
###   @6='fxnqzef3rrpc7qzxcjsvt14nypep4rqi'
###   @7='10vapb6'
###   @8='xx'
### SET
###   @1=9992
###   @2='2sfq3oftc'
###   @3='815047282'
###   @4='ub'
###   @5='73hw14kbaaoa'
###   @6='fxnqzef3rrpc7qzxcjsvt14nypep4rqi'
###   @7='10vapb6'
###   @8='xxx'
...这里省略了很多内容
```

可见，`基于行的binlog`将更新语句执行过程中每一条记录更新前后的值都记录下来了。

## 基于语句的binlog的问题

在有主从复制的场景中，使用`基于语句的日志`可能会造成主服务器和从服务器维护的数据不一致的情况。

比方说我们有一个表t：

```sql
CREATE TABLE t (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    c VARCHAR(100),
    PRIMARY KEY(ID)
);
```

如果我们执行如下语句：

```sql
INSERT INTO t(c) SELECT c FROM other_table;
```

这个语句是想将`other_table`表中列`c`的值都插入到表t的列c中，而表t的id列是自增列，可以自动生成。

如果主库和从库的服务器执行`SELECT c FROM other_table`返回记录的顺序不同的话（不同服务器版本、不同的系统变量配置都可能导致同一条语句返回结果的顺序不同），那么针对表t相同id值的记录来说，列c就可能具有不同的值，这就会造成主从之间数据的不一致。

而如果将binlog的格式改为`基于行的日志`的话，由于主库在执行完语句后将该语句插入的每条完整的记录都写入binlog日志，就不会造成主从之间不一致了。

好像又写了很多枯燥的内容... 下次不写这么枯燥的了，我看着都有点儿烦~