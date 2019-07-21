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
package com.github.registry.corgi.server.core;

import com.github.registry.corgi.server.Constants;
import com.github.registry.corgi.server.exceptions.StartingException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import java.util.List;

/**
 * Zookeeper连接实际处理类
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-19 15:48
 */
public class ZookeeperConnectionHandler extends ZookeeperConnection implements ZookeeperCommands {
    private ZookeeperCommandsImpl zookeeperCommands;

    public ZookeeperConnectionHandler(String host, int sessionTimeoutMs, int connectionTimeoutMs) {
        super(host, sessionTimeoutMs, connectionTimeoutMs);
    }

    /**
     * Zookeeper相关初始化操作
     *
     * @return
     * @throws StartingException
     */
    public ZookeeperConnectionHandler init() throws StartingException {
        conn();
        zookeeperCommands = new ZookeeperCommandsImpl(getFramework());
        return this;
    }

    @Override
    public String createPersistentNode(String rootPath) throws Exception {
        return zookeeperCommands.createPersistentNode(rootPath);
    }

    @Override
    public String createEphemeralNode(String rootPath, String path) throws Exception {
        return zookeeperCommands.createEphemeralNode(rootPath, path);
    }

    @Override
    public void deleteChildren(String path) throws Exception {
        zookeeperCommands.deleteChildren(path);
    }

    @Override
    public void deleteChildren(String rootPath, String path) throws Exception {
        zookeeperCommands.deleteChildren(rootPath, path);
    }

    @Override
    public List<String> getChildrens(String rootPath) throws Exception {
        return zookeeperCommands.getChildrens(rootPath);
    }

    @Override
    public List<String> getChildrensSnapshot(String rootPath) throws Exception {
        return zookeeperCommands.getChildrensSnapshot(rootPath);
    }

    @Override
    public void watch(String rootPath, WatchCallBack callBack) throws Exception {
        zookeeperCommands.watch(rootPath, callBack);
    }
}