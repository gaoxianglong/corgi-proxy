<div align=center><img src="https://github.com/gaoxianglong/corgi-proxy/blob/master/corgi-logo.jpeg"/></div>
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)[![Join the chat at https://gitter.im/gaoxianglong/shark](https://badges.gitter.im/gaoxianglong/shark.svg)](https://gitter.im/gaoxianglong/shark?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## corgi-proxy是啥？
大规模服务化场景下，Zookeeper其实是不适合直接作为注册中心使用的，因为它是一个典型的CP系统，是基于ZAB协议的强一致性中间件，它的写操作存在单点问题，无法通过水平扩容来解决。换句话说，当TPS越高时，服务注册时的写入效率就越低，这会导致上游产生大量的请求排队，表象就是服务启动变得异常缓慢。其次冗余的服务配置项会导致Zookeeper的数据量急剧膨胀，服务节点的任何变化，都会导致消费者每次都全量拉取一个服务接口下的所有地址列表，大促前后服务的扩容/缩容操作所带来的瞬时流量容易瞬间就把Zookeeper集群的的网卡打满，因此我们需要一种基于最终一致性、增量数据拉取，且去中心化的注册中心解决方案，这就是corgi-proxy诞生的意义。
