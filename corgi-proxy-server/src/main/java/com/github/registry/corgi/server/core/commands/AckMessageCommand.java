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

import java.util.Map;
import java.util.Vector;

/**
 * ACK处理类，客户端成功拉取到信息后才真正删除数据
 *
 * @author gao_xianglong@sina.com
 * @version 0.2-SNAPSHOT
 * @date created in 2019-08-26 00:40
 */
public class AckMessageCommand extends CorgiCommandTemplate {
    private Map<String, Vector<String>> eventMap;

    protected AckMessageCommand(CorgiProtocol protocol, ZookeeperConnectionHandler connectionHandler) {
        super(protocol, connectionHandler);
    }

    protected AckMessageCommand(CorgiProtocol protocol, ZookeeperConnectionHandler connectionHandler,
                                Map<String, Vector<String>> eventMap) {
        this(protocol, connectionHandler);
        this.eventMap = eventMap;
    }

    @Override
    public TransferBo.Content run(TransferBo transferBo) throws CommandException {
        final int pullSize = transferBo.getPullSize();
        Vector<String> events = eventMap.get(transferBo.getPersistentNode());
        for (int i = 0; i < pullSize; i++) {
            try {
                if (events.isEmpty()) {
                    break;
                }
                events.remove(Constants.ACK_REMOVE_INDEX);
            } catch (Throwable e) {
                e.printStackTrace();
                throw new CommandException("Ack command execution failed!!!", e);
            }
        }
        return new TransferBo.Content();
    }
}
