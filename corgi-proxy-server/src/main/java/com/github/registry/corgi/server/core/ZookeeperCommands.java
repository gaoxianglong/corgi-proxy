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
import org.apache.curator.framework.recipes.cache.TreeCache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zookeeper相关命令接口
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-19 17:45
 */
public interface ZookeeperCommands {
    /**
     * 每个节点都会由一个全局TreeCache来保证一个时序正确、最终一致的事件流
     */
    Map<String, TreeCache> treeCacheMap = new ConcurrentHashMap<>(Constants.INITIAL_CAPACITY);

    /**
     * 创建持久节点
     *
     * @param rootPath
     * @return
     * @throws Exception
     */
    String createPersistentNode(String rootPath) throws Exception;

    /**
     * 创建瞬时节点
     *
     * @param rootPath
     * @param path
     * @return
     * @throws Exception
     */
    String createEphemeralNode(String rootPath, String path) throws Exception;

    /**
     * 删除瞬时节点
     *
     * @param path
     * @throws Exception
     */
    void deleteChildren(String path) throws Exception;

    void deleteChildren(String rootPath, String path) throws Exception;

    /**
     * 获取所有子节点
     *
     * @param rootPath
     * @return
     * @throws Exception
     */
    List<String> getChildrens(String rootPath) throws Exception;

    /**
     * 从TreeCache的快照中获取所有子节点
     *
     * @param rootPath
     * @return
     * @throws Exception
     */
    List<String> getChildrensSnapshot(String rootPath) throws Exception;

    /**
     * 监听子节点变化
     *
     * @param rootPath
     * @param callBack
     * @throws Exception
     */
    void watch(String rootPath, WatchCallBack callBack) throws Exception;
}
