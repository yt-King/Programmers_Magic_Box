# 15、DDD-Demo 演示（四）：复杂逻辑处理

上一篇我们学习了如何使用应用服务来编排业务逻辑，让你对于应用服务在DDD中的实际使用与地位有了一点粗略的了解。虽然我们可以在应用服务中把所有的逻辑都编排完成，但是很多时候为了让其中一部分的原子化编排逻辑更加突出，我们还会再进行增加领域服务或者能力层来使得业务逻辑的展现更加突出且明显。

本文则将通过用户被新增这个事件出发来为你讲解如何将应用服务、能力层、领域模型、领域服务与事件驱动模型融会贯通地使用。

## 一、应用服务直接编排

**回顾一下用户新增的需求**：

1. 用户新增时需要关联角色（一个用户多个角色）、单位（一个用户一个单位），且新增时用户名不可重复；
2. 用户新增时需要依据角色类型打印出标签。

以上逻辑我们直接在应用服务中如何实现？

代码如下：

```less
 @Override
 @Transactional(rollbackFor = Exception.class)
 public void create(CreateUserCommand command){
     //校验用户名不存在                                ValidationUtil.isTrue(Objects.isNull(userRepository.byUserName(command.getUserName())),
                        "user.user.name.is.exist");
         //校验角色存在
     List<Role> roles = roleRepository.listByIds(command.getRoles());
     ValidationUtil.isTrue(CollectionUtils.isNotEmpty(roles) &&
                     Objects.equals(roles.size(),command.getRoles().size()),"user.role.is.not.exist");
                     
      //工厂创建用户
      User user = command.toUser(command);
 
     //执行用户新增相关业务逻辑
     user.printCreate();
 
     //打印标签
     roles.forEach(role->{
       //省略大量逻辑
       if(Objects.equals(role.getCode(),"admin")){
           log.info("用户：{}的标签解析为：{}",user.getUserName(),role.getName());
       }
     })
 
     //存储用户
     User save = userRepository.save(user);
 
     //发布用户新建的领域事件
     domainEventPublisher.publish(new UserCreateEvent(save));
 }
```

编码思路还是比较简单的，就是依据需求将业务逻辑编排在应用服务中。

首先我要说的是，这样写是完全符合DDD传统架构分层编码规范的，也是内聚了业务逻辑的写法。不过精益求精嘛，我们来看看直接编排存在什么样的小问题。

- **原子化编排逻辑**。17行到23行本质上想是要实现如果用户关联的角色存在管理员，就打印一下标签。其实这个操作是属于用户领域中的打印标签逻辑，只不过这个逻辑涉及到了用户领域模型与角色领域模型协作，应用服务只能一行行的编排。
- **应用服务复用**。应用服务之间互相调用并不是禁止的，但也不提倡。因为会导致两个不同领域的功能点互相产生依赖，而不是直接编排的逻辑，但是为了复用逻辑，这种互相调用又不可能避免。

## 二、收缩编排逻辑

我们能够通过引入能力层与领域服务的方式来解决应用服务中可能存在的问题，看一下实现后的代码。

### 1. 领域服务

我们把17到23行的用户根据角色打印标签逻辑合并至用户领域服务中，代码如下：

```less
 @Service
 @Slf4j
 public class UserDomainServiceImpl implements UserDomainService {
 
     @Override
     public void printTag(User user, List<Role> roles){
         roles.forEach(role->{
             //省略大量逻辑
             if(role.isAdmin()){
                 log.info("用户：{}的标签解析为：{}",user.getUserName(),role.getName());
             }
         });
     }
 }
```

在角色聚合根中定义业务方法如下：

```csharp
 /**
 * 获取用户角色标签
 *
 * @return
 */
 public Boolean isAdmin(){
   //此处省略100行代码
   return Objects.equals(this.code,"admin");
 }
```

