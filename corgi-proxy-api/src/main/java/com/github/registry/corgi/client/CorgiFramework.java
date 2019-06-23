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

import com.github.registry.corgi.utils.CorgiProtocol;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提供给业务使用的Corgi客户端API
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 23:45
 */
public class CorgiFramework implements CorgiCommands {
    /**
     * 每个全局唯一的msgid对应一个业务线程
     */
    private volatile static Map<Long, Object> threadMap = new ConcurrentHashMap<>(Constants.INITIAL_CAPACITY);
    /**
     * 存储请求结果的全局集合，每一个结果与全局唯一的msgid对应
     */
    private volatile static Map<Long, CorgiProtocol> resultMap = new ConcurrentHashMap<>(Constants.INITIAL_CAPACITY);
    /**
     * 保存所有的注册项，断线重连后重新注册
     */
    private volatile static Map<String, RegisterCallBack> registerPaths = new ConcurrentHashMap<>(Constants.INITIAL_CAPACITY);
    private volatile static CorgiConnectionHandler connectionHandler;
    private volatile static List<HostAndPort> hostAndPorts = new Vector<>(Constants.INITIAL_CAPACITY);
    /**
     * 系列化类型，缺省为FST
     */
    private SerializationType serialization;
    /**
     * 重试次数,缺省2次
     */
    private int redirections;
    /**
     * 每次批量拉取的数量,缺省为1,不开启批量此参数无意义
     */
    private int pullSize;
    /**
     * 拉取超时时间,缺省为10000ms,不开启批量此参数无意义
     */
    private int pullTimeOut;
    /**
     * 批量拉取开关,缺省关闭
     */
    private boolean isBatch;

    private CorgiFramework(Builder builder) {
        hostAndPorts.addAll(builder.hostAndPorts);
        this.serialization = builder.serialization;
        this.redirections = builder.redirections;
        this.pullSize = builder.pullSize;
        this.pullTimeOut = builder.pullTimeOut;
        this.isBatch = builder.isBatch;
    }

    public static class Builder {
        private int pullSize = Constants.DEFAULT_PULL_SIZE;
        private int pullTimeOut = Constants.DEFAULT_PULL_TIMEOUT;
        private int redirections = Constants.REDIRECTIONS;
        private List<HostAndPort> hostAndPorts;
        private boolean isBatch;
        private SerializationType serialization = SerializationType.FST;

        public Builder(HostAndPort hostAndPort) {
            this.hostAndPorts = Arrays.asList(hostAndPort);
        }

        public Builder(List<HostAndPort> hostAndPorts) {
            this.hostAndPorts = hostAndPorts;
        }

        public Builder serialization(SerializationType serialization) {
            this.serialization = serialization;
            return this;
        }

        public Builder redirections(int redirections) {
            this.redirections = redirections;
            return this;
        }

        public Builder pullSize(int pullSize) {
            this.pullSize = pullSize;
            return this;
        }

        public Builder isBatch(boolean isBatch) {
            this.isBatch = isBatch;
            return this;
        }

        public Builder pullTimeOut(int pullTimeOut) {
            this.pullTimeOut = pullTimeOut;
            return this;
        }

        public CorgiFramework builder() {
            return new CorgiFramework(this);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "pullSize=" + pullSize +
                    ", pullTimeOut=" + pullTimeOut +
                    ", redirections=" + redirections +
                    ", hostAndPorts=" + hostAndPorts +
                    ", isBatch=" + isBatch +
                    ", serialization=" + serialization +
                    '}';
        }
    }

    /**
     * 相关初始化操作
     *
     * @return
     */
    public CorgiFramework init() {
        connectionHandler = new CorgiConnectionHandler(hostAndPorts).init();
        return this;
    }

    @Override
    public String register(String persistentNode, String ephemeralNode) {
        return new CorgiCommandsTemplate(serialization, redirections).register(persistentNode, ephemeralNode);
    }

    @Override
    public String unRegister(String persistentNode, String ephemeralNode) {
        return new CorgiCommandsTemplate(serialization, redirections).unRegister(persistentNode, ephemeralNode);
    }

    @Override
    public NodeBo subscribe(String persistentNode) {
        CorgiCommandsTemplate template = isBatch ? new CorgiCommandsTemplate(serialization, redirections, isBatch, pullSize, pullTimeOut)
                : new CorgiCommandsTemplate(serialization, redirections);
        return template.subscribe(persistentNode);
    }

    public static enum SerializationType {
        FST("fst"), FASTJSON("fastjson"), KRYO("kryo");
        public String serialization;

        SerializationType(String serialization) {
            this.serialization = serialization;
        }

    }

    public boolean isActive() {
        return connectionHandler.isActive();
    }

    public void close() {
        connectionHandler.close();//资源释放，断开与corgi-server的会话连接
    }

    protected static Map<Long, Object> getThreadMap() {
        return threadMap;
    }

    protected static Map<Long, CorgiProtocol> getResultMap() {
        return resultMap;
    }

    public static CorgiConnectionHandler getCorgiConnectionHandler() {
        return connectionHandler;
    }

    public static void setCorgiConnectionHandler(CorgiConnectionHandler connectionHandler) {
        CorgiFramework.connectionHandler = connectionHandler;
    }

    protected static Map<String, RegisterCallBack> getRegisterPaths() {
        return registerPaths;
    }

    public static List<HostAndPort> getHostAndPorts() {
        return hostAndPorts;
    }
}
