# MySQL乱码

标签： MySQL是怎样运行的

------

## 字符集转换概述

我们有必要说明一下，`字符`其实是面向人类的一个概念，计算机可并不关心字符是什么，它只关心这个字符对应的字节编码是什么。对于一个字节序列，计算机怎么知道它是使用什么字符集编码的呢？计算机不知道，所以其实在计算机中表示一个字符串时，都需要附带上它对应的字符集是什么，就像这样（以C++语言为例）：

```C++
class String {
    byte* content;
    CHARSET_INFO* charset;
}
```

比方说我们现在有一个以`utf8`字符集编码的汉字`'我'`，那么意味着计算机中不仅仅要存储`'我'`的utf8编码`0xE68891`，还需要存储它是使用什么字符集编码的信息，就像这样：

```css
{
    content: 0xE68891;
    charset: utf8;
}
```

计算机内部包含将一种字符集转换成另一种字符集的函数库，也就是某个字符在某种字符集下的编码可以很顺利的转换为另一种字符集的编码，我们将这个过程称之为`字符集转换`。比方说我们可以将上述采用utf8字符集编码的字符'我'，转换成gbk字符集编码的形式，就变成了这样：

```css
{
    content: 0xCED2;
    charset: gbk;
}
```

> 小贴士：
>
> 我们上边所说的'编码'可以当作动词，也可以当作名词来理解。当作动词的话意味着将一个字符映射到一个字节序列的过程，当作名词的话意味着一个字符对应的字节序列。大家根据上下文理解'编码'的含义。

## MySQL客户端和服务器是怎么通信的

MySQL客户端发送给服务器的请求以及服务器发送给客户端的响应其实都是遵从一定格式的，我们把它们通信过程中事先规定好的数据格式称之为MySQL通信协议，这个协议是公开的，我们可以简单的使用wireshark等截包软件十分方便的分析这个通信协议。在了解了这个通信协议之后，我们甚至可以动手制作自己的客户端软件。市面上的MySQL客户端软件多种多样，我们并不想各个都分析一下，现在只选取在MySQL安装目录的`bin`目录下自带的`mysql`程序（此处的`mysql`程序指的是名字叫做`mysql`的一个可执行文件），如图所示： ![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208222054842.webp) 我们在计算机的黑框框中执行该可执行文件，就相当于启动了一个客户端，就像这样： ![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208222054854.webp)

> 小贴士：
>
> 我们这里的'黑框框'指的是Windows操作系统中的cmd.exe或者UNIX系统中的Shell。

我们通常是按照下述步骤使用MySQL的：

1. 启动客户端并连接到服务器
2. 客户端发送请求。
3. 服务器接收到请求
4. 服务器处理请求
5. 服务器处理请求完毕生成对该客户端的响应
6. 客户端接收到响应

下边我们就详细分析一下每个步骤中都影响到了哪些字符集。

## 启动客户端并连接到服务器过程

每个MySQL客户端都维护者一个客户端默认字符集，这个默认字符集按照下边的套路进行取值：

