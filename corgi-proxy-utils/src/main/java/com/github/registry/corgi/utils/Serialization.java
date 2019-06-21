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
 * 序列化/反序列化策略接口，支持FST、JSON、KRYO等三种序列化方式
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 14:08
 */
public interface Serialization {
    /**
     * 反序列化(Bytes to object)
     *
     * @param content
     * @return
     */
    TransferBo deserialize(byte[] content);

    /**
     * 序列化(Object to bytes)
     *
     * @param transferBo
     * @return
     */
    byte[] serialize(TransferBo transferBo);
}
