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

//5.使用File来创建流
Path path = Paths.get("xx\\xx\\file.txt");
        Stream<String> fileStream = Files.lines(path);//每一行都会成为流的每一个元素
        Stream<String> fileStreamWithCharset = Files.lines(path, Charset.forName("UTF-8"));

//6.使用iterate() 来创建一个流
Stream<Integer> iteratedStream = Stream.iterate(10, n -> n + 1).limit(10);

//7.使用Stream.iterate()，generate() 方法接受一个Supplier<T>来生成元素
Stream<String> generatedStream = Stream.generate(() -> "ele").limit(5);

//8.基本类型的流——int,long,double可以创建对应的流
//8.1range()和rangeClosed()
IntStream intStream = IntStream.range(1, 3);//1,2
LongStream longStream = LongStream.rangeClosed(1, 3);//1,2,3
/**
range(int start, int end) 方法会创建一个从start到end的有序流，它的步长是1，但是它不包括end。

rangeClosed(int start, int end) 与range() 方法的区别在于，前者会包括end。
*/
//8.2 of()
int[] intArray = {1,2,3};
IntStream intStream = IntStream.of(intArray);//1,2,3
IntStream intStream2 = IntStream.of(1, 2, 3);//1,2,3

long[] longArray = {1L, 2L, 3L};
LongStream longStream = LongStream.of(longArray);//1,2,3
LongStream longStream2 = LongStream.of(1L, 2L, 3L);//1,2,3

double[] doubleArray = {1.0, 2.0, 3.0};
DoubleStream doubleStream = DoubleStream.of(doubleArray);
DoubleStream doubleStream2 = DoubleStream.of(1.0, 2.0, 3.0);//1.0,2.0,3.0
//8.3 Random
Random random = new Random();//随机的数变成流
IntStream intStream = random.ints(3);
LongStream longStream = random.longs(3);
DoubleStream doubleStream = random.doubles(3);

//9.字符串的流
Stream<String> stringStream = Pattern.compile(",").splitAsStream("a,b,c");
```

### 3.基本用法

#### 3.1 forEach()方法（终结操作）

对每个元素执行指定的动作，也就是对元素进行遍历

```java
Arrays.asList("Try", "It", "Now")
                .stream()
                .forEach(System.out::println);
```

输出结果：

```
Try
It
Now
```

#### 3.2 filter()方法（中间操作）

**filter()** 方法的作用是返回符合条件的Stream。

```java
Arrays.asList("Try", "It", "Now")
                .stream()
                .filter(ele -> ele.length() == 3)
                .forEach(System.out::println);
```

输出结果：

```
Try
Now
```

#### 3.3 distinct()方法（中间操作）

**distinct()** 方法返回一个去重的stream。

```java
Arrays.asList("Try", "It", "Now", "Now")
                .stream()
                .distinct()
                .forEach(System.out::println);
```

#### 3.4 sorted()方法（中间操作）

排序函数有两个，一个是自然顺序，还有一个是自定义比较器排序。

```java
Arrays.asList("Try", "It", "Now")
                .stream()
                .sorted((str1, str2) -> str1.length() - str2.length())
                .forEach(System.out::println);
```

输出结果：

```
It
Try
Now
```

#### 3.5 map()方法（中间操作）

**map()** 方法对每个元素按照某种操作进行转换，转换后流的元素个数不会改变，但是元素类型取决于转换之后的类型。

```java
Arrays.asList("Try", "It", "Now")
                .stream()
                .map(String::toUpperCase)
                .forEach(System.out::println);
```

输出结果：

```
TRY
IT
NOW
```

#### 3.6 limit()方法（中间操作）

设置流的最大长度，超出的部分将被抛弃

```java
Arrays.asList("Try", "It", "Now")
                .stream()
                .limit(1)
                .forEach(System.out::println);
```

输出结果：

```java
Try
```

#### 3.7 skip()方法（中间操作）

跳过流中的前n个元素，返回剩下的元素

```java
 Arrays.asList("Try", "It", "Now")
                .stream()
                .skip(1)
                .forEach(System.out::println);
