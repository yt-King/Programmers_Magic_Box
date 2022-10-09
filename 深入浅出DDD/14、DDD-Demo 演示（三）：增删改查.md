# 14、DDD-Demo 演示（三）：增删改查

应用服务是整个系统对外的门面，也是DDD六边形架构对外的统一出入口。如果说领域模型是整个系统业务逻辑的体现，那么应用服务就是整个系统功能的体现。应用服务通过编排领域逻辑来实现系统中大大小小的功能。

本文将为你讲解在拿到需求后建立完领域模型之后，我们如何使用应用服务来编排功能逻辑。

## 一、MVC 处理业务逻辑回顾

我们以**用户名被修改事件**为例为你逐步分析编写思路。

老规矩，我们先想一下如果我们在MVC的架构中，使用Service层如何来编写这个逻辑，我们来写一下伪代码。

```scss
 public void updateUserName(UpdateUserDTO dto){
   
   //校验用户id是否存在
   UserPO exist = userMapper.selectById(dto.getId());
   if(Objects.isNull(exist)){
     throw new RunnableException("用户不存在");
   }
   
   //校验修改的用户名是否存在
   UserPO existUserName = userMapper.selectByUserName(dto.getUserName());
   if(Objects.nonNull(existUserName)  && Objects.equals(existUserName.getId(),dto.getId())){
     throw new RunnableException("用户名已存在，修改失败");
   }
   
   //修改用户名
   exist.setUserName(dto.getUserName());
   
   //执行一些逻辑，省略100行代码
   
   userMapper.updateById(exist);
   
   //调用其他需要感知用户名修改的其他Service，同步修改
 }
```

代码逻辑比较简单，这么也没有任何问题。但是如果省略的代码被展开来的话，我们是不是就无法感知到核心的逻辑其实就是在修改用户名，只不过修改用户名之后发生了一些二次触发业务规则的其他事件。但是这部分的代码全部堆在了Service中，核心逻辑不够突出。我们再看一下第9行到第16行的代码，发现没有，修改用户名的前置条件是必须系统中不存在当前用户名。但是这个逻辑却被分成了两步，如果我们前置校验逻辑再复杂一点，换个人来维护这段逻辑的时候就很容易漏掉其中一个校验，导致用户名被修改成功。

这就是所谓了领域的业务逻辑被外泄，由调用方来维护了领域模型的完整性与一致性，显然这是不合理的。

## 二、应用服务编排逻辑

**那么我们来看看DDD中使用应用服务如何来实现上述的功能呢？**

```scss
 public void updateUserName(UpdateUserCommand command){
     //【应用服务仅允许此种判断，抛出错误情况，即为参数校验，不允许实际业务逻辑处理】
     // 前置校验逻辑很长或者通用率高的情况，可以考虑抽取一个UserValidationUtil统一管理前置的业务校验
 
     //先校验用户是否存在
     User user = userRepository.byId(command.getUserId());
     ValidationUtil.isTrue(Objects.nonNull(user),"user.is.not.exist");
 
     //修改用户名
     User existUser = userRepository.byUserName(command.getUserName());
     user.bindUserName(command.getUserName(),existUser);
 
     //执行用户修改相关业务逻辑
     user.printUpdate();
 
     //存储用户
     User save = userRepository.save(user);
 
     //发布用户修改的领域事件
     domainEventPublisher.publish(new UserUpdateEvent(save));
 }
```

我们从方法入参开始进行讲解。

### 1. 方法入参

MVC中用户名的修改信息使用DTO包装，而DDD中使用CQRS模型中的Command指令参数，语义化更强。严格意义上来说，应用服务的所有入参都应该是Command指令。因为应用服务的所有方法都是针对领域的指令性操作，即为增删改动作指令。但是这也只是建议，我觉得这里2个参数及以下的直接入参就好了，不要包装一个Command了，不然业务编码上实在有点繁琐。比如，执行一个根据id删除用户的动作，入参本身只需要一个id，我包装成一个UserDeleteCommand，实在是有点形式主义。

### 2. 参数校验

Service在进入正式的业务逻辑之前都会经过必要的参数校验逻辑，比如，用户名长度、用户id是否存在、用户名是否已经存在等。

Service统一认为它们都是前置的参数校验，但实际上在业务角度看它们是有区别的。

