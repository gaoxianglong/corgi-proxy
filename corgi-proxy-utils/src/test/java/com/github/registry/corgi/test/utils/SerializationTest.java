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
package com.github.registry.corgi.test.utils;

import com.github.registry.corgi.utils.CorgiSerializationUtil;
import com.github.registry.corgi.utils.TransferBo;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-23 18:11
 */
public class SerializationTest {
    private Logger log = LoggerFactory.getLogger(SerializationTest.class);

    @Test
    public void test() {
        //flag低位第1位为1表示使用Kryo二进制序列化协议，第2位为1表示使用Fastjson序列化协议，第3位或第4位为1表示使用FST二进制系列化协议
        TransferBo transferBo = new TransferBo();
        transferBo.setPersistentNode("dubbo");
        transferBo.setBatch(true);
        transferBo.setPullSize(10);
        transferBo.setPullTimeOut(10);
        TransferBo.Content content = new TransferBo.Content();
        content.setResult("ok");
        transferBo.setContent(content);
        log.info("{}", CorgiSerializationUtil.deserialize((byte) 0b00000001, CorgiSerializationUtil.serialize((byte) 0b00000001, transferBo)).toString());
        log.info("{}", CorgiSerializationUtil.deserialize((byte) 0b00000010, CorgiSerializationUtil.serialize((byte) 0b00000010, transferBo)).toString());
        log.info("{}", CorgiSerializationUtil.deserialize((byte) 0b00000100, CorgiSerializationUtil.serialize((byte) 0b00000100, transferBo)).toString());
    }
}
