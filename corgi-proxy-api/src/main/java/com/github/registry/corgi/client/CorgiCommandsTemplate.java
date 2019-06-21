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
package com.github.registry.corgi.client;

import com.github.registry.corgi.client.exceptions.CommandException;
import com.github.registry.corgi.client.exceptions.CorgiMaxRedirectionsException;
import com.github.registry.corgi.utils.CorgiProtocol;
import com.github.registry.corgi.utils.CorgiSerializationUtil;
import com.github.registry.corgi.utils.TransferBo;

import java.util.Map;

/**
 * Corgi命令模板类
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 22:25
 */
public class CorgiCommandsTemplate implements CorgiCommands {
    private Map<String, CorgiClientCallBack> registerPaths = CorgiFramework.getRegisterPaths();
    private CorgiConnectionHandler connectionHandler = CorgiFramework.getCorgiConnectionHandler();
    /**
     * 所使用的序列化类型
     */
    private CorgiFramework.SerializationType serialization;
    private final int redirections;

    protected CorgiCommandsTemplate(CorgiFramework.SerializationType serialization, int redirections) {
        this.serialization = serialization;
        this.redirections = redirections;
    }

    /**
     * 模板方法,主要处理获取连接、序列化/反序列化相关等相关任务
     *
     * @return
     */
    private TransferBo execute(int redirections, TransferBo transferBo, CorgiProtocol protocol, CommandException e) {
        if (redirections <= 0) {
            throw new CorgiMaxRedirectionsException("Too many redirections!!!", e);
        }
        if (!connectionHandler.isActive()) {
            throw new CommandException("The connection is unavailable!!!");
        }
        TransferBo result = null;
        try {
            final byte FLAG = protocol.getFlag();
            byte[] content = CorgiSerializationUtil.serialize(FLAG, transferBo);//序列化
            protocol.setContent(content);
            protocol.setLength(content.length);
            CorgiFramework.getThreadMap().put(protocol.getMsgId(), this);
            connectionHandler.sendCommand(protocol, this);
            //对结果进行反序列化
            result = CorgiSerializationUtil.deserialize(FLAG, connectionHandler.getResult(protocol.getMsgId()).getContent());
        } catch (Throwable e1) {
            execute(--redirections, transferBo, protocol, new CommandException("Command execution failed!!!", e1));
        }
        return result;
    }

    @Override
    public String register(String persistentNode, String ephemeralNode) {
        TransferBo transferBo = new TransferBo.Builder(persistentNode).ephemeralNode(ephemeralNode).builder();
        final String result = execute(redirections, transferBo,
                assemblyProtocol(getFlag(), CorgiProtocol.createMsgId(), Constants.REGISTER_TYPE), null).getContent().getResult();
        if (result.equals(Constants.REQUEST_RESULT)) {
            registerPaths.put(String.format("%s/%s", persistentNode, ephemeralNode), () -> {
                this.connectionHandler = CorgiFramework.getCorgiConnectionHandler();
                register(persistentNode, ephemeralNode);//断线重连时，回调重新注册
            });
        }
        return result;
    }

    @Override
    public String unRegister(String persistentNode, String ephemeralNode) {
        return execute(redirections, new TransferBo.Builder(persistentNode).ephemeralNode(ephemeralNode).builder(),
                assemblyProtocol(getFlag(), CorgiProtocol.createMsgId(), Constants.UNREGISTER_TYPE), null).getContent().getResult();
    }

    @Override
    public NodeBo subscribe(String persistentNodd) {
        TransferBo.Content content = execute(redirections, new TransferBo.Builder(persistentNodd).builder(),
                assemblyProtocol(getFlag(), CorgiProtocol.createMsgId(), Constants.SUBSCRIBE_TYPE), null).getContent();
        if (content.getResult().equals(Constants.REQUEST_RESULT)) {
            return new NodeBo(content.getPlusNodes(), content.getReducesNodes());
        }
        return null;
    }

    /**
     * 获取序列化类型获取标志位
     *
     * @return
     */
    private byte getFlag() {
        byte flag = -1;
        if (null != serialization) {
            //flag低位第1位为1表示使用Kryo二进制序列化协议，第2位为1表示使用Fastjson序列化协议，第3位或者第4位为1表示使用FST二进制系列化协议
            flag = (byte) (CorgiFramework.SerializationType.KRYO == serialization ? Constants.REQUEST_KRYO_FLAG :
                    (CorgiFramework.SerializationType.FASTJSON == serialization ?
                            Constants.REQUEST_FASTJSON_FLAG : Constants.REQUEST_FST_FLAG));
        }
        return flag;
    }

    /**
     * 协议组装
     *
     * @param flag
     * @param msgId
     * @param type
     * @return
     */
    private CorgiProtocol assemblyProtocol(byte flag, long msgId, byte type) {
        return new CorgiProtocol.Builder().flag(flag).msgId(msgId).type(type).builder();
    }
}