比如校验用户名长度、手机号合法性、传入的用户id是否存在等校验都是属于通用的与业务含义无关的通用合法性参数校验。合法性参数校验允许被直接定义在应用服务中，因为它们会决定应用服务编排的走向，参数校验不通过就不执行后续业务逻辑了。

但是像修改用户名称之前，校验修改后的用户名在当前系统中不存在这个逻辑，它就是属于业务性的逻辑。因为我们在修改用户名称之前，必须保证用户名在系统是不存在的，并不是参数合法性的校验，而是从业务角度出发的校验逻辑。也就是说修改用户与校验用户名未重复应该是被内聚在领域的逻辑中的。

即 MVC 中 Service 的这段逻辑：

```scss
 //校验修改的用户名是否存在
 UserPO existUserName = userMapper.selectByUserName(dto.getUserName());
 if(Objects.nonNull(existUserName)  && Objects.equals(existUserName.getId(),dto.getId())){
   throw new RunnableException("用户名已存在，修改失败");
 }
 
 //修改用户名
 exist.setUserName(dto.getUserName());
```

在应用服务中的体现是这样：

```ini
 //修改用户名
 User existUser = userRepository.byUserName(command.getUserName());
 user.bindUserName(command.getUserName(),existUser);
```

而bindUserName这个方法实际的逻辑被定义在User的聚合根内：

```typescript
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
```

而业务参数的合法性校验大多数情况下都是被定义在入参上，通过Spring提供@Valid参数校验支撑。但是有的时候可能必须要在定义在应用服务内，并且前置的合法性参数校验比较多。这种情况下为了突出业务逻辑与复用校验逻辑，可以单独抽取一个XXValidationService，用于专门处理某一聚合的参数校验逻辑。

### 3. 逻辑编排

只要涉及到业务逻辑处理或者对领域模型属性进行修改的逻辑，都不可以直接在应用服务内进行操作。需要将业务逻辑定义在领域模型内部，应用服务只是无脑地调用领域服务的逻辑，像搭积木一样拼凑出一个功能。

例如，Service中的第18行省略的100行代码可能就是在执行某一个业务逻辑操作，在应用服务中它的呈现方式应该是将这100行代码的业务逻辑定义至聚合内部。

```csharp
 /**
  * 演示修改业务逻辑
  */
 public void printUpdate(){
     //此处省略100行代码
     log.info(this.userName + "发生修改");
 }
```

然后应用服务中调用这段逻辑：

```scss
 //执行用户修改相关业务逻辑
 user.printUpdate();
```

**当然这仅仅是比较简单的场景，有的还会涉及到直接流程分支走向，体现到代码中就是if/else与switch。**

这部分的判断逻辑我在“DDD的分层详解”一文中也叙述过，一定要区分好当前判断是业务流程还是分支流程走向。滥用分支判断条件会导致应用服务退化成Service。

### 4. 仓储调用

Service中操作的都是数据模型，保存时调用的是Dao。而在应用服务中，编排完领域模型后，我们调用仓储操作领域模型，由仓储处理转化成数据模型。

与Service不同的是，应用服务保存领域模型没有各种update方法，只有一个save操作，全量更新。这么做保证了，保证仓储层是没有任何业务逻辑的。功能点已经被应用服务调用领域逻辑编排完成。

**这里需要特别注意的是，不允许在一个应用服务内调用两个不同仓储的save方法**。

这样会导致领域模型的修改入口不是集中在某一块功能点。如果有同时修改两个领域模型的需求时，我们应该通过事件驱动模型或者对应领域的能力层来协作，而不是直接通过调用另一领域的仓储修改领域模型。

**那如果我需要调用其他领域的查询能力呢？**

查询与增删改不同，它本身对数据不具备修改能力，因此允许在应用服务的方法内执行不同仓储的byId方法获取不同的仓储进行业务逻辑处理。

但是这里我们需要注意，尽量不要调用queryApplicationService去获取dto数据，然后来执行相应的业务逻辑。首先这肯定违背了CQRS模型设计的初衷，把CQ的逻辑混淆了，页面的数据展示模型混入了领域模型的逻辑编排中。其次，后续如果查询逻辑变了，领域逻辑将被“挟持”被迫更改，这是很危险的行为。如果业务需求实在没办法必须调用查询应用服务来支撑查询逻辑时，我也建议你专门写一个查询的能力层来防腐数据模型透入到领域模型中。

