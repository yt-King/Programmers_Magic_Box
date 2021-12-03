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

