# shiro笔记

---

## 1.简介

官方文档：https://www.infoq.com/articles/apache-shiro/

Apache Shiro (发音为“ shee-roh”，日语中的“城堡”)是一个功能强大、易于使用的 Java 安全框架，它执行身份验证、授权、加密和会话管理，可用于保护任何应用程序——从命令行应用程序、移动应用程序到最大的 web 和企业应用程序。下图为shiro的所有功能

![image-20220108151538779](shiro%E7%AC%94%E8%AE%B0.images/image-20220108151538779.png)

**shiro 四个主要的功能：**

**Authentication：**身份认证/登录，验证用户是不是拥有相应的身份；

**Authorization：**授权，即权限验证，判断某个已经认证过的用户是否拥有某些权限访问某些资源，一般授权会有角色授权和权限授权；

**SessionManager：**会话管理，即用户登录后就是一次会话，在没有退出之前，它的所有信息都在会话中；会话可以是普通JavaSE环境的，也可以是如Web环境的，web 环境中作用是和 HttpSession 是一样的；

**Cryptography：**加密，保护数据的安全性，如密码加密存储到数据库，而不是明文存储；

**shiro 的其它几个特点：**

- **Web Support：**Web支持，可以非常容易的集成到Web环境；
- **Caching：**缓存，比如用户登录后，其用户信息、拥有的角色/权限不必每次去查，这样可以提高效率；
- **Concurrency：**shiro支持多线程应用的并发验证，即如在一个线程中开启另一个线程，能把权限自动传播过去；
- **Testing：**提供测试支持；
- **Run As：**允许一个用户假装为另一个用户（如果他们允许）的身份进行访问；
- **Remember Me：**记住我，这个是非常常见的功能，即一次登录后，下次再来的话不用登录了。

### 1.1-两大核心

>authentication 认证——确定用户身份
>
>authorization 授权——对用户访问系统资源的行为做出控制（后台接口，前台页面，敏感数据）

### 1.2-三个核心概念 ——Subject  、SecurityManager 和 Realms

#### Subject

Subject 是一个安全术语，基本上意味着“当前正在执行的用户”。它之所以不被称为“用户”，是因为“用户”这个词通常与人联系在一起。在安全领域，“ Subject”这个术语可以指人，也可以指第三方进程、守护进程帐户或任何类似的东西。它仅仅意味着“当前与软件相互作用的东西”。不过，对于大多数意图和目的，你可以把它看作 Shiro 的用户概念。我们可以轻松地在代码中的任何位置获取 Shiro Subject

```java
import org.apache.shiro.subject.Subject;
import org.apache.shiro.SecurityUtils;
...
Subject currentUser = SecurityUtils.getSubject();//获取Subject
```

一旦获得了 Subject，就可以立即访问当前用户希望 Shiro 做的90% 的事情，比如登录、注销、访问他们的会话、执行授权检查等等。这里的关键点在于 Shiro 的 API 在很大程度上是直观的，因为它反映了开发人员在“每个用户”安全控制中思考的自然趋势。在代码中的任何地方都可以轻松地访问 Subject，从而允许在任何需要的地方执行安全操作。

#### Security manager

Subject 表示当前用户的安全操作，SecurityManager 则管理所有用户的安全操作。它是 Shiro 架构的核心，作为一种“保护伞”对象，引用许多内部嵌套的安全组件，形成对象图。但是，一旦配置了 SecurityManager 及其内部对象图（object graph），通常就不会对它进行任何处理，应用程序开发人员几乎所有时间都花在 Subject API 上。

Shiro 通过基于文本的 INI 配置提供了一个默认的“公共基础”解决方案。INI 易于阅读，使用简单，并且需要很少的依赖性。您还将看到，通过对对象图导航的简单理解，可以有效地使用 INI 配置简单的对象图（object graph），如 SecurityManager。同时Shiro 也支持 Spring XML 配置和其他替代方案。