这样我们可以在能力层通过传递用户聚合根与角色聚合根列表至用户领域服务的方式来完成打印用户角色标签这个原子化功能了。

### 2. 能力层

我们在“DDD的分层详解”一文中已经明白了能力分层的定位，它使用来为应用服务提供能力的。它严格意义上不是一个层，是为了表示一个功能特色比较原子且逻辑突出的小型应用服务。

从它的定义我们不难看出来，能力层不像应用服务那样聚集了一堆功能点，它只有一个入口，对应这提供什么样的能力。

来看一下能力层的抽象：

```java
 @Component
 public abstract class BaseAbility<T, R> {
 
     /**
      * 能力点执行
      *
      * @param abilityCmd
      * @return
      */
     @Transactional(rollbackFor = Exception.class)
     public Result<R> executeAbility(T abilityCmd) {
         try {
 
             //初始化上下文
             AbilityContext.initContext();
 
             //能力点执行前的参数校验
             checkHandler(abilityCmd);
 
             //能力点执行前的幂等校验
             Result<R> checkIdempotent = checkIdempotent(abilityCmd);
             if (Objects.isNull(checkIdempotent) || !Objects.equals(checkIdempotent.getCode(), BaseResult.CODE_SUCCESS)) {
                 return checkIdempotent;
             }
 
             //执行能力业务
             return execute(abilityCmd);
 
         } finally {
             AbilityContext.clearContext();
         }
     }
 
     /**
      * 能力点执行前的参数校验
      *
      * @param abilityCmd abilityCmd
      * @return
      */
     public abstract void checkHandler(T abilityCmd);
 
     /**
      * 能力点执行前的幂等校验
      *
      * @param abilityCmd abilityCmd
      * @return false：当前能力点已执行，不再执行业务逻辑、true：当前能力点未执行，继续执行业务逻辑
      */
     public abstract Result<R> checkIdempotent(T abilityCmd);
 
     /**
      * 执行能力业务
      *
      * @param abilityCmd abilityCmd
      * @return return
      */
     public abstract Result<R> execute(T abilityCmd);
 
 }
```

所有的能力通过继承抽象能力的方式来实现具体的业务逻辑。对应的应用服务调用能力执行点**executeAbility**即可。

**执行点里面的执行主要分为五步。**

1. 初始化当前能力的上下文信息，主要是为后续步骤提供能力点内的线程变量访问能力，避免重复加载数据。
2. 功能点前置的参数校验，校验不通过则通过抛出异常的方式中断功能点的执行。
3. 数据幂等的校验，例如订单已经支付则不在进行支付。通过返回结果的方式中断流程，由应用服务决定如何处理。
4. 具体能力点的逻辑执行，编写规范与应用服务一致。
5. 清楚能力点执行过程中加载的上下文信息，防止内存泄漏。

将一个能力执行分为五步后，实际的业务编排时放在第四步的执行处，让具体的执行点的逻辑更加清晰明了。

我们再看一下用户新增能力点的代码：