```

输出结果：

```java
It
Now
```

#### 3.8 flatMap()方法（中间操作）

flat的英文就是”平坦的“意思，而flatMap()方法的作用就是将流的元素摊平，借助下面这个例子我们更好理解：

```java
Stream.of(Arrays.asList("Try", "It"), Arrays.asList("Now"))
                .flatMap(list -> list.stream())
                .forEach(System.out::println);
```

输出结果：

```
Try
It
Now
```

在上述这段代码中，原来的stream有两个元素，分别是两个List，执行了flatMap()之后，将每个List都”摊平“成了一个个的元素，所以会产生一个有三个字符串组成的流。

#### 3.9 count()方法（终结操作）

获取当前流中元素的个数

#### 3.10 max&min()方法（终结操作）

获取流中的最值，返回Optional对象

```java
System.out.println(Arrays.asList("Try", "It", "Nooow")
                .stream()
                .max((s1, s2) -> s1.length() - s2.length()));
//Optional[Nooow]
```

#### 3.11 查找与匹配方法（终结操作）

- anyMatch

判断是否有任意符合匹配条件的元素，结果为布尔类型

```java
boolean flag = students
                .stream()
                .anyMatch(student -> student.getName().equals("Jack"));
System.out.println("flag = " + flag);//flag = true
```

- allMatch

判断所有元素是否全部符合

```java
 boolean flag = students
                .stream()
                .allMatch(student -> student.getName().equals("Jack"));
System.out.println("flag = " + flag);//flag = false
```

- noneMatch

判断是否全部不一致

```java
boolean flag = students
    .stream()
    .noneMatch(student -> student.getName().equals("Jack"));
System.out.println("flag = " + flag);//flag = false
```

- findAny

找到一个值，不一定是第一个，可以跟过滤器搭配使用，返回一个optional对象

```java
//找到一个名字长度大于三个字人的名字 
Optional<Student> res = students
     .stream()
     .filter(student -> student.getName().length() > 3)
     .findAny();
res.ifPresent(x -> System.out.println(x.getName()));//Jack
```

- findFirst

找到第一个符合条件的值，可以跟过滤器搭配使用，返回一个optional对象

```java
//找到第一个名字长度大于三个字人的名字
Optional<Student> res = students
        .stream()
        .filter(student -> student.getName().length() > 3)
        .findFirst();
res.ifPresent(x -> System.out.println(x.getName()));//Jack
```

### 4.归约操作

> 归约操作（reduction operation）也被称为折叠操作(fold)，是通过某种连接动作将所有元素汇总成一个结果的过程。元素求和、求最大值、求最小值、求总数，将所有元素转换成一个集合等都属于归约操作。

Stream类库有两个通用的归约操作**reduce()和collect()** ，也有一些为简化书写而设计的专用归约操作，比如sum()、max()、min()、count()等。

#### 4.1  reduce()

reduce操作可以实现从一组元素中生成一个值，比如sum()、max()、min()、count()等都是reduce操作。

reduce()方法定义有三种形式：

```java
Optional<T> reduce(BinaryOperator<T> accumulator)

T reduce(T identity, BinaryOperator<T> accumulator)

<U> U reduce(U identity, BiFunction<U,? super T,U> accumulator, BinaryOperator<U> combiner)

//1. identity-初始值

//2. accumulator-累加器

//3. combiner-拼接器，只有并行执行时才会用到。
```

示例：

```java
Optional<Integer> reducedInt = Stream.of(1, 2, 3).reduce((a, b) -> a + b);
//reducedInt = 1 + 2 + 3 = 6

int reduceIntWithTwoParams = Stream.of(1, 2, 3).reduce(10, (a, b) -> a + b);
//reduceIntWithTwoParams = 10 + 1 + 2 + 3 = 16

