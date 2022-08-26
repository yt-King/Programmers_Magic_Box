# 抽象工厂

## 1.意图

提供一个接口一创建一系列相关或相互依赖的对象而无需指定他们具体的类。

## 2.动机

假设你正在开发一款家具商店模拟器。 你的代码中包括一些类， 用于表示：

1. 一系列相关产品， 例如 `椅子`Chair 、  `沙发`Sofa和 `咖啡桌`Coffee­Table 。
2. 系列产品的不同变体。 例如， 你可以使用 `现代`Modern 、  `维多利亚`Victorian 、  `装饰风艺术`Art­Deco等风格生成 `椅子` 、  `沙发`和 `咖啡桌` 。

![image-20220826141736348](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261417412.png)

为了保证各个产品之间的可以执行，一个应用不应该为一个特定的外观或者风格进行硬编码，在整个应用中实例化特定的产品类型将使得后续难以进行修改。

## 3.解决方案

通过定义一个抽象的Factory类来解决这个问题，首先， 抽象工厂模式建议为系列中的每件产品明确声明接口 （例如椅子、 沙发或咖啡桌）。 然后， 确保所有产品变体都继承这些接口。 例如， 所有风格的椅子都实现 `椅子`接口； 所有风格的咖啡桌都实现 `咖啡桌`接口， 以此类推。

接下来， 我们需要声明*抽象工厂*——包含系列中所有产品构造方法的接口。 例如 `create­Chair`创建椅子 、  `create­Sofa`创建沙发和 `create­Coffee­Table`创建咖啡桌 。 这些方法必须返回**抽象**产品类型， 即我们之前抽取的那些接口：  `椅子`， `沙发`和 `咖啡桌`等等。

那么该如何处理产品变体呢？ 对于系列产品的每个变体， 我们都将基于 `抽象工厂`接口创建不同的工厂类。 每个工厂类都只能返回特定类别的产品， 例如，  `现代家具工厂`Modern­Furniture­Factory只能创建 `现代椅子`Modern­Chair 、  `现代沙发`Modern­Sofa和 `现代咖啡桌`Modern­Coffee­Table对象。

![image-20220826141741529](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261417592.png)

客户仅通过抽象工厂接口创建产品，而不知道那些类实现了特定的产品。换言之，客户仅通过抽象类定义的接口交互，而不是用特定的具体类的接口。同时抽象工厂也增强了产品系列之间的依赖关系，一个维多利亚风格的咖啡桌应该与维多利亚风格的沙发，椅子一起使用，这一约束条件是作为使用维多利亚风格的具体工厂类的结果自动加上的。

最后一点说明： 如果客户端仅接触抽象接口， 那么谁来创建实际的工厂对象呢？ 一般情况下， 应用程序会在初始化阶段创建具体工厂对象。 而在此之前， 应用程序必须根据配置文件或环境设定选择工厂类别。

## 4.适用性

- 一个系统要独立于他的产品的创建、组合和表示
- 一个系统要由多个产品系列中的一个来配置
- 要强调一系列相关的产品对象的设计以便进行联合使用
- 提供一个产品类库，但是只想显示他们的接口而不是实现

## 5.结构

![image-20220826141748287](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261417361.png)

## 6.参与者

- AbstractFactory

  ——声明一个创建抽象产品对象的操作接口

- ConcreateFactory

  ——实现创建具体产品对象的操作

- AbstractProduct

  ——为一类产品对象声明一个接口

- ConcreteProduct

  ——定义一个将被相应的具体工厂创建的产品对象

  ——实现AbstractProduct接口

- Client

  ——仅使用AbstractFactory和AbstractProduct类声明的接口

## 7.优缺点分析

1. 分离了具体的类

   抽象工厂模式可以帮助开发者控制一个应用创建的对象的类。因为一个工厂封装创建产品对象的责任和过程，它将客户和类的实现分离，客户通过他们的抽象接口操纵实例。产品的类名也在具体工厂的实现中被隔离，即他们不出现在客户代码中。

2. 易于交换产品系列

   一个具体的工厂类在一个应用中仅出现一次——在他初始化的时候。所以改变一个应用的具体工厂变得很容易，在初始化的时候通过改变具体的实现工厂就可以使用不同的产品配置。在下面的代码示例中就是通过事先设好的逻辑判断使用哪一种工厂。

