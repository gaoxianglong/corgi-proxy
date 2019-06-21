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
package com.github.registry.corgi.utils;

import java.util.Arrays;

/**
 * cokey应用层传输协议
 * <p>
 * +----------------------------------------------------------------------------------------------+
 * |                                                                                              |
 * |                                        Cokey Protocol                                        |
 * |                                                                                              |
 * +-----------------------------------------------------------------------+----------------------+
 * |                                  Header                               |         Body         |
 * +---------------+-------------+-------------+-------------+-------------+----------------------+
 * |       4       |      4      |      1      |      8      |      1      |      Body Length     |
 * +---------------+-------------+-------------+-------------+-------------+----------------------+
 * |  Body Length  |    Magic    |     Flag    |    Msg Id   |     Type    |      Body Content    |
 * +---------------+-------------+-------------+-------------+-------------+----------------------+
 * <p>
 * Cokey应用层传输协议采用定长消息头(18byte)和变长消息体来进行数据传输,其中消息体缺省采用的是基于Kryo的二进制序列化协议
 * [Body Length]：消息体的长度，int类型，4byte
 * [Magic]：类似bytecode中的魔术，用于判断是否是Cokey协议的数据包，魔术常量为0xcafe，用于判断报文的开始，int类型，4byte
 * [Flag]：标志位，一共8个地址位，高4位的第1位表示ping事件,第2位表示request请求，第3位表示response请求，低4位表示消息体的序列化方式，byte类型，1byte
 * [Msg Id]：消息Id，每个请求的唯一标识Id，由于采用的NIO模型，用于对应request/response，long类型，8byte
 * [Type]：代表命令类型，byte类型，8byte
 * [Body Content]：序列化后的消息体
 * <p>
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 21:43
 */
public class CorgiProtocol {
    /**
     * 报文长度
     */
    private int length;
    /**
     * 魔术
     */
    private int magic;
    /**
     * 标志位
     */
    private byte flag;
    /**
     * 消息唯一id
     */
    private long msgId;
    /**
     * 命令类型
     */
    private byte type;
    /**
     * 报文体
     */
    private byte[] content;

    private CorgiProtocol(Builder builder) {
        this.length = builder.length;
        this.magic = builder.magic;
        this.flag = builder.flag;
        this.msgId = builder.msgId;
        this.type = builder.type;
        this.content = builder.content;
    }

    public static class Builder {
        private int length;
        private int magic = 0xcafe;
        private byte flag;
        private long msgId;
        private byte[] content;
        private byte type;

        public Builder() {
        }

        public Builder length(int length) {
            this.length = length;
            return this;
        }

        public Builder type(byte type) {
            this.type = type;
            return this;
        }

        public Builder content(byte[] content) {
            this.content = content;
            return this;
        }

        public Builder magic(int magic) {
            this.magic = magic;
            return this;
        }

        public Builder msgId(long msgId) {
            this.msgId = msgId;
            return this;
        }

        public Builder flag(byte flag) {
            this.flag = flag;
            return this;
        }

        public CorgiProtocol builder() {
            return new CorgiProtocol(this);
        }
    }

    public int getLength() {
        return length;
    }

    public int getMagic() {
        return magic;
    }

    public byte getFlag() {
        return flag;
    }

    public long getMsgId() {
        return msgId;
    }

    public byte getType() {
        return type;
    }

    public byte[] getContent() {
        return content;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public static long createMsgId() {
        return (long) (Math.random() * Long.MAX_VALUE);//理论上碰撞概率还是很低的
    }

    @Override
    public String toString() {
        return "CorgiProtocol{" +
                "length=" + length +
                ", magic=" + magic +
                ", flag=" + flag +
                ", msgId=" + msgId +
                ", type=" + type +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
