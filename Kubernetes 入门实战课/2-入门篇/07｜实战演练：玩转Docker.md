# 07｜实战演练：玩转Docker

学到今天的这次课，我们的“入门篇”就算是告一段落了，有这些容器知识作为基础，很快我们就要正式开始学习 Kubernetes。不过在那之前，来对前面的课程做一个回顾和实践，把基础再夯实一下。

要提醒你的是，Docker 相关的内容很多很广，在入门篇中，我只从中挑选出了一些最基本最有用的介绍给你。而且在我看来，我们不需要完全了解 Docker 的所有功能，我也不建议你对 Docker 的内部架构细节和具体的命令行参数做过多的了解，太浪费精力，只要会用够用，需要的时候能够查找官方手册就行。

毕竟我们这门课程的目标是 Kubernetes，而 Docker 只不过是众多容器运行时（Container Runtime）中最出名的一款而已。当然，如果你当前的工作是与 Docker 深度绑定，那就另当别论了。

好下面我先把容器技术做一个简要的总结，然后演示两个实战项目：使用 Docker 部署 Registry 和 WordPress。

## 容器技术要点回顾

容器技术是后端应用领域的一项重大创新，它彻底变革了应用的开发、交付与部署方式，是“云原生”的根本（01 讲）。

容器基于 Linux 底层的 namespace、cgroup、chroot 等功能，虽然它们很早就出现了，但直到 Docker“横空出世”，把它们整合在一起，容器才真正走近了大众的视野，逐渐为广大开发者所熟知（02 讲）。

容器技术中有三个核心概念：**容器（Container）**、**镜像（Image）**，以及**镜像仓库（Registry）**（03 讲）。

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/c8116066bdbf295a7c9fc25b87755dfe.jpg)

从本质上来说，容器属于虚拟化技术的一种，和虚拟机（Virtual Machine）很类似，都能够分拆系统资源，隔离应用进程，但容器更加轻量级，运行效率更高，比虚拟机更适合云计算的需求。

镜像是容器的静态形式，它把应用程序连同依赖的操作系统、配置文件、环境变量等等都打包到了一起，因而能够在任何系统上运行，免除了很多部署运维和平台迁移的麻烦。

镜像内部由多个层（Layer）组成，每一层都是一组文件，多个层会使用 Union FS 技术合并成一个文件系统供容器使用。这种细粒度结构的好处是相同的层可以共享、复用，节约磁盘存储和网络传输的成本，也让构建镜像的工作变得更加容易（04 讲）。

为了方便管理镜像，就出现了镜像仓库，它集中存放各种容器化的应用，用户可以任意上传下载，是分发镜像的最佳方式（05 讲）。

目前最知名的公开镜像仓库是 Docker Hub，其他的还有 quay.io、gcr.io，我们可以在这些网站上找到许多高质量镜像，集成到我们自己的应用系统中。

容器技术有很多具体的实现，Docker 是最初也是最流行的容器技术，它的主要形态是运行在 Linux 上的“Docker Engine”。我们日常使用的 docker 命令其实只是一个前端工具，它必须与后台服务“Docker daemon”通信才能实现各种功能。

操作容器的常用命令有 docker ps、docker run、docker exec、docker stop 等；操作镜像的常用命令有 docker images、docker rmi、docker build、docker tag 等；操作镜像仓库的常用命令有 docker pull、docker push 等。

好简单地回顾了容器技术，下面我们就来综合运用在“入门篇”所学到的各个知识点，开始实战演练，玩转 Docker。

## 搭建私有镜像仓库

在第 5 节课讲 Docker Hub 的时候曾经说过，在离线环境里，我们可以自己搭建私有仓库。但因为镜像仓库是网络服务的形式，当时还没有学到容器网络相关的知识，所以只有到了现在，我们具备了比较完整的 Docker 知识体系，才能够搭建私有仓库。

私有镜像仓库有很多现成的解决方案，今天我只选择最简单的 Docker Registry，而功能更完善的 CNCF Harbor 留到后续学习 Kubernetes 时再介绍。

你可以在 Docker Hub 网站上搜索“registry”，找到它的官方页面（https://registry.hub.docker.com/_/registry/）：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/cbab37b3a4f2a621ab81564bcdba1326.png" alt="img" style="zoom:50%;" />

Docker Registry 的网页上有很详细的说明，包括下载命令、用法等，我们可以完全照着它来操作。

首先，你需要使用 docker pull 命令拉取镜像：

```bash
docker pull registry
```

然后，我们需要做一个端口映射，对外暴露端口，这样 Docker Registry 才能提供服务。它的容器内端口是 5000，简单起见，我们在外面也使用同样的 5000 端口，所以运行命令就是 docker run -d -p 5000:5000 registry ：

```bash
docker run -d -p 5000:5000 registry
```

