# dockerfile构建docker镜像

## 1.dockerfile概念

Dockerfile是一个文本文件，文件里是命令的集合。

一般情况下创建一个目录或者是git上的一个资源库，放置Dockerfile文件和build过程所需要的资源，如果Dockerfile的文件名就是Dockerfile并且就在此目录下，build时不需要指定dockerfile位置，否则需要-f 指定文件位置和文件名

```
Warning！
警告
不要使用根目录/作为构建上下文的 PATH，因为它会导致构建将硬盘驱动器的全部内容传输到 Docker 守护进程。
```

创建一个文件夹

![image-20211125184517300](image-20211125184517300.png)

创建dockerfile

![image-20211125184841137](image-20211125184841137.png)



```java
build 命令：docker build -t 映像名：tag .  //最后的点表示资源在当前目录下，不可以省略
```

![image-20211125191315987](image-20211125191315987.png)

验证当前映像：理想情况是，启动容器，进行端口映射后就可以使用

进入容器后启动nginx—— ./usr/sbin/nginx      nginx生效

![image-20211125194127240](image-20211125194127240.png)

修改dockerfile

```
FROM centos
RUN yum update -y
RUN yum install -y nginx
RUN ./usr/sbin/nginx
```

创建容器后依然无法直行nginx，因为没有前台任务

再次修改即可运行

```
FROM centos
RUN yum update -y
RUN yum install -y nginx
CMD nginx -g "daemon off;"
```



## 2.Dockerfile构成

![image-20211125190839404](image-20211125190839404.png)

```
1）FROM
//说明当前构建的image以哪个image为基础
2）RUN
//创建映像时要执行的命令，不是在容器中执行的命令
3）CMD
CMD 指令有三种形式:

CMD ["executable","param1","param2"] (exec 执行官 form, this is the preferred form) 表格，这是首选的表格)
CMD ["param1","param2"] (as (作为default parameters to ENTRYPOINT ENTRYPOINT 的缺省参数)
CMD command param1 param2 (shell 贝壳 form) 表格)
```

## 3.使用Dockerfile部署后台jar包

数据库mysql和redis单独在用另外的docker容器部署

### 3.1-mysql容器化

拉去官方镜像

```sql
docker pull mysql:5.7
```

在本地创建mysql的映射目录

```sql
mkdir -p /root/mysql/data /root/mysql/logs /root/mysql/conf
```

在/root/mysql/conf中创建 *.cnf 文件

```sql
touch my.cnf
```

创建容器,将数据,日志,配置文件映射到本机

```sql
docker run -p 3306:3306 --name mysql -v /root/mysql/conf:/etc/mysql/conf.d -v /root/mysql/logs:/logs -v /root/mysql/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -d mysql:5.7
```

>**-d:** 后台运行容器
>
>**-p** 将容器的端口映射到本机的端口
>
>**-v** 将主机目录挂载到容器的目录
>
>**-e** 设置参数

启动mysql容器

```sql
docker start mysql
```

### 3.2-项目打包

将后端的项目打成jar包，放到服务器上，在服务器新建 dockerfiledemo 文件夹存放jar包和dockerfile

```java
mkdir dockerfiledemo
```

![image-20211208212031882](image-20211208212031882.png)

编写Dockerfile文件：

![image-20211208212120161](image-20211208212120161.png)

### 3.3-Dockerfile命令说明

>FROM：指定基础镜像，必须为第一个命令
>
>MAINTAINER: 维护者信息
>
>LABEL：用于为镜像添加元数据
>
>ADD：将本地文件添加到容器中，tar类型文件会自动解压(网络压缩资源不会被解压)，可以访问网络资源，类似wget
>
>COPY：功能类似ADD，但是是不会自动解压文件，也不能访问网络资源
>
>WORKDIR：工作目录，类似于cd命令
>
>RUN：构建镜像时执行的命令
>
>CMD：构建容器后调用，也就是在容器启动时才进行调用。
>
>ENTRYPOINT：配置容器，使其可执行化。配合CMD可省去"application"，只使用参数。
>
>ENV：设置环境变量
>
>EXPOSE：指定于外界交互的端口
>
>VOLUME：用于指定持久化目录
>
>ARG：用于指定传递给构建运行时的变量
>
>USER:指定运行容器时的用户名或 UID，后续的 RUN 也会使用指定用户。使用USER指定用户时，可以使用用户名、UID或GID，或是两者的组合。当服务不需要管理员权限时，可以通过该命令指定运行用户。并且可以在之前创建所需要的用户
>
>ONBUILD：用于设置镜像触发器

### 3.4-编译Dockerfile文件生成Docker镜像（镜像名 不能有大写）

```java
docker build -t ytking-essays .
```

![image-20211208212427055](image-20211208212427055.png)

### 3.5-运行构建的镜像

![image-20211208212536458](image-20211208212536458.png)

开放8088端口，与项目的配置文件中的端口一致，再将服务器的安全组和服务器的防火墙开放8088端口，就可以通过api访问接口

### 3.6-测试

通过postman测试接口成功

![image-20211208212726424](image-20211208212726424.png)

查看容器运行的输出日志

```java
docker logs -f 容器ID
```

![image-20211208213000101](image-20211208213000101.png)