- 自动检测操作系统使用的字符集

  MySQL客户端会在启动时检测操作系统当前使用的字符集，并按照一定规则映射成为MySQL支持的一些字符集（通常是操作系统当前使用什么字符集，就映射为什么字符集，有一些特殊情况，比方说如果操作系统当前使用的是ascii字符集，会被映射为latin1字符集）。

  - 当我们使用UNIX操作系统时

    此时会调用操作系统提供的`nl_langinfo(CODESET)`函数来获取操作系统当前正在使用的字符集，而这个函数的结果是依赖`LC_ALL`、`LC_CTYPE`、`LANG`这三个环境变量的。其中`LC_ALL`的优先级比`LC_CTYPE`高，`LC_CTYPE`的优先级比`LANG`高。也就是说如果设置了`LC_ALL`，不论有没有设置`LC_CTYPE`或者`LANG`，最终都以`LC_ALL`为准；如果没有设置`LC_ALL`，那么就以`LC_CTYPE`为准；如果既没有设置`LC_ALL`也没有设置`LC_CTYPE`，就以`LANG`为准。比方说我们将环境变量`LC_ALL`设置为`zh_CN.UTF-8`，就像这样：

    ```ini
    export LC_ALL=zh_CN.UTF-8
    ```

    那么我们在黑框框里启动MySQL客户端时，MySQL客户端就会检测到这个操作系统使用的是`utf8`字符集，并将客户端默认字符集设置为`utf8`。

    当然，如果这三个环境变量都没有设置，那么`nl_langinfo(CODESET)`函数将返回操作系统默认的字符集，比方说在我的`macOS 10.15.3`操作系统中，该默认字符集为：

    ```
    US-ASCII
    ```

    此时MySQL客户端的默认字符集将会被设置为`latin1`。

    另外，我们这里还需要强调一下，我们使用的黑框框展示字符的时候有一个自己特有的字符集，比如在我的mac上使用`iTerm2`作为黑框框，我们可以打开：Preferences->Profiles->Terminal选项卡，可以看到`iTerm2`使用`utf8`来展示字符： ![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208222054849.webp) 我们一般要把黑框框展示字符时采用的编码和操作系统当前使用的编码保持一致，如果不一致的话，我们敲击的字符可能都无法显示到屏幕上。比方说如果我此时把`LC_ALL`属性设置成`GBK`，那么我们再向黑框框上输入汉字的话，屏幕都不会显示了，就像这样（如下图所示，我敲击了汉字`'我'`的效果）： ![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208222054801.webp)

  - 当我们使用Windows操作系统时

    此时会调用操作系统提供的`GetConsoleCP`函数来获取操作系统当前正在使用的字符集。在Windows里，会把当前cmd.exe使用的字符集映射到一个数字，称之为代码页（英文名：`code page`），我们可以通过右键点击`cmd.exe`标题栏，然后点击属性->选项，如下图所示，`当前代码页`的值是936，代表当前cmd.exe使用gbk字符集： ![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208222054853.webp) 更简便一点，我们可以运行`chcp`命令直接看到当前code page是什么： ![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208222054804.webp) 这样我们在黑框框里启动MySQL客户端时，MySQL客户端就会检测到这个操作系统使用的是`gbk`字符集，并将客户端默认字符集设置为`gbk`。我们前边提到的utf8字符集对应的代码页为`65001`，如果当前代码页的值为65001，之后再启动MySQL客户端，那么客户端的默认字符集就会变成`utf8`。

- 如果MySQL不支持自动检测到的操作系统当前正在使用的字符集，或者在某些情况下不允许自动检测的话，MySQL会使用它自己的内建的默认字符集作为客户端默认字符集。这个内建的默认字符集在`MySQL 5.7`以及之前的版本中是`latin1`，在`MySQL 8.0`中修改为了`utf8mb4`。

- 使用了`default-character-set`启动参数

  如果我们在启动MySQL客户端是使用了`default-character-set`启动参数，那么客户端的默认字符集将不再检测操作系统当前正在使用的字符集，而是直接使用启动参数`default-character-set`所指定的值。比方说我们使用如下命令来启动客户端：

  ```arduino
  mysql --default-character-set=utf8
  ```

  那么不论我们使用什么操作系统，操作系统目前使用的字符集是什么，我们都将会以utf8作为MySQL客户端的默认字符集。

在确认了MySQL客户端默认字符集之后，客户端就会向服务器发起登陆请求，传输一些诸如用户名、密码等信息，在这个请求里就会包含客户端使用的默认字符集是什么的信息，服务器收到后就明白了稍后客户端即将发送过来的请求是采用什么字符集编码的，自己生成的响应应该以什么字符集编码了（剧透一下：其实服务器在明白了客户端使用的默认字符集之后，就会将`character_set_client`、`character_set_connection`以及`character_set_result`这几个系统变量均设置为该值）。

### 客户端发送请求

登陆成功之后，我们就可以使用键盘在黑框框中键入我们想要输入的MySQL语句，输入完了之后就可以点击回车键将该语句当作请求发送到服务器，可是客户端发送的语句（本质是个字符串）到底是采用什么字符集编码的呢？这其实涉及到应用程序和操作系统之间的交互，我们的MySQL客户端程序其实是一个应用程序，它从黑框框中读取数据其实是要调用操作系统提供的读取接口。在不同的操作系统中，调用的读取接口其实是不同的，我们还得分情况讨论一下：