```ini
#配置ini文件
[main]
cm = org.apache.shiro.authc.credential.HashedCredentialsMatcher
cm.hashAlgorithm = SHA-512
cm.hashIterations = 1024
# Base64 encoding (less text): 
cm.storedCredentialsHexEncoded = false
[users]
jdoe = TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJpcyByZWFzb2
asmith = IHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbXNoZWQsIG5vdCB
```

 加载 shiro.ini 配置文件

```java
//1. Load the INI configuration
Factory<SecurityManager> factory =
new IniSecurityManagerFactory("classpath:shiro.ini");

//2. Create the SecurityManager
SecurityManager securityManager = factory.getInstance();

//3. Make it accessible
SecurityUtils.setSecurityManager(securityManager);
```

#### Realms

Shiro 的第三个也是最后一个核心概念是一个领域。Realm 充当 Shiro 和应用程序的安全数据之间的“桥梁”或“连接器”。也就是说，当需要与用户帐户等与安全相关的数据进行实际交互以执行身份验证(登录)和授权(访问控制)时，Shiro 会从为应用程序配置的一个或多个Realms查找其中的内容。

从这个意义上讲，一个Realm本质上是一个**特定于安全的 DAO**: 它封装了数据源的连接细节，并根据需要将关联的数据提供给 Shiro。在配置 Shiro 时，您必须指定至少一个 Realm 用于身份验证和/或授权。可以配置多个Realm，但至少需要一个。

Shiro 提供了开箱即用的域(out-of-the-box Realms) ，以连接到许多安全数据源(即目录) ，如 LDAP、关系数据库(JDBC)、文本配置源(如 INI 和属性文件)等等。如果缺省的 Realm 不能满足需求，可以插入自己的 Realm 实现来表示自定义数据源。下面是一个配置 Shiro (通过 INI)使用 LDAP 目录作为应用程序realm之一的示例。

```ini
#连接到 LDAP 用户数据存储区的示例realm配置代码片段
[main]
ldapRealm = org.apache.shiro.realm.ldap.JndiLdapRealm
ldapRealm.userDnTemplate = uid={0},ou=users,dc=mycompany,dc=com
ldapRealm.contextFactory.url = ldap://ldapHost:389
ldapRealm.contextFactory.authenticationMechanism = DIGEST-MD5 
```

### 1.3-RBAC权限模型

RBAC 是基于角色的访问控制（`Role-Based Access Control` ）在RBAC 中，权限与角色相关联，用户通过成为适当角色的成员而得到这些角色的权限。这就极大地简化了权限的管理。这样管理都是层级相互依赖的，权限赋予给角色，而把角色又赋予用户，这样的权限设计很清楚，管理起来很方便。

![image-20220108142012096](shiro%E7%AC%94%E8%AE%B0.images/image-20220108142012096.png)

## 2.Authentication（认证）

### 2.1-认证流程

身份验证是验证用户身份的过程。也就是说，当用户使用应用程序进行身份验证时，他们是在证明自己确实是他们所说的那个人。这有时也被称为“登录”。这通常是一个三步骤的过程。

>1. 收集用户的识别信息
>2.  向系统提交*principals* 和*credentials* 
>3. 如果提交的凭据与系统期望的用户标识(主体)相匹配，则认为该用户已通过身份验证。如果它们不匹配，则认为该用户没有经过身份验证

- **principals**是一个subject的识别属性。主体可以是任何标识主体的东西，例如名(给定名)、姓(姓或家庭名)、用户名、社会安全号码等。当然，诸如姓氏之类的东西并不擅长独特地识别一个人Subject, 因此，用于身份验证的最佳主体对于应用程序是唯一的——通常是用户名或电子邮件地址

- **Credentials** 通常是秘密值，只作为他们事实上拥有声称的身份的证据。Credentials的一些常见例子是密码、指纹和视网膜扫描等生物特征数据以及 x. 509证书

