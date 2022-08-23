# binlog那些事儿（一）

标签： MySQL是怎样运行的

------

上一篇文章中有同学留言说想让小孩子写写MySQL的两阶段提交。

动手写的时候想到：是不是很多同学连XA事务是什么也不清楚，甚至很多同学连binlog是什么也不清楚～

好吧，这篇文章先来唠叨一下MySQL中的binlog到底是什么。

## binlog的作用

`binlog`是`binary log`的缩写，即`二进制日志`。`binlog`中记载了数据库发生的变化，比方说新建了一个数据库或者表、表结构发生改变、表中的数据发生了变化时都会记录相应的binlog日志。

`binlog`主要用在下边两个方面：

- 用途一： 用于复制。

现在人们张口闭口就是**亿级并发**，虽然是夸张，但**单台物理机器所能同时处理的请求是有限的**却是一个事实。为了提高并发处理请求的能力，一般将MySQL服务部署在多台物理机器中，这些服务器中维护相同的数据副本。

其中一个典型的部署方案就是`一主多从`，即一台主服务器（Master）和多台从服务器（Slave）。对于改变数据库状态的请求（DDL、DML等），就将它们发送给主服务器，对于单纯的查询（如SELECT语句）请求，就将它们发送给从服务器。为了让各个从服务器中存储的数据和主服务器中存储的数据一致，每当我们改变了主服务器中的数据后，就需要将改变的信息**同步**给各个从服务器。`binlog`日志中正好记录了数据库发生的各种改变的信息，从服务器读取主服务器产生的`binlog`日志，然后执行这些`binlog`日志中所记录的数据库变化语句，从而达到主从服务器数据一致的效果。

- 用途二： 用于恢复。

  工作中我们可能有意无意的就将数据库里的数据给“毁”了，比方说写DELETE语句不加WHERE子句，那一整个表的数据都就没了！为了数据的安全性，我们需要定时备份数据库（mysqldump命令），不过这种全量备份我们不可能每秒都做一遍，而是每天或者每个月做一次全量备份。那如果在两次全量备份中间手贱写了不加WHERE条件的DELETE语句该怎么办呢？只能将数据库恢复到前一次全量备份时的样子吗？还好我们有`binlog`日志，我们可以从上一次全量备份开始，执行自改次备份后产生的`binlog`日志，直到我们写DELETE语句之前的binlog日志为止。这样就可以完成数据库恢复的功能。

## 怎么配置binlog

MySQL服务器并不一定会生成`binlog`日志，我们可以通过查看`log_bin`系统变量来判断当前MySQL服务器是否生成`binlog`日志：

```sql
mysql> show variables like 'log_bin';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| log_bin       | ON    |
+---------------+-------+
1 row in set, 1 warning (0.02 sec)
```

上例中`bin_log`系统变量的值为`ON`，表明当前服务器生成`binlog`，若为`OFF`表明当前服务器不生成`binlog`。

如果当前服务器不生成`binlog`，我们想开启`binlog`，那么就需要重启服务器，设置`log-bin`启动选项：

```css
--log-bin[=base_name] 
```

`binlog`日志并不是仅写到一个文件中，而是写入一组文件中，这组文件的命名是这样的：

```erlang
basename.000001
basename.000002
basename.000003
basename.000004
...
```

也就是这组日志文件名称都包含一个basename，然后以一个数字结尾。

启动选项`log-bin[=base_name]`中的`base_name`就是这组binlog日志文件名称都包含的部分。如果我们不指定`base_name`（即单纯的使用--log-bin），那MySQL服务器会默认将`主机名-bin`作为binlog日志文件的basename。

我们看一下例子。

如果启动服务器的命令是：

```c
mysqld --log-bin
```

表示开启binlog，并将binlog写入**MySQL服务器的数据目录**下。我的主机名是`xiaohaizi`，那MySQL服务器程序生成的binlog日志文件名就像是这样：

