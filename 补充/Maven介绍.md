# Maven介绍

## 1、什么是 Maven

Maven 是一种用来管理 Java 项目的工具，但不是那种用来管理资源规划和调度的工具。 相反，它处理的是**管理一个具体的项目所涉及的各种任务**，如编译、测试、打包、文档以及分发。

Maven 包括以下的几个部分：

- 一组用于处理依赖管理、目录结构以及构建工作流的约定。基于这些约定实现的标准化 可以极大地简化开发过程。例如，一个常用的目录结构使得开发者可以更加容易地跟上 不熟悉的项目的节奏。
- 一个用于项目配置的`XML Schema`：项目对象模型（`Project Object Model`），简称`POM`。 每一个`Maven`项目都拥有一个`POM`文件，默认命名为`pom.xml`，包含了`Maven`用于管理 该项目的所有的配置信息
- 一个委托外部组件来执行项目任务的**插件架构**。这简化了更新以及扩展 `Maven` 能力的 过程

### 1.1 Maven 的基本概念

#### 标准的目录结构

`Maven`定义了一个标准的项目目录结构，并不是每种类型的项目都需要`Maven`的所有元素， 很多都可以在必要的时候在`POM`文件中重写。下表展示了一个基本的`WAR`项目，有别于`JAR`项 目，它拥有`src/main/webapp`文件夹。当`Maven`构建该项目时，该目录（其中包含WEB-INF目 录）的内容将会被放置在`WAR`文件的根路径上。位于该文件树根部的`${project.basedir}`是一个标准的`Maven`属性，标识了当前项目的根目录。

![image-20221026160750554](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221026160750554.png)

#### POM 大纲

![image-20221026160948865](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221026160948865.png)

#### 构件

任何可以被 `Maven` 的坐标系统（参见接下来的关于 GAV 坐标的讨论）唯一标识的对象都是一个 `Maven` 构件。大多数情况下，构件是构建 `Maven` 项目所生成的文件，如 `JAR`。但是，只包含其他 `POM`（该文件本身并不产生构件）使用的定义的 `POM` 文件也是 `Maven` 构件。 

`Maven` 构件的类型由其 `POM` 文件的元素指定。最常用的值是 `pom`、`jar`、 `ear`、`war` 以及 `maven-plugin`。

#### POM 文件的用例

可以通过以下的方式来使用 POM 文件：

- 默认的——用于构建一个构件
- 父 POM——提供一个由子项目继承的单个配置信息源——声明这个 POM 文件作为它们 的元素的值
- 聚合器——用于构建一组声明为的项目，这些子项目位于其当前聚合器项目 的文件夹中，每个都包含有它自己的 POM 文件

作为父 POM 或者聚合器的 POM 文件的元素的值将是 pom。注意，一个 POM 文件可能同时提供两项功能

#### GAV 坐标

POM 定义了 5 种称为坐标的元素，用于标识 `Maven` 构件。首字母缩写 GAV 指的是必须始 终指定的 3 个坐标< groupId >、< artifactId >以及< version >的首字母。

1. < groupId >是项目或者项目组的全局的唯一标识符。这通常是 Java 源代码中使用的全 限定的 Java 包名。例如，`io.netty、com.google`。
2. < artifactId >用于标识和某个< groupId >相关的不同的构件。例如，`netty-all、 netty-handler`。
3. < type >是指和项目相关的主要构件的类型（对应于构件的 POM 文件中的 值）。它的默认值是 `jar`。例如，`pom、war、ear`。
4. < version >标识了构件的版本。例如，1.1、2.0-SNAPSHOT 、4.1.9.Final。
5. < classifier >用于区分属于相同的 POM 但是却被以不同的方式构建的构件。例如 `javadoc、sources、jdk16、jdk17`。

POM 文件必须声明它所管理的构件的坐标。一个具有如下坐标的项目将会产生一个具有以下格式的名称的构件：

![image-20221026162645951](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/image-20221026162645951.png)

在这种情况下，它将产生这个构件：**netty-all-4.1.9.Final.jar**

#### 依赖

项目的依赖是指编译和执行它所需要的外部构件。在大多数情况下，你的项目的依赖项也会有它自己的依赖。我们称这些依赖为你的项目的传递依赖。一个复杂的项目可能会有一个深层级的依赖树；`Maven`提供了各种用于帮助理解和管理它的工具。

Maven的< dependency >声明在POM的< dependencies >元素中:

