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

import com.github.registry.corgi.server.Constants;
import com.github.registry.corgi.server.core.ZookeeperConnectionHandler;
import com.github.registry.corgi.server.exceptions.CommandException;
import com.github.registry.corgi.utils.CorgiProtocol;
import com.github.registry.corgi.utils.TransferBo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 订阅命令处理类，用户Consumer订阅寻址使用
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-19 17:32
 */
public class SubscribeCommand extends CorgiCommandTemplate {
    private Map<String, Vector<String>> nodes;
    private List<String> subscribePaths;
    private Logger log = LoggerFactory.getLogger("");

    protected SubscribeCommand(CorgiProtocol protocol, ZookeeperConnectionHandler connectionHandler) {
        super(protocol, connectionHandler);
    }

    protected SubscribeCommand(CorgiProtocol protocol, ZookeeperConnectionHandler connectionHandler,
                               Map<String, Vector<String>> nodes, List<String> subscribePaths) {
        this(protocol, connectionHandler);
        this.nodes = nodes;
        this.subscribePaths = subscribePaths;

    }

    @Override
    public TransferBo.Content run(TransferBo transferBo)
            throws CommandException {
        ZookeeperConnectionHandler connectionHandler = super.getConnectionHandler();
        TransferBo.Content content = new TransferBo.Content();
        final String PATH = transferBo.getPersistentNode();
        int index = transferBo.getIndex();//corgi-client位点
        int pullSize = transferBo.getPullSize();
        Vector<String> node = nodes.get(PATH);
        ReentrantLock lock = lockMap.get(PATH);
        Condition condition = conditionMap.get(PATH);
        try {
            synchronized (nodes) {
                if (null == node) {
                    if (null == lock) {
                        lock = new ReentrantLock();
                        lockMap.put(PATH, lock);
                    }
                    if (null == condition) {
                        condition = lock.newCondition();
                        conditionMap.put(PATH, condition);
                    }
                    node = new Vector<>(Constants.CAPACITY);
                    nodes.put(PATH, node);
                    Vector<String> finalNode = node;
                    Condition finalCondition = condition;
                    ReentrantLock finalLock = lock;
                    //watch,检测到事件流后触发后回调
                    connectionHandler.watch(PATH, event -> {
                        finalLock.lockInterruptibly();
                        try {
                            finalNode.add(event);//记录变化的上/下线事件流
                            finalCondition.signalAll();
                            log.debug("event:{}", event);
                        } finally {
                            finalLock.unlock();
                        }
                    });
                }
            }
            if (!subscribePaths.contains(PATH)) {
                subscribePaths.add(PATH);
                List<String> result = null;
                do {
                    result = connectionHandler.getChildrensSnapshot(PATH); //如果是第一次订阅，则返回本地快照的全量数据
                } while (result.isEmpty());
                content.setPlusNodes(result.toArray(new String[result.size()]));
                content.setInitIndex(node.size());//返回给客户端的初始位点,避免重复拉取
            } else {
                //如果开启了批量拉取，超过超时时间则返回，不会一直阻塞
                if (transferBo.isBatch()) {
                    List<String> plusNodesList = null;
                    List<String> reducesNodesList = null;
                    int nodeSize = 0;
                    long nanos = TimeUnit.MILLISECONDS.toNanos(transferBo.getPullTimeOut());
                    lock.lockInterruptibly();
                    try {
                        while ((nodeSize = node.size()) < (index + pullSize)) {//判断是否能够拉取到足够的数据,如果够拉取则不阻塞
                            if (nanos <= 0) {
                                break;
                            }
                            nanos = condition.awaitNanos(nanos);
                        }
                    } finally {
                        lock.unlock();
                    }
                    //获取客户端真正的拉取数量
                    pullSize = (nodeSize - index) <= 0 ? 0 : (nodeSize - index) >= pullSize ?
                            pullSize : nodeSize - index;
                    for (int i = 0; i < pullSize; i++) {
                        final String temp = node.get(index++);
                        if (StringUtils.isEmpty(temp)) {
                            continue;
                        }
                        if (temp.startsWith(Constants.PLUS_EVENT)) {
                            if (null == plusNodesList) {
                                plusNodesList = new Vector<>(Constants.INITIAL_CAPACITY);
                            }
                            plusNodesList.add(temp.substring(Constants.BEGIN_INDEX));
                        } else if (temp.startsWith(Constants.REDUCES_EVENT)) {
                            if (null == reducesNodesList) {
                                reducesNodesList = new Vector<>(Constants.INITIAL_CAPACITY);
                            }
                            reducesNodesList.add(temp.substring(Constants.BEGIN_INDEX));
                        }
                    }
                    addNodes(content, null != plusNodesList ? plusNodesList.toArray(new String[plusNodesList.size()]) : null,
                            null != reducesNodesList ? reducesNodesList.toArray(new String[reducesNodesList.size()]) : null);
                } else {
                    lock.lockInterruptibly();
                    try {
                        while (node.size() - index <= 0) {
                            condition.await();
                        }
                        final String temp = node.get(index);
                        final String[] nodes = new String[]{temp.substring(Constants.BEGIN_INDEX)};
                        if (temp.startsWith(Constants.PLUS_EVENT)) {
                            addNodes(content, nodes, null);
                        } else if (temp.startsWith(Constants.REDUCES_EVENT)) {
                            addNodes(content, null, nodes);
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
        } catch (Throwable e) {
            throw new CommandException("Subscribe Command execution failed!!!", e);
        }
        return content;
    }

    /**
     * 添加上/下线事件流
     *
     * @param content
     * @param plusNodes
     * @param reducesNodes
     */
    private void addNodes(TransferBo.Content content, String[] plusNodes, String[] reducesNodes) {
        content.setPlusNodes(plusNodes);
        content.setReducesNodes(reducesNodes);
    }
}
