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
 * ated in 2019-06-19 17:32
 */
public class SubscribeCommand extends CorgiCommandTemplate {
    /**
     * 每一个Channel都会对应一个存储事件流的集合
     */
    private Map<String, Vector<String>> eventMap;
    private String channelId;
    private Logger log = LoggerFactory.getLogger("");

    protected SubscribeCommand(CorgiProtocol protocol, ZookeeperConnectionHandler connectionHandler) {
        super(protocol, connectionHandler);
    }

    protected SubscribeCommand(CorgiProtocol protocol, ZookeeperConnectionHandler connectionHandler,
                               Map<String, Vector<String>> eventMap, String channelId) {
        this(protocol, connectionHandler);
        this.eventMap = eventMap;
        this.channelId = channelId;
    }

    @Override
    public TransferBo.Content run(TransferBo transferBo)
            throws CommandException {
        ZookeeperConnectionHandler connectionHandler = super.getConnectionHandler();
        TransferBo.Content content = new TransferBo.Content();
        final String PATH = transferBo.getPersistentNode();//获取目标服务接口znode
        int pullSize = transferBo.getPullSize();
        Vector<String> events = eventMap.get(PATH);
        ReentrantLock lock = lockMap.get(channelId);
        Condition condition = conditionMap.get(channelId);
        boolean isContains = true;
        try {
            if (null == events) {
                try {
                    if (null == lock) {
                        lock = new ReentrantLock();
                        lockMap.put(channelId, lock);
                    }
                    if (null == condition) {
                        condition = lock.newCondition();
                        conditionMap.put(channelId, condition);
                    }
                    eventMap.put(PATH, events = new Vector<>(Constants.INITIAL_CAPACITY));
                    Condition finalCondition = condition;
                    ReentrantLock finalLock = lock;
                    //watch,检测到事件流后触发后回调
                    Vector<String> finalEvents = events;
                    connectionHandler.watch(PATH, event -> {
                        finalLock.lockInterruptibly();
                        try {
                            finalEvents.add(event);//记录服务的上/下线事件流
                            finalCondition.signal();
                            log.debug("event:{}", event);
                        } finally {
                            finalLock.unlock();
                        }
                    }, channelId);
                } finally {
                    isContains = false;
                }
            }
            if (!isContains) {
                if (null != events && !events.isEmpty()) {
                    events.clear();//全量数据返回之前首先执行去重操作，最大程度上避免重复拉取
                }
                List<String> result = null;
                do {
                    result = connectionHandler.getChildrensSnapshot(PATH); //如果是第一次订阅，则返回本地快照的全量数据
                } while (result.isEmpty());
                content.setPlusNodes(result.toArray(new String[result.size()]));
            } else {
                List<String> plusNodesList = null;
                List<String> reducesNodesList = null;
                long nanos = TimeUnit.MILLISECONDS.toNanos(transferBo.getPullTimeOut());
                lock.lockInterruptibly();
                try {
                    while (events.size() < pullSize) {//判断是否能够拉取到足够的数据,如果够拉取则不阻塞
                        if (nanos <= 0) {
                            pullSize = events.size();//如果拉取数量>事件数量，则拉取数量=事件数量
                            break;
                        }
                        nanos = condition.awaitNanos(nanos);
                    }
                } finally {
                    lock.unlock();
                }
                for (int i = 0; i < pullSize; i++) {
                    final String temp = events.get(i);
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
            }
        } catch (Throwable e) {
            throw new CommandException("Subscribe command execution failed!!!", e);
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