- 对于UNIX操作系统来说

  在我们使用某个输入法软件向黑框框中输入字符时，该字符采用的编码字符集其实是操作系统当前使用的字符集。比方说当前`LC_ALL`环境变量的值为`zh_CN.UTF-8`，那么意味着黑框框中的字符其实是使用utf8字符集进行编码。稍后MySQL客户端程序将调用操作系统提供的read函数从黑框框中读取数据（其实就是所谓的从标准输入流中读取数据），所读取的数据其实就是采用utf8字符集进行编码的字节序列，稍后将该字节序列作为请求内容发送到服务器。

  这样其实会产生一个问题，如果客户端的默认字符集和操作系统当前正在使用的字符集不同，那么将产生比较尴尬的结果。比方说我们在启动客户端是携带了`--default-character-set=gbk`的启动参数，那么客户端的默认字符集将会被设置成gbk，而如果操作系统此时采用的字符集是utf8。比方说我们的语句中包含汉字`'我'`，那么客户端调用`read`函数读到的字节序列其实是`0xE68891`，从而将`0xE68891`发送到服务器，而服务器认为客户端发送过来的请求都是采用gbk进行编码的，这样就会产生问题（当然，这仅仅是发生乱码问题的前奏，并不意味着产生乱码，乱码只有在最后一步，也就是客户端应用程序将服务器返回的数据写到黑框框里时才会发生）。

- 对于Windows操作系统来说

  在Windows操作系统中，从黑框框中读取数据调用的是Windows提供的`ReadConsoleW`函数。在该函数执行后，MySQL客户端会得到一个宽字符数组（其实就是一组16位的UNICODE），然后客户端需要把该宽字符数组再次转换成客户端使用的默认字符集编码的字节序列，然后才将该字节序列作为请求的内容发送到服务器。

  这样在UNIX操作系统中可能产生的问题，在Windows系统中却可以避免。比方说我们在启动客户端是携带了`--default-character-set=gbk`的启动参数，那么客户端的默认字符集将会被设置成gbk，假如此时操作系统采用的字符集是utf8。比方说我们的语句中包含汉字`'我'`，那么客户端调用`ReadConsoleW`函数先读到一个代表着`我`字的宽字符数组，之后又将其转换为客户端的默认字符集，也就是gbk字符集编码的数据`0xCED2`，然后将`0xCED2`发送到服务器。此时服务器也认为客户端发送过来的请求就是采用gbk进行编码的，这样就完全正确了～

### 服务器接收请求

服务器接收到到的请求本质上就是一个字节序列，服务器将其看作是采用系统变量`character_set_client`代表的字符集进行编码的字节序列。`character_set_client`是一个SESSION级别的系统变量，也就是说每个客户端和服务器建立连接后，服务器都会为该客户端维护一个单独的`character_set_client`变量，每个客户端在登录服务器的时候都会将客户端的默认字符集通知给服务器，然后服务器设置该客户端专属的`character_set_client`。

我们可以使用SET命令单独修改`character_set_client`对应的值，就像这样：

```ini
SET character_set_client=gbk;
```

需要注意的是，`character_set_client`对应的字符集一定要包含请求中的字符，比方说我们把`character_set_client`设置成`ascii`，而请求中发送了一个汉字`'我'`，将会发生这样的事情：

```sql
mysql> SET character_set_client=ascii;
Query OK, 0 rows affected (0.00 sec)

mysql> SHOW VARIABLES LIKE 'character%';
+--------------------------+------------------------------------------------------+
| Variable_name            | Value                                                |
+--------------------------+------------------------------------------------------+
| character_set_client     | ascii                                                |
| character_set_connection | utf8                                                 |
| character_set_database   | utf8                                                 |
| character_set_filesystem | binary                                               |
| character_set_results    | utf8                                                 |
| character_set_server     | utf8                                                 |
| character_set_system     | utf8                                                 |
| character_sets_dir       | /usr/local/Cellar/mysql/5.7.21/share/mysql/charsets/ |
+--------------------------+------------------------------------------------------+
8 rows in set (0.00 sec)

mysql> SELECT '我';
+-----+
| ??? |
+-----+
| ??? |
+-----+
1 row in set, 1 warning (0.00 sec)

mysql> SHOW WARNINGS\G
*************************** 1. row ***************************
  Level: Warning
   Code: 1300
Message: Invalid ascii character string: '\xE6\x88\x91'
1 row in set (0.00 sec)
```

如图所示，最后提示了`'E6、88、91'`并不是正确的ascii字符。

> 小贴士：
>
> 可以将character_set_client设置为latin1，看看还会不会报告WARNINGS，以及为什么～

### 服务器处理请求

服务器在处理请求时会将请求中的字符再次转换为一种特定的字符集，该字符集由系统变量`character_set_connection`表示，该系统变量也是SESSION级别的。每个客户端在登录服务器的时候都会将客户端的默认字符集通知给服务器，然后服务器设置该客户端专属的`character_set_connection`。

