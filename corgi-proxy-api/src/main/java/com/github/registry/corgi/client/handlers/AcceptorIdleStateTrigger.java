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

import com.github.registry.corgi.client.Constants;
import com.github.registry.corgi.utils.CorgiProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 写空闲超时处理的ChannelHandler
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 22:51
 */
public class AcceptorIdleStateTrigger extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            //在单位时间内如果没有发生过任何的出站事件，则向服务端发送心跳
            if (state == IdleState.WRITER_IDLE) {
                CorgiProtocol protocol = new CorgiProtocol.Builder().flag(Constants.REQUEST_PING_FLAG).
                        content(new byte[0]).length(0).builder();
                ctx.writeAndFlush(protocol);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
