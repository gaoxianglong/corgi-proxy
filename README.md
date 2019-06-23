<div align=center><img width="350" height="262" src="https://github.com/gaoxianglong/corgi-proxy/blob/master/resources/imgs/corgi-logo.jpeg"/></div>

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html) ![License](https://img.shields.io/badge/build-passing-brightgreen.svg) ![License](https://img.shields.io/badge/version-0.1--SNAPSHOT-blue.svg)
> 最终一致性、增量数据拉取，且去中心化的Registry中间件<br/>
> 大规模服务化场景下注册中心性能瓶颈解决方案<br/>
## 项目背景
Zookeeper并不适合作为大规模服务化场景下的Registry，其写操作存在单点问题，不可扩展，由于采用的原子广播协议（>=N/2+1），集群节点越多，写入效率就越低，表象就是应用启动异常缓慢；其次服务节点的任何变化，都会导致消费者每次都全量拉取一个服务接口下的所有地址列表，大促前后服务的扩容/缩容操作所带来的瞬时流量容易瞬间就把Registry集群的的网卡打满。**因此我们需要一个最终一致性、增量数据拉取，且去中心化的Registry解决方案，corgi-proxy由此诞生**。

## corgi-proxy使用
- corgi-proxy基于SPI扩展了dubbo的注册中心:
```java
<dubbo:registry protocol="corgi" id="corgi" address="${host}"/>
```
- 在src/resources目录下新建META-INF/dubbo/com.alibaba.dubbo.registry.RegistryFactory文件：
```java
corgi=com.github.registry.corgi.CorgiRegistryFactory
```
- 非dubbo项目，原生客户端API的使用：
```java
CorgiFramework framework = new CorgiFramework.Builder(new HostAndPort(hostName, port))//绑定host和port
        .redirections(redirections)//指定重试次数
        .serialization(CorgiFramework.SerializationType.FST)//指定序列化协议
        .builder()
        .init();//相关初始化
framework.register("/dubbo/service", "127.0.0.1:20890");//服务注册
framework.subscribe("/dubbo/service");//订阅
framework.unRegister("/dubbo/service", "127.0.0.1:20890");//取消注册
Executors.newSingleThreadScheduledExecutor().execute(() -> {
    while (true) {
        framework.subscribe("/dubbo/service");//持续订阅
    }
});
```
目前corgi-proxy暂未提交到Maven中央仓库，请自行编译打包；更多使用案例，参考corgi-proxy-demo。

## corgi-proxy特性&要求
- `低侵入性`：只需引入corgi-proxy-api构件，添加相关配置即可
- `简单易用`：低成本，如果是基于dubbo，缺省提供有基于SPI扩展的注册中心实现
- `高伸缩性`：corgi-proxy集群中各个节点之间无状态，可随意伸缩
- `增量拉取`：服务发现场景下可避免惊群效应时所带来的瞬时流量将Registry集群的网卡打满
- `高可靠性`：封装和依赖Apache Curator的TreeCache来保证一个时序正确、最终一致的事件流
- `版本要求`：Java8以上，不支持低版本JDK

## 整体架构
<div align=center><img width="350" height="262" src="https://github.com/gaoxianglong/corgi-proxy/blob/master/resources/imgs/architecture.jpeg"/></div>
应用无需直连Zookeeper，而是通过corgi-proxy中间件来完成服务的注册/发现，极大程度上缩减了Zookeeper的客户端连接数。corgi-proxy内部通过依赖Apache Curator的TreeCache来实现数据的增量拉取策略，换句话说，corgi-proxy内部会维系一份内存快照数据，当目标节点发生任何变化时，通过比对内存快照中的元信息（zxid）来明确具体的上/下线事件，然后再将具体的更新事件写入到与每个corgi-proxy客户端对应的阻塞队列中，等待其主动增量拉取消费（首次拉取为全量）。