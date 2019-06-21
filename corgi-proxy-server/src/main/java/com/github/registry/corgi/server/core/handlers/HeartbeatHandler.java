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

import com.github.registry.corgi.utils.ByteUtil;
import com.github.registry.corgi.utils.CorgiProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理心跳事件的ChannelHandler,如果是心跳事件则不触发ChannelHandler链的下一个
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-19 16:34
 */
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    private Logger log = LoggerFactory.getLogger("");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CorgiProtocol protocol = (CorgiProtocol) msg;
        if (0xcafe == protocol.getMagic()) {//只接受以固定magic开头的报文
            final byte FLAG = protocol.getFlag();
            if (1 != ByteUtil.get(FLAG, 4)) {//如果高4位的第1位为1则表示为一个ping事件
                if (1 == ByteUtil.get(FLAG, 5)) {//高4位的第2位表示request请求
                    ctx.fireChannelRead(protocol);
                }
            } else {
                log.debug("Channel{} heartbeat...", ctx.channel());
            }
        }
    }
}