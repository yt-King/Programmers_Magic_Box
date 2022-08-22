# MySQL：为什么查询列表中多了它，GROUP BY语句就会报错呢？

标签： MySQL是怎样运行的

------

## 事前准备

为了故事的顺利发展，我们先得建一个表：

```sql
CREATE TABLE student_score (
  number INT(11) NOT NULL,
  name VARCHAR(30) NOT NULL,
  subject VARCHAR(30) NOT NULL,
  score TINYINT(4) DEFAULT NULL,
  PRIMARY KEY (number,subject)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

这个`student_score`表是用来存储学生成绩的，我们为这个条填充一些数据，填充后的效果就像这样：

```sql
mysql> SELECT * FROM student_score;
+----------+-----------+-----------------------------+-------+
| number   | name      | subject                     | score |
+----------+-----------+-----------------------------+-------+
| 20180101 | 杜子腾    | 母猪的产后护理              |    78 |
| 20180101 | 杜子腾    | 论萨达姆的战争准备          |    88 |
| 20180102 | 杜琦燕    | 母猪的产后护理              |   100 |
| 20180102 | 杜琦燕    | 论萨达姆的战争准备          |    98 |
| 20180103 | 范统      | 母猪的产后护理              |    59 |
| 20180103 | 范统      | 论萨达姆的战争准备          |    61 |
| 20180104 | 史珍香    | 母猪的产后护理              |    55 |
| 20180104 | 史珍香    | 论萨达姆的战争准备          |    46 |
+----------+-----------+-----------------------------+-------+
8 rows in set (0.00 sec)
```

## GROUP BY是在干什么？

我们知道`MySQL`提供了一系列的`聚集函数`，诸如：

- `COUNT`：统计记录数。
- `MAX`：查询某列的最大值。
- `MIN`：查询某列的最小值。
- `SUM`：某列数据的累加总和。
- `AVG`：某列数据的平均数。

比方说我们想查看一下`student_score`表中所有人成绩的平均数就可以这么写：

```sql
mysql> SELECT AVG(score) FROM student_score;
+------------+
| AVG(score) |
+------------+
|    73.1250 |
+------------+
1 row in set (0.00 sec)
```

如果我们只想查看`《母猪的产后护理》`这个科目的平均成绩，那加个`WHERE`子句就好了：

```sql
mysql> SELECT AVG(score) FROM student_score WHERE subject = '母猪的产后护理';
+------------+
| AVG(score) |
+------------+
|    73.0000 |
+------------+
1 row in set (0.00 sec)
```

同理，我们也可以单独查看`《论萨达姆的战争准备》`这门课程的平均成绩：

```sql
mysql> SELECT AVG(score) FROM student_score WHERE subject = '论萨达姆的战争准备';
+------------+
| AVG(score) |
+------------+
|    73.2500 |
+------------+
1 row in set (0.00 sec)
```

这时候问题来了，如果这个`student_score`表中存储了20门科目的成绩信息，那我们怎么单独的得到这20门课程的平均成绩呢？单独写20个查询语句？那要是有100门课呢？

很显然，不能傻兮兮的写一百个语句，设计`MySQL`的大叔给我们提供了`分组`的概念。我们可以按照某个列将表中的数据进行分组，比方说我们现在按照`subject`列对表中数据进行分组，那么所有的记录就会被分成2组，如图所示：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208222053198.webp)

让`MySQL`产生这样子的分组的语句就是`GROUP BY`子句，我们只要在`GROUP BY`后边把需要分组的列写上就好，然后在查询列表处就可以针对每一个分组来写相应的聚集函数去统计该分组，就像这样：

```sql
mysql> SELECT subject, AVG(score) FROM student_score GROUP BY subject;
+-----------------------------+------------+
| subject                     | AVG(score) |
+-----------------------------+------------+
| 母猪的产后护理              |    73.0000 |
| 论萨达姆的战争准备          |    73.2500 |
+-----------------------------+------------+
2 rows in set (0.00 sec)
```

## 报错

可以从上边带有`GROUP BY`子句的查询语句中看出来，我们只在查询列表处放了分组列subject以及对该分组中的记录调用的聚集函数AVG，那如果我们把不是分组列的字段也放到查询列表中会出现啥情况：

```vbnet
mysql> SELECT subject, name, AVG(score) FROM student_score GROUP BY subject;

