# 1.Class文件结构

## 1.1. 概述

### 1.1.1. 字节码文件的跨平台性

**1、Java 语言：跨平台的语言（write once, run anywhere）**

- 当Java源代码成功编译成字节码之后，如果想在不同的平台上运行，不需要再次编译。
- 这个优势不是很吸引人了，因为Python、PHP、Perl、Ruby、Lisp等语言都有强大的解释器
- 跨平台已经几乎快成为一门语言的必选特性

**2、Java 虚拟机：跨语言的平台**

- **`Java虚拟机不与包含Java在内的任何语言进行绑定，它只和 "Class" 文件这种特定的二进制文件格式关联`**。无论使用何种语言进行软件开发， 只要能将源文件编译为正确的 Class 文件，那么这种语言就可以在 Java 虚拟机上执行，可以说，统一而强大的 Class 文件结构，就是 Java 虚拟机的基石、桥梁。

![image-20201129235101373](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222927313-1282513211.png)

**JVM规范：**

https://docs.oracle.com/javase/specs/index.html

官方文档路径：

![image-20201129235626698](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222927017-618050031.png)

点击

![image-20201129235955609](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222926725-2010997453.png)

![image-20201130000015501](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222926406-1364298638.png)

![image-20201130000058105](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222926127-530325220.png)

所有的 JVM 全部遵守 Java 虚拟机规范，也就是说所有的 JVM 环境都是一样的， 这样一来字节码文件可以在各种 JVM 上进行。

**想要让一个 Java 程序正确地运行在 JVM 中，Java 源码就是必须要被编译为符合 JVM 规范的字节码。**

**前端编译器的主要任务**就是负责将符合 Java 语法规范的 Java 代码转换为符合 JVM 规范的字节码文件。

javac 是一种能够将 Java 源码编译为字节码的前端编译器。

javac 编译器在将 Java 源码编译为一个有效的字节码文件过程中经历了4个步骤，分别是**`词法分析、语法分析、语义分析以及生成字节码。`**

![image-20201130213939937](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222925822-1972209088.png)

Oracle 的 JDK 软件包括两部分内容：

- 一部分是将 Java 源代码编译成 Java 虚拟机的指令集的编译器(java源代码---》字节码文件)
- 另一部分是用于实现 Java 虚拟机的运行时环境（解释器、即时编译器...）

### 1.1.2. Java的前端编译器

![image-20201130214131991](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222925476-674321732.png)

前端编译器 VS 后端编译器

Java 源代码的编译结果是字节码，那么肯定需要有一种编译器能够将 Java 源码编译为字节码，承担这个重要责任的就是配置在 path 环境变量中的 **`javac 编译器`**。javac 是一种能够将 Java 源码编译为字节码的**`前端编译器`**。

HotSpot VM 并没有强制要求前端编译器只能使用 javac 来编译字节码，其实只要编译结果符合 JVM 规范都可以被 JVM 所识别即可。在 Java 的前端编译器领域，除了 javac 之外，还有一种被大家经常用到的前端编译器，那就是内置在 Eclipse 中的 **`ECJ (Eclipse Compiler for Java)编译器`**。和 javac 的`全量式`编译不同，ECJ 是一种`增量式`编译器。

- 在 Eclipse 中，当开发人员编写完代码后，使用"Ctrl + S"快捷键时，ECJ 编译器所采取的**编译方案**是把未编译部分的源码逐行进行编译，而非每次都全量编译。因此 ECJ 的编译效率会比 javac 更加迅速和高效，当然编译质量和 javac 相比大致还是一样的。
- ECJ 不仅是 Eclipse 的默认内置前端编译器，在 Tomcat 中同样也是使用 ECJ 编译器来编译 jsp 文件。由于 ECJ 编译器是采用 GPLv2 的开源协议进行源代码公开，所以，大家可以登录 Eclipse 官网下载 ECJ 编译器的源码进行二次开发。
- 默认情况下，IntelliJ IDEA 使用 javac 编译器（还可以自己设置为 AspectJ 编译器 ajc）

前端编译器并不会直接涉及编译优化等方面的技术，而是将这些具体优化细节移交给 HotSpot 的 JIT 编译器负责。

复习:AOT(静态提前编译器，Ahead of Time compiler):在程序运行之前，直接把字节码编译成本地机器指令（无需经过翻译器或者即时编译器）

### 1.1.3. 透过字节码指令看代码细节

1、BAT 面试题

① 类文件结构有几个部分？

② 知道字节码吗？字节码都有哪些？Integer x = 5; int y = 5; 比较 x == y 都经过哪些步骤？

2、代码举例

例子1：

```java
package _10;
 
public class _01_IntegerTest {
    public static void main(String[] args) {
        Integer x = 5;
        int y = 5;
        System.out.println(x == y);
 
        Integer i1 = 10;
        Integer i2 = 10;
        System.out.println(i1 == i2);
 
        Integer i3 = 128;
        Integer i4 = 128;
        System.out.println(i3 == i4);
    }
}
 
```

运行结果

```ini
x == y：true
i1 == i2：true
i3 == i4：false
```

查看字节码

![image-20201130220252193](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222925126-1946346545.png)

定义Integer变量就是调用valueOf()方法

![image-20201201220507118](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222924730-866054613.png)

看下Integer.valueOf()的源码

![image-20201201220137239](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222924401-139962909.png)

解释：

![image-20201201220715082](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222924079-1715581609.png)

![image-20201201231256863](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222923709-2133990627.png)

例子2：

```java
public class _02_StringTest {
    public static void main(String[] args) {
        String str = new String("hello") + new String("world");
        String str2 = "helloword";
        System.out.println(str2 == str);  // 输出：false
    }
}
```

字节码，具体分析参考“JVM详解-上篇”

![image-20201201232316078](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222923356-1021768892.png)

例子3：

```java
package _10;
 
/**
 * 成员变量（非静态的）的赋值过程：
 * 1. 默认初始化
 * 2. 显示初始化 / 代码块中初始化
 * 3. 构造器初始化
 * 4. 有了对象之后，可以“对象.属性”或“对象.方法” 的方式对成员变量进行赋值。
 *
 */
class Father {
    int x = 10;
 
    public Father() {
        this.print();
        x = 20;
    }
 
    public void print() {
        System.out.println("Father.x = " + x);
    }
}
 
class Son extends Father {
    int x = 30;
 
    public Son() {
        this.print();
        x = 40;
    }
 
    public void print() {
        System.out.println("Son.x = " + x);
    }
}
 
public class _03_SonTest {
    public static void main(String[] args) {
        Father f = new Son();
        System.out.println(f.x);
    }
}
 
```

运行

```undefined
Son.x = 0
Son.x = 30
20
```

分析：

注释掉new Son();先看下new Father()的情况

![image-20201201235158789](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222923089-813640832.png)

运行

```undefined
Father.x = 10
20
```

结合字节码分析

![image-20201202000252633](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222922733-1904506301.png)

换回来new Son();

![image-20201202000353074](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222922360-548486127.png)

运行结果

```undefined
Son.x = 0
Son.x = 30
20
```

分析（注意Son的x和Father的x不是同一个x）

![image-20201202234019989](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222921848-1207882256.png)

---

## 1.2. 虚拟机的基石：Class文件

（调优也可以从代码层面调优，所以要关注字节码文件，不单纯是JVM的参数调优。）

**字节码文件里是什么？**

源代码经过编译器编译之后便会生成一个字节码文件，字节码是一种二进制的类文件，它的内容是 JVM 的指令，而不像 C、C++ 经由编译器直接生成机器码。

**什么是`字节码指令(byte code)`？**

Java 虚拟机的指令由一个字节长度的、代表着某种特定操作含义的**`操作码(opcode)`**以及跟随其后的零至多个代表此操作所需参数的**`操作数(operand)`**所构成。虚拟机中许多指令并不包含操作数，只有一个操作码。

比如：操作码（操作数） - 这两个加在一起就是字节码指令，只是操作数可有可无。

![image-20201202234739533](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222921508-849248851.png)

![image-20201202235235580](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222921190-589693086.png)

![image-20201202235340753](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222920882-2084251895.png)

**如何解读供虚拟机解释执行的二进制字节码？**

方式一：一个一个二进制的看，这里用到的是 Notepad++，需要安装一个 HEX-Editor 插件，或者使用 Binary Viewer

![image-20220421012132395](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222920631-1834115333.png)

使用插件

![image-20220421011853942](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222920340-1016312468.png)

方式二：使用 javap 指令，JDK 自带的反解析工具

![image-20201202235925174](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222920070-345329846.png)

方式三：使用 IDEA 插件，jclasslib 或 jclasslib bytecode viewer 客户端工具

![image-20201202235436897](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222919786-1604786920.png)

IDEA的jclasslib就是经常用的这个

![image-20201202235528615](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222919535-1572962603.png)

或

![image-20201202235705778](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222919248-1673955411.png)

打开

![image-20201202235900542](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222918708-1385266559.png)

---

## 1.3. Class文件结构

（这节的小节目录顺序需要记一下）

**• 官方文档位置**

https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html

![image-20201205234024093](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222918209-1275238184.png)

**• Class 类的本质**

任何一个 Class 文件都对应着唯一一个类或接口的定义信息，但反过来说，Class 文件实际上它并不一定以磁盘文件形式存在，有可能来源于网络传输。

Class 文件是一组以8位字节为基础单位的**`二进制流`**。

**• Class 文件格式**

Class 的结构不像 XML 等描述语言，由于它**`没有任何分隔符号`**。所以在其中的数据项，无论是字节顺序还是数量，都是被严格限定的，哪个字节代表什么含义，长度是多少，先后顺序如何，都不允许改变。

> 没有任何分隔符：“下雨天留客天留我不留”。
>
> 下雨天留客天，留我不留？
>
> 下雨天，留客天，留我不？留！
>
> 下雨，天留客？天留，我不留！

Class 文件格式采用一种类似于 C 语言结构体的方式进行数据存储，这种结构中只有两种数据类型：**无符号数**和**表**。

- **无符号数**属于基本的数据类型，以 u1、u2、u4、u8 来分别代表1个字节、2个字节、4个字节和8个字节的无符号数，无符号数可以用来描述数字、索引引用、数量值或者按照 UTF-8 编码构成字符串值。
- **表**（表相当于Java的数组）是由多个无符号数或者其他表作为数据项构成的复合数据类型，所有表都习惯性地以"_info"结尾。`表用于描述有层次关系的复合结构的数据`，整个 Class 文件本质上就是一张表。由于表没有固定长度，所以通常会在其前面加上具体的数来标明表的长度（因为没有分隔符）。

以"_info"结尾

![image-20201205233940233](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222917918-113370498.png)

表用于描述有层次关系的复合结构的数据

![image-20201206100214962](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222917542-1968136932.png)

整个 Class 文件本质上就是一张表

![image-20201206100606116](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222917216-983477128.png)

举例：

```java
public class _04_Demo {     // Demo类，前面的数字只是为了辅助排序用的
    private int num = 1;
 
    public int add() {
        num = num + 2;
        return num;
    }
}
 
```

![image-20201220111129548](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222916948-1872915287.png)

编译一下

![image-20201206101249481](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222916682-809931079.png)

查看.class文件，反编译

![image-20201206100904103](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222916368-1745557226.png)

字节码文件_04_Demo.class用notepad++打开，是乱码的

![image-20201206103331438](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222916032-1894682722.png)

先安装插件，查看版本

![image-20201206102410516](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222915793-1922219042.png)

![image-20201206102421338](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222915521-999517720.png)

下载插件HexEdit：https://github.com/chcg/NPP_HexEdit/releases

![image-20201206102459879](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222915199-1761979899.png)

解压得到dll

![image-20201206102531105](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222914909-1358710726.png)

dll放入新建目录，复制到plugins目录，重启即可

![image-20201206103000548](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222914642-852611399.png)

![image-20201206103017160](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222914396-1852526666.png)

![image-20201206103105382](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222914136-1174397402.png)

![image-20201206103257252](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222913862-1721535903.png)

**换句话说，充分理解了每一个字节码文件的细节，自己也可以反编译出 Java 源文件来**。

**• Class 文件结构概述**

Class 文件的结构并不是一成不变的，随着 Java 虚拟机的不断发展，总是不可避免地会对 Class 文件结构做出一些调整，但是其基本结构和框架是非常稳定的。

**Class 文件的总体结构如下：**

- 魔数
- Class 文件版本
- 常量池
- 访问标志
- 类索引、父类索引、接口索引集合
- 字段表集合
- 方法表集合
- 属性表集合

![image-20201206105541799](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222913596-60409282.png)

![image-20201206111356535](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222913265-179561718.png)

### 1.3.1. Java字节码总的结构表

整合成一张Java字节码总的结构表，后面对字节码逐一解读会用到。

| 类型           | 名称                | 说明                   | 长度    | 数量                  |
| :------------- | :------------------ | :--------------------- | :------ | :-------------------- |
| u4             | magic               | 魔数,识别Class文件格式 | 4个字节 | 1                     |
| u2             | minor_version       | 副版本号(小版本)       | 2个字节 | 1                     |
| u2             | major_version       | 主版本号(大版本)       | 2个字节 | 1                     |
| u2             | constant_pool_count | 常量池计数器           | 2个字节 | 1                     |
| cp_info        | constant_pool       | 常量池表               | n个字节 | constant_pool_count-1 |
| u2             | access_flags        | 访问标识               | 2个字节 | 1                     |
| u2             | this_class          | 类索引                 | 2个字节 | 1                     |
| u2             | super_class         | 父类索引               | 2个字节 | 1                     |
| u2             | interfaces_count    | 接口计数器             | 2个字节 | 1                     |
| u2             | interfaces          | 接口索引集合           | 2个字节 | interfaces_count      |
| u2             | fields_count        | 字段计数器             | 2个字节 | 1                     |
| field_info     | fields              | 字段表                 | n个字节 | fields_count          |
| u2             | methods_count       | 方法计数器             | 2个字节 | 1                     |
| method_info    | methods             | 方法表                 | n个字节 | methods_count         |
| u2             | attributes_count    | 属性计数器             | 2个字节 | 1                     |
| attribute_info | attributes          | 属性表                 | n个字节 | attributes_count      |

![image-20201206111947155](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222912935-1379502184.png)

复制16进制的字节码（java8环境编译下）

![image-20201206162426579](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222912622-1851313894.png)

粘贴出来，可以看到00都变成了20

![image-20201206162816726](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222912367-1741288825.png)

替换一下20为00，然后按照原来的长度换行一下

![image-20201206114325085](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222912071-1231354053.png)

空格替换为tab键（末尾最后的tab键去掉）

![image-20201206114303579](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222911804-700458630.png)

直接复制粘贴到Excel

![image-20201206114447029](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222911503-620989746.png)

然后调整一下个位数的，前面添加0

![image-20201206114609492](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222911212-979646955.png)

![image-20201206114707574](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222910903-335444544.png)

然后格式刷一下全部数据

![image-20201206114754788](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222910664-252352221.png)

右对齐，然后格式化再刷一下全部

![image-20201206115041497](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222910397-1213987123.png)

完成，后面就会对这些16进制的字节码进行逐一解析

![image-20201206115121289](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222910125-975948231.png)

### 1.3.2. 魔数：Class文件的标志

**Magic Number(魔数)**

- 每个 Class 文件开头的4个字节的无符号整数称为魔数(Magic Number)。
- 它的唯一作用是确定这个文件是否为一个能被虚拟机接受的有效合法的 Class 文件。即：`魔数是 Class 文件的标识符`。
- 魔数值固定为 0xCAFEBABE，不会改变（0x表示16进制，cafebabe是咖啡宝贝）。
- 如果一个 Class 文件不以 0xCAFEBABE 开头，虚拟机在进行文件校验的时候就会直接抛出以下错误：

```vbnet
Error: A JNI error has occurred, please check your installation and try again
Exception in thread "main" java.lang.ClassFormatError: Incompatible magic value 1885430635 in class file StringTest
```

- 使用魔数而不是扩展名来进行识别主要是基于安全方面的考虑，因为文件扩展名可以随意地改动

![image-20201206115638664](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222909854-1752677777.png)

其他类型的文件也有类似的文件标志

![image-20201206120636624](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222909589-357265177.png)

执行修改后缀的.class文件

![image-20201206161410023](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222909283-3282595.png)

### 1.3.3. Class文件版本号

### 1.3.3. Class文件版本号

• 紧接着魔数的4个字节存储的是 Class 文件的版本号。同样也是4个字节。第5个和第6个字节所代表的含义就是编译的副版本号 minor_version，而第7个和第8个字节就是编译的主版本号 major_version。（下图为Java字节码总的结构表）

![image-20201206161938775](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222909017-1501542089.png)

• 它们共同构成了 Class 文件的格式版本号。譬如某个 Class 文件的主版本号为 M，服版本号为 m，那么这个 Class 文件的格式版本号就确定为 M.m

• 版本号和 Java 编译器的对应关系如下表：（每增加一个JDK版本，主版本就增加一个数值，副版本后面都是0）

| 主版本（十进制） | 副版本（十进制） | 编译器版本 |
| :--------------- | :--------------- | :--------- |
| 45               | 3                | 1.1        |
| 46               | 0                | 1.2        |
| 47               | 0                | 1.3        |
| 48               | 0                | 1.4        |
| 49               | 0                | 1.5        |
| 50               | 0                | 1.6        |
| 51               | 0                | 1.7        |
| 52               | 0                | 1.8        |
| 53               | 0                | 1.9        |
| 54               | 0                | 1.10       |
| 55               | 0                | 1.11       |

回到Demo.class文件，主版本的16进制=34，转为十进制就是52，所以编辑器版本就是1.8

![image-20201206164521842](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222908731-1613230057.png)

• Java 的版本号是从45开始的，JDK 1.1 之后的每个 JDK 大版本发布主版本号向上加1。

• **不同版本的 Java 编译器编译的 Class 文件对应的版本是不一样的。目前，高版本的 Java 虚拟机可以执行由低版本编译器生成的 Class 文件，但是低版本的 Java 虚拟机不能执行由高版本编译器生成的 Class 文件。否则 JVM 会抛出`java.lang.UnsupportedClassVersionError` 异常**。(向下兼容)

![image-20201206165309740](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222908437-1390527199.png)

• 在实际应用中，由于开发环境和生产环境的不同，可能会导致该问题的发生。因此，需要我们在开发时，特别注意开发编译的 JDK 版本和生产环境的 JDK 版本是否一致

• 虚拟机 JDK 版本为 1.k （k >= 2）时，对应的 Class 文件格式版本号的范围为 45.0 ~ 44 + k.0（含两端）。比如JDK11，那么k = 11，1.k = 1.11，44 + k.0 = 44 + 11 = 55，就是45到55的版本JDK11都支持，最低版本就是45，也就是向下兼容所有的版本。

### 1.3.4. 常量池：存放所有常量

• 常量池是 Class 文件中内容最为丰富的区域之一。常量池对于 Class 文件中的字段和方法解析也有着至关重要的作用。

• 随着 Java 虚拟机的不断发展，常量池的内容也日渐丰富，可以说，`常量池是整个 Class 文件的基石`。

（`常量池可以看做class文件的资源仓库`，比如汽车修理厂的汽车配件的仓库。）

![image-20201206170554207](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222908082-1263979881.png)

• 在版本号之后，紧跟着的是常量池的数量，以及若干个常量池表项。

• 常量池中常量的数量是不固定的，所以在常量池的入口需要放置一项 u2 类型的无符号数，代表常量池容量计数值(constant_pool_count)。与 Java 中语言习惯不一样的是，这个容量计数是从1而不是0开始的。

![image-20201206170816606](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222907792-805614572.png)

由上表可见，Class 文件使用了一个前置的容量计数器(constant_pool_count)加若干个连续的数据项(constant_pool)的形式来描述常量池内容，我们把这一系列连续常量池数据称为常量池集合。

- **常量池表项**中，用于存放编译时期生成的各种**字面量**和**符号引用**，这部分内容将在类加载后进入方法区的**运行时常量池**中存放。（JDK7把字符串常量池、静态变量放到堆中(注意，变量指向的对象一直在堆中)，其他内容都在方法区的运行时常量池，JDK8的方法区在元空间中。）

#### 1.3.4.1. 常量池计数器

**constant_pool_count（常量池计数器）**

• 由于常量池的数量不固定，时长时短，所以需要放置两个字节来表示常量池容量计数值。

• 常量池容量计数值(u2 类型)：**从1开始**，表示常量池中有多少项常量。即 constant_pool_count = 1 表示常量池中有0个常量项。（会有相差1的关系）

• Demo.class 的值为：

![image-20201206173011511](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222907522-429975983.png)

其值为0x0016，这是16进制，转成10进制就是22。

需要注意的是，这实际上只有21项常量。索引为范围是1-21。为什么呢？

> 通常我们写代码时都是从0开始的，但是这里的常量池却是从1开始，因为它把第0项常量空出来了。这是为了满足后面`某些指向常量池的索引值的数据在特定情况下需要表达“不引用任何一个常量池项目”的含义，这种情况可用索引值0来表示。`

#### 1.3.4.2. 常量池表

• constant_pool 是一种表结构，以 1 ~ constant_pool_count - 1 为索引。表明了后面有多少个常量项。

• 常量池主要存放两大类常量：**`字面量(Literal)`**和**`符号引用(Symbolic References)`**

• 它包含了 Class 文件结构及其子结构中引用的所有字符串常量、类或接口名、字段名和其他常量。常量池中的每一项都具备相同的特征。`第1个字节作为类型标记，用于确定该项的格式，这个字节称为 tag byte(标记字节、标签字节)`。（常量池表中可能是下面表格中的任意一个类型，标志15、16、18是JDK7加进来的。）

| 类型                             | 标志(或标识) | 描述                   |
| :------------------------------- | :----------- | :--------------------- |
| CONSTANT_utf8_info               | 1            | UTF-8编码的字符串      |
| CONSTANT_Integer_info            | 3            | 整型字面量             |
| CONSTANT_Float_info              | 4            | 浮点型字面量           |
| CONSTANT_Long_info               | 5            | 长整型字面量           |
| CONSTANT_Double_info             | 6            | 双精度浮点型字面量     |
| CONSTANT_Class_info              | 7            | 类或接口的符号引用     |
| CONSTANT_String_info             | 8            | 字符串类型字面量       |
| CONSTANT_Fieldref_info           | 9            | 字段的符号引用         |
| CONSTANT_Methodref_info          | 10           | 类中方法的符号引用     |
| CONSTANT_InterfaceMethodref_info | 11           | 接口中方法的符号引用   |
| CONSTANT_NameAndType_info        | 12           | 字段或方法的符号引用   |
| CONSTANT_MethodHandle_info       | 15           | 表示方法句柄           |
| CONSTANT_MethodType_info         | 16           | 标志方法类型           |
| CONSTANT_InvokeDynamic_info      | 18           | 表示一个动态方法调用点 |

##### 1.3.4.2.1. 字面量和符号引用

在对这些常量解读前，需要搞清楚几个概念

常量池主要存放两大类常量：字面量(Literal)和符号引用(Symbolic References)。如下表：

![image-20201206190548879](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222907251-116900497.png)

**1.1 字面量**

```java
String str = "hello";  // 文本字符串
final int NUM = 10;   // 声明为final的常量值
 
// 这都是字面量
```

**1.2 全限定名**（符号引用：类和接口的全限定名）

**com/atguigu/test/Demo 这个就是类的全限定名**，仅仅是把包的"."替换成"/"，为了使连续的多个全限定名之间不产生混淆，在使用时最后一般会加入一个";"表示全限定名结束

**1.3 简单名称**（符号引用：字段、方法的名称）

简单名称是指没有类型和参数修饰的方法或者字段名称，例子中的类的 add() 方法和 num 字段的简单名称分别是 add 和 num。

![image-20201206203529961](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222906988-437873507.png)

**1.4 描述符**（符号引用：字段、方法的描述符）

**`描述符的作用是用来描述字段的数据类型、方法的参数列表(包括数量、类型以及顺序)和返回值。`**根据描述符规则，基本数据类型（byte、char、double、float、int、long、short、boolean）以及代表无返回值的 void 类型都用一个大写字符来表示，而对象类型则用字符 L 加对象的全限定名表示，详见下表：（数据类型：① 基本数据类型 ② 引用数据类型）

###### 1.3.4.2.1.1. 类型描述符