```python
xiaohaizi-bin.000001
xiaohaizi-bin.000002
xiaohaizi-bin.000003
xiaohaizi-bin.000004
...
```

如果启动命令是：

```ini
mysqld --log-bin=xx
```

表示开启binlog，并将binlog写入**MySQL服务器的数据目录**下，binlog日志文件名就像是这样：

```erlang
xx.000001
xx.000002
xx.000003
xx.000004
...
```

我们可以在将启动选项`log-bin[=base_name]`的`base_name`指定为一个绝对路径，那么binlog日志就不会被放到默认的数据目录中，而是写到我们指定的绝对路径下了。比方说启动命令是：

```ini
mysqld --log-bin=/Users/xiaohaizi/xx
```

这样binlog日志就会被写入**/Users/xiaohaizi/**路径下，binlog日志文件名就像是这样：

```erlang
xx.000001
xx.000002
xx.000003
xx.000004
...
```

> 小贴士：
>
> log-bin启动选项也可以放在配置文件中，我们这里就不赘述了。

## binlog在文件系统中的内容

我们刚强调，binlog日志不是单个文件，而是一组包含共同basename的文件。比方说现在我的机器上有以下4个binlog文件：

```python
xiaohaizi-bin.000001
xiaohaizi-bin.000002
xiaohaizi-bin.000003
xiaohaizi-bin.000004
```

这些binlog文件并不能直接被当作文本打开，毕竟人家的名字是binlog，存储的是二进制数据。

除了真正存储binlog日志的文件外，MySQL服务器还会在相同的路径下生成一个关于binlog的索引文件，在我的系统上它的名称就是：

```python
xiaohaizi-bin.index
```

这个索引文件是一个文本文件，我们可以直接打开：

```bash
shell> cat xiaohaizi-bin.index
./xiaohaizi-bin.000001
./xiaohaizi-bin.000002
./xiaohaizi-bin.000003
./xiaohaizi-bin.000004
```

可以看到，这个索引文件只是简单的将各个binlog文件的路径存储了起来而已。

## 查看binlog的语句

binlog中记录数据库发生更改的各种事件（events），这些事件的种类非常多，完整的事件类型如下所示：

```ini
enum Log_event_type { 
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
  NEW_LOAD_EVENT= 12, 
  RAND_EVENT= 13, 
  USER_VAR_EVENT= 14, 
  FORMAT_DESCRIPTION_EVENT= 15, 
  XID_EVENT= 16, 
  BEGIN_LOAD_QUERY_EVENT= 17, 
  EXECUTE_LOAD_QUERY_EVENT= 18, 
  TABLE_MAP_EVENT = 19, 
  PRE_GA_WRITE_ROWS_EVENT = 20, 
  PRE_GA_UPDATE_ROWS_EVENT = 21, 
  PRE_GA_DELETE_ROWS_EVENT = 22, 
  WRITE_ROWS_EVENT = 23, 
  UPDATE_ROWS_EVENT = 24, 
  DELETE_ROWS_EVENT = 25, 
  INCIDENT_EVENT= 26, 
  HEARTBEAT_LOG_EVENT= 27, 
  IGNORABLE_LOG_EVENT= 28,
  ROWS_QUERY_LOG_EVENT= 29,
  WRITE_ROWS_EVENT = 30,
  UPDATE_ROWS_EVENT = 31,
  DELETE_ROWS_EVENT = 32,
  GTID_LOG_EVENT= 33,
  ANONYMOUS_GTID_LOG_EVENT= 34,
  PREVIOUS_GTIDS_LOG_EVENT= 35, 
  ENUM_END_EVENT 
  /* end marker */ 
};
```

其中的一些我们熟悉的事件：

- WRITE_ROWS_EVENT：插入记录。
- UPDATE_ROWS_EVENT：更新记录。
- DELETE_ROWS_EVENT：删除记录。

像创建、修改数据库或者表结构这些语句也都可以找到对应类型的`事件`，我们这里就不一一展开了。

为了查看我们的binlog中包含了哪些`事件`，可以使用下边这个语句（带中括号`[]`的表示可以省略的语句）：

```css
SHOW BINLOG EVENTS
   [IN 'log_name']
   [FROM pos]
   [LIMIT [offset,] row_count]
