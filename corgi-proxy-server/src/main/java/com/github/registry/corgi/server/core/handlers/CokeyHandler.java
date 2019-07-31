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
package com.github.registry.corgi.server.core.handlers;

import com.github.registry.corgi.server.Constants;
import com.github.registry.corgi.server.core.ServiceEvents;
import com.github.registry.corgi.server.core.ZookeeperConnectionHandler;
import com.github.registry.corgi.server.core.commands.CorgiCommandHandler;
import com.github.registry.corgi.utils.CorgiProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 处理最终入站事件的ChannelHandler,负责调用下游具体的命令执行
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-19 16:38
 */
public class CokeyHandler extends ChannelInboundHandlerAdapter {
    private ZookeeperConnectionHandler connectionHandler;
    private ExecutorService executor;
    private Map<String, ServiceEvents> nodes;
    /**
     * 记录一个Channel上注册过的所有节点，断开连接时全部都需要取消注册
     */
    private List<String> registerPaths = new Vector<>(Constants.INITIAL_CAPACITY);
    /**
     * 记录一个Channel上订阅过的所有节点
     */
    private List<String> subscribePaths = new Vector<>(Constants.INITIAL_CAPACITY);
    private Logger log = LoggerFactory.getLogger("");

    public CokeyHandler(ZookeeperConnectionHandler connectionHandler, ExecutorService executor,
                        Map<String, ServiceEvents> nodes) {
        this.connectionHandler = connectionHandler;
        this.executor = executor;
        this.nodes = nodes;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CorgiProtocol protocol = (CorgiProtocol) msg;
        //为了避免I/O线程阻塞，客户端请求会派发给具体的业务线程池处理，I/O线程仅仅只需负责连接的接收/断开、入站/出站事件等
        CompletableFuture<CorgiProtocol> future = CompletableFuture.supplyAsync(() -> {
            return new CorgiCommandHandler(protocol, connectionHandler, nodes, registerPaths, subscribePaths).execute();
        }, executor);
        future.exceptionally(x -> {
            log.error("error", x);
            return null;
        }).thenAccept(y -> {
            if (null != y) {
                ctx.channel().writeAndFlush(y).addListener(x -> {
                    if (!x.isSuccess()) {
                        x.cause();
                    }
                });
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("", cause);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel{}成功注册到EventLoop上", ctx.channel());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel{}未注册到EventLoop上", ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel{}处于活跃状态", ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel{}处于非活跃状态", ctx.channel());
        executor.execute(() -> {
            registerPaths.parallelStream().filter(x -> !StringUtils.isEmpty(x)).forEach(x -> {
                try {
                    connectionHandler.deleteChildren(x);//客户端断开连接时，删除所有的临时节点,先暂时简单处理
                } catch (Throwable e) {
                    //...
                }
            });
        });
        if (!subscribePaths.isEmpty()) {
            subscribePaths.clear();
        }
    }
}
