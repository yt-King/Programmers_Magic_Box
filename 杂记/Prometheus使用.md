# Prometheus使用

## 1.安装

```bash
sudo mkdir /etc/prometheus

sudo touch /etc/prometheus/prometheus.yml

docker run -d -p 9090:9090 -v /etc/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml -name prometheus prom/prometheus
```

**通过http://localhost:9090/访问：**

![image-20230802104442423](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2023-6/image-20230802104442423.png)

## 2.第一个exporter

Exporter可以是一个相对开放的概念，其可以是一个独立运行的程序独立于监控目标以外，也可以是直接内置在监控目标中。只要能够向Prometheus提供标准格式的监控样本数据即可。 为了能够采集到主机的运行指标如CPU, 内存，磁盘等信息，可以使用[Node Exporter](https://github.com/prometheus/node_exporter)。

```bash
docker run -d -p 9100:9100 --name=node-exporter prom/node-exporter
```

**通过http://localhost:9100/访问：**

![image-20230802142753489](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2023-6/image-20230802142753489.png)

## 3.可视化

安装grafana

```bash
docker run -d --name grafana -p 3000:3000  grafana/grafana
```

配置yml文件：

```bash
cd /etc/prometheus

vim prometheus.yml

#-----------将以下内容写入------------
global:
  scrape_interval: 60s
  evaluation_interval: 60s

scrape_configs:
  - job_name: prometheus
    static_configs:
      - targets: ['localhost:9090']
        labels:
          instance: prometheus

  - job_name: node_exporter
    static_configs:
      - targets: ['你的ip地址:9100']
        labels:
          instance: localhost
#------------------------------------- 

#	重启容器
docker restart xxxxx(prometheus容器id)
```

通过http://localhost:3000/datasources/new新增一个数据源，选择prometheus：

<img src="https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2023-6/image-20230802152210541.png" alt="image-20230802152210541" style="zoom:67%;" />

进行相关配置后保存：

![image-20230802152455897](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2023-6/image-20230802152455897.png)

数据源配置完成后进行可视化配置，在https://grafana.com/grafana/dashboards/11074下载模板页面的json文件，在grafana首页的右上角点击➕，选择import dashboard，上传刚刚下载的json文件并选择配置好的数据源，最后即可看到数据面板：

![image-20230802152944468](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2023-6/image-20230802152944468.png)

## 4.任务和实例

在Prometheus中，每一个暴露监控样本数据的HTTP服务称为一个实例。例如在当前主机上运行的node exporter可以被称为一个实例(Instance)。

而一组用于相同采集目的的实例，或者同一个采集进程的多个副本则通过一个一个任务(Job)进行管理。

```
* job: node
   * instance 2: 1.2.3.4:9100
   * instance 4: 5.6.7.8:9100
```

当前在每一个Job中主要使用了静态配置(static_configs)的方式定义监控目标。除了静态配置每一个Job的采集Instance地址以外，Prometheus还支持与DNS、Consul、E2C、Kubernetes等进行集成实现自动发现Instance实例，并从这些Instance上获取监控数据。

可以访问http://localhost:9090/targets直接从Prometheus的UI中查看当前所有的任务以及每个任务对应的实例信息。

## 5.核心组件

### Prometheus Server

Prometheus Server是Prometheus组件中的核心部分，负责实现对监控数据的获取，存储以及查询。 Prometheus Server可以通过静态配置管理监控目标，也可以配合使用Service Discovery的方式动态管理监控目标，并从这些监控目标中获取数据。其次Prometheus Server需要对采集到的监控数据进行存储，Prometheus Server本身就是一个时序数据库，将采集到的监控数据按照时间序列的方式存储在本地磁盘当中。最后Prometheus Server对外提供了自定义的PromQL语言，实现对数据的查询以及分析。

Prometheus Server内置的Express Browser UI，通过这个UI可以直接通过PromQL实现数据的查询以及可视化。

Prometheus Server的联邦集群能力可以使其从其他的Prometheus Server实例中获取数据，因此在大规模监控的情况下，可以通过联邦集群以及功能分区的方式对Prometheus Server进行扩展。

### Exporters

Exporter将监控数据采集的端点通过HTTP服务的形式暴露给Prometheus Server，Prometheus Server通过访问该Exporter提供的Endpoint端点，即可获取到需要采集的监控数据。

一般来说可以将Exporter分为2类：

- 直接采集：这一类Exporter直接内置了对Prometheus监控的支持，比如cAdvisor，Kubernetes，Etcd，Gokit等，都直接内置了用于向Prometheus暴露监控数据的端点。
- 间接采集：间接采集，原有监控目标并不直接支持Prometheus，因此我们需要通过Prometheus提供的Client Library编写该监控目标的监控采集程序。例如： Mysql Exporter，JMX Exporter，Consul Exporter等。

### AlertManager

在Prometheus Server中支持基于PromQL创建告警规则，如果满足PromQL定义的规则，则会产生一条告警，而告警的后续处理流程则由AlertManager进行管理。在AlertManager中我们可以与邮件，Slack等等内置的通知方式进行集成，也可以通过Webhook自定义告警处理方式。AlertManager即Prometheus体系中的告警处理中心。

### PushGateway

由于Prometheus数据采集基于Pull模型进行设计，因此在网络环境的配置上必须要让Prometheus Server能够直接与Exporter进行通信。 当这种网络需求无法直接满足时，就可以利用PushGateway来进行中转。可以通过PushGateway将内部网络的监控数据主动Push到Gateway当中。而Prometheus Server则可以采用同样Pull的方式从PushGateway中获取到监控数据。

## 6.实际使用

以java为例，如何结合prometheus进行相关数据的上报与页面展示，数据源与prometheus交互的方式有两种，第一种是像上文的node-exporter一样开放数据端口让prometheus定时拉取；另一种是主动将数据推送到PushGateway，prometheus定时拉取PushGateway的数据。本次的例子采用第二种方式。

### 准备工作

docker形式创建PushGateway：

```
docker run -d \
  --name=pushgateway \
  -p 9091:9091 \
  prom/pushgateway
```

配置prometheus的配置文件：

```bash
~ % cd /etc/prometheus 
prometheus % sudo vim prometheus.yml 
#写入以下配置后重启prometheus容器
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: prometheus
    static_configs:
      - targets: ['localhost:9090']
        labels:
          instance: prometheus

  - job_name: node_exporter
    static_configs:
      - targets: ['你的ip地址:9100']
        labels:
          instance: localhost
  - job_name: push_gateway
    static_configs:
      - targets: ['你的ip地址:9091']
        labels:
          instance: gateway
```

新建maven项目，java依赖如下：

```xml
<dependencies>
     <dependency>
         <groupId>io.prometheus</groupId>
         <artifactId>simpleclient</artifactId>
         <version>0.9.0</version>
     </dependency>
     <dependency>
         <groupId>io.prometheus</groupId>
         <artifactId>simpleclient_pushgateway</artifactId>
         <version>0.9.0</version>
     </dependency>
</dependencies>
```

### 模拟推送

模拟接口统计信息推送：

```java
public static void main(String[] args) throws InterruptedException, IOException {
    Random rnd = new Random();
    PushGateway gateway = new PushGateway("localhost:9091");
    Counter counterSuccess = Counter.build()
            .name("success_count") //请求访问成功数
            .labelNames("method_name") //请求名
            .help("method_name_success_count") //这个名字随便起
            .register(); //注：通常只能注册1次，1个实例中重复注册会报错
    Counter counterCostSuccess = Counter.build()
            .name("success_cost") //请求访问成功耗时
            .labelNames("method_name") //请求名
            .help("method_name_success_cost_count") //这个名字随便起
            .register(); //注：通常只能注册1次，1个实例中重复注册会报错
    while (true) {
        //请求成功数+随机数
        int num1 = rnd.nextInt(10);
        counterSuccess.labels("/item/user/v1/list").inc(num1);
        //请求成功耗时+随机数
        int num2 = rnd.nextInt(1000);
        counterCostSuccess.labels("/item/user/v1/list").inc(num2);
        counterCostSuccess.labels("/item/user/v1/query").inc(num2 + 100);
        //利用网关采集数据
        gateway.push(counterSuccess, "method_count", Map.of("client", "count"));
        gateway.push(counterCostSuccess, "method_count", Map.of("client", "cost"));
        System.out.println("counterSuccess:" + num1 + "       counterCostSuccess" + num2);
        //辅助输出日志
        Thread.sleep(10000);
    }
}
```

### 面板绘制

通过http://localhost:9091/metrics可以看到推送的原始数据

```bash
......
......
# HELP success_cost method_name_success_cost_count
# TYPE success_cost counter
success_cost{client="cost",instance="",job="method_count",method_name="/item/user/v1/list"} 8940
success_cost{client="cost",instance="",job="method_count",method_name="/item/user/v1/query"} 10740
# HELP success_count method_name_success_count
# TYPE success_count counter
success_count{client="count",instance="",job="method_count",method_name="/item/user/v1/list"} 89
......
......
```

进入http://localhost:3000/dashboard/new?orgId=1创建新的面板，选择对应指标进行展示：

![image-20230804143032022](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2023-6/image-20230804143032022.png)

上图展示的是我模拟的两个接口的每次请求耗时的累计值，想要观察数据的变化趋势可以使用rate函数显示指标的单位时间内的变化趋势：

![image-20230804143356784](https://typora-imagehost-1308499275.cos.ap-shanghai.myqcloud.com/2023-6/image-20230804143356784.png)

通过rate函数可以看到单位时间内的变化情况，[1m]表示的就是这1分钟内的平均变化趋势，由于数据上报是每十秒一次，所以最后的结果要乘上10才是正确的数据。