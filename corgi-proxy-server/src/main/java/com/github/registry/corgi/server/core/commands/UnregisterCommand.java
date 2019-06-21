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

import com.github.registry.corgi.server.core.ZookeeperConnectionHandler;
import com.github.registry.corgi.server.exceptions.CommandException;
import com.github.registry.corgi.utils.CorgiProtocol;
import com.github.registry.corgi.utils.TransferBo;

/**
 * 取消注册命令处理类，用于Consumer/Provider取消注册
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-19 17:31
 */
public class UnregisterCommand extends CorgiCommandTemplate {
    protected UnregisterCommand(CorgiProtocol protocol, ZookeeperConnectionHandler connectionHandler) {
        super(protocol, connectionHandler);
    }

    @Override
    public TransferBo.Content run(TransferBo transferBo) throws CommandException {
        ZookeeperConnectionHandler connectionHandler = super.getConnectionHandler();
        TransferBo.Content content = new TransferBo.Content();
        try {
            connectionHandler.deleteChildren(transferBo.getPersistentNode(), transferBo.getEphemeralNode());
        } catch (Throwable e) {
            throw new CommandException("Unregister Command execution failed!!!", e);
        }
        return content;
    }
}