不过我们之后可以通过SET命令单独修改这个`character_set_connection`系统变量。比方说客户端发送给服务器的请求中包含字节序列`0xE68891`，然后服务器针对该客户端的系统变量`character_set_client`为`utf8`，那么此时服务器就知道该字节序列其实是代表汉字`'我'`，如果此时服务器针对该客户端的系统变量`character_set_connection`为gbk，那么在计算机内部还需要将该字符转换为采用gbk字符集编码的形式，也就是`0xCED2`。

有同学可能会想这一步有点儿像脱了裤子放屁的意思，但是大家请考虑下边这个查询语句：

```ini
mysql> SELECT 'a' = 'A';
```

请问大家这个查询语句的返回结果应该是TRUE还是FALSE？其实结果是不确定。这是因为我们并不知道比较两个字符串的大小到底比的是什么！我们应该从两个方面考虑：

- 考虑一：这些字符串是采用什么字符集进行编码的呢？
- 考虑二：在我们确定了编码这些字符串的字符集之后，也就意味着每个字符串都会映射到一个字节序列，那么我们怎么比较这些字节序列呢，是直接比较它们二进制的大小，还是有别的什么比较方式？比方说`'a'`和`'A'`在utf8字符集下的编码分别为`0x61`和`0x41`，那么`'a' = 'A'`是应该直接比较`0x61`和`0x41`的大小呢，还是将`0x61`减去32之后再比较大小呢？其实这两种比较方式都可以，每一种比较方式我们都称作一种`比较规则`（英文名：`collation`）。

`MySQL`中支持若干种字符集，我们可以使用`SHOW CHARSET`命令查看，如下图所示（太多了，只展示几种，具体自己运行一下该命令）：

```sql
mysql> SHOW CHARSET;
+----------+---------------------------------+---------------------+--------+
| Charset  | Description                     | Default collation   | Maxlen |
+----------+---------------------------------+---------------------+--------+
| big5     | Big5 Traditional Chinese        | big5_chinese_ci     |      2 |
| latin1   | cp1252 West European            | latin1_swedish_ci   |      1 |
| latin2   | ISO 8859-2 Central European     | latin2_general_ci   |      1 |
| ascii    | US ASCII                        | ascii_general_ci    |      1 |
| gb2312   | GB2312 Simplified Chinese       | gb2312_chinese_ci   |      2 |
| gbk      | GBK Simplified Chinese          | gbk_chinese_ci      |      2 |
| utf8     | UTF-8 Unicode                   | utf8_general_ci     |      3 |
| utf8mb4  | UTF-8 Unicode                   | utf8mb4_general_ci  |      4 |
| utf16    | UTF-16 Unicode                  | utf16_general_ci    |      4 |
| utf16le  | UTF-16LE Unicode                | utf16le_general_ci  |      4 |
| utf32    | UTF-32 Unicode                  | utf32_general_ci    |      4 |
| binary   | Binary pseudo charset           | binary              |      1 |
| gb18030  | China National Standard GB18030 | gb18030_chinese_ci  |      4 |
+----------+---------------------------------+---------------------+--------+
41 rows in set (0.04 sec)
```

其中每一种字符集又对应着若干种比较规则，我们以utf8字符集为例（太多了，也只展示几个）：

```sql
mysql> SHOW COLLATION WHERE Charset='utf8';
+--------------------------+---------+-----+---------+----------+---------+
| Collation                | Charset | Id  | Default | Compiled | Sortlen |
+--------------------------+---------+-----+---------+----------+---------+
| utf8_general_ci          | utf8    |  33 | Yes     | Yes      |       1 |
| utf8_bin                 | utf8    |  83 |         | Yes      |       1 |
| utf8_unicode_ci          | utf8    | 192 |         | Yes      |       8 |
| utf8_icelandic_ci        | utf8    | 193 |         | Yes      |       8 |
| utf8_latvian_ci          | utf8    | 194 |         | Yes      |       8 |
| utf8_romanian_ci         | utf8    | 195 |         | Yes      |       8 |
+--------------------------+---------+-----+---------+----------+---------+
27 rows in set (0.00 sec)
```

其中`utf8_general_ci`是utf8字符集默认的比较规则，在这种比较规则下是不区分大小写的，不过`utf8_bin`这种比较规则就是区分大小写的。

