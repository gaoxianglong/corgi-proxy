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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 订阅命令处理类，用户Consumer订阅寻址使用
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-19 17:32
 */
public class SubscribeCommand extends CorgiCommandTemplate {
    private Map<String, LinkedBlockingQueue<String>> queues;
    private Logger log = LoggerFactory.getLogger("");

    protected SubscribeCommand(CorgiProtocol protocol, ZookeeperConnectionHandler connectionHandler) {
        super(protocol, connectionHandler);
    }

    protected SubscribeCommand(CorgiProtocol protocol, ZookeeperConnectionHandler connectionHandler,
                               Map<String, LinkedBlockingQueue<String>> queues) {
        this(protocol, connectionHandler);
        this.queues = queues;

    }

    @Override
    public TransferBo.Content run(TransferBo transferBo)
            throws CommandException {
        ZookeeperConnectionHandler connectionHandler = super.getConnectionHandler();
        TransferBo.Content content = new TransferBo.Content();
        final String PATH = transferBo.getPersistentNode();
        LinkedBlockingQueue<String> queue = queues.get(PATH);
        try {
            if (null == queue) {
                queue = new LinkedBlockingQueue(Constants.CAPACITY);
                queues.put(PATH, queue);
//                connectionHandler.watch(PATH, new CorgiCallBack() {
//                    @Override
//                    public void execute(String path) {
//                        try {
//                            queue.put(path);
//                        } catch (InterruptedException e) {
//                            log.error("Queue write failure!!!", e);
//                        }
//                    }
//                });
                LinkedBlockingQueue<String> finalQueue = queue;
                connectionHandler.watch(PATH, msg -> {
                    try {
                        finalQueue.put(msg);
                    } catch (InterruptedException e) {
                        log.error("Queue write failure!!!", e);
                    }
                });
                List<String> result = null;
                do {
                    result = connectionHandler.getChildrensSnapshot(PATH); //如果是第一次订阅，则返回本地快照的全量数据
                } while (result.isEmpty());
                content.setPlusNodes(result.toArray(new String[result.size()]));
                for (int i = 0; i < result.size(); i++) {
                    queue.poll();//消费去重,避免全量拉取数据后，再重复消费队列中的数据
                }
            } else {
                final String temp = queue.take();//阻塞等待,直至有具体的事件发生
                String[] result = new String[]{temp.substring(1)};
                if (temp.startsWith("+")) {
                    content.setPlusNodes(result);
                } else if (temp.startsWith("-")) {
                    content.setReducesNodes(result);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new CommandException("Subscribe Command execution failed!!!", e);
        }
        return content;
    }
}
