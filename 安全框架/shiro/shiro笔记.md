# shiro笔记

---

## 1.简介

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

## 2.Authentication