```

其中：

- `[IN 'log_name']`：`log_name`表示我们要查看哪个`binlog`日志文件的内容。
- `[FROM pos]`：`pos`表示我们要查看binlog文件的起始偏移量（通过指定这个值可以直接去查看某个偏移量处的事件）。
- `LIMIT [offset,] row_count`：这个LIMIT子句的含义和我们写SQL语句中LIMIT子句的含义是一样的，`offset`表示我们要从哪个事件开始查看，`row_count`表示我们要查看多少个事件。

下边该做一下测试了。

如果我们直接执行`SHOW BINLOG EVENTS`，表示查看第1个binlog日志文件的内容，在我的机器上就是`xiaohaizi-bin.000001`这个文件的内容：

```sql
mysql> SHOW BINLOG EVENTS;
+----------------------+-----+----------------+-----------+-------------+---------------------------------------+
| Log_name             | Pos | Event_type     | Server_id | End_log_pos | Info                                  |
+----------------------+-----+----------------+-----------+-------------+---------------------------------------+
| xiaohaizi-bin.000001 |   4 | Format_desc    |         3 |         123 | Server ver: 5.7.21-log, Binlog ver: 4 |
| xiaohaizi-bin.000001 | 123 | Previous_gtids |         3 |         154 |                                       |
| xiaohaizi-bin.000001 | 154 | Anonymous_Gtid |         3 |         219 | SET @@SESSION.GTID_NEXT= 'ANONYMOUS'  |
| xiaohaizi-bin.000001 | 219 | Query          |         3 |         296 | BEGIN                                 |
| xiaohaizi-bin.000001 | 296 | Table_map      |         3 |         367 | table_id: 138 (xiaohaizi.s1)          |
| xiaohaizi-bin.000001 | 367 | Update_rows    |         3 |         634 | table_id: 138 flags: STMT_END_F       |
| xiaohaizi-bin.000001 | 634 | Xid            |         3 |         665 | COMMIT /* xid=65 */                   |
| xiaohaizi-bin.000001 | 665 | Stop           |         3 |         688 |                                       |
+----------------------+-----+----------------+-----------+-------------+---------------------------------------+
8 rows in set (0.01 sec)
```

可以看到共输出了8个列，其中：

- `Log_name`：表示binlog日志的文件名。
- `Pos`：表示该事件在binlog日志文件中的起始偏移量。
- `Event_type`：表示这个事件的类型。
- `Server_id`：表示产生该事件的server_id（server_id是一个系统变量，我们可以通过配置让不通的MySQL服务器拥有不通的server_id）。
- `End_log_pos`：表示下一个事件的在binlog日志文件中的起始偏移量。
- `Info`：关于本事件的一些说明。

如果您对MySQL了解不多的话，那上边执行`SHOW BINLOG EVENTS`语句的输出的大部分事件可能都比较懵，可能能看懂两个事件：

- 起始偏移量为219（Pos=219）的事件是一个表明开始事务（BEGIN）的事件。
- 起始偏移量为367（Pos=367）的事件是一个更新记录的事件（UPDATE）。

好了现在先不细究各种类型的事件都代表什么，我们目前只需要知道binlog日志是由若干个事件组成的就好了。

如果我们想看其他binlog日志的详细情况，那就需要用到IN子句了：

```sql
mysql> SHOW BINLOG EVENTS IN 'xiaohaizi-bin.000004';
+----------------------+-----+----------------+-----------+-------------+-------------------------------------------+
| Log_name             | Pos | Event_type     | Server_id | End_log_pos | Info                                      |
+----------------------+-----+----------------+-----------+-------------+-------------------------------------------+
| xiaohaizi-bin.000004 |   4 | Format_desc    |         3 |         123 | Server ver: 5.7.21-log, Binlog ver: 4     |
| xiaohaizi-bin.000004 | 123 | Previous_gtids |         3 |         154 |                                           |
| xiaohaizi-bin.000004 | 154 | Anonymous_Gtid |         3 |         219 | SET @@SESSION.GTID_NEXT= 'ANONYMOUS'      |
| xiaohaizi-bin.000004 | 219 | Query          |         3 |         327 | use `xiaohaizi`; create table tt1 (c int) |
| xiaohaizi-bin.000004 | 327 | Anonymous_Gtid |         3 |         392 | SET @@SESSION.GTID_NEXT= 'ANONYMOUS'      |
| xiaohaizi-bin.000004 | 392 | Query          |         3 |         469 | BEGIN                                     |
| xiaohaizi-bin.000004 | 469 | Table_map      |         3 |         520 | table_id: 167 (xiaohaizi.tt1)             |
| xiaohaizi-bin.000004 | 520 | Write_rows     |         3 |         560 | table_id: 167 flags: STMT_END_F           |
| xiaohaizi-bin.000004 | 560 | Xid            |         3 |         591 | COMMIT /* xid=71 */                       |
| xiaohaizi-bin.000004 | 591 | Anonymous_Gtid |         3 |         656 | SET @@SESSION.GTID_NEXT= 'ANONYMOUS'      |
| xiaohaizi-bin.000004 | 656 | Query          |         3 |         733 | BEGIN                                     |
| xiaohaizi-bin.000004 | 733 | Table_map      |         3 |         784 | table_id: 167 (xiaohaizi.tt1)             |
| xiaohaizi-bin.000004 | 784 | Update_rows    |         3 |         830 | table_id: 167 flags: STMT_END_F           |
| xiaohaizi-bin.000004 | 830 | Xid            |         3 |         861 | COMMIT /* xid=73 */                       |
+----------------------+-----+----------------+-----------+-------------+-------------------------------------------+
14 rows in set (0.00 sec)
```

这样我们就看到了`xiaohaizi-bin.000004`这个binlog日志文件中存储了哪些事件了。

大家可以自行测试一下FROM子句和LIMIT子句的用法，这里就不展示了。

## mysqlbinlog工具的使用

由于binlog是二进制格式的，我们不能直接以文本的形式查看。使用`SHOW BINLOG EVENTS`又只能看到粗略的信息，如果我们想查看binlog日志文件的详细信息的话，就需要使用MySQL给我们提供的实用工具——mysqlbinlog。

像`mysqld`、`mysql`这些可执行文件一样，`mysqlbinlog`也被放在了MySQL安装目录下的bin目录下。

我们可以将想查看的binlog日志文件路径作为`mysqlbinlog`的参数，就能查看文本形式的事件详细信息了。比方说我们看一下`xiaohaizi-bin.000001`：

```ini
shell> mysqlbinlog ./xiaohaizi-bin.000001
/*!50530 SET @@SESSION.PSEUDO_SLAVE_MODE=1*/;
/*!50003 SET @OLD_COMPLETION_TYPE=@@COMPLETION_TYPE,COMPLETION_TYPE=0*/;
DELIMITER /*!*/;
# at 4
#211202 20:01:14 server id 3  end_log_pos 123 CRC32 0xa308715b 	Start: binlog v 4, server v 5.7.21-log created 211202 20:01:14 at startup
ROLLBACK/*!*/;
BINLOG '
irWoYQ8DAAAAdwAAAHsAAAAAAAQANS43LjIxLWxvZwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
AAAAAAAAAAAAAAAAAACKtahhEzgNAAgAEgAEBAQEEgAAXwAEGggAAAAICAgCAAAACgoKKioAEjQA
AVtxCKM=
'/*!*/;
# at 123
#211202 20:01:14 server id 3  end_log_pos 154 CRC32 0x0d6a1ce6 	Previous-GTIDs
# [empty]
# at 154
#211202 20:07:07 server id 3  end_log_pos 219 CRC32 0xab157b64 	Anonymous_GTID	last_committed=0	sequence_number=1	rbr_only=yes
/*!50718 SET TRANSACTION ISOLATION LEVEL READ COMMITTED*//*!*/;
SET @@SESSION.GTID_NEXT= 'ANONYMOUS'/*!*/;
# at 219
#211202 20:07:07 server id 3  end_log_pos 296 CRC32 0xedb6b609 	Query	thread_id=2	exec_time=0	error_code=0
SET TIMESTAMP=1638446827/*!*/;
SET @@session.pseudo_thread_id=2/*!*/;
SET @@session.foreign_key_checks=1, @@session.sql_auto_is_null=0, @@session.unique_checks=1, @@session.autocommit=1/*!*/;
SET @@session.sql_mode=1436549152/*!*/;
SET @@session.auto_increment_increment=1, @@session.auto_increment_offset=1/*!*/;
/*!\C utf8 *//*!*/;
SET @@session.character_set_client=33,@@session.collation_connection=33,@@session.collation_server=33/*!*/;
SET @@session.lc_time_names=0/*!*/;
SET @@session.collation_database=DEFAULT/*!*/;
BEGIN
/*!*/;
# at 296
#211202 20:07:07 server id 3  end_log_pos 367 CRC32 0x43cd57ee 	Table_map: `xiaohaizi`.`s1` mapped to number 138
# at 367
#211202 20:07:07 server id 3  end_log_pos 634 CRC32 0xe2981d9e 	Update_rows: table id 138 flags: STMT_END_F