一个常见例子是用户名/密码组合，每个人都熟悉这个例子。当大多数用户登录到软件应用程序时，他们通常提供他们的用户名(主体)和支持他们的密码(凭证)。如果系统中存储的密码(或其表示形式)与用户指定的密码相匹配，则认为这些密码是经过身份验证的。Shiro 以一种简单直观的方式支持同样的工作流程。Shiro 有一个以Subject为中心的 API ——在运行时，几乎所有你想用 Shiro 做的事情都是通过与当前正在执行的Subject交互来实现的。因此，要登录 Subject，只需调用它的 login 方法，传递一个 AuthenticationToken 实例，该实例表示已提交的主体和凭据(在本例中为用户名和密码)。

```java
//1.获取提交的principals和credentials
AuthenticationToken token = new UsernamePasswordToken(username, password);
/**
这里使用 UsernamePasswordToken，支持最常见的用户名/密码身份验证方法。这是 Shiro 的 org.apache.Shiro.authc 的实现。AuthenticationToken 接口，它是 Shiro 的身份验证系统用来表示提交的主体和凭据的基本接口。需要注意的是 Shiro 并不关心你是如何获得这些信息的: 可能数据是由提交 HTML 表单的用户获取的，或者可能是从 HTTP 头部获取的，或者可能是从 Swing 或 Flex GUI 密码表单读取的，或者可能是通过命令行参数。从应用程序最终用户收集信息的过程与 Shiro 的 AuthenticationToken 概念完全分离。我们可以随心所欲地构造和表示 AuthenticationToken 实例——它与协议无关。
*/
//2. 获取当前用户（subject）
Subject currentUser = SecurityUtils.getSubject();

//3. 在获取当前正在执行的 Subject 之后，进行一个登录调用，传递前面创建的 AuthenticationToken 实例。
currentUser.login(token);
```

当登录方法被调用时，SecurityManager 将接收 **AuthenticationToken** 并将其分派到一个或多个realms，以允许每个realm根据需要执行身份验证检查。每个realm都可以根据需要对提交的 authenticationtoken 进行响应。但是如果尝试登录失败了会发生什么呢？如果用户指定的密码不正确怎么办？您可以通过对 Shiro 的运行时 AuthenticationException 做出响应来处理故障

```java
//异常捕获
try {
    currentUser.login(token);
} catch ( UnknownAccountException uae ) { ...
} catch ( IncorrectCredentialsException ice ) { ...
} catch ( LockedAccountException lae ) { ...
} catch ( ExcessiveAttemptsException eae ) { ...
} ... catch your own ...
} catch ( AuthenticationException ae ) {
    //unexpected error?
}

//No problems, continue on as expected...
```

Shiro 有一个丰富的运行时身份验证异常层次结构，可以准确地指出尝试失败的原因。您可以在 try/catch 块中封装 login，捕获任何您希望捕获的异常并相应地对其作出响应。如果现有的一个异常类不满足需要，可以创建自定义 authenticationexception 来表示特定的失败场景。

### 2.2-Remembered vs. Authenticated

```java
//示例
UsernamePasswordToken token = new UsernamePasswordToken(username, password);

//表示希望 Shiro 为身份验证尝试执行“ Remember Me”服务。这可以确保 Shiro 在以后返回到应用程序时记住用户身份
token.setRememberMe(true);
```

如上面的例子所示，Shiro 除了支持正常的登录过程之外，还支持“记住我”的概念。值得一提的是，Shiro 对记住的subject和经过实际验证的subject做了非常精确的区分:

- **Remembered**:  记住的**Subject** 是非匿名的，并且有一个已知的身份 (即 subject.getPrincipals ()是非空的 ) 。但是这个身份是在前一个会话期间的上一个身份验证中记住的。如果 subject.isRemembered ()返回 true，则认为该主题已被记住。
- **Authenticated**:  认证的**Subject** 是在当前会话期间成功身份验证的**Subject**(即在没有抛出异常的情况下调用登录方法)。如果 subject.isAuthenticated ()返回 true，则认为**Subject**已通过身份验证。

上述的这两个状态是相互排斥的——一个状态的 true 值表示另一个状态的 false 值，反之亦然。原因如下：

