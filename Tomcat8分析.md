# Tomcat8

## Tomcat总体架构

![在这里插入图片描述](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-9/82001f5a288d4d9eb5464a6a3e2c8f18~tplv-k3u1fbpfcp-zoom-in-crop-mark:3024:0:0:0.awebp)

> Connector：开启Socket并监听客户端请求，返回响应数据； Container：负责具体的请求处理；
>
> > 一个Service负责维护多个Connector和一个Container，这样来自Connector的请求只能有它所属的Service维护的Container处理；
>
> Engine：代表整个servlet引擎 Host：表示一个虚拟主机 Context：表示一个应用 wrapper：表示一个servlet



































































```
2022-10-11 18:00:56.099 ERROR 18452 --- [           main] com.alibaba.spring.util.BeanUtils        : Root bean: class [null]; scope=; abstract=false; lazyInit=null; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null can't be resolved bean type!
```



































