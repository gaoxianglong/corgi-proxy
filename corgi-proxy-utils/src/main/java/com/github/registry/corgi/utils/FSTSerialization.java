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

import org.nustaq.serialization.FSTConfiguration;

/**
 * 基于FST的二进制序列化/反序列化工具类
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 21:08
 */
public class FSTSerialization implements Serialization {
    final static FSTConfiguration configuration = FSTConfiguration
            .createStructConfiguration();

    @Override
    public TransferBo deserialize(byte[] content) {
        TransferBo transferBo = null;
        if (null != content) {
            transferBo = (TransferBo) configuration.asObject(content);
        }
        return transferBo;
    }

    @Override
    public byte[] serialize(TransferBo transferBo) {
        byte[] content = null;
        if (null != transferBo) {
            content = configuration.asByteArray(transferBo);
        }
        return content;
    }
}