```scss
 @Service
 public class UserCreateAbility extends BaseAbility<CreateUserAbilityCommand, Void> {
 
     @Autowired
     RoleRepository roleRepository;
 
     @Autowired
     UserRepository userRepository;
 
     @Autowired
     DomainEventPublisher domainEventPublisher;
 
     @Autowired
     UserDomainService userDomainService;
 
     private final static String ROLE_INFO_KEY = "roleInfo";
 
     @Override
     public void checkHandler(CreateUserAbilityCommand command) {
         //校验用户名不存在
         ValidationUtil.isTrue(Objects.isNull(userRepository.byUserName(command.getUserName())),"user.user.name.is.exist");
         //校验角色存在
         List<Role> roles = roleRepository.listByIds(command.getRoles());
         ValidationUtil.isTrue(CollectionUtils.isNotEmpty(roles) &&
                         Objects.equals(roles.size(),command.getRoles().size()),
                 "user.role.is.not.exist");
         AbilityContext.putValue(ROLE_INFO_KEY,roles);
     }
 
     @Override
     public Result<Void> checkIdempotent(CreateUserAbilityCommand command) {
 
         //在这里进行幂等处理判断
 
         return Result.success(null);
     }
 
     @Override
     public Result<Void> execute(CreateUserAbilityCommand command) {
 
         //工厂创建用户
         User user = command.toUser(command);
 
         //执行用户新增相关业务逻辑
         user.printCreate();
 
         //仅仅为了演示领域服务使用，这没必要这么做，能力点已经是一个比较原子的业务逻辑点了
         //理论上有了能力层之后直接可以砍掉领域服务层
         List<Role> roles = (List<Role>)AbilityContext.getValue(ROLE_INFO_KEY);
         userDomainService.printTag(user, roles);
 
         //存储用户
         User save = userRepository.save(user);
 
         //发布用户新建的领域事件
         domainEventPublisher.publish(new UserCreateEvent(save));
 
         return Result.success(null);
     }
 
 }
```

通过实现抽象接口的三个抽象方法的方式来完成业务逻辑。

注意看checkHandler这里的参数校验逻辑。我们需要区分新增与修改的参数校验逻辑。

用户修改时，用户领域模型已经存在了，因此对存在的领域模型进行修改都需要通过领域模型内的业务方法来修改，保证领域模型的完整性与一致性。

而新增的时候，领域模型还没有生成。我习惯先做参数校验，通过后再进行领域模型的新增。

还有一种方式是定义一个工厂类，把领域模型的构造与校验混合在工厂类中。这种方式我在实际应用时发现，这种工厂类大多数情况下只能被某一个应用服务或者能力层给使用。而且一旦使用不当，很容易在工厂类里面写很多的逻辑，我不太推荐这种做法。

再来看看第27行，我们通过增加上下文变量的方式来避免重复查询角色聚合来进行后续的业务逻辑处理。

### 3. 应用服务

```less
 @Override
 @Transactional(rollbackFor = Exception.class)
 public void create(CreateUserAbilityCommand command){
     userCreateAbility.executeAbility(command);
 }
```

最后应用服务直接应用能力层的执行点即可。不同领域之间如果需要应用服务的协作，则可以抽离一个能力层的方式来作为应用服务之间的桥梁。

应用服务入参与能力点入参有差异的可以定义一个对应能力点的assmbler进行参数转换，例如：`com.baiyan.ddd.application.ability.user.assembler`。

## 三、落地事件驱动模型

到目前为止，我们已经把处于同一事务或者强业务逻辑的功能点落地代码分析完成。

在《DDD-Demo演示（三）：增删改查》中我们已经知道了如何使用事件驱动模型解耦不同事务下或者非强业务逻辑相关的功能。

但我们只是针对事件驱动模型使用了其解耦的功能，别忘了它还有事件溯源的能力。

我们以用户被删除事件为例，一起来看看怎么将事件溯源合并进事件驱动模型。

### 1. 定义抽象领域事件

```arduino
 @Getter
 @Setter
 @NoArgsConstructor
 public abstract class BaseDomainEvent<T> implements Serializable {
 
     private static final long serialVersionUID = 1465328245048581896L;
 
     /**
      * 幂等键:即为当前事件的id
      */
     private String id;
 
     /**
      * 领域对象id
      */
     private String domainId;
 
     /**
      * 事件状态
      */
     private EventStatusEnum eventStatus;
 
     /**
      * 事件类型
      */
     private DomainEventEnum eventType;
 
     /**
      * 业务发生时间
      */
     private LocalDateTime occurredOn;
 
     /**
      * 领域事件数据
      */
     private T data;
 
     public BaseDomainEvent(String domainId, String id, DomainEventEnum eventType, EventStatusEnum eventStatus, LocalDateTime occurredOn, T data) {
         this.domainId = domainId;
         this.id = id;
         this.eventType = eventType;
         this.eventStatus = eventStatus;
         this.data = data;
         this.occurredOn = occurredOn;
     }
 
     /**
      * 修改时间状态为成功
      */
     public void handleSuccess() {
         this.eventStatus = EventStatusEnum.SUCCESS;
     }
 
     /**
      * 修改事件状态为失败
      */
     public void handleFailed() {
         this.eventStatus = EventStatusEnum.FAILED;
     }
 
 }
```

