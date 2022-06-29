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
>

![image-20220628211438488](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-%C2%B76/202206282114832.png)

## 3.@componentScan