```apl
“认证”这个词有着非常强烈的证明的意味。也就是说Subject已经说明了他是谁，这是可以确定的。但是处于被记住的状态与程序交互式“认证”的状态就不存在了，“记住我”这个状态只能给系统一个我可能是谁的概念，没有办法绝对保证记住的Subject就是预期的用户。所以尽管应用程序的许多部分可以根据记住的Subject执行用户特定的逻辑，但是要避免一些高敏感的操作（在用户认证自己之前），例如，一个检查 Subject 是否可以访问财务信息的检查应该几乎总是依赖于 isAuthenticated () ，而不是依赖于 isRemembered () ，以保证预期的和经过验证的身份。
```

### 2.3-Logging Out 退出

与验证相反的是释放所有已知的识别状态。当 Subject 与应用程序交互完成后，可以调用 Subject.logout ()放弃所有标识信息:

```java
currentUser.logout(); //删除所有的识别信息，并使他们的会话失效。
```

当调用注销时，任何现有的 Session 都将失效，任何身份都将被去除(例如，在 web 应用程序中，RememberMe cookie 也将被删除)。在 Subject 注销之后，Subject 实例再次被认为是匿名的，如果需要，**除了 web 应用程序之外**，可以重新使用它再次登录。

![image-20220109123540278](shiro%E7%AC%94%E8%AE%B0.images/image-20220109123540278.png)

### 2.4-Authentication Sequence 认证顺序

接下来看 Shiro 内部在发生身份验证尝试时所发生的情况。流程图如下：

![image-20220109123654166](shiro%E7%AC%94%E8%AE%B0.images/image-20220109123654166.png)

1. 应用程序代码调用 Subject.login 方法，并传入代表最终用户主体和凭据的构造 AuthenticationToken 实例。

2. Subject 实例，通常是 DelegatingSubject (或子类)通过调用 SecurityManager.login (令牌)委托给应用程序的 SecurityManager，实际的身份验证工作从这里开始。

3. SecurityManager 作为一个基本的“保护伞”组件，接收令牌并通过调用 Authenticator.authenticate (token)简单地委托给它的内部 Authenticator 实例。基本上都是 ModularRealmAuthenticator 实例，它支持在身份验证期间协调一个或多个 Realm 实例。Modularrealmauthentator 实质上为 Apache Shiro 提供了一个 PAM- 风格的范例(在 PAM 术语中，每个realm都是一个“模块”)。

4. 如果为应用程序配置了多个realm，ModularRealmAuthenticator 实例将利用其配置的 AuthenticationStrategy 启动多领域身份验证尝试。在调用领域进行身份验证之前、期间和之后，将调用 AuthenticationStrategy，以允许它对每个领域的结果做出反应。

   ![image-20220109134320845](shiro%E7%AC%94%E8%AE%B0.images/image-20220109134320845.png)

5. 查看每个配置的 Realm 是否支持提交的 AuthenticationToken。支持 Realm 的 getawatitationinfo 方法将被提交的令牌调用。**getAuthenticationInfo**方法有效地代表了针对特定领域的单一身份验证尝试。

Shiro SecurityManager 的实现默认是使用 **ModularRealmAuthenticator** 实例，支持单领域和多领域的应用程序。在single realm应用程序中，**ModularRealmAuthenticator** 将直接调用single realm。如果配置了两个或多个realm，它将使用 AuthenticationStrategy 实例来协调尝试的发生。

如果希望使用自定义 Authenticator 实现配置 SecurityManager，可以在 shiro.ini 中这样做，例如:

```ini
[main]
...
authenticator = com.foo.bar.CustomAuthenticator
securityManager.authenticator = $authenticator
```

### 2.5-AuthenticationStrategy 认证策略

当为一个应用程序配置两个或多个realm时，modularrealmauthink 依赖于内部 AuthenticationStrategy 组件来确定身份验证尝试成功或失败的条件。AuthenticationStrategy 是一个无状态组件，在身份验证尝试期间被查询4次(这4次交互所需的任何必要状态都将作为方法参数给出) : 

