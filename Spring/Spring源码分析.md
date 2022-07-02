 

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

## 7.Spring容器中的对象都是代理对象吗

**答案：不是**

> spring的ioc 容器中默认都是原生对象，只有通过aop增强的对象才是代理对象。
> 配置了aop的类或者类中方法上有@Transactional注解的（因为@Transactional注解的原理就是基于aop的）

## 8.spring AOP是怎么工作的

AOP会增强容器中的原生对象，通过CGLib增强为一个代理对象。现以UserService为例，通过AOP增强test方法：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202206270910329.png" alt="image-20220627091052195" style="zoom: 50%;" /><img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202206270913976.png" alt="image-20220627091316912" style="zoom: 67%;" />

通过调试发现在调用这个对象的test方法时该对象是通过CGLib代理的对象，其内部的属性是空的，（因为原对象的属性是在实例化后通过依赖注入实现的，但是**代理对象生成后没有经过依赖注入**，所以那些带**@Autowired**的属性自然是空的），但是当我们进到test方法后orderService是有值的。因为代理对象中有个`target`属性是指向原来的普通对象的，也就是经历过依赖注入的对象，所以调用test方法后orderService是有值的。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202206270923304.png" alt="image-20220627092305233" style="zoom:70%;" />

## 9.spring事务底层是怎么工作的

> spring事务也是通过AOP增强实现的，其内部的逻辑也是用一个父子类代理对象中的方法，刚开始会判断是否加了事务注解，然后通过事务管理器建立连接（因为如果是用jdbcTemplate还是mybatis等建立连接的话是一次sql建立一个的，如果一个事务里面有多个sql就会有多个连接，执行完一个就提交一个事务就失效了），然后调用原来的test方法，到最后若没有异常则进行提交，否则进行回滚。
>
> ![image-20220627095942450](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202206270959503.png)

注意一个事务注解失效的场景：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202206271032901.png" alt="image-20220627103238836" style="zoom:80%;" />

可以看到在test方法上加了注解，test方法又调用了一个加了注解的a()方法，a()方法按道理说是以非事务的方式运行，如果当前存在事务，则抛出异常。可是运行结果并没有抛出异常，也就是说当前的**注解失效**了（但是test的事务还是生效的）。失效的原因就在于加了事务注解以后也是通过AOP生成了一个代理类，代理类中的方法（a方法）是通过target对象去调用的，而target对象是**普通对象**，并不是**代理类生成的对象**，所以说我们在**调用a方法的时候是以普通对象调用而不是代理对象，不是代理对象的话就不会去判断是否加了事务注解等等切面操作，就直接走方法里面的内容**，所以这个时候注解就失效了。

> 修改方法：将方法独立出来变成一个新的类，然后调用该方法走的就是代理对象。或者直接在原来的类中自己注入自己
>
> （用@Autowired注解自己本身）。

总结：思考方法上的事务注解有没有用，就去看这个方法被**调用的时候是不是代理对象**，只有代理对象才会走事务。

相关链接：**[一口气说出 6种，@Transactional注解的失效场景](https://juejin.cn/post/6844904096747503629#heading-3)**

## ！！10.Spring为什么要用三级缓存来解决循环依赖

Spring 解决循环依赖的核心就是提前暴露对象，而提前暴露的对象就是放置于第二级缓存中。下表是三级缓存的说明：

| 名称                  | 描述                                                         |
| --------------------- | ------------------------------------------------------------ |
| singletonObjects      | 一级缓存，存放完整的 Bean。                                  |
| earlySingletonObjects | 二级缓存，存放提前暴露的Bean，Bean 是不完整的，未完成属性注入和执行 init 方法。 |
| singletonFactories    | 三级缓存，存放的是 Bean 工厂，主要是生产 Bean，存放到二级缓存中。 |

- 所有被 Spring 管理的 Bean，最终都会存放在 singletonObjects 中，这里面存放的 Bean 是经历了所有生命周期的（除了销毁的生命周期），完整的，可以给用户使用的。
- earlySingletonObjects 存放的是已经被实例化，但是还没有注入属性和执行 init 方法的 Bean。
- singletonFactories 存放的是生产 Bean 的工厂。**三级缓存的目的是为了在没有循环依赖的情况下，延迟代理对象的创建，使 Bean 的创建符合 Spring 的设计原则。**

![image-20220627151918599](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202206271520289.png)

> 总结：
>
> 1. 完整对象创建分两个步骤：实例化 + 初始化，只要初始化没进行完毕，就不完整对象分三种类型：刚实例化的普通对象（放在三级缓存），提前进行AOP的不完整对象（放在二级缓存），完整对象（一级缓存/单例池）
> 2. 循坏依赖带来了什么问题？重复创建，如何解决？第三级缓存通过存储刚创建的实例化普通对象来打破循环，但循环依赖需要的有可能是完成了AOP的代理对象，所以三级缓存中的对象是一个bean工厂，只有在发生了循环以来的情况下才会提前进行AOP或者生成普通对象并放入二级缓存
> 3. 那为什么要二级缓存？因为需要区分“已进行AOP的不完整对象”和“完整的代理对象”，并防止多次AOP（多个循环依赖叠加可导致）
> 4. 相关链接：[哪些循环依赖问题Spring解决不了？](https://blog.csdn.net/wang489687009/article/details/120546430)

