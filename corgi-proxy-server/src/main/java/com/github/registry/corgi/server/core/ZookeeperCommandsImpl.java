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
import com.github.registry.corgi.server.exceptions.CommandException;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Zookeeper相关命令接口实现
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-19 17:59
 */
public class ZookeeperCommandsImpl implements ZookeeperCommands {
    private CuratorFramework framework;
    private Logger log = LoggerFactory.getLogger("");

    protected ZookeeperCommandsImpl(CuratorFramework framework) {
        this.framework = framework;
    }

    @Override
    public String createPersistentNode(String rootPath) throws Exception {
        String result = null;
        isEmpty(rootPath);
        if (isExists(rootPath)) {
            return result;
        }
        try {
            result = framework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(rootPath);
        } catch (Throwable e) {
            log.debug(String.format("The directory {} already exists", rootPath));
        }
        return result;
    }

    @Override
    public String createEphemeralNode(String rootPath, String path) throws Exception {
        isEmpty(rootPath, path);
        createPersistentNode(rootPath);//如果根目录不存在，则首先创建根目录
        final String PATH = String.format("%s/%s", rootPath, path);
        deleteChildren(PATH);//如果目标临时节点已经存在，则先删除后再创建
        return framework.create().withMode(CreateMode.EPHEMERAL).forPath(PATH);
    }

    @Override
    public void deleteChildren(String path) throws Exception {
        isEmpty(path);
        if (!isExists(path)) {
            return;
        }
        framework.delete().deletingChildrenIfNeeded().forPath(path);
    }

    @Override
    public void deleteChildren(String rootPath, String path) throws Exception {
        deleteChildren(String.format("%s/%s", rootPath, path));
    }

    @Override
    public List<String> getChildrens(String rootPath) throws Exception {
        isEmpty(rootPath);
        return framework.getChildren().forPath(rootPath);
    }

    @Override
    public List<String> getChildrensSnapshot(String rootPath) throws Exception {
        isEmpty(rootPath);
        List<String> result = new Vector<>();
        Map<String, ChildData> childDataMap = getTreeCache(rootPath).getCurrentChildren(rootPath);
        if (null != childDataMap) {
            childDataMap.values().parallelStream().
                    filter(x -> null != x).forEach(x -> {
                result.add(x.getPath().split(String.format("%s/", rootPath))[1]);
            });
        }
        return result;
    }

    /**
     * 获取TreeCache，每个rootPath都会有一个TreeCache
     *
     * @param rootPath
     * @return
     * @throws Exception
     */
    private synchronized TreeCache getTreeCache(String rootPath) throws Exception {
        TreeCache cache = treeCacheMap.get(rootPath);
        if (null == cache) {
            cache = new TreeCache(framework, rootPath);
            cache.start();
            treeCacheMap.put(rootPath, cache);//每一个根节点会对应一个TreeCache
            log.debug("Create treeCache by {}", rootPath);
        }
        return cache;
    }

    @Override
    public void watch(String rootPath, WatchCallBack callBack, String channelId) throws Exception {
        TreeCacheListener listener = (client, event) -> {
            String path = null;
            try {
                path = event.getData().getPath();
            } catch (Throwable e) {
                //不同的事件，有可能取得是null值
            }
            switch (event.getType()) {
                case NODE_ADDED:
                    if (!rootPath.equalsIgnoreCase(path)) {
                        log.debug("ThreadName:{}, NODE_ADDED:{}", Thread.currentThread().getName(), path);
                        callBack.execute(String.format("+%s", path.split(String.format("%s/", rootPath))[1]));
                    }
                    break;
                case NODE_REMOVED:
                    if (!rootPath.equalsIgnoreCase(path)) {
                        log.debug("ThreadName:{},NODE_REMOVED:{}", Thread.currentThread().getName(), path);
                        callBack.execute(String.format("-%s", path.split(String.format("%s/", rootPath))[1]));
                    }
                    break;
                case NODE_UPDATED:
                    break;
                case CONNECTION_LOST:
                    break;
                case CONNECTION_SUSPENDED:
                    break;
                case CONNECTION_RECONNECTED:
                    break;
                case INITIALIZED:
                    break;
            }
        };
        log.debug("listener: {}", listener);
        TreeCache cache = getTreeCache(rootPath);
        //每个Channel在相同的TreeCache上都有一个独立的监听器
        cache.getListenable().addListener(listener, executorService);
        Map<TreeCache, Vector<TreeCacheListener>> listenerMap = listenerMaps.get(channelId);
        if (null == listenerMap) {
            listenerMap = new ConcurrentHashMap<>(Constants.INITIAL_CAPACITY);
            listenerMaps.put(channelId, listenerMap);
        }
        Vector<TreeCacheListener> listeners = listenerMap.get(cache);
        if (null == listeners) {
            listeners = new Vector<>(Constants.INITIAL_CAPACITY);
            listenerMap.put(cache, listeners);
        }
        listeners.add(listener);
    }

    @Override
    public void unWatch(String channelId) {
        Map<TreeCache, Vector<TreeCacheListener>> map = listenerMaps.get(channelId);
        if (null != map && !map.isEmpty()) {
            map.forEach((treeCache, listeners) -> {
                //从TreeCache中删除目标Channel绑定的TreeCacheListeners，取消监听
                listeners.parallelStream().forEach(listener -> treeCache.getListenable().removeListener(listener));
            });
            listenerMaps.remove(channelId);
        }
    }

    private boolean isExists(String path) throws Exception {
        return null != framework.checkExists().forPath(path);
    }

    private void isEmpty(String... paths) throws CommandException {
        for (String path : paths.clone()) {
            if (StringUtils.isEmpty(path)) {
                throw new CommandException("Unable to execute this command!!!");
            }
        }
    }
}
