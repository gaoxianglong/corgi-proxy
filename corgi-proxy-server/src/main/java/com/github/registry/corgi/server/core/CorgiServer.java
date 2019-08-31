/*
 * Copyright 2019-2119 gao_xianglong@sina.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.registry.corgi.server.core;

import com.github.registry.corgi.server.Constants;
import com.github.registry.corgi.server.Parameters;
import com.github.registry.corgi.server.core.handlers.*;
import com.github.registry.corgi.server.exceptions.StartingException;
import com.github.registry.corgi.utils.CapacityConvert;
import com.github.registry.corgi.utils.HostAddressUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * corgi-server引导类,ServerChannel
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-19 14:45
 */
public class CorgiServer {
    /**
     * 业务线程池,缺省使用fixed
     */
    private ExecutorService executor;
    private final String OS = Constants.OS_NAME;
    private Parameters parameters;
    private ZookeeperConnectionHandler connectionHandler;
    private long beginTime;
    private static Logger log = LoggerFactory.getLogger("");

    public CorgiServer(long beginTime, Parameters parameters, ExecutorService executor,
                       ZookeeperConnectionHandler connectionHandler) {
        this.beginTime = beginTime;
        this.parameters = parameters;
        this.executor = executor;
        this.connectionHandler = connectionHandler;
    }

    public void start() throws StartingException {
        bind(parameters.getBossThreadSize(), parameters.getWorkerThreadSize(), parameters.getPort(),
                parameters.getBackLog(), parameters.getRcvbuf());
    }

    /**
     * corgi-server相关初始化操作
     *
     * @param bossThreadSize
     * @param workerThreadSize
     * @param port
     * @param backLog
     * @param rcvbuf
     * @throws StartingException
     */
    private void bind(int bossThreadSize, int workerThreadSize, int port, int backLog, int rcvbuf) throws StartingException {
        EventLoopGroup bossGroup = getEventLoopGroup(bossThreadSize);
        EventLoopGroup workerGroup = getEventLoopGroup(workerThreadSize);
        ServerBootstrap bootstrap = new ServerBootstrap();//创建服务端引导类
        try {
            bootstrap.group(bossGroup, workerGroup)//添加boss线程和worker线程
                    .channel(getChannelClass())//指定Channel的传输类型,如果是Linux环境下则使用EpollServerSocketChannel
                    .localAddress(port)//指定目标端口
                    .childHandler(new ChannelInitializer<SocketChannel>() {//添加相关的ChannelHandler实现
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("encoder", new ProtocolEncoder());//添加编码器
                            /*
                             * 添加解码器
                             *
                             * length字段索引为0,占4字节长度,其它报文头占13字节长度, 最终输出截取掉length字段的4字节
                             */
                            ch.pipeline().addLast("decoder", new ProtocolDecoder((int) Constants.MAX_FRAME_LENGTH, Constants.LENGTH_FIELD_OFFSET,
                                    Constants.LENGTH_FIELD_LENGTH, Constants.LENGTH_ADJUSTMENT, Constants.INITIAL_BYTES_TO_STRIP));
                            ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(Constants.READER_IDLE_TIME, Constants.WRITE_IDLE_TIME,
                                    Constants.ALL_IDLE_TIME, TimeUnit.SECONDS));//添加IdleStateHandler
                            ch.pipeline().addLast("acceptorIdleStateTrigger", new AcceptorIdleStateTrigger());//添加读空闲超时处理的ChannelHandler
                            ch.pipeline().addLast("heartbeatHandler", new HeartbeatHandler());//添加心跳处理的ChannelHandler
                            ch.pipeline().addLast("cokeyHandler", new CokeyHandler(connectionHandler, executor));//添加核心命令处理的ChannelHandler
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, backLog)//Socket参数,服务端接受连接的队列长度,如果队列已满,客户端连接将被拒绝
                    .option(ChannelOption.SO_RCVBUF, (int) CapacityConvert.convert(rcvbuf, CapacityConvert.KB))//Socket参数,TCP数据接收缓冲区大小
                    /*
                     * 是否启用心跳保活机制.在双方TCP套接字建立连接后(即都进入ESTABLISHED状态)
                     * 并且在两个小时左右上层没有任何数据传输的情况下,这套机制才会被激活
                     */
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind().sync();
            if (future.isSuccess()) {
                try {
                    registerCorgiNode();//服务启动成功后,将自身注册到注册中心
                    long endTime = System.currentTimeMillis();
                    log.info("Initialization processed in {} s", new BigDecimal(endTime - beginTime)
                            .divide(new BigDecimal(1000), 2, BigDecimal.ROUND_DOWN).doubleValue());
                    log.info("Corgi-server start successful (bind port: {}, pid: {})", port, Constants.PID);
                } finally {
                    //clean();
                    destroyAll(bossGroup, workerGroup);
                }
            }
            future.channel().closeFuture().sync();
        } catch (Throwable e) {
            throw new StartingException(e);
        }
    }

    /**
     * 释放资源
     *
     * @param bossGroup
     * @param workerGroup
     */
    private void destroyAll(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        //注册钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (null != connectionHandler) {
                log.debug("Stopping the corgi-server...");
                try {
                    if (!executor.isShutdown()) {
                        executor.shutdownNow(); //发起interrupt信号，业务线程池拒绝再接收任何新请求
                        while (true) {
                            //缺省每隔2s检测工作线程是否已经全部结束
                            if (executor.awaitTermination(Constants.CHECK_TIMEOUT,
                                    TimeUnit.SECONDS)) {
                                break;
                            }
                        }
                    }
                    //释放Netty的I/O线程组资源
                    bossGroup.shutdownGracefully().sync();
                    workerGroup.shutdownGracefully().sync();
                    //断开Zookeeper的会话连接
                    connectionHandler.close();
                } catch (InterruptedException e) {
                    //...
                }
            }
        }));
    }

    /**
     * 注册corgi-server，后续可用于Corgi自身的发布/订阅，目前仅仅用于显示/输出当前corgi-server节点数量
     *
     * @throws Exception
     */
    private void registerCorgiNode() throws Exception {
        final String ROOT_PATH = Constants.ROOT_PATH;
        final String PATH = String.format("%s:%s", HostAddressUtil.getLocalAddress(), parameters.getPort());
        connectionHandler.createEphemeralNode(ROOT_PATH, PATH);//根目录不存在会自动创建
        log.info("Register node success (znode: {}/{})", ROOT_PATH, PATH);
        log.info("Topology snapshot [servers={}, CPUs={}]",
                connectionHandler.getChildrens(ROOT_PATH).size(), Runtime.getRuntime().availableProcessors());
    }

    /**
     * 根据操作系统类型选择合适的EventLoopGroup实现，Linux操作系统下直接使用性能更好的EpollEventLoopGroup
     *
     * @return
     */
    private EventLoopGroup getEventLoopGroup(int threadSize) {
        return "linux".equalsIgnoreCase(OS) ? new EpollEventLoopGroup(threadSize)
                : new NioEventLoopGroup(threadSize);
    }

    /**
     * 根据操作系统类型选择合适的ServerChannel实现，Linux操作系统下直接使用性能更好的EpollServerSocketChannel
     *
     * @return
     */
    private Class<? extends ServerChannel> getChannelClass() {
        return "linux".equalsIgnoreCase(OS) ? EpollServerSocketChannel.class
                : NioServerSocketChannel.class;
    }
}
