<div align=center><img width="350" height="262" src="https://github.com/gaoxianglong/corgi-proxy/blob/master/resources/imgs/corgi-logo.jpeg"/></div>

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html) ![License](https://img.shields.io/badge/build-passing-brightgreen.svg) ![License](https://img.shields.io/badge/version-0.1--SNAPSHOT-blue.svg)
> Decentralized Registry middleware with ultimate consistency and incremental data pull<br/>
> Performance bottleneck solution for Registry in large-scale service scenario<br/>
## Project Background
Zookeeper is not suitable for Registry in large-scale service scenarios. There is a single point problem in its write operation, which is not scalable. Due to the atomic broadcast protocol (>= N/2+1), the more cluster nodes there are, the lower the writing efficiency will be, which means that the application starts abnormally slowly. Secondly, any change of service nodes will cause consumers to pull all address lists under one service interface at a time. The instantaneous flow generated by the expansion/contraction of the service before and after the promotion will easily fill up the network card of Registry cluster in an instant. **Therefore, we need a decentralized Registry solution with ultimate consistency and incremental data pull, thus corgi-proxy comes into being**. 

## Use of Corgi-proxy
#### Dubbo Item
- Corgi-proxy extends dubbo's registration center based on SPI:
```java
<dubbo:registry protocol="corgi" id="corgi" address="${host}"/>
```
- Make a new META-INF/dubbo/com.alibaba.dubbo.registry.Registry Factory file under src/resources directory:
```java
corgi=com.github.registry.corgi.CorgiRegistryFactory
```
#### Non-Dubbo Item
- Use of native client API:
```java
CorgiFramework framework = new CorgiFramework.Builder(new HostAndPort("127.0.0.1", 9376))//binding host and port 
        .redirections(2)//Designated number of retries
        .serialization(CorgiFramework.SerializationType.FST)//Designated serialization protocol
        .isBatch(true)//Batch pull-out switch, only one incremental pull at a time when it is closed. 
        .pullSize(10)//Designated the number of single pulls
        .pullTimeOut(10000)//pull time out, unit-ms
        .builder()
        .init();//Relevant initialization
        
framework.register("/dubbo/service", "127.0.0.1:20890");
framework.subscribe("/dubbo/service");
framework.unRegister("/dubbo/service", "127.0.0.1:20890");
Executors.newSingleThreadScheduledExecutor().execute(() -> {
    while (true) {
        framework.subscribe("/dubbo/service");
    }
});
```
Currently, corgi-proxy has not been submitted to Maven Central Warehouse. Please compile and package it by yourself. For more use cases, refer to corgi-proxy-demo.

## Corgi-proxy Requirements & Characteristics
- `Low invasiveness`：Simply introduce the corgi-proxy-api component and add relevant configuration
- `Easy to use`：Low-cost, if dubbo-based, default provides registry implementation based on SPI extensions
- `High scalability`：Stateless and scalable among nodes in corgi-proxy cluster
- `Incremental pull`：Under the service discovery scenario, the instantaneous traffic caused by the shock effect filling up the network card of Registry cluster could be avoided
- `High reliability`：TreeCache, encapsulating and relying on Apache Curator, ensures a properly timed and ultimately consistent flow of events
- `Requirements of version`：Java 8+, low version JDK is not supported

## Overall Structure
Instead of directly connecting to Zookeeper, the application uses corgi-proxy middleware to complete service registration/discovery, which greatly reduces the number of client connections. 
<div align=center><img width="550" height="372" src="https://github.com/gaoxianglong/corgi-proxy/blob/master/resources/imgs/architecture.jpeg"/></div>
Corgi-proxy realizes incremental data pull strategy by relying on TreeCache of Apache Curator. In other words, the corgi-proxy internal maintains a snapshot data in memory. When the target node changes, it identifies specific up/down events by comparing the meta-information (zxid) in the memory snapshot, and then the specific update events are written to the blocking queue corresponding to each corgi-proxy client, waiting for its active incremental pull consumption(the first pull is the full amount). 

## Monitor
<div align=center><img src="https://github.com/gaoxianglong/corgi-proxy/blob/master/resources/imgs/monitor.jpeg"/></div>