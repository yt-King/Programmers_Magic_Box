# MySQL加锁规则

[ininodb加锁](https://mp.weixin.qq.com/s?__biz=MzIxNTQ3NDMzMw==&mid=2247486432&idx=1&sn=720f7a8c8fe06dea56f016d2eb1600ae&chksm=97968212a0e10b048f2f98663a13c2aac1fb004a918f8f20344d0b14c5e353ee14d5e3d80b4a&scene=20&xtrack=1#rd)

[语句加锁分析](https://mp.weixin.qq.com/s?__biz=MzIxNTQ3NDMzMw==&mid=100000550&idx=1&sn=8a24323f2b8b10c479ec315efbcc7d53&chksm=17968ad420e103c247d196d1a57376864dfd30c810463ed5dbc5dfcf7785de611a4ea36d9ef7&scene=20&xtrack=1#rd)

## InnoDB锁的内存结构

我们来看一下InnoDB锁的内存结构，InnoDB中用`lock_t`这个结构来定义：

![image-20220815093948210](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208150939280.png)

**对一条记录加锁的本质就是在内存中创建一个锁结构与之关联**，那么是不是一个事务对多条记录加锁，就要创建多个锁结构呢？如果一个事务要获取10000条记录的锁，就要生成10000个这样的结构，开销就太大了。

InnoDB在对不同记录加锁时，如果符合下边这些条件，那么这些记录的锁就可以被放到**一个锁结构中：**

- 在同一个事务中进行加锁操作
- 被加锁的记录在同一个页面中
- 加锁的类型是一样的
- 等待状态是一样的

锁的内存结构示意图如下：

![image-20220815094134187](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208150941252.png)

接下来具体分析一下每个结构所代表的是什么信息

- 锁所在的事务信息：哪个事务生成了这个锁结构，就记载这个事务的信息。

- 索引信息：对于行锁来说，需要记录一下加锁的记录是属于哪个索引的。

- 表锁/行锁信息 ：表锁结构和行锁结构在这个位置的内容是不同的：

  - 表锁：记载着这是对哪个表加的锁，还有其他的一些信息。
  - 行锁，记载了三个重要的信息：
    - `Space ID`：记录所在表空间。
    - `Page Number`：记录所在页号。
    - `n_bits`：对于行锁来说，一条记录就对应着一个比特位，一个页面中包含很多记录，用不同的比特位来区分到底是哪一条记录加了锁。为此在行锁结构的末尾放置了一堆比特位，这个`n_bits`属性代表使用了多少比特位。

- type_mode：这是一个32位的数，被分成了lock_mode、lock_type和rec_lock_type三个部分。

  <img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208150948106.png" alt="image-20220815094858990" style="zoom: 50%;" />

  - 锁的模式（`lock_mode`），占用低4位，可选的值如下：
    - `LOCK_IS`（十进制的`0`）：表示共享意向锁，也就是`IS锁`。
    - `LOCK_IX`（十进制的`1`）：表示独占意向锁，也就是`IX锁`。
    - `LOCK_S`（十进制的`2`）：表示共享锁，也就是`S锁`。
    - `LOCK_X`（十进制的`3`）：表示独占锁，也就是`X锁`。
    - `LOCK_AUTO_INC`（十进制的`4`）：表示`AUTO-INC锁`。
  - 
  - 锁的类型（`lock_type`），占用第5～8位，不过现阶段只有第5位和第6位被使用：
    - `LOCK_TABLE`（十进制的`16`），也就是当第5个比特位置为1时，表示表级锁。
    - `LOCK_REC`（十进制的`32`），也就是当第6个比特位置为1时，表示行级锁。
  - 行锁的具体类型（`rec_lock_type`），使用其余的位来表示。只有在`lock_type`的值为`LOCK_REC`时，也就是只有在该锁为行级锁时，才会被细分为更多的类型：
    - `LOCK_ORDINARY`（十进制的`0`）：表示`next-key锁`。
    - `LOCK_GAP`（十进制的`512`）：也就是当第10个比特位置为1时，表示`gap锁`。
    - `LOCK_REC_NOT_GAP`（十进制的`1024`）：也就是当第11个比特位置为1时，表示`正经记录锁`。
    - `LOCK_INSERT_INTENTION`（十进制的`2048`）：也就是当第12个比特位置为1时，表示插入意向锁。
    - 其他的类型：还有一些不常用的类型。

- 其他信息 ：为了更好的管理系统运行过程中生成的各种锁结构而设计了各种哈希表和链表。

- 比特位：如果是行锁结构的话，在该结构末尾还放置了一堆比特位。页面中的每条记录在记录头信息中都包含一个heap_no属性，伪记录Infimum的heap_no值为0，Supremum的heap_no值为1，之后每插入一条记录，heap_no值就增1。锁结构最后的一堆比特位就对应着一个页面中的记录，一个比特位映射一个heap_no。

  ![image-20220815095351648](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208150953703.png)