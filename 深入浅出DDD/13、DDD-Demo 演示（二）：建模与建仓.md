# 13、DDD-Demo 演示（二）：建模与建仓

初学DDD的同学经常会问我：**DDD的架构分层是什么样的？有开发的脚手架或者模板吗？** 这部分同学的架构思想常常被限制在了中间件或者实际研发时的分层上，而忽略了最重要的一步——需求分析。

没错，DDD中最重要的就是需求分析。业务相关的人员通过事件风暴分析建立起领域模型，依据领域模型再去套用框架。领域模型如果建立得不好，那么后续按照架构分层的写法走，虽然也能运行，但还是换汤不换药地存在业务编码混乱的隐患。

本文将以**增删改查用户的需求**出发，带领你完成领域模型的搭建。

## 一、需求描述

用户服务是几乎所有系统都会配置的一个功能模块或者微服务。

假设我们现在有一个用户的增删改查需求，需求明细如下。

1. 用户基本业务属性包括：用户名，真实姓名，手机号，密码，所属省，所属市，所属区，所属单位，关联角色。
2. 用户新增时需要关联角色（一个用户多个角色）、单位（一个用户一个单位），且新增时用户名不可重复。
3. 用户可以修改用户名，修改的用户名不可与系统中已存在的用户名重复。
4. 用户删除后，与角色、单位的关联关系解除。
5. 我们可以通过分页的方式查询所有的用户信息。
6. 单位数据由另一系统服务单独维护，用户针对单位的业务逻辑需要以单位服务中最新数据为准。
7. 角色可以独立增删改查，一个角色能被多个用户关联，业务属性仅包含角色code与角色名称，用户针对角色的业务逻辑需要以角色的最新数据为准。

## 二、如何构建领域模型

**需求很简单，关键在于我们如何来划分领域模型。**

```
我们回想一下MVC分层中我们会怎么设计？
```

我们会新建用户表、角色表、用户与角色的关联表，然后分别建立对应的Dao层，然后CURD写逻辑代码。

```
如果使用DDD呢？
```

需求分析前置的关键步骤——事件风暴。这里需求比较简单，对应的图我就不画了。我们直接总结一下以用户为维度这里会发生的事件：**用户被新增事件、用户名被修改事件、用户被删除事件**。

我们以这三个事件出发，反推对应出发这个事件的动作与人，能够很轻松地知道**用户**是我们的领域模型。

**那领域模型应该包含的属性有哪些呢？**

首先我们要区分，领域模型应该包含哪些业务属性来支撑当前模型完成上述的事件。

**根据需求，我们来罗列一下用户的所有属性：** id，用户名，真实姓名，手机号，密码，所属省，所属市，所属区，所属单位，关联角色。

然后我们再根据实体与值对象的划分原则，规整领域模型的属性。

从属性分析可以得知，省市区信息其实就是一个地址的值对象，不存在独立的生命周期。

所属单位由于是外域的服务，能够在外域有独立的生命周期，并且用户在关联逻辑执行时，需要感知到最新的单位数据，因此它们之间的关联应该通过单位的不变属性单位id做关联。**唯一标识+生命周期=实体。** 我们得知所属单位应该以实体形式存在于模型内。

同理，角色也有独立的生命周期，能独立处理业务逻辑，是一个独立的限界上下文。因此关联角色在用户领域中也是实体。

最后就是根据事件风暴所分析得到的事件来总结出当前领域模型所拥有的业务逻辑，**将领域能力定义在领域模型内部。**

通过上面的分析，我们能够构建出**用户的聚合根**如下：

```less
/**
 * 用户聚合根
 *
 * @author baiyan
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class User implements AggregateRoot {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户真实名称
     */
    private String realName;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 用户地址
     */
    private Address address;

    /**
     * 用户单位实体
     */
    private Unit unit;

    /**
     * 角色实体
     */
    private List<Role> roles;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;

    /**
     * 修改时间
     */
    private LocalDateTime gmtModified;
    
    //省略部分逻辑

    /**
     * 修改用户名
     *
     * @param userName 修改后的用户名
     * @param existUser 根据修改后的用户名查询出来用户
     */
    public void bindUserName(String userName,User existUser){
        ValidationUtil.isTrue(Objects.isNull(existUser) || Objects.equals(existUser.getId(),this.id),"user.user.name.is.exist");
        this.userName = userName;
    }
}
```

通过实现定义在base层的标记接口**AggregateRoot**来标识当前类为聚合根。我们可以看到领域模型区分于我们常见的数据模型，字段属性并非与数据库表一一对应，而是根据实际的业务形态存在。

我们可以发现聚合根内的属性没有set方法，因为任何的属性修改都是业务逻辑的体现。比较修改用户名这个逻辑，MVC中我们可能在Service中先进行判断数据库中有没有存在同用户名的用户，然后调用set方法进行赋值。模型的完整性被放在了业务调用方维护。而DDD中，我们修改用户名时，通过将已存在的用户传递进行来的方式，将校验与修改用户绑定，让属性的变更有了业务逻辑的支撑。

**角色实体如下**：

```less
/**
 * 角色实体
 * @author baiyan
 */
@Data
@AllArgsConstructor
public class Role implements Entity {

    /**
     * 角色id
     */
    private Long id;
}
```

