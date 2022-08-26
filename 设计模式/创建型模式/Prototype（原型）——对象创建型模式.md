# Prototype（原型）——对象创建型模式

## 1.意图

用原型实例指定创建对象的种类，并且通过拷贝这些原型创建新的对象

## 2.动机

如果你有一个对象， 并希望生成与其完全相同的一个复制品， 你该如何实现呢？ 首先， 你必须新建一个属于相同类的对象。 然后， 你必须遍历原始对象的所有成员变量， 并将成员变量值复制到新对象中。但有个小问题。 并非所有对象都能通过这种方式进行复制， 因为有些对象可能拥有私有成员变量， 它们在对象本身以外是不可见的。

![image-20220826141904820](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261419884.png)

直接复制还有另外一个问题。 因为你必须知道对象所属的类才能创建复制品， 所以代码必须依赖该类。 即使你可以接受额外的依赖性， 那还有另外一个问题： 有时你只知道对象所实现的接口， 而不知道其所属的具体类， 比如可向方法的某个参数传入实现了某个接口的任何对象。

## 3.解决方案

原型模式将克隆过程委派给被克隆的实际对象。 模式为所有支持克隆的对象声明了一个通用接口， 该接口让你能够克隆对象， 同时又无需将代码和对象所属类耦合。 通常情况下， 这样的接口中仅包含一个 `克隆`方法。

所有的类对 `克隆`方法的实现都非常相似。 该方法会创建一个当前类的对象， 然后将原始对象所有的成员变量值复制到新建的类中。 你甚至可以复制私有成员变量， 因为绝大部分编程语言都允许对象访问其同类对象的私有成员变量。

支持克隆的对象即为*原型*。 当你的对象有几十个成员变量和几百种类型时， 对其进行克隆甚至可以代替子类的构造。其运作方式如下： 创建一系列不同类型的对象并不同的方式对其进行配置。 如果所需对象与预先配置的对象相同， 那么你只需克隆原型即可， 无需新建一个对象。

## 4.适用性

- 当一个系统应该独立于他的产品创建、构成和表示时
- 当要实例化的类是在运行时指定——例如通过动态加载
- 为了避免创建一个与产品类层次平行的工厂类层次
- 当一个类的实例只能有几个不同状态组合中的一种时。建立相对应数目的原型并克隆他们可能比每次用合适的状态手工实例化该类更方便一点

## 5.结构

![image-20220826141908966](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261419049.png)

##  6.参与者

- Prototype

  —— 声明一个克隆自身的接口

- ConcretePrototype

  —— 实现一个克隆自身的操作

- Client

  —— 让一个原型克隆自身从而创建一个新的对象

## 7.优缺点分析

原型模式有许多和抽象工厂和生成器类似的效果：他对客户隐藏了具体的产品类，因此减少了客户知道的名字的数目。此外，这些模式使得客户无需改变即可使用与特定应用相关的类。

**1）运行时增加和删除产品**  原型模式允许只通过客户注册原型实例就将一个新的具体的类并入系统。比其他的创建型模式更为灵活，因为客户可以再运行时建立和删除原型。

**2）改变值以指定新对象**  高度动态的系统允许你通过对象组合定义新的行为。通过实例化已有类并且将这些实例注册为客户对象的原型，就可以有效定义新类别的对象。客户可以将职责代理给原型，从而表现出新的行为，这种设计使得用户无需编程即可定义新”类“。

**3）改变结构以指定新对象**  许多应用由部件和子部件来创建对象。比如电路设计编辑器就是由子电路来构造电路的。为方便起见，这样的应用通常允许用户实例化复杂的、用户定义的结构。在原型模式中，只需将这个子电路作为一个原型增加到可用的电路元素选择版中。只需组合电路对象将Clone实现为一个深拷贝，具有不同结构的电路就可以是原型了。

**4）减少子类的构造**  

**5）用类动态配置应用**  

![image-20220826141912135](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208261419197.png)

## 8.示例

```c++
// 基础原型。
abstract class Shape is
    field X: int
    field Y: int
    field color: string

    // 常规构造函数。
    constructor Shape() is
        // ...

    // 原型构造函数。使用已有对象的数值来初始化一个新对象。
    constructor Shape(source: Shape) is
        this()
        this.X = source.X
        this.Y = source.Y
        this.color = source.color

    // clone（克隆）操作会返回一个形状子类。
    abstract method clone():Shape


// 具体原型。克隆方法会创建一个新对象并将其传递给构造函数。直到构造函数运
// 行完成前，它都拥有指向新克隆对象的引用。因此，任何人都无法访问未完全生
// 成的克隆对象。这可以保持克隆结果的一致。
class Rectangle extends Shape is
    field width: int
    field height: int

    constructor Rectangle(source: Rectangle) is
        // 需要调用父构造函数来复制父类中定义的私有成员变量。
        super(source)
        this.width = source.width
        this.height = source.height

    method clone():Shape is
        return new Rectangle(this)


class Circle extends Shape is
    field radius: int

    constructor Circle(source: Circle) is
        super(source)
        this.radius = source.radius

    method clone():Shape is
        return new Circle(this)


// 客户端代码中的某个位置。
class Application is
    field shapes: array of Shape

    constructor Application() is
        Circle circle = new Circle()
        circle.X = 10
        circle.Y = 10
        circle.radius = 20
        shapes.add(circle)

        Circle anotherCircle = circle.clone()
        shapes.add(anotherCircle)
        // 变量 `anotherCircle（另一个圆）`与 `circle（圆）`对象的内
        // 容完全一样。

        Rectangle rectangle = new Rectangle()
        rectangle.width = 10
        rectangle.height = 20
        shapes.add(rectangle)

    method businessLogic() is
        // 原型是很强大的东西，因为它能在不知晓对象类型的情况下生成一个与
        // 其完全相同的复制品。
        Array shapesCopy = new Array of Shapes.

        // 例如，我们不知晓形状数组中元素的具体类型，只知道它们都是形状。
        // 但在多态机制的帮助下，当我们在某个形状上调用 `clone（克隆）`
        // 方法时，程序会检查其所属的类并调用其中所定义的克隆方法。这样，
        // 我们将获得一个正确的复制品，而不是一组简单的形状对象。
        foreach (s in shapes) do
            shapesCopy.add(s.clone())

        // `shapesCopy（形状副本）`数组中包含 `shape（形状）`数组所有
        // 子元素的复制品。
```

