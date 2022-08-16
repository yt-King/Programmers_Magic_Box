#  Java 8 - 默认方法

## 什么是默认方法，为什么要有默认方法

### 先上例子

```java
public interface A {
    default void foo(){
       System.out.println("Calling A.foo()");
    }
}

public class Clazz implements A {
    public static void main(String[] args){
       Clazz clazz = new Clazz();
       clazz.foo();//调用A.foo()
    }
}
```

代码是可以编译的，即使Clazz类并没有实现foo()方法。在接口A中提供了foo()方法的默认实现。

### 什么是默认方法

**Java 8 引入了新的语言特性——默认方法（Default Methods）。**

>Default methods enable new functionality to be added to the interfaces of libraries and ensure binary compatibility with code written for older versions of those interfaces. 
>
>默认方法允许您添加新的功能到现有库的接口中，并能确保与采用旧版本接口编写的代码的二进制兼容性。

简单说，就是接口可以有实现方法，而且不需要实现类去实现其方法。只需在方法名前面加个default关键字即可。

### 为什么出现默认方法

为什么要有这个特性? 首先，之前的接口是个双刃剑，好处是面向抽象而不是面向具体编程，缺陷是，当需要修改接口时候，需要修改全部实现该接口的类，默认方法解决了这个问题，它可以为接口添加新的方法，而不会破坏已有的接口的实现。这在 lambda 表达式作为 Java 8 语言的重要特性而出现之际，为升级旧接口且保持向后兼容提供了途径。

## java 8抽象类与接口对比

