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
package com.github.registry.corgi.client.handlers;

import com.github.registry.corgi.client.CorgiClientCallBack;
import com.github.registry.corgi.client.CorgiConnectionHandler;
import com.github.registry.corgi.client.CorgiFramework;
import com.github.registry.corgi.client.RegisterCallBack;
import com.github.registry.corgi.client.exceptions.CorgiClientException;
import com.github.registry.corgi.utils.CorgiProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * corgi-client处理最终入站事件的ChannelHandler
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 22:56
 */
public class CorgiClientHandler extends ChannelInboundHandlerAdapter {
    private Map<Long, Object> threadMap;
    private Map<String, Integer> indexMap;
    private Map<Long, CorgiProtocol> resultMap;
    private Map<String, RegisterCallBack> registerPaths;
    private Logger log = LoggerFactory.getLogger(CorgiClientHandler.class);

    public CorgiClientHandler(Map<Long, Object> threadMap, Map<Long, CorgiProtocol> resultMap,
                              Map<String, RegisterCallBack> registerPaths, Map<String, Integer> indexMap) {
        this.threadMap = threadMap;
        this.resultMap = resultMap;
        this.registerPaths = registerPaths;
        this.indexMap = indexMap;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("The connection({}) has been disconnected", ctx.channel());
        indexMap.forEach((x, y) -> {
            indexMap.put(x, 0);//重置客户端位点
        });
        threadMap.values().parallelStream().forEach(x -> {
            synchronized (x) {
                x.notify();
            }
        });
        //断线重连
//        new Thread(()->{
//            CorgiFramework.setCorgiConnectionHandler(new CorgiConnectionHandler(CorgiFramework.getHostAndPorts()).init());
//            System.out.println(registerPaths.size());
//            registerPaths.forEach((key, callBack) -> {
//                callBack.execute(); //断线重连后，重新注册
//                log.info("Reregister directory {}", key);
//            });
//        }).start();
        ctx.channel().eventLoop().schedule(() -> {
            CorgiFramework.setCorgiConnectionHandler(new CorgiConnectionHandler(CorgiFramework.getHostAndPorts()).init());
            registerPaths.forEach((key, callBack) -> {
                callBack.execute(); //断线重连后，重新注册
                log.info("Reregister directory {}", key);
            });
        }, 2, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CorgiProtocol protocol = (CorgiProtocol) msg;
        final long MSG_ID = protocol.getMsgId();
        Object obj = threadMap.get(MSG_ID);
        if (null != obj) {
            threadMap.remove(MSG_ID);
            resultMap.put(MSG_ID, protocol);
            synchronized (obj) {
                obj.notify();//唤醒当前业务线程
            }
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel{}成功注册到EventLoop上", ctx.channel());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel{}未注册到EventLoop上", ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel{}处于活跃状态(客户端{}成功连接服务器)", ctx.channel(), ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        throw new CorgiClientException(cause);
    }
}