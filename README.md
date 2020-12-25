# sentinel-dashboard-plus

在 Alibaba sentinel-dashboard 中集成各类数据源实现 sentinel 规则持久化存储

## 1. 概述

Sentinel是阿里巴巴开源的服务流量控制、熔断降级组件，具体介绍可见 [Sentinel 官网](https://sentinelguard.io/zh-cn/)。Sentinel 提供了 Sentinel 控制台（sentinel-dashboard）可以很方便的进行流量控制、熔断降级等规则的配置和管理。Sentinel 官方提供了[支持集成 Nacos、Apollo 等的扩展](https://sentinelguard.io/zh-cn/docs/dynamic-rule-configuration.html)以实现将 Sentinel 规则持久化到对应数据源中。但 sentinel-dashboard 中并没有进行对应实现，官方实现只是将规则存储在内存中，一旦应用服务或 sentinel-dashboard 服务关闭，配置的规则即丢失。这对于生产应用肯定无法接收，本项目在修改官方 sentinel-dashboard 源码的基础上，尝试集成各类数据源（目前只集成了 Nacos），以实现 Sentinel 规则的持久化存储。同时，在集成数据源的过程中，对一些地方（主要是 controller 层）的代码进行了重构。

## 2. Sentinel-Dashboard 部署

### 2.1 编译

下载源码，解压，使用以下命令编译打包，获取项目目录下 target 子目录中的 sentinel-dashboard-plus.jar。

```bash
$ mvn clean package -Dmaven.test.skip=true
```

### 2.2 启动

以使用 Nacos 作为数据源示例。

使用以下命令启动 sentinel-dashboar：

```java
$ java -Dserver.port=8080 \
    -Dsentinel.nacos.serverAddr=127.0.0.1:8848 \
    -Dsentinel.nacos.namespace=8b67d870-000d-4e1e-8124-fa7f506cec53 \
    -Dsentinel.nacos.group-id=SENTINEL-GROUP
    -jar sentinel-dashboard-plus.jar
```

各参数说明如下：

- `server.port`：指定 sentinel-dashboard 服务使用的端口。如果不指定，默认为 8080。

- `sentinel.nacos.serverAddr`：指定 Nacos server 地址。如果不配置，默认为 127.0.0.1:8848。通常需要配置该项。
- `sentinel.nacos.namespace`：指定 Sentinel 规则存储位于 Nacos 的哪个 namespace 下（注意，为 Nacos namespace id，而不是 namespace 名称）。如果不指定，默认为 public。推荐配置该项。
- `sentinel.nacos.group-id`：指定 Sentinel 规则存储于 Nacos 的 group 属性。如果不指定，默认为 SENTINEL-GROUP。通常不需配置，使用默认值即可。

### 2.3 访问控制台

以上 sentinel-dashboard-plus 启动成功后，通过 http://{IP}:{PORT} 即可访问 sentinel-dashboard 控制台（默认用户名和密码为 sentinel）。

## 3. Sentinel 客户端接入

以下以 Spring Boot 项目 Sentinel 客户端接入为例。

在项目中引入以下依赖：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-sentinel</artifactId>
    <version>${spring-cloud-alibaba.version}</version>
</dependency>
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-nacos</artifactId>
    <version>${sentinel-datasource-nacos.version}</version>
</dependency>
```

在 Spring Boot `application.yaml`中引入以下配置：

```yaml
spring:
    sentinel:
      transport:
        dashboard: localhost:8080  # sentinel-dashboard 部署地址
      datasource:
        flow:  # 配置流量控制
          nacos:
            server-addr: localhost:8848  # nacos server 部署地址
            data-id: ${spring.application.name}-flow-rules  # 固定写法
            group-id: SENTINEL_GROUP  # 与部署 sentinel-dashboard 时的 nacos group 配置保持一致
            namespace: 8b67d870-000d-4e1e-8124-fa7f506cec53  # 与部署 sentinel-dashboard 时的 nacos namespace 配置保持一致
            data-type: json
            rule-type: flow
        degrade:  # 配置熔断降级
          nacos:
            server-addr: localhost:8848
            data-id: ${spring.application.name}-degrade-rules
            group-id: SENTINEL_GROUP
            namespace: 8b67d870-000d-4e1e-8124-fa7f506cec53
            data-type: json
            rule-type: degrade
        authority:  # 配置授权
          nacos:
            server-addr: localhost:8848
            data-id: ${spring.application.name}-auth-rules
            group-id: SENTINEL_GROUP
            namespace: 8b67d870-000d-4e1e-8124-fa7f506cec53
            data-type: json
            rule-type: authority
```

应用服务中接入 Sentinel 客户端后，在 sentinel-dashboard 中对相应资源添加流量控制、熔断降级等规则即可。

## 4. 更多

有关 sentinel-dashboard 的使用以及其他功能和配置说明可参考 [Sentinel 官方文档](https://sentinelguard.io/zh-cn/docs/introduction.html)。