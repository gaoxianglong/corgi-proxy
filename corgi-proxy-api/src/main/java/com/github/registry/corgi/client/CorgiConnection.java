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
package com.github.registry.corgi.client;

import com.github.registry.corgi.client.handlers.AcceptorIdleStateTrigger;
import com.github.registry.corgi.client.handlers.CorgiClientHandler;
import com.github.registry.corgi.client.handlers.ProtocolDecoder;
import com.github.registry.corgi.client.handlers.ProtocolEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * corgi-client引导类，Channel
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 23:12
 */
public class CorgiConnection {
    /**
     * 获取操作系统类型
     */
    private final String OS = Constants.OS_NAME;
    private InetSocketAddress address;
    private Channel channel;
    private EventLoopGroup group;
    private Logger log = LoggerFactory.getLogger(CorgiConnection.class);

    protected CorgiConnection(InetSocketAddress address) {
        this.address = address;
    }

    protected void conn() {
        close();
        group = getEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(group).channel(getChannelClass()).remoteAddress(address).handler(
                    new ChannelInitializer<SocketChannel>() {
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
                            ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(Constants.READER_IDLE_TIME,
                                    Constants.WRITE_IDLE_TIME, Constants.ALL_IDLE_TIME, TimeUnit.SECONDS));//添加IdleStateHandler
                            ch.pipeline().addLast("acceptorIdleStateTrigger", new AcceptorIdleStateTrigger());//添加写空闲超时处理的ChannelHandler
                            //添加处理最终入站事件的ChannelHandler
                            ch.pipeline().addLast("corgiHandler", new CorgiClientHandler(CorgiFramework.getThreadMap(), CorgiFramework.getResultMap(),
                                    CorgiFramework.getRegisterPaths()));
                        }
                    });
            ChannelFuture future = bootstrap.connect().sync();//尝试连接corgi-server
            if (future.isSuccess()) {
                log.info("Corgi-server({}) connection successful", address.toString());
                channel = future.channel();
            }
        } catch (Throwable e) {
            log.warn("Corgi-server({}) connection failed!!!,reconnect...", address.toString());
            if (null != group) {
                try {
                    group.shutdownGracefully().sync();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            conn();
            try {
                TimeUnit.SECONDS.sleep(Constants.RECONNECT); //每隔5秒重连一次
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    public Channel getChannel() {
        return channel;
    }

    /**
     * 根据操作系统类型选择合适的EventLoopGroup实现，Linux操作系统下直接使用性能更好的EpollEventLoopGroup
     *
     * @return
     */
    private EventLoopGroup getEventLoopGroup() {
        return "linux".equalsIgnoreCase(OS) ? new EpollEventLoopGroup()
                : new NioEventLoopGroup();
    }

    /**
     * 根据操作系统类型选择合适的SocketChannel实现，Linux操作系统下直接使用性能更好的EpollSocketChannel
     *
     * @return
     */
    private Class<? extends SocketChannel> getChannelClass() {
        return "linux".equalsIgnoreCase(OS) ? EpollSocketChannel.class
                : NioSocketChannel.class;
    }

    protected boolean isActive() {
        if (null == channel) {
            return false;
        }
        return channel.isActive();
    }

    /**
     * 手动释放资源
     */
    public void close() {
        if (null != channel) {
            try {
                channel.close().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (null != group) {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