所有业务的领域事件均需要继承此类，属性解释如下。

- **id**：表示当前事件被溯源时的唯一id，全局唯一，建议通过专门的雪花id生成服务生成。
- **domainId**：领域对象的id，比如订单领域下表示订单id。可以根据此业务id获取某个业务对象的所有历史事件，便于溯源整理。
- **eventStatus**：事件状态，用于表示事件在溯源过程中的状态。发送时处于带待处理状态，事件被正确处理后改为处理成功，处理失败后修改状态为初始失败。该字段仅为事件溯源使用，如果直接发送消息，不需要事件溯源，则该字段没有任何意义。
- **eventType**：事件类型，用于管理全局的事件。
- **occurredOn**：事件发生时间，一般情况下就是当前代码执行时间，如果业务有特殊需求则可单独指定。
- **data**：事件数据。

### 2. 定义业务领域事件

```scala
 /**
  * 用户删除领域事件
  *
  * @author baiyan
  */
 public class UserDeleteEvent extends BaseDomainEvent<Long> {
 
     public UserDeleteEvent(Long id) {
         super(String.valueOf(id),
                 //仅做演示，领域事件id为防止重复建议自定义雪花id
                 UUID.fastUUID().toString(),
                 DomainEventEnum.USER_DELETE,
                 EventStatusEnum.PENDING,
                 LocalDateTime.now(),
                 id
         );
     }
 
 }
```

定义业务领域事件来通知其他领域模型并溯源。

### 3. 定义同一业务事件发送方

接口定义如下：

```php
 /**
  * 领域事件发布接口
  *
  * @author baiyan
  */
 public interface DomainEventPublisher {
 
     /**
      * 发布事件
      *
      * @param event event
      */
     <EVENT extends BaseDomainEvent> void publish(EVENT event);
 
     /**
      * 发布事件并保存
      *
      * @param event event
      */
     <EVENT extends BaseDomainEvent> void publishAndSave(EVENT event);
 
 }
```

实现类如下：

```typescript
 @Component
 @Slf4j
 public class DomainEventPublisherImpl implements DomainEventPublisher {
 
     @Autowired
     private ApplicationEventPublisher applicationEventPublisher;
 
     @Autowired
     private DomainEventRepository domainEventRepository;
 
     @Override
     public <EVENT extends BaseDomainEvent> void publish(EVENT event) {
         log.info("发布事件,event:{}", GsonUtil.gsonToString(event));
         applicationEventPublisher.publishEvent(event);
     }
 
     @Override
     public <EVENT extends BaseDomainEvent> void publishAndSave(EVENT event) {
         log.info("保存并发布事件,event:{}", GsonUtil.gsonToString(event));
 
         List<BaseDomainEvent> baseDomainEventList = domainEventRepository.loadByDomainId(event.getDomainId());
         if (CollectionUtils.isNotEmpty(baseDomainEventList)) {
 
             boolean anyMatch = baseDomainEventList.stream().anyMatch(e -> Objects.equals(e.getId(), event.getId()));
             if (anyMatch) {
                 applicationEventPublisher.publishEvent(event);
                 return;
             }
         }
 
         domainEventRepository.save(event);
         applicationEventPublisher.publishEvent(event);
     }
 
 }
```

如果我们只需要使用事件驱动模型进行业务解耦，则直接调用**publish方法**即可。

