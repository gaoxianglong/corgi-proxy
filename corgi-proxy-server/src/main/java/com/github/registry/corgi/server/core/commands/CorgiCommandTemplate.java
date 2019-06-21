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
package com.github.registry.corgi.server.core.commands;

import com.github.registry.corgi.server.core.ZookeeperConnectionHandler;
import com.github.registry.corgi.server.exceptions.CommandException;
import com.github.registry.corgi.server.exceptions.CorgiException;
import com.github.registry.corgi.utils.ByteUtil;
import com.github.registry.corgi.utils.CorgiProtocol;
import com.github.registry.corgi.utils.CorgiSerializationUtil;
import com.github.registry.corgi.utils.TransferBo;

/**
 * Corgi命令模板类
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-19 17:27
 */
public abstract class CorgiCommandTemplate implements CorgiCommandStrategy<TransferBo.Content> {
    private CorgiProtocol protocol;
    private ZookeeperConnectionHandler connectionHandler;

    protected CorgiCommandTemplate(CorgiProtocol protocol, ZookeeperConnectionHandler connectionHandler) {
        this.protocol = protocol;
        this.connectionHandler = connectionHandler;
    }

    /**
     * 模板方法,主要处理获取连接、序列化/反序列化相关等相关任务
     *
     * @return
     */
    protected CorgiProtocol execute() {
        byte flag = protocol.getFlag();
        TransferBo.Content tContent = null;
        TransferBo transferBo = CorgiSerializationUtil.deserialize(flag, protocol.getContent());//反序列化
        try {
            transferBo.setContent(run(transferBo));//执行具体命令
        } catch (CorgiException e) {
            tContent = new TransferBo.Content();
            tContent.setResult(String.format("fail:%s", e.getMessage()));//执行失败返回异常信息
            transferBo.setContent(tContent);
        } finally {
            byte[] content = CorgiSerializationUtil.serialize(flag, transferBo);//对结果进行序列化
            flag = getFlag(flag);
            protocol = new CorgiProtocol.Builder().flag(flag).content(content).length(content.length).
                    msgId(protocol.getMsgId()).type(protocol.getType()).builder();
        }
        return protocol;
    }

    protected ZookeeperConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    /**
     * 重新设置flag响应给客户端
     *
     * @param flag
     * @return
     */
    private byte getFlag(byte flag) {
        //flag高4位第3位表示response请求，即0100
        return ByteUtil.bitStringToByte(String.format("0100%s", ByteUtil.byteToBitString(flag).substring(4)));
    }
}
