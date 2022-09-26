# Go语言基础

## 1什么是go语言

1. 高性能，高并发

   go语言是一门高性能高并发的编程语言，有着和C++，java媲美的性能。还内建了对高并发的支持，而不像很多编程语言以库的形式支持。也就是说在golang里面，你不需要像其他语言一样，需要去寻找一些经过高度性能优化的第三化库来开发应用，只需要使用标准库或者任意基于标准库的第三化库即可开发高并发应用程序。

2. 语法简单，学习曲线平缓

   其语法风格类似于C语言，并且在C语言的基础上进行了**大幅度的简化**。

3. 丰富的标准库

   拥有大量完善的极其丰富，功可靠的标准库，在很多情况下，你不需要借助第三方库就可以完成大部分基础功能的开发，这大大降低了学习和使用成本。最关键的是标准库**有很高的稳定性和兼容性保障**，还能**持续享受语言迭代带来的性能优化**，这是第三方库所不具备的。

4. 完善的工具链

   拥有丰富的工具链，无论是编译代码格式化、错误检查帮助文档，包管理，还有代码补充提示，这些都有对应的工具。同时内置了完整的单元测试框架，能够支持单元测试，性能测试，代码覆盖率，数据竞争检测性能优化，这些都是保障代码能够正确和稳定运行的必备利器。

5. 静态链接

   在go语言里面，所有的编译结果默认都是**静态链接**的，只需要拷贝编译之后的唯一一个**可执行文件**，不需要附加任何东西用来部署运行。在线上的容器环境下运行，镜像体积可以控制的非常小，部署也非常方便快捷。

6. 快速编译

   go语言拥有静态语言里面几乎最快的编译速度。

7. 跨平台，支持交叉编译

8. 垃圾回收

## 2.go语言入门

### 2.1开发环境