在我们将请求中的字节序列转换为`character_set_connection`对应的字符集编码的字节序列后，也要配套一个对应的比较规则，这个比较规则就由`collation_connection`系统变量来指定。我们现在通过SET命令来修改一下` `和`collation_connection`的值分别设置为`utf8`和`utf8_general_ci`，然后比较一下`'a'`和`'A'`：

```sql
mysql> SET character_set_connection=utf8;
Query OK, 0 rows affected (0.00 sec)

mysql> SET collation_connection=utf8_general_ci;
Query OK, 0 rows affected (0.00 sec)

mysql> SELECT 'a' = 'A';
+-----------+
| 'a' = 'A' |
+-----------+
|         1 |
+-----------+
1 row in set (0.00 sec)
```

可以看到在这种情况下这两个字符串就是相等的。

我们现在通过SET命令来修改一下` `和`collation_connection`的值分别设置为`utf8`和`utf8_bin`，然后比较一下`'a'`和`'A'`：

```sql
mysql> SET character_set_connection=utf8;
Query OK, 0 rows affected (0.00 sec)

mysql> SET collation_connection=utf8_bin;
Query OK, 0 rows affected (0.00 sec)

mysql> SELECT 'a' = 'A';
+-----------+
| 'a' = 'A' |
+-----------+
|         0 |
+-----------+
1 row in set (0.00 sec)
```

可以看到在这种情况下这两个字符串就是不相等的。

当然，如果我们并不需要单独指定将请求中的字符串采用何种字符集以及比较规则的话，并不用太关心`character_set_connection`和`collation_connection`设置成啥，不过需要注意一点，就是`character_set_connection`对应的字符集必须包含请求中的字符。

### 服务器处理请求完毕生成对该客户端的响应

为了故事的顺利发展，我们先创建一个表：

```ini
CREATE TABLE t (
    c VARCHAR(100)
) ENGINE=INNODB CHARSET=utf8;
```

然后向这个表插入一条记录：

```sql
INSERT INTO t VALUE('我');
```

现在这个表中的数据就如下所示：

```sql
mysql> SELECT * FROM t;
+------+
| c    |
+------+
| 我   |
+------+
1 row in set (0.00 sec)
```

我们可以看到该表中的字段其实是使用`utf8`字符集编码的，所以底层存放格式是：`0xE68891`，将它读出后需要发送到客户端，是不是直接将`0xE68891`发送到客户端呢？这可不一定，这个取决于`character_set_result`系统变量的值，该系统变量也是一个SESSION级别的变量。服务器会将该响应转换为`character_set_result`系统变量对应的字符集编码后的字节序列发送给客户端。每个客户端在登录服务器的时候都会将客户端的默认字符集通知给服务器，然后服务器设置该客户端专属的`character_set_result`。

我们也可以使用SET命令来设置`character_set_result`的值。不过也需要注意，`character_set_result`对应的字符集应该包含响应中的字符。

这里再强调一遍，`character_set_client`、`character_set_connection`和`character_set_result`这三个系统变量是服务器的系统变量，每个客户端在与服务器建立连接后，服务器都会为这个连接维护这三个变量，如图所示（我们假设连接1的这三个变量均为`utf8`，连接1的这三个变量均为`gbk`，连接1的这三个变量均为`ascii`，）： ![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208222054237.webp) 一般情况下`character_set_client`、`character_set_connection`和`character_set_result`这三个系统变量应该和客户端的默认字符集相同，`SET names`命令可以一次性修改这三个系统变量：

```sql
SET NAMES 'charset_name'
```

该语句和下边三个语句等效：

```ini
SET character_set_client = charset_name;
SET character_set_results = charset_name;
SET character_set_connection = charset_name;
```

不过这里需要大家特别注意，`SET names`语句并不会改变客户端的默认字符集！

### 客户端接收到响应

客户端收到的响应其实仍然是一个字节序列。客户端是如何将这个字节序列写到黑框框中的呢，这又涉及到应用程序和操作系统之间的一次交互。

