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
import org.apache.commons.lang3.StringUtils;

import javax.xml.soap.Node;
import java.util.Map;

/**
 * Corgi命令模板类
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 22:25
 */
public class CorgiCommandsTemplate implements CorgiCommands {
    private Map<String, RegisterCallBack> registerPaths = CorgiFramework.getRegisterPaths();
    private CorgiConnectionHandler connectionHandler = CorgiFramework.getCorgiConnectionHandler();
    /**
     * 所使用的序列化类型
     */
    private CorgiFramework.SerializationType serialization;
    private final int redirections;
    private boolean isBatch;
    private int pullSize;
    private int pullTimeOut;

    protected CorgiCommandsTemplate(CorgiFramework.SerializationType serialization, int redirections) {
        this(serialization, redirections, false, 0, 0);
    }

    protected CorgiCommandsTemplate(CorgiFramework.SerializationType serialization, int redirections,
                                    boolean isBatch, int pullSize, int pullTimeOut) {
        this.serialization = serialization;
        this.redirections = redirections;
        this.isBatch = isBatch;
        this.pullSize = pullSize;
        this.pullTimeOut = pullTimeOut;
    }

    private <T> T run(CheckCallBack<T> callBack, String... params) {//TODO 这里的设计稍微复杂了点，后续调整
        check(params);//非空校验
        return callBack.execute();
    }

    /**
     * 模板方法,主要处理获取连接、序列化/反序列化相关等相关任务
     *
     * @return
     */
    private TransferBo.Content runWithRetries(int redirections, TransferBo transferBo, CorgiProtocol protocol, CommandException e) {
        if (redirections <= 0) {
            throw new CorgiMaxRedirectionsException("Too many redirections!!!", e);
        }
        if (null == connectionHandler || !connectionHandler.isActive()) {
            throw new CommandException("The connection is unavailable!!!");
        }
        TransferBo.Content result = null;
        try {
            final byte FLAG = protocol.getFlag();
            byte[] content = CorgiSerializationUtil.serialize(FLAG, transferBo);//序列化
            protocol.setContent(content);
            protocol.setLength(content.length);
            CorgiFramework.getThreadMap().put(protocol.getMsgId(), this);
            connectionHandler.sendCommand(protocol, this);
            //对结果进行反序列化
            TransferBo temp = CorgiSerializationUtil.deserialize(FLAG, connectionHandler.getResult(protocol.getMsgId()).getContent());
            if (null != temp) {
                result = temp.getContent();
            }
        } catch (Throwable e1) {
            runWithRetries(--redirections, transferBo, protocol, new CommandException("Command execution failed!!!", e1));
        }
        return result;
    }

    @Override
    public String register(String persistentNode, String ephemeralNode) {
        TransferBo transferBo = new TransferBo();
        transferBo.setPersistentNode(persistentNode);
        transferBo.setEphemeralNode(ephemeralNode);
        return run(() -> {
            final String RESULT = runWithRetries(redirections, transferBo,
                    assemblyProtocol(getFlag(), CorgiProtocol.createMsgId(), Constants.REGISTER_TYPE), null).getResult();
            //判断命令是否执行成功,命令执行成功才执行后续的断线回调
            if (RESULT.equals(Constants.REQUEST_RESULT)) {
                final String PATH = String.format("%s/%s", persistentNode, ephemeralNode);
                if (!registerPaths.containsKey(PATH)) {
                    registerPaths.put(PATH, () -> {
                        this.connectionHandler = CorgiFramework.getCorgiConnectionHandler();
                        register(persistentNode, ephemeralNode);//断线重连时，回调重新注册
                    });
                }
            }
            return RESULT;
        }, persistentNode, ephemeralNode);
    }

    @Override
    public String unRegister(String persistentNode, String ephemeralNode) {
        TransferBo transferBo = new TransferBo();
        transferBo.setPersistentNode(persistentNode);
        transferBo.setEphemeralNode(ephemeralNode);
        return run(() -> runWithRetries(redirections, transferBo,
                assemblyProtocol(getFlag(), CorgiProtocol.createMsgId(), Constants.UNREGISTER_TYPE), null).getResult(),
                persistentNode, ephemeralNode);
    }

    @Override
    public NodeBo subscribe(String persistentNode) {
        TransferBo transferBo = new TransferBo();
        transferBo.setPersistentNode(persistentNode);
        transferBo.setBatch(isBatch);
        transferBo.setPullSize(pullSize);
        transferBo.setPullTimeOut(pullTimeOut);
        Map<String, Integer> indexMap = CorgiFramework.getIndexMap();
        int index = 0;
        if (!indexMap.containsKey(persistentNode)) {
            indexMap.put(persistentNode, index);
        } else {
            index = indexMap.get(persistentNode);
        }
        transferBo.setIndex(index);
        int finalIndex = index;
        return run(() -> {
            TransferBo.Content content = runWithRetries(redirections, transferBo,
                    assemblyProtocol(getFlag(), CorgiProtocol.createMsgId(), Constants.SUBSCRIBE_TYPE), null);
            //判断命令是否执行成功,只有执行成功才返回结果集
            if (content.getResult().equals(Constants.REQUEST_RESULT)) {
                final int initIndex = content.getInitIndex();
                if (initIndex > 0) {
                    indexMap.put(persistentNode, initIndex);//第一次全量返回时，初始客户端位点
                } else {
                    int size = 0;
                    if (null != content.getPlusNodes()) {
                        size += content.getPlusNodes().length;
                    }
                    if (null != content.getReducesNodes()) {
                        size += content.getReducesNodes().length;
                    }
                    indexMap.put(persistentNode, finalIndex + size);
                }
                return new NodeBo(content.getPlusNodes(), content.getReducesNodes());
            }
            return null;
        }, persistentNode);
    }

    private void check(String... params) {
        String[] paths = params.clone();
        for (String path : paths) {
            if (StringUtils.isEmpty(path)) {
                throw new CommandException("No way to dispatch this command to corgi-server");
            }
        }
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