| 相同点                                                       | 不同点                                                       |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| 都是抽象类型                                                 | 抽象类不可以多重继承，接口可以(无论是多重类型继承还是多重行为继承) |
| 都可以有实现方法(以前接口不行)                               | 抽象类和接口所反映出的设计理念不同。其实抽象类表示的是”is-a”关系，接口表示的是”like-a”关系。{[java 面向对象关系is-a has-a like-a](https://blog.csdn.net/qq_23835497/article/details/75333350)} |
| 都可以不需要实现类或者继承者去实现所有方法，(以前不行，现在接口中默认方法不需要实现者实现) | 接口中定义的变量默认是public static final 型，且必须给其初值，所以实现类中不能改变其值；抽象类中的变量默认是 friendly 型，其值可以在子类中重新定义，也可以重新赋值。 |

## 默认方法的继承

```java
interface InterfaceA {
    default void print() {
        System.out.println("InterfaceA print");
    }
}

interface InterfaceB extends InterfaceA {

}

interface InterfaceC extends InterfaceA {
    @Override
    default void print() {
        System.out.println("InterfaceC print");
    }
}

interface InterfaceD extends InterfaceA {
    @Override
    void print();
}
//---------------------------------------------
public class Java8Test {
    public static void main(String[] args) {
        new InterfaceB() {}.print(); // 打印："InterfaceA print"
        new InterfaceC() {}.print();// 打印："InterfaceC print"
        new InterfaceD() {
            @Override
            public void print(){
                System.out.println("InterfaceD print");
            }
        }.print();// 打印：“InterfaceD print”

        // 或者使用 lambda 表达式
        ((InterfaceD) () -> System.out.println("InterfaceD print")).print();
    }
}
```

接口默认方法的继承分三种情况（分别对应上面的 `InterfaceB` 接口、`InterfaceC` 接口和 `InterfaceD` 接口）：

- 不覆写默认方法，直接从父接口中获取方法的默认实现。
- 覆写默认方法，这跟类与类之间的覆写规则相类似。
- 覆写默认方法并将它重新声明为抽象方法，这样新接口的子类必须再次覆写并实现这个抽象方法。

## 多重继承的冲突

由于同一个方法可以从不同接口引入，自然而然的会有冲突的现象，默认方法判断冲突的规则如下:

1.一个声明在类里面的方法优先于任何默认方法(classes always win)

2.否则，则会优先选取路径最短的。

### case1

```java
public interface A{
	default void aa() {
		System.out.println("A's aa");
	}
}
public interface B{
	default void aa() {
		System.out.println("B's aa");
	}
}
public static class D implements A,B{
	
}
```

运行结果如下：

![image-20220816094347054](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208160943135.png)

如果一定要这么写的话，IDEA也给我们提示了，指定需要实现哪个接口的方法即可：

![image-20220815153740301](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-8/202208151537357.png)

```java
public static class D implements A,B{
    @Override
    public void aa(){
        A.super.aa();
    }
}
```

### case2

```java
public interface A{
	default void aa() {
		System.out.println("A's aa");
	}
}
public interface B{
	default void aa() {
		System.out.println("B's aa");
	}
}
public interface C extends A,B{
}
```

报错，与case1类似。

### case3

```java
public interface A{
	default void aa() {
		System.out.println("A's aa");
	}
}
public interface B{
	default void aa() {
		System.out.println("B's aa");
	}
}
public interface C extends A, B{
	default void aa() {
		System.out.println("C's aa");
	}
}
public static class D implements A,B,C{
	
}
//输出：C's aa（优先选取路径最短）
```

### case4

```java
public interface A{
	default void aa() {
		System.out.println("A's aa");
	}
}
public interface C extends A{
	default void aa() {
		System.out.println("C's aa");
	}
}
public static class D implements C{
	
}
//输出：C's aa（优先选取路径最短）
```

>通过Case1-4可以知道它是找唯一的最短路径的default，如果是多个那么报错。

###  case5

```java
public interface A{
	default void aa() {
		System.out.println("A's aa");
	}
}
public interface B{
	default void aa() {
		System.out.println("B's aa");
	}
}
public interface C extends A,B{
	default void aa() {
		System.out.println("C's aa");
	}
}
public static class D implements C{
	@Override
    public void aa(){
        C.super.aa();
    }
}
//输出 C's aa 
```

> 可见`C.super`表示的是C接口，同时D无法访问A,B的aa；通过Case 5也可以看出，C虽然有两个最短路径相同的的aa, 但是它自己有一个更高优先级的aa，所以不会报错; 但是如果将C改为case2的形式则会报错，因为C自己没有aa，同时他还有两个最短路径相同的的aa，自然就会报错了。

## 静态默认方法

Java 8 的另一个特性是接口可以声明（并且可以提供实现）静态方法，与默认方法类似，不同之处在于我们无法在实现类中覆盖它们。如果实现类中的实现不佳，此功能可帮助我们避免不良结果。

```java
public interface InterfaceDemo3 {
    default void log(String str) {
        if (!isEmpty(str)) {
            System.out.println("InterfaceDemo3 log:" + str);
        }
    }
    static boolean isEmpty(String str) {
        System.out.println("InterfaceDemo3 default method parameter empty check");
        return str == null ? true : "".equals(str) ? true : false;
    }
}
//实现类
public class StaticMethodDemo implements InterfaceDemo3 {
    public boolean isEmpty(String str) {
        System.out.println("StaticMethodDemo default method parameter empty check");
        return str == null ? true : false;
    }
    public static void main(String[] args) {
        StaticMethodDemo demo = new StaticMethodDemo();
        demo.log("");
        demo.log("test");
    }
}
/**
输出结果：
InterfaceDemo3 default method parameter empty check
InterfaceDemo3 default method parameter empty check
InterfaceDemo3 log:test
*/
/**
如果将接口方法从静态设置为默认，则会得到以下输出:
StaticMethodDemo default method parameter empty check
InterfaceDemo3 log:
StaticMethodDemo default method parameter empty check
InterfaceDemo3 log:test
*/
```

Java接口静态方法仅对接口方法可见，实例对象无法访问，但是我们可以采用`类名.方法()`的方式来调用，如：`InterfaceDemo3.isEmpty("test")`

## 总结

>默认方法给予我们修改接口而不破坏原来的实现类的结构提供了便利，目前java 8的集合框架已经大量使用了默认方法来改进了，当我们最终开始使用Java 8的lambdas表达式时，提供给我们一个平滑的过渡体验。也许将来我们会在API设计中看到更多的默认方法的应用。

**接口默认方法需要注意的几点**

- 接口默认方法将帮助我们扩展接口，而不必担心破坏实现类。
- 接口默认方法缩小了接口和抽象类之间的差异。
- 接口默认方法使得我们无需创建基类，由实现类自己选择覆盖哪个默认方法实现。
- 接口默认方法增强了Java 8中的Collections API以支持lambda表达式。
- 层次结构中的任何类都具有具有相同签名的方法，则默认方法将变得无关紧要。如默认方法不能覆盖`java.lang.Object`中的方法。因为Object是所有java类的基类，即使我们将Object类方法定义为接口中的默认方法，也将是无用的，因为类始终使用的是Object类方法。