- 对于UNIX操作系统来说，MySQL客户端向黑框框中写入数据使用的是操作系统提供的`fputs`、`putc`或者`fwrite`函数，这些函数基本上相当于直接就把接收到的字节序列写到了黑框框中（请注意我们用词：`'基本上相当于'`，其实内部还会做一些工作，但是我们这里就不想再关注这些细节了）。此时如果该字节序列实际的字符集和黑框框展示字符所使用的字符集不一致的话，就会发生所谓的乱码（大家注意，这个时候和操作系统当前使用的字符集没啥关系）。

  比方说我们在启动MySQL客户端的时候使用了`--default-character-set=gbk`的启动参数，那么服务器的`character_set_result`变量就是gbk。然后再执行`SELECT * FROM t`语句，那么服务器就会将字符`'我'`的gbk编码，也就是`0xCDE2`发送到客户端，客户端直接把这个字节序列写到黑框框中，如果黑框框此时采用utf8字符集展示字符，那自然就会发生乱码。

- 对于Windows操作系统来说，MySQL客户端向黑框框中写入数据使用的是操作系统提供的`WriteConsoleW`函数，该函数接收一个宽字符数组，所以MySQL客户端调用它的时候需要显式地将它从服务器收到的字节序列按照客户端默认的字符集转换成一个宽字符数组。正因为这一步骤的存在，所以可以避免上边提到的一个问题。

  比方说我们在启动MySQL客户端的时候使用了`--default-character-set=gbk`的启动参数，那么服务器的`character_set_result`变量就是gbk。然后再执行`SELECT * FROM t`语句，那么服务器就会将字符`'我'`的gbk编码，也就是`0xCDE2`发送到客户端，客户端将这个字节序列先从客户端默认字符集，也就是gbk的编码转换成一个宽字符数组，然后再调用`WriteConsoleW`函数写到黑框框，黑框框自然可以把它显示出来。

## 乱码问题应该如何分析

好了，介绍了各个步骤中涉及到的各种字符集，大家估计也看的眼花缭乱了，下边总结一下我们遇到乱码的时候应该如何分析，而不是胡子眉毛一把抓，随便百度一篇文章，然后修改某个参数，运气好修改了之后改对了，运气不好改了一天也改不好。知其然也要知其所以然，在学习了本篇文章后，大家一定要有节奏的去分析乱码问题：

- 我使用的是什么操作系统

  - 对于UNIX系统用户来说，要搞清楚我使用的黑框框到底是使用什么字符集展示字符，就像是`iTerm2`中的`character encoding`属性： ![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208222054269.webp)
    同样还要搞清楚操作系统当前使用什么字符集，运行`locale`命令查看：

    ```ini
    王大爷喊你输入呢，跟这儿>locale
    LANG=""
    LC_COLLATE="zh_CN.UTF-8"
    LC_CTYPE="zh_CN.UTF-8"
    LC_MESSAGES="zh_CN.UTF-8"
    LC_MONETARY="zh_CN.UTF-8"
    LC_NUMERIC="zh_CN.UTF-8"
    LC_TIME="zh_CN.UTF-8"
    LC_ALL="zh_CN.UTF-8"
    王大爷喊你输入呢，跟这儿>
    ```

    没有什么特别极端的特殊需求的话，一定要保证上述两个字符集是相同的，否则可能连汉字都输入不进去！

  - 对于Windows用户来说

    搞清楚自己使用的黑框框的代码页是什么，也就是操作系统当前使用的字符集是什么。

- 搞清楚客户端的默认字符集是什么

  启动MySQL客户端的时候有没有携带`--default-character-set`参数，如果携带了，那么客户端默认字符集就以该参数指定的值为准。否则分析自己操作系统当前使用的字符集是什么。

- 搞清楚客户端发送请求时是以什么字符集编码请求的

  - 对于UNIX系统来说，我们可以认为请求就是采用操作系统当前使用的字符集进行编码的。
  - 对于Windows系统来说，我们可以认为请求就是采用客户端默认字符集进行编码的。

- 通过执行`SHOW VARIABLES LIKE 'character%'`命令搞清楚：

  - `character_set_client`：服务器是怎样认为客户端发送过来的请求是采用何种字符集编码的
  - `character_set_connection`：服务器在运行过程中会采用何种字符集编码请求中的字符
  - `character_set_result`：服务器会将响应使用何种字符集编码后再发送给客户端的

- 客户端收到响应之后：

  对于服务器发送过来的字节序列来说：

  - 在UNIX操作系统上，可以认为会把该字节序列直接写到黑框框里。此时应该搞清楚我们的黑框框到底是采用何种字符集展示数据。
  - 在Windows操作系统上，该字节序列会被认为是由客户端字符集编码的数据，然后再转换成宽字符数组写入到黑框框中。

请认真分析上述的每一个步骤，然后发出惊呼：小样，不就是个乱码嘛，还治不了个你！