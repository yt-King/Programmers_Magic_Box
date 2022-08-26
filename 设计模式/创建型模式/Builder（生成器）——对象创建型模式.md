# 生成器

## 1.意图

将一个复杂对象的构建与他的表示分离，使得同样的构建过程可以创建不同的表示。

## 2.动机

例如， 我们来思考如何创建一个 `房屋`House对象。 建造一栋简单的房屋， 首先你需要建造四面墙和地板， 安装房门和一套窗户， 然后再建造一个屋顶。 但是如果你想要一栋更宽敞更明亮的房屋， 还要有院子和其他设施 （例如暖气、 排水和供电设备）， 那又该怎么办呢？

最简单的方法是扩展 `房屋`基类， 然后创建一系列涵盖所有参数组合的子类。 但最终你将面对相当数量的子类。 任何新增的参数 （例如门廊类型） 都会让这个层次结构更加复杂。

![image-20220826141802666](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261418739.png)

另一种方法则无需生成子类。 你可以在 `房屋`基类中创建一个包括所有可能参数的超级构造函数， 并用它来控制房屋对象。 这种方法确实可以避免生成子类， 但它却会造成另外一个问题。通常情况下， 绝大部分的参数都没有使用， 这使得[对于构造函数的调用十分不简洁](https://refactoringguru.cn/smells/long-parameter-list)。 例如， 只有很少的房子有游泳池， 因此与游泳池相关的参数十之八九是毫无用处的。

![image-20220826141807115](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261418178.png)

## 3.解决方案

生成器模式建议将对象构造代码从产品类中抽取出来， 并将其放在一个名为*生成器*的独立对象中。该模式会将对象构造过程划分为一组步骤， 比如 `build­Walls`创建墙壁和 `build­Door`创建房门创建房门等。 每次创建对象时， 你都需要通过生成器对象执行一系列步骤。 重点在于你无需调用所有步骤， 而只需调用创建特定对象配置所需的那些步骤即可。

![image-20220826141811677](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261418742.png)

当你需要创建不同形式的产品时， 其中的一些构造步骤可能需要不同的实现。 例如， 木屋的房门可能需要使用木头制造， 而城堡的房门则必须使用石头制造。在这种情况下， 你可以创建多个不同的生成器， 用不同方式实现一组相同的创建步骤。 然后你就可以在创建过程中使用这些生成器 （例如按顺序调用多个构造步骤） 来生成不同类型的对象。

**你可以进一步将用于创建产品的一系列生成器步骤调用抽取成为单独的*主管*类。 主管类可定义创建步骤的执行顺序， 而生成器则提供这些步骤的实现。**

## 4.适用性

- 当创建复杂对象的算法应该独立于该对象的组成部分以及他们的装配方式时
- 当构造过程必须允许被构造的对象有不同的表示时

## 5.结构模式

![image-20220826141815880](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261418961.png)

## 6.参与者

- Bulider

  —— 为创建一个Product对象的各个部件指定抽象接口

- ConcreteBulider

  —— 实现Bulider的接口以构造和装配该产品的各个部件

  —— 定义并跟踪他所创建的表示

  —— 提供一个检索产品的接口

- Director

  —— 构造一个使用Builder接口的对象

- Product

  —— 表示被构造的复杂对象，ConcreteBulider创建该产品的内部表示并定义它的装配过程

  —— 包含定义组成部件的类，包括将这些部件装配成最终产品的接口

## 7.协作

- 客户创建Director对象，并使用它所想要的Bulider对象进行配置
- 一旦生成了产品部件，Director会通知生成器
- 生成器处理Director的请求，并将部件添加到该产品中
- 客户从生成器中检索产品

严格来说， 你的程序中并不一定需要Director。 客户端代码可直接以特定顺序调用创建步骤。 不过，Director中非常适合放入各种例行构造流程， 以便在程序中反复使用。此外， 对于客户端代码来说，Director完全隐藏了产品构造细节。 客户端只需要将一个生成器与Director关联， 然后使用Director来构造产品， 就能从生成器处获得构造结果了。协作流程示意图如下：

![image-20220826141820483](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261418554.png)

## 8.优缺点分析

1. 它使你可以改变一个产品的内部表示

   Bulider对象提供给Director一个构造产品的抽象接口。该接口使得生成器可以隐藏这个产品的表示和内部结构，同时也隐藏也产品内的装配过程。因为产品是抽象构造的，在改变产品内部的表示时只需要定义新的生成器即可。

2. 将构造代码和表示代码分开

   生成器模式通过封装一个复杂对象的创建和表示方式提高了对象的模块性。客户不需要知道定义产品内部结构的类的所有信息，这些类是不出现在Builder接口中的。每个ConcreteBuilder包含了创建和装配一个特定产品的所有代码。这些代码只需要写一次，然后不同的Director可以复用它在相同部件集合的基础上构建不同的Product。

3. 它使得你可对构造过程进行更精细的控制

   Builder模式是在Director的控制下一步一步构造产品的。仅当该产品完成时才从生成器中取回他。因此Builder接口相比其他创建型模式能更好的反映产品的构造过程，使得可以更精细的控制构建过程，从而更精细的控制产品的内部结构。

   ![image-20220826141825059](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261418120.png)

## 9.实现

通常有一个抽象的Builder类为Director可能要创建的每一个构建定义一个操作。这些操作缺省的情况下什么都不做，只需要一个ConcreteBulider对他需要创建的构建重定义操作即可。

以构建汽车为例，如果客户端代码需要组装一辆与众不同、 精心调教的汽车， 它可以直接调用生成器。 或者， 客户端可以将组装工作委托给主管类， 因为主管类知道如何使用生成器制造最受欢迎的几种型号汽车。

```c++
// 只有当产品较为复杂且需要详细配置时，使用生成器模式才有意义。下面的两个
// 产品尽管没有同样的接口，但却相互关联。
class Car is
    // 一辆汽车可能配备有 GPS 设备、行车电脑和几个座位。不同型号的汽车（
    // 运动型轿车、SUV 和敞篷车）可能会安装或启用不同的功能。

class Manual is
    // 用户使用手册应该根据汽车配置进行编制，并介绍汽车的所有功能。


// 生成器接口声明了创建产品对象不同部件的方法。
interface Builder is
    method reset()
    method setSeats(...)
    method setEngine(...)
    method setTripComputer(...)
    method setGPS(...)

// 具体生成器类将遵循生成器接口并提供生成步骤的具体实现。你的程序中可能会
// 有多个以不同方式实现的生成器变体。
class CarBuilder implements Builder is
    private field car:Car

    // 一个新的生成器实例必须包含一个在后续组装过程中使用的空产品对象。
    constructor CarBuilder() is
        this.reset()

    // reset（重置）方法可清除正在生成的对象。
    method reset() is
        this.car = new Car()

    // 所有生成步骤都会与同一个产品实例进行交互。
    method setSeats(...) is
        // 设置汽车座位的数量。

    method setEngine(...) is
        // 安装指定的引擎。

    method setTripComputer(...) is
        // 安装行车电脑。

    method setGPS(...) is
        // 安装全球定位系统。

    // 具体生成器需要自行提供获取结果的方法。这是因为不同类型的生成器可能
    // 会创建不遵循相同接口的、完全不同的产品。所以也就无法在生成器接口中
    // 声明这些方法（至少在静态类型的编程语言中是这样的）。
    //
    // 通常在生成器实例将结果返回给客户端后，它们应该做好生成另一个产品的
    // 准备。因此生成器实例通常会在 `getProduct（获取产品）`方法主体末尾
    // 调用重置方法。但是该行为并不是必需的，你也可让生成器等待客户端明确
    // 调用重置方法后再去处理之前的结果。
    method getProduct():Car is
        product = this.car
        this.reset()
        return product

// 生成器与其他创建型模式的不同之处在于：它让你能创建不遵循相同接口的产品。
class CarManualBuilder implements Builder is
    private field manual:Manual

    constructor CarManualBuilder() is
        this.reset()

    method reset() is
        this.manual = new Manual()

    method setSeats(...) is
        // 添加关于汽车座椅功能的文档。

    method setEngine(...) is
        // 添加关于引擎的介绍。

    method setTripComputer(...) is
        // 添加关于行车电脑的介绍。

    method setGPS(...) is
        // 添加关于 GPS 的介绍。

    method getProduct():Manual is
        // 返回使用手册并重置生成器。


// 主管只负责按照特定顺序执行生成步骤。其在根据特定步骤或配置来生成产品时
// 会很有帮助。由于客户端可以直接控制生成器，所以严格意义上来说，主管类并
// 不是必需的。
class Director is
    private field builder:Builder

    // 主管可同由客户端代码传递给自身的任何生成器实例进行交互。客户端可通
    // 过这种方式改变最新组装完毕的产品的最终类型。
    method setBuilder(builder:Builder)
        this.builder = builder

    // 主管可使用同样的生成步骤创建多个产品变体。
    method constructSportsCar(builder: Builder) is
        builder.reset()
        builder.setSeats(2)
        builder.setEngine(new SportEngine())
        builder.setTripComputer(true)
        builder.setGPS(true)

    method constructSUV(builder: Builder) is
        // ...


// 客户端代码会创建生成器对象并将其传递给主管，然后执行构造过程。最终结果
// 将需要从生成器对象中获取。
class Application is

    method makeCar() is
        director = new Director()

        CarBuilder builder = new CarBuilder()
        director.constructSportsCar(builder)
        Car car = builder.getProduct()

        CarManualBuilder builder = new CarManualBuilder()
        director.constructSportsCar(builder)

        // 最终产品通常需要从生成器对象中获取，因为主管不知晓具体生成器和
        // 产品的存在，也不会对其产生依赖。
        Manual manual = builder.getProduct()
```