1. 在任何realms被调用之前
2. 在单个realm被调用之前
3. 在单个realm被调用之后
4. 在所有realms被调用之后

此外，AuthenticationStrategy 还负责聚合每个成功的realm的结果，并将它们打包成单个 AuthenticationInfo 表示。这个最终的聚合 AuthenticationInfo 实例是 Authenticator 实例返回的内容，也是 Shiro 用来表示 Subject 的最终身份的内容。

有三个认证策略可供选择：

| `AuthenticationStrategy` **class** | **Description**                                              |
| ---------------------------------- | ------------------------------------------------------------ |
| AtLeastOneSuccessfulStrategy       | 如果一个(或多个)领域验证成功，则认为成功。如果一个都没有成功验证，则认证失败 |
| FirstSuccessfulStrategy            | 只使用从第一个成功验证的realm返回的信息。其余的realm将被忽略。如果未通过身份验证，则认证失败 |
| AllSuccessfulStrategy              | 有配置的realm都必须通过身份验证，才能认为整体认证是成功的。如果任何一个身份验证不成功，则认证失败 |

**ModularRealmAuthenticator**默认采用的是**`AtLeastOneSuccessfulStrategy`** ，因为这是最常见的期望策略。当然也可以通过INI配置自定义策略：

```ini
[main]
...
authcStrategy = org.apache.shiro.authc.pam.FirstSuccessfulStrategy

securityManager.authenticator.authenticationStrategy = $authcStrategy
...
```

### 2.6-Realm Authentication Order  —— realm验证顺序

**ModularRealmAuthenticator** 将以迭代的顺序与 Realm 实例进行交互。**ModularRealmAuthenticator**可以访问 SecurityManager 上配置的realm实例。当执行身份验证尝试时，它将遍历该集合，并且对于每个支持提交 AuthenticationToken 的 Realm，调用 Realm 的 getuthmatchationinfo 方法。

#### 隐式排序

当使用 Shiro 的 INI 配置格式时，将按照在 INI 文件中定义的顺序来查询realm。也就是说，对于下面的 shiro.ini 示例:

```ini
blahRealm = com.company.blah.Realm
...
fooRealm = com.company.foo.Realm
...
barRealm = com.company.another.Realm
#可以省略引用，定义的每个realm都将自动添加，但在显式排序是不可省略
securityManager.realms = $blahRealm, $fooRealm, $barRealm
```

SecurityManager 将使用这三个领域进行配置，在进行身份验证的过程中，blahRealm、 fooRealm 和 barRealm 将按照这个顺序进行调用。

#### 显式排序

需要注意的是，当显式配置 securityManager.realms 属性时，SecurityManager 上将只配置被引用的领域。这意味着可以在 INI 中定义5个领域，但是只有在领域属性引用了3个领域的情况下才会实际使用3个领域。这也是和隐式排序的主要区别。

```ini
blahRealm = com.company.blah.Realm
...
fooRealm = com.company.foo.Realm
...
barRealm = com.company.another.Realm
#根据引用的顺序来执行
securityManager.realms = $fooRealm, $barRealm, $blahRealm
...
```

## 3.Authorization（授权）

### 3.1-简介

授权实质上是访问控制——控制用户在应用程序中可以访问的内容，如资源、网页等。大多数用户通过使用角色和权限等概念来执行访问控制。也就是说，通常允许用户根据分配给他们的角色和/或权限做某些事情或不做某些事情。然后，应用程序可以基于对这些角色和权限的检查来控制所公开的功能。subjectAPI 允许我们非常容易地执行角色和权限检查。

```java
//角色检查示例
if ( subject.hasRole(“administrator”) ) {
    //show the ‘Create User’ button
} else {
    //grey-out the button?
} 
```

### 3.2-核心三要素——权限、角色、用户

#### 权限

