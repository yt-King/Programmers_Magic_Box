# 函数式编程

概要：面向对象思想要关注用什么对象完成什么事情。而函数式编程就类似于我们数学中的函数，他主要关注对数据进行了什么操作。他是的代码简洁，开发快速，接近自然语言易于理解，易于“并发编程”。

## Lambda表达式

### 1.概述

Lambda时jdk8的一个语法糖，他可以对某些匿名内部类的写法进行简化，是函数式编程的一个重要体现。我们不用关注是什么对象，而是更关注我们对数据进行了什么操作。

### 2.核心原则

> 可推导可省略

### 3.基本格式

```java
(参数列表)->{代码}
```

刚开始的时候可以先将匿名内部类的写法写出来，然后只需关注匿名内部类中方法的参数和方法体的代码再进行改写即可。

![image-20220507200913819](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/5/202205072009939.png)

![image-20220507203955028](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/5/202205072039345.png)

### 4.省略规则

1.  参数类型可以省略
2. 方法体只有一句代码时大括号return和唯一一句代码的分号可以省略
3. 方法只有一个参数时小括号可以省略
4. 以上方法都记不住也可以省略不计（bushi）,也可以在idea中用**Alt+回车**的快捷键简化成最终的Lambda表达式

## Stream流

### 1.概述

java8的 `Stream` 使用的是函数式编程模式，可以用来对集合或数组进行链状流式的操作，元素流在管道中经过中间操作（intermediate opertaion）的处理，最后由终端操作（terminal opertaion）得到前面处理的结果（每一个流只能有一次终端处理）。

中间操作可以分为**无状态操作和有状态操作**，前者是指元素的处理不受之前元素的影响；后者是指该操作只有拿到所有元素才能继续下去。

终端操作也可分为**短路与非短路操作**，前者是指遇到符合条件的元素就可以得到最终结果，而后者必须处理所有元素才能得到最终结果。

![image-20220507205740070](https://gitee.com/master_p/ImageHost/raw/master/Typora/2022/5/202205072057167.png)

### 2.创建流

```java
//1.创建一个空的流
Stream<String> emptyStream = Stream.empty();

//可以用empty() 方法来返回一个空流从而避免返回null
public Stream<String> streamOf(List<String> list) {
    return list == null || list.isEmpty() ? Stream.empty() : list.stream();
}

//2.使用数组创建流
String[] arr = new String[]{"1", "2", "3"，"4", "5"};
Stream<String> entireArrayStream = Arrays.stream(arr);//使用全部
Stream<String> partArrayStream = Arrays.stream(arr, 1, 4);//使用一部分

//3.使用集合创建流
Collection<String> collection = Arrays.asList("1", "2", "3");
Stream<String> collectionStream = collection.stream();

//4.使用Stream.Builder()来创建流，注意申明好类型，否则会创建 Stream<Obejct> 的流
Stream<String> streamBuilder = Stream.<String>builder().add("1").add("2").add("3").build();


```