BINLOG '
67aoYRMDAAAARwAAAG8BAAAAAIoAAAAAAAEACXhpYW9oYWl6aQACczEACAMPDw8PDw8PDiwBLAEs
ASwBLAEsASwB/u5XzUM=
67aoYR8DAAAACwEAAHoCAAAAAIoAAAAAAAEAAgAI//8ApAMAABgAZmt3YW91b2syY2sxY2RlMzA2
bzZ2NHcxCQAxMzA4NzI2NzgTAHBqdHFxc2dsMngxMjd4MWZjdngBAG0MAHBycmp3bmtxbjV1aRoA
NHN3cWJsNXEzd3V2bzUyZGdscmI1eWlmencJAGxzMjFoNHZhNwCkAwAAGABma3dhb3VvazJjazFj
ZGUzMDZvNnY0dzEJADEzMDg3MjY3OBMAcGp0cXFzZ2wyeDEyN3gxZmN2eAEAbQwAcHJyandua3Fu
NXVpGgA0c3dxYmw1cTN3dXZvNTJkZ2xyYjV5aWZ6dwIAeHieHZji
'/*!*/;
# at 634
#211202 20:07:07 server id 3  end_log_pos 665 CRC32 0xe586ffeb 	Xid = 65
COMMIT/*!*/;
# at 665
#211202 20:07:19 server id 3  end_log_pos 688 CRC32 0x8c69bad2 	Stop
SET @@SESSION.GTID_NEXT= 'AUTOMATIC' /* added by mysqlbinlog */ /*!*/;
DELIMITER ;
# End of log file
/*!50003 SET COMPLETION_TYPE=@OLD_COMPLETION_TYPE*/;
/*!50530 SET @@SESSION.PSEUDO_SLAVE_MODE=0*/;
```

哇唔，好大一片输出！

其中以`# at xx`开头的表示这是一个事件的开始，诸如：

- `# at 4`：表示在偏移量为4的地方有一个事件，下边是该事件的详细信息。
- `# at 123`表示在偏移量为123的地方有一个事件，下边是该事件的详细信息。

...

具体的格式我们就先不展开了。

## binlog的文件格式

本来我们还想唠叨一下binlog日志文件是怎么设计的，每一个事件是怎样表示的。可是回头一看好像已经写了很多内容了，小孩子太累了，之后抽时间再给大家写吧...

还有MySQL的主从复制是怎么跑起来的？XA事务是什么？为啥要两阶段提交？

再会～