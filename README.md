# JavaInterview

java面试资料整理，涵盖基础知识，分布式，消息队列，redis，并发编程，jvm，常用框架，面试真题等....
├── IDEA奇技淫巧
│   └── 多光标使用技巧.md
├── Java并发编程JUC
│   ├── Java并发 - ThreadLocal详解.md
│   ├── Java并发 - 理论与线程基础.md
│   ├── JUC锁 - LockSupport详解.md
│   ├── JUC锁 - 聊聊Lock锁.md
│   ├── JUC线程池 - FutureTask详解.md
│   ├── JUC线程池 - ThreadPoolExecutor详解.md
│   ├── JUC原子类: CAS, Unsafe和原子类详解.md
│   ├── 关键字
│   │   ├── 对final的理解.md
│   │   ├── 对Volatile的理解.md
│   │   └── 聊聊Synchronized.md
│   └── 面试题.md
├── java基础
│   ├── java特性
│   │   ├── Java8 - LocalDateLocalDateTime.md
│   │   ├── Java8 - 函数式编程.md
│   │   └── Java8 - 默认方法.md
│   └── 基础知识点及java机制
│       ├── java反射机制详解.md
│       ├── SPI机制详解.md
│       ├── 泛型机制详解.md
│       ├── 基础知识点.md
│       └── 异常机制详解.md
├── JVM
│   ├── JVM基础 - JAVA类加载机制.md
│   ├── JVM基础 - Java内存模型.md
│   ├── JVM基础 - JVM内存结构.md
│   └── JVM 基础 - 类字节码详解.md
├── README.md
├── Spring
│   ├── Spring——AOP.md
│   ├── Spring——IOC.md
│   ├── Spring常见问题总结.md
│   ├── Spring——事务.md
│   ├── Spring循环依赖.pdf
│   └── Spring注解分析.md
├── 补充
│   ├── Itext7_PDF
│   │   ├── Itext7使用简介.md
│   │   └── ItextDemo
│   │       ├── bin
│   │       │   ├── main
│   │       │   │   ├── application.properties
│   │       │   │   └── com
│   │       │   │       └── ytking
│   │       │   │           └── itextdemo
│   │       │   │               ├── DevActivityInfoDao.class
│   │       │   │               ├── EnterpriceRegDao.class
│   │       │   │               ├── ItextController$1.class
│   │       │   │               ├── ItextController.class
│   │       │   │               ├── ItextDemoApplication.class
│   │       │   │               └── PathUtils.class
│   │       │   └── test
│   │       │       └── com
│   │       │           └── ytking
│   │       │               └── itextdemo
│   │       │                   └── ItextDemoApplicationTests.class
│   │       ├── build.gradle
│   │       ├── gradle
│   │       │   └── wrapper
│   │       │       ├── gradle-wrapper.jar
│   │       │       └── gradle-wrapper.properties
│   │       ├── gradlew
│   │       ├── gradlew.bat
│   │       ├── mode1.pdf
│   │       ├── mode.docx
│   │       ├── mode.pdf
│   │       ├── settings.gradle
│   │       ├── src
│   │       │   ├── main
│   │       │   │   ├── java
│   │       │   │   │   └── com
│   │       │   │   │       └── ytking
│   │       │   │   │           └── itextdemo
│   │       │   │   │               ├── DevActivityInfoDao.java
│   │       │   │   │               ├── EnterpriceRegDao.java
│   │       │   │   │               ├── ItextController.java
│   │       │   │   │               ├── ItextDemoApplication.java
│   │       │   │   │               └── PathUtils.java
│   │       │   │   └── resources
│   │       │   │       └── application.properties
│   │       │   └── test
│   │       │       └── java
│   │       │           └── com
│   │       │               └── ytking
│   │       │                   └── itextdemo
│   │       │                       └── ItextDemoApplicationTests.java
│   │       ├── testImage.jpg
│   │       └── test.pdf
│   ├── java——UML类图.md
│   ├── 软件工程考点复习.md
│   └── 数据库缓存一致性研究
│       └── 数据库缓存一致性研究.md
├── 计算机网络
│   ├── 高频面试题.md
│   ├── 图解http阅读笔记.md
│   └── 图解TCPIP阅读笔记.md
├── 设计模式
│   ├── 创建型模式
│   │   ├── Abstract Factory（抽象工厂）——对象创建型模式.md
│   │   ├── Builder（生成器）——对象创建型模式.md
│   │   ├── Factory Method（工厂方法）——对象创建型模式.md
│   │   └── Prototype（原型）——对象创建型模式.md
│   ├── 行为模式
│   │   └── 模板方法模式.md
│   ├── 结构型模式
│   │   └── 适配器模式.md
│   └── 设计原则
│       └── 里氏替换原则.md
├── 数据库
│   ├── MySQL
│   │   ├── MySQL的RR级别解决幻读问题了吗.md
│   │   ├── MySQL加锁规则.md
│   │   ├── MySQL索引原理及慢查询优化.md
│   │   ├── MySQL知识体系.md
│   │   └── MySQL中的sql如何执行与解析.md
│   ├── MySQL是怎样运行的：从根儿上理解MySQL
│   │   ├── 10、存放页面的大池子 —— InnoDB 的表空间.md
│   │   ├── 11、条条大路通罗马 —— 单表访问方法.md
│   │   ├── 12、两个表的亲密接触 —— 连接的原理.md
│   │   ├── 13、谁最便宜就选谁 —— MySQL 基于成本的优化.md
│   │   ├── 14、兵马未动，粮草先行 —— InnoDB 统计数据是如何收集的.md
│   │   ├── 15、不好看就要多整容 —— MySQL 基于规则的优化（内含关于子查询优化二三事儿）.md
│   │   ├── 16、查询优化的百科全书 —— Explain 详解（上）.md
│   │   ├── 17、查询优化的百科全书 —— Explain 详解（下）.md
│   │   ├── 18、神兵利器 —— optimizer trace 的神奇功效.md
│   │   ├── 19、调节磁盘和CPU的矛盾 —— InnoDB 的 Buffer Pool.md
│   │   ├── 20、从猫爷被杀说起 —— 事务简介.md
│   │   ├── 21、说过的话就一定要办到 —— redo 日志（上）.md
│   │   ├── 22、说过的话就一定要办到 —— redo 日志（下）.md
│   │   ├── 23、后悔了怎么办 —— undo 日志 (上).md
│   │   ├── 24、后悔了怎么办 —— undo 日志 (下).md
│   │   ├── 25、一条记录的多幅面孔 —— 事务的隔离级别与MVCC.md
│   │   ├── 26、工作面试老大难 —— 锁.md
│   │   ├── 2、装作自己是个小白 —— 重新认识MySQL.md
│   │   ├── 3、MySQL的调控按钮 —— 启动选项和系统变量.md
│   │   ├── 4、乱码的前世今生 —— 字符集和比较规则.md
│   │   ├── 5、从一条记录说起—— InnoDB 记录结构.md
│   │   ├── 6、盛放记录的大盒子 —— InnoDB 数据页结构.md
│   │   ├── 7、快速查询的秘籍 —— B+ 树索引.md
│   │   ├── 8、好东西也得先学会怎么用 —— B+ 树索引的使用.md
│   │   ├── 9、数据的家 —— MySQL 的数据目录.md
│   │   └── 专题讲解
│   │       ├── 28、专题式讲解 —— MySQL的server层和存储引擎层是如何交互的.md
│   │       ├── 29、专题式讲解 —— MySQL查询成本和扫描区间(MySQL中IS NULL、IS NOT NULL、!=不能用索引？胡扯！).md
│   │       ├── 30、专题式讲解 —— 听说有一个最左原则？这回终于讲清楚了.md
│   │       ├── 31、专题式讲解 —— 设计MySQL的大叔为何偏爱ref.md
│   │       ├── 32、专题式讲解 —— MySQL中NULL值引起的小锅.md
│   │       ├── 33、专题式讲解 —— MySQL使用索引执行IN子句.md
│   │       ├── 34、专题式讲解 —— MySQL的COUNT语句是怎么执行的.md
│   │       ├── 35、专题式讲解 —— MySQL的LIMIT这么差劲的吗.md
│   │       ├── 36、专题式讲解 —— MySQL：为什么查询列表中多了它，GROUP BY语句就会报错呢？.md
│   │       ├── 37、专题式讲解 —— MySQL冷知识：t1.id=t2.id=t3.id看着咋这么怪呢？.md
│   │       ├── 38、专题式讲解 —— MySQL乱码.md
│   │       ├── 39、专题式讲解 —— 字符从UTF-8转成GBK发生了什么？.md
│   │       ├── 40、专题式讲解 —— MySQL的定点数类型.md
│   │       ├── 41、专题式讲解 —— Innodb到底是怎么加锁的.md
│   │       ├── 42、专题式讲解 —— 语句加锁分析实例.md
│   │       ├── 43专题式讲解 —— MySQL如何查看事务加锁情况.md
│   │       ├── 44、专题式讲解 —— MySQL介于普通读和锁定读的加锁方式—— semi-consistent read.md
│   │       ├── 45、专题式讲解 —— 两条一样的INSERT语句竟然引发了死锁？.md
│   │       ├── 46、专题式讲解 —— binlog那些事儿（一）.md
│   │       ├── 47、专题式讲解 —— binlog那些事儿（二）.md
│   │       ├── 48、专题式讲解 —— redo、undo、buffer pool、binlog，谁先谁后，有点儿乱.md
│   │       └── 49、专题式讲解 —— XA事务与两阶段提交.md
│   ├── Redis
│   │   ├── Redis——Stream详解（未完）.md
│   │   ├── Redis底层数据结构.md
│   │   └── Redis基础知识.md
│   ├── 关系型数据库是如何工作的.md
│   └── 数据库系统核心知识点.md
├── 算法demo
│   ├── out
│   │   └── production
│   │       └── 算法demo
│   │           ├── 递归
│   │           │   ├── 归并求逆序对.class
│   │           │   └── 快排_基于荷兰旗思想.class
│   │           ├── 递归和动态规划
│   │           │   ├── 换钱的最少货币数.class
│   │           │   ├── 机器人达到指定位置方法数_重点.class
│   │           │   └── 矩阵的最小路径和_压缩空间方法.class
│   │           ├── 堆
│   │           │   ├── K范围内有序的排序问题.class
│   │           │   ├── K范围内有序的排序问题$comp.class
│   │           │   └── 堆操作.class
│   │           ├── 基数排序.class
│   │           ├── 链表
│   │           │   ├── 打印两个升序链表的公共部分.class
│   │           │   ├── 判断回文链表.class
│   │           │   └── 判断回文链表$ListNode.class
│   │           ├── 数组和矩阵
│   │           │   ├── 不重复打印排序数组中相加和为给定值的所有二元组.class
│   │           │   ├── 不重复打印排序数组中相加和为给定值的所有三元组.class
│   │           │   ├── 生成窗口最大值数组.class
│   │           │   ├── 未排序数组中累加和为给定值的最长子数组长度_含负数.class
│   │           │   ├── 未排序数组中累加和为给定值的最长子数组系列问题_补充扩展.class
│   │           │   ├── 未排序正数数组中累加和为给定值的最长子数组的长度.class
│   │           │   └── 最长的可整合子数组的长度.class
│   │           └── 栈和队列
│   │               ├── 单调栈结构.class
│   │               ├── 用递归函数和栈逆序一个栈.class
│   │               └── 由两个栈组成的队列.class
│   ├── src
│   │   ├── 递归
│   │   │   ├── 归并求逆序对.java
│   │   │   └── 快排_基于荷兰旗思想.java
│   │   ├── 递归和动态规划
│   │   │   ├── 换钱的最少货币数.java
│   │   │   ├── 机器人达到指定位置方法数_重点.java
│   │   │   └── 矩阵的最小路径和_压缩空间方法.java
│   │   ├── 堆
│   │   │   ├── K范围内有序的排序问题.java
│   │   │   └── 堆操作.java
│   │   ├── 基数排序.java
│   │   ├── 链表
│   │   │   ├── 打印两个升序链表的公共部分.java
│   │   │   ├── 反转链表.java
│   │   │   └── 判断回文链表.java
│   │   ├── 数组和矩阵
│   │   │   ├── 不重复打印排序数组中相加和为给定值的所有二元组.java
│   │   │   ├── 不重复打印排序数组中相加和为给定值的所有三元组.java
│   │   │   ├── 生成窗口最大值数组.java
│   │   │   ├── 未排序数组中累加和为给定值的最长子数组长度_含负数.java
│   │   │   ├── 未排序数组中累加和为给定值的最长子数组系列问题_补充扩展.java
│   │   │   ├── 未排序正数数组中累加和为给定值的最长子数组的长度.java
│   │   │   ├── 子矩阵的最大累加和问题.java
│   │   │   └── 最长的可整合子数组的长度.java
│   │   └── 栈和队列
│   │       ├── 单调栈结构.java
│   │       ├── 单调栈结构_进阶包含重复数据.java
│   │       ├── 求最大子矩阵的大小.java
│   │       ├── 用递归函数和栈逆序一个栈.java
│   │       └── 由两个栈组成的队列.java
│   ├── 笔记
│   │   └── 布隆过滤器.md
│   └── 算法demo.iml
└── 阅读资料
    ├── 1000道 互联网Java工程师面试题 .pdf
    ├── 200道java面试题资料.pdf
    ├── Java 8实战.pdf
    ├── Java开发手册(黄山版).pdf
    ├── Java 全栈知识点问题汇总（上） _ Java 全栈知识体系.pdf
    ├── Spring揭秘.pdf
    ├── 阿里java开发手册1.5.pdf
    ├── 《关键对话》免费下载（高效能沟通，营造无往不利的事业和人生）.pdf
    ├── 设计模式之禅（第2版）.pdf
    ├── 图解HTTP 彩色版.pdf
    └── 《图解TCP IP(第5版)》.((日)竹下隆史).[PDF].&ckook.pdf

# 链接

[Java8 新特性](https://mp.weixin.qq.com/s/ojyl7B6PiHaTWADqmUq2rw)

[记录一次前后端分离完成cas单点登录的过程](https://juejin.cn/post/7068922646448046094)

[幂等设计](https://mp.weixin.qq.com/s?__biz=Mzg3NzU5NTIwNg==&mid=2247497427&idx=1&sn=2ed160c9917ad989eee1ac60d6122855&chksm=cf2229faf855a0ecf5eb34c7335acdf6420426490ee99fc2b602d54ff4ffcecfdab24eeab0a3&token=529683793&lang=zh_CN#rd)

[如何向singleton级别的bean中注入request级别的bean](https://juejin.cn/post/7070290856967667742)

[关于Mysql索引的数据结构](https://www.modb.pro/db/404375)
