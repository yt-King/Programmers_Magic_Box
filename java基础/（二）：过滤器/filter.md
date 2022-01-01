# Java Web Servlet中的Filter过滤器的原理以及常见用法

# 1-Filter接口

过滤器在Java中对应着javax.servlet.Filter接口，仅此而已，实现了Filter接口的类就可以被称作过滤器，Filter接口中有三个抽象方法，其中init和destroy方法作为过滤器的申请周期方法！

```java
public interface Filter {

    default public void init(FilterConfig filterConfig) throws ServletException {}

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException;

    default public void destroy() {}
}
```

## 1.1 过滤器的生命周期

---

1. `诞生`：过滤器的实例是在web应用被加载时就完成的实例化，并调用init方法初始化的。servlet 容器在实例化Filter后只调用`init`方法一次。在要求过滤器执行任何过滤工作之前，init 方法必须成功完成。区别于Servlet，过滤器会全部立即初始化。
2. 每个过滤器在init初始化方法都会传递一个 FilterConfig 对象，从该对象可以获取其初始化参数。并可通过FilterConfig获取 ServletContext对象，比如可以使用该对象加载筛选任务所需的资源。
3. `存活`：和应用的生命周期一致的。在内存中是单例的。针对拦截范围内的资源，每次访问都会调用void doFIlter(request,response.chain)进行拦截。
4. `死亡`：应用被卸载时，Filter将被调用，此时会调用destroy方法，该方法只会被调用一次。

## 1.2 doFilter过滤方法

---

**过滤器在 `doFilter` 方法中执行过滤操作。**

doFilter方法中有一个`FilterChain` 参数对象，该对象由Servlet容器创建并传递给开发人员的。FilterChain表示一个过滤器链，客户端请求的资源在链的末尾。

在当前过滤器中，如果复合过滤规则，那么可以使用`FilterChain#doFilter`方法调用链中的下一个过滤器器，或者如果调用过滤器是链中的最后一个过滤器，则该方法将调用链末尾的资源。

也就是说，一个Web应用中可以有多个过滤器，它们将会按照一定顺序形成一个过滤器链，在链的最末尾就是要访问的资源，当一个请求到来的时候，他必须通过所有的过滤器，才能访问到真正的资源。

# 2 -Filter的使用

## 2.1-创建

```java
public class FirstFilter implements Filter { //只需实现Filter接口，实现doFilter方法
    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("before");
        HttpServletRequest httpServletRequest= (HttpServletRequest) request;
        //获取当前请求的URL
        System.out.println(httpServletRequest.getRequestURL());
        //放行，调用下一个过滤器或者访问资源
        chain.doFilter(request, response);
        System.out.println("after");
    }

    @Override
    public void destroy() {
        System.out.println("destroy");
    }
}
```

## 2.2-部署

---

通常情况下，Filter过滤器在 Web 应用程序的部署描述符中配置，也就是web.xml文件，这类似于Servlet。

一个`<filter/>`标签表示定义一个过滤器，`<filter-name/>`表示当前过滤器的name，`<filter-class/>`表示当前过滤器的类全路径名，`<init-param/>`表示当前过滤器的初始化参数，可以通过在`init方法`的参数`FilterConfig`对象获取这些参数。

```xml
<filter>
    <filter-name>FirstFilter</filter-name>
    <filter-class>com.example.filter.FirstFilter</filter-class>
    <!--当前过滤器的初始化参数，可以通过在init方法的参数FilterConfig对象获取-->
    <init-param>
        <param-name>aaa</param-name>
        <param-value>bbb</param-value>
    </init-param>
    <init-param>
        <param-name>ccc</param-name>
        <param-value>ddd</param-value>
    </init-param>
</filter>

<!--定义这个过滤器可以作用于哪些资源或者哪些Servlet-->
<filter-mapping>
    <filter-name>FirstFilter</filter-name>
    <!--指定拦截指定路径的资源URL-->
    <url-pattern>/aa/*</url-pattern>
</filter-mapping>
<filter-mapping>
    <filter-name>FirstFilter</filter-name>
    <!--指定拦截指定的ServletName-->
    <servlet-name>Servlet2</servlet-name>
    <!--调度程序-->
    <!--<dispatcher>REQUEST</dispatcher>-->
</filter-mapping>

```

