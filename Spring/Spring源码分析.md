 

# Spring源码分析

## 1.Spring，SpringBoot和SpringMVC的区别

- spring是一个**IOC容器**，用来管理bean，使用依赖注入实现控制反转，可以方便的整合各种框架，提供AOP机制弥补OOP（面向对象）的代码重复问题、更方便的将不同类的不同方法中的共同**处理抽取成切面**、自动注入给方法执行，比如日志、异常等等
- springmvc是Spring**对web框架的一个解决方案**，提供了一个总的前段控制器servlet，用来接收请求，然后定义了一套路由策略（url到handle的映射）以及适配执行handle，将结果使用视图解析技术生成视图展现给前端
- springboot是spring提供的一个**快速开发工具**，让程序员更方便的、更快速的开发spring+springMVC应用，简化了配置（约定了默认配置），整合了一系列解决方案（starter机制）、redis、es等，可以开箱即用

## 2.spring是如何创建一个bean对象的

[Spring的单例bean与原型bean](https://www.jianshu.com/p/36a065065e3c)

UserService类--->推断构造方法（先byType再byName）--->对象--->依赖注入（（先byType再byName））--->初始化前（**@PostConstruct**）--->初始化(**initializingbean接口**)--->初始化后（AOP）--->代理对象--->放入Map单例池--->Bean对象（单例bean）

> 单例bean的含义：指的是gerBean的时候如果传递同一个bean的名字得到的是同一个bean，而不是说这个类型的bean只能存在一个，分清**单例模式和单例bean的区别**

> **`@PostConstruct`** : 用来修饰方法，标记在项目启动的时候执行这个方法,一般用来执行某些初始化操作比如全局配置。`PostConstruct` 注解的方法会在构造函数之后执行,Servlet 的`init()`方法之前执行。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202206261050990.png" alt="image-20220626105025901" style="zoom:80%;" />

> 单例bean的实现：通过一个**Map<beanName,Bean对象>**实现，在getBean的时候有beanName直接拿出bean对象，没有的话就创建一个bean对象。

## 3.bean对象和普通对象有什么区别

[Java Bean 与Spring Bean 的区别](https://www.bbsmax.com/A/nAJvZp28Jr/)

通常new出来的叫他普通对象，也就是无参构造方法之后的对象，在经过一系列步骤后就成为了bean对象

## 4.bean的初始化是如何工作的

！！！**[Spring源码解析——Bean加载(doCreateBean方法补充)](https://blog.csdn.net/weixin_43591980/article/details/117093260)**

Spring 容器中的 Bean 是有生命周期的，Spring 允许在 Bean 在初始化完成后以及 Bean 销毁前执行特定的操作，常用的设定方式有以下三种：

- 通过实现 InitializingBean/DisposableBean 接口来定制初始化之后/销毁之前的操作方法；
- 通过 <bean> 元素的 init-method/destroy-method 属性指定初始化之后 /销毁之前调用的操作方法；
- 在指定方法上加上@PostConstruct 或@PreDestroy注解来制定该方法是在初始化之后还是销毁之前调用。

三种方式对比如下：

1. spring bean的初始化执行顺序：构造方法 --> `@PostConstruct`注解的方法 --> `afterPropertiesSet`方法（**实现initializingbean时需要重写的方法**） --> `init-method`指定的方法。具体可以参考例子 
2. `afterPropertiesSet`通过接口实现方式调用（效率上高一点），`@PostConstruct`和`init-method`都是通过反射机制调用

## 5.bean的实例化和初始化有什么区别

实例化：通过构造方法得到一个对象

初始化：实例化后的对象再执行一些初始化的方法（三种方式）

## 6.推断构造方法是什么意思

在没有显示指定的情况下Spring生成bean对象时要先通过构造方法生成普通对象，在有多个构造方法的情况下如果有无参构造方法则使用无参构造方法，没有的话则会报错，不过可以用**@autowired**注解加在构造方法上指定使用这个构造函数。只有一个构造方法的情况下就用唯一的那个构造方法。