3. 有利于产品的一致性

   当一个系列中的产品对象被设计成一起工作时，一个应用一次只能使用同一个系列中的对象，这一点很重要。

4. 难以支持新种类的产品

   抽象工厂接口确定了可以被创建的产品集合，支持新种类的产品就需要扩展该工厂的接口，这就涉及到所有子类的改变。

![image-20220303092752409](Abstract%20Factory%EF%BC%88%E6%8A%BD%E8%B1%A1%E5%B7%A5%E5%8E%82%EF%BC%89%E2%80%94%E2%80%94%E5%AF%B9%E8%B1%A1%E5%88%9B%E5%BB%BA%E5%9E%8B%E6%A8%A1%E5%BC%8F.images/image-20220303092752409.png)

## 8.实现

1. 将工厂作为单件

   最好将抽象工厂实现为一个Singleton（单例模式）

2. 创建产品

   AbstractFactory仅声明一个创建产品的接口，真正创建产品由ConcreateFactory子类实现。最通常的方法是为一个产品定义一个工厂方法（后续文章会讲解工厂方法模式）。如果有多个可能的产品系列，具体工厂也可以使用Prototype模式实现。

3. 定义可扩展的工厂

   一个灵活但是不太安全的设计是给创建对象的操作增加一个参数。该参数指定了将创建的对象的种类。可以是一个标识符，一个整数，一个字符串等等，只要可以表示这个产品就行。使用这种方法的AbstractFactory是需要一个“Make”操作和一个指示要创建对象的种类的参数即可。

## 9.代码示例

通过应用**抽象工厂**模式， 使得客户端代码无需与具体 UI 类耦合， 就能创建跨平台的 UI 元素， 同时确保所创建的元素与指定的操作系统匹配。

```c++
// 抽象工厂接口声明了一组能返回不同抽象产品的方法。这些产品属于同一个系列
// 且在高层主题或概念上具有相关性。同系列的产品通常能相互搭配使用。系列产
// 品可有多个变体，但不同变体的产品不能搭配使用。
interface GUIFactory is
    method createButton():Button
    method createCheckbox():Checkbox


// 具体工厂可生成属于同一变体的系列产品。工厂会确保其创建的产品能相互搭配
// 使用。具体工厂方法签名会返回一个抽象产品，但在方法内部则会对具体产品进
// 行实例化。
class WinFactory implements GUIFactory is
    method createButton():Button is
        return new WinButton()
    method createCheckbox():Checkbox is
        return new WinCheckbox()

// 每个具体工厂中都会包含一个相应的产品变体。
class MacFactory implements GUIFactory is
    method createButton():Button is
        return new MacButton()
    method createCheckbox():Checkbox is
        return new MacCheckbox()


// 系列产品中的特定产品必须有一个基础接口。所有产品变体都必须实现这个接口。
interface Button is
    method paint()

// 具体产品由相应的具体工厂创建。
class WinButton implements Button is
    method paint() is
        // 根据 Windows 样式渲染按钮。

class MacButton implements Button is
    method paint() is
        // 根据 macOS 样式渲染按钮

// 这是另一个产品的基础接口。所有产品都可以互动，但是只有相同具体变体的产
// 品之间才能够正确地进行交互。
interface Checkbox is
    method paint()

class WinCheckbox implements Checkbox is
    method paint() is
        // 根据 Windows 样式渲染复选框。

class MacCheckbox implements Checkbox is
    method paint() is
        // 根据 macOS 样式渲染复选框。

// 客户端代码仅通过抽象类型（GUIFactory、Button 和 Checkbox）使用工厂
// 和产品。这让你无需修改任何工厂或产品子类就能将其传递给客户端代码。
class Application is
    private field factory: GUIFactory
    private field button: Button
    constructor Application(factory: GUIFactory) is
        this.factory = factory
    method createUI() is
        this.button = factory.createButton()
    method paint() is
        button.paint()


// 程序会根据当前配置或环境设定选择工厂类型，并在运行时创建工厂（通常在初
// 始化阶段）。
class ApplicationConfigurator is
    method main() is
        config = readApplicationConfigFile()

        if (config.OS == "Windows") then
            factory = new WinFactory()
        else if (config.OS == "Mac") then
            factory = new MacFactory()
        else
            throw new Exception("错误！未知的操作系统。")

        Application app = new Application(factory)
```