通过实现**Entity**接口来标识当前类为实体。

实体的存在形态一般仅为id，因为关联的内部属性是可变的。但如果对应的属性不可变，为了业务逻辑执行方便考虑，我们也可以把属性放在实体内。

**单位实体如下**：

```less
/**
 * 单位实体
 * @author baiyan
 */
@Data
@AllArgsConstructor
public class Unit implements Entity {

    /**
     * 单位id
     */
    private Long id;
}
```

分析同上。

**地址的值对象如下**：

```less
/**
 * 地址值对象
 *
 * @author baiyan
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Address implements ValueObject<Address> {

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String county;

    /**
     * 比较地址相等
     *
     * @param address 地址
     * @return
     */
    @Override
    public boolean sameValueAs(Address address){
        return Objects.equals(this,address);
    }

}
```

通过实现**ValueObject**接口来标识当前类为值对象。

值对象的属性不可变，如果要对值对象进行修改，则需要整体替换值对象。值对象的比较通过在值对象内定义属性比较方法进行等值判断。

## 三、如何构建仓储层

领域模型确认完成后，我们所需要做的，就是将领域模型映射成数据模型。其实这个步骤还是比较简单的，就是领域模型字段的扁平化处理。

但在转化的过程中我们尤其需要注意类似于角色实体的映射处理。我们从需求分析的角度得知，用户与角色的关联关系是多对多的关系。即为一个用户可以拥有多个角色，一个角色可以被多个用户给关联。MVC中我们处理这种多对多的关系通常是建立一张用户角色关联表。用户与角色之间的关联关系通过关联表的来组合。这种方式在DDD中极不提倡，因为关联表的数据能被两个不同的领域模型给修改到，领域的数据完整性与一致性将得不到保护。

因此，一旦出现这种多对多的关系，我们需要将其拆分成一对多与一对多的形式来取消对应数据模型之间的强耦合。

在Demo中，我将角色id的列表转化成以逗号间隔的字符串进行存储，所以我们定义用户的持久化对象结构为：

```scala
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class UserPO extends BaseUuidEntity {

    /**
     * 用户名
     * */
    private String userName;

    /**
     * 真实姓名
     * */
    private String realName;

    /**
     * 手机号
     * */
    private String phone;

    /**
     * 密码
     * */
    private String password;

    /**
     * 关联的单位id
     *
     * */
    private Long unitId;

    /**
     * 关联的角色id列表
     * */
    private String roleIds;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String county;

}
```

**那么这里又会引出来一个新的问题。**

假设角色A被用户1、用户2所关联，用户1关联的角色A与角色B。现在我从用户领域的功能入口解绑了用户1与角色A的关联，那么角色A如何感知到这个逻辑呢？

**事件通知模型！**

比如用户1解绑角色A，发送一个用户解绑事件。角色聚合监听该事件，解除角色A与用户1的关联关系。

（需要说明一下：Demo中为了演示清晰，并没有加入这段逻辑代码。）

数据模型一旦确定下来之后，仓储就是相当有套路的步骤了，如下图所示：

![image-20211218184918480.png](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/b2b639b2056d4b13bc91df49f6266fd3~tplv-k3u1fbpfcp-zoom-in-crop-mark:3024:0:0:0.awebp)

- base层我们定义好通用的仓储上层抽象接口；
- domain层在对应的聚合包内定义业务的仓储接口；
- 基础设施层内定义业务仓储接口的实现类与数据模型相关的Dao层配置。

**这里着重讲解一下业务仓储接口的定义。**

我们在“DDD的仓储落地”中说过仓储只允许拥有四个方法：save、saveAndFlush、delete、byId。但是在Demo中的UserRepository与RoleRepository中分别都定义了一个业务的查询方法，这是不是破坏了仓储本身的职责呢？

```
答案必然是否定的。
```

我们知道仓储存在的意义是帮我们隔绝领域模型与数据模型。而在业务模型中，一个模型的唯一性可能有很多种方式确定，byId只是最显著最常用的方式。比如，我在UserRepository中定义了一个byUserName的方法，用户领域中，userName是具有唯一性的，因此它其实可以被看做是byId方法的重载。

拓展开来，如果业务所必须的情况下，查询可以被多种方式重载，只要入参是包装类字段，出参是聚合根或者聚合根列表都可以。

**但是，save跟saveAndFlush方法可不能重载，它们是需要维护领域模型完整的入口。**

## 四、总结

本文以Demo中用户的增删改查需求出发，介绍了如何分析需求与建立用户领域模型。

在建立仓储层时，分析了领域模型之间多对多关系映射到数据模型时需要注意的点。与MVC的数据关联逻辑不同，DDD通过将多对多转化为两个一对多的方式来舍弃掉关联表存在，让领域模型的数据独自管理。如果关联领域间需要感知变化，则通过事件驱动的方式来解决。

最后我们为了业务上的使用便捷性，“重载了”仓储层的byId查询逻辑，支持更多维度去查询得到聚合根。但是我们还是保证了领域模型修改与新增的入口唯一性，没有让仓储失去它只是一个“仓库”、无法理解业务逻辑的定位。