**如果是系统外领域的数据，则通过适配器接口来调用获取。**

### 5. 发送领域事件

应用服务对于Service调用外域服务告知当前领域修改的逻辑通过事件驱动模型来实现。在聚合包中定义特顶的领域事件（即事件风暴讨论出来的事件），然后在事件被触发后发送领域事件，让外域自行决定如何监听处理。

比如应用服务发送用户名被修改事件：

```arduino
 //发布用户修改的领域事件
 domainEventPublisher.publish(new UserUpdateEvent(save));
```

其他领域需要感知此事件的增加对应的监听类：

```less
 @Component
 @Slf4j
 public class UserEventHandler {
 
     @AutoWired
     XXApplicationService xXApplicationService
 
     @TransactionalEventListener(fallbackExecution = true)
     public Result<Object> handleEvent(UserUpdateEvent event) {
            //用户名后，后续执行强相关的链式调用逻辑
            xXApplicationService.doSomething();
     }
 
 }
```

由于应用服务是领域模型修改的入口，因此监听事件本质上也需要调用外域的领域服务来完成对外域的处理。

**从宏观角度来看，事件监听类也属于用户交互层，属于外部输入。**

## 三、查询应用服务

查询应用服务遵从CQRS模型，它不像应用服务那样那么多的限制。它不需要领域模型内聚逻辑，只需要按照前端想要的数据模型组合出后端的数据即可。

比如查询用户列表：

```ini
 @Override
 public Page<UserPageDTO> userPage(KeywordQuery query){
     Page<UserPO> pos = userMapper.userPage(query);
     return pos.convert(po->{
         UserPageDTO dto = new UserPageDTO();
         this.initUserDTO(dto,po);
         List<Long> roleIds = Arrays.stream(po.getRoleIds().split(",")).map(Long::valueOf).collect(Collectors.toList());
         List<RoleDTO> roles = roleQueryApplicationService.list(roleIds);
         dto.setRoles(roles);
         return dto;
     });
 }
```

不过它还是有一些基本规约。

### 1. 方法入参

我们遵从CQRS模型，两个以上的方法入参包装成Query参数，逻辑语义更强。

### 2. 逻辑处理

查询虽然说是组装存储介质中的数据，但是它们也是有一定的逻辑的。因此，尽量避免在Dao层做过于复杂的sql查询逻辑，将业务逻辑处理与数据组合放在查询应用服务中处理。

### 3. 选择性建立查询仓储

正常情况下，我们都是在单一存储介质中进行数据获取的。一旦涉及到跨存储介质获取数据的逻辑，我们就需要在Dao层与查询应用服务之间嫁接一层查询仓储来屏蔽多存储介质下的取数逻辑。

比如，根据用户id查询用户详情先从缓存查，缓存存在就返回，缓存不存在则从数据库查，然后写入缓存再返回。这个逻辑本身其实就是为了拿到用户信息，我们应该定义一个查询应用仓储：

```kotlin
 public class UserQueryRepository{
 
   public UserInfoDTO byId(Long userId){
     UserInfoDTO info = userCache.get(id)
     if(info!=null){
       return info;
     }
     UserInfoDTO info = userMapper.byId(id);
     if(info == null){
       return info;
     }
     userCache.set(id,info);
     return info;
    }
 }
```

然后在应用服务中调用查询仓储逻辑：

```ini
 UserInfoDTO info = userQueryRepository.byId(id);
```

当然如果你只是根据id直接获取用户信息，你也可以直接在用户交互层的接口中直接调用查询仓储，不用再转接一层查询应用服务。

### 4. 查询应用服务互查

查询是允许应用服务之间互相调用的，毕竟只是数据模型的数据组装，本质上就是为了方法的复用嘛。

## 四、总结

本文从实际的用户增删改查需求出发，介绍了应用服务在增删改领域模型时需要注意的点。我们如果换一种角度来看待应用服务，它就像是流程引擎。应用服务本身不定义逻辑，它只负责根据既定的规则串联定义在领域模型中的业务逻辑，最终形成一个功能点。

查询就没有那么多弯弯绕绕的，根据查询所需要的数据自由组合Dao层的数据就好了。但是需要注意数据来源于多个存储介质时，我们还是需要加一个查询仓储的，这样可以让查询应用服务的业务逻辑更加突出。