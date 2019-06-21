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

/**
 * 序列化/反序列化工具类
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 21:25
 */
public class CorgiSerializationUtil {
    /**
     * 序列化(Object to bytes)
     *
     * @param flag
     * @param cokeyBean
     * @return
     */
    public static byte[] serialize(byte flag, TransferBo cokeyBean) {
        Serialization serialization = context(flag);
        return serialization.serialize(cokeyBean);
    }

    /**
     * 反序列化(Bytes to object)
     *
     * @param flag
     * @param conent
     * @return
     */
    public static TransferBo deserialize(byte flag, byte[] conent) {
        Serialization serialization = context(flag);
        return serialization.deserialize(conent);
    }

    /**
     * 根据不同的策略选择不同的序列化/反序列化协议
     * <p>
     * flag低位第1位为1表示使用Kryo二进制序列化协议，第2位为1表示使用Fastjson序列化协议，第3位或第4位为1表示使用FST二进制系列化协议
     *
     * @param b
     * @return
     */
    private static Serialization context(byte b) {
        return 1 == ByteUtil.get(b, 0) ? new KryoSerialization() :
                (1 == ByteUtil.get(b, 1) ? new FastjsonSerialization() : new FSTSerialization());
    }
}