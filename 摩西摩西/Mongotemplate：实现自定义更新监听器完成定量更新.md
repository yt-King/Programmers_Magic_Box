# Mongotemplate：实现自定义更新监听器完成定量更新

> ## 背景
>
> 公司现在使用的是grpc进行各项微服务的调用，自从pb更新到3.0之后就一直有一个问题，在2.0版本的时代pb文件可以有`optional`和`required`选项，所以可以实现前端调用http接口走到网关转成grpc请求时可以通过属性是否为`null`来判断该字段是否为用户主动输入；但是更新到3.0版本后就没有了这些字段，转成grpc后null值统一变为了默认值，这就导致无法获取用户主动输入的值。
>
> 举个例子：有一个msg字段，前端如果没传这个字段，grpc2.0读到的就是null，3.0就是一个空字符串”“，但是如果前端传了这个字段但是传了个”“空字符串，这时候就没法知道这是用户主动输入的还是grpc赋默认值的，这就导致后续的一些业务会受到影响。
>
> 所以需要解决的就是怎么样去获取用户主动输入的值！

目前的rpc通信使用的是grpc，数据库使用mongo，解决办法是前端获取用户主动输入字段并将所有字段合并成一个字符串（命名为`keys`）用逗号分割，跟随请求信息一起发出，在网关层进行处理将`keys`字段取出放入grpc的`metadata`中，最后在后端服务中根据`keys`内的字段将实体类中属性不在`keys`中的赋null值，达到剔除不是用户主动输入的字段的目的，这样在更新mongo数据库时null值就不会更新进去，只更新用户主动输入的值。

## 1、大体方向——通过mongo的监听器实现

这个需求在后端需要解决的就是只有更新的时候实现上面说的这个逻辑，那么当我看到mongo的`AbstractMongoEventListener`中的

`BeforeSaveEvent`和其他的`xxxEvent`时我就想到通过监听器监听更新时间来完成，但可惜的是spring-data-mongodb的库中只实现了新增和删除的监听器，不知为何没有实现删除的监听器，所以只能自己实现一个新增的监听器。

![image-20221104185923259](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/image-20221104185923259.png)

仿照着mongo的新增监听器的写法，先定义一个监听事件，这个事件继承了MongoMappingEvent：

```java
import org.bson.Document;
import org.springframework.data.mongodb.core.mapping.event.MongoMappingEvent;

public class UpdateApplicationEvent extends MongoMappingEvent {
    public UpdateApplicationEvent(Object source, Document document, String collectionName) {
        super(source, document, collectionName);
    }
}
```

然后再来定义一个监听器：

```java
import cn.moxi.common.mongo.event.UpdateApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.mapping.event.MongoMappingEvent;
import org.springframework.stereotype.Component;

@Component
public class UpdateMongoEventListener implements ApplicationListener<MongoMappingEvent> {
    @Override
    public void onApplicationEvent(MongoMappingEvent event) {
         if(event instanceof UpdateApplicationEvent){
             this.onBeforeUpdate(event);
        }else{
            System.out.println("有其它事件发生:"+event.getClass().getName());
        }
    }

    public void onBeforeUpdate(MongoMappingEvent event) {
        System.out.println("UpdateApplicationEvent！");

    }
}
```

这个监听器是专门用来监听更新时间发生的，现在直需要在我们的更新代码中发布更新事件即可：

```java
@Autowired
private ApplicationContext applicationContext;

public String insertOrUpdate(Object object) {
    String id = (String) ReflectUtil.getFieldValue(object, fieldId);
    Object objectOrg = StrUtil.isNotEmpty(id) ? mongoTemplate.findById(id, object.getClass()) : null;
    Date date = new Date();

    if (objectOrg == null) {
		//插入操作
    } else {
        Document document = new Document();
        // 发布更新时间
        applicationContext.publishEvent(new UpdateApplicationEvent(object,document,"test"));
        // 设置更新时间
        setUpdateTime(object, (double) date.getTime());
        // 更新
//            Field[] fields = ReflectUtil.getFields(object.getClass());
        String updateString = "{'$set':"+document.toJson()+"}";
        Update update = new BasicUpdate(updateString);

//            // 拷贝属性
//            for (Field field : fields) {
//                if (!field.getName().equals(fieldId) && ReflectUtil.getFieldValue(object, field) != null) {
//                    update.set(field.getName(), ReflectUtil.getFieldValue(object, field));
//                }
//            }

        Query query = new Query();
        query.addCriteria(Criteria.where(fieldId).is(id));
        updateFirst(query, update, objectOrg.getClass());
    }
    return id;
}
```

这样一个基础的实现就有了，剩下的细节可以根据需求具体优化。

## 2、在业务代码中实现需求

完成了基础实现后，回到业务代码中，我们自定义一个监听器来继承我们之前定义的监听器并重写他的方法将我们的逻辑加入其中：

```java
@Component
public class MyMongoEventListener extends UpdateMongoEventListener {

    @Override
    public void onBeforeUpdate(MongoMappingEvent event) {
        Metadata metadata = ThreadLocalUtil.get();
        String keys = metadata.get(Metadata.Key.of("keys", Metadata.ASCII_STRING_MARSHALLER));
        List<String> keysList = StringUtils.isBlank(keys) ? new ArrayList<>() : Arrays.stream(keys.split(",")).collect(Collectors.toList());
        //防止传入空字符串导致数据无法正常写入
        keysList.remove("");

        // 得到操作的实体类对象
        Object source = event.getSource();
        // spring-mongo-data与MongoDB交互的document对象
        Document document = event.getDocument();
        // 利用反射进行相关操作
        ReflectionUtils.doWithFields(source.getClass(), field -> {
            // 使操作的成员可访问
            ReflectionUtils.makeAccessible(field);
            Object value = ReflectUtil.getFieldValue(source, field);
            if (keysList.isEmpty() || keysList.contains(field.getName())) {
                document.put(field.getName(), value);
            }
        });
        super.onBeforeUpdate(event);
    }
}
```

在这里我的实现逻辑是通过反射获取传入的键值对，然后将每个键去keys数组中判断，如果存在则放入document中，不存在则舍弃，这样经过这个监听器后的document内存在的只有keys中的值，也就是用户主动输入的值。

至此我们的功能就基本实现了，当前端将数据发送并附带keys时，后端就能根据keys中的值来定量跟新数据库，因为对于null来说数据库是不会跟新进去的。

## 3、遇到的问题

在之前的更新代码中可以看到我注释了一段代码：

```java
        // 更新
//            Field[] fields = ReflectUtil.getFields(object.getClass());
        String updateString = "{'$set':"+document.toJson()+"}";
        Update update = new BasicUpdate(updateString);

//            // 拷贝属性
//            for (Field field : fields) {
//                if (!field.getName().equals(fieldId) && ReflectUtil.getFieldValue(object, field) != null) {
//                    update.set(field.getName(), ReflectUtil.getFieldValue(object, field));
//                }
//            }
```

原来的更新是通过new一个update然后将属性一个个放入update中实现的，但是这样会导致定量更新失败，因为在监听器里我是修改document来实现定量更新的，所以我将更新语句也改为了通过document来进行更新。

