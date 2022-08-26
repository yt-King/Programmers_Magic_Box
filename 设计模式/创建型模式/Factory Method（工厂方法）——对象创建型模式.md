#  Factory Method（工厂方法）——对象创建型模式

## 1.意图

定义一个用于创建对象的接口，让子类决定实例化哪一个。Factory Method使一个类的实例化延迟到子类。

## 2.动机

框架使用抽象类定义和维护对象之间的关系，这些对象的创建通常也由框架负责。

考虑这样一个应用框架，他可以向用户展示多个文档，在这个框架中，两个主要的抽象类时Application和Document。这两个类都是抽象的，客户必须用过他的子类来做具体应用相关的实现。例如，为了创建一个绘图应用，我们定义DrawingApplication和DrawingDocument。Application类负责管理Document并根据需要创建他们——例如，当用户从菜单中选择open或者new的时候。

因为被实例化的特定Document子类是与特定的应用相关的，所以Application类不可能预测到哪一个Document子类将被实例化——Application类进知道一个新的文档合何时应被创建，而不知道Document将被创建。这就产生了一个尴尬的局面：框架必须实例化类，但是只知道不能被实例化的抽象类。

**假设**你正在开发一款物流管理应用。 最初版本只能处理卡车运输， 因此大部分代码都在位于名为 `卡车`的类中。一段时间后， 这款应用变得极受欢迎。 你每天都能收到十几次来自海运公司的请求， 希望应用能够支持海上物流功能。但是代码问题该如何处理呢？ 目前， 大部分代码都与 `卡车`类相关。 在程序中添加 `轮船`类需要修改全部代码。 更糟糕的是， 如果你以后需要在程序中支持另外一种运输方式， 很可能需要再次对这些代码进行大幅修改。

![image-20220826141841629](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261418692.png)

## 3.解决方案

工厂方法模式建议使用特殊的*工厂*方法代替对于对象构造函数的直接调用 （即使用 `new`运算符）。 不用担心， 对象仍将通过 `new`运算符创建， 只是该运算符改在工厂方法中调用罢了。 工厂方法返回的对象通常被称作 “产品”。

![image-20220826141845313](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261418368.png)

 现在你可以在子类中重写工厂方法， 从而改变其创建产品的类型。但有一点需要注意:仅当这些产品具有共同的基类或者接口时， 子类才能返回不同类型的产品， 同时基类中的工厂方法还应将其返回类型声明为这一共有接口。

![image-20220826141848260](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261418322.png)

举例来说，  `卡车`Truck和 `轮船`Ship类都必须实现 `运输`Transport接口， 该接口声明了一个名为 `deliver`交付的方法。 每个类都将以不同的方式实现该方法： 卡车走陆路交付货物， 轮船走海路交付货物。  `陆路运输`Road­Logistics类中的工厂方法返回卡车对象， 而 `海路运输`Sea­Logistics类则返回轮船对象。

![image-20220826141851192](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261418296.png)

## 4.适用性

- 当一个类不知道他所必须创建的对象的类的时候
- 当一个类希望由他的子类来指定它所创建的的对象的时候
- 当类将创建对像的职责委托给多个帮助子类中的某一个，并且你希望将哪一个帮助子类是代理者这一信息局部化的时候

## 5.结构

![image-20220826141854702](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261418776.png)

## 6.参与者

- Product

  —— 定义工厂方法所创建的对象的接口

- ConcreteProduct

  —— 实现Product接口

- Creator

  —— 声明工厂方法，该方法返回一个Product类型的对象。Creator也可以定义一个工厂方法的缺省实现，他返回一个缺省的ConcreteProduct对象

  —— 可以调用工厂方法以创建一个Product对象

- ConcereteCreator

  —— 重定义工厂方法以返回一个ConcreteProduct实例

## 7.优缺点分析

工厂方法不再将和特定应用有关的类绑定到你的代码中。代码仅处理Product接口，因此他可以与用户定义的任何ConcreteProduct类一起使用。

![image-20220826141859055](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261418120.png)

## 8.实现

当应用 Factory Method 模式时要考虑下面一些问题：

**主要有两种不同的情况**   Factory Method 模式主要有两种不同的情况：1.Creator是一个抽象类并且不提供他所申明的工厂方法实现；2.是一个具体的类而且为工厂方法提供了一个缺省的实现。第一种情况需要子类定义来实现，因为没有合理的缺省实现，他避免了不得不实例化不可预见类的问题。第二种情况中，具体的Creator遵循的准则时——用一个独立的操作创建对象，这样子类才能重定义他们的创建方式。这条准则保证了子类的设计者能够在必要的时候改变父类所实例化的对象的类。

## 9.代码

以下示例演示了如何使用**工厂方法**开发跨平台 UI （用户界面） 组件， 并同时避免客户代码与具体 UI 类之间的耦合。

```c++
// 创建者类声明的工厂方法必须返回一个产品类的对象。创建者的子类通常会提供
// 该方法的实现。
class Dialog is
    // 创建者还可提供一些工厂方法的默认实现。
    abstract method createButton():Button

    // 请注意，创建者的主要职责并非是创建产品。其中通常会包含一些核心业务
    // 逻辑，这些逻辑依赖于由工厂方法返回的产品对象。子类可通过重写工厂方
    // 法并使其返回不同类型的产品来间接修改业务逻辑。
    method render() is
        // 调用工厂方法创建一个产品对象。
        Button okButton = createButton()
        // 现在使用产品。
        okButton.onClick(closeDialog)
        okButton.render()


// 具体创建者将重写工厂方法以改变其所返回的产品类型。
class WindowsDialog extends Dialog is
    method createButton():Button is
        return new WindowsButton()

class WebDialog extends Dialog is
    method createButton():Button is
        return new HTMLButton()


// 产品接口中将声明所有具体产品都必须实现的操作。
interface Button is
    method render()
    method onClick(f)

// 具体产品需提供产品接口的各种实现。
class WindowsButton implements Button is
    method render(a, b) is
        // 根据 Windows 样式渲染按钮。
    method onClick(f) is
        // 绑定本地操作系统点击事件。

class HTMLButton implements Button is
    method render(a, b) is
        // 返回一个按钮的 HTML 表述。
    method onClick(f) is
        // 绑定网络浏览器的点击事件。


class Application is
    field dialog: Dialog

    // 程序根据当前配置或环境设定选择创建者的类型。
    method initialize() is
        config = readApplicationConfigFile()

        if (config.OS == "Windows") then
            dialog = new WindowsDialog()
        else if (config.OS == "Web") then
            dialog = new WebDialog()
        else
            throw new Exception("错误！未知的操作系统。")

    // 当前客户端代码会与具体创建者的实例进行交互，但是必须通过其基本接口
    // 进行。只要客户端通过基本接口与创建者进行交互，你就可将任何创建者子
    // 类传递给客户端。
    method main() is
        this.initialize()
        dialog.render()
```