Apache shiro 中的权限表示安全策略中最原子的元素。它们从根本上看是关于行为的声明，并且明确地表示在应用程序中可以做什么。权限语句从本质上描述了资源以及 Subject 与这些资源交互时可能采取的操作。例如：

>- Open a file 打开一个文件
>- View the ‘/user/list’ web page 查看“/user/list”网页
>- Print documents 打印文件
>- Delete the ‘jsmith’ user 删除“ jsmith”用户

需要注意的式权限只表示一种行为，不能反映出谁有权力做这种行为。定义谁(用户)可以做什么(权限)是以某种方式向用户分配权限。这通常是由应用程序完成的，并且在不同的应用程序之间会有很大的不同。**shiro中权限的粒度可以划分的非常细致**。

#### 角色

角色是一个被命名的实体，通常表示一组行为或职责。这些行为可以转化为软件应用程序可以执行或不可以执行的操作。角色通常分配给用户帐户，因此通过关联，用户可以使用分配给不同角色的权限。Shiro 支持两种角色概念:

- **Implicit Roles 隐式角色**：即直接通过角色来验证用户有没有操作权限，如在应用中CTO、技术总监、开发工程师可以使用打印机，假设某天不允许开发工程师使用打印机，此时需要从应用中删除相应代码；再如在应用中CTO、技术总监可以查看用户、查看权限；突然有一天不允许技术总监查看用户、查看权限了，需要在相关代码中把技术总监角色从判断逻辑中删除掉；即粒度是以角色为单位进行访问控制的，粒度较粗；如果进行修改可能造成多处代码修改。

- **Explicit Roles 显式角色**：在程序中通过权限控制谁能访问某个资源，角色聚合一组权限集合；这样假设哪个角色不能访问某个资源，只需要从角色代表的权限集合中移除即可；无须修改多处代码；即粒度是以资源/实例为单位的；粒度较细。

权限检查是执行授权的另一种方式。上面例子中的角色检查有一个重大缺陷: 不能在运行时添加或删除角色。如果需要能够在运行时更改角色的含义，或者根据需要添加或删除角色，那么您必须依赖其他东西。为此，Shiro 支持其权限概念。权限是功能的原始声明，例如“打开一扇门”、“创建博客条目”、“删除 jsmith”用户等等。通过**使用权限反映应用程序的原始功能**，只需要在更改应用程序的功能时更改权限检查。反过来，您可以在运行时根据需要将权限分配给角色或用户。

#### 用户（users）

在shiro中，subject实际上是用户的概念，用户可以通过与角色或直接权限的关联在应用程序中执行某些操作。比如说，我们有一个user实体类，我们可以直接将权限分配给这个用户，也可以只把权限分配个角色，然后将用户与角色关联，用户可以传递性地“拥有”分配给他们角色的权限。

### 3.3-三种授权方式

#### 编程式：通过写if/else 授权代码块完成

```java
Subject subject = SecurityUtils.getSubject();
if(subject.hasRole(“admin”)) {
	//有权限
} else {undefined
	//无权限
}
```

#### 注解式：通过在执行的Java方法上放置相应的注解完成

```java
@RequiresRoles("admin")
public void hello() {undefined
	//有权限
}//没有权限将抛出相应的异常；
```

#### JSP/GSP 标签：在JSP/GSP 页面通过相应的标签完成

```jsp
<shiro:hasRole name="admin">
<!— 有权限—>
</shiro:hasRole>
```

### 3.4-Role-Based Authorization（基于角色的授权）

最简单的一种方式是直接检查该用户是否具有某个特定的角色并授权或进行下一步的行为。

```java
Subject currentUser = SecurityUtils.getSubject();

if (currentUser.hasRole("administrator")) {
    //show the admin button 
} else {
    //don't show the button?  Grey it out? 
}
```

除了通过hasRole（）方法检查还可以简单地断言它们在执行逻辑之前有一个预期的角色。如果 Subject 没有预期的角色，则将引发 AuthorizationException。如果它们确实具有预期的角色，那么逻辑将按照预期继续。