如果我们还需要使用事件溯源能力，则调用**publishAndSave方法**，在发送事件之前会先将待处理状态的事件入库，等待事件处理完成之后的回调修改事件状态。

### 4. 定义事件解析注解与切面

注解：

```less
 /**
  * 保存事件结果解析注解
  *
  * @author baiyan
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
 public @interface NeedSaveEventResult {
 }
```

切面：

```less
 @Aspect
 @Component
 @Slf4j
 public class NeedSaveEventHandlerAspect {
 
     @Resource
     private DomainEventRepository domainEventRepository;
 
     @Pointcut("@annotation(com.baiyan.ddd.domain.share.event.NeedSaveEventResult)")
     public void pointcut() {
     }
 
     @AfterReturning(value = "pointcut()", returning = "result")
     public void afterReturning(JoinPoint joinPoint, Result result) {
         BaseDomainEvent baseEvent = (BaseDomainEvent)joinPoint.getArgs()[0];
         if(Objects.equals(result.getCode(), BaseResult.CODE_SUCCESS)){
             // 更新事件状态为成功
             log.info("更新事件{}状态为成功", baseEvent.getId());
             baseEvent.handleSuccess();
         }else {
             // 更新状态失败
             log.info("更新事件{}状态为失败", baseEvent.getId());
             baseEvent.handleFailed();
         }
         domainEventRepository.update(baseEvent);
     }
 
 }
```

只要将注解标注在事件处理类上，当事件处理完成之后将会被切面处理回调修改事件状态。事件状态修改为成功后才会被正确溯源处理。

### 5. 定义事件处理类

```less
 @Component
 @Slf4j
 public class UserEventHandler {
 
     @TransactionalEventListener(fallbackExecution = true)
     @NeedSaveEventResult
     public Result<Object> handleEvent(UserDeleteEvent event) {
         try {
             log.info("用户删除后，后续执行强相关的链式调用逻辑");
             return Result.ok();
         } catch (Exception e) {
             return Result.error(e.getMessage());
         }
     }
 
 }
```

定义用户被删除事件监听类并标注 **@NeedSaveEventResult**注解，处理用户被删除事件。

```
注意此处如果不需要事件溯源功能，则不需要标注@NeedSaveEventResult，即事件处理完成后就抛掉了，仅做功能逻辑解耦。
```

如果我们不需要事件驱动模型的事件溯源能力，代码非常简单。只需要发送业务领域事件，对应的处理类处理即可。

如果我们需要事件溯源能力，那么在业务侧的编码配置也是比较简单的。事件发送时定义事件状态为待处理，然后通过DomainEventPublisher.publishAndSave方法存储事件并发送，最后切面将会解析标注了@NeedSaveEventResult注解的处理类的返回结果回调修改事件状态。溯源事件存储后，就可以根据业务需求来解析事件溯源的数据了。

## 四、总结

本文针对应用服务在编排逻辑中可能存在的小瑕疵出发，分析讲解了如何使用能力层跟领域服务的方式，并且对能力层与领域服务的使用规范与注意点做了编码介绍。

在分析完同一事务与强业务逻辑的处理方式后，我们介绍了如何使用事件驱动模型来解耦弱业务关联的逻辑与落地事件溯源的方式。

最后，我们来总结一下一个复杂的增删改业务功能在DDD中的调用时序图：

![image-20211220184142146.png](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-10/341911180f154048941c6f7177d1deea~tplv-k3u1fbpfcp-zoom-in-crop-mark:3024:0:0:0.awebp)

1. 外部请求通过Command参数调用应用服务的接口；
2. 应用服务实现类编排能力层，领域逻辑，领域服务来完成功能点逻辑；
3. 应用服务调用仓储接口，由定义在基础设施层的仓储实现类完成领域模型到数据模型的转换，保存领域模型；
4. 发送领域事件进行业务解耦与事件溯源；
5. 返回请求结果给外部请求方。