[Go 语言环境安装](https://www.runoob.com/go/go-environment.html)//[goland下载](https://www.jetbrains.com/go/)//[[Go] 路径、目录名、包名、文件名 ](https://www.cnblogs.com/52php/p/6063957.html)

#### GOPATH

> 总的来说，GOPATH是go命令常常需要用到的，如`go run`，`go install`， `go get`等执行的目标路径，这些命令所操作的目录，就是GOPATH。

GOPATH也被称为Golang的工作目录。它分为两种，一种是全局GOPATH，另一种是项目对应的GOPATH，GOPATH包含的结构如下：

```css
.（GOPATH的路径）
├── bin
├── pkg
└── src
```

- src存放的是我们的我们项目的源代码(比如：.go .c .h .s等)
- pkg编译时生成的中间文件（比如：.a）
- bin编译后生成的可执行文件

在Golang的项目中，所有的源码都是放在/src中，一般的做法是一个目录一个项目。

也就是说，在开发过程中，如果需要引用到其他的项目，使用`import "xxx"`,将会引用到$GOPATH/src/xxx这个项目，我们可以将这里的src联想为Java开发中的Maven仓库，src内是一个个依赖。`go get`命令所对应的效果就是给所选择的项目引入远程的包，可以理解为引入依赖。而对于某个项目中，若有`package main`，则这个go文件是主文件，将会被编译运行。

[Go mod包依赖管理工具使用详解](https://juejin.cn/post/7026523806730551333#heading-9)

### 2.2声明

#### 变量的定义

> 在Golang中定义一个变量，需要使用`var`关键字，而与C或者Java不同的是，我们需要将这个变量的类型写在变量名的**后面**。不仅如此，在Golang中，允许我们一次性定义多个变量并同时赋值。

```go
var a int
var b float32
var c, d float64
e, f := 9, 10
var g = "Ricardo"
```

还有另外的一种做法，是使用`:=`这个符号。使用了这个符号之后，开发者不再需要写`var`关键字，只需要定义变量名，并在后面进行赋值即可。并且，Golang编译器会根据后面的值的类型，自动推导出变量的类型。在变量的定义过程中，如果定义的时候就赋予了变量的初始值，是不需要再声明变量的类型的，如变量`g`。

---

**注意，Golang是强类型的一种语言，所有的变量必须拥有类型，并且变量仅仅可以存储特定类型的数据。**

#### 匿名变量

> _标识符为 _（下划线）的变量，是系统保留的匿名变量，在赋值后，会被立即释放，称之为匿名变量。其作用是变量占位符，对其变量赋值结构。通常会在批量赋值时使用。例如，函数返回多个值，我们仅仅需要**其中部分**，则不需要的使用 _（下划线）来占位_

```go
func main() {
  // 调用函数，仅仅需要第二个返回值，第一，三使用匿名变量占位
  _, v, _ := getData()
  fmt.Println(v)
}
// 返回两个值的函数
func getData() (int, int, int) {
  // 返回3个值
  return 2, 4, 8
}
```

#### 常量

使用`const`关键字，并且**不能**使用`:=`标识符。值得一提的是在go语言中常量没有确定的类型，它会根据上下文推导出确定的类型。

### 2.3判断

在Golang中，不需要小括号，但是大括号是必须的。如下：

```go
func pow(x, n, lim float64) float64 {
	if v := math.Pow(x, n); v < lim {
	    return v
	}
	return lim
}
```

除去不需要写小括号以外，Golang还允许在判断条件之前执行一个简单的语句，并用一个分号`；`隔开。

### 2.4循环

在Golang中，只有一种循环，**for**循环。和判断语句一样，在Golang中也是没有小括号的。

```css
func main() {
	sum := 0
	for i := 0; i < 10; i++ {
		sum += i
	}
	fmt.Println(sum)
}
```

此外，在循环条件中，初始化语句和后置语句是可选的，这个时候把分号去掉，`for循环`就变成了`while循环`。

```bash
func main() {
	sum := 1
	for sum < 1000 {
		sum += sum
	}
	fmt.Println(sum)
}
```

不仅如此，如果省略循环条件，该循环就不会结束，因此无限循环可以写得很紧凑，这个时候，和`while(true)`的效果是一样的。

```csharp
func main() {
	for {
	    ...
	}
}
```

### 2.5switch

switch 语句执行的过程从上至下，直到找到匹配项，匹配项后面也不需要再加 break。

switch 的 default 不论放在哪都是最后执行。

switch 默认情况下 case 最后自带 break 语句，匹配成功后就不会执行其他 case，如果我们需要执行后面的 case，可以使用 **fallthrough** 。

```go
package main

import "fmt"

func main() {

    switch {
    case false:
            fmt.Println("1、case 条件语句为 false")
            fallthrough
    case true:
            fmt.Println("2、case 条件语句为 true")
            fallthrough
    case false:
            fmt.Println("3、case 条件语句为 false")
            fallthrough
    case true:
            fmt.Println("4、case 条件语句为 true")
    case false:
            fmt.Println("5、case 条件语句为 false")
            fallthrough
    default:
            fmt.Println("6、默认 case")
    }
}
//2、case 条件语句为 true
//3、case 条件语句为 false
//4、case 条件语句为 true
```

支持多条件匹配

```go
switch{
    case 1,2,3,4:
    default:
}
```

不同的 case 之间不使用 break 分隔，默认只会执行一个 case。

如果想要执行多个 case，需要使用 fallthrough 关键字，也可用 break 终止。

```go
switch{
    case 1:
    ...
    if(...){
        break
    }

    fallthrough // 此时switch(1)会执行case1和case2，但是如果满足if条件，则只执行case1

    case 2:
    ...
    case 3:
}
```

#### Type Switch

switch 语句还可以被用于 type-switch 来判断某个 interface 变量中实际存储的变量类型。

```go
switch x.(type){
    case type:
       statement(s);      
    case type:
       statement(s); 
    /* 你可以定义任意个数的case */
    default: /* 可选 */
       statement(s);
}
```

### 2.6函数

#### 函数的定义

在Golang的函数定义中，所有的函数都以`func`开头，并且Golang命名推荐使用驼峰命名法。

> **注意，在Golang的函数中，如果首字母是小写，则只能在包内使用；如果首字母是大写，则可以在包外被引入使用。可以理解为，使用小写的函数，是`private`的，使用大写的函数，是`public`的。**

在Golang的函数定义中，一样可以不接受参数，或者接受多个参数。而在参数的定义过程中，也是按照定义变量的格式，先定义变量名，再声明变量类型。对于函数的返回类型，也是按照这样的格式，先写函数名，再写返回类型：

```csharp
func add(x int, y int) int {
	return x + y
}

func main() {
	fmt.Println(add(42, 13))
}
```

并且，对于相同类型的两个参数，参数类型可以只写一个，用法如下：

```go
func add(x, y int) int {
	return x + y
}
```

在Golang中，对于函数的返回值，和C以及Java是不一样的。

> Golang中的函数可以返回任意多个返回值。

例如下面的小例子，

```go
func swap(x, y string) (string, string) {
	return y, x
}

func main() {
	a, b := swap("hello", "world")
	fmt.Println(a, b)
}
```

其次，函数的返回值是可以被命名的：

```python
func split(sum int) (x, y int) {
	x = sum * 4 / 9
	y = sum - x
	return
}
```

在这里，我们可以理解为在函数的顶部预先定义了这些变量值，而空的`return`语句则默认返回所有已经定义的返回变量。

#### defer

在Golang中，有一个关键字叫`defer`。

> defer 语句会将函数推迟到外层函数返回之后执行。 推迟调用的函数其参数会立即求值，但直到外层函数返回前该函数都不会被调用。

```go
func main() {
	defer fmt.Println("world")

	fmt.Println("hello")
}
```

在这段代码中，本来的执行路径是从上往下，也就是先输出“world”，然后再输出“hello”。但是因为`defer`这个关键字的存在，这行语句将在最后才执行，所以产生了先打印“hello”然后再打印“world”的效果。

> 注意，defer后面必须是函数调用语句，不能是其他语句，否则编译器会报错。

可以考虑到的场景是，文件的关闭，或数据库连接的释放等，这样打开和关闭的代码写在一起，既可以使得代码更加的整洁，也可以防止出现开发者在写了长长的业务代码后，忘记关闭的情况。

至于defer的底层实现，简单来讲就是将defer语句后面的函数调用的地址压进一个栈中，在当前的函数执行完毕，CPU即将执行函数外的下一行代码之前，先把栈中的指令地址弹出给CPU执行，直到栈为空，才结束这个函数，继续执行后面的代码。

从上文刚刚的表述中也可以推断出，如果有多条refer语句，将会从下往上依次执行。

### 2.7指针

在Golang中，指针的用法和 C 是一样的。同样是用`&`取地址，用`*`取地址中的值。

> 但是，与 C 不同，Golang没有指针运算。

### 2.8 数组

在Golang中，数组的定义是这样的：

```go
var a [10]int
```

这样做会将变量 a 声明为拥有 10 个整数的数组。

> 注意，在Golang中，数组的大小也同样和 C 语言一样不能改变。

#### 切片

数组的切片，顾名思义，就是将一个数组按需切出自己所需的部分。

每个数组的大小都是固定的。而切片则为数组元素提供动态大小的、灵活的视角。在实践中，切片比数组更常用。

切片通过两个下标来界定，即一个上界和一个下界，二者以冒号分隔：

```go
a[low : high]
```

它会选择一个半开区间，包括第一个元素，但排除最后一个元素。

以下表达式创建了一个切片，它包含 a 中下标从 1 到 3 的元素：

```css
a[1:4]
```

举个例子：

```go
func main() {
	str := [4]string{
	    "aaa",
	    "bbb",
	    "ccc",
	    "ddd",
	}
	fmt.Println(str)

	a := str[0:2]
	b := str[1:3]
	fmt.Println(a, b)

	b[0] = "XXX"
	fmt.Println(a, b)
	fmt.Println(str)
}
```

我们定义了一个数组，里面含有"aaa","bbb","ccc","ddd"四个元素。然后我们定义了两个切片，`a`和`b`，根据定义可以知道，`a`为"aaa"和"bbb"，`b`为"bbb"和"ccc"。

这个时候，我们把b[0]改成了"XXX"，那么`b`变成了"XXX"和"ccc"，这是毋庸置疑的。但是与直觉相违背的是，这个时候的数组`str`，也变成了"aaa","XXX","ccc","ddd"。

这是因为，Golang中的切片，不是拷贝，而是定义了新的指针，指向了原来数组所在的内存空间。所以，修改了切片数组的值，也就相应的修改了原数组的值了。

此外，切片可以用append增加元素。但是，如果此时底层数组容量不够，此时切片将会指向一个重新分配空间后进行拷贝的数组。

**因此可以得出结论：**

- 切片并不存储任何数据，它只是描述了底层数组中的一段。
- 更改切片的元素会修改其底层数组中对应的元素。
- 与它共享底层数组的切片都会观测到这些修改。

#### make

> 切片可以用内建函数 make 来创建，这也是你创建动态数组的方式。
>
> ```
> 在此之前需要解释两个定义，len（长度）和cap（容量）。
> len是数组的长度，指的是这个数组在定义的时候，所约定的长度。  
> cap是数组的容量，指的是底层数组的长度，也可以说是原数组在内存中的长度。
> 在前文中所提到的切片，如果我定义了一个str[0,0]的切片，此时的长度为0，但是容量依旧还是5。
> ```

make 函数会分配一个元素为零值的数组并返回一个引用了它的切片：

```go
a := make([]int, 5)  // len(a)=5
```

要指定它的容量，需向 make 传入第三个参数：

```go
b := make([]int, 0, 5) // len(b)=0, cap(b)=5

b = b[:cap(b)] // len(b)=5, cap(b)=5
b = b[1:]      // len(b)=4, cap(b)=4
```

也就是说，make函数可以自定义切片的大小。用Java的话来说，他可以被重载。

有两种形式，如果只有两个参数，第一个参数是数组内元素的类型，第二个参数是数组的长度（此时长度和容量都为5）。

而如果有第三个参数，那么第三个参数可以指定数组的容量，即可以指定这个数组在内存中分配多大的空间。

### 2.9Map

可以使用内建函数 make 也可以使用 map 关键字来定义 Map（如果不初始化 map，那么就会创建一个 nil map。nil map 不能用来存放键值对）:

```go
/* 声明变量，默认 map 是 nil */
var map_variable map[key_data_type]value_data_type

/* 使用 make 函数 */
map_variable := make(map[key_data_type]value_data_type)

//实例演示
package main

import "fmt"

func main() {
    var countryCapitalMap map[string]string /*创建集合 */
    countryCapitalMap = make(map[string]string)

    /* map插入key - value对,各个国家对应的首都 */
    countryCapitalMap [ "France" ] = "巴黎"
    countryCapitalMap [ "Italy" ] = "罗马"
    countryCapitalMap [ "Japan" ] = "东京"
    countryCapitalMap [ "India " ] = "新德里"

    /*使用键输出地图值 */
    for country := range countryCapitalMap {
        fmt.Println(country, "首都是", countryCapitalMap [country])
    }

    /*查看元素在集合中是否存在 */
    capital, ok := countryCapitalMap [ "American" ] /*如果确定是真实的,则存在,否则不存在 */
    /*fmt.Println(capital) */
    /*fmt.Println(ok) */
    if (ok) {
        fmt.Println("American 的首都是", capital)
    } else {
        fmt.Println("American 的首都不存在")
    }
}
/*
以上实例运行结果为：

France 首都是 巴黎
Italy 首都是 罗马
Japan 首都是 东京
India  首都是 新德里
American 的首都不存在
*/
```

#### delete()

> delete() 函数用于删除集合的元素, 参数为 map 和其对应的 key。实例如下：

```go
package main

import "fmt"

func main() {
        /* 创建map */
        countryCapitalMap := map[string]string{"France": "Paris", "Italy": "Rome", "Japan": "Tokyo", "India": "New delhi"}

        fmt.Println("原始地图")

        /* 打印地图 */
        for country := range countryCapitalMap {
                fmt.Println(country, "首都是", countryCapitalMap [ country ])
        }

        /*删除元素*/ delete(countryCapitalMap, "France")
        fmt.Println("法国条目被删除")

        fmt.Println("删除元素后地图")

        /*打印地图*/
        for country := range countryCapitalMap {
                fmt.Println(country, "首都是", countryCapitalMap [ country ])
        }
}
/*
以上实例运行结果为：

原始地图
India 首都是 New delhi
France 首都是 Paris
Italy 首都是 Rome
Japan 首都是 Tokyo
法国条目被删除
删除元素后地图
Italy 首都是 Rome
Japan 首都是 Tokyo
India 首都是 New delhi
*/
```

### 2.10Range

Go 语言中 range 关键字用于 for 循环中迭代数组(array)、切片(slice)、通道(channel)或集合(map)的元素。在数组和切片中它返回元素的索引和索引对应的值，在集合中返回 key-value 对。

```go
package main

import "fmt"

var pow = []int{1, 2, 4, 8, 16, 32, 64, 128}

func main() {
   for i, v := range pow {
      fmt.Printf("2**%d = %d\n", i, v)
   }
}
/*
2**0 = 1
2**1 = 2
2**2 = 4
2**3 = 8
2**4 = 16
2**5 = 32
2**6 = 64
2**7 = 128
*/
//-------------------------------------------
package main
import "fmt"

func main() {
    map1 := make(map[int]float32)
    map1[1] = 1.0
    map1[2] = 2.0
    map1[3] = 3.0
    map1[4] = 4.0
   
    // 读取 key 和 value
    for key, value := range map1 {
      fmt.Printf("key is: %d - value is: %f\n", key, value)
    }

    // 读取 key
    for key := range map1 {
      fmt.Printf("key is: %d\n", key)
    }

    // 读取 value
    for _, value := range map1 {
      fmt.Printf("value is: %f\n", value)
    }
}

/*
key is: 4 - value is: 4.000000
key is: 1 - value is: 1.000000
key is: 2 - value is: 2.000000
key is: 3 - value is: 3.000000
key is: 1
key is: 2
key is: 3
key is: 4
value is: 1.000000
value is: 2.000000
value is: 3.000000
value is: 4.000000
*/
```

### 2.11结构体

而在Golang中，则没有对象这一说法，因为Golang是一个面向过程的语言。但是，我们又知道面向对象在开发中的便捷性，所以我们在Golang中有了**结构体**这一类型。

> 结构体是复合类型，当需要定义类型，它由一系列属性组成，每个属性都有自己的类型和值的时候，就应该使用结构体，它把数据聚集在一起。 组成结构体类型的那些数据成为字段（fields）。每个字段都有一个类型和一个名字；在一个结构体中，字段名字必须是唯一的。**注意，在结构体中也遵循用大小写来设置公有或私有的规则。如果这个结构体名字的第一个字母是大写，则可以被其他包访问，否则，只能在包内进行访问。而结构体内的字段也一样，也是遵循一样的大小写确定可用性的规则。**

#### 定义

对于结构体，他的定义方式如下：

```go
type 结构体名 struct {
    字段1 类型
    字段2 类型
}
```

#### 声明

对于结构体的声明和初始化，有以下几种形式：

1. **使用var关键字**

   ```go
   var s T
   s.a = 1
   s.b = 2
   ```

   注意，在使用了`var`关键字之后**不需要初始化**，这和其他的语言有些不同。Golang会自动分配内存空间，并将该内存空间设置为默认的值，我们只需要按需进行赋值即可。

2. **使用new函数**

   ```go
   type people struct {
       name string
       age int
   }
   
   func main() {
       ming := new(people)
       ming.name = "xiao ming"
       ming.age = 18
   }
   ```

3. **使用字面量**

   ```go
   type people struct {
       name string
       age int
   }
   
   func main() {
       ming := &people{"xiao ming", 18}
   }
   ```

这几种方式的区别：第一种使用`var`声明的方式，返回的是该实例的结构类型，而第二第三种，返回的是一个指向这个结构类型的一个**指针**，是地址。

> 相关链接：[golang | 是返回struct还是返回struct的指针](https://cloud.tencent.com/developer/article/1861199)

对于第二第三种返回指针的声明形式，在我们需要修改他的值的时候，其实应该使用的方式是:

```go
(*ming).name = "xiao wang"
```

也就是说，对于指针类型的数值，应该要先用`*`取值，然后再修改。

但是，在Golang中，可以省略这一步骤，直接使用`ming.name = "xiao wang"`。尽管如此，我们应该知道这一行为的原因，分清楚自己所操作的对象究竟是什么类型。

### 2.12方法

> 在Go语言中有一个概念，它和方法有着同样的名字，并且大体上意思相同，Go的 方法是作用在接收器（receiver）上的一个函数，接收器是某种类型的变量，因此方法是**一种特殊类型的函数**。

在函数的基础上，在`func`和`函数名`之间，加上接受者的类型，就可以定义一个方法。

```go
type Vertex struct {
    X, Y float64
}

func (v Vertex) Abs() float64 {
	return math.Sqrt(v.X*v.X + v.Y*v.Y)
}

func main() {
	v := Vertex{3, 4}
	fmt.Println(v.Abs())
}
```

可以看到，我们定义了一个`Vertex`为接收者的方法。也就是说，这个方法，仅仅可以被`Vertex`的结构体数值调用。

> 注意，接受者有两种类型，即指针接收者和非指针接受者。

```go
type Vertex struct {
	X, Y float64
}

func (v Vertex) test1(){
    v.X++;
    v.Y++;
}

func (v *Vertex) test2(){
    v.X++;
    v.Y++;   
}
```

在这里我们定义了两个方法，`test1`和`test2`，他们唯一的区别就是方法名前面的接收者不同，一个是指针类型的，一个是值类型的。

并且，执行这两个方法，也需要定义不同的结构体类型。

```go
v1 := Vertex{1, 1}
v2 := &Vertex{1, 1}

v1.test1()
v2.test2()

fmt.Println(v1)
fmt.Println(v2)
//执行结果：
//{1 1}
//&{2 2}
```

也就是说，只有指针接收者类型的方法，才能修改这个接收器的成员值，非指针接收者，方法修改的只是这个传入的指针接收者的一个**拷贝**。

还有一点需要补充说明：不管是指针接收者还是非指针接收者，他在接受一个对象的时候，会自动将这个对象转换为这个方法所需要的类型。也就是说，如果我现在有一个非指针类型的对象，去调用一个指针接收者的方法，那么这个对象将会自动被**取地址**然后再被调用。

> 换句话说，方法的调用类型不重要，重要的是方法是**怎么定义**的。

### 2.13接口

把所有的具有共性的方法定义在一起，任何其他类型只要实现了这些方法就是实现了这个接口。

```go
/* 定义接口 */
type interface_name interface {
   method_name1 [return_type]
   method_name2 [return_type]
   method_name3 [return_type]
   ...
   method_namen [return_type]
}

/* 定义结构体 */
type struct_name struct {
   /* variables */
}

/* 实现接口方法 */
func (struct_name_variable struct_name) method_name1() [return_type] {
   /* 方法实现 */
}
...
func (struct_name_variable struct_name) method_namen() [return_type] {
   /* 方法实现*/
}
//实例：
package main

import (
    "fmt"
)

type Phone interface {
    call()
}

type NokiaPhone struct {
}

func (nokiaPhone NokiaPhone) call() {
    fmt.Println("I am Nokia, I can call you!")
}

type IPhone struct {
}

func (iPhone IPhone) call() {
    fmt.Println("I am iPhone, I can call you!")
}

func main() {
    var phone Phone

    phone = new(NokiaPhone)
    phone.call()

    phone = new(IPhone)
    phone.call()

}
//结果
//I am Nokia, I can call you!
//I am iPhone, I can call you!
```