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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 基于Kyro的二进制序列化/反序列化工具类
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 21:12
 */
public class KryoSerialization implements Serialization {
    static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
//            kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(
//                    new StdInstantiatorStrategy()));
            kryo.register(TransferBo.class);
            return kryo;
        }
    };

    @Override
    public TransferBo deserialize(byte[] content) {
        TransferBo transferBo = null;
        if (null != content) {
            Kryo kryo = kryoThreadLocal.get();
            transferBo = kryo.readObject(new Input(content), TransferBo.class);
        }
        return transferBo;
    }

    @Override
    public byte[] serialize(TransferBo transferBo) {
        byte[] content = null;
        if (null != transferBo) {
            Kryo kryo = kryoThreadLocal.get();
            ByteArrayOutputStream out = null;
            Output output = null;
            try {
                out = new ByteArrayOutputStream();
                output = new Output(out);
                kryo.writeObject(output, transferBo);
                output.flush();
                content = out.toByteArray();
            } finally {
                if (null != output) {
                    output.close();
                }
                if (null != out) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return content;
    }
}