通过多个`<filter-mapping/>`标签可以为一个过滤器配置多个过滤映射，当然也可以将多个映射一个到一个`<filter-mapping/>`标签中

1. `<filter-name/>`指定某个过滤器的名字
2. `<url-pattern/>`指定过滤器所拦截的资源路径URL，“/*”表示所有的Web资源都需要途径该过滤器，“*.xxx”表示拦截访问xxx后缀的资源的请求。
3. `<servlet-name/>`指定过滤器所拦截的某个Servlet的名字。
4. `<dispatcher/>`指定过滤器在拦截时所应用的调度模式，一共有五个可选配置：FORWARD, REQUEST, INCLUDE, ASYNC, ERROR。

>`FORWARD`：如果目标资源是通过RequestDispatcher的forward()方法访问的，那么该过滤器将被调用，除此之外，该过滤器不会被调用。
>
>`REQUEST`：当用户直接通过普通路径访问资源时，Web容器将会调用过滤器。如果目标资源是通过RequestDispatcher的include()或forward()方法访问时，那么该过滤器就不会被调用。这是默认的模式。
>
>`INCLUDE`：如果目标资源是通过RequestDispatcher的include()方法访问的，那么该过滤器将被调用，除此之外，该过滤器不会被调用。
>
>`ERROR`：如果目标资源是通过声明式的异常处理机制调用的，那么该过滤器将被调用。除此之外，过滤器不会被调用。
>
>`SYNC`：意味着过滤器将在从异步上下文AsyncContext的调用下应用。

**在Servlet 3.0以及之后，支持在Filter实现类上直接使用@WebFilter注解的方式配置过滤器，降低了配置文件的复杂度**。例如：

```java
@WebFilter(urlPatterns = "/path/*",servletNames = "Servlet2")
```

# 3 -Filter的执行顺序

过滤器链的完整流程顺序：**客户端发送http请求到Web服务器上，Web服务器对该请求URL找到对应负责的过滤器形成过滤器链，接着从第一个过滤器开始进行过滤操作，也就是调用Filter.doFilter方法，这个方法的逻辑是开发者编写的，当当前请求满足当前过滤器的要求或者是过滤操作完毕之后，应在调用chain.doFilter方法进行放行，该方法又会调用链中的下一个过滤器的doFilter方法继续过滤，如果当前过滤器是过滤器链的最后一个过滤器，那么chain.doFilter方法将会执行资源访问操作，访问完毕之后，将会依照最开始过滤器的调用顺序倒序的返回，接着执行chain.doFilter方法后面的代码。最终将响应结果交给Web服务器，Web服务器再将响应返回给客户端。**

![image-20220101190256975](filter.images/image-20220101190256975.png)

# 4-整合springboot

## 4.1-创建过滤器类

---

```java
@Slf4j
public class LogFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
         HttpServletRequest req=(HttpServletRequest)servletRequest;
         log.info(req.getRequestURI());
         filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {

    }
}
```

## 4.2-使用FilterRegistrationBean注册过滤器

```java
@Configuration
public class LogFilterConfiguration {
    @Bean
    public FilterRegistrationBean registrationBean(){
        FilterRegistrationBean registrationBean=new FilterRegistrationBean();
        registrationBean.setFilter(new LogFilter());
        //匹配的过滤器
        registrationBean.addUrlPatterns("/*");
        //过滤器名称
        registrationBean.setName("logFilter");
        //过滤器顺序
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
```

## 4.3-使用Servlet3.0注解定义过滤器

```java
@WebFilter(urlPatterns = "/*",filterName = "authFiler")
@Slf4j
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        log.info("进行权限校验.........");
        chain.doFilter(servletRequest,servletResponse);
    }

}
```

两种方式的区别：

1. WebFilter这个注解并没有指定执行顺序的属性，其执行顺序依赖于Filter的名称，是根据Filter类名（注意不是配置的filter的名字）的字母顺序倒序排列
2. @WebFilter指定的过滤器优先级都高于FilterRegistrationBean配置的过滤器
3. FilterRegistrationBean方式可以注入SpringBoot IOC容器中的Bean