```java
Subject currentUser = SecurityUtils.getSubject();

//先进行检查，如果没有该角色则抛出异常
currentUser.checkRole("bankTeller");
openBankAccount();
```

与 hasRole 方法相比，这种方法的一个好处是代码可以更加简洁。

### 3.5-Permission-Based Authorization（基于权限的授权）

执行访问控制的更好方法通常是通过基于权限的授权，因为它与应用程序的原始功能(以及应用程序核心资源的行为)密切相关，所以基于权限的授权源代码在功能更改时更改，而不是在安全策略更改时更改。这意味着与类似的基于角色的授权代码相比，因为代码收到的影响会小很多。

我们可以通过isallowed()方法来检查权限，有两种主要的权限检查方法——使用基于对象的权限**实例**或使用表示权限的**字符串**

#### Object-based Permission Checks  基于对象的权限检查

实例化 Shiro 的 org.apache.Shiro.authz 实例接口，并将其传递给  isPermitted 方法，这些方法接受权限实例。例如，考虑下面的场景: 在一个办公室里有一台打印机和一台唯一标识符激光4400n。我们的软件需要检查，看看目前的用户是否允许在打印机上打印文件，然后我们才允许他们按下“打印”按钮。权限检查，看看这是否可能是这样制定的:

```java
Permission printPermission = new PrinterPermission("laserjet4400n", "print");

Subject currentUser = SecurityUtils.getSubject();

if (currentUser.isPermitted(printPermission)) {
    //show the Print button 
} else {
    //don't show the button?  Grey it out?
}
```

基于对象的权限在以下情况下很有用:

- 需要编译时类型安全性
- 希望保证权限的表示和正确使用
- 需要对如何使用权限解析逻辑(称为权限蕴涵逻辑，基于权限接口)进行显式控制
- 希望保证“权限”能够准确地反映应用程序资源(例如，可以在项目构建期间基于项目的域模型自动生成“权限”类)

#### String-based permission checks  基于字符串的权限检查

依照上面的例子，可以将代码改为如下：

```java
Subject currentUser = SecurityUtils.getSubject();
//Permission p = new WildcardPermission("printer:print:laserjet4400n"); 相当于是快捷方式
if (currentUser.isPermitted("printer:print:laserjet4400n")) {
    //show the Print button
} else {
    //don't show the button?  Grey it out? 
}
```

这个示例仍然显示相同的实例级权限检查，但是权限的重要部分——打印机(资源类型)、打印(操作)和 laserjet4400n (实例 id)——都表示在一个 String 中。

如同角色授权一样也可以采用断言的方式简化代码：

```java
Subject currentUser = SecurityUtils.getSubject();
//实例化的方式
Permission p = new AccountPermission("open");
currentUser.checkPermission(p);
openBankAccount();
//string字符串的方式
currentUser.checkPermission("account:open");
openBankAccount();
```

### 3.6-Annotation-based Authorization  基于注释的授权



## 4.Session Management（会话管理）

Apache Shiro 在安全框架领域提供了一些独一无二的东西: 在任何应用程序和任何架构层中都可以使用的一致的 Session API。也就是说，Shiro 可以为任何应用程序提供会话编程范型，从小型的独立守护程序到最大的集群式 web 应用程序。这意味着希望使用会话的应用程序开发人员不再被迫在不需要 Servlet 或 EJB 容器的情况下使用它们。或者，如果使用这些容器，开发人员现在可以选择在任何层中使用统一和一致的会话 API，而不是 servlet 或特定于 ejb 的机制。

Shiro Session最重要的好处之一可能是它们是独立于容器的，在session集群方面，Shiro 的架构允许可插拔的 Session 数据存储，这意味着只需配置一次会话集群，无论部署环境是 Tomcat、 Jetty、 JEE 服务器还是其他，它都将以相同的方式工作。没有必要根据如何部署应用程序来重新配置应用程序。Shiro Session的另一个好处是，如果需要，可以跨客户端共享会话数据。

