# Spring注解分析

## 1.@value

### 普通字符串

```java
//直接把当前字符串赋值给当前字段
@value（“xxx”）
```

### 占位符

```java
//先进行占位符的替换，然后将替换的字符串赋值给当前字段
//根据操作系统环境变量、jvm环境变量、yml文件等作为替换来源
@value("$xxx")
```

### SpringEL

[Spring 表达式语言 (SpEL)](http://itmyhome.com/spring/expressions.html)

```java
//先解析Spring表达式，将结果赋值给当前字段
//注意：解析表达式得到的有可能是字符串，也有可能是一个bean对象
@value("#{xxx}")
```

### 基于@value的扩展

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202206280930434.png" alt="image-20220628093008364"  />

## 2.@bean

> Spring的@Bean注解用于告诉方法，产生一个Bean对象，然后这个Bean对象交给Spring管理。 产生这个Bean对象的方法Spring只会调用一次，随后这个Spring将会将这个Bean对象放在自己的IOC容器中。@Bean明确地指示了一种方法，什么方法呢？产生一个bean的方法，并且交给Spring容器管理；从这我们就明白了为啥@Bean是放在方法的注释上了，因为它很明确地告诉被注释的方法，你给我产生一个Bean，然后交给Spring容器，剩下的你就别管了。记住，@Bean就放在方法上，就是让方法去产生一个Bean，然后交给Spring容器。**@Component , @Repository , @ Controller , @Service 这些注解只局限于自己编写的类，而@Bean注解能把第三方库中的类实例加入IOC容器中并交给spring管理。@Bean注解的另一个好处就是能够动态获取一个Bean对象，能够根据环境不同得到不同的Bean对象。**

> [@Bean是否要跟@Configuration配合使用](https://blog.csdn.net/AwayFuture/article/details/105845005)
>
> 结论：@Bean + @Component，虽然@Bean注解的方法返回的实例已经注入到SpringIOC容器中，但是每次调用@Bean注解的方法时，都会创建新的对象实例bean返回，并不会从IOC容器中获取。
>
>
>     因此，要实现在@Bean注解方法时，要求从IOC容器中返回实例bean而不是每次都新创建一个对象，则@Bean要跟@Configuration配合使用
> **tips**:@Bean的autowired参数已经不建议使用了，因为这个属性是可以将这个bean里面的属性根据要求去进行注入，但是没有@Autowired注解那么灵活，@Autowired加在bean的哪个属性上就注入哪个，但是autowired参数则全部注入，不够灵活，所以默认是不开启的（NO）。

![image-20220628211438488](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202206282114832.png)

## 3.@componentScan

@ComponentScan主要就是**定义扫描的路径**从中找出标识了**需要装配的类**自动装配到spring的bean容器中，**主要作用是：**

> 用于完成组件扫描，指定 spring 扫描范围，通过它指定的路径，Spring 会从被指定的包及其下级包扫描 @Component 及其子类注释的类，用于 Spring 容器自动装配，也就是告诉 Spring 从哪里找到 bean。
>
> **不过需要注意，其仅仅是指定了要扫描的包，并没有装配其中的类，这个真正装配这些类是 @EnableAutoConfiguration 完成的。**

### includeFilters（包含规则）&excludeFilters（排除规则）

![image-20220629222200475](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202206292222562.png)

FilterType为一个[枚举类](https://so.csdn.net/so/search?q=枚举类&spm=1001.2101.3001.7020),总共有5个值,也就是说type总共有5个可选值

```java
public enum FilterType {
	ANNOTATION,//按照注解方式
	ASSIGNABLE_TYPE,//按照指定类型的方式
	ASPECTJ,//使用ASPECTJ表达式的方式
	REGEX,//利用正则表达式进行指定
	CUSTOM//自己实现TypeFilter接口进行自定义规则(如下面的代码)
}
//示例：
// includeFilters 用法 包含Animal.class类可以被扫描到，包括其子类
@ComponentScan(value = "com.spring"
  includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {Animal.class}
)}
)

// excludeFilters 用法 排除包含@Controller注解的类
@ComponentScan(value = "com.spring"
        , excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION
                , classes = {Controller.class}
        ),

})
```

### 扫描索引

[META-INF/spring.factories文件的作用是什么](https://blog.csdn.net/qq_35549286/article/details/109047777)

## 4.@Conditional

![image-20220701145640072](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202207011456218.png)

> [一文了解@Conditional注解说明和使用](https://www.cnblogs.com/cxuanBlog/p/10960575.html)

## 5.@Autowired 

### 在属性上使用*@Autowired*

*@Autowired*可以直接应用到类的属性上，即使该属性的类型不是public、同时也没有为其定义setter方法，也不会影响自动装配的功能。Spring在创建Bean的过程中，会根据字段的类型和名字从Spring容器中找到所匹配的Bean对象进行赋值。

### 在setter方法上使用*@Autowired*

与在属性上使用*@Autowired*来完成自动装配相比较，在setter上使用*@Autowired*没有什么明显的不 同。当我们在装配某个属性的同时还希望执行某些逻辑操作时，往往会这么做。Spring会**根据入参参数信息从容器中找到所匹配的Bean对象传给入参**，通过set方法中给属性赋值，从而达到了set注入的目的，需要注意的是这个注解**还可以加在普通方法**上，不一定非得是set方法。

### 在构造函数上使用*@Autowired*

当在某个构造参数上使用*@Autowired* 时表示该Bean在创建时会使用这个构造方法来实例化得到一个对象，并且Spring会根据该构造方法入参参数的类型和名字，**从Spring容器中找到所匹配的Bean对象传给入参进行赋值**，从而达到构造方法注入的目的。值得注意的是，在较新的Spring版本中完全可以省略在构造函数上声明的*@Autowired*注解，但是**只限于只有一个构造方法**的情况下，如果有多个带参数的构造方法需要**添加@Autowired注解指定构造方法**。

### NoSuchBeanDefinitionException

NoSuchBeanDefinitionException直译为：没有找到相关的bean。根据实际情况不同，解决这个异常的方法分为两种：

- 第一种是前期我们容易犯的错误，即我们的确需要这样的一个bean，而且该bean也是由我们提供的，但忘记使用*@Component* 相关注解声明了。解决的方法当然是声明该类型的bean即可。

- 第二种是该bean可以有，也可以没有。那么此时则可以将*@Autowired* 的*required* 属性设置为*false* :

  ```java
  @Service
  public class BarService {
      @Autowired(required = false)
      BarRepository barRepository;
  }
  ```

### NoUniqueBeanDefinitionException

默认情况下`@Autowired`注解是根据**类型**来完成自动装配的，在装配的过程中如果同一类型的bean存在多个，则会发生*NoUniqueBeanDefinitionException*异常。

### 自定义装配限定器

Spring还允许我们将@Autowired写在其他注解上，使得该注解也拥有@Autowired注解的相关功能。

### static

static字段或方法是不会进行依赖注入的。

### 为什么Spring团队推荐总是在您的bean中使用构造函数建立依赖注入？

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202207021432754.png" alt="image-20220702143255662" style="zoom:80%;" />

@Autowired 可以对**成员变量、方法（一般是set方法，当然其他方法也可以）以及构造方法**三种方式操作（另外还可以加在参数前面，不过没什么用，以及加在其他注解上自定义注解）。@Autowired注入bean，**相当于在配置文件中配置bean**。而对构造方法，就相当于是使用构造函数进行依赖注入。通过一个例子说明**@Autowired和构造方法执行顺序差异**：

![image-20220702144327923](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202207021443995.png)

> Java变量的初始化顺序：静态变量或静态语句块–>实例变量或初始化语句块–>构造方法–>@Autowired

```
补充：为什么要加final？
网上解释：

1.spring配置默认的bean的scope是singleton，也就是启动后一直有。通过设置bean的scope属性为prototype来声明该对象为动态创建。但是，如果你的service本身是singleton，注入只执行一次。@Autowired本身就是单例模式，只会在程序启动时执行一次，即使不定义final也不会初始化第二次，所以这个final是没有意义的吧。可能是为了防止，在程序运行的时候，又执行了一遍构造函数

2.或者是更容易让人理解的意思，加上final只会在程序启动的时候初始化一次，并且在程序运行的时候不会再改变。
```

## 6.@Lazy

![image-20220702155412998](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202207021554085.png)

## 7.@Resource

![image-20220702161759857](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202207021617919.png)

## 8.@Configuration 

![image-20220703102211365](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202207031022499.png)

> **当proxyBeanMethods为true时（默认为true），调用配置类组件创建bean的方法，获得的bean是从容器中直接获取的，而为false时，则是重新创建。这其实是springboot底层的两种模式Full模式和Lite模式，当我们配置 类组件之间无依赖关系时用Lite模式加速容器启动过程，减少判断，配置类组件之间有依赖关系，方法会被调用得到之前单实例组件，用Full模式。**

## 9.@Import

[spring注解之@Import注解的三种使用方式](https://juejin.cn/post/6844904035212853255#heading-3)