```xml
<dependencies>   
	<dependency>
        <groupId>xxxx</groupId>
        <artifactId>xxxx</artifactId>
        <version>xxxx</version>
        <type>xxxx</typetype>
        <scope>xxxx</scope>
        <systemPath>xxxx</systemPath>
    </dependency>
    ...
<dependencies>  
```

在< dependency >声明中，GAV坐标总是**必不可少的**，type以及scope元素对于那些值不是默认值jar和compile的依赖来说也是必需的。

< scope >元素可以具有以下值:

- `compile`—编译和执行需要的（默认值）。
- `runtime`—只有执行需要。
- `optional`——不被引用了这个项目所产生的构件的其他项目，视为传递依赖。
- `provided`——不会被包含在由这个 POM 产生的 WAR 文件的 WEB_INF/lib 目录中。
- `test`——只有编译和测试的执行需要。
- `import`——只使用在< dependencyManagement >中，将在后面的“依赖管理”部分进行讨论。

< systemPath >元素用来指定文件系统中的绝对位置:

Maven 用来管理项目依赖的方式，包括了一个用来存储和获取这些依赖的存储库协议，已经彻底地改变了在项目之间共享 JAR 文件的方式，从而有效地消除了项目的中每个开发人员都维 护一个私有 lib 目录时经常会出现的问题

#### 依赖管理

POM 的< dependencyManagement >元素包含可以被其他项目使用的< dependency >声明。 这样的 POM 的子项目将会自动继承这些声明。其他项目可以通过使用< scope >元素的 import 值来导入它们。

举个例子，想必大家在做SpringBoot应用的时候，都会有如下代码：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>1.3.3.RELEASE</version>
</parent>
```

继承一个父模块，然后再引入相应的依赖。假如说，我不想继承，或者我想继承多个，怎么做？可以写这样一个用于依赖管理的pom：

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.test.sample</groupId>
    <artifactId>base-parent1</artifactId>
    <packaging>pom</packaging>
    <version>1.0.0-SNAPSHOT</version>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>junit</groupId>
                <artifactid>junit</artifactId>
                <version>4.8.2</version>
            </dependency>
            <dependency>
                <groupId>log4j</groupId>
                <artifactid>log4j</artifactId>
                <version>1.2.16</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

然后就可以通过非继承的方式来引入这段依赖管理配置:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.test.sample</groupId>
            <artifactid>base-parent1</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
 
<dependency>
    <groupId>junit</groupId>
    <artifactid>junit</artifactId>
</dependency>
<dependency>
    <groupId>log4j</groupId>
    <artifactid>log4j</artifactId>
</dependency>
```

> 注意：import scope只能用在dependencyManagement里面

#### 构建的生命周期

Maven 构建的生命周期是一个明确定义的用于构建和分发构件的过程。有 3 个内置的构 建生命周期：`clean`、`default` 和 `site`。一个构建的生命周期由一系列的阶段所组成。下面是默认的构建生命周期的各个阶段的一个 部分清单。

- validate——检查项目是否正确，所有必需的信息是否已经就绪。
- process-sources——处理源代码，如过滤任何值。
- compile——编译项目的源代码。
- process-test-resources——复制并处理资源到测试目标目录中。
- test-compile——将测试源代码编译到测试目标目录中。
- test——使用合适的单元测试框架测试编译的源代码。
- package——将编译的代码打包为它的可分发格式，如 JAR。
- integration-test——处理并将软件包部署到一个可以运行集成测试的环境中。
- verify——运行任何的检查以验证软件包是否有效，并且符合质量标准。
- install——将软件包安装到本地存储库中，在那里其他本地构建项目可以将它引用为依赖。
- deploy——将最终的构件上传到远程存储库，以与其他开发人员和项目共享。

执行这些阶段中的一个阶段将会调用所有前面的阶段。

#### 插件

虽然`Maven`协调了所有构建生命周期阶段的执行，但是它并没有直接实现它们，相反，它将 它们**委托给了插件**，这些插件是`maven-plugin`类型的构件（打包为`JAR`文件 ）。`Apache Maven` 项目为标准构建生命周期所定义的所有任务都提供了插件，更多的是由第三方生产的，用于处理 各种自定义的任务。

插件可能拥有多个内部步骤，或者目标，其也可以被单独调用。例如，在一个 `JAR` 项目中， 默认的构建生命周期由 `maven-jar-plugin` 处理，其将构建的各个阶段映射到了它自己的以及其他插件的目标中。

#### 插件管理