int reducedIntWithAllParams = Arrays.asList(1, 2, 3).parallelStream().reduce(10, (a, b) -> a + b, (a, b) -> {
    System.out.println("Combiner was invoked");
    return a + b;
});
//这里应该使用parallelStream()方法！！
//reducedIntWithAllParams = (10 + 1)+ ((10 + 2) + (10 + 3)) = 36
```

> Collection.stream() 和 Collection.parallelStream() 分别产生序列化流(普通流)和并行流。
>
> 并行(parallel)和并发(concurrency)是有区别的。
>
> 并发是指一个处理器同时处理多个任务。而并行是指多个处理器或者是多核的处理器同时处理多个不同的任务。
>
> 并发是逻辑上的同时发生，而并行是物理上的同时发生。
>
> 打个比方：并发是一个人同时吃三个馒头，而并行是三个人同时吃三个馒头。
>
> 并且并行不一定快，尤其在数据量很小的情况下，可能比普通流更慢。只有在大数据量和多核的情况下才考虑并行流。
>
> 在并行处理情况下，传入给reduce()的集合类，需要是线程安全的，否则执行结果会与预期结果不一样。

#### 4.2 collect()方法

示例：

```java
@Data
@AllArgsConstructor
public class Student {
    private String name;
    private Integer score;
}
```

```java
List<Student> students = Arrays.asList(new Student("Jack", 90)
                , new Student("Tom", 85)
                , new Student("Mike", 80));
```

**1. 常规归约操作**

- 获取平均值

```java
Double averagingScore = students.stream().collect(Collectors.averagingDouble(Student::getScore));
```

- 获取和

```java
Double summingScore = students.stream().collect(Collectors.summingDouble(Student::getScore));
```

- 获取分析数据

```java
//可以从doubleSummaryStatistics 获取最大值、最小值、平均值等常见统计数据。
DoubleSummaryStatistics doubleSummaryStatistics = students.stream().collect(Collectors.summarizingDouble(Student::getScore));
System.out.println(doubleSummaryStatistics.getMax());//90.0
System.out.println(doubleSummaryStatistics.getMin());//80.0
System.out.println(doubleSummaryStatistics.getAverage());//85.0
System.out.println(doubleSummaryStatistics.getSum());//255.0
```

**2. 将流转换成Collection**

```java
//转换成list
List<String> collect = Arrays.asList("Try", "It", "Nooow")
                .stream()
                .collect(Collectors.toList());
System.out.println(collect);//[Try, It, Nooow]
//转换成set
 Set<String> collect = Arrays.asList("Try", "It", "Nooow","It", "Nooow")
                .stream()
                .collect(Collectors.toSet());
System.out.println(collect);//[Nooow, Try, It]
```

- 提取集合中的Student的Name属性，并且装入字符串类型的集合当中

```java
List<String> studentNameList = students.stream().map(Student::getName).collect(Collectors.toList());
//[Jack, Tom, Mike]
//可以通过Collectors.joining() 方法来连接字符串。 并且Collector会帮你处理后最后一个元素不应该再加分隔符的问题。
String studentNameList = students.stream().map(Student::getName).collect(Collectors.joining(",", "[", "]"));
//[Jack,Tom,Mike]
```

**3. 将流转换成Map**

Map不能直接转换成Stream，但是Stream生成Map是可行的，在生成Map之前，我们应该先定义好Map的Key和Value分别代表什么。

**Collectors.toMap()**

下面这个例子为我们展示了怎么将`students`列表转换成`<Student student, double score>`组成的map。

```java
Map<String, Integer> collect = students
    .stream()
    .collect(Collectors.toMap(Student::getName, Student::getScore));
System.out.println(collect);
//{Mike=80, Tom=85, Jack=90}
```

**Collectors.groupingBy()**

这个操作有点类似于SQL中的groupBy操作，按照某个属性对数据进行分组，而属性相同的元素会被分配到同一个key上。

而下面这个例子将会把Student按照Score进行分组：

```java
Map<Double, List<Student>> nameStudentMap = students.stream().collect(Collectors.groupingBy(Student::getScore));
//{80=[Student(name=Mike, score=80)], 85=[Student(name=Tom, score=85)], 90=[Student(name=Jack, score=90)]}
```

**Collectors.partitioningBy()**

partitioningBy()按照某个二元逻辑将stream中的元素分为两个部分，比如说下面这个例子将Student分成了成绩及格或者不及格的部分。

```java
Map<Boolean, List<Student>> map = students.stream().collect(Collectors.partitioningBy(ele -> ele.getScore() >= 85));
//{false=[Student(name=Mike, score=80)], true=[Student(name=Jack, score=90), Student(name=Tom, score=85)]}
```