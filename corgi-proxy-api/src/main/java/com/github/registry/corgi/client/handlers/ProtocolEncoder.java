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

import com.github.registry.corgi.client.exceptions.ProtocolException;
import com.github.registry.corgi.utils.CorgiProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Corgi应用层协议编码器
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 22:40
 */
public class ProtocolEncoder extends MessageToByteEncoder<CorgiProtocol> {
    @Override
    protected void encode(ChannelHandlerContext ctx, CorgiProtocol protocol, ByteBuf out) throws Exception {
        if (null == protocol) {
            throw new ProtocolException("Encoder fail!!!");
        }
        out.writeInt(protocol.getLength());
        out.writeInt(protocol.getMagic());
        out.writeByte(protocol.getFlag());
        out.writeLong(protocol.getMsgId());
        out.writeByte(protocol.getType());
        out.writeBytes(protocol.getContent());
    }
}