```java
//任何环境中访问 Subject 的会话
Session session = subject.getSession();
Session session = subject.getSession(boolean create);
//Session methods
session.getAttribute(“key”, someValue);
Date start = session.getStartTimestamp();
Date timestamp = session.getLastAccessTime();
session.setTimeout(millis);
...
```

这些方法在概念上与 HttpServletRequest API 相同。第一个方法将返回 Subject 的现有 Session，或者如果还没有，它将创建一个新 Session 并返回它。第二个方法接受一个布尔参数，这个参数决定是否创建一个还不存在的 Session。一旦你获得了主体的 Session，你可以几乎和 HttpSession 一样地使用它。唯一不同的是你可以在任何应用程序中使用 Shiro Sessions，而不仅仅是在 web 应用程序中。

## 5.Cryptography（加密）

Shiro 在加密方面的目标是简化和使用 JDK 的加密支持。需要注意的是，密码术通常并不特定于Subject，因此它是 Shiro API 中不特定于Subject的一个领域。可以在任何地方使用 Shiro 的加密支持，即使没有使用 Subject。Shiro 真正关注加密支持的两个领域是加密哈希(又名消息摘要)和加密密码。

### 5.1-简单功能

#### 接口驱动的、基于 POJO 的

Shiro 的所有 api 都是基于接口的，并作为 POJO 实现。这允许我们使用与 javabean 兼容的格式(如 JSON、 YAML、 Spring XML 等)轻松地配置 Shiro Cryptography 组件。还可以根据需要重写或定制 Shiro，利用其 API 节省时间和精力。

#### JCE 上的简化包装器

 Java 密码扩展(JCE)很复杂并且难以使用。Shiro 的 Cryptography api 更容易理解和使用，而且它们极大地简化了 JCE 的概念。

####  Object Orientifies加密概念

JDK/JCE 的 Cipher and Message Digest (Hash)类是抽象类，相当混乱，要求使用 obtuse factory 方法和类型不安全的字符串参数来获取您想要使用的实例。Shiro的‘Object Orientifies’加密和散列，基于一个干净的对象层次结构，并允许通过简单的实例化使用它们。

#### **Runtime Exceptions**

和shiro的其他地方一样，所有的加密异常都是 runtimeexception。可以根据需要决定是否捕获异常。

### 5.2-加密功能

#### OO 层次结构

与 JCE 不同，Shiro cipherservice 遵循一个面向对象的类层次结构，这个类层次结构与它们的数学概念相匹配: AbstractSymmetricCipherService、 DefaultBlockCipherService、 AesCipherService 等。这样可以轻松地覆盖现有的类并根据需要扩展功能。

#### 只需实例化一个类

与 JCE 使用 String 标记参数的混乱的工厂方法不同，使用 Shiro 加密器要容易得多——只需实例化一个类，必要时使用 javabean 属性配置它，并按需使用它。例如：new AesCipherService ()

#### 更安全的默认设置

JCE Cipher 实例假设一个最小公分母默认值，并不会自动启用更安全的选项。Shiro 将自动启用更安全的选项，以确保数据在默认情况下尽可能安全，防止意外的安全漏洞。

### 5.3-散列特性

#### 默认接口实现 

Shiro 提供了默认的现成的 Hash (即 JDK 中的消息摘要)实现，比如 MD5、 SHA1、 sha-256等等。这提供了一种类型安全的构造方法(例如，新的 Md5Hash (data)) ，而不必在 JDK 中使用类型不安全的字符串工厂方法。

#### 内置的十六进制和 base64转换

Shiro Hash 实例可以通过 toHex ()和 toBase64()方法自动提供十六进制和 base-64编码的散列数据。所以现在不需要指出如何自己正确地编码数据。

#### 内置 Salt 和反复散列支持

盐和反复散列迭代在散列数据时是非常有价值的工具，特别是在保护用户密码时。Shiro 的 Hash 实现支持盐和多重散列迭代，所以不必在任何你可能需要的地方重复这个逻辑。