ERROR 1055 (42000): Expression #2 of SELECT list is not in GROUP BY clause and contains nonaggregated column 'dahaizi.student_score.name' which is not functionally dependent on columns in GROUP BY clause; this is incompatible with sql_mode=only_full_group_by

mysql>
```

可以看到报错了，为啥会报错呢？回想一下我们使用`GROUP BY`子句的初衷，我们只是想把记录分为若干组，然后再对各个组分别调用聚集函数去做一些统计工作。本例中的查询列表处放置了既非分组列、又非聚集函数的`name`列，那我们想表达啥意思呢？从各个分组中的记录中取一个记录的`name`列？该取哪条记录为好呢？比方说对于`'母猪的产后护理'`这个分组中的记录来说，`name`列的值应该取`杜子腾`，还是`杜琦燕`，还是`范统`，还是`史珍香`呢？这个我们也不知道，所以把非分组列放到查询列表中会引起争议，导致结果不确定，所以设计`MySQL`的大叔才会为上述语句报错。

不过有的同学会说，假如分组后的某个分组的某个非分组列的值都一样，那我把该非分组列加入到查询列表中也没啥问题呀。比方说按照`subject`列进行分组后，假如在`'母猪的产后护理'`的分组中各条记录的`name`列的值都相同，在`'论萨达姆的战争准备'`的分组中各条记录的`name`列的值也都相同，那么我们把`name`列放在查询列表中也没啥问题。可能设计`MySQL`的大叔觉得这种说法也有点儿道理，他们竟然同意在一些情况下把非分组列也放到查询列表中，这就设计到一个称之为`sql_mode`的系统变量，我们先看一下在我的电脑上这个系统变量的值：

```sql
mysql> SHOW VARIABLES LIKE 'sql_mode';
+---------------+-------------------------------------------------------------------------------------------------------------------------------------------+
| Variable_name | Value                                                                                                                                     |
+---------------+-------------------------------------------------------------------------------------------------------------------------------------------+
| sql_mode      | ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION |
+---------------+-------------------------------------------------------------------------------------------------------------------------------------------+
1 row in set (0.02 sec)
```

哇唔，好长的一段。不过大家不必在意，我们只关心其中一个称之为`ONLY_FULL_GROUP_BY`的家伙。只要`sql_mode`的值里边有这个东东，`MySQL`服务器就“比较正常”（也就是不允许非分组列放到查询列表中），但是如果我们把这个东东从`sql_mode`系统变量中移除（移除这个东东只要重新设置一下这个系统变量，把这个东东从值里边去除掉就好，我们现在不必要关心值里边儿后边那一坨东西是干嘛的，照着抄下来就好）：

```ini
mysql> set sql_mode='STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';

Query OK, 0 rows affected (0.00 sec)
```

然后再执行上边那个曾经报错的语句：

```sql
mysql> SELECT subject, name, AVG(score) FROM student_score GROUP BY subject;
+-----------------------------+-----------+------------+
| subject                     | name      | AVG(score) |
+-----------------------------+-----------+------------+
| 母猪的产后护理              | 杜子腾    |    73.0000 |
| 论萨达姆的战争准备          | 杜子腾    |    73.2500 |
+-----------------------------+-----------+------------+
2 rows in set (0.00 sec)
```

看，这回就不会报错了。但这是个好事儿么？个人觉得不是，因为`MySQL`服务器也不能保证结果集中的`name`列的值到底是分组中的哪条记录的。大家在日常工作中，也希望尽量不要用这个投机取巧的功能，没啥乱用，而且容易产生错误。

> 小贴士：
>
> 不同MySQL版本中sql_mode的值可能默认包含ONLY_FULL_GROUP_BY这个家伙，也可能不包含ONLY_FULL_GROUP_BY这个家伙，也就是说不同MySQL版本中可能默认不支持查询列表中包含非分组列，也可能默认支持查询列表中包含非分组列。