| 标志符 | 含义                                                         |
| :----- | :----------------------------------------------------------- |
| B      | 基本数据类型byte                                             |
| C      | 基本数据类型char                                             |
| D      | 基本数据类型double                                           |
| F      | 基本数据类型float                                            |
| I      | 基本数据类型int                                              |
| J      | 基本数据类型long（L被对象类型使用了）                        |
| S      | 基本数据类型short                                            |
| Z      | 基本数据类型boolean（B被byte使用了）                         |
| V      | 代表void类型                                                 |
| L      | 对象类型，比如：`Ljava/lang/Object;`                         |
| [      | 数组类型，代表一维数组；[[代表二维；[[[代表三维。比如：`double[][][] is [[[D` |

用描述符来描述方法时，按照先参数列表，后返回值的顺序描述，参数列表按照参数的严格顺序放在一组小括号"()"之内，如方法 `java.lang.String toString()`的描述符为 `() Ljava/lang/String;`（java.lang.String是返回值），方法 `int abc(int[] x ,int y)`描述符为`([II) I`。

例子：

```java
public class _05_ArrayTest {
    public static void main(String[] args) {
        Object[] arr = new Object[10];
        System.out.println(arr);  // [Ljava.lang.Object;@677327b6
 
        String[] arr1 = new String[10];
        System.out.println(arr1);  // [Ljava.lang.String;@14ae5a5
 
        long[][] arr2 = new long[10][];
        System.out.println(arr2);  // [[J@7f31245a 
    }
}
```

运行

```java
[Ljava.lang.Object;@677327b6    // 表示一维引用类型的数组。[是以为数组、L是对象类型、java.lang.Object;是全限定名，分号结束
[Ljava.lang.String;@14ae5a5    // 
[[J@7f31245a    // J是long类型
```

补充说明：

虚拟机在加载 Class 文件时才会进行动态链接，也就是说，Class 文件中不会保存各个方法和字段的最终内存布局信息，因此，这些字段和方法的符号引用不经过转换是无法直接被虚拟机使用的。**`当虚拟机运行时，需要从常量池中获得对应的符号引用，再在类加载过程中的解析阶段将其替换为直接引用，并翻译到具体的内存地址中。`**（加载的时候才知道数据真正存放的地址在哪里，class文件中存放的只是符号引用，真正的地址要加载到内存才能知道。） 这里说明下符号引用和直接引用的区别与关联：

- 符号引用：符号引用以**一组符号**来描述所引用的目标，符号可以是任何形式的字面量，只要使用时能无歧义地定位到目标即可。**符号引用与虚拟机实现的内存布局无关**，引用的目标并不一定已经加载到内存中
- 直接引用：直接引用可以是直接**指向目标的指针、相对偏移量或是一个能间接定位到目标的句柄。直接引用是与虚拟机实现的内存布局相关的，**同一个符号引用在不同虚拟机实例上翻译出来的直接引用一般不会相同。如果有了直接引用，那说明引用的目标必定已经存在于内存之中了。（之前上篇理解错了，以为直接引用是在字节码层面上最后定位到的目标，原来是在虚拟机层面的）

动态链接一定是加载class文件到内存了。

![image-20201206210808147](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222906663-751519419.png)

加载class的过程

![image-20201206211109827](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222906313-664900758.png)

![image-20220421165056377](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222906007-991684503.png)

##### 1.3.4.2.2. 常量类型和结构

常量池中每一项常量都是一个表，JDK 1.7 之后共14种不同的表结构数据。如下表格所示：

![image-20201206212808528](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222905662-306385339.png)

根据上图每个类型的描述我们也可以知道每个类型是用来描述常量池中哪些内容（主要是字面量、符号引用）的。比如： CONSTANT_Integer_info 是用来描述常量池中字面量信息的，而且只是整型字面量信息。

标志为15、16、18的常量项类型是用来支持动态语言调用的（JDK 1.7时才加入）。

**细节说明：**

- **CONSTANT_Class_info 结构用于表示类或接口**
- **CONSTANT_Fieldref_info、CONSTANT_Methodref_info 和 CONSTANT_InterfaceMethodref_info 结构表示字段、方法和接口方法**
- **CONSTANT_String_info 结构用于表示 String 类型的常量对象**
- **CONSTANT_Integer_info 和 CONSTANT_Float_info 表示4字节(int 和 float)的数值常量**
- CONSTANT_Long_info 和 CONSTANT_Double_info 结构表示8字节(long 和 double)的数值常量
  - **在 Class 文件的常量池表中，所有的8字节常量均占两个表成员(项)的空间，如果一个 CONSTANT_Long_info 或 CONSTANT_Double_info 结构的项在常量池表中的索引位 n，则常量池表中下一个可用项的索引位 n + 2，此时常量池表中索引为 n + 1，的项仍然有效但必须视为不可用的**
- **CONSTANT_NameAndType_info 结构用于表示字段或方法，但是和之前的3个结构不同，CONSTANT_NameAndType_info 结构没有指明该字段或方法所属的类或接口**
- **CONSTANT_Utf8_info 用于表示字符常量的值**
- **CONSTANT_MethodHandle_info 结构用于表示方法句柄**
- **CONSTANT_MethodType_info 结构表示方法类型**
- **CONSTANT_InvokeDynamic_info 结构用于表示 invokedynamic 指令所用到的引导方法(bootstrap method)、引导方法所用到的动态调用名称(dynamic invocation name)、参数和返回类型，并可以给引导方法传入一系列称为静态参数(static argument)的常量**

**解析方式:**

- 一个字节一个字节的解析

![image-20201213174831436](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222905317-902552272.png)

- 使用 javap 命令解析：javap -verbose _04_Demo.class 或 jclasslib 工具会更方便。

###### 1.3.4.2.2.1. 常量池结构解读

先解读常量池每一项是什么类型

常量池第01项，类型是CONSTANT_Methodref_info，表示类中方法的符号引用

![image-20201206235637152](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222904802-1931776793.png)

常量池第02项，类型是CONSTANT_Fieldref_info，表示字段的符号引用

![image-20201207224943843](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222904136-1797372820.png)

常量池第03项，类型是CONSTANT_Class_info，表示类或接口的符号引用

![image-20201207225208533](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222903479-502880781.png)

常量池04，同理

![image-20201207225608413](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222902798-222186999.png)

常量池05，类型是CONSTANT_utf8_info，表示UTF-8编码的字符串

![image-20201207230444049](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222902181-990257650.png)

常量池06

![image-20201207230756653](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222901540-1268568714.png)

后面都是字符串，就字符串长度不同，直到常量池第18项才不是字符串

![image-20201207230959395](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222901019-933720441.png)

常量池18，类型是CONSTANT_NameAndType_info，表示字段或方法的符号引用

![image-20201207231300481](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222900567-1058436782.png)

常量池19跟18是同一个类型，后面20、21都是字符串，常量池长度是22，共22-1项，所以21项结束就是常量池的解读结束了。

![image-20201207231428939](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222900070-2045132990.png)

用IDEA的jclasslib看一下

![image-20201207231849448](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222859755-1995875710.png)

抽一个看看，常量池第01项

![image-20201207232245019](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222859226-465550602.png)

###### 1.3.4.2.2.2. 常量池细节解读

前面已经把常量池的每个项是什么类型已经解读了，现在开始深入解读每个类型具体的含义。由于其他类型最终都指向字符串类型，所以优先解读全部字符串，这样子更方便后面解读其他类型。

常量池第05项的内容为字符串“num”

![image-20201208210829976](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222858538-1992424481.png)

常量池06项的内容为字符串为“I”（大写的i）

![image-20201208211541385](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222858014-2002975368.png)

其他依次类推，最后解读出来所有的字符串信息

![image-20201213103345284](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222857493-999803062.png)

换成常量池的字符串的对应关系来看

![image-20201213103726682](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222856982-285877522.png)

现在开始具体解读常量池中除了字符串类型之外的其他类型。

先解读01项。01指向04、18，

![image-20201213111304691](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222856472-1675586342.png)

解读04、18项。04指向21（java/lang/Object）；18两个两个指向，一个指向07（即 init），一个指向08（即()V）

![image-20201213112947486](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222855582-1284127178.png)

换一种角度来看01指向04、18，04指向21，18指向07和08。所以01的意思是01表示一个方法，一个空参空返回值的构造方法，该方法由java/lang/Object来定义的。

![image-20201213115020465](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222847491-1167498631.png)

对应的代码段

![image-20201213165510105](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222854461-895775441.png)

补充V的含义：

![image-20201213172130824](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222854190-1353896963.png)

现在开始解读02。

![image-20201213143835652](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222853725-708629007.png)

所以02的意思是02表示一个名为num、类型为int的字段，该字段是_10/_04_Demo类定义的。

![image-20201213144526077](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222848232-1525653468.png)

对应的代码段

![image-20201213165546804](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222853003-1531300179.png)

补充大写i的含义

![image-20201213172159896](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222852708-281191016.png)

至此，常量池的21项都解读完了。

对比一下IDEA的jclasslib插件的解读结果（结果是一样的）：

![image-20201213171828804](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222852175-845384051.png)

###### 1.3.4.2.2.3. 小结：

![image-20201213173851389](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222851591-690961086.png)

- 这14种表(或者常量项结构)的共同点是：表开始的第一位是一个 u1 类型的标志位(tag)，代表当前这个常量项使用的是哪种表结构，即哪种常量类型。
- 在常量池列表中，CONSTANT_Utf8_info 常量项是一种使用改进过的 UTF-8 编码格式来存储诸如文字字符串、类或者接口的全限定名、字段或者方法的简单名称以及描述符等常量字符串信息。
- 这14种常量项结构还有一个特点是，其中13个常量项占用的字节固定，只有 CONSTANT_Utf8_info 占用字节不固定，其大小由 length 决定。为什么呢？**`因为从常量池存放的内容可知，其存放的是字面量和符号引用，最终这些内容都会是一个字符串，这些字符串的大小是在编写程序时才确定，`**比如你定义一个类，类名可以取长去短，所以在没编译前，大小不固定，编译后，通过 UTF-8 编码，就可以知道其长度。

补充：

final修饰就是常量了，就会出现在常量池中，这时候就能看到Integer类型了

![image-20201213174134892](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222851232-1674553939.png)

#### 1.3.4.3. 总结

- 常量池：可以理解为 Class 文件之中的资源仓库，它是 Class 文件结构中与其他项目关联最多的数据类型(后面的很多数据类型都会指向此处)，也是占用 Class 文件空间最大的数据项目之一。
- 常量池中为什么包含这些内容？

> Java 代码在进行 javac 编译的时候，并不像 C 和 C++ 那样有“连接”这一步骤，而是在虚拟机加载 Class 文件的时候进行动态链接。也就是说，**`在 Class 文件中不会保存各个方法、字段的最终内存布局信息，因此这些字段、方法的符号引用不经过运行期转换的话无法得到真正的内存入口地址，也就无法直接被虚拟机使用。`**当虚拟机运行时，需要从常量池获得对应的符号引用，再在类创建时或运行时解析、翻译到具体的内存地址之中。关于类的创建和动态链接内容，在虚拟机类加载过程中再进行详细解释。

### 1.3.5. 访问标识

访问标识（access_flag）又叫访问标志、访问标记。

• 在常量池后，紧跟着访问标记。**该标记使用两个字节表示**，用于识别一些类或者接口层次的访问信息，包括：这个 Class 是类还是接口；是否定义为 public 类型；是否定义为 abstract 类型；如果是类的话，是否被声明为 final 等。各种访问标记如下所示（官网翻译过来的）：

| Flag Name(标志名称) | Value(标志值) | Interpretation(含义)                                         |
| :-----------------: | :------------ | :----------------------------------------------------------- |
|     ACC_PUBLIC      | 0x0001        | 标志为public类型                                             |
|      ACC_FINAL      | 0x0010        | 标志被声明为final，只有类可以设置                            |
|      ACC_SUPER      | 0x0020        | 标志允许使用invokespecial字节码指令的新语义，JDK1.0.2之后编译出来的类的这个标志默认为真。（使用增强的方法调用父类方法）（任何一个Java类都有这个标识） |
|    ACC_INTERFACE    | 0x0200        | 标志这是一个接口                                             |
|    ACC_ABSTRACT     | 0x0400        | 是否为abstract类型，对于接口或者抽象类来说，此标志值为真，其他类型为假 |
|    ACC_SYNTHETIC    | 0x1000        | 标志此类并非由用户代码产生（即：由编译器产生的类，没有源码对应） |
|   ACC_ANNOTATION    | 0x2000        | 标志这是一个注解                                             |
|      ACC_ENUM       | 0x4000        | 标志这是一个枚举                                             |

• 类的访问权限通常为 ACC_ 开头的常量

• 每一种类型的表示都是通过设置访问标记的32位中的特定位来实现的。比如，若是 public final 的类，则该标记为 ACC_PUBLIC | ACC_FINAL。

• 使用 ACC_SUPER 可以让类更准确地定位到父类的方法 super.method()，现代编译器都会设置并且使用这个标记。

对应的官方规范表

![image-20201219204834938](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222850845-1840921008.png)

继续解读Demo类的字节码

![image-20201219205850282](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222850574-1896029554.png)

访问标识占2个字节，0x0021 = 0x0001 + 0x0020，所以Demo类是public修饰的

![image-20201219210251609](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222850141-776413358.png)

对照一个源码

![image-20201219210529353](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222849740-845968044.png)

在jclasslib上也能验证

![image-20201219212352666](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222849451-1160895994.png)

**补充说明：**

1. 带有 ACC_INTERFACE 标志的 Class 文件表示的是接口而不是类，反之则表示的是类而不是接口。

   - **1）如果一个 Class 文件被设置了 ACC_INTERFACE 标志，那么同时也得设置 ACC_ABSTRACT 标志。同时它不能再设置 ACC_FINAL、ACC_SUPER 或 ACC_ENUM 标志。**
   - **2）如果没有设置 ACC_INTERFACE 标志，那么这个 Class文件可以具有上表中除 ACC_ANNOTATION 外的其他所有标志。当然，ACC_FINAL 和 ACC_ABSTRACT 这类互斥的标志除外。这两个标志不能同时设置。**

2. ACC_SUPER 标志用于确定类或接口里面的 invokespecial 指令使用的是哪一种执行语义。

   针对 Java 虚拟机指令集的编译器都应当设置这个标志。

   对于 Java SE 8 及后续版本来说，无论 Class 文件中这个标志的实际值是什么，也不管 Class 文件的版本号是多少，JVM为每个Class文件都设置了ACC_SUPER标志。

   - **1）ACC_SUPER 标志是为了向后兼容的由旧的Java编译器所编译的代码而设计的。目前的ACC_SUPER标志是由JDK1.0.2 之前的编译器生成的 ，access_flags 中是没有确定含义的，如果设置了该标志，那么 Oracle 的Java虚拟机实现会将其忽略。**

3. ACC_SYNTHETIC 标志意味着该类或者该接口是由编译器生成的，而不是由源代码生成的。

4. 注解类型必须设置 ACC_ANNOTATION 标志。如果设置了 ACC_ANNOTATION 标志，那么也必须设置 ACC_INTERFACE 标志。

5. ACC_ENUM 标志标明该类或其父类为枚举类型。

6. 表中没有使用的 access_flags 标志是为未来扩充而预留的，这些预留的标志在编译器中应该设置为0，Java 虚拟机实现也应该忽略他们

看下注解的标识

![image-20201219211806523](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222849004-1268182372.png)

### 1.3.6. 类索引、父类索引、接口索引集合

• 在访问标记后，会指定该类的类别、父类类别以及实现的接口，格式如下：

| 长度 | 含义                                                         |
| :--- | :----------------------------------------------------------- |
| u2   | this_class（类索引）                                         |
| u2   | super_class（父类索引。java是单继承，所以父类只有一个）      |
| u2   | interfaces_count（接口计数器。接口可以多个，所以接口是表结构） |
| u2   | interfaces[interfaces_count]（接口索引集合）                 |

• 这三项数据来确定这个类的继承关系：

- 类索引用于确定这个类的全限定名。
- 父类索引用于确定这个类的父类的全限定名。由于 Java 语言不允许多重继承，所以父类索引只有一个，除了 java.lang.Object 之外，所有的 Java 类都有父类，因此除了 java.lang.Object 外，所有 Java 类的父类索引都不为0。
- 接口索引集合就用来描述这个类实现了哪些接口，这些被实现的接口将按 implements 语句(如果这个类本身是一个接口，则应当是 extends 语句)后的接口顺序从左到右排列在接口索引集合中。

1、this_class （类索引）

- 2个字节无符号整数，指向常量池的索引。它提供了类的全限定名，如 com/atguigu/java/Demo。this_class 的值必须是对常量池中的某项的一个有效的索引值。常量池在这个索引处的成员必须是 CONSTANT_Class_info 类结构体，该结构体表示这个class文件所定义的类或者接口。

继续解读Demo类的class文件

![image-20201220104035186](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222848597-258207988.png)

前面解读过的常量池03项的内容，就是_10/_04_Demo，即当前类是全限定名的Demo类

![image-20201213144526077](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222848232-1525653468.png)

2、super_class (父类索引)

- 2个字节无符号整数，指向常量池的索引，它提供了当前类的父类全限定名。如果没有继承任何类，其默认继承的就是java/lang/Object 类，同时，由于Java不支持多继承，所以其父类只有一个。
- super_class 指向的父类不能为final。（final修饰的类是不可以被继承的）

继续解读Demo类的class文件（如果当前类是Object类，那么父类索引就是0000，因为Object没有父类）

![image-20201220104409993](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222847882-1956465498.png)

之前解读过的常量池04项，所以Demo类的父类是Object

![image-20201213115020465](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222847491-1167498631.png)

对照下源码，没有继承任何父类，那么父类就是Object

![image-20201220104605517](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222846926-626077544.png)

3、interfaces

- 指向常量池索引集合，它提供了一个符号引用到所有已经实现的接口
- 由于一个类可以实现多个接口，因此需要以数组形式保存多个接口的索引，表示接口的每个索引也是一个指向常量池的CONSTANT_Class （当然这里就必须是接口，不能是类）
- 3.1、interfaces_count （接口计数器）
  - interfaces_count 项的值表示当前类或者接口的直接接口的数量。
- 3.2、interfaces[interfaces_count]（接口索引集合）
  - interfaces [] 中每个成员的值必须是常量池表中某项有效的索引值，它的长度为 interfaces_count。每个成员interfaces[i] 必须为CONSTANT_Class_info 结构，其中 0<=i < interfaces_count。
  - 在interfaces[] 中，各个成员所表示的接口顺序和对应的源代码中给定的接口顺序是一致的（从左到右），也就是说 interfaces[0] 对应的是源代码中最左边的接口。

继续解读Demo类的class文件（0000表示没有实现接口，也就没有接口索引集合）

![image-20201220105410041](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222846569-1466782864.png)

对照看下jclasslib的反编译

![image-20201220105627560](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222846137-44494575.png)

### 1.3.7. 字段表集合(Fields)

**filed的提前了解**

- 用于描述接口或类中声明的变量。字段(field)包括`类级变量以及实例级变量`，但是不包括方法内部、代码块内部声明的局部变量。
- 字段叫什么名字、字段被定义为什么数据类型，这些都是无法固定的，只能引用常量池中的常量来描述。
- 它指向常量池索引集合，它描述了每个字段的完整信息。比如`字段的标识符、访问修饰符（public、private或protected)、是类变量还是实例变量(static修饰符）、是否是常量(final修饰符）等`。

注意事项

![image-20220421211553461](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222845706-377362222.png)

**fields_count （字段计数器）**

- fields_count 的值表示当前class文件fields表的成员个数。使用2个字节表示。
- fields表中的每一个成员都是一个 field_info 结构，用于表示该类或者接口所声明的所有类或者实例字段，不包括方法内部声明的变量，也不包括从父类或者父接口继承的那些字段。

继续解析Demo类的class文件，0x0001表示只有一个字段

![image-20201220112748453](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222845277-1684163513.png)

对照源码

![image-20201220111559119](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222844907-460598562.png)

#### 1.3.7.2. 字段表

**fields[] 字段表**

- fields 表中的每个成员都必须是一个 fields_info 结构的数据项，用于表示当前类或者接口中某个字段的完整描述。

- 一个字段的信息包括如下这些信息。在这些信息中，

  各个修饰符都是布尔值，要么有，要么没有

  .

  - 作用域（public、private、proteced修饰符）
  - 是实例变量还是类变量（static修饰符）
  - 可变性（final）
  - 并发可见性（volatile修饰符，是否强制从主内存读写）
  - 可否序列化（transient 修饰符）
  - 字段数据类型（基本数据类型、对象、数组）
  - 字段名称

- 字段表结构

字段作为一个表，有其自己的结构：

| 类型           | 名称             | 含义       | 数量             |
| :------------- | :--------------- | :--------- | :--------------- |
| u2             | access_flags     | 访问标识   | 1                |
| u2             | name_index       | 字段名索引 | 1                |
| u2             | descriptor_index | 描述符索引 | 1                |
| u2             | attributes_count | 属性计数器 | 1                |
| attribute_info | attributes       | 属性集合   | attributes_count |

##### 1.3.7.2.1. 字段表访问标识

我们知道，一个字段可以被各种关键字去修饰，比如：作用域修饰符(public、private、protected)、static 修饰符、final 修饰符、volatile 修饰符等等。因此，其可像类的访问标志那样，使用一些标志来标记字段。字段的访问标志有如下这些：

| 标志名称      | 标志值 | 含义                       |
| :------------ | :----- | :------------------------- |
| ACC_PUBLIC    | 0x0001 | 字段是否为public           |
| ACC_PRIVATE   | 0x0002 | 字段是否为private          |
| ACC_PROTECTED | 0x0004 | 字段是否为protected        |
| ACC_STATIC    | 0x0008 | 字段是否为static           |
| ACC_FINAL     | 0x0010 | 字段是否为final            |
| ACC_VOLATILE  | 0x0040 | 字段是否为volatile         |
| ACC_TRANSTENT | 0x0080 | 字段是否为transient        |
| ACC_SYNCHETIC | 0x1000 | 字段是否为由编译器自动产生 |
| ACC_ENUM      | 0x4000 | 字段是否为enum             |

继续解析Demo类的class文件

![image-20201220113328536](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222844519-854565951.png)

02表示字段是private

![image-20201220113439952](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222844242-2112080434.png)

对应源码

![image-20201220113509813](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222843825-712603446.png)

##### 1.3.7.2.2. 字段名索引

继续解析Demo类的class文件

![image-20201220113829746](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222843498-1683920530.png)

![image-20201220114026747](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222843048-1630989423.png)

##### 1.3.7.2.3. 字段名描述符索引

描述符的作用是用来描述字段的数据类型、方法的参数列表(包括数量、类型以及顺序)和返回值。根据描述符规则，基本数据类型(byte、char、double、float、int、long、short、boolean)及代表无返回值的 void 类型都用一个大写字符来表示，而对象则用字符L加对象的全限定名来表示，如下所示：（与类型描述符的区别是没有void）

| 标志符 | 类型      | 含义                                                     |
| :----- | :-------- | :------------------------------------------------------- |
| B      | byte      | 基本数据类型byte                                         |
| C      | char      | 基本数据类型char                                         |
| D      | double    | 基本数据类型double                                       |
| F      | float     | 基本数据类型float                                        |
| I      | int       | 基本数据类型int                                          |
| J      | long      | 基本数据类型long                                         |
| S      | short     | 基本数据类型short                                        |
| Z      | boolean   | 基本数据类型boolean                                      |
| L      | reference | 对象类型，比如：`Ljava/lang/Object;`                     |
| [      | reference | 数组类型，代表一维数组。多维比如：`double[][][] is [[[D` |

继续解析Demo类的class文件，所以字段类型是int

![image-20201220132701370](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222842516-1214645535.png)

![image-20201220132755371](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222841992-306325792.png)

##### 1.3.7.2.4. 属性表集合

`一个字段还可能拥有一些属性，用于存储更多的额外信息`。比如初始化值（常量才有初始化值这个属性，变量没有）、一些注释信息等。属性个数存放在 attribute_count 中，属性具体内容存放在 attributes 数组中

以常量属性为例，结构为：

```java
ConstantValue_attribute {
    u2 attribute_name_index;
    u4 attribute_length;
    u2 constantvalue_index;
}
```

说明：对于常量属性而言，**attribute_length 值固定为2**。

继续解析Demo类的class文件，字段属性计数器为0x0000，表示没有属性。

![image-20201220133716470](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222841721-1619180107.png)

补充例子：（常量字段的初始化值的属性）

![image-20201220145139707](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222841110-637675508.png)

小结：

整体看一下，jclasslib

![image-20201220144454626](https://www.cnblogs.com/chenguanqin/p/JVM(中).assets/image-20201220144454626.png)

### 1.3.8. 方法表集合

methods: 指向常量池索引集合，它完整描述了每个方法的签名。

- 在字节码文件中，**`每一个 method_info 项都对应着一个类或者接口中的方法信息。`**比如方法的访问修饰符(public、private 或 protected)，方法的返回值类型以及方法的参数信息等。
- 如果这个方法不是抽象的或者不是 native 的，那么字节码中会体现出来。
- 一方面，methods 表只描述当前类或接口中声明的方法，不包括从父类或父接口继承的方法。另一方面，methods 表有可能会出现由编译器自动添加的方法，最典型的便是编译器产生的方法信息（比如：类(接口)初始化方法`clinit()` 和实例初始化方法 `init()`）。

**使用注意事项：**

在 Java 语言中，要重载(Overload)一个方法，除了要与原方法具有相同的简单名称之外，还要求必须拥有一个与原方法不同的特征签名，`特征签名就是一个方法中各个参数在常量池中的字段符号引用的集合`，也就是因为返回值不会包含在特征签名之中，因此 Java 语言里无法仅仅依靠返回值的不同来对一个已有方法进行重载。

但在 Class 文件格式中，特征签名的范围更大一些，只要描述符不是完全一致的两个方法就可以共存。也就是说，**如果两个方法有相同的名称和特征签名，但返回值不同，那么也是可以合法共存于同一个 Class 文件中**。 也就是说，尽管 Java 语法规范并不允许在一个类或者接口中声明多个方法签名相同的方法，但是和 Java 语法规范相反，字节码文件中却恰恰允许存放多个方法签名相同的方法，唯一的条件就是这些方法之间的返回值不能相同。

![image-20201220150446155](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222840246-1864448571.png)

#### 1.3.8.1. 方法计数器

**methods_count(方法计数器)**

methods_count 的值表示当前 class 文件 methods 表的成员个数，使用两个字节来表示。

methods 表中每个成员都是一个 method_info 结构。

继续解析Demo类的class文件

![image-20201220151204967](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222839927-1806453983.png)

#### 1.3.8.2. 方法表

**methods[] （方法表）**

- methods 表中的每个成员都必须是一个 method_info 结构，用于表示当前类或接口中某个方法的完整描述。如果某个 method_info 结构的 access_flags 项既没有设置 ACC_NATIVE 标志也没有设置 ACC_ABSTRACT 标志，那么该结构中也应包含实现这个方法所有的 Java 虚拟机指令。（native、abstract修饰的方法都没有方法体）
- method_info 结构可以表示类和接口中定义的所有方法，包括实例方法、类方法、实例初始化方法和类或接口初始化方法。
- 方法表的结构实际跟字段表是一样的，方法表结构如下：

| 类型           | 名称             | 含义       | 数量             |
| :------------- | :--------------- | :--------- | :--------------- |
| u2             | access_flags     | 访问标识   | 1                |
| u2             | name_index       | 方法名索引 | 1                |
| u2             | descriptor_index | 描述符索引 | 1                |
| u2             | attributes_count | 属性计数器 | 1                |
| attribute_info | attributes       | 属性集合   | attributes_count |

##### 1.3.8.2.1. 方法表访问标识

跟字段表一样，方法表也有访问标志，而且他们的标志有部分相同，部分则不同，方法表的具体访问标志如下：

| 标志名称          | 标志值 | 含义                       |
| :---------------- | :----- | :------------------------- |
| ACC_PUBLIC        | 0x0001 | 方法是否为public           |
| ACC_PRIVATE       | 0x0002 | 方法是否为private          |
| ACC_PROTECTED     | 0x0004 | 方法是否为protected        |
| ACC_STATIC        | 0x0008 | 方法是否为static           |
| ACC_FINAL         | 0x0010 | 方法是否为final            |
| ACC_SYHCHRONRIZED | 0x0020 | 方法是否为synchronized     |
| ACC_BRIDGE        | 0x0040 | 方法是否由编译器产生的方法 |
| ACC_VARARGS       | 0x0080 | 方法是否接受参数           |
| ACC_NATIVE        | 0x0100 | 方法是否为native           |
| ACC_ABSTRACT      | 0x0400 | 方法是否为abstract         |
| ACC_STRICTFP      | 0x0800 | 方法是否为strictfp         |
| ACC_SYNTHETIC     | 0x1000 | 方法是否由编译器自动产生   |

继续解析Demo类的class文件

![image-20201220153021514](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222839522-576222980.png)

01表示public

![image-20201220153206613](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222839146-961216498.png)

对照源码

![image-20201220153224540](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222838882-444041528.png)

##### 1.3.8.2.2. 方法名索引

继续解读，指向07，即方法名，这是默认自动生成的空参构造方法

![image-20201220153506114](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222838574-2052519379.png)

反编译得到源码

![image-20201220154245431](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222838303-652853498.png)

##### 1.3.8.2.3. 方法描述符索引

继续解读，08对应空参数、无返回值的描述符，这是默认自动生成的空参构造方法的参数和返回值

![image-20201220153741680](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222837913-254029486.png)

反编译得到源码

![image-20201220154254514](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222837634-1648674294.png)

##### 1.3.8.2.4. 方法的属性计数器

继续解读，01表示该方法只有一个属性

![image-20201220154142744](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222837327-429299817.png)

提前预告一下，该构造方法的属性就叫Code

![image-20201220154749814](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222837027-1438434980.png)

先不解读构造方法的Code属性，下面先解读另一个方法（预告：add方法）

![image-20201220155846716](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222836624-1943778972.png)

该方法是public修饰，方法名为add，方法描述符为空参、返回值是int，一个属性Code

![image-20201220160655899](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222836255-1759669300.png)

![image-20201220160847311](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222835996-1162718434.png)

对照源码

![image-20201220160534710](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222835750-544263825.png)

方法的Code属性会在后面“Code属性”小节解读。

### 1.3.9. 属性表集合

**属性表集合（attributes）**

方法表集合之后的属性表集合（注意，这里属性表集合不是方法表中的Code属性，但属性类型介绍包括Code属性），**`指的是 class 文件所携带的辅助信息`**，比如该 class 文件的源文件的名称。以及任何带有 RetentionPolicy.CLASS 或者 RetentionPolicy.RUNTIME 的注解。这类信息通常被用于 Java 虚拟机的验证和运行，以及 Java 程序的调试，**一般无需深入了解**。

此外，字段表、方法表都可以有自己的属性表。用于描述某些场景专有的信息。

属性表集合的限制没有那么严格，不再要求各个属性表具有严格的顺序，并且只要不与已有的属性名重复，任何人实现的编译器都可以向属性表中写入自己定义的属性信息，但 Java 虚拟机运行时会忽略掉它不认识的属性。

#### 1.3.9.1. 属性计数器

**attributes_count(属性计数器)**

attributes_count 的值表示当前 class 文件属性表的成员个数。属性表中每一项都是一个 attribute_info 结构。

继续解读（本教程制作的时候是先解读方法里面的Code属性，后解读附加属性表的，先后顺序不影响）

![image-20201221080239074](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222835404-1593585618.png)

#### 1.3.9.2. 属性表

**attributes[] (属性表)**

属性表的每个项的值必须是 attribute_info 结构。属性表的结构比较灵活，各种不同的属性只要满足以下结构即可。

##### 1.3.9.2.1. 属性表的通用格式

| 类型 | 名称                 | 数量             | 含义       |
| :--- | :------------------- | :--------------- | :--------- |
| u2   | attribute_name_index | 1                | 属性名索引 |
| u4   | attribute_length     | 1                | 属性长度   |
| u1   | info                 | attribute_length | 属性表     |

即只需说明属性的名称以及占用位数的长度即可，属性表具体的结构可以去自定义。

继续解读（本教程制作的时候是先解读方法里面的Code属性，后解读附加属性表的，先后顺序不影响）

![image-20201221080716655](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222834886-1846026416.png)

可以看到，定位到SourceFile，所以后续解读在“SourceFile属性”小节解读。

#### 1.3.9.3. 属性类型

属性表实际上可以有很多类型，上面看到的 Code 属性只是其中一种， Java 8 里面定义了23种属性。

下面这些是虚拟机中预定义的属性：

![image-20201220162717153](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222834177-2023193296.png)

| 属性名称                             | 使用位置                 | 含义                                                         |
| :----------------------------------- | :----------------------- | :----------------------------------------------------------- |
| Code                                 | 方法表中                 | Java代码编译成的字节码指令(即：具体的方法逻辑字节码指令)     |
| ConstantValue                        | 字段表中                 | final关键字定义的常量值                                      |
| Deprecated                           | 类中、方法表中、字段表中 | 被声明为deprecated的方法和字段                               |
| Exceptions                           | 方法表中                 | 方法声明的异常                                               |
| LocalVariableTable                   | Code属性中               | 方法的局部变量描述                                           |
| LocalVariableTypeTable               | 类中                     | JDK1.5中新增的属性，它使用特征签名代替描述符，是为了引入泛型语法之后能描述泛型参数化类型而添加 |
| InnerClasses                         | 类中                     | 内部类列表                                                   |
| EnclosingMethod                      | 类中                     | 仅当一个类为局部类或者匿名类时，才能拥有这个属性，这个属性用于表示这个类所在的外围方法 |
| LineNumberTable                      | Code属性中               | Java源码的行号与字节码指令的对应关系                         |
| StackMapTable                        | Code属性中               | JDK1.6中新增的属性，供新的类型检查验证器(Type Checker)检查和处理目标方法的局部变量和操作数栈所需要的类型是否匹配 |
| Signature                            | 类中、方法表中、字段表中 | JDK1.5新增的属性，这个属性用于支持泛型情况下的方法签名，在Java语言中，任何类、接口、初始化方法或成员的泛型签名如果包含了类型变量(Type Variables)或参数类型(Parameterized Types),则Signature属性会为它记录泛型签名信息。由于Java的泛型采用擦除法实现，在为了避免类型信息被擦除后导致签名混乱，需要这个属性记录泛型中的相关信息 |
| SourceFile                           | 类中                     | 记录源文件名称                                               |
| SourceDebugExtension                 | 类中                     | JDK1.6中新增的属性，SourceDebugExtension用于存储额外的调试信息。如在进行JSP文件调试时，无法通过Java堆栈来定位到JSP文件的行号，JSR-45规范为这些非Java语言编写，却需要编译成字节码运行在Java虚拟机汇中的程序提供了一个进行调试的标准机制，使用SourceDebugExtension就可以存储这些调试信息。 |
| Synthetic                            | 类中、方法表中、字段表中 | 标识方法或字段为编译器自动产生的                             |
| RuntimeVisibleAnnotations            | 类中、方法表中、字段表中 | JDK1.5中新增的属性，为动态注解提供支持。RuntimeVisibleAnnotations属性，用于指明哪些注解是运行时(实际上运行时就是进行反射调用)可见的。 |
| RuntimeInvisibleAnnotations          | 类中、方法表中、字段表中 | JDK1.5中新增的属性，作用与RuntimeVisibleAnnotations相反用于指明哪些注解是运行时不可见的。 |
| RuntimeVisibleParameterAnnotations   | 方法表中                 | JDK1.5中新增的属性，作用与RuntimeVisibleAnnotations类似，只不过作用对象为方法的参数。 |
| RuntimeInvisibleParameterAnnotations | 方法表中                 | JDK1.5中新增的属性，作用与RuntimeInvisibleAnnotations类似，只不过作用对象为方法的参数。 |
| AnnotationDefault                    | 方法表中                 | JDK1.5中新增的属性，用于记录注解类元素的默认值               |
| BootstrapMethods                     | 类中                     | JDK1.7新增的属性，用于保存invokedynamic指令引用的引导方法限定符 |

或（查看官网）

![image-20201220162934245](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222833537-1295684073.png)

![image-20201220162951333](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222833260-46466207.png)

![image-20201220163003112](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222832937-2016423331.png)

##### 1.3.9.3.1. 部分属性详解

###### 1.3.9.3.1.1. ConstantValue 属性

ConstantValue 属性表示一个常量字段的值。位于 field_info 结构的属性表中。

```java
ConstantValue_attribute {
    u2 attribute_name_index;
    u4 attribute_length;
    u2 constantvalue_index; //字段值在常量池中的索引，常量池在该索引处的项给出该属性表示的常量值。(例如，值是long型的，在常量池中便是CONSTANT_Long)
}
```

###### 1.3.9.3.1.2. Deprecated 属性

Deprecated属性是在JDK1.1为了支持注解中的关键词@Deprecated而引入的。

```java
Deprecated_attribute {
    u2 attribute_name_index;
    u4 attribute_length;
}
```

###### 1.3.9.3.1.3. Code 属性

Code 属性就是存放方法体里面的代码，但是，并非所有方法表都有 Code 属性，像接口或者抽象方法，他们没有具体的方法体，因此也就不会有 Code 属性了。

Code 属性表的结构，如下：

| 类型           | 名称                   | 数量                   | 含义                     |
| :------------- | :--------------------- | :--------------------- | :----------------------- |
| u2             | attribute_name_index   | 1                      | 属性名索引               |
| u4             | attribute_length       | 1                      | 属性长度                 |
| u2             | max_stack              | 1                      | 操作数栈深度的最大值     |
| u2             | max_locals             | 1                      | 局部变量表所需的存储空间 |
| u4             | code_length            | 1                      | 字节码指令的长度         |
| u1             | code                   | code_length            | 存储字节码指令           |
| u2             | exception_table_length | 1                      | 异常表长度               |
| exception_info | exception_table        | exception_table_length | 异常表                   |
| u2             | attribute_count        | 1                      | 属性集合计数器           |
| attribute_info | attributes             | attribute_count        | 属性集合                 |

可以看到：Code 属性表的前两项跟属性表是一致的，即 Code 属性表遵循属性表的结构，后面那些则是他自定义的结构。

继续解读Demo类的class文件，属性名索引=Code、属性长度=56、操作数栈的最大深度=2、局部变量表的长度=1、字节码指令的长度=10。

![image-20201220203952606](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222832627-1898485120.png)

对照一下jclasslib的反编译（除了“字节码指令的长度=10”没有体现到，其他都一模一样）

![image-20201220205345853](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222832305-1615502343.png)

具体的字节码指令

![image-20201220205518466](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222832064-1705850541.png)

跳转到官网

![image-20201220205713883](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222831782-1846802167.png)

解读字节码的指令，第一个就是2a

![image-20201220205834786](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222831495-1623706317.png)

其他指令同理

![image-20201220210907176](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222831065-163221484.png)

接着继续往下解读。异常表的长度=0，所以异常表就没有了。构造方法的Code属性的属性有两个。

![image-20201220211243353](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222830609-1559424282.png)

对照一下jclasslib，预告一下是哪两个属性

![image-20201220211450131](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222830266-1497813552.png)

解读LineNumberTable 、LocalVariableTable

![image-20201220211735369](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222829484-568613392.png)

###### 1.3.9.3.1.4. InnerClasses 属性

为了方便说明特别定义一个表示类或接口的 Class 格式为 C。如果 C 的常量池中包含某个 CONSTANT_Class_info 成员，且这个成员所表示的类或接口不属于任何一个包，那么 C 的 ClassFile 结构的属性表中就必须含有对应的 InnerClasses 属性。InnerClasses 属性是在 JDK 1.1 中为了支持内部类和内部接口而引入的，位于 ClassFile 结构的属性表。

###### 1.3.9.3.1.5. LineNumberTable 属性

LineNumberTable 属性是可选变长属性，位于 Code 结构的属性表。

LineNumberTable 属性是**`用来描述 Java 源码行号与字节码行号之间的对应关系`**。这个属性可以用来在调试的时候定位代码执行的行数。

- **`start_pc，即字节码行号；line_number，即 Java 源代码行号。`**

在 Code 属性的属性表中，LineNumberTable 属性可以按照任意顺序出现，此外，多个 LineNumberTable 属性可以共同表示一个行号在源文件中表示的内容，即 LineNumberTable 属性不需要与源文件的行一一对应。

LineNumberTable 属性表结构：

```java
LineNumberTable_attribute {
    u2 attribute_name_index;
    u4 attribute_length;
    u2 line_number_table_length;
    {
        u2 start_pc;
        u2 line_number;
    } line_number_table[line_number_table_length];
}
```

继续解读

![image-20201220212427286](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222829101-1100203504.png)

对照一下jclasslib

![image-20201220212542532](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222828777-2114648937.png)

###### 1.3.9.3.1.6. LocalVariableTable 属性

LocalVariableTable 是可选变长属性，位于 Code 属性的属性表中。它被调试器**用于确定方法在执行过程中局部变量的信息。**在 Code 属性的属性中，LocalVariableTable 属性可以按照任意顺序出现。Code 属性中的每个局部变量最多只能有一个 LocalVariableTable 属性。

- **start_pc + length 表示这个变量在字节码中的生命周期起始和结束的偏移位置**
- **index 就是这个变量在局部变量表中的槽位(槽位可复用)**
- **name 就是变量名称**
- **Descriptor 表示局部变量类型描述**

LocalVariableTable 属性表结构：

```java
LocalVariableTable_attribute {
    u2 attribute_name_index;
    u4 attribute_length;
    u2 local_variable_table_length;
    {
        u2 start_pc;
        u2 length;
        u2 name_index;
        u2 descriptor_index;
        u2 index;
    } local_variable_table[local_variable_table_length];
}
```

继续解读

![image-20201220212923636](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222828348-2075009923.png)

对照jclasslib

![image-20201220213248704](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222828026-899415780.png)

到此为止，构造方法就解读完了，除了具体的字节码指令的具体含义还没有解读，下一章会解读。

add方法解读同理。

![image-20201221075425806](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222827717-1680013414.png)

![image-20201221075454566](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222827217-198333903.png)

![image-20201221075623711](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222826945-2130812049.png)

![image-20201221075634515](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222826630-721288722.png)

![image-20201221075654807](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222826382-653272917.png)

###### 1.3.9.3.1.7. Signature 属性

Signature 属性是可选的定长属性，位于 ClassFile，field_info 或 method_info 结构的属性表中。在 Java 语言中，任何类、接口、初始化方法或成员的泛型签名如果包含了类型变量(Type Variables)或参数化类型(Parameterized Types)，则 Signature 属性会为它记录泛型签名信息。

###### 1.3.9.3.1.8. SourceFile 属性

SourceFile 属性结构

```java
SourceFile_attribute {
    u2 attribute_name_index;
    u4 attribute_length;  // 该属性长度必须为2
    u2 sourcefile_index;
}
```

结构分析

| 类型 | 名称                 | 数量 | 含义         |
| :--- | :------------------- | :--- | :----------- |
| u2   | attribute_name_index | 1    | 属性名索引   |
| u4   | attribute_length     | 1    | 属性长度     |
| u2   | sourcefile_index     | 1    | 源码文件索引 |

可以看到，其长度总是固定的8个字节。

官网

![image-20201221081158502](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222826037-1878432429.png)

继续解读，承接“属性表”的解读

![image-20201221081852220](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222825679-1590122839.png)

至此就全部解读了，字节码指令的含义解读放到下一章。

###### 1.3.9.3.1.9. 其他属性

Java 虚拟机中预定义的属性有20多个，这里就不一一介绍了，通过上面几个属性的介绍，只要领会其精髓，其他属性的解读也是易如反掌。

### 1.3.10. 小结

本章主要介绍了 Class 文件的基本格式。

随着 Java 平台的不断发展，在将来，Class 文件的内容也一定会做进一步的扩充，但是其基本的格式和结构不会做重大调整。

从 Java 虚拟机的角度看，通过 Class 文件，可以让更多的计算机语言支持 Java 虚拟机平台。因此，Class 文件结构不仅仅是 Java 虚拟机的执行入口，更是 Java 生态圈的基础和核心。

![image-20201221082651094](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222825337-1028290464.png)

---

## 1.4. 使用javap指令解析Class文件

### 1.4.1. 解析字节码的作用

通过反编译生成的字节码文件，我们可以深入的了解 Java 代码的工作机制。但是，自己分析类文件结构太麻烦了，除了使用第三方的 jclasslib 工具之外，Oracle 官方也提供了工具：javap。

javap 是 JDK 自带的反解析工具。它的作用就是根据 Class 字节码文件，反解析出当前类对应的 Code 区(字节码指令)、局部变量表、异常表和代码行偏移量映射表、常量池等信息。

通过局部变量表，我们可以查看局部变量的作用域范围、所在槽位等信息，甚至可以看到槽位复用等信息。

### 1.4.2. javac -g操作

解析字节码文件得到的信息中，有些信息(如局部变量表、指令和代码行偏移量映射表、常量池中方法的参数名称等等)需要在使用 javac 编译成 Class 文件时，指定参数才能输出。

比如，直接 javac xx.java，就不会生成对应的**局部变量表等信息**，如果使用 javac -g xx.java 就可以生成所有相关信息。如果使用的 Eclipse 或 IDEA，则默认情况下，Eclipse、IDEA 在编译时会生成局部变量表、指令和代码行偏移量映射表等信息。

例子：

```java
public class _06_JavapTest {
    private int num;
    boolean flag;
    protected char gender;
    public String info;
 
    public static final int COUNTS = 1;
    static{
        String url = "www.atguigu.com";
    }
    {
        info = "java";
    }
    public _06_JavapTest(){
 
    }
    private _06_JavapTest(boolean flag){
        this.flag = flag;
    }
    private void methodPrivate(){
 
    }
    int getNum(int i){
        return num + i;
    }
    protected char showGender(){
        return gender;
    }
    public void showInfo(){
        int i = 10;
        System.out.println(info + i);
    }
}
 
```

![image-20201222075042202](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222825029-1030380179.png)

Notepad++安装Compare插件之后，选中一个文件，然后如下图操作即可比较当前文件和当前文件的前一个文件。

![image-20201222074424112](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222824759-321534333.png)

带-g的大小会大一些。

![image-20201222074556482](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222824445-2079684903.png)

-g主要就是多了局部变量表

![image-20201222074717387](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222824150-1669274358.png)

![image-20201222074804209](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222823766-189184754.png)

比较文本或者用Beyond Compare

![image-20201222074031544](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222823445-1942123200.png)

### 1.4.3. javap的用法

**javap 的用法格式：javap**

```
javap <options> <class>
```

其中，classes 就是要反编译的 class 文件。

在命令行中直接输入 javap 或 javap -help 可以看到 javap 的 `options 有如下选项`：

![image-20201222075605668](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222823166-1249715221.png)

这里重组一下：

```shell
  -help  --help  -?        输出此用法消息
  -version                 版本信息
  
  // -p常用
  -public                  仅显示公共类和成员
  -protected               显示受保护的/公共类和成员
  -p  -private             显示所有类和成员
  -package                 显示程序包/受保护的/公共类和成员 (默认)
  -sysinfo                 显示正在处理的类的系统信息 (路径, 大小, 日期, MD5 散列)
  -constants               显示最终常量
 
  // 常用
  -s                       输出内部类型签名(即描述符相关信息)
  -l                       输出行号和本地变量表
  -c                       对代码进行反汇编（即Code属性里面的指令)
  -v  -verbose             输出附加信息
  
  
  -classpath <path>        指定查找用户类文件的位置
  -cp <path>               指定查找用户类文件的位置
  -bootclasspath <path>    覆盖引导类文件的位置
      
  最全的输出用法是：javap -v -p  _06_JavapTest.class
```

### 1.4.4. 使用举例

同样是前面的 _06_JavapTest.java

```java
D:\Code\Java\JVMDetail\src\_10>javap -version _06_JavapTest.class
1.8.0_211
Compiled from "_06_JavapTest.java"
public class _10._06_JavapTest {
  boolean flag;
  protected char gender;
  public java.lang.String info;
  public static final int COUNTS;
  public _10._06_JavapTest();
  int getNum(int);
  protected char showGender();
  public void showInfo();
  static {};
}
 
// 不带class文件则直接输出版本信息
D:\Code\Java\JVMDetail\src\_10>javap -version
1.8.0_211
 
// 只有public的变量和方法
D:\Code\Java\JVMDetail\src\_10>javap -public _06_JavapTest.class
Compiled from "_06_JavapTest.java"
public class _10._06_JavapTest {
  public java.lang.String info;
  public static final int COUNTS;
  public _10._06_JavapTest();
  public void showInfo();
}
 
// protected、public都有
D:\Code\Java\JVMDetail\src\_10>javap -protected _06_JavapTest.class
Compiled from "_06_JavapTest.java"
public class _10._06_JavapTest {
  protected char gender;
  public java.lang.String info;
  public static final int COUNTS;
  public _10._06_JavapTest();
  protected char showGender();
  public void showInfo();
}
 
// private、protected、public都有
D:\Code\Java\JVMDetail\src\_10>javap -p _06_JavapTest.class
Compiled from "_06_JavapTest.java"
public class _10._06_JavapTest {
  private int num;
  boolean flag;
  protected char gender;
  public java.lang.String info;
  public static final int COUNTS;
  public _10._06_JavapTest();
  private _10._06_JavapTest(boolean);
  private void methodPrivate();
  int getNum(int);
  protected char showGender();
  public void showInfo();
  static {};
}
 
// package是不显示私有方法的
D:\Code\Java\JVMDetail\src\_10>javap -package _06_JavapTest.class
Compiled from "_06_JavapTest.java"
public class _10._06_JavapTest {
  boolean flag;
  protected char gender;
  public java.lang.String info;
  public static final int COUNTS;
  public _10._06_JavapTest();
  int getNum(int);
  protected char showGender();
  public void showInfo();
  static {};
}
 
D:\Code\Java\JVMDetail\src\_10>javap -sysinfo _06_JavapTest.class
// 这就是
Classfile /D:/Code/Java/JVMDetail/src/_10/_06_JavapTest.class
  Last modified 2020-12-22; size 1342 bytes
  MD5 checksum dc7d02adc8c75b0ec1d8e62032025206
      
  Compiled from "_06_JavapTest.java"
public class _10._06_JavapTest {
  boolean flag;
  protected char gender;
  public java.lang.String info;
  public static final int COUNTS;
  public _10._06_JavapTest();
  int getNum(int);
  protected char showGender();
  public void showInfo();
  static {};
}
 
D:\Code\Java\JVMDetail\src\_10>javap -constants _06_JavapTest.class
Compiled from "_06_JavapTest.java"
public class _10._06_JavapTest {
  boolean flag;
  protected char gender;
  public java.lang.String info;
  // 常量
  public static final int COUNTS = 1;
  public _10._06_JavapTest();
  int getNum(int);
  protected char showGender();
  public void showInfo();
  static {};
}
 
// 输出内部类型签名，主要是描述符的信息（不包含private）
D:\Code\Java\JVMDetail\src\_10>javap -s _06_JavapTest.class
Compiled from "_06_JavapTest.java"
public class _10._06_JavapTest {
  boolean flag;
    descriptor: Z  // boolean的描述符就是Z
  protected char gender;
    descriptor: C  // char的描述符就是C
  public java.lang.String info;
    descriptor: Ljava/lang/String;
  public static final int COUNTS;
    descriptor: I
  public _10._06_JavapTest();
    descriptor: ()V  // 参数、返回值的描述符
 
  int getNum(int);
    descriptor: (I)I
 
  protected char showGender();
    descriptor: ()C
 
  public void showInfo();
    descriptor: ()V
 
  static {};
    descriptor: ()V
}
 
D:\Code\Java\JVMDetail\src\_10>javap -s -p _06_JavapTest.class
Compiled from "_06_JavapTest.java"
public class _10._06_JavapTest {
  // 带-p就显示private的描述符
  private int num;
    descriptor: I
  boolean flag;
    descriptor: Z
  protected char gender;
    descriptor: C
  public java.lang.String info;
    descriptor: Ljava/lang/String;
  public static final int COUNTS;
    descriptor: I
  public _10._06_JavapTest();
    descriptor: ()V
 
  private _10._06_JavapTest(boolean);
    descriptor: (Z)V
 
  private void methodPrivate();
    descriptor: ()V
 
  int getNum(int);
    descriptor: (I)I
 
  protected char showGender();
    descriptor: ()C
 
  public void showInfo();
    descriptor: ()V
 
  static {};
    descriptor: ()V
}
 
// 输出行号和本地变量表信息
D:\Code\Java\JVMDetail\src\_10>javap -l _06_JavapTest.class
Compiled from "_06_JavapTest.java"
public class _10._06_JavapTest {
  boolean flag;
 
  protected char gender;
 
  public java.lang.String info;
 
  public static final int COUNTS;
 
  public _10._06_JavapTest();
    // 行号表
    LineNumberTable:
      line 16: 0
      line 14: 4
      line 18: 10
    // 本地变量表
    LocalVariableTable:
      Start  Length  Slot  Name   Signature
          0      11     0  this   L_10/_06_JavapTest;
 
  int getNum(int);
    LineNumberTable:
      line 26: 0
    LocalVariableTable:
      Start  Length  Slot  Name   Signature
          0       7     0  this   L_10/_06_JavapTest;
          0       7     1     i   I
 
  protected char showGender();
    LineNumberTable:
      line 29: 0
    LocalVariableTable:
      Start  Length  Slot  Name   Signature
          0       5     0  this   L_10/_06_JavapTest;
 
  public void showInfo();
    LineNumberTable:
      line 32: 0
      line 33: 3
      line 34: 30
    LocalVariableTable:
      Start  Length  Slot  Name   Signature
          0      31     0  this   L_10/_06_JavapTest;
          3      28     1     i   I
 
  static {};
    LineNumberTable:
      line 11: 0
      line 12: 3
    LocalVariableTable:
      Start  Length  Slot  Name   Signature
}
 
// 显示方法的Code属性
D:\Code\Java\JVMDetail\src\_10>javap -c _06_JavapTest.class
Compiled from "_06_JavapTest.java"
public class _10._06_JavapTest {
  boolean flag;
 
  protected char gender;
 
  public java.lang.String info;
 
  public static final int COUNTS;
 
  public _10._06_JavapTest();
    // Code属性就有反汇编的字节码指令
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: aload_0
       5: ldc           #2                  // String java
       7: putfield      #3                  // Field info:Ljava/lang/String;
      10: return
 
  int getNum(int);
    Code:
       0: aload_0
       1: getfield      #5                  // Field num:I
       4: iload_1
       5: iadd
       6: ireturn
 
  protected char showGender();
    Code:
       0: aload_0
       1: getfield      #6                  // Field gender:C
       4: ireturn
 
  public void showInfo();
    Code:
       0: bipush        10
       2: istore_1
       3: getstatic     #7                  // Field java/lang/System.out:Ljava/io/PrintStream;
       6: new           #8                  // class java/lang/StringBuilder
       9: dup
      10: invokespecial #9                  // Method java/lang/StringBuilder."<init>":()V
      13: aload_0
      14: getfield      #3                  // Field info:Ljava/lang/String;
      17: invokevirtual #10                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
      20: iload_1
      21: invokevirtual #11                 // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
      24: invokevirtual #12                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
      27: invokevirtual #13                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
      30: return
 
  static {};
    Code:
       0: ldc           #14                 // String www.atguigu.com
       2: astore_0
       3: return
}
 
 
```

比较-c、-v的区别

![image-20201222082349811](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222822909-1647261343.png)

-v有版本信息、常量池、描述符、局部变量等

![image-20201222082521479](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222822612-781099006.png)

-c主要是Code属性，-v也有Code属性

![image-20201222082626425](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222822335-1483241846.png)

所以最全的输出用法是：**`javap -v -p _06_JavapTest.class`**

![image-20201223074512132](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222822105-2089697593.png)

```java
Classfile /D:/Code/Java/JVMDetail/src/_10/_06_JavapTest.class    // 字节码文件所属的路径
  Last modified 2020-12-22; size 1342 bytes                      // 最后修改时间，字节码文件的大小
  MD5 checksum dc7d02adc8c75b0ec1d8e62032025206                  // MD5散列值
  Compiled from "_06_JavapTest.java"                             // 源文件的名称
public class _10._06_JavapTest
  minor version: 0                                               // 副版本
  major version: 52                                              // 主版本
  flags: ACC_PUBLIC, ACC_SUPER                                   // 类的访问标识
Constant pool:                                                   // 常量池
   #1 = Methodref          #16.#46        // java/lang/Object."<init>":()V
   #2 = String             #47            // java
   #3 = Fieldref           #15.#48        // _10/_06_JavapTest.info:Ljava/lang/String;
   #4 = Fieldref           #15.#49        // _10/_06_JavapTest.flag:Z
   #5 = Fieldref           #15.#50        // _10/_06_JavapTest.num:I
   #6 = Fieldref           #15.#51        // _10/_06_JavapTest.gender:C
   #7 = Fieldref           #52.#53        // java/lang/System.out:Ljava/io/PrintStream;
   #8 = Class              #54            // java/lang/StringBuilder
   #9 = Methodref          #8.#46         // java/lang/StringBuilder."<init>":()V
  #10 = Methodref          #8.#55         // java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
  #11 = Methodref          #8.#56         // java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
  #12 = Methodref          #8.#57         // java/lang/StringBuilder.toString:()Ljava/lang/String;
  #13 = Methodref          #58.#59        // java/io/PrintStream.println:(Ljava/lang/String;)V
  #14 = String             #60            // www.atguigu.com
  #15 = Class              #61            // _10/_06_JavapTest
  #16 = Class              #62            // java/lang/Object
  #17 = Utf8               num
  #18 = Utf8               I
  #19 = Utf8               flag
  #20 = Utf8               Z
  #21 = Utf8               gender
  #22 = Utf8               C
  #23 = Utf8               info
  #24 = Utf8               Ljava/lang/String;
  #25 = Utf8               COUNTS
  #26 = Utf8               ConstantValue
  #27 = Integer            1
  #28 = Utf8               <init>
  #29 = Utf8               ()V
  #30 = Utf8               Code
  #31 = Utf8               LineNumberTable
  #32 = Utf8               LocalVariableTable
  #33 = Utf8               this
  #34 = Utf8               L_10/_06_JavapTest;
  #35 = Utf8               (Z)V
  #36 = Utf8               methodPrivate
  #37 = Utf8               getNum
  #38 = Utf8               (I)I
  #39 = Utf8               i
  #40 = Utf8               showGender
  #41 = Utf8               ()C
  #42 = Utf8               showInfo
  #43 = Utf8               <clinit>
  #44 = Utf8               SourceFile
  #45 = Utf8               _06_JavapTest.java
  #46 = NameAndType        #28:#29        // "<init>":()V
  #47 = Utf8               java
  #48 = NameAndType        #23:#24        // info:Ljava/lang/String;
  #49 = NameAndType        #19:#20        // flag:Z
  #50 = NameAndType        #17:#18        // num:I
  #51 = NameAndType        #21:#22        // gender:C
  #52 = Class              #63            // java/lang/System
  #53 = NameAndType        #64:#65        // out:Ljava/io/PrintStream;
  #54 = Utf8               java/lang/StringBuilder
  #55 = NameAndType        #66:#67        // append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
  #56 = NameAndType        #66:#68        // append:(I)Ljava/lang/StringBuilder;
  #57 = NameAndType        #69:#70        // toString:()Ljava/lang/String;
  #58 = Class              #71            // java/io/PrintStream
  #59 = NameAndType        #72:#73        // println:(Ljava/lang/String;)V
  #60 = Utf8               www.atguigu.com
  #61 = Utf8               _10/_06_JavapTest
  #62 = Utf8               java/lang/Object
  #63 = Utf8               java/lang/System
  #64 = Utf8               out
  #65 = Utf8               Ljava/io/PrintStream;
  #66 = Utf8               append
  #67 = Utf8               (Ljava/lang/String;)Ljava/lang/StringBuilder;
  #68 = Utf8               (I)Ljava/lang/StringBuilder;
  #69 = Utf8               toString
  #70 = Utf8               ()Ljava/lang/String;
  #71 = Utf8               java/io/PrintStream
  #72 = Utf8               println
  #73 = Utf8               (Ljava/lang/String;)V
 
---------------------------------------- 字段表集合的信息 ---------------------------------------
  {
  private int num;                           // 字段名
    descriptor: I                            // 字段描述符：字段的类型
    flags: ACC_PRIVATE                       // 字段的访问标识
 
  boolean flag;
    descriptor: Z
    flags:
 
  protected char gender;
    descriptor: C
    flags: ACC_PROTECTED
 
  public java.lang.String info;
    descriptor: Ljava/lang/String;
    flags: ACC_PUBLIC
 
  public static final int COUNTS;
    descriptor: I
    flags: ACC_PUBLIC, ACC_STATIC, ACC_FINAL
    ConstantValue: int 1                      // 常量池字段的属性：ConstantValue
 
---------------------------------------- 方法表集合的信息 ---------------------------------------
  public _10._06_JavapTest();                // javap直接还原了构造方法，jclasslib看到的是<init>
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: aload_0
         5: ldc           #2                  // String java
         7: putfield      #3                  // Field info:Ljava/lang/String;
        10: return
      LineNumberTable:
        line 16: 0
        line 14: 4
        line 18: 10
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      11     0  this   L_10/_06_JavapTest;
 
  private _10._06_JavapTest(boolean);          // 有参数的构造器
    descriptor: (Z)V
    flags: ACC_PRIVATE
    Code:
      stack=2, locals=2, args_size=2
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: aload_0
         5: ldc           #2                  // String java
         7: putfield      #3                  // Field info:Ljava/lang/String;
        10: aload_0
        11: iload_1
        12: putfield      #4                  // Field flag:Z
        15: return
      LineNumberTable:
        line 19: 0
        line 14: 4
        line 20: 10
        line 21: 15
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      16     0  this   L_10/_06_JavapTest;
            0      16     1  flag   Z
 
  private void methodPrivate();
    descriptor: ()V
    flags: ACC_PRIVATE
    Code:
      stack=0, locals=1, args_size=1
         0: return
      LineNumberTable:
        line 24: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       1     0  this   L_10/_06_JavapTest;
 
  int getNum(int);
    descriptor: (I)I
    flags:
    Code:
      stack=2, locals=2, args_size=2          // 有形参的方法，args_size=2，this也是一个 参数
         0: aload_0
         1: getfield      #5                  // Field num:I
         4: iload_1
         5: iadd
         6: ireturn
      LineNumberTable:
        line 26: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       7     0  this   L_10/_06_JavapTest;
            0       7     1     i   I
 
  protected char showGender();
    descriptor: ()C
    flags: ACC_PROTECTED
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: getfield      #6                  // Field gender:C
         4: ireturn
      LineNumberTable:
        line 29: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   L_10/_06_JavapTest;
 
  public void showInfo();
    descriptor: ()V                          // 方法描述符：方法的形参列表、返回值类型
    flags: ACC_PUBLIC                        // 方法的访问标识
    Code:                                    // 方法的Code属性
      stack=3, locals=2, args_size=1         // stack：操作数栈的最大深度   locals：局部变量表的长度  args_size：方法接收参数的个数
  // 偏移量  操作码         操作数
         0: bipush        10
         2: istore_1
         3: getstatic     #7                  // Field java/lang/System.out:Ljava/io/PrintStream;
         6: new           #8                  // class java/lang/StringBuilder
         9: dup
        10: invokespecial #9                  // Method java/lang/StringBuilder."<init>":()V
        13: aload_0
        14: getfield      #3                  // Field info:Ljava/lang/String;
        17: invokevirtual #10                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
        20: iload_1
        21: invokevirtual #11                 // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
        24: invokevirtual #12                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
        27: invokevirtual #13                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        30: return
      LineNumberTable:                        // 行号表：指明字节码指令的偏移量与Java源程序中代码的行号的一一对应关系
        line 32: 0
        line 33: 3
        line 34: 30
      LocalVariableTable:                     // 局部变量表：描述内部局部变量的相关信息
        Start  Length  Slot  Name   Signature // Slot是局部变量表的槽位
            0      31     0  this   L_10/_06_JavapTest;
            3      28     1     i   I
 
  static {};                                 // javap直接还原了静态代码块，而jclasslib是<clinit>
    descriptor: ()V
    flags: ACC_STATIC
    Code:
      stack=1, locals=1, args_size=0
         0: ldc           #14                 // String www.atguigu.com
         2: astore_0
         3: return
      LineNumberTable:
        line 11: 0
        line 12: 3
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
}
SourceFile: "_06_JavapTest.java"             // 附加属性：指明当前字节码文件对应的源程序文件名
 
```

### 1.4.5. 总结

1、通过 javap 命令可以查看一个 Java 类反汇编得到的 Class 文件版本号、常量池、访问标识、变量表、指令代码行号表等信息。不显式类索引、父类索引、接口索引集合、()、()等结构

2、通过对前面的例子代码反汇编文件的简单分析，可以发现，一个方法的执行通常会涉及下面几块内存的操作：

（1）Java 栈中：局部变量表、操作数栈。

（2）Java 堆： 通过对象的地址引用去操作。

（3）常量池。

（4）其他如帧数据区、方法区的剩余部分等情况，测试中没有显示出来，这里说明一下。

![image-20201223082017376](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222818978-2132504790.png)

3、平常，我们比较关注的是 Java 类中每个方法的反汇编中的指令操作过程，这些指令都是顺序执行的，可以参考官方文档查看每个指令含义。

https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html

# 2. 字节码指令集与解析举例

这章主要是方法体的内容，即Code属性。

![image-20201224075745137](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222821477-1245600286.png)

---

## 2.1. 概述

- Java 字节码对于虚拟机，就好像汇编语言对于计算机，属于基本执行命令。
- Java 虚拟机的指令由一个字节长度的、代表着某种特定操作含义的数字(称为**`操作码，Opcode`**)以及跟随其后的零至多个代表此操作所需参数(称为**`操作数，Operands`**)而构成，由于 Java 虚拟机采用面向操作数栈而不是寄存器的结构，所以大多数的指令都不包含操作数，只有一个操作码。
- 由于限制了 Java 虚拟机操作码的长度为一个字节(即0~255)，这意味着指令集的操作码总数不可能超过256条。（2^8=256）
- 官方文档：[https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html](https://link.zhihu.com/?target=https%3A//docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html)
- 熟悉虚拟机的指令对于动态字节码生成、反编译 Class 文件、 Class 文件修补都有着非常重要的价值。因此，阅读字节码作为了解 Java 虚拟机的基础技能，需要熟练掌握常见指令。

字节码指令又叫助记符。

![image-20201224080527376](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222821179-1547585668.png)

### 2.1.1. 执行模型

如果不考虑异常处理的话，那么 Java 虚拟机的解释器可以使用下面这个伪代码当做最基本的执行模型来理解。

```java
do {
    自动计算PC寄存器的值加1;
    根据PC寄存器的指示位置，从字节码流中取出操作码;
    if(字节码存在操作数) {
        从字节码流中取出操作数;
    }
    执行操作码所定义的操作;
}while(字节码长度>0);  // 字节码长度大于0，则一直循环执行
```

### 2.1.2. 字节码与数据类型

在 Java 虚拟机的指令集中，大多数的指令都包含了其操作所对应的数据类型信息。例如，iload 指令用于从局部变量表中加载 int 类型的数据到操作数栈中，而 fload 指令加载的则是 float 类型的数据。

对于大部分与数据类型相关的字节码指令，**`它们的操作码助记符中都有特殊的字符来表明专门为哪种数据类型服务`**

- i 代表对 int 类型的数据操作
- l 代表 long
- s 代表 short
- b 代表 byte
- c 代表 char
- f 代表 float
- d 代表 double
- a 代表对象类型

也有一些指令的助记符中**没有明确地指明操作类型的字母**，如 arraylength 指令（获取数组对象的长度的指令），它没有代表数据类型的特殊字符，但操作数永远只能是一个数组类型的对象。

还有另一些指令，如无条件跳转指令 goto 则是与**数据类型无关的**。

大部分的指令都没有支持整数类型 byte、char 和 short，甚至没有任何指令支持 boolean 类型。编译器会在编译期或运行期将 byte 和short 类型的数据带符号扩展(Sign-Extend)为相应的 int 类型数据，将 boolean 和 char 类型数据零位扩展(Zero-Extend)为相应的 int 类型数据。与之类似，在处理 boolean、byte、short 和 char 类型的数组时，也会转换为使用对应的 init 类型的字节码指令来处理。因此，大多数对于 boolean、byte、short 和 char 类型数据的操作，实际上都是使用相应的 int 类型作为运算类型。

a是对象类型

![image-20201224081519497](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222820918-1584236799.png)

整数所占槽位

![image-20201224081856733](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222820658-1918677079.png)

### 2.1.3. 指令分类

- 由于完全介绍和学习这些指令需要花费大量时间，为了让能够更快地熟悉和了解这些基本指令，这里将 JVM 中的字节码指令集按用途大致分成9类：
  - **加载与存储指令**
  - **算术指令**
  - **类型转换指令**
  - **对象的创建与访问指令**
  - **方法调用与返回指令**
  - **操作数栈管理指令**
  - **比较控制指令**
  - **异常处理指令**
  - **同步控制指令**
- 在做值相关操作时：
  - 一个指令，可以从局部变量表、常量池、堆中对象、方法调用、系统调用等中取得数据，这些数据(可能是值，可能是对象的引用)被压入操作数栈。
  - 一个指令，也可以从操作数栈中取出一到多个值(pop 多次)，完成赋值、加减乘除、方法传参、系统调用等操作。

![image-20201224082359843](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222820286-1597800337.png)

---

## 2.2. 加载与存储指令

1、作用

加载和存储指令用于将数据从栈帧的局部变量表和操作数栈之间来回传递。

![image-20201223082017376](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222818978-2132504790.png)

2、常用指令

- `【局部变量压栈指令】`将一个局部变量加载到操作数栈：`xload、xload_n`(`其中 x 为 i、l、f、d、a； n 为 0 到 3`)（比如iload_0、lload_1、aload_3，因为0~3的操作次数比较多，所以就预先定义好了，4以上就要自己定义，就没有下划线，比如iload 4）。
- `【常量入栈指令】`将一个常量加载到操作数栈：`bipush、sipush、ldc、ldc_w、ldc2_w、aconst_null、iconst_m1、iconst_<i>、iconst_<l>、fconst_<f>、dconst_<d>`。
- `【出栈装入局部变量表指令】`将一个数值从操作数栈存储到局部变量表：`xstore、xstore_<n>`(`其中 x 为 i、l、f、d、a，n 为 0 到 3`); `xastore`(`其中 x 为 i、l、f、d、a、b、c、s`)。
- 扩充局部变量表的访问索引的指令：`wide`。

**`小结：load、push、ldc、const都是压栈到操作数栈中，store是保存数据到局部变量表中。`**

上面所列举的指令助记符中，有一部分是以下划线加上n结尾的(例如`iload_n`)。这些指令助记符实际上代表了一组指令(例如`iload_n`代表了`iload_0、iload_1、iload_2和iload_3`这几个指令)。这几组指令都是某个带有一个操作数的通用指令(例如 `iload`)的特殊形式，**`对于这若干组特殊指令来说，它们表面上没有操作数，不需要进行取操作数的动作，但操作数都隐含在指令中`**。

除此之外，它们的语义与原生的通用指令完全一致(例如 `iload_0`的语义与操作数为0时的`iload`指令语义完全一致)。在load前面的字母指定了指令隐含操作数的数据类型，代表非负的整数，*代表是 int 类型数据，代表 long 类型，代表 float 类型，代表 double 类型。*

*操作 byte、char、short 和 boolean 类型数据时，经常用 int 类型的指令来表示。*

例子：

iload_0：将局部变量表中索引为0位置上的数据压入操作数栈中；（iload_0与iload 0是一样的，区别在于iload 0占用3个字节，操作码1个字节码，操作数2个字节，而iload_0只占用1个字节，所以iload_0更节省空间。）

![image-20201225075659722](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222819597-761451946.png)

### 2.2.1. 复习：再谈操作数栈与局部变量表

#### 2.2.1.1. 操作数栈(Operand Stacks)

![image-20201225080336223](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222819281-145440929.png)

![image-20201223082017376](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222818978-2132504790.png)

我们知道，Java 字节码是 Java 虚拟机所使用的指令集。因此，它与 Java 虚拟机基于栈的计算模型是密不可分的。

在解释执行过程中，每当为 Java 方法分配栈帧时，Java 虚拟机往往需要**开辟一块额外的空间作为操作数栈，来存放计算的操作数以及返回结果**。

具体来说便是：**执行每一条指令之前，Java 虚拟机要求该指令的操作数已被压入操作数栈中。在执行指令时，Java 虚拟机会将该指令所需的操作数弹出，并且将指令的结果重新压入栈中**。

以加法指令 iadd 为例。假设在执行该指令之前，栈顶的两个元素分别为 int 值 1 和 int 值 2，那么 iadd 指令将弹出这两个 int（由于 iadd 指令只消耗栈顶的两个元素，因此，对于离栈顶距离为 2 的元素，即图中的问号，iadd 指令并不关心它是否存在，更加不会对其进行修改），并将求得的和 int 值为 3 压入栈中。

![image-20201225080748980](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222818702-1112784793.png)

![image-20201225080850246](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222818399-255356127.png)

#### 2.2.1.2. 局部变量表(Local Variables)

Java 方法栈帧的另外一个重要组成部分则是局部变量区，**`字节码程序可以将计算的结果缓存在局部变量区之中`**。

实际上，Java 虚拟机将局部变量区当成**一个数组**，依次存放 this 指针(仅非静态方法)、所传入的参数、以及字节码中的局部变量。

和操作数栈一样，long 类型以及 double 类型的值将占据两个单元，其余类型仅占据一个单元。

![image-20201225081439684](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222818107-1550878803.png)

例子：

```java
public void foo(long l, float f) {
  {
    int i = 0;
  }
  {
    String s = "Hello, World"
  }
}
```

![image-20201225081821080](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222817830-805363219.png)

在栈帧中，与性能调优关系最为密切的部分就是局部变量表。局部变量表中的变量也是重要的垃圾回收根节点，只要被局部变量表中直接或间接引用的对象都不会被回收。

在方法执行时，虚拟机使用局部变量表完成方法的传递。

### 2.2.2. 局部变量压栈指令

局部变量压栈指令将给定的局部变量表中的数据压入操作数栈。

这类指令大体可以分为：

- *`xload_<n>`(`x`为`i、l、f、d、a， n为 0 到 3`)*
- *`xload`(`x`为`i、l、f、d、a`)*

说明：在这里，x的取值表示数据类型，n为局部变量表的索引值。

指令`xload_n`表示将第n个局部变量压入操作数栈，比如`iload_1、fload_0、aload_0`等指令。其中`aload_n`表示将一个对象引用压栈。

指令`xload`通过指定参数的形式，把局部变量压入操作数栈，当使用这个命令时，表示局部变量的数量可能超过了4个，比如指令`iload、fload`等。

例子：

```java
import java.util.Date;
 
public class _07_LoadAndStoreTest {
    // 1.局部变量压栈指令
    public void load(int num, Object obj,long count,boolean flag,short[] arr) {
        System.out.println(num);
        System.out.println(obj);
        System.out.println(count);
        System.out.println(flag);
        System.out.println(arr);
    }
 
    // 2.常量入栈指令
    public void pushConstLdc() {
        int i = -1;
        int a = 5;
        int b = 6;
        int c = 127;
        int d = 128;
        int e = 32767;
        int f = 32768;
    }
 
    public void constLdc() {
        long a1 = 1;
        long a2 = 2;
        float b1 = 2;
        float b2 = 3;
        double c1 = 1;
        double c2 = 2;
        Date d = null;
 
    }
 
    // 3.出栈装入局部变量表指令
    public void store(int k, double d) {
        int m = k + 2;
        long l = 12;
        String str = "atguigu";
        float f = 10.0F;
        d = 10;
    }
 
    public void foo(long l, float f) {
        {
            int i = 0;
        }
        {
            String s = "Hello, World";
        }
    }
}
 
```

分析load方法的字节指令

![image-20201227095459865](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222817534-1225986731.png)

局部变量表

![image-20201227095938223](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222817077-179713973.png)

字节指令执行过程

iload_

![image-20201227100835651](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222816574-1573116719.png)

除了iload 5不一样，其他load指令同理

![image-20201227101520671](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222815965-1599123370.png)

### 2.2.3. 常量入栈指令

常量入栈指令的功能是将常数压入操作数栈，根据数据类型和入栈内容的不同，又可以分为 `const 系列`、`push 系列`和 `ldc 指令`（这个三个指令范围依次变大）。

**指令 const 系列：**用于对特定的常量入栈，入栈的常量隐含在指令本身里。指令有：`iconst_<i>(i从-1到5)、lconst_<l>(l从0到1)、fconst_<f>(f从0到2)、dconst_<d>(d从0到1)、aconst_null`（注意，这里的取值都是指具体的常量值，不是“局部变量压栈指令”那样的索引值；引用类型，都有一个默认的null值）。

**比如：**

- **iconst_m1将-1压入操作数栈（m1就是-1，m是minus的缩写）**
- **iconst_x(x为0到5)将 x 压入栈**
- **lconst_0、lconst_1 分别将长整数0和1压入栈**
- **fconst_0、fconst_1、fconst_2 分别将浮点数0、1、2压入栈**
- **dconst_0 和 dconst_1 分别将 double 型0和1压入栈**
- **aconst_null 将 null 压入操作数栈**

**`从指令的命名上不难找出规律，指令助记符的第一个字符总是喜欢表示数据类型，i 表示整数，l 表示长整型，f 表示浮点数，d 表示双精度浮点，习惯上用 a 表示对象引用。如果指令隐含操作的参数，会以下划线形式给出。`**

```java
Java代码： int i = 3;  -->  对应的字节码指令：iconst_3
Java代码： int j = 6;  -->  对应的字节码指令：不是iconst 6，因为iconst只能操作-1到5的数，而是bipush 6
```

**指令 push 系列：**主要包括 bipush 和 sipush。它们的区别在于接受数据类型的不同，bipush 接收8位（1字节）整数作为参数，sipush 接收16位（2字节）整数，它们都将参数压入栈。

**指令 ldc 系列：**如果以上指令都不能满足需求，那么可以使用万能的 ldc 指令，它可以接收一个8位的参数，该参数指向常量池中的 int、float 或者 String 的索引（`ldc操作的常量值会从常量池获取的，与其他指令直接操作具体的常量值不同`），将指定的内容压入堆栈。

**类似的还有 \**ldc_w\**，它接收两个8位参数，能支持的索引范围大于 ldc。**

如果要压入的元素是 long 或者 double 类型的，则使用 **ldc2_w** 指令，使用方式都是类似的。

**总结如下：**

![image-20201227103924096](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222815550-877480815.png)

**例子：（接着前面的_07_LoadAndStoreTest）**

![image-20201227104949674](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222815194-1068143928.png)

![image-20201227105604823](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222814887-1678168692.png)

### 2.2.4. 出栈装入局部变量表指令

**出栈装入局部变量表指令用于将操作数栈中栈顶元素弹出后，装入局部变量表的指定位置，用于给局部变量赋值。**

**这类指令主要以`store` 的形式存在，比如 `xstore(x 为 i、l、f、d、a)、xstore_n(x 为 i、l、f、d、a，n 为0至3)`。**

- **其中，指令 istore_n 将从操作数栈中弹出一个整数，并把它赋值给局部变量表中索引为n的位置（n表示索引，与load指令类似）。**
- **指令 xstore 由于没有隐含参数信息，故需要提供一个 byte 类型的参数类指定目标局部变量表的位置。**

**说明：**

**一般说来，类似像 store 这样的命令需要带一个参数，用来指明将弹出的元素放在局部变量表的第几个位置**。但是，为了尽可能压缩指令大小，使用专门的 istore_1 指令表示将弹出的元素放置在局部变量表第1个位置。类似的还有 istore_0、istore_2、istore_3，它们分别表示从操作数栈顶弹出一个元素，存放在局部变量表第0、2、3个位置.

**由于局部变量表前几个位置总是非常常用，因此**这种做法虽然增长了指令，但是可以大大压缩生成的字节码的体积（也就是降低字节码指令的数量）**。如果局部变量表很大，需要存储的槽位大于3，那么可以使用 istore 指令，外加一个参数，用来表示需要存放的槽位位置。**

**例子：（接着前面的_07_LoadAndStoreTest）**

![image-20201227111332985](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222814421-172329550.png)

![image-20220422172130405](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222814029-105807236.png)

![image-20201227111525913](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222813618-1924694303.png)

![image-20201227112100285](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222813150-693529123.png)

**有个需要注意的地方**

![image-20201227114052698](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222812592-1413078924.png)

### 2.2.5. 总结：

**load是局部变量压入操作数栈；**

**const是常量压入操作数栈；**

**store是操作数栈的栈顶出栈装入局部变量。**

---

## 2.3. 算术指令

**1、作用**

**算术指令用于对两个操作数栈上的值进行某种特定运算，并把结果重新压入操作数栈。**

**2、分类**

大体上算术指令可以分为两种：对**整型数据进行运算的指令**与对**浮点型类型数据进行运算的指令**。

**3、byte、short、char 和 boolean 类型说明**在每一大类中，都有针对 Java 虚拟机具体数据类型的专用算术指令。但没有直接支持 byte、short、char 和 boolean 类型的算术指令，对于这些数据的运算，都使用 int 类型的指令来处理。此外，在处理 boolean、byte、short 和 char 类型的数组时，也会转换为使用对应的 int 类型的字节码指令来处理。

![image-20201227144941651](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222812236-184446536.png)

**疑问：既然boolean、byte、short 和 char 类型最终都会转成int处理，那么是不是有些类型实际不需要存在，比如short？**

**4、运算时的溢出**

数据运算可能会导致溢出，例如两个很大的正整数相加，结果可能是一个负数。其实 Java 虚拟机规范并无明确规定过整型数据溢出的具体结果，仅规定了在处理整型数据时，只有除法指令以及求余指令中当出现除数为0时会导致虚拟机抛出异常 ArithmeticException。

**5、运算模式**

- 向最接近数舍入模式（四舍五入）：JVM 要求在进行浮点数计算时，所有的运算结果都必须舍入到适当的精度，非精确结果必须舍入为可被表示的最接近的精确值，如果有两种可表示的形式与该值一样接近，将优先选择最低有效位为零的.
- 向零舍入模式（向下取整）：将浮点数转换为整数时，采用该模式，该模式将在目标数值类型中选择一个最接近但是不大于原值的数字作为最精确的舍入结果。

**6、NaN 值使用**

当一个操作产生溢出时，将会使用有符号的无穷大`Infinity`表示；如果某个操作结果没有明确的数学定义的话，将会使用 `NaN`值来表示。而且所有使用 NaN 值作为操作数的算术操作，结果都会返回 NaN。

**例子：**

```java
import org.junit.Test;
 
public class _08_ArithmeticTest {
     @Test
    public void method1(){
//        int n = 10;
//        int m = n / 0;
//        System.out.println(m);//java.lang.ArithmeticException: / by zero
 
        int i = 10;
        // 0.0是一个很小的数值，实际不是0，一个数除以很小的数就是无穷大
        double j = i / 0.0;
        System.out.println(j);// Infinity = 无穷大（有符号的使用无穷大表示）
 
 
        double d1 = 0.0;
        /**
         * 两个一样的数相除应该是1；
         * 但是单纯看分子，0除以任何数都是0；
         * 但是单纯看分母，一个数除以0.0（很小的值）应该是无穷大的，所以这个结果不确定。
         */
        double d2 = d1 / 0.0;
        System.out.println(d2);//NaN = not a number
    }
}
 
```

### 2.3.1. 所有算术指令

**所有算术指令包括：**

- **加法指令：iadd、ladd、fadd、dadd**
- **减法指令：isub、lsub、fsub、dsub**
- **乘法指令：imul、lmul、fmul、dmul**
- **除法指令：idiv、ldiv、fdiv、ddiv**
- **求余指令：irem、lrem、frem、drem // rem = remainder：余数**
- **取反指令：ineg、lneg、fneg、dneg // neg = negation：取反**
- **自增指令：iinc**
- **位运算指令，又可分为：**
- - **位移指令：ishl（左移）、ishr（右移）、iushr（无符号右移）、lshl（long的左移）、lshr（long的右移）、lushr（long的无符号右移）**
  - **按位或指令：ior、lor**
  - **按位与指令：iand、land**
  - **按位异或指令：ixor、lxor**
- **比较指令：dcmpg、dcmlp、fcmpg、fcmpl、lcmp**

#### 2.3.1.1. 例子1：

```java
public class _08_ArithmeticTest2 {
 
    public void method2(){
        float i = 10;
        float j = -i;
        i = -j;
    }
 
    public void method3(int j){
        int i = 100;
        i = i + 10;
//        i += 10;
    }
    public int method4(){
        int a = 80;
        int b = 7;
        int c = 10;
        return (a + b) * c;
    }
 
    public int method5(int i ,int j){
        return ((i + j - 1) & ~(j - 1));
    }
}
 
```

![image-20201227153719460](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222811937-877705341.png)

**i = i + 10;**

![image-20201227154559872](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222811650-1958611250.png)

**i += 10; 比 i = i + 10; 少几个步骤，如果i是byte，还能减少转型为int的操作**

![image-20201227154800826](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222811367-1837865957.png)

**算术的字节码步骤**

![image-20201227160113173](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222810973-83024868.png)

**通过异或达到取反的结果**

![image-20201227170525494](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222810630-1468222471.png)

#### 2.3.1.2. 例子2

**曾经的一个例子（上篇：方法区使用举例）：注意一下，一个方法内部调用另一个方法的情况**

```java
public class _45_MethodAreaDemo {
    public static void main(String[] args) {
        int x = 500;
        int y = 100;
        int a = x / y;
        int b = 50;
        System.out.println(a + b);
    }
}
```

**反编译得到字节码**

![image-20201029165058849](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222810315-1888240170.png)

**执行过程**

![image-20201029165202397](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222809855-174959989.png)

![image-20201029165213308](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222809380-675788039.png)

![image-20201029165225784](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222808770-1793206065.png)

![image-20201029165346015](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222808326-1410167404.png)

![image-20201029165403313](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222807962-1134942637.png)

![image-20201029165444201](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222807529-1717353327.png)

![image-20201029165457601](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222807070-1550997001.png)

![image-20201029165511369](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222806556-1431839422.png)

![image-20201029165537330](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222806143-1976778331.png)

![image-20201029165552115](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222805752-1486593628.png)

![image-20201029165609001](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222805396-1215846493.png)

![image-20201029165627314](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222804887-1910505894.png)

![image-20201029165636144](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222804468-2043673504.png)

![image-20201029165646492](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222804011-1520713994.png)

**println方法是main方法里面的一个新方法，所以会开辟一个新的栈帧，a+b的结果55就会作为局部变量传递给println。**

![image-20201029165656744](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222803628-960950824.png)

![image-20201029165728537](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222803252-1901032004.png)

#### 2.3.1.3. 例子3：

**i++;（前++） 和 ++i;（后++） ---- i的值没有赋值给局部变量的情况**

```java
package _10;
 
 
public class _08_ArithmeticTest3 {
 
    //关于(前)++和(后)++
    public void method6(){
        int i = 10;
        i++;
//        ++i;
 
//        for(int j = 0;j < 10;j++){}
    }
    public void method7(){
        int i = 10;
        int a = i++;
 
        int j = 20;
        int b = ++j;
    }
    //思考
    public void method8(){
        int i = 10;
        i = i++;
        System.out.println(i);//10
    }
}
 
```

**i++;**

![image-20201227220000865](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222802968-832865498.png)

**++i;**

![image-20201227220218965](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222802661-44820154.png)

**所以for循环中，`for(int i=0; i<10; i++){}` 和 `for(int i=0; i<10; ++i){}`是一样的。**

**i++; 和 ++i; ------------------- i的值有赋值给变量的情况**

**（i++先load出来栈 ，然后i自增，再把栈里的数出栈压入局部变量(在操作数栈有个临时变量的概念)；而++i是先i自增，然后load出来赋值给局部变量）**

![image-20201227223650251](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222802297-231314434.png)

**思考题：**

![image-20201228081236710](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222801793-39365563.png)

### 2.3.2. 比较指令的说明

**（本小节在控制转移指令的时候再回头看）**

- **比较指令的作用是比较栈顶两个元素的大小，并将比较结果入栈。**

- 比较指令有：`dcmpg、dcmpl、fcmpg、fcmpl、lcmp`

  ,

  ```
  适用于double、float、long类型的比较
  ```

  。（开头带d的是double，带f的是float，带l的是long。double、float有两个是因为有NaN，long只有一个是因为没有NaN）

  - **与前面讲解的指令类似，首字符 d 表示 double 类型，f 表示 float，l 表示 long**

- **对于 double 和 float 类型的数字，由于 NaN 的存在，各有两个版本的比较指令，以 float 为例，有 fcmpg 和 fcmpl 两个指令，它们的区别在于在数字比较时，若遇到 NaN 值，处理结果不同。**

- **指令 dcmpl 和 dcmpg 也是类似的，根据其命名可以推测其含义，在此不再赘述。**

- **指令 lcmp 针对 long 型整数，由于 long 型整数没有 NaN 值，故无需准备两套指令。**

**举例：**

**指令 fcmpg 和 fcmpl 都从栈中弹出两个操作数，并将它们做比较，设栈顶的元素为 v2， 栈顶顺位第2位元素为 v1，弹出v1、v2进行比较，若 v1 = v2，则压入0；若 v1 > v2 则压入1；若 v1 < v2 则压入-1。**

**两个指令的不同之处在于，如果遇到 NaN 值，fcmpg 会压入1，而 fcmpl 会压入-1。**

![image-20201228081954920](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222801528-1680650116.png)

**数值类型的数据才可以谈大小！boolean、引用数据类型不能比较大小。尽管引用类型可以排序，排序的本质就是比较，实际还是比较数值，不能说对象谁大谁小，只能说等于或者不等于（对象地址），如果要排序的就要重写compareTo方法，实际比较的是数值，不是对象本身。（为什么没有比较byte、short、char、int，这部分在“控制转移指令”会解释。这里的比较指令得到一个int值，然后就是条件跳转，对于后面在“控制转移指令”讲解。）**

---

## 2.4. 类型转换指令

**类型转换指令说明：**

1. 类型转换指令可以将两种不同的数值类型进行相互转换。
2. 这些转换操作一般用于实现用户代码中的`显式类型转换操作`，或者用来处理`字节码指令集中数据类型相关指令无法与数据类型一一对应的问题`。

### 2.4.1. 宽化类型转换（小转大）

**本小节例子源码：**

```java
import org.junit.Test;
 
public class _09_ClassCastTest1 {
    //宽化类型转换
 
    //针对于宽化类型转换的基本测试
    public void upCast1(){
        int i  = 10;
        long l = i;
        float f = i;
        double d = i;
 
        float f1 = l;
        double d1 = l;
 
        double d2 = f1;
    }
    
    //举例：精度损失的问题
    @Test
    public void upCast2(){
        int i = 123123123;
        // i转f
        float f = i;
        // 1.2312312E8(10的8次方) = 123123120（最后一位3丢失了）
        System.out.println(f);
 
        long l = 123123123123L;
        // 小数值的l转d
        double d = l;
        // 1.23123123123E11 = 123123123123（没有丢失）
        System.out.println(d);
 
        long l2 = 123123123123123123L;
        // 大数值的l转d
        double d2 = l2;
        //1.2312312312312312E17 = 123123123123123120（最后一位3丢失了）
        System.out.println(d2);
 
    }
    //针对于byte、short等转换为容量大的类型时，将此类型看做int类型处理。
    public void upCast3(byte b){
        int i = b;
        long l = b;
        double d = b;
    }
 
    public void upCast4(short s){
        int i = s;
        long l = s;
        float f = s;
    }
}
 
```

**宽化类型转换(Widening Numeric Conversions)**

**1、转换规则**

**Java 虚拟机直接支持以下数值的宽化类型转换(Widening Numeric Conversion，小范围类型向大范围类型的安全转换)。也就是说，并不需要指令执行，包括：**

- 从 int 类型到 long、float 或者 double 类型，对应的指令为：i2l、i2f、i2d
- 从 long 类型到 float、double 类型。对应的指令为：l2f、l2d
- 从 float类型到 double 类型。对应的指令为：f2d

**简化为：`int --> long --> float --> double`**

**例子：**

![image-20210103100603317](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222801178-41593701.png)

**2、精度损失问题**

2.1、宽化类型转换是不会因为超过目标类型最大值而丢失信息的，例如，从 int 转换到 long，或者从 int 转换到 double，都不会丢失任何信息，转换前后的值是精确相等的。

2.2、从 int、long 类型数值转换到 float，或者 long 类型数值转换到 double 时，将可能发生丢失精度——可能丢失掉几个最低有效位上的值，转换后的浮点数值是根据 IEEE754 最接近舍入模式所得到的正确整数数值。（float占4字节，float一部分是底数和一部分是指数，精度会降低，所以4字节的int转float精度会丢失，转double会比float的精度高；long和double都是8字节，但是double同样是一部分底数、一部分指数构成。）尽管宽化类型转换实际上是可能发生精度丢失的，但是这种转换永远不会导致 Java 虚拟机抛出运行时异常。

**例子：**

![image-20210103102751163](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222800876-331652160.png)

**3、补充说明：**

**`从 byte、char 和 short 类型到 int 类型的宽化类型转换实际上是不存在的`**，对于 byte 类型转换为 int，虚拟机并没有做实质性的转化处理，只是简单地通过操作数栈交换了两个数据。而将 byte 转为 long 时，使用的是 i2l，可以看到在内部 byte 在这里已经等同于 int 类型处理，类似的还有 short 类型，这种处理方式有两个特点：

**一方面可以减少实际的数据类型，如果为 short 和 byte 都准备一套指令，那么指令的数量就会大增**，而虚拟机目前的设计上，只愿意使用一个字节表示指令，因此指令总数不能超过256个，为了节省指令资源，将 short 和 byte 当作 int 处理也是情理之中**。**

**另一方面**，由于局部变量表中的槽位固定为32位**，无论是 byte 或者 short 存入局部变量表，都会占用32位空间。从这个角度来说，也没有必要特意区分这几种数据类型。**

**例子：**

![image-20210103103204697](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222800535-1922960.png)

### 2.4.2. 窄化类型转换（大转小）

**本小节源码**

```java
import org.junit.Test;
 
public class _09_ClassCastTest2 {
    //窄化类型转换
    //基本的使用
    public void downCast1(){
        int i = 10;
        byte b = (byte)i;
        short s = (short)i;
        char c = (char)i;
 
        long l = 10L;
        int i1 = (int)l;
        byte b1 = (byte) l;
 
    }
    public void downCast2(){
        float f = 10;
        long l = (long)f;
        int i = (int)f;
        byte b = (byte)f;
 
        double d = 10;
        byte b1 = (byte)d;
 
    }
 
    public void downCast3(){
        short s = 10;
        byte b = (byte)s;
    }
 
    //窄化类型转换的精度损失
    @Test
    public void downCast4(){
        int i = 128;
        byte b = (byte)i;
        System.out.println(b);  // -128
    }
    //测试NaN,无穷大的情况
    @Test
    public void downCast5(){
        double d1 = Double.NaN; // NaN = 0.0 / 0.0
        int i = (int)d1;
        System.out.println(d1); // NaN
        System.out.println(i); // 0
 
        double d2 = Double.POSITIVE_INFINITY;  // double的最大值
        System.out.println(d2); // Infinity
        long l = (long)d2;
        int j = (int)d2;
        // double最大值窄化为Long，取Long的最大值
        System.out.println(l);              // 9223372036854775807
        System.out.println(Long.MAX_VALUE); // 9223372036854775807
        // double最大值窄化为int，取int的最大值
        System.out.println(j);                 // 2147483647
        System.out.println(Integer.MAX_VALUE); // 2147483647
 
        float f = (float)d2;
        // double最大值窄化为float，取float的最大值
        System.out.println(f); // Infinity
 
        float f1 = (float)d1;
        // NaN转窄化还是NaN
        System.out.println(f1); // NaN
    }
}
 
```

**窄化类型转换(Narrowing Numeric Conversion)**

**1、转换规则**

**Java 虚拟机也直接支持以下窄化类型转换**：

- 从 int 类型至 byte、short 或者 char 类型。对应的指令有：i2b、i2s、i2c
- 从 long 类型到 int 类型。对应的指令有：l2i
- 从 float 类型到 int 或者 long 类型。对应的指令有：f2i、f2l
- 从 double 类型到 int、long 或者 float 类型。对应的指令有：d2i、d2l、d2f

**例子：**

![image-20210103104309094](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222800156-280399346.png)

![image-20210103105345515](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222759829-394053320.png)

**2、精度损失问题**

窄化类型转换可能会导致转换结果具备不同的正负号（大转小会砍掉一部分，砍掉后可能会导致最高位是1，就会得到负数的值，哪怕原来是正数）、不同的数量级，因此，转换过程很可能会导致数值丢失精度。

尽管数据类型窄化转换可能会发生上限溢出、下限溢出和精度丢失等情况，但是 Java 虚拟机规范中明确规定数值类型的**窄化转换指令永远不可能导致虚拟机抛出运行时异常**

------

**例子：**

![image-20210103105722435](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222759560-1570800221.png)

![image-20210103110553139](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222759216-357150080.png)

**3、补充说明**

**3.1、当将一个浮点值窄化转换为整数类型 T(T 限于 int 或 long 类型之一)的时候，将遵循以下转换规则：**

- 如果浮点值是 NaN，那转换结果就是 int 或 long 类型的0。
- 如果浮点值不是无穷大的话，浮点值使用 IEEE 754 的向零舍入模式取整，获得整数值 v，如果 v 在目标类型 T(int 或 long)的表示范围之内，那转换结果就是 v。否则，将根据 v 的符号，转换为 T 所能表示的最大或者最小正数。

**3.2、当将一个 double 类型窄化转换为 float 类型时，将遵循以下转换规则：**

通过向最接近数舍入模式舍入一个可以使用 float 类型表示的数字。最后结果根据下面这3条规则判断：

- 如果转换结果的绝对值太小而无法使用 float 来表示，将返回 float 类型的正负零。
- 如果转换结果的绝对值太大而无法使用 float 来表示，将返回 float 类型的正负无穷大。
- 对于 double 类型的 NaN 值将按规定转换为 float 类型的 NaN 值。

**例子：**

![image-20210103113353836](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222758922-888589332.png)

---

## 2.5. 对象的创建与访问指令

Java 是面向对象的程序设计语言，虚拟机平台从字节码层面就对面向对象做了深层次的支持。有一系列指令专门用于对象操作（包括从类创建的对象，还有数组对象），可进一步细分为**`创建指令`、`字段访问指令`、`数组操作指令`和`类型检查指令`。**

### 2.5.1. 创建指令

**虽然类实例和数组都是对象，但 Java 虚拟机对类实例和数组的创建与操作使用了不同的字节码指令：**

**1、创建类实例的指令：**

- 创建类实例的指令：new
  - 它接收一个操作数，为指向常量池的索引，表示要创建的类型，执行完成后，将对象的引用压入栈

**例子：**

```java
import java.io.File;
 
public class _10_NewTest1 {
    //1.创建指令
    public void newInstance() {
        Object obj = new Object();
 
        File file = new File("words.txt");
    }
}
 
```

![image-20210103160229339](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222758553-10447397.png)

**以前对于一个对象引用存放的简化解释是虚拟机栈，如下图。**

![image-20210103160533862](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222758299-334860037.png)

**而现在是具体的解释，实际是局部变量表存放着对象的地址（不是临时的操作数栈存放的地址）**

![image-20210103163208405](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222757958-1285609018.png)

![image-20210103164304973](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222757640-262332855.png)

**2、创建数组的指令：**

**创建数组的指令：`newarray`、`anewarray`、`multianewarray`**

- newarray：创建基本类型数组
- anewarray：创建引用类型数组
- multianewarray：创建多维数组

上述创建指令可以用于创建对象或者数组，由于对象和数组在 Java 中的广泛使用，这些指令的使用频率也非常高。

**例子：**

```java
import java.io.File;
 
public class _10_NewTest2 {
    //1.创建指令
    public void newArray() {
        int[] intArray = new int[10];
        Object[] objArray = new Object[10];
        int[][] mintArray = new int[10][10];
 
        String[][] strArray = new String[10][];
    }
}
 
```

![image-20210103170754372](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222757226-1565271455.png)

### 2.5.2. 字段访问指令

**对象创建后，就可以通过对象访问指令获取对象实例或数组实例中的字段或者数组元素。**

- `访问类字段(static 字段，或者称为类变量)的指令：getstatic（压字段入操作数栈）`、`putstatic（出操作数栈赋值给字段）`
- `访问类实例字段(非 static 字段，或者称为实例变量)的指令：getfield、putfield`

**举例：**

以 getstatic 指令为例，它含有一个操作数，为指向常量池的 Fieldref 索引，它的作用就是获取 Fieldref 指定的对象或者值，并将其压入操作数栈。

```java
public void sayHello(){
    System.out.println("Hello");
}
```

**对应的字节码指令：**

```Java
0 getstatic #8 <java/lang/System.out>   // out就是静态字段
3 ldc #9 <Hello>
5 invokevirtual #10 <java/io/PrintStream.println>
8 return
```

**图示：**

![image-20210104075148113](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222756896-725000261.png)

**例子：**

```java
public class _10_NewTest3 {
    //2.字段访问指令
    public void sayHello() {
        System.out.println("hello");
    }
 
    public void setOrderId(){
        Order order = new Order();
        order.id = 1001;
        System.out.println(order.id);
 
        Order.name = "ORDER";
        System.out.println(Order.name);
    }
}
 
class Order{
    int id;
    static String name;
}
```

![image-20210104075733461](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222756499-872515088.png)

![image-20210104082014186](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222756114-3368034.png)

![image-20210104082857916](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222755693-53905644.png)

![image-20210110100650614](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222755306-1539328311.png)

### 2.5.3. 数组操作指令

**数组操作指令主要有：xastore 和 xaload 指令。具体为：**

- 把一个数组元素加载到操作数栈的指令：baload（byte、boolean都是b，a是数组）、caload、saload、iaload、laload、faload、daload、aaload
- 将一个操作数栈的值存储到数组元素中的指令：bastore、castore、sastore、iastore、lastore、fastore、dastore、aastore

**即：**

![image-20210110162256298](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222754409-2065397175.png)

- 取数组长度的指令：arraylength
  - 该指令弹出栈顶的数组元素，获取数组的长度，将长度压入栈

之前的store是出栈后把数据存放到局部变量表中，这里操作数组的store不一样，局部变量表只是数组的地址，所以这里的store会把数据存放到堆空间中真正的位置。

![image-20210110162513147](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222754189-1174095260.png)

**说明：**

- 指令 xaload 表示将数组的元素压栈，比如 saload、caload 分别表示压入 short 数组和 char 数组。指令 xaload 在执行时，要求操作数中栈顶元素为**数组索引 i**，栈顶顺位第2个元素为**数组引用 a**，该指令会弹出栈顶这两个元素，并将 a[i] 重新压入堆栈。
- xastore 则专门针对数组操作，以 iastore 为例，它用于给一个 int 数组的给定索引赋值。在 iastore 执行前，操作数栈顶需要以此准备3个元素：**值、索引、数组引用**，iastore 会弹出这3个值，并将值赋给数组中指定索引的位置.

**例子：**

```java
public class _10_NewTest4 {
    //3.数组操作指令
    public void setArray() {
        int[] intArray = new int[10];
        intArray[3] = 20;
        System.out.println(intArray[1]);
        
//        boolean[] arr = new boolean[10];
//        arr[1] = true;
    }
    public void arrLength(){
 
        double[] arr = new double[10];
        System.out.println(arr.length);
    }
}
 
```

![image-20210110173405240](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222753866-1410011309.png)

**int数组的存储和寻找：iastore、iaload**

![image-20210110173106848](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222753520-92427680.png)

**boolean数组**

![image-20210110173750112](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222753098-1421239406.png)

**数组长度**

![image-20210110174111458](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222752749-1188071592.png)

### 2.5.4. 类型检查指令

**检查 类实例或数组类型的指令：instanceof、checkcast**

- 指令 checkcast 用于检查类型强制转换是否可以进行。如果可以进行，那么 checkcast 指令不会改变操作数栈，否则它会抛出 ClassCastException 异常。
- 指令 instanceof 用来判断给定对象是否是某一个类的实例，它会将判断结果压入操作数栈。（instanceof 判断指定对象是不是一个类的实例，如果是就可以用checkcast 强转）

**例子：**

```java
public class _10_NewTest5 {
    //4.类型检查指令
    public String checkCast(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        } else {
            return null;
        }
    }
}
 
```

![image-20210110174837670](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222752425-1819859215.png)

---

## 2.6. 方法调用与返回指令

### 2.6.1. 方法调用指令

**方法调用指令：`invokevirtual`、`invokeinterface`、`invokespecial`、`invokestatic`、`invokedynamic`**

**以下5条指令用于方法调用：**

- invokevirtual 指令用于调用对象的实例方法，根据对象的实际类型进行分派(虚方法分派)，支持多态。这也是 Java 语言中最常见的方法分派方式。（动态绑定，有方法的重写）
- invokeinterface 指令用于调用接口方法，它会在运行时搜索由特定对象所实现的这个接口方法，并找出适合的方法进行调用。（这是编译的时候调用接口方法，运行的时候会调用实现类的方法）
- invokespecial 指令用于调用一些需要特殊处理的实例方法，包括**`实例初始化方法(构造器)、私有方法和 super.父类方法（这些不存在方法重写。父类方法是往上一层找，没有就继续往上找，这也是确定的方法，即静态绑定。）`**。这些方法都是`静态类型绑定`的，不会在调用时进行动态派发。
- invokestatic 指令用于调用命名类中的类方法(static 方法)。这是`静态绑定`的。
- invokedynamic 调用动态绑定的方法，这个是 JDK 1.7 后新加入的指令。用于在运行时动态解析出调用点限定符所引用的方法，并执行该方法。前面4条调用指令的分派逻辑都固化在 Java 虚拟机内部，而 invokedynamic 指令的分派逻辑是由用户所设定的引导方法决定的。（这个暂时不讨论）

**例子：**

```java
import java.util.Date;
 
public class _11_MethodInvokeTest {
    //方法调用指令:invokespecial:静态分派
    public void invoke1(){
        //情况1：类实例构造器方法：<init>()
        Date date = new Date();
 
        Thread t1 = new Thread();
        //情况2：父类的方法
        super.toString();
        //情况3：私有方法
        methodPrivate();
    }
 
    private void methodPrivate(){
 
    }
    //方法调用指令:invokestatic:静态分派
    public void invoke2(){
        methodStatic();
    }
    public static void methodStatic(){
 
    }
 
    //方法调用指令:invokeinterface
    public void invoke3(){
        Thread t1 = new Thread();
        ((Runnable)t1).run();
 
        Comparable<Integer> com = null;
        com.compareTo(123);
    }
 
    //方法调用指令:invokevirtual:动态分派
    public void invoke4(){
        System.out.println("hello");
 
        Thread t1 = null;
        t1.run();
    }
}
 
```

**invokespecial：静态分派**

![image-20210110200110019](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222752045-908734571.png)

**如果改成public方法，就变成invokevirtual**

![image-20210110200224079](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222751560-877449756.png)

**invokestatic: 静态分派**

![image-20210110200451125](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222751205-940391472.png)

**改成私有的，不变**

![image-20210110200538570](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222750573-188678911.png)

**invokeinterface**

![image-20210110201225144](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222750283-1476933188.png)

**接口源码**

![image-20210110200852926](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222749796-1006867632.png)

![image-20210110201029362](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222749272-1262464326.png)

**invokevirtual: 动态分派**

![image-20210110201527874](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222748983-1994815280.png)

**补充例子：**

```java
public class _12_InterfaceMethodTest {
    public static void main(String[] args) {
        AA aa = new BB();
 
        aa.method2();
 
        AA.method1();
    }
}
 
 
interface AA{
    // JDK8中接口可以定义静态方法
    public static void method1(){
 
    }
 
    /**
     * JDK8新特性，default修饰的方法不是抽象方法，
     * 所以实现接口时实现类不需要实现接口中的default修饰的方法。
     * 当然也可以去实现default方法。
     */
    public default void method2(){
 
    }
}
 
class BB implements AA{
 
}
```

![image-20210110202323416](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222748549-688106234.png)

### 2.6.2. 方法返回指令

方法调用结束前，需要进行返回。方法返回指令是`根据返回值的类型区分`

- 包括 ireturn(当返回值是 boolean、byte、char、short 和 int 类型时使用)、lreturn、freturn、dreturn 和 areturn。
- 另外还有一条 return 指令供声明为 void 的方法、实例初始化方法以及类和接口的类初始化方法使用。

![image-20210110202504157](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222747921-1083806168.png)

**举例：**

通过 ireturn 指令，将当前函数操作数栈的顶层元素弹出，并将这个元素压入调用者函数的操作数栈中(因为调用者非常关心函数的返回值)，所有在当前函数操作数栈中的其他元素都会被丢弃。

如果当前返回的是 synchronized 方法，那么还会执行一个隐含的 monitorexit 指令，退出临界区。

最后，会丢弃当前方法的整个帧，恢复调用者的帧，并将控制权转交给调用者。

![image-20210110222252508](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222747491-972627467.png)

**例子：**

```java
public class _13_MethodReturnTest {
    //方法的返回指令
    public int returnInt(){
        int i = 500;
        return i;
    }
 
    public double returnDouble(){
        return 0.0;
    }
 
    public String returnString(){
        return "hello,world";
    }
 
    public int[] returnArr(){
        return null;
    }
    public float returnFloat(){
        int i = 10;
        return i;
    }
 
    public byte returnByte(){
        return 0;
    }
 
    public void methodReturn(){
        int i = returnByte();
    }
}
 
```

![image-20210110222852470](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222747089-1483475286.png)

---

## 2.7. 操作数栈管理指令

**如同操作一个普通数据结构中的堆栈那样，JVM 提供的操作数栈管理指令，可以用于直接操作操作数栈的指令。**

**这类指令包括如下内容：**

- 将一个或两个元素从栈顶弹出，并且直接废弃：`pop`、`pop2`
- 复制栈顶一个或两个数值并将复制值或双份的复制值重新压入栈顶：`dup`、dup2、dup_x1、dup2_x1、dup_x2、dup2_x2
- 将栈最顶端的两个 Slot 数值位置交换：`swap`。Java 虚拟机没有提供交换两个64位数据类型(long、double)数值的指令。（没有swap2
- 指令 `nop`是一个非常特殊的指令，它的字节码为0x00。和汇编语言中的 nop 一样，它表示什么都不做，这条指令一般可用于调试、占位等

这些指令属于通用型，对栈的压入或者弹出无需指明数据类型。（比如前面说的goto)

**说明：**

- 不带 _x 的指令是复制栈顶数据并压入栈顶。包括两个指令，dup 和 dup2，dup 的系数代表要复制的 Slot 个数

  - `dup 开头的指令用于复制1个 Slot 的数据`。例如1个 int 或1个 reference 类型数据
  - d`up2 开头的指令用于复制2个 Slot 的数据`。例如1个 long，或2个 int，或1个 int 加1个 float 类型数据

- 带 _x 的指令是复制栈顶数据并插入栈顶以下的某个位置。共有4个指令，dup_x1、dup2_x1、dup_x2、dup2_x2。对于带 _x 的复制插入指令，只要将指令的 dup 和 x 的系数相加，结果即为需要插入的位置。因此

  - dup_x1 插入位置：1+1=2，即栈顶2个 Slot 下面

  - dup_x2 插入位置：1+2=3，即栈顶3个 Slot 下面

    ![image-20210110224559373](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222746448-105824943.png)

  - dup2_x1 插入位置：2+1=3，即栈顶3个 Slot 下面

  - dup2_x2 插入位置：2+2=4，即栈顶4个 Slot 下面

    ![image-20210111074908849](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222746149-771126689.png)

- `pop：将栈顶的1个 Slot 数值出栈`。例如1个 short 类型数值。

- `pop2：将栈顶的2个 Slot 数值出栈`。例如1个 double 类型数值，或者2个 int 类型数值。

**例子：**

```java
public class _14_StackOperateTest {
    public void print(){
        Object obj = new Object();
//        String info = obj.toString();
        obj.toString();
    }
    //类似的
    public void foo(){
        bar();
    }
    public long bar(){
        return 0;
    }
 
    public long nextIndex() {
        return index++;
    }
 
    private long index = 0;
}
 
```

![image-20210111080506956](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222745829-399120184.png)

![image-20210111080646709](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222745560-915185769.png)

**return index++;的操作（有一种原来是这么操作的感叹）**

![image-20210111080928494](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222745296-2013402047.png)

**分析**

![image-20210111082330760](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222744941-466816394.png)

![image-20210111082558424](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222744645-1240940569.png)

![image-20210111082817123](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222744343-1422777712.png)

![image-20210111082950920](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222744041-568139339.png)

---

## 2.8. 控制转移指令

程序流程离不开条件控制，为了支持条件跳转，虚拟机提供了大量字节码指令，大体上可以分为：

**1）`比较指令`、2）`条件跳转指令`、3）`比较条件跳转指令`、4）`多条件分支跳转指令`、5）`无条件跳转指令等`。**

### **比较指令**

**（在前面：本章 - 算术指令 - 比较指令的说明,主要包括`dcmpg、dcmpl、fcmpg、fcmpl、lcmp`）**，`适用于double、float、long类型的比较`

### 2.8.1. 条件跳转指令

条件跳转指令通常和比较指令结合使用。在条件跳转指令执行前，一般可以先用比较指令进行栈顶元素的准备，然后进行条件跳转。

**条件跳转指令有：**`ifeq、iflt、ifle、ifne、ifgt、ifge、ifnull、ifnonnull`，`这些指令和0或者和null比较`**。这些指令都接收两个字节的操作数，用于计算跳转的位置(16位符号整数作为当前位置的 offset)。**

它们的统一含义为：弹出栈顶元素，测试它是否满足某一条件，如果满足条件，则跳转到给定位置**。**

具体说明：（比较指令 的结果使用如下指令进行跳转处理）

![image-20210111214046941](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222743741-1454631137.png)

**注意：**

**1、与前面运算规则一致：**

- 对于 boolean、byte、char、short 类型的条件分支比较操作，都是使用 int 类型的比较指令完成。
- 对于 long、float、double 类型的条件分支比较操作，则会先执行相应类型的比较运算指令，运算指令会返回一个整型值到操作数栈中，随后再执行 int 类型的条件分支比较操作来完成整个分支跳转。

**2、由于`各类型的比较最终都会转为 int 类型的比较操作`，所以 Java 虚拟机提供的 int 类型的条件分支指令是最为丰富和强大的。**

**例子：**

```java
public class _15_IfSwitchGotoTest1 {
    //1.条件跳转指令
    public void compare1(){
        int a = 0;
        if(a == 0){
            a = 10;
        }else{
            a = 20;
        }
    }
    public boolean compareNull(String str){
        if(str == null){
            return true;
        }else{
            return false;
        }
    }
    //结合比较指令
    public void compare2() {
        float f1 = 9;
        float f2 = 10;
        System.out.println(f1 < f2); // true
    }
    public void compare3() {
        int i1 = 10;
        long l1 = 20;
        System.out.println(i1 > l1); // false
    }
 
    public int compare4(double d) {
        if (d > 50.0) {
            return 1;
        } else {
            return -1;
        }
    }
}
```

![image-20210112075938842](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222743465-2071148120.png)

![image-20210112080312509](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222743185-834413461.png)

**详细分析一下两个float的比较**

![image-20210112080853101](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222742915-99320557.png)

![image-20210112082408799](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222742643-1034767623.png)

**println方法的重载**

![image-20210112081401359](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222742340-419720345.png)

**前面是小于，现在看看大于**

![image-20210113075034672](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222742054-1360031288.png)

**dcmpl**

![image-20210113075222017](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222741787-1860645727.png)

### 2.8.2. 比较条件跳转指令

**比较条件跳转指令类似于比较指令和条件跳转指令的结合体，它将比较和跳转两个步骤合二为一。**

**这类指令有：if_icmpeq、if_icmpne、if_icmplt、if_icmpgt、if_icmple、if_icmpge、if_acmpeq和 if_acmpne。**

```
使用范围：比较俩个int类型的整数，或者比较俩个引用数据类型
```

其中指令助记符加上 "if_" 后，以字符 "i" 开头的指令针对 int 型整数操作(也包括 short 和 byte 类型)，以字符 "a" 开头的指令表示对象引用的比较。

**具体说明：**

![image-20210111215135234](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222741309-1825462480.png)

这些指令都接收两个字节的操作数作为参数，用于计算跳转的位置。同时在执行指令时，栈顶需要准备两个元素进行比较。指令执行完成后，栈顶的这两个元素被清空，且没有任何数据入栈。如果预设条件成立，则执行跳转，否则，继续执行下一条语句。

**例子：**

```java
public class _15_IfSwitchGotoTest2 {
    //2.比较条件跳转指令
    public void ifCompare1(){
        int i = 10;
        int j = 20;
        System.out.println(i < j);
    }
    public void ifCompare2() {
        short s1 = 10;
        byte b1 = 20;
        System.out.println(s1 > b1);
    }
 
    public void ifCompare3() {
        Object obj1 = new Object();
        Object obj2 = new Object();
        System.out.println(obj1 == obj2);//false
        System.out.println(obj1 != obj2);//true
    }
 
}
```

**if_icmpge**

![image-20210113080538876](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222740894-1545175526.png)

![image-20210113080948625](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222740527-928656906.png)

**引用比较**

![image-20210113081720585](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222740219-631393121.png)

### 2.8.3. 多条件分支跳转

**多条件分支跳转指令是专为 switch-case 语句设计的，主要有`tableswitch` 和 `lookupswitch`。**

![image-20210115075757278](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222739918-649511530.png)

**从助记符上看，两者都是 switch 语句的实现，它们的区别：**

- `tableswitch 要求多个条件分支值是连续的`，它内部只存放起始值和终止值，以及若干个跳转偏移量，通过给定的操作数 index，可以立即定位到跳转偏移量位置，因此效率比较高。
- `lookupswitch 内部存放着各个离散的 case-offset 对`，每次执行都要搜索全部的 case-offset 对，找到匹配的 case 值，并根据对应的 offset 计算跳转地址，因此效率较低。

个人理解：tableswitch类似数组，可根据索引取值；lookupswitch类似列表，必须从头开始取值

指令 tableswitch 的示意图如下图所示。由于 tableswitch 的 case 值是连续的，因此只需要记录最低值和最高值，以及每一项对应的 offset 偏移量，根据给定的 index 值通过简单的计算即可直接定位到 offset。

![image-20210115080415679](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222739661-1380993123.png)

指令 lookupswitch 处理的是离散的 case 值，但是出于效率考虑，将 case-offset 对按照 case 值大小排序，给定 index 时，需要查找与 index 相等的 case，获得其 offset，如果找不到则跳转到 default。指令 lookupswitch 如下图所示。

![image-20210115080501424](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222739322-1317854515.png)

**例子：**

```java
public class _15_IfSwitchGotoTest3 {
    //3.多条件分支跳转
    public void swtich1(int select){
        int num;
        switch(select){
            case 1:
                num = 10;
                break;
            case 2:
                num = 20;
                break;
            case 3:
                num = 30;
                break;
            default:
                num = 40;
        }
 
    }
    public void swtich2(int select){
        int num;
        switch(select){
            case 100:
                num = 10;
                break;
            case 500:
                num = 20;
                break;
            case 200:
                num = 30;
                break;
            default:
                num = 40;
        }
    }
    //jdk7新特性：引入String类型
    public void swtich3(String season){
        switch(season){
            case "SPRING":break;
            case "SUMMER":break;
            case "AUTUMN":break;
            case "WINTER":break;
        }
    }
 
}
```

**tableswitch**

![image-20210115081208832](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222739003-1789038570.png)

**case穿透**

![image-20210115081356275](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222738648-1985675624.png)

**lookupswitch**

![image-20210115081631882](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222738336-1272848686.png)

jdk7新特性：引入String类型，需要根据字符串的hashCode进行判断

![image-20210115081841876](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222738062-1886132621.png)

### 2.8.4. 无条件跳转

**目前主要的无条件跳转指令为 `goto`。指令 goto 接收两个字节的操作数，共同组成一个带符号的整数，**用于指定指令的偏移量，指令执行的目的就是跳转到偏移量给定的位置处**。**

如果指令偏移量太大，超过双字节的带符号整数的范围，则可以使用指令 goto_w，它和 goto 有相同的作用，但是它接收4个字节的操作数，可以表示更大的地址范围

指令 jsr、jsr_w、ret 虽然也是无条件跳转的，但主要用于 try-finally 语句，且已经被虚拟机逐渐废弃，故不在这里介绍

![image-20210115082208568](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222737814-1446373388.png)

**例子：**

```java
public class _15_IfSwitchGotoTest4 {
    //4.无条件跳转指令
    public void whileInt() {
        int i = 0;
        while (i < 100) {
            String s = "atguigu.com";
            i++;
        }
    }
 
    public void whileDouble() {
        double d = 0.0;
        while(d < 100.1) {
            String s = "atguigu.com";
            d++;
        }
    }
 
    public void printFor() {
        short i;
        for (i = 0; i < 100; i++) {
            String s = "atguigu.com";
        }
 
    }
 
    //思考：如下两个方法的操作有何不同？
    public void whileTest(){
        int i = 1;
        while(i <= 100){
 
            i++;
        }
        //可以继续使用i
    }
    public void forTest(){
        for(int i = 1;i <= 100;i++){
 
        }
        //不可以继续使用i
    }
    //更进一步
    public void doWhileTest(){
        int i = 1;
        do{
            i++;
        }while(i <= 100);
    }
 
}
```

**while**

![image-20210116095959874](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222737536-872978533.png)

![image-20210116100636851](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222737239-461818376.png)

**for**

![image-20210116100943910](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222736949-574720394.png)

**while和for循环的区别**

![image-20210116101233602](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222736688-278059663.png)

**do while**

![image-20210116101326618](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222736448-174995154.png)

---

## 2.9. 异常处理指令

### 2.9.1. 抛出异常指令

**（1）athrow 指令**

在 Java 程序中显式抛出异常的操作(throw 语句)都是由 athrow 指令来实现。

除了使用 throw 语句显式抛出异常情况之外，**`JVM 规范还规定了许多运行时异常会在其它 Java 虚拟机指令检测到异常状况时自动抛出`**。例如，在之前介绍的整数运算时，当除数为零时，虚拟机会在 idiv 或 ldiv 指令中抛出 ArithmeticException 异常

**（2）注意**

正常情况下，操作数栈的压入弹出都是一条条指令完成的。唯一的例外情况是**`在抛异常时，Java 虚拟机会清除操作数栈上的所有内容，而后将异常实例压入调用者操作数栈上`**。（效果类似于return）

**异常及异常的处理：**

**过程一**：`异常对象的生成` :throw(手动/自动) ---> 指令：athrow（手动抛出能看到athrow指令，自动抛出字节码看不到，运行时才会有。抛出来的是一个对象，所以是a

**过程二** ：`异常的处理`：抓抛模型 try-catch-finally ---> 使用异常表

### 2.9.2. 异常处理与异常表

**1、处理异常**

在 Java 虚拟机中，**处理异常(catch 语句)不是由字节码指令来实现的(早期使用 jsr、ret 指令)，而是采用异常表来完成的**。

**2、异常表**

`如果一个方法定义了一个 try-catch 或者 try-finally 的异常处理，就会创建一个异常表`。它包含了每个异常处理或者 finally 块的信息。异常表保存了每个异常处理信息。比如

- 起始位置
- 结束位置
- 程序计数器记录的代码处理的偏移地址
- 被捕获的异常类在常量池中的索引

`当一个异常被抛出时，JVM 会在当前的方法里寻找一个匹配的处理，如果没有找到，这个方法会强制结束并弹出当前栈帧，并且异常会重新抛给上层调用的方法(在调用方法栈帧)`。如果在所有栈帧弹出前仍然没有找到合适的异常处理，这个线程将终止。如果这个异常在最后一个非守护线程里抛出，将会导致 JVM 自己终止，比如这个线程是个 main 线程。（所以异常一定要处理，尽量不要直接抛给JVM）

`不管什么时候抛出异常，如果异常处理最终匹配了所有异常类型，代码就会继续执行`。在这种情况下， 如果方法结束后没有抛出异常，仍然执行 finally 块，在 return 前，它直接跳到 finally 块来完成目标。

例子：

```java
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
 
public class _16_ExceptionTest {
    public void throwZero(int i){
        if(i == 0){
            throw new RuntimeException("参数值为0");
        }
    }
    public void throwOne(int i) throws RuntimeException, IOException {
        if(i == 1){
            throw new RuntimeException("参数值为1");
        }
    }
    public void throwArithmetic() {
        int i = 10;
        int j = i / 0;
        System.out.println(j);
    }
 
    public void tryCatch(){
        try{
            File file = new File("d:/hello.txt");
            FileInputStream fis = new FileInputStream(file);
 
            String info = "hello!";
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(RuntimeException e){
            e.printStackTrace();
        }
    }
 
    //思考：如下方法返回结果为多少？
    public static String func() {
        String str = "hello";
        try{
            return str;
        }
        finally{
            str = "atguigu";
        }
    }
 
    public static void main(String[] args) {
 
        System.out.println(func());//hello
    }
}
 
```

![image-20210116112812421](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222736154-1535735450.png)

**抛出异常的情况（原来如此）**

![image-20210116113128425](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222735767-122876890.png)

**没有Exceptions属性，异常表没有任何信息**

![image-20210116113221253](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222735427-204960010.png)

**方法抛出异常的情况**

![image-20210116113727051](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222735153-972129935.png)

![image-20210116113744730](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222734931-2056914116.png)

![image-20210116113813539](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222734709-1876592098.png)

**Code属性刻画的是方法体，而throws是在方法的声明处，throws是刻画方法的，相当于另外的属性，所以跟Code属性并列的，不会放到Code属性里面（即不会在字节码指令体现）。**

**运行时系统抛出的异常，字节码指令是看不到athrow的**

![image-20210116114154945](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222734472-343505824.png)

![image-20210116114259001](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222734201-377612178.png)

**try-catch**

![image-20210116114910254](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222733916-606056205.png)

![image-20210116181449716](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222733530-1901994416.png)

![image-20210116182427199](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222733150-1297323650.png)

**这就很清楚为什么try-catch之后能保证程序不会异常退出或程序崩溃，还能健壮的运行。**

**思考：**

![image-20210116183913187](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222732834-1090618850.png)

![image-20210116183953869](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222732602-469429936.png)

**从字节码来看**

![image-20210116184437734](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222732372-787761337.png)

**不出现异常的时候，怎么执行的finally**

![image-20210116191328863](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222732107-1827479532.png)

返回的是str的复制品，最初的str已经被修改了，后面可以有个例子：如果修改的不是str而是对象的值，那么return后的结果是finally中修改过的，而这里只是复制了对象的地址，所以值还是原来hello的值。

**异常的时候执行的finally**

![image-20210116191730987](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222731752-794734172.png)

**例子：如果finally修改的是对象的值**

```java
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
 
public class _16_ExceptionTest2 {
 
    //思考：如下方法返回结果为多少？
    public static String[] func() {
        String[] str = {"hello"}; // 数组对象
        try{
            return str;
        }
        finally{
            str[0] = "atguigu"; // 修改数组对象的值
        }
    }
 
    public static void main(String[] args) {
 
        System.out.println(func()[0]);// atguigu
    }
}
```

![image-20210116192334081](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222731436-1856758909.png)

---

## 2.10. 同步控制指令

Java 虚拟机支持两种同步结构：**`方法级同步`** 和 **`方法内部一段指令序列的同步（同步代码块）`**，这两种同步都是使用 monitor （监听器）来支持的。

### 2.10.1. 方法级的同步

方法级的同步：**`是隐式的`**，即无需通过字节码指令来控制，它实现在方法调用和返回操作之中。虚拟机可以从方法常量池的方法表结构中的 **`ACC_SYNCHRONIZED`** 访问标志得知一个方法是否声明为同步方法

当调用方法时，调用指令将会检查方法的 ACC_SYNCHRONIZED 访问标志是否设置。

- 如果设置了，执行线程将先持有同步锁，然后执行方法，最后在方法完成(无论是正常完成还是非正常完成)时释放同步锁。
- 在方法执行期间，执行线程持有了同步锁，其它任何线程都无法再获得同一个锁
- 如果一个同步方法执行期间抛出了异常，并且在方法内部无法处理此异常，那么这个同步方法所持有的锁将在异常抛到同步方法之外时自动释放

**ACC_SYNCHRONIZED是方法的访问标识，前面讲过。**

![image-20210116210348836](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222731117-1592456865.png)

**举例：**

```java
private int i = 0;
 
public synchronized void add() {
  i++;
}
```

**对应字节码：（字节码看不到synchronized关键字）**

```java
0 aload_0
1 dup
2 getdield #2 <com/atguigu/java1/SynchronizedTest.i>
5 iconst_1
6 iadd
7 putfield #2 <com/atguigu/java1/SynchronizedTest.i>
10 return
```

**说明：**

这段代码和普通的无同步操作的代码没有什么不同，没有使用 monitorenter（加锁） 和 monitorexit （释放锁）进行同步区控制。这是因为，对于同步方法而言，当`虚拟机通过方法的访问标识符判断是一个同步方法时，会自动在方法调用前进行加锁，当同步方法执行完毕后，不管方法是正常结束还是有异常抛出，均会由虚拟机释放这个锁`。因此，对于**`同步方法而言，monitorenter 和 monitorexit 指令是隐式存在的，并未直接出现在字节码中`**。

**例子：**

```java
package _10;
 
public class _17_SynchronizedTest {
    private int i = 0;
    public synchronized void add(){
        i++;
    }
}
```

![image-20210116222123444](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222730885-112857126.png)

![image-20210116223451997](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222730648-147839575.png)

### 2.10.2. 方法内指定指令序列的同步

**同步一段指令集序列：通常是由 Java 中的 synchronized 语句块来表示的。JVM 的指令集有 monitorenter 和 monitorexit 两条指令来支持 synchronized 关键字的语义。**

当一个线程进入同步代码块时，它使用 monitorenter 指令请求进入。如果当前对象的监视器计数器为0，则它会被准许进入，若为1，则判断持有当前监视器的线程是否为自己，如果是，则进入，否则进行等待，知道对象的监视器计数器为0，才会被允许进入同步块。

当线程退出同步块时，需要使用 monitorexit 声明退出。在 Java 虚拟机中，任何对象都有一个监视器与之相关联，用来判断对象是否被锁定，当监视器被持有后，对象处于锁定状态。

指令 monitorenter 和 monitorexit 在执行时，都需要在操作数栈顶压入对象，之后 monitorenter 和 monitorexit 的锁定和释放都是针对这个对象的监视器进行的。

**下图展示了监视器如何保护临界区被多个线程访问，只有当线程4离开临界区后，线程1、2、3才有可能进入。**

![image-20210116222409892](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222730384-507958467.png)

**锁的状态标志**

![image-20210116223554251](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222730034-588774133.png)

编译器必须确保无论方法通过何种方式完成，方法中调用过的每条 monitorenter 指令都必须执行其对应的 monitorexit 指令，而无论这个方法是正常结束还是异常结束。

为了保证在方法异常完成时 monitorenter 和 monitorexit 指令依然可以正确配对执行，编译器会自动产生一个异常处理器，这个异常处理器声明可处理所有的异常，它的目的就是用来执行 monitorexit 指令。

**例子：**

```java
public class _17_SynchronizedTest2 {
    private int i = 0;
    private Object obj = new Object();
    public void subtract(){
        synchronized (obj){
            i--;
        }
    }
}
```

**这种方式能看到monitorenter 和 monitorexit**

![image-20210116223347995](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222729734-486107146.png)

![image-20210117103448479](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222729336-904955168.png)

**官网对\*monitorenter\*的解释**

![image-20210117103727530](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222728975-1914791210.png)

**代码层面的优化可以参考字节码指令来进行优化。**

# 3. 类的加载过程详解

类的加载 所指向的类 是 类、接口、enum等。

![image-20210117153956542](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222728654-726464855.png)

---

## 3.1. 概述

在 Java 中数据类型分为基本数据类型和引用数据类型。`基本数据类型由虚拟机预先定义，引用数据类型则需要进行类的加载。`

按照 Java 虚拟机规范，从 Class 文件到加载到内存中的类，到类卸载出内存位置，它的整个生命周期包括如下7个阶段：

![image-20210117155419246](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222728362-269213889.png)

**其中，`验证、准备、解析3个部分统称为链接(Linking)`，就可以划分为5个阶段。**

**从程序中类的使用过程看：**

![image-20210117160050106](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222727975-1548980013.png)

---

## 3.2. 过程一：Loading(加载)阶段

类的加载是广义的加载，这里的是狭义的加载。

### 3.2.1. 加载完成的操作

加载的理解：

所谓加载，简而言之就是**`将 Java 类的字节码文件加载到机器内存中，并在内存中构建出 Java 类的原型-- 类模板对象`**。**所谓`类模板对象`，其实就是 Java 类在 JVM 内存中的一个快照，JVM 将从字节码文件中解析出的常量池、类字段、类方法等信息存储到模板中，这样 JVM 在运行期便能通过类模板而获取 Java 类中的任意信息（反射），能够对 Java 类的成员变量进行遍历，也能进行 Java 方法的调用。**

反射的机制即基于这一基础。如果 JVM 没有将 Java 类的声明信息存储起来，则 JVM 在运行期也无法反射。

加载完成的操作：

```
加载阶段，简言之，查找并加载类的二进制数据，生成 Class 的实例。
```

**在加载类时，Java 虚拟机必须完成以下3件事情：**

- 通过类的全名，获取类的二进制数据流

  ![image-20210117162234004](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222727631-1074867310.png)

- 解析类的二进制数据流为方法区内的数据结构(Java 类模型)

- 创建 java.lang.Class 类的实例（在堆中），表示该类型。作为方法区这个类的各种数据的访问入口

### 3.2.2. 二进制流的获取方式

**对于类的二进制数据流，虚拟机可以通过多种途径产生或获得。**(只要所读取的字节码符合 JVM 规范即可)

- 虚拟机可能通过文件系统读入一个 Class 后缀的文件(最常见)
- 读入 jar、zip 等归档数据包，提取类文件
- 事先存放在数据库中的类的二进制数据
- 使用类似于 HTTP 之类的协议通过网络进行加载
- 在运行时生成一段 Class 的二进制信息等

在获取到类的二进制信息后，Java 虚拟机就会处理这些数据，并最终转为一个 java.lang.Class 的实例。

如果输入数据不是 ClassFile 的结构，则会抛出 ClassFormatError。

### 3.2.3. 类模型与Class实例的位置

1、**类模型的位置**

加载的类在 JVM 中创建相应的类结构，类结构会存储在方法区(JDK 1.8之前：永久代；JDK 1.8之后：元空间)。

2、**Class实例的位置**

将 .class 文件加载至元空间后，会在堆中创建一个 java.lang.Class 对象，用来封装类位于方法区内的数据结构，该 Class 对象是在加载类的过程中创建的，每个类都对应有一个 Class 类型的对象。

3、图示

![image-20210117162554601](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222727330-1106353132.png)

**外部可以通过访问代表 Order 类的 Class 对象来获取 Order 的类数据结构。**

4、再说明

Class 类的构造方法是私有的，只有 JVM 能够创建。

java.lang.Class 实例是访问类型元数据的接口，也是实现反射的关键数据、入口。通过 Class 类提供的接口，可以获得目标类所关联的 .class 文件中具体的数据结构：方法、字段等信息。

**（理解Class文件之后再看反射就很清楚了。）**

**例子：**

```java
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
 
/**
 *  过程一：加载阶段
 *
 *  通过Class类，获得了java.lang.String类的所有方法信息，并打印方法访问标识符、描述符
 */
public class _18_LoadingTest {
    public static void main(String[] args) {
        try {
            Class clazz = Class.forName("java.lang.String");
            //获取当前运行时类声明的所有方法
            Method[] ms = clazz.getDeclaredMethods();
            for (Method m : ms) {
                //获取方法的修饰符
                String mod = Modifier.toString(m.getModifiers());
                System.out.print(mod + " ");
                //获取方法的返回值类型
                String returnType = m.getReturnType().getSimpleName();
                System.out.print(returnType + " ");
                //获取方法名
                System.out.print(m.getName() + "(");
                //获取方法的参数列表
                Class<?>[] ps = m.getParameterTypes();
                if (ps.length == 0) System.out.print(')');
                for (int i = 0; i < ps.length; i++) {
                    char end = (i == ps.length - 1) ? ')' : ',';
                    //获取参数的类型
                    System.out.print(ps[i].getSimpleName() + end);
                }
                System.out.println();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
 
    }
}
 
```

**运行输出结果（String类的所有方法）**

```java
public boolean equals(Object)
public String toString()
public int hashCode()
public int compareTo(String)
public volatile int compareTo(Object)
public int indexOf(String,int)
public int indexOf(String)
public int indexOf(int,int)
public int indexOf(int)
static int indexOf(char[],int,int,char[],int,int,int)
static int indexOf(char[],int,int,String,int)
public static String valueOf(int)
public static String valueOf(long)
public static String valueOf(float)
public static String valueOf(boolean)
public static String valueOf(char[])
public static String valueOf(char[],int,int)
public static String valueOf(Object)
public static String valueOf(char)
public static String valueOf(double)
public char charAt(int)
private static void checkBounds(byte[],int,int)
public int codePointAt(int)
public int codePointBefore(int)
public int codePointCount(int,int)
public int compareToIgnoreCase(String)
public String concat(String)
public boolean contains(CharSequence)
public boolean contentEquals(CharSequence)
public boolean contentEquals(StringBuffer)
public static String copyValueOf(char[])
public static String copyValueOf(char[],int,int)
public boolean endsWith(String)
public boolean equalsIgnoreCase(String)
public static transient String format(Locale,String,Object[])
public static transient String format(String,Object[])
public void getBytes(int,int,byte[],int)
public byte[] getBytes(Charset)
public byte[] getBytes(String)
public byte[] getBytes()
public void getChars(int,int,char[],int)
 void getChars(char[],int)
private int indexOfSupplementary(int,int)
public native String intern()
public boolean isEmpty()
public static transient String join(CharSequence,CharSequence[])
public static String join(CharSequence,Iterable)
public int lastIndexOf(int)
public int lastIndexOf(String)
static int lastIndexOf(char[],int,int,String,int)
public int lastIndexOf(String,int)
public int lastIndexOf(int,int)
static int lastIndexOf(char[],int,int,char[],int,int,int)
private int lastIndexOfSupplementary(int,int)
public int length()
public boolean matches(String)
private boolean nonSyncContentEquals(AbstractStringBuilder)
public int offsetByCodePoints(int,int)
public boolean regionMatches(int,String,int,int)
public boolean regionMatches(boolean,int,String,int,int)
public String replace(char,char)
public String replace(CharSequence,CharSequence)
public String replaceAll(String,String)
public String replaceFirst(String,String)
public String[] split(String)
public String[] split(String,int)
public boolean startsWith(String,int)
public boolean startsWith(String)
public CharSequence subSequence(int,int)
public String substring(int)
public String substring(int,int)
public char[] toCharArray()
public String toLowerCase(Locale)
public String toLowerCase()
public String toUpperCase()
public String toUpperCase(Locale)
public String trim()
```

**分析**

![image-20210117165721204](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222727044-1162537190.png)

**没有创建String实例，只要String类加载到了内存中就可以通过反射获取到该类的信息。**

### 3.2.4. 数组类型的加载

创建数组类的情况稍微有些特殊，因为`数组类本身并不是由类加载器负责创建`，而是由 JVM 在运行时根据需要而直接创建的，但数组的元素类型仍然需要依靠类加载器去创建。创建数组类(下述简称 A)的过程：

1. 如果数组的元素类型是引用类型，那么就遵循定义的加载过程递归加载和创建数组 A 的元素类型。（引用类型数组比如String[]，要加载String类。如果是int[]则不需要类加载，JVM直接创建即可，因为基本数据类型由虚拟机预先定义）
2. JVM 使用指定的元素类型和数组维度来创建新的数组类。

如果数组的元素类型是引用类型，数组类的可访问性就由元素类型的可访问性决定。否则数组类的可访问性将被缺省定义为 public。

---

## 3.3. 过程二：Linking(链接)阶段

### 3.3.1. 环节1：链接阶段之Verification(验证)

当类加载到系统后，就开始链接操作，验证是链接操作的第一步。

**它的目的是`保证加载的字节码是合法、合理并符合规范的。`**

验证的步骤比较复杂，实际要验证的项目也很繁多，大体上 Java 虚拟机需要做以下检查，如图所示。

![image-20210117170545243](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222726715-1900008436.png)

整体说明：

**验证的内容则涵盖了类数据信息的`格式验证`、`语义检查`、`字节码验证`，以及`符号引用验证`等。**

- 其中格式验证会和加载阶段一起执行（只是归结的时候归结到链接这里）。验证通过之后，类加载器才会成功将类的二进制数据信息加载到方法区中。
- 格式验证之外的验证操作将会在方法区中进行。（即上图中“语义检查”、“字节码验证”、“符号引用验证”是加载后在方法区中进行）

链接阶段的验证虽然拖慢了加载速度，但是它避免了在字节码运行时还需要进行各种检查。（磨刀不误砍柴工）

具体说明：

1、**格式验证**：是否以魔数 0xCAFEBABE 开头，主版本和副版本号是否在当前 Java 虚拟机的支持范围内，数据中每一个项是否都拥有正确的长度等。

2、Java 虚拟机会进行字节码的**语义检查**，但凡在语义上不符合规范的，虚拟机也不会给予验证通过。比如：

- 是否所有的类都有父类的存在(在 Java 里，除了 Object 外，其他类都应该有父类)
- 是否一些被定义为 final 的方法或者类被重写或继承了
- 非抽象类是否实现了所有抽象方法或者接口方法
- 是否存在不兼容的方法(比如方法的签名除了返回值不同，其他都一样，这种方法会让虚拟机无从下手调度；absract 情况下的方法，就不能是final 的了)

3、Java 虚拟机还会进行**字节码验证**，字节码验证也是验证过程中最为复杂的一个过程。它试图通过对字节码流的分析，判断字节码是否可以被正确地执行。比如：

- 在字节码的执行过程中，是否会跳转到一条不存在的指令
- 函数的调用是否传递了正确类型的参数
- 变量的赋值是不是给了正确的数据类型等

栈映射帧(StackMapTable)就是在这个阶段，用于检测在特定的字节码处，其局部变量表和操作数栈是否有着正确的数据类型。但遗憾的是，100%准确地判断一段字节码是否可以被安全执行是无法实现的，因此，该过程只是尽可能地检查出可以预知的明显的问题。如果在这个阶段无法通过检查，虚拟机也不会正确装载这个类。但是，如果通过了这个阶段的检查，也不能说明这个类是完全没有问题的。

![image-20210117173145164](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222726425-1104764452.png)

在前面3次检查中，已经排除了文件格式错误、语义错误以及字节码的不正确性。但是依然不能确保类是没有问题的。

4、校验器还将进行符号引用的验证。Class 文件在其常量池会通过字符串记录自己将要使用的类或者方法。因此，在验证阶段，虚拟机就会检查这些类或者方法确实是存在的，并且当前类有权限访问这些数据，如果一个需要使用类无法在系统中找到，则会抛出 NoClassDefFoundError，如果一个方法无法被找到，则会抛出 NoSuchMethdError。（开发中一般是jar版本不对或jar冲突导致的这两种Error）

此阶段在解析环节才会执行。（只是归结到链接阶段）

### 3.3.2. 环节2：链接阶段之Preparation(准备)

准备阶段(Preparation)，简言之，****。

当一个类验证通过时，虚拟机就会进入准备阶段。在这个阶段，虚拟机就会为这个类分配相应的内存空间，并设置默认初始值。

Java 虚拟机为各类型变量默认的初始值如表所示：

![image-20210117174400636](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222726125-1674910150.png)

**注意：Java 并不支持 boolean 类型，对于 boolean 类型，内部实现是 int，由于 int 的默认值是0，故对应的，boolean 的默认值就是 false，boolean为true就是1。**

**注意：**

1、这里不包含基本数据类型的字段用 static final（这是常量，不是变量） 修饰的情况，因为`final 在编译的时候就会分配了，准备阶段会显式赋值，如果使用字面量的方式定义一个字符串的常量的话，也是在准备环节直接进行显示赋值。`

2、注意这里不会为实例变量分配初始化，类变量会分配在方法区中，而实例变量是会随着对象一起分配到 Java 堆中。

3、在这个阶段不会像初始化阶段中那样会有初始化或者代码被执行。

**例子：**

```java
/**
 * 过程二：链接阶段
 *
 * 基本数据类型（静态的）：非final修饰的变量，在准备环节进行默认初始化赋值。
 *                       final修饰以后，在准备环节直接进行显示赋值。
 *
 *  拓展：如果使用字面量的方式定义一个字符串的常量的话，也是在准备环节直接进行显示赋值。
 */
public class _19_LinkingTest {
    private static long id;
    private static final int num = 1;
 
    public static final String constStr = "CONST";
    public static final String constStr1 = new String("CONST");
}
```

**分析**

![image-20210117190330377](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222725838-1432439666.png)

![image-20210117220744594](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222725541-388452927.png)

### 3.3.3. 环节3：链接阶段之Resolution(解析)

在准备阶段完成后，就进入了解析阶段。

解析阶段(Resolution)，简言之，**`将类、接口、字段和方法的符号引用转为直接引用。`**

1、具体描述：

符号引用就是一些字面量的引用，和虚拟机的内部数据结构和内存布局无关。比较容理解的就是在 Class 类文件中，通过常量池进行了大量的符号引用。但是在程序实际运行时，只有符号引用是不够的，比如当如下 println() 方法被调用时，系统需要明确知道该方法的位置。

举例：输出操作 System.out.println() 对应的字节码：

```bash
invokevirtual #24 <java/io/PrintStream.println>
```

# 24是符号引用，指向的是println()方法，而println()方法在内存中的真实地址就是符号引用转直接引用（直接引用就是内存中的地址）。

![image-20210117221312986](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222725253-1971434285.png)

以方法为例，Java 虚拟机为每个类都准备了一张方法表，将其所有的方法都列在表中，当需要调用一个类的方法的时候，只要知道这个方法在方法表中的偏移量就可以直接调用该方法。通过解析操作，符号引用就可以转变为目标方法在类中方法表中的位置，从而使得方法被成功调用。

打个比方：查了地图要去某个地方，在地图上显示要先做1号线，然后在某个站点转2号线才能到达，这就是符号引用。而直接引用是真正去坐车去那个地方，必须去到固定站点，找到对应的列车，才能坐到目的地。

2、小结

所谓解析就是将符号引用转为直接引用，也就是得到类、字段、方法在内存中的指针或者偏移量。因此，可以说，如果直接引用存在，那么可以肯定系统中存在该类、方法或者字段。但只存在符号引用，不能确定系统中一定存在该结构。

不过 Java 虚拟机规范并没有明确要求解析阶段一定要按照顺序执行。在 HotSpot VM 中，加载、验证、准备和初始化会按照顺序有条不紊地执行，但链接阶段中的解析操作往往会伴随着 JVM 在执行完初始化之后再执行。

3、字符串的复习

最后，再来看一下 CONSTANT_String 的解析。由于字符串在程序开发中有着重要的作用，因此，读者有必要了解一下 String 在 Java 虚拟机中的处理。当在 Java 代码中直接使用字符串常量时，就会在类中出现 CONSTANT_String，它表示字符串常量，并且会引用一个 CONSTANT_UTF8 的常量项。在 Java 虚拟机内部运行中的常量池，会维护一张字符串拘留表(intern)（字符串常量池），它会保存所有出现过的字符串常量，并且没有重复项。只要以 CONSTANT_String 形式出现的字符串也都会在这张表中。使用 String.intern() 方法可以得到一个字符串在拘留表中的引用，因为该表中没有重复项，所以任何字面相同的字符串的 String.intern() 方法返回总是相等的。

---

## 3.4. 过程三：Initialization(初始化)阶段

初始化阶段，简言之，**`为类的静态变量赋予正确的初始值（类变量显示赋值）`**。

**1、具体描述**

类的初始化是类装载的最后一个阶段。如果前面的步骤都没有问题，那么表示类可以顺利装载到系统中。此时，类才会开始执行 Java 字节码。(即：到了初始化阶段，才真正开始执行类中定义的 Java 程序代码。)

**初始化阶段的重要工作是执行类的初始化方法：`clinit() 方法`。（初始化方法是clinit()并不是构造方法）**

- 该方法仅能由 Java 编译器生成并由 JVM 调用，程序开发者无法自定义一个同名的方法，更无法直接在 Java 程序中调用该方法，虽然该方法也是由字节码指令所组成。
- 它会把类静态成员的赋值语句以及 static 语句块合并一起，然后执行。

**2、说明**

2.1、在加载一个类之前，虚拟机总是会试图加载该类的父类，因此父类的clinit()总是在子类clinit()之前被调用.也就是说，父类的 static 块优先级高于子类.

2.2、`Java 编译器并不会为所有的类都产生clinit()初始化方法。哪些类在编译为字节码后，字节码文件中将不会包含 clinit() 方法？`

- 一个类中并没有声明任何的类变量（静态变量），也没有静态代码块时
- 一个类中声明类变量，但是没有明确使用类变量的初始化语句以及静态代码块来执行初始化操作时
- 一个类中包含 static final 修饰的基本数据类型的字段，这些类字段初始化语句采用编译时常量表达式

**例子1：**

```java
public class _20_InitializationTest1 {
 
    public static int id = 1; // 显示赋值（clinit()方法内执行）
    public static int number;
 
    // 静态代码块在初始化阶段执行（执行clinit()方法）
    static {
        number = 2;
        System.out.println("father static{}");
    }
}
 
```

![image-20210121081901019](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222724922-14269412.png)

**例子2：继承上一个例子的_20_InitializationTest1**

```java
public class _21_SubInitialization extends _20_InitializationTest1 {
    static{
        number = 4;//number属性必须提前已经加载：一定会先加载父类。
        System.out.println("son static{}");
    }
 
    public static void main(String[] args) {
        System.out.println(number);
    }
}
 
```

**输出结果**

```cpp
father static{}
son static{}
4
```

**例子3：**

```java
/**
 * 哪些场景下，java编译器就不会生成<clinit>()方法
 */
public class _22_InitializationTest2 {
    //场景1：对应非静态的字段，不管是否进行了显式赋值，都不会生成<clinit>()方法
    public int num = 1;
    //场景2：静态的字段，没有显式的赋值，不会生成<clinit>()方法
    public static int num1;
    //场景3：比如对于声明为static final的基本数据类型的字段，不管是否进行了显式赋值，都不会生成<clinit>()方法
    public static final int num2 = 1;
}
 
```

![image-20210122075154221](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222724673-674312748.png)

**去掉final，对比**

![image-20210122075656136](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222724381-1695818438.png)

**总结**

![image-20210122081239460](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222723990-417062448.png)

### 3.4.1. static与final的搭配问题

```java
import java.util.Random;
 
/**
 * 说明：使用static + final修饰的字段的显式赋值的操作，到底是在哪个阶段进行的赋值？
 * 情况1：在链接阶段的准备环节赋值
 * 情况2：在初始化阶段<clinit>()中赋值
 *
 * 结论：
 * 在链接阶段的准备环节赋值的情况：
 * 1. 对于基本数据类型的字段来说，如果使用static final修饰，则显式赋值(直接赋值常量，而非调用方法）通常是在链接阶段的准备环节进行
 * 2. 对于String来说，如果使用字面量的方式赋值，使用static final修饰的话，则显式赋值通常是在链接阶段的准备环节进行
 *
 * 在初始化阶段<clinit>()中赋值的情况：
 * 排除上述的在准备环节赋值的情况之外的情况。（并不是只是根据final来区分）
 * （上述说的还有一点不够准确，比如：public static final int NUM1 = new Random().nextInt(10);）
 *
 * 最终结论：使用static + final修饰，且显示赋值中不涉及到方法或构造器调用的基本数据类型或String类型的显式赋值，是在链接阶段的准备环节进行；
 * 除此之外都在初始化阶段进行，这很适合跟面试官对线。
 */
public class _23_InitializationTest3 {
    //在初始化阶段<clinit>()中赋值
    public static int a = 1;
    //在链接阶段的准备环节赋值
    public static final int INT_CONSTANT = 10;
 
    //在初始化阶段<clinit>()中赋值
    public static final Integer INTEGER_CONSTANT1 = Integer.valueOf(100);
    //在初始化阶段<clinit>()中赋值
    public static Integer INTEGER_CONSTANT2 = Integer.valueOf(1000);
 
    //在链接阶段的准备环节赋值（字面量的方式声明）
    public static final String s0 = "helloworld0";
    //在初始化阶段<clinit>()中赋值（new对象的方式声明）
    public static final String s1 = new String("helloworld1");
 
    public static String s2 = "helloworld2";
 
    //在初始化阶段<clinit>()中赋值（编译之后NUM1无法确定，必须执行代码）
    public static final int NUM1 = new Random().nextInt(10);
}
 
```

![image-20210123184014122](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222723571-1427001681.png)

### 3.4.2. `<clinit>()的线程安全性`

**对于clinit() 方法的调用，也就是类的初始化，虚拟机会在内部确保其多线程环境中的安全性。**

虚拟机会保证一个类的 clinit() 方法在多线程环境中被正确地加锁、同步，如果多个线程同时去初始化一个类，那么只会有一个线程去执行这个类的 clinit() 方法，其他线程都需要阻塞等待，直到活动线程执行 clinit() 方法完毕。

正是因为函数 clinits() 带锁线程安全的，因此，如果一个在类的 clinit() 方法中有耗时很长的操作，就可能造成多个线程阻塞，引发死锁。并且这种死锁是很难发现的，因为看起来它们并没有可用的锁信息（没有synchronized关键词，是隐式的锁）。

如果之前的线程成功加载了类，则等在队列中的线程就没有机会再执行 clinit() 方法了。那么，当需要使用这个类时，虚拟机会直接返回给它已经准备好的信息。

**例子：**

```java
/**
 * A初始化会加载B，B初始化会加载A，导致死锁
 */
public class _24_StaticDeadLockMain extends Thread {
    private char flag;
 
    public _24_StaticDeadLockMain(char flag) {
        this.flag = flag;
        this.setName("Thread" + flag);
    }
 
    @Override
    public void run() {
        try {
            Class.forName("_10.Static" + flag);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(getName() + " over");
    }
 
    public static void main(String[] args) throws InterruptedException {
        _24_StaticDeadLockMain loadA = new _24_StaticDeadLockMain('A');
        loadA.start();
        _24_StaticDeadLockMain loadB = new _24_StaticDeadLockMain('B');
        loadB.start();
    }
}
 
class StaticA {
    static {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        try {
            Class.forName("_10.StaticB");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("StaticA init OK");
    }
}
class StaticB {
    static {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        try {
            Class.forName("_10.StaticA");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("StaticB init OK");
    }
}
```

**死锁导致没有任何输出**

![image-20210123185334419](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222723274-1673436448.png)

### 3.4.3. 类的初始化情况：主动使用 vs 被动使用

**Java 程序对类的使用分为两种：`主动使用` 和 `被动使用`。主动使用会调用()方法，被动使用不会调用，即主动使用才会有初始化。**

#### 3.4.3.1. 主动使用

Class 只有在首次使用的时候才会被装载，Java 虚拟机不会无条件地装载 Class 类型。Java 虚拟机规定，一个类或接口在初次使用前，必须要进行初始化。这里指的"使用"，是指主动使用，主动使用只有下列几种情况：(即：如果出现如下的情况，则会对类进行初始化操作。而初始化操作之前的加载、验证、准备都已经完成。)

1. 当创建一个类的实例时，比如使用 new 关键字，或者通过反射、克隆、反序列化
2. 当调用类的静态方法时，即当使用了字节码 invokestatic 指令
3. 当使用类、接口的静态字段时(final 修饰特殊考虑)，比如，使用 getstatic 或者 putsttic 指令。(对应访问变量、赋值变量操作)
4. 当使用 java.lang.reflect 包中的方法反射类的方法时。比如：Class.forname("com.atguigu.java.Test")
5. 当初始化子类时，如果发现其父类还没有进行过初始化，则需要先触发其父类的初始化
6. 如果一个接口定义了 default 方法，那么直接实现或者间接实现该接口的类的初始化，该接口要在其之前被初始化
7. 当虚拟机启动时，用户需要指定一个要执行的主类(包含 main() 方法的那个类)，虚拟机会先初始化这个主类
8. 当初次调用 MethodHandle 实例时，初始化该 MethodHandle 指向的方法所在的类。(涉及解析 REF_getStatic、REF_putStatic、REF_invokeStatic 方法句柄对应的类)

针对上述第5点，补充说明：

当 Java 虚拟机初始化一个类时，要求它的所有父类都已经被初始化，但是这条规则并不适用于接口

- 在初始化一个类时，并不会先初始化它所实现的接口
- 在初始化一个接口时，并不会先初始化它的父接口

因此，一个父接口并不会因为它的子接口或者实现类的初始化而初始化，只有当程序首次使用特定接口的静态字段时，才会导致该接口的初始化。

针对上述第7点，说明：

JVM 启动的时候通过引导类加载器加载一个初始类。这个类在调用 public static void main(String[]) 方法之前被链接和初始化。这个方法的执行将依次导致所需的类的加载、链接和初始化。

**例子1：**

```java
import org.junit.Test;
 
import java.io.*;
 
/**
 * 测试类的主动使用：意味着会调用类的<clinit>()，即执行了类的初始化阶段
 *
 * 1. 当创建一个类的实例时，比如使用new关键字，或者通过反射、克隆、反序列化。
 * 2. 当调用类的静态方法时，即当使用了字节码invokestatic指令。
 */
public class _25_ActiveUse1 {
 
    // new一个类的实例
    @Test
    public void test0() {
        Order order = new Order();
    }
 
    //序列化的过程：
    @Test
    public void test1() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream("order.dat"));
 
            oos.writeObject(new Order());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null)
                    oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
 
    //反序列化的过程：（验证）
    @Test
    public void test2() {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream("order.dat"));
 
            Order order = (Order) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ois != null)
                    ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
 
    // 调用类的静态方法会调用clinit方法
    @Test
    public void test3(){
        Order.method();
    }
}
 
// Serializable只是标识接口，没有任何抽象方法
class Order implements Serializable {
    static {
        System.out.println("Order类的初始化过程");
    }
 
    public static void method(){
        System.out.println("Order method()....");
    }
}
 
```

**Order类的clinit方法**

![image-20210124100811384](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222722772-1483917935.png)

![image-20210124101400618](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222722471-1338500149.png)

![image-20210124101845825](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222722073-1753909451.png)

**例子2：**

```java
import org.junit.Test;
 
import java.util.Random;
 
/**
 * 3. 当使用类、接口的静态字段时(final修饰特殊考虑)，比如，使用getstatic或者putstatic指令。（对应访问变量、赋值变量操作）
 *
 */
public class _26_ActiveUse2 {
 
    @Test
    public void test1(){
        System.out.println(User.num); // 会初始化
//        System.out.println(User.num1); // 不会初始化
//        System.out.println(User.num2); // 会初始化
    }
}
 
class User{
    static{
        System.out.println("User类的初始化过程");
    }
 
    // 初始化阶段那赋值
    public static int num = 1;
    // 准备阶段就赋值好了
    public static final int num1 = 1;
    // 初始化阶段才赋值
    public static final int num2 = new Random().nextInt(10);
}
 
```

![image-20210124105930250](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222721790-1834635271.png)

![image-20210124110024454](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222721556-2091376309.png)

![image-20210124110040369](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222710011-98737841.png)

**例子3：**

```java
import org.junit.Test;
 
import java.util.Random;
 
/**
 * 3. 当使用类、接口的静态字段时(final修饰特殊考虑)，比如，使用getstatic或者putstatic指令。（对应访问变量、赋值变量操作）
 *
 */
public class _27_ActiveUse3 {
    @Test
    public void test1(){
        System.out.println(CompareA.NUM1);
//        System.out.println(CompareA.NUM2);
    }
}
 
interface CompareA{
 
    // 接口不能执行静态代码块，但静态变量使用代码的方式赋值会在clinit执行
    public static final Thread t = new Thread(){
        {
            // 这句话输出了就说明接口初始化了
            System.out.println("CompareA的初始化");
        }
    };
 
    public static final int NUM1 = 1;
    public static final int NUM2 = new Random().nextInt(10);
 
}
 
```

![image-20210124110945723](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222709673-1778013780.png)

![image-20210124111054386](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222709236-803837162.png)

![image-20210124111110655](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222708934-1942769717.png)

**例子4：**

```java
//4. 当使用java.lang.reflect包中的方法反射类的方法时。比如：Class.forName("com.atguigu.java.Test")
class Order implements Serializable{
    static {
        System.out.println("Order类的初始化过程");
    }
 
    public static void method(){
        System.out.println("Order method()....");
    }
} 
public class ActiveUse3 {
    @Test
	public void test1() {
    	try {
        	Class clazz = Class.forName("com.atguigu.java1.Order");
   	 	} catch (ClassNotFoundException e) {
        	e.printStackTrace();
   	 	}
	}
}
 
```

运行结果：

![image-20220424182226456](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222708565-528095291.png)

例5：当初始化子类时，如果发现其父类还没有进行过初始化，则需要先触发其父类的初始化。

```java
//当初始化子类时，如果发现其父类还没有进行过初始化，则需要先触发其父类的初始化。
class Father {
    static {
        System.out.println("Father类的初始化过程");
    }
}
 
class Son extends Father {
    static {
        System.out.println("Son类的初始化过程");
    }
 
    public static int num = 1;
}
 
public class ActiveUse3 {
    @Test
    public void test2() {
        System.out.println(Son.num);
    }    
}
```

运行结果

![image-20220424182622605](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222708255-1683529154.png)

例6：

```java
/*
 * 当Java虚拟机初始化一个类时，要求它的所有父类都已经被初始化，但是这条规则并不适用于接口。
 * >在初始化一个类时，并不会先初始化它所实现的接口
 * >在初始化一个接口时，并不会先初始化它的父接口
 * 因此，一个父接口并不会因为它的子接口或者实现类的初始化而初始化。只有当程序首次使用特定接口的静态字段时，才会导致该接口的初始化。
 *
*/
 
 
 
//在初始化一个类时，并不会先初始化它所实现的接口
interface CompareB {
    public static final Thread t = new Thread() {
        {
            System.out.println("CompareB的初始化");
        }
    };
}
class Father {
    static {
        System.out.println("Father类的初始化过程");
    }
}
 
class Son extends Father implements CompareB{
    static {
        System.out.println("Son类的初始化过程");
    }
 
    public static  int num = 1;
}
 
 
public class ActiveUse3 {
    @Test
    public void test2() {
        System.out.println(Son.num);
    }
}
```

运行结果：

![image-20220424185113426](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222707977-1245840107.png)

例7

```java
/*
 * 当Java虚拟机初始化一个类时，要求它的所有父类都已经被初始化，但是这条规则并不适用于接口。
 * >在初始化一个类时，并不会先初始化它所实现的接口(没有default修饰的方法的接口)
 * >在初始化一个接口时，并不会先初始化它的父接口
 * 因此，一个父接口并不会因为它的子接口或者实现类的初始化而初始化。只有当程序首次使用特定接口的静态字段时，才会导致该接口的初始化。
 *
*/
 
 
//在初始化一个接口时，并不会先初始化它的父接口
interface CompareB {
    public static final Thread t = new Thread() {
        {
            System.out.println("CompareB的初始化");
        }
    };
   // 如果一个接口定义了default方法，那么直接实现或者间接实现该接口的类的初始化，该接口要在其之前被初始化。
    public default void method1(){
        System.out.println("你好！");
    }
 
}
 
interface CompareC extends CompareB {
    public static final Thread t = new Thread() {
        {
            System.out.println("CompareC的初始化");
        }
    };
 
//    public static final int NUM1 = new Random().nextInt();  //输出结果：先初始化CompareB,在初始化C
    public static final int NUM1 = 11;  //输出结果：11
}
 
 
public class ActiveUse3 {
    @Test
    public void test3(){
        System.out.println(CompareC.NUM1);
    }
}    
```

运行结果：

![image-20220424212844929](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222707548-241342081.png)

例8:

```java
//7. 当虚拟机启动时，用户需要指定一个要执行的主类（包含main()方法的那个类），虚拟机会先初始化这个主类。
// 8. 当初次调用 MethodHandle 实例时，初始化该 MethodHandle 指向的方法所在的类。（涉及解析REF_getStatic、REF_putStatic、REF_invokeStatic方法句柄对应的类） -- 这点不举例
public class ActiveUse3 {
    static{
        System.out.println("ActiveUse3的初始化过程");
    }
    
    public static void main(String[] args) {
        System.out.println("hello");
    }
}
```

运行结果：

![image-20220424213216226](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222706930-1493836341.png)

**设置jvm参数**

![image-20210124132245665](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222706617-926808633.png)

#### 3.4.3.2. 被动使用

除了以上的情况属于主动使用，其他的情况均属于被动使用。**`被动使用不会引起类的初始化。`**

也就是说：**`在代码中出现的类，一定会被加载但是不一定被初始化。如果不符合主动使用的条件，类就不会初始化`。**

1. 当访问一个静态字段时，只有真正声明这个字段的类才会被初始化
   - 当通过子类引用父类的静态变量，不会导致子类初始化（但子类是有加载阶段的）
2. 通过数组定义类引用，不会触发此类的初始化
3. 引用变量不会触发此类或接口的初始化。因为常量在链接阶段就已经被显式赋值了
4. 调用 ClassLoader 类的 loadClass() 方法加载一个类，并不是对类的主动使用，不会导致类的初始化

如果针对代码，设置参数 -XX:+TraceClassLoading，可以追踪类的加载信息并打印出来

**例子1：**

```java
import org.junit.Test;
 
/**
 * 关于类的被动使用，即不会进行类的初始化操作，即不会调用<clinit>()
 *
 * 1. 当访问一个静态字段时，只有真正声明这个字段的类才会被初始化。
 *     > 当通过子类引用父类的静态变量，不会导致子类初始化
 * 2. 通过数组定义类引用，不会触发此类的初始化
 *
 * 说明：没有初始化的类，不意味着没有加载！
 */
public class _29_PassiveUse1 {
 
    // 当通过子类引用父类的静态变量，不会导致子类初始化
    @Test
    public void test1(){
        System.out.println(Child.num);
    }
 
    // 通过数组定义类引用，不会触发此类的初始化
    @Test
    public void test2(){
        Parent[] parents = new Parent[10];
        System.out.println(parents.getClass());
        System.out.println(parents.getClass().getSuperclass());
 
//        parents[0] = new Parent(); // new才会初始化
//        parents[1] = new Parent(); // 再new也不会再次初始化，初始化方法只会执行一次
    }
}
 
class Parent{
    static{
        System.out.println("Parent的初始化过程");
    }
 
    public static int num = 1;
}
 
class Child extends Parent{
    static{
        System.out.println("Child的初始化过程");
    }
}
 
```

![image-20210124134053921](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222706345-980595585.png)

![image-20210124134156883](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222706068-1499077427.png)

![image-20210124134339839](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222705809-1145801608.png)

![image-20210124134433965](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222705540-141912513.png)

**例子2：**

```java
import org.junit.Test;
 
import java.util.Random;
 
/**
 *  3. 引用常量不会触发此类或接口的初始化。因为常量在链接阶段就已经被显式赋值了。
 *  4. 调用ClassLoader类的loadClass()方法加载一个类，并不是对类的主动使用，不会导致类的初始化。
 */
public class _30_PassiveUse2 {
 
    // 3. 引用常量不会触发此类或接口的初始化。因为常量在链接阶段就已经被显式赋值了。（类）
    @Test
    public void test1(){
        System.out.println(Person.NUM); // 引用常量不会触发此类或接口的初始化。
        System.out.println("----------------------------------");
        System.out.println(Person.NUM1); // 引用常量不会触发此类或接口的初始化，但是赋值需要执行代码就需要初始化
    }
 
    // 3. 引用常量不会触发此类或接口的初始化。因为常量在链接阶段就已经被显式赋值了。（接口）
    @Test
    public void test2(){
        System.out.println(SerialA.ID);
        System.out.println("----------------------------------");
        System.out.println(SerialA.ID1);
    }
 
    // 4. 调用ClassLoader类的loadClass()方法加载一个类，并不是对类的主动使用，不会导致类的初始化。
    @Test
    public void test3(){
        try {
            // 有两种方式加载类
//            Class clazz = Class.forName("_11.Person");
            Class clazz = ClassLoader.getSystemClassLoader().loadClass("_11.Person");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
 
}
 
class Person{
    static{
        System.out.println("Person类的初始化");
    }
    public static final int NUM = 1;//在链接过程的准备环节就被赋值为1了。
    public static final int NUM1 = new Random().nextInt(10);//此时的赋值操作需要在<clinit>()中执行
}
 
interface SerialA{
    // 接口字段默认是public static final
    public static final Thread t = new Thread() {
        {
            System.out.println("SerialA的初始化");
        }
    };
 
    int ID = 1;
    int ID1 = new Random().nextInt(10);//此时的赋值操作需要在<clinit>()中执行
}
 
 
```

![image-20210124135110574](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222705229-1471425032.png)

![image-20210124135134401](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222704964-799206774.png)

![image-20210124135332171](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222704657-222031047.png)

---

## 3.5. 过程四：类的Using(使用)

![image-20210120082702784](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222704299-1057564819.png)

**链接阶段的验证阶段在加载阶段同时进行，链接阶段的解析阶段在初始化阶段同时进行。**

![image-20220424215218337](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222703976-937795250.png)

任何一个类型在使用之前都必须经历过完整的加载、链接和初始化3个类加载步骤。一旦一个类型成功经历过这3个步骤之后，便“万事俱备，只欠东风”，就等着开发者使用了。

开发人员可以在程序中访问和调用它的静态类成员信息(比如：静态字段、静态方法)，或者使用 new 关键字为其创建对象实例。

---

## 3.6. 过程五：类的Unloading(卸载)

**ClassLoader**

![image-20210124225402745](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222703703-1060800177.png)

**一、类、类的加载器、类的实例之间的引用关系**

在类加载器的内部实现中，用一个 Java 集合来存放所加载类的引用。另一方面，一个 Class 对象总是会引用它的类加载器，调用 Class 对象的 getClassLoader() 方法，就能获得它的类加载器。由此可见，代表某个类的 Class 实例与其类的加载器之间为双向关联关系。

一个类的实例总是引用代表这个类的 Class 对象。在 Object 类中定义了 getClass() 方法，这个方法返回代表对象所属类的 Class 对象的引用。此外，所有的 Java 类都有一个静态属性 Class，它引用代表这个类的 Class 对象。

![image-20210124225012483](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222703381-559257605.png)

**二、类的生命周期**

当 Sample 类被加载、链接和初始化后，它的生命周期就开始了。当代表 Sample 类的 Class 对象不再被引用，即不可触及时，Class 对象就会结束生命周期，Sample 类在方法区内的数据也会被卸载，从而结束 Sample 类的生命周期。

**`一个类何时结束生命周期，取决于代表它的 Class 对象何时结束生命周期`**

三、具体例子

![image-20210124225023189](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222703022-880904884.png)

**loader1 变量和 obj 变量间接应用代表 Sample 类的 Class 对象，而 objClass 变量则直接引用它。**

![image-20210124225839805](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222702728-985843526.png)

如果程序运行过程中，将上图左侧三个引用变量都置为 null，此时 Sample 对象结束生命周期，MyClassLoader 对象结束生命周期，代表 Sample 类的 Class 对象也结束生命周期，Sample 类在方法区内的二进制数据被卸载。

当再次有需要时，会检查 Sample 类的 Class 对象是否存在，如果存在会直接使用，不再重新加载；如果不存在 Sample 类会被重新加载，在 Java 虚拟机的堆区会生成一个新的代表 Sample 类的 Class 实例(可以通过哈希码查看是否是同一个实例)。

四、类的卸载

（1）启动类加载器加载的类型在整个运行期间是不可能被卸载的(JVM 和 JSL 规范)

（2）被系统类加载器和扩展类加载器加载的类型在运行期间不太可能被卸载，因为系统类加载器实例或者扩展类的实例基本上在整个运行期间总能直接或者间接的访问的到，其达到 unreachable 的可能性极小。（要卸载的类没有使用，但其他类可能会一直访问到）

（3）被开发者自定义的类加载器实例加载的类型只有在很简单的上下文环境中才能被卸载，而且一般还要借助于强制调用虚拟机的垃圾收集功能才可以做到。可以预想，稍微复杂点的应用场景(比如：很多时候用户在开发自定义类加载器实例的时候采用缓存的策略以提高系统性能)，被加载的类型在运行期间也是几乎不太可能被卸载的(至少卸载的时间是不确定的)。

综合以上三点，一个已经加载的类型被卸载的几率很小至少被卸载的时间是不确定的。同时我们可以看的出来，开发者在开发代码时候，不应该对虚拟机的类型卸载做任何假设的前提下，来实现系统中的特定功能。

##### 3.6.1.1. 回顾：方法区的垃圾回收

方法区的垃圾收集主要回收两部分内容：**`常量池中废弃的常量和不再使用的类型。`**

HotSpot 虚拟机对常量池的回收策略是很明确的，只要常量池中的常量没有被任何地方引用，就可以被回收。

判定一个常量是否"废弃"还是相对简单，而要判定一个类型是否属于"不再被使用的类"的条件就比较苛刻了。需要同时满足下面三个条件：

- 该类所有的实例都已经被回收。也就是 Java 堆中不存在该类及其任何派生子类的实例。（下图的Sample对象）

- 加载该类的类加载器已经被回收。这个条件除非是经过精心设计的可替换类加载器的场景，如 OSGI、JSP 的重加载等，否则通常是很难达成的。（下图的MyClassLoader对象）

- 该类对应的 java.lang.Class 对象没有在任何地方被引用，无法在任何地方通过反射访问该类的方法。（下图代表 Sample类的Class对象）

  ![image-20210125075544718](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222702431-1518035978.png)

Java 虚拟机被允许对满足上述三个条件的无用类进行回收，这里说的仅仅是"被允许"（可能会被回收），而并不是和对象一样，没有引用了就必然会回收

# 4. 再谈类的加载器

---

## 4.1. 概述

**类加载器是 JVM 执行类加载机制的前提。**

ClassLoader 的作用：

ClassLoader 是 Java 的核心组件，所有的 Class 都是由 ClassLoader 进行加载的，ClassLoader 负责通过各种方式将 Class 信息的二进制数据流读入 JVM 内部，转换为一个与目标类对应的 java.lang.Class 对象实例。然后交给 Java 虚拟机进行链接、初始化等操作。因此，ClassLoader 在整个装载阶段，只能影响到类的加载，而无法通过 ClassLoader 去改变类的链接和初始化行为。至于它是否可以运行，则由 Execution Engine 决定。

![image-20210125081037564](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222702034-1895779760.png)

**与前面的图一样的**

![image-20210125081134527](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222701733-1833273929.png)

类加载器最早出现在 Java 1.0 版本中，那个时候只是单纯地为了满足 Java Applet 应用而被研发出来，但如今类加载器却在 OSGI、字节码加解密领域大放异彩。这主要归功于 Java 虚拟机的设计者们当初在设计类加载器的时候，并没有考虑将它绑定在 JVM 内部，这样做的好处就是能够更加灵活和动态地执行类加载操作。

### 4.1.1. 大厂面试题

```scss
蚂蚁金服：
深入分析 ClassLoader，双亲委派机制
类加载器的双亲委派模型是什么？
一面：双亲委派机制及使用原因
 
百度：
都有哪些类加载器，这些类加载器都加载哪些文件？
手写一个类加载器 Demo
Class的forName("java.lang.String") 和 Class的getClassLoader()的loadClass("java.lang.String")有什么区别？
 
腾讯：
什么是双亲委派模型？
类加载器有哪些？
 
小米：
双亲委派模型介绍一下
 
滴滴：
简单说说你了解的类加载器
一面：讲一下双亲委派模型，以及其优点
 
字节跳动：
什么是类加载器，类加载器有哪些？
 
京东：
类加载器的双亲委派模型是什么？
双亲委派机制可以打破吗？为什么？
```

### 4.1.2. 类加载的分类

类的加载分类：`显式加载` vs `隐式加载`

**Class 文件的显式加载与隐式加载的方式是指 JVM 加载 Class 文件到内存的方式**

- 显式加载指的是在代码中通过调用 `ClassLoader` 加载 Class 对象，如直接使用 Class.forName(name) 或 this.getClass().getClassLoader().loadClass() 加载 Class 对象。
- 隐式加载则是不直接在代码中调用 ClassLoader 的方法加载 Class 对象，而是通过虚拟机自动加载到内存中，如在加载某个类的 class 文件时，该类的 class 文件中引用了另外一个类的对象，此时额外引用的类将通过 JVM 自动加载到内存中。

**在日常开发中以上两种方式一般会混合使用。**

**例子：**

```java
public class User {
    private int id;
 
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                '}';
    }
}
package _12;
 
public class _31_UserTest {
    public static void main(String[] args) {
        // 调用构造器就要先加载该类
        User user = new User(); //隐式加载
 
        // ----------------------------------------------------------------------------------------
 
        try {
            Class clazz = Class.forName("_12.User"); //显式加载
            ClassLoader.getSystemClassLoader().loadClass("_12.User");//显式加载
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
 
    }
}
```

### 4.1.3. 类加载器的必要性

**一般情况下，Java 开发人员并不需要在程序中显式地使用类加载器，但是了解类加载器的加载机制却显得至关重要。从以下几个方面说：**

- 避免在开发中遇到 java.lang.ClassNotFoundException 异常或 java.lang.NoClassDefFoundError 异常时手足无措。只有了解类加载器的加载机制才能够在出现异常的时候快速地根据错误异常日志定位问题和解决问题。
- 需要支持类的动态加载或需要对编译后的字节码文件进行加解密操作时，就需要与类加载器打交道了。
- 开发人员可以在程序中编写自定义类加载器来重新定义类的加载规则，以便实现一些自定义的处理逻辑。

### 4.1.4. 命名空间

**1、何为类的唯一性？**

对于任意一个类，都需要由加载它的`类加载器和这个类本身一同确认其在 Java 虚拟机中的唯一性`。`每一个类加载器，都拥有一个独立的类名称空间：比较两个类是否相等，只有在这两个类是由同一个类加载器加载的前提下才有意义`。否则，即使这两个类源自同一个 Class 文件，被同一个虚拟机加载，只要加载他们的类加载器不同，那这两个类就必定不相等。（原来命名空间的本质是这么一个概念）

![image-20210125082844763](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222701395-820325194.png)

**2、命名空间**

- 每个类加载器都有自己的命名空间，命名空间由该加载器所有的父加载器所加载的类组成
- 在同一命名空间中，不会出现类的完整名字(包括类的包名)相同的两个类（只加载一次是对于同一个加载器来说的，加载过的就不会再加载，就会使用已有的类模板。一般开发中默认都是系统类加载器，所以不允许相同的类出现。）
- 在不同的命名空间中，有可能会出现类的完整名字(包括类的包名)相同的两个类

在大型应用中，我们往往借助这一特性，来运行同一个类的不同版本。（比如Tomcat中，同一份class文件使用不同的类加载器实现不同的应用隔离。）

**例子：**

```java
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
 
public class _32_UserClassLoader extends ClassLoader {
    private String rootDir;
 
    public _32_UserClassLoader(String rootDir) {
        this.rootDir = rootDir;
    }
 
    /**
     * 编写findClass方法的逻辑
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // 获取类的class文件字节数组
        byte[] classData = getClassData(name);
        if (classData == null) {
            throw new ClassNotFoundException();
        } else {
            //直接生成class对象
            return defineClass(name, classData, 0, classData.length);
        }
    }
 
    /**
     * 编写获取class文件并转换为字节码流的逻辑 * @param className * @return
     */
    private byte[] getClassData(String className) {
        // 读取类文件的字节
        String path = classNameToPath(className);
        try {
            InputStream ins = new FileInputStream(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            // 读取类文件的字节码
            while ((len = ins.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
 
    /**
     * 类文件的完全路径
     */
    private String classNameToPath(String className) {
        return rootDir + "\\" + className.replace('.', '\\') + ".class";
    }
 
    public static void main(String[] args) {
        String rootDir = "D:\\Code\\Java\\JVMDetail\\src\\";
 
        try {
            //创建自定义的类的加载器1
            _32_UserClassLoader loader1 = new _32_UserClassLoader(rootDir); // 指定这个加载器的加载路径
            Class clazz1 = loader1.findClass("_12.User");
 
            //创建自定义的类的加载器2
            _32_UserClassLoader loader2 = new _32_UserClassLoader(rootDir); // 指定这个加载器的加载路径
            Class clazz2 = loader2.findClass("_12.User");
 
            // 这两个自定义加载器所加载的类得到不同的类模板，这就是应用隔离的原理
            System.out.println(clazz1 == clazz2); // false。 clazz1与clazz2对应了不同的类模板结构。
 
            System.out.println(clazz1.getClassLoader()); // _12._32_UserClassLoader@677327b6
            System.out.println(clazz2.getClassLoader()); // _12._32_UserClassLoader@7f31245a
 
            // ----------------------------------------------------------------------------
 
            // 一般默认都是系统类加载器或叫应用类加载器（AppClassLoader）
            Class clazz3 = ClassLoader.getSystemClassLoader().loadClass("_12.User"); // 注意系统类加载器路径默认在out或target目录
            System.out.println(clazz3.getClassLoader()); // sun.misc.Launcher$AppClassLoader@18b4aac2
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
 
 
    }
}
 
```

![image-20210125223859863](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222701145-1396744860.png)

![image-20210125223933903](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222700876-819730061.png)

### 4.1.5. 类加载机制的基本特征

**通常类加载机制有三个基本特征：**

- **`双亲委派模型`**。但不是所有类加载都遵守这个模型，有的时候，启动类加载器所加载的类型，是可能要加载用户代码的，比如 JDK 内部的 ServiceProvider/ServiceLoader 机制，用户可以在标准 API 框架上，提供自己的实现，JDK 也需要提供些默认的参考实现。例如，Java 中 JNDI、JDBC、文件系统、Cipher 等很多方面，都是利用的这种机制，这种情况就不会用双亲委派模型去加载，而是利用所谓的上下文加载器。
- **`可见性`**。子类加载器可以访问父加载器加载的类型，但是反过来是不允许的。不然，因为缺少必要的隔离，我们就没有办法利用类加载器去实现容器的逻辑。
- **`单一性`**。由于父加载器的类型对于子加载器是可见的，所以父加载器中加载过的类型，就不会在子加载器中重复加载。但是注意，类加载器"邻居"间（即应用隔离），同一类型仍然可以被加载多次，因为相互并不可见。

---

## 4.2. 复习：类的加载器分类

JVM 支持两种类型的类加载器，分别为`引导类加载器(Bootstrap ClassLoader)`和`自定义类加载器(User-Defined ClassLoader)。`

从概念上来讲，自定义类加载器一般指的是程序中由开发人员自定义的一类类加载器，但是 Java 虚拟机规范却没有这么定义，而是将所有派生于抽象类 `ClassLoader` 的类加载器都划分为自定义类加载器。无论类加载器的类型如何划分，在程序中我们最常见的类加载器结构主要是如下情况：

![image-20210125225228163](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222700579-1947703789.png)

- 除了顶层的启动类加载器外，其余的类加载器都应当有自己的"父类"加载器。
- 不同类加载器看似是继承(Inheritance)关系，实际上是包含关系。在下层加载器中，包含着上层加载器的引用。

```java
class ClassLoader {
    ClassLoader parent; //父类加载器
 
    public ClassLoader(ClassLoader parent) {
        this.parent = parent;
    }
}
 
class ParentClassLoader extends ClassLoader {
    public ParentClassLoader(ClassLoader parent) {
        super(parent);
    }
}
 
class ChildClassLoader extends ClassLoader {
    public ChildClassLoader(ClassLoader parent) { //parent = new ParentClassLoader();
        super(parent);
    }
}
```

### 4.2.1. 引导类加载器

启动类加载器(引导类加载器，Bootstrap ClassLoader)

- 这个类加载使用 C/C++ 语言实现的，嵌套在 JVM 内部。
- `它用来加载 Java 的核心库(JAVA_HOME/jre/lib/rt.jar 或 sun.boot.class.path 路径下的内容)。用于提供 JVM 自身需要的类。`
- 并不继承自 java.lang.ClassLoader，没有父加载器。
- 出于安全考虑，Bootstrap 启动类加载器只加载包名为 java、javax、sun 等开头的类。
- 加载扩展类和应用程序类加载器，并指定为他们的父类加载器。

![image-20210125225837712](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222700124-2092079759.png)

**使用 -XX:+TraceClassLoading 参数得到。**

**启动类加载器使用 C++ 编写的？Yes！**

- C/C++：指针函数 & 函数指针、C++ 支持多继承、更加高效
- Java ：由 C++ 演变而来，(C++)-- 版，单继承

### 4.2.2. 扩展类加载器

扩展类加载器(Extension ClassLoader)

- Java 语言编写，由 sun.misc.Launcher$ExtClassLoader 实现
- 继承于 ClassLoader 类
- 父类加载器为启动类加载器
- `从 java.ext.dirs 系统属性所指定的目录中加载类库，或从 JDK 的安装目录的 jre/lib/ext 子目录下加载类库`。如果用户创建的 JAR 放在此目录下，也会自动由扩展类加载器加载

![image-20210126224353696](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222659764-1313376409.png)

**例子：（之前的例子）**

```java
import java.net.URL;
 
public class _33_ClassLoaderTest {
    public static void main(String[] args) {
        System.out.println("**启动类加载器**");
        //获取BootstrapClassLoader能够加载的api的路径
        URL[] urLs = sun.misc.Launcher.getBootstrapClassPath().getURLs();
        for (URL element : urLs) {
            System.out.println(element.toExternalForm());
        }
        //从上面的路径中随意选择一个类,来看看他的类加载器是什么:引导类加载器
        ClassLoader classLoader = java.security.Provider.class.getClassLoader();
        System.out.println(classLoader);
 
        System.out.println("***扩展类加载器*");
        String extDirs = System.getProperty("java.ext.dirs");
        for (String path : extDirs.split(";")) {
            System.out.println(path);
        }
 
//        //从上面的路径中随意选择一个类,来看看他的类加载器是什么:扩展类加载器
        ClassLoader classLoader1 = sun.security.ec.CurveDB.class.getClassLoader();
        System.out.println(classLoader1);//sun.misc.Launcher$ExtClassLoader@1540e19d
 
    }
}
 
```

**执行结果**

```java
**启动类加载器**
file:/C:/Software/Java/jdk1.8.0_211/jre/lib/resources.jar
file:/C:/Software/Java/jdk1.8.0_211/jre/lib/rt.jar
file:/C:/Software/Java/jdk1.8.0_211/jre/lib/sunrsasign.jar
file:/C:/Software/Java/jdk1.8.0_211/jre/lib/jsse.jar
file:/C:/Software/Java/jdk1.8.0_211/jre/lib/jce.jar
file:/C:/Software/Java/jdk1.8.0_211/jre/lib/charsets.jar
file:/C:/Software/Java/jdk1.8.0_211/jre/lib/jfr.jar
file:/C:/Software/Java/jdk1.8.0_211/jre/classes
null
***扩展类加载器*
C:\Software\Java\jdk1.8.0_211\jre\lib\ext
C:\Windows\Sun\Java\lib\ext
sun.misc.Launcher$ExtClassLoader@5cad8086
```

### 4.2.3. 系统类加载器

应用程序类加载器(系统类加载器，AppClassLoader)

- Java 语言编写，由 sun.misc.Launcher$AppClassLoader 实现
- 继承于 ClassLoader 类
- 父类加载器为扩展类加载器
- `它负责加载环境变量 classpath 或系统属性 java.class.path 指定路径下的类库`
- **`应用程序中的类加载器默认是系统类加载器`**
- 它是用户自定义类加载器的默认父加载器
- 通过 ClassLoader 的 getSystemClassLoader() 方法可以获取到该类加载器

### 4.2.4. 用户自定义类加载器

- 在 Java 的日常应用程序开发中，类的加载几乎是由上述3种类加载器相互配合执行的。在必要时，我们还可以自定义类加载器，来定制类的加载方式。
- 体现 Java 语言强大生命力和巨大魅力的关键因素之一便是，Java 开发者可以自定义类加载器来实现类库的动态加载，加载源可以是本地的 JAR 包，也可以是网络上的远程资源。
- `通过类加载器可以实现非常绝妙的插件机制`，这方面的实际应用案例不胜枚举。例如，著名的 OSGI 组件框架，再如 Eclipse 的插件机制。类加载器为应用程序提供了一种动态增加新功能的机制，这种机制无需重新打包发布应用程序就能实现。
- 同时，`自定义加载器能够实现应用隔离`，例如 Tomcat、Spring 等中间件和组件框架都在内部实现了自定义的加载器，并通过自定义加载器隔离不同的组件模块。这种机制比 C/C++ 程序要好太多，想不修改 C/C++ 程序就能为其新增功能，几乎是不可能的，仅仅一个兼容性便能阻挡所有美好的设想。
- 自定义类加载器通常需要继承于 ClassLoader。

---

## 4.3. 测试不同的类加载器

每个 Class 对象都会包含一个定义它的 ClassLoader 的一个引用。

获取 ClassLoader 的途径

![image-20220425153758475](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222659477-1451553316.png)

```java
//获取当前类的 ClassLoader
clazz.getClassLoader();
 
//获得当前线程上下文的 ClassLoader
Thread.currentThread().getContextClassLoader();
 
//获得系统的 ClassLoader
ClassLoader.getSystemClassLoader();
```

说明：

站在程序的角度看，引导类加载器与另外两种类加载器(系统类加载器和扩展类加载器)并不是同一个层次意义上的加载器，引导类加载器是使用 C++ 语言编写而成的，而另外两种类加载器则是使用 Java 语言编写的。由于引导类加载器压根儿就不是一个 Java 类，因此在 Java 程序中只能打印出空值。

**`数组类的 Class 对象，不是由类加载器去创建的，而是在 Java 运行期 JVM 根据需要自动创建的`**。对于数组类的类加载器来说，是通过 Class.getClassLoader() 返回的，与数组当中元素类型的类加载器是一样的：如果数组当中的元素类型是基本数据类型，数组类是没有类加载器的。

**例子：**

```java
public class _34_ClassLoaderTest2 {
    public static void main(String[] args) {
        //获取系统该类加载器
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        System.out.println(systemClassLoader);//sun.misc.Launcher$AppClassLoader@18b4aac2
        //获取扩展类加载器
        ClassLoader extClassLoader = systemClassLoader.getParent();
        System.out.println(extClassLoader);//sun.misc.Launcher$ExtClassLoader@677327b6
        //试图获取引导类加载器：失败
        ClassLoader bootstrapClassLoader = extClassLoader.getParent();
        System.out.println(bootstrapClassLoader);//null
 
        //###########################
        try {
            ClassLoader classLoader = Class.forName("java.lang.String").getClassLoader();
            System.out.println(classLoader); // null
            //自定义的类默认使用系统类加载器
            ClassLoader classLoader1 = Class.forName("_12._34_ClassLoaderTest2").getClassLoader();
            System.out.println(classLoader1); // sun.misc.Launcher$AppClassLoader@18b4aac2
 
            //关于数组类型的加载:使用的类的加载器与数组元素的类的加载器相同
            String[] arrStr = new String[10];
            System.out.println(arrStr.getClass().getClassLoader());//null:表示使用的是引导类加载器
 
            _34_ClassLoaderTest2[] arr1 = new _34_ClassLoaderTest2[10];
            System.out.println(arr1.getClass().getClassLoader());//sun.misc.Launcher$AppClassLoader@18b4aac2
 
            int[] arr2 = new int[10];
            System.out.println(arr2.getClass().getClassLoader());//null:不需要类的加载器（基本数据类型虚拟机预先定义了）
 
            // 获得当前线程上下文的 ClassLoader
            System.out.println(Thread.currentThread().getContextClassLoader()); // sun.misc.Launcher$AppClassLoader@18b4aac2
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
 
```

---

## 4.4. ClassLoader源码解析

ClassLoader 与现有类加载的关系：

![image-20210127081630642](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222659143-1614531305.png)

除了以上虚拟机自带的加载器外，用户还可以定制自己的类加载器。Java 提供了抽象类 java.lang.ClassLoader，所有用户自定义的类加载器都应该继承 ClassLoader 类。

![image-20210127082244582](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222658740-1824930843.png)

**例子：（前面的例子）**

![image-20210127081952347](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222658415-271938780.png)

### 4.4.1. ClassLoader的主要方法

抽象类 ClassLoader 的主要方法：(内部没有抽象方法)

![image-20220425161151834](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222657922-1424365283.png)

![image-20220425173055001](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222657464-1487971719.png)

![image-20220425174405468](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222656948-419660448.png)

**例子：（源码）**

**Ctrl + N全局搜索类**

![image-20210130183717935](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222656548-328392606.png)

![image-20210130184033702](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222656251-1933065299.png)

![image-20210130184553932](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222655911-344694727.png)

![image-20210130190633566](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222655652-659260000.png)

![image-20210130190756662](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222655410-1008480697.png)

**这是核心代码（后面有loadClass()剖析）**

![image-20210130190833434](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222655070-203020422.png)

![image-20210131102731017](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222654831-623728594.png)

![image-20210131103034492](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222654418-509476316.png)

![image-20210131103720913](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222654095-917564816.png)

**根据class文件返回Class实例（加载一个类的逻辑是在这里完成的）**

![image-20210131104200741](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222653877-515573773.png)

**多次跟踪，可以定位到调用的是一个native本地方法得到一个Class对象的**

![image-20210131104029661](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222653640-658418224.png)

#### 4.4.1.1. loadClass()的剖析

```java
// 测试调用loadClass()
ClassLoader.getSystemClassLoader().loadClass("com.atguigu.java.User");
 
 
// loadClass()源码
// resolve:true 加载Class的同时进行解析操作
protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    // 同步操作，保证只能加载一次
    synchronized (getClassLoadingLock(name)) {
        // First, check if the class has already been loaded
        // 首先，在缓存中判断是否已经加载同名的类
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            long t0 = System.nanoTime();
            try {
                // 获取当前类加载器的父类加载器
                if (parent != null) {
                    // 如果存在父类加载器，则调用父类加载器进行类的加载（递归，就是双亲委派机制，即有parent就交给parent去加载
                    c = parent.loadClass(name, false);
                } else {
                    // parent为null：父类加载器是引导类加载器
                    c = findBootstrapClassOrNull(name);
                }
            } catch (ClassNotFoundException e) {
                // ClassNotFoundException thrown if class not found
                // from the non-null parent class loader
            }
 
            // 当前类的加载器的父类加载器未加载此类 or 当前类加载器未加载此类
            if (c == null) {
                long t1 = System.nanoTime();
                // 调用当前ClassLoader的findClass() 方法
                c = findClass(name);
 
                // this is the defining class loader; record the stats
                sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                sun.misc.PerfCounter.getFindClasses().increment();
            }
        }
        // 是否进行解析操作
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
}
```

**加载一个类的过程**

![image-20210131102503099](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222649831-1057338976.png)

**例子：（前面的一个自定义加载器的例子）**

```java
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
 
public class _32_UserClassLoader extends ClassLoader {
    private String rootDir;
 
    public _32_UserClassLoader(String rootDir) {
        this.rootDir = rootDir;
    }
 
    /**
     * 编写findClass方法的逻辑
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // 获取类的class文件字节数组
        byte[] classData = getClassData(name);
        if (classData == null) {
            throw new ClassNotFoundException();
        } else {
            //直接生成class对象
            return defineClass(name, classData, 0, classData.length);
        }
    }
 
    /**
     * 编写获取class文件并转换为字节码流的逻辑 * @param className * @return
     */
    private byte[] getClassData(String className) {
        // 读取类文件的字节
        String path = classNameToPath(className);
        try {
            InputStream ins = new FileInputStream(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            // 读取类文件的字节码
            while ((len = ins.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
 
    /**
     * 类文件的完全路径
     */
    private String classNameToPath(String className) {
        return rootDir + "\\" + className.replace('.', '\\') + ".class";
    }
 
    public static void main(String[] args) {
        String rootDir = "D:\\Code\\Java\\JVMDetail\\src\\";
 
        try {
            //创建自定义的类的加载器1
            _32_UserClassLoader loader1 = new _32_UserClassLoader(rootDir); // 指定这个加载器的加载路径
            Class clazz1 = loader1.findClass("_12.User");
 
            //创建自定义的类的加载器2
            _32_UserClassLoader loader2 = new _32_UserClassLoader(rootDir); // 指定这个加载器的加载路径
            Class clazz2 = loader2.findClass("_12.User");
 
            // 这两个自定义加载器所加载的类得到不同的类模板，这就是应用隔离的原理
            System.out.println(clazz1 == clazz2); // false。 clazz1与clazz2对应了不同的类模板结构。
 
            System.out.println(clazz1.getClassLoader()); // _12._32_UserClassLoader@677327b6
            System.out.println(clazz2.getClassLoader()); // _12._32_UserClassLoader@7f31245a
 
            // ----------------------------------------------------------------------------
 
            // 一般默认都是系统类加载器或叫应用类加载器（AppClassLoader）
            Class clazz3 = ClassLoader.getSystemClassLoader().loadClass("_12.User"); // 注意系统类加载器路径默认在out或target目录
            System.out.println(clazz3.getClassLoader()); // sun.misc.Launcher$AppClassLoader@18b4aac2
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
 
    }
}
 
```

**小结：**

**讲源码的原因是为了更好的自定义类的加载器，包括破坏双亲委派机制、遵循双亲委派机制的实现方式。**

### 4.4.2. SecureClassLoader与URLClassLoader

接着 SecureClassLoader 扩展了 ClassLoader，新增了几个与使用相关的代码源(对代码源的位置及其证书的验证)和权限定义类验证(主要针对 class 源码的访问权限)的方法，一般我们不会直接跟这个类打交道，更多的是与它的子类 URLClassLoader 有所关联。

前面说过，ClassLoader 是一个抽象类，很多方法是空的没有实现，比如 findClass()、findResource() 等。而 URLClassLoader 这个实现类为这些方法提供了具体的实现。并新增了 URLClassPath 类协助取得 Class 字节码流等功能。`在编写自定义类加载器时，如果没有太过于复杂的需求，可以直接继承 URLClassLoader 类`，这样就可以避免自己去编写 findClass() 方法及其获取字节码流的方式，使自定义类加载器编写更加简洁。

![image-20210131105254521](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222652769-282332962.png)

### 4.4.3. ExtClassLoader与AppClassLoader

ExtClassLoader 并没有重写 loadClass() 方法，这足以说明其遵循双亲委派模式，而 AppClassLoader 重载了 loadClass() 方法，但最终调用的还是父类 loadClass() 方法，因此依然遵循双亲委派模式。

![image-20210131105621753](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222652338-333080692.png)

![image-20210131110510189](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222652015-1403130321.png)

### 4.4.4. Class.forName()与ClassLoader.loadClass()

- **`Class.forName()`**：是一个静态方法，最常用的是 Class.forName(String className)；根据传入的类的全限定名返回一个 Class 对象。`该方法在将 Class 文件加载到内存的同时，会执行类的初始化`。如：Class.forName("com.atguigu.java.HelloWorld");
- **`ClassLoader.loadClass()`** 这是一个实例方法，需要一个 ClassLoader 对象来调用该方法。`该方法将 Class 文件加载到内存时，并不会执行类的初始化，直到这个类第一次使用时才进行初始化`。该方法因为需要得到一个 ClassLoader 对象，所以可以根据需要指定使用哪个类加载器，如：ClassLoader c1 = .....; c1.loadClass("com.atguigu.java.HelloWorld");

前面的主动使用 vs 被动使用：

![image-20210131111023534](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222651642-195484822.png)

![image-20210131111028794](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222651330-394904130.png)

**源码**

![image-20210131111207494](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222651035-1450905748.png)

**实例方法loadClass()解析不做，初始化也不做。**

**小结：**

静态的Class.forName没有继承关系即没有家业就很勤奋，什么都做；而实例的ClassLoader.loadClass有继承即有家业就很懒散，所以很多东西都不做。（这里不是嘲讽，只是有一些现象特别适合这个例子，就练习起来了。）

---

## 4.5. 双亲委派模型

### 4.5.1. 定义与本质

类加载器用来把类加载到 Java 虚拟机中。从 JDK 1.2 版本开始，类的加载过程采用双亲委派机制，这种机制能更好地保证 Java 平台的安全。

**1、定义**

如果一个类加载器在接到加载类的请求时，它首先不会自己尝试去加载这个类，而是把这个请求任务委托给父类加载器去完成，依次递归，如果父类加载器可以完成类加载任务，就成功返回。只有父类加载器无法完成此加载任务时，才自己去加载。

**2、本质**

规定了类加载的顺序是：引导类加载器先加载，若加载不到，由扩展类加载器加载，若还加载不到，才会由系统类加载器或自定义的类加载器进行加载。

![image-20210131115110515](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222650653-1155413692.png)

![image-20210131114717413](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222650238-1755016535.png)

### 4.5.2. 优势与劣势

**1、双亲委派机制优势**

- **`避免类的重复加载，确保一个类的全局唯一性`**（双亲委派可以保证不同命名空间的类只有一个；同一命名空间不能有相同的类，避免重复的意义。）
- Java 类随着它的类加载器一起**`具备了一种带有优先级的层级关系`**，通过这种层级关系可以避免类的重复加载，当父亲已经加载了该类时，就没有必要子 ClassLoader 再加载一次。
- **`保护程序安全，防止核心 API 被随意篡改`**（上篇测试过自定义java.lang.String类的例子，没有双亲委派，自定义String类就会被加载，原有的String就会被替代篡改）

**2、代码支持**

- 双亲委派机制在 java.lang.ClassLoader.loadClass(String, boolean) 接口中体现。该接口的逻辑如下：
  - 1）先在当前加载器的缓存中查找有无目标类，如果有，直接返回。
  - 2）判断当前加载器的父加载器是否为空，如果不为空，则调用 parent.loadClass(name, false) 接口进行加载。
  - 3）反之，如果当前加载器的父类加载器为空，则调用 findBootstrapClassOrNull(name) 接口，让引导类加载器进行加载。
  - 4）如果通过以上3条路径都没能成功加载，则调用 findClass(name) 接口进行加载。该接口最终会调用 java.lang.ClassLoader 接口的 defineClass 系列的 native 接口加载目标 Java 类。
- 双亲委派的模型就隐藏在第2和第3步中。

**前面的源码剖析：**

![image-20210131102503099](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222649831-1057338976.png)

**3、举例**

假设当前加载的是 java.lang.Object 这个类，很显然，该类属于 JDK 中核心的不能再核心的一个类，因此一定只能由引导类加载器进行加载。当 JVM 准备加载 java.lang.Object 时，JVM 默认会使用系统类加载器去加载，按照上面4步加载的逻辑，在第1步从系统类的缓存中肯定查找不到该类，于是进入第2步。由于系统类加载器的父加载器是扩展类加载器，于是扩展类加载器继续从第1步开始重复。由于扩展类加载器的缓存中也一定查找不到该类，因此进入第3步。扩展类的父加载器是 null，因此系统调用 findBootstrapClassOrNull(String)，最终通过引导类加载器进行加载。

**4、思考**

如果在自定义的类加载器中重写 java.lang.ClassLoader.loadClass(String) 或 java.lang.ClassLoader.loadClass(String, boolean) 方法，抹去其中的双亲委派机制，仅保留上面这4步中的第1步和第4步，那么是不是就能够加载核心类库了呢？

这也不行！因为 JDK 还为核心类库提供了一层保护机制。不管是自定义的类加载器，还是系统类加载器抑或扩展类加载器，最终都必须调用 java.lang.ClassLoader.defineClass(String, byte[], int, int,ProtectionDomain) 方法，而该方法会执行 preDefineClass() 接口，该接口中提供了对 JDK 核心类库的保护。

![image-20210131120648006](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222649427-1830893252.png)

![image-20210131120728296](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222649180-1423147713.png)

![image-20210131120747943](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222648898-1911852383.png)

![image-20210131120914914](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222648643-394232749.png)

**由引导类加载器执行**

![image-20210131121026621](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222648329-1821368269.png)

**5、双亲委派模式的弊端**

检查类是否加载的委派过程是单向的，这个方式虽然从结构上说比较清晰，使各个 ClassLoader 的职责非常明确，但是同时会带来一个问题，即顶层的 ClassLoader 无法访问底层的 ClassLoader 所加载的类。

通常情况下，启动类加载器 中的类为系统核心类，包括一些重要的系统接口，而在应用类加载器中，为应用类。按照这种模式，`应用类访问系统类自然是没有问题，但是系统类访问应用类就会出现问题`。比如在系统类中提供了一个接口，该接口需要在应用类中得以实现，该接口还绑定一个工厂方法，用于创建该接口的实例，而接口和工厂方法都在启动类加载器中。这时，就会出现该工厂方法无法创建由应用类加载器加载的应用实例的问题。（上层无法访问下层的实例。）

**6、结论**

```
由于Java 虚拟机规范并没有明确要求类加载器的加载机制一定要使用双亲委派模型，只是建议采用这种方式而已。
```

比如 Tomcat 中，类加载器所采用的加载机制就和传统的双亲委派模型有一定区别，当缺省的类加载器接收到一个类的加载任务时，首先会由它自行加载，当它加载失败时，才会将类的加载任务委派给它的超类加载器去执行，这同时也是 Servlet 规范推荐的一种做法。

### 4.5.3. 破坏双亲委派机制

#### 4.5.3.1. 破坏双亲委派机制1

双亲委派模型并不是一个具有强制性约束的模型，而是 Java 设计者推荐给开发者们的类加载器实现方式。

在 Java 的世界中大部分的类加载器都遵循这个模型，但也有例外情况，直到 Java 模块化出现为止，双亲委派模型主要出现过3次较大规模"被破坏"的情况。

**第一次破坏双亲委派机制：**

双亲委派模型的第一次"被破坏"其实发生在双亲委派模型出现之前 -- 即 JDK 1.2 面世以前的"远古"时代。

由于双亲委派模型在 JDK 1.2 之后才被引入，但是类加载器的概念和抽象类 java.lang.ClassLoader 则在 Java 的第一个版本中就已经存在，面对已经存在的用户自定义类加载器的代码，Java 设计者们引入双亲委派模型时不得不做出一些妥协，`为了兼容这些已有的代码，无法再以技术手段避免 loadClass() 被子类覆盖的可能性，只能在 JDK 1.2 之后的 java.lang.ClassLoader 中添加一个新的 protected 方法 findClass()`，并引导用户编写的类加载逻辑时尽可能去重写这个方法，而不是在 loadClass() 中编写代码。上节我们已经分析过 loadClass() 方法，双亲委派的具体逻辑就实现在这里面，按照 loadClass() 方法的逻辑，如果父类加载失败，会自动调用自己的 findClass() 方法来完成加载，这样既不影响用户按照自己的意愿去加载类，又可以保证新写出来的类加载器是符合双亲委派规则的。

#### 4.5.3.2. 破坏双亲委派机制2

**第二次破坏双亲委派机制：线程上下文类加载器**

双亲委派模型的第二次"被破坏"是由这个模型自身的缺陷导致的，双亲委派很好地解决了各个类加载器协作时基础类型的一致性问题`(越基础的类由越上层的加载器进行加载)`，基础类型之所以被称为"基础"，是因为它们总是作为被用户代码继承、调用的 API 存在，但程序设计往往没有绝对不变的完美规则，`如果有基础类型又要调用回用户代码，那该怎么办？`

这并非是不可能出现的事情，一个典型的例子便是 JNDI 服务，JNDI 现在已经是 Java 的标准服务，它的代码由启动类加载器来完成加载(在 JDK 1.3 时加入到 rt.jar)，肯定属于 Java 中很基础的类型了。但 JNDI 存在的目的就是对资源进行查找和集中管理，它需要调用由其它厂商实现并部署在应用程序的 ClassPath 下的 JNDI 服务提供者接口(Service Provider Interface，SPI) 的代码，现在问题来了，启动类加载器时绝对不可能认识、加载这些代码的，那该怎么办？(SPI：在 Java 平台中，通常把核心类 rt.jar 中提供外部服务、可由应用层自行实现的接口称为 SPI)

为了解决这个困境，Java 的设计团队只好引入了一个不太优雅的设计：`线程上下文类加载器(Thread Context ClassLoader)`。这个类加载器可以通过 java.lang.Thread 类的 setContextClassLoader() 方法进行设置，如果创建线程时还未设置，它将会从父线程中继承一个，如果在应用程序的全局范围内都没有设置过的话，那这个类加载器默认就是应用程序类加载器。

有了线程上下文类加载器，程序就可以做一些"舞弊"的事情了。JNDI 服务使用这个线程上下文类加载器去加载所需的 SPI 服务代码。`这是一种父类加载器去请求子类加载器完成类加载的行为，这种行为实际上是打通了双亲委派模型的层次结构来逆向使用类加载器，已经违背了双亲委派模型的一般性原则`，但也是无可奈何的事情。Java 中涉及 SPI 的加载基本上都采用这种方式来完成，例如 JNDI、JDBC、JCE、JAXB 和 JBI 等。不过，当 SPI 的服务提供者多于一个的时候，代码就只能根据具体提供者的类型来硬编码判断，为了消除这种极不优雅的方式，在 JDK 6 时，JDK 提供了 java.util.ServiceLoader 类，以 META-INF/Services 中的配置信息，辅以责任链模式，这才算是给 SPI 的加载提供了一种相对合理的解决方案。

![image-20210131162904113](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222647952-564290825.png)

默认上下文加载器就是应用类加载器，这样以上下文加载器为中介，使得启动类加载器中的代码也可以访问应用类加载器中的类。

**源码**

![image-20210131165224194](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222647481-804922744.png)

#### 4.5.3.3. 破坏双亲委派机制3

**第三次破坏双亲委派机制：**

双亲委派模型的第三次"被破坏"是由于**用户对程序动态性的追求而导致**的。如：`代码热替换(Hot Swap)`、`模块热部署(Hot Deployment)`等。（就像电脑外设一样，不用重启，插上就能用。）

IBM 公司主导的 JSR-291(即 OSGi R4.2)实现模块化热部署的关键是它自定义的类加载器机制的实现，每个程序模块(OSGi 中称为 Bundle)都有一个自己的类加载器，当需要更换一个 Bundle 时，就把 Bundle 连同类加载器一起换掉以实现代码的热替换。在 OSGi 环境下，类加载器不再双亲委派模型推荐的树状结构，而是进一步发展为更加复杂的网状结构。（双亲委派机制本身是树状结构，后面增加了同级的类加载器即横向的，就成了网状。）

当收到类加载请求时，OSGi 将按照下面的顺序进行类搜索：

1）将以 java.* 开头的类，委派给父类加载器加载。

2）否则，将委派列表名单内的类，委派给父类加载器加载。

3）否则，将 Import 列表中的类，委派给 Export 这个类的 Bundle 的类加载器加载。

4）否则，查找当前 Bundle 的 ClassPath，使用自己的类加载器加载。

5）否则，查找类是否在自己的 Fragment Bundle 中，如果在，则委派给 Fragment Bundle 的类加载器加载。

6）否则，查找 Dynamic Import 列表的 Bundle，委派给对应 Bundle 的类加载器加载。

7）否则，类查找失败。

**说明：只有开头两点仍然符合双亲委派模型的原则，其余的类查找都是在平级的类加载器中进行的。**

**小结：**

这里，我们使用了"被破坏"这个词来形容上述不符合双亲委派模型原则的行为，但这里"被破坏"并不一定是带有贬义的。只要有明确的目的和充分的理由，突破旧有原则无疑是一种创新。 正如：OSGi 中的类加载器的设计不符合传统的双亲委派的类加载器架构，且业界对其为了实现热部署而带来的额外的高复杂度还存在不少争议，但对这方面有了解的技术人员基本还是能达成一个共识，认为 OSGi 中对类加载器的运用是值得学习的，完全弄懂了 OSGi 的实现，就算是掌握了类加载器的精髓。

### 4.5.4. 热替换的实现

热替换是指在程序运行过程中，不停止服务，只通过替换程序文件来修改程序的行为。热替换的关键需求在于服务不能中断，修改必须立即表现正在运行的系统之中。基本上大部分脚本语言都是天生支持热替换的，比如：PHP，只要替换了 PHP 源文件，这种改动就会立即生效，而无需重启 Web 服务器。

但对 Java 来说，热替换并非天生就支持，如果一个类已经加载到系统中，通过修改类文件，并无法让系统再来加载并重定义这个类。因此，在 Java中 实现这一功能的一个可行的方法就是灵活运用 ClassLoader。

注意：由不同 ClassLoader 加载的同名类属于不同的类型，不能相互转换和兼容。即两个不同的 ClassLoader 加载同一个类，在虚拟机内部，会认为这2个类是完全不同的。

根据这个特点，可以用来模拟热替换的实现，基本思路如下图所示：

![image-20210131171437999](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222647168-1735447503.png)

**需要热替换时，自定义的ClassLoader实例是新创建的，需要热替换的类也是新创建的，需要替换的类的对象也是新创建的，但是方法名还是原来的方法名，主要是对方法的替换。（IDEA部署SSM等项目中的war exploded，仅改动方法体的代码刷新即可实现热替换？）**

**例子：**

```java
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
 
/**
 * 自定义类的加载器
 */
public class MyClassLoader extends ClassLoader {
    private String rootDir;
 
    public MyClassLoader(String rootDir) {
        this.rootDir = rootDir;
    }
 
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        Class clazz = this.findLoadedClass(className);
        FileChannel fileChannel = null;
        WritableByteChannel outChannel = null;
        if (null == clazz) {
            try {
                String classFile = getClassFile(className);
                FileInputStream fis = new FileInputStream(classFile);
                fileChannel = fis.getChannel();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                outChannel = Channels.newChannel(baos);
                ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
                while (true) {
                    int i = fileChannel.read(buffer);
                    if (i == 0 || i == -1) {
                        break;
                    }
                    buffer.flip();
                    outChannel.write(buffer);
                    buffer.clear();
                }
 
                byte[] bytes = baos.toByteArray();
                clazz = defineClass(className, bytes, 0, bytes.length);
 
 
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileChannel != null)
                        fileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (outChannel != null)
                        outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return clazz;
    }
 
    /**
     * 类文件的完全路径
     */
    private String getClassFile(String className) {
        return rootDir + "\\" + className.replace('.', '\\') + ".class";
    }
}
 
 
 
 
/**
 * 需要热替换的类
 */
public class Demo1 {
    public void hot() {
        System.out.println("OldDemo1");
//        System.out.println("OldDemo1---> NewDemo1");
    }
 
}
 
package _13_hot;
 
import java.lang.reflect.Method;
 
/**
 * 每5秒创建一套类加载的内容
 */
public class LoopRun {
    public static void main(String args[]) {
        // 循环创建很多套
        while (true) {
            try {
                //1. 创建自定义类加载器的实例
                MyClassLoader loader = new MyClassLoader("D:\\Code\\Java\\JVMDetail\\src\\_13_hot\\");
                //2. 加载指定的类
                Class clazz = loader.findClass("_13_hot.Demo1");
                //3. 创建运行时类的实例
                Object demo = clazz.newInstance();
                //4. 获取运行时类中指定的方法
                Method m = clazz.getMethod("hot"); // hot是public修饰
                //5. 调用指定的方法
                m.invoke(demo);
                Thread.sleep(5000);
            } catch (Exception e) {
                System.out.println("not find");
 
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
 
            }
        }
    }
 
}
 
```

**用命令行编译Demo1类**

![image-20210131174248340](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222646907-2101686736.png)

**报错，所以指定编码编译**

![image-20210131174218482](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222646574-1587807246.png)

**执行LoopRun，程序会一直运行，每隔5秒调用一下hot方法输出结果**

![image-20210131174825241](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222646304-1190800827.png)

**修改一下hot方法的代码**

![image-20210131174912949](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222646063-1870549750.png)

**手动编译一下**

![image-20210131174953096](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222645792-447898215.png)

**然后就能保证LoopRun不停止的情况下，实现热替换**

![image-20210131175051036](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222645540-1626167353.png)

**小结：**

**实际没有替换掉原来的类，只是创建了新的一套与该类相关的类，最后通过调用同一个方法名的方法实现热替换。这是利用不同的命名空间（即不同的ClassLoader）可以加载相同的类实现的。**

---

## 4.6. 沙箱安全机制

- **`保护程序安全`**
- **`保护 Java 原生的 JDK 代码`**

**`Java 安全模型的核心就是 Java 沙箱(Sandbox)。什么是沙箱？沙箱就是一个限制程序运行的环境`**。

**沙箱机制就是将 Java 代码限定在虚拟机(JVM)特定的运行范围中，并且严格限制代码对本地系统资源访问。通过这样的措施来保证对代码的有限隔离，防止对本地系统造成破坏。**

沙箱主要限制系统资源访问，那系统资源包括什么？CPU、内存、文件系统、网络。不同级别的沙箱对这些资源访问的限制也可以不一样。

所有的 Java 程序运行都可以指定沙箱，可以定制安全策略。

### 4.6.1. JDK1.0时期

在 Java 中将执行程序分成本地代码和远程代码两种，本地代码默认视为可信任的，而远程代码则被看作是不受信任的。对于授信的本地代码，可以访问一切本地资源。而对于非授信的远程代码在早期的 Java 实现中，安全依赖于沙箱(Sandbox)机制。如下图所示 JDK 1.0 安全模型

![image-20210131175851160](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222645204-740955553.png)

### 4.6.2. JDK1.1时期

JDK 1.0 中如此严格的安全机制也给程序的功能扩展带来障碍，比如当用户希望远程代码访问本地系统的文件时候，就无法实现。

因此在后续的 JDK 1.1 版本中，针对安全机制做了改进，增加了安全策略。允许用户指定代码对本地资源的访问权限。

**如下图所示 JDK 1.1 安全模型**

![image-20210131175912059](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222644943-554699374.png)

### 4.6.3. JDK1.2时期

在 JDK 1.2 版本中，再次改进了安全机制，增加了代码签名。不论本地代码或是远程代码，都会按照用户的安全策略设定，由类加载器加载到虚拟机中权限不同的运行空间，来实现差异化的代码执行权限控制。如下图所示 JDK 1.2 安全模型：

![image-20210131175929183](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222644581-630598746.png)

### 4.6.4. JDK1.6时期

**当前最新的安全机制实现，则引入了域(Domain)的概念。**

虚拟机会把所有代码加载到不同的系统域和应用域。**系统域部分专门负责与关键资源进行交互**（类似于操作系统的内核态和应用态），而各个应用域部分则通过系统域的部分代理来对各种需要的资源进行访问。虚拟机中不同的受保护域(Protected Domain)，对应不一样的权限(Permission)。存在于不同域中的类文件就具有了当前域的全部权限，如下图所示，最新的安全模型(JDK 1.6)

![image-20210131175949198](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222644262-1962462291.png)

---

## 4.7. 自定义类的加载器

1、为什么要自定义类加载器？

- **`隔离加载类`**

在某些框架内进行中间件与应用的模块隔离，把类加载到不同的环境。比如：阿里内某容器框架通过自定义类加载器确保应用中依赖的 jar 包不会影响到中间件运行时使用的 jar 包（jar版本不同会导致类冲突，隔离可以防止冲突）。再比如：Tomcat 这类 Web 应用服务器，内部自定义了好几种类加载器，用于隔离同一个 Web 应用服务器上的不同应用程序。(类的仲裁 --> 类冲突)

- **`修改类加载的方式`**

类的加载模型并非强制，除 Bootstrap 外，其他的加载并非一定要引入，或者根据实际情况在某个时间点按需进行动态加载。

![image-20210131181300611](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222643911-1403183056.png)

- **`扩展加载源`**

比如从数据库、网络、甚至是电视机机顶盒进行加载

- **`防止源码泄露`**

Java 代码容易被编译和篡改，可以进行编译加密。那么类加载也需要自定义，还原加密的字节码

2、常见的场景

- 实现类似进程内隔离，类加载器实际上用作不同的命名空间，以提供类似容器、模块化的效果。例如，两个模块依赖于某个类库的不同版本，如果分别被不同的容器加载，就可以互不干扰。这个方面的集大成者是 Java EE 和 OSGI、JPMS 等框架。
- 应用需要从不同的数据源获取类定义信息，例如网络数据源，而不是本地文件系统。或者是需要自己操纵字节码，动态修改或者生成类型。

3、注意：

在一般情况下，使用不同的类加载器去加载不同的功能模块，会提高应用程序的安全性。但是，如果涉及 Java 类型转换，则加载器反而容易产生不美好的事情。在做 Java 类型转换时，只有两个类型都是由同一个加载器所加载，才能进行类型转换，否则转换时会发生异常。

![image-20210131182201334](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222643615-1245733896.png)

### 4.7.1. 实现方式

用户通过定制自己的类加载器，这样可以重新定义类的加载规则，以便实现一些自定义的处理逻辑。

1、实现方式

- Java 提供了抽象类 java.lang.ClassLoader，所有用户自定义的类加载器都应该继承 ClassLoader 类
- 在自定义 ClassLoader 的子类时候，我们常见的会有两种做法：
  - 方式一：重写 loadClass() 方法（破坏双亲委派机制）
  - 方式二：重写 findClass() 方法（遵循双亲委派机制）

2、对比

这两种方法本质上差不多，毕竟 loadClass() 也会调用 findClass()，但是从逻辑上讲我们最好不要直接修改 loadClass() 的内部逻辑。建议的做法是只在 findClass() 里重写自定义类的加载方法，根据参数指定类的名字，返回对应的 Class 对象的引用。

- loadClass() 这个方法是实现双亲委派模型逻辑的地方，擅自修改这个方法会导致模型被破坏，容易造成问题。因此我们最好是在双亲委派模型框架内进行小范围的改动，不破坏原有的稳定结构。同时，也避免了自己重写 loadClass() 方法的过程中必须写双亲委托的重复代码，从代码的复用性来看，不直接修改这个方法始终是比较好的选择.
- 当编写好自定义类加载器后，便可以在程序中调用 loadClass() 方法来实现类加载操作.

3、说明

- 其父类加载器是系统类加载器
- JVM 中的所有类加载都会使用 java.lang.ClassLoader.loadClass(String) 接口(自定义类加载器并重写java.lang.ClassLoader.loadClass(String) 接口的除外)，连 JDK 的核心类库也不能例外。

**源码：**

![image-20210131182951349](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222643158-802275862.png)

**例子：**

```java
public class Demo2 {
    public void print() {
        System.out.println("Hello World");
    }
}
 
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
 
/**
 * 自定义ClassLoader
 */
public class MyClassLoader2 extends ClassLoader {
    private String byteCodePath;
 
    public MyClassLoader2(String byteCodePath) {
        this.byteCodePath = byteCodePath;
    }
 
    public MyClassLoader2(ClassLoader parent, String byteCodePath) {
        super(parent);
        this.byteCodePath = byteCodePath;
    }
 
    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;
        try {
            //获取字节码文件的完整路径
            String fileName = byteCodePath + className + ".class";
            //获取一个输入流
            bis = new BufferedInputStream(new FileInputStream(fileName));
            //获取一个输出流
            baos = new ByteArrayOutputStream();
            //具体读入数据并写出的过程
            int len;
            byte[] data = new byte[1024];
            while ((len = bis.read(data)) != -1) {
                // 写到字节数组流
                baos.write(data, 0, len);
            }
            //获取内存中的完整的字节数组的数据
            byte[] byteCodes = baos.toByteArray();
            //调用defineClass()，将字节数组的数据转换为Class的实例。
            Class clazz = defineClass(null, byteCodes, 0, byteCodes.length);
            return clazz;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
 
        return null;
    }
}
 
 
 
 
 
/**
 */
public class _36_MyClassLoaderTest {
    public static void main(String[] args) {
        MyClassLoader2 loader = new MyClassLoader2("D:\\Code\\Java\\JVMDetail\\src\\");
 
        try {
            Class clazz = loader.loadClass("_14.Demo2");
            System.out.println("加载此类的类的加载器为：" + clazz.getClassLoader().getClass().getName());
 
            System.out.println("加载当前Demo1类的类的加载器的父类加载器为：" + clazz.getClassLoader().getParent().getClass().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
 
```

**运行_36_MyClassLoaderTest**

```bash
加载此类的类的加载器为：sun.misc.Launcher$AppClassLoader
加载当前Demo1类的类的加载器的父类加载器为：sun.misc.Launcher$ExtClassLoader
```

---

## 4.8. Java9新特性

**前面是针对Java8讲的JVM，Java9的变化对于Java开发来说变化不大，但是对于底层的加载过程的变化还是比较大的。（模块化）**

为了保证兼容性，JDK 9 没有从根本上改变三层类加载器架构和双亲委派模型，但为了模块化系统的顺利运行，仍然发生了一些值得被注意的变动。

1、扩展机制被移除，扩展类加载器由于向后兼容性的原因被保留，不过被重命名为平台类加载器(Platform Class Loader)。可以通过 ClassLoader 的新方法 getPlatformClassLoader() 来获取。（扩展类的名字不太适合了）

JDK 9 时基于模块化进行构建(原来的 rt.jar 和 tools.jar 被拆分成数十个 JMOD 文件)，其中的 Java 类库就已天然地满足了可扩展的需求，那自然无需再保留 <JAVA_HOME>\lib\ext 目录，此前使用这个目录或者 java.ext.dirs 系统变量来扩展 JDK 功能的机制已经没有继续存在的价值了。

2、平台类加载器和应用程序类加载器都不再继承自 java.net.URLClassLoader。

现在启动类加载器、平台类加载器、应用程序类加载器全都继承于 jdk.internal.loader.BuiltinClassLoader。

![image-20210131212200162](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222642882-1877329153.png)

如果有程序直接依赖了这种继承关系，或者依赖了 URLClassLoader 类的特定方法，那代码很可能会在 JDK 9 及更高版本的 JDK 中崩溃。

**对比Java8的继承关系**

![image-20210131212832244](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222642450-1856636089.png)

3、在 Java 9 中，类加载器有了名称。该名称在构造方法中指定，可以通过 getName() 方法来获取。平台类加载器的名称是 platform，应用类加载器的名称是 app。类加载器的名称在调试与类加载器相关的问题时会非常有用。

4、启动类加载器现在是在 JVM 内部和 Java 类库共同协作实现的类加载器(以前是 C++ 实现)，但为了与之前代码兼容，在获取启动类加载器的场景中仍然会返回 null，而不会得到 BootClassLoader 实例。

5、类加载的委派关系也发生了变动。

当平台及应用程序类加载器收到类加载请求，在委派给父加载器加载前，要先判断该类是否能够归属到某一个系统模块中，如果可以找到这样的归属关系，就要优先委派给负责哪个模块的加载器完成加载。

双亲委派模式示意图

![image-20210131212328142](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222642064-1362109833.png)

**附加：**

**在 Java 模块化系统明确规定了三个类加载器负责各自加载的模块：**

- 启动类加载器负责加载的模块

```mipsasm
java.base                           java.security.sasl
java.datatransfer                   java.xml
java.desktop                        jdk.httpserver
java.instrument                     jdk.internal.vm.ci
java.logging                        jdk.management
java.management                     jdk.management.agent
java.management.rmi                 jdk.naming.rmi
java.naming                         jdk.net
java.prefs                          jdk.sctp
java.rmi                            jdk.unsupported

```

- 平台类加载器负责加载的模块

```mipsasm
java.activation*                    jdk.accessibility
java.compiler*                      jdk.charsets
java.corba*                         jdk.crypto.cryptoki
java.scripting                      jdk.crypto.ec
java.se                             jdk.dynalink
java.se.ee                          jdk.incubator.httpclient
java.security.jgss                  jdk.internal.vm.compiler*
java.smartcardio                    jdk.jsobject
java.sql                            jdk.localedata
java.sql.rowset                     jdk.naming.dns
java.transaction*                   jdk.scripting.nashorn
java.xml.bind*                      jdk.security.auth
java.xml.crypto                     jdk.security.jgss
java.xml.ws*                        jdk.xml.dom
java.xml.ws.annotation*             jdk.zipfs

```

- 应用程序类加载器负责加载的模块

```mipsasm
jdk.aot                             jdk.jdeps
jdk.attach                          jdk.jdi
jdk.compiler                        jdk.jdwp.agent
jdk.editpad                         jdk.jlink
jdk.hotspot.agent                   jdk.jshell
jdk.internal.ed                     jdk.jstatd
jdk.internal.jvmstat                jdk.pack
jdk.internal.le                     jdk.policytool
jdk.internal.opt                    jdk.rmic
jdk.jartool                         jdk.scripting.na
shorn.shell
jdk.javadoc                         jdk.xml.bind*
jdk.jcmd                            jdk.xml.ws*

```

**例子1：**

**新建模块**

![image-20210131210511440](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222641737-862322305.png)

![image-20210131210554930](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222641464-1772689625.png)

![image-20210131210634836](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222641237-1797704365.png)

![image-20210131210656642](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222640948-200317812.png)

![image-20210131210725671](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222640717-1917168362.png)

![image-20210131210801198](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222640462-645922643.png)

![image-20210131211304998](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222640105-1695520985.png)

**代码**

```java
public class _37_ClassLoaderTest {
    public static void main(String[] args) {
        System.out.println(_37_ClassLoaderTest.class.getClassLoader());
        System.out.println(_37_ClassLoaderTest.class.getClassLoader().getParent());
        System.out.println(_37_ClassLoaderTest.class.getClassLoader().getParent().getParent());
    }
}
 
```

**运行结果**

```kotlin
jdk.internal.loader.ClassLoaders$AppClassLoader@726f3b58
jdk.internal.loader.ClassLoaders$PlatformClassLoader@e73f9ac
null
```

**与Java8结果对比**

![image-20210131211715155](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222639846-693072043.png)

**例子2：**

```java
public class _38_ClassLoaderTest2 {
    public static void main(String[] args) {
        //获取系统类加载器
        System.out.println(ClassLoader.getSystemClassLoader());
        //获取平台类加载器
        System.out.println(ClassLoader.getPlatformClassLoader());
        //获取类的加载器的名称
        System.out.println(_38_ClassLoaderTest2.class.getClassLoader().getName());
    }
}
 
```

**执行结果**

```kotlin
jdk.internal.loader.ClassLoaders$AppClassLoader@726f3b58
jdk.internal.loader.ClassLoaders$PlatformClassLoader@e73f9ac
app
```

**例子3：**

**java8**

![image-20210131212649429](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222639569-1253915937.png)

**java9，变成了mod，没有扩展的概念了**

![image-20210131212726476](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222639294-1921024121.png)

**例子4：java9的继承关系的源码**

![image-20210131213107554](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222638972-719207224.png)

![image-20210131213154810](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222638596-1687808766.png)

**启动类加载器不再由C++实现了，而是由JVM内部和Java类库协作实现。**

![image-20210131213216804](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222638306-2140129804.png)

![image-20210131213239047](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222638012-2005934473.png)

![image-20210131213304618](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222637592-2123282547.png)

![image-20210131213323271](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-12/2402456-20220501222637182-1879602182.png)