启动 Docker Registry 之后，你可以使用 docker ps 查看它的运行状态，可以看到它确实把本机的 5000 端口映射到了容器内的 5000 端口。

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/183e8953bb544c1dbf42022bfacef101.png)

接下来，我们就要使用 docker tag 命令给镜像打标签再上传了。因为上传的目标不是默认的 Docker Hub，而是本地的私有仓库，所以镜像的名字前面还必须再加上仓库的地址（域名或者 IP 地址都行），形式上和 HTTP 的 URL 非常像。

比如在这里，我就把“nginx:alpine”改成了“127.0.0.1:5000/nginx:alpine”：

```bash
docker tag nginx:alpine 127.0.0.1:5000/nginx:alpine
```

现在，这个镜像有了一个附加仓库地址的完整名字，就可以用 docker push 推上去了：

```bash
docker push 127.0.0.1:5000/nginx:alpine
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/c851b97a722de6fe2a144ec75c69e27f.png" alt="img" style="zoom:50%;" />

为了验证是否已经成功推送，我们可以把刚才打标签的镜像删掉，再重新下载：

```bash
docker rmi  127.0.0.1:5000/nginx:alpine

docker pull 127.0.0.1:5000/nginx:alpine
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/54a288df48531205236f024ecd91816e.png" alt="img" style="zoom:50%;" />

这里 docker pull 确实完成了镜像下载任务，不过因为原来的层原本就已经存在，所以不会有实际的下载动作，只会创建一个新的镜像标签。

Docker Registry 虽然没有图形界面，但提供了 RESTful API，也可以发送 HTTP 请求来查看仓库里的镜像，具体的端点信息可以参考官方文档（https://docs.docker.com/registry/spec/api/），下面的这两条 curl 命令就分别获取了镜像列表和 Nginx 镜像的标签列表：

```bash
curl 127.1:5000/v2/_catalog

curl 127.1:5000/v2/nginx/tags/list
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/c4715261ef7e131b53af31931c1ecee8.png" alt="img" style="zoom:50%;" />

可以看到，因为应用被封装到了镜像里，所以我们只用简单的一两条命令就完成了私有仓库的搭建工作，完全不需要复杂的软件安装、环境设置、调试测试等繁琐的操作，这在容器技术出现之前简直是不可想象的。

## 搭建 WordPress 网站

Docker Registry 应用比较简单，只用单个容器就运行了一个完整的服务，下面我们再来搭建一个有点复杂的 WordPress 网站。

网站需要用到三个容器：WordPress、MariaDB、Nginx，它们都是非常流行的开源项目，在 Docker Hub 网站上有官方镜像，网页上的说明也很详细，所以具体的搜索过程我就略过了，直接使用 docker pull 拉取它们的镜像：

```bash
docker pull wordpress:5

docker pull mariadb:10

docker pull nginx:alpine
```

我画了一个简单的网络架构图，你可以直观感受一下它们之间的关系：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/59dfbe961bcd233b83e1c1ec064e2eca.png" alt="img" style="zoom:50%;" />

这个系统可以说是比较典型的网站了。MariaDB 作为后面的关系型数据库，端口号是 3306；WordPress 是中间的应用服务器，使用 MariaDB 来存储数据，它的端口是 80；Nginx 是前面的反向代理，它对外暴露 80 端口，然后把请求转发给 WordPress。

我们先来运行 MariaDB。根据说明文档，需要配置“MARIADB_DATABASE”等几个环境变量，用 --env 参数来指定启动时的数据库、用户名和密码，这里我指定数据库是“db”，用户名是“wp”，密码是“123”，管理员密码（root password）也是“123”。

下面就是启动 MariaDB 的 docker run 命令：

```bash
docker run -d --rm \
    --env MARIADB_DATABASE=db \
    --env MARIADB_USER=wp \
    --env MARIADB_PASSWORD=123 \
    --env MARIADB_ROOT_PASSWORD=123 \
    mariadb:10
```

启动之后，我们还可以使用 docker exec 命令，执行数据库的客户端工具“mysql”，验证数据库是否正常运行：

```bash
docker exec -it 9ac mysql -u wp -p
```

输入刚才设定的用户名“wp”和密码“123”之后，我们就连接上了 MariaDB，可以使用 show databases; 和 show tables; 等命令来查看数据库里的内容。当然，现在肯定是空的。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/ef28b4482ef243c215b09dae6889695e.png" alt="img" style="zoom:50%;" />

因为 Docker 的 bridge 网络模式的默认网段是“172.17.0.0/16”，宿主机固定是“172.17.0.1”，而且 IP 地址是顺序分配的，所以如果之前没有其他容器在运行的话，MariaDB 容器的 IP 地址应该就是“172.17.0.2”，这可以通过 docker inspect 命令来验证：

```bash
docker inspect 9ac |grep IPAddress
```

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/f371e89b97a0410acb628ac2889427e6.png" alt="img" style="zoom:50%;" />

现在数据库服务已经正常，该运行应用服务器 WordPress 了，它也要用 --env 参数来指定一些环境变量才能连接到 MariaDB，注意“WORDPRESS_DB_HOST”必须是 MariaDB 的 IP 地址，否则会无法连接数据库：

```bash
docker run -d --rm \

    --env WORDPRESS_DB_HOST=172.17.0.2 \

    --env WORDPRESS_DB_USER=wp \

    --env WORDPRESS_DB_PASSWORD=123 \

    --env WORDPRESS_DB_NAME=db \

    wordpress:5
```

WordPress 容器在启动的时候并没有使用 -p 参数映射端口号，所以外界是不能直接访问的，我们需要在前面配一个 Nginx 反向代理，把请求转发给 WordPress 的 80 端口。

配置 Nginx 反向代理必须要知道 WordPress 的 IP 地址，同样可以用 docker inspect 命令查看，如果没有什么意外的话它应该是“172.17.0.3”，所以我们就能够写出如下的配置文件（Nginx 的用法可参考其他资料，这里就不展开讲了）：

```nginx
server {
  listen 80;
  default_type text/html;
  
  location / {
      proxy_http_version 1.1;
      proxy_set_header Host $host;
      proxy_pass http://172.17.0.3;
  }
}
```

有了这个配置文件，最关键的一步就来了，我们需要用 -p 参数把本机的端口映射到 Nginx 容器内部的 80 端口，再用 -v 参数把配置文件挂载到 Nginx 的“conf.d”目录下。这样，Nginx 就会使用刚才编写好的配置文件，在 80 端口上监听 HTTP 请求，再转发到 WordPress 应用：

```bash
docker run -d --rm \
    -p 80:80 \
    -v `pwd`/wp.conf:/etc/nginx/conf.d/default.conf \
    nginx:alpine
```

三个容器都启动之后，我们再用 docker ps 来看看它们的状态：

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/353f0f3aed1d1b540312548c29cb6c15.png)

可以看到，WordPress 和 MariaDB 虽然使用了 80 和 3306 端口，但被容器隔离，外界不可见，只有 Nginx 有端口映射，能够从外界的 80 端口收发数据，网络状态和我们的架构图是一致的。

现在整个系统就已经在容器环境里运行好了，我们来打开浏览器，输入本机的“127.0.0.1”或者是虚拟机的 IP 地址（我这里是“http://192.168.10.208”），就可以看到 WordPress 的界面：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/a63084a8dd95d0034ba72dcb60613531.png" alt="img" style="zoom:33%;" />

在创建基本的用户、初始化网站之后，我们可以再登录 MariaDB，看看是否已经有了一些数据：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/a22dcabe805471e304a74c715e7fb51e.png" alt="img" style="zoom: 33%;" />

可以看到，WordPress 已经在数据库里新建了很多的表，这就证明我们的容器化的 WordPress 网站搭建成功。

## 小结

好了，今天我们简单地回顾了一下容器技术，这里有一份思维导图，是对前面所有容器知识要点的总结，你可以对照着用来复习。

![img](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/79f8c75e018e0a82eff432786110ef16.jpg)

我们还使用 Docker 实际搭建了两个服务：Registry 镜像仓库和 WordPress 网站。

通过这两个项目的实战演练，你应该能够感受到容器化对后端开发带来的巨大改变，它简化了应用的打包、分发和部署，简单的几条命令就可以完成之前需要编写大量脚本才能完成的任务，对于开发、运维来绝对是一个“福音”。

不过，在感受容器便利的同时，你有没有注意到它还是存在一些遗憾呢？比如说：

- 我们还是要手动运行一些命令来启动应用，然后再人工确认运行状态。
- 运行多个容器组成的应用比较麻烦，需要人工干预（如检查 IP 地址）才能维护网络通信。
- 现有的网络模式功能只适合单机，多台服务器上运行应用、负载均衡该怎么做？
- 如果要增加应用数量该怎么办？这时容器技术完全帮不上忙。

其实，如果我们仔细整理这些运行容器的 docker run 命令，写成脚本，再加上一些 Shell、Python 编程来实现自动化，也许就能够得到一个勉强可用的解决方案。

这个方案已经超越了容器技术本身，是在更高的层次上规划容器的运行次序、网络连接、数据持久化等应用要素，也就是现在我们常说的“**容器编排**”（Container Orchestration）的雏形，也正是后面要学习的 Kubernetes 的主要出发点。

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2022-11/43ccbfc6b629eed231ffec8d6eaf99fa.jpg" alt="img" style="zoom:33%;" />