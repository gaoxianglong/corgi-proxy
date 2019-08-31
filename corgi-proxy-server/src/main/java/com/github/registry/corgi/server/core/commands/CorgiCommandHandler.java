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
import com.github.registry.corgi.utils.CorgiProtocol;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 实际命令处理类
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-20 00:00
 */
public class CorgiCommandHandler implements CorgiCommandStrategy {
    private CorgiProtocol protocol;
    private ZookeeperConnectionHandler connectionHandler;
    private List<String> registerPaths;
    private Map<String, Vector<String>> eventMap;
    private String channelId;

    public CorgiCommandHandler(CorgiProtocol protocol, ZookeeperConnectionHandler connectionHandler,
                               List<String> registerPaths,
                               Map<String, Vector<String>> eventMap,
                               String channelId) {
        this.protocol = protocol;
        this.connectionHandler = connectionHandler;
        this.registerPaths = registerPaths;
        this.eventMap = eventMap;
        this.channelId = channelId;
    }


    /**
     * 根据具体的命令类型选择具体的命令执行
     *
     * @return
     */
    public CorgiProtocol execute() {
        CorgiProtocol result = null;
        final byte TYPE = protocol.getType();
        switch (TYPE) {
            case Constants.REGISTER_TYPE:
                result = new RegisterCommand(protocol, connectionHandler, registerPaths).execute();
                break;
            case Constants.UNREGISTER_TYPE:
                result = new UnregisterCommand(protocol, connectionHandler).execute();
                break;
            case Constants.SUBSCRIBE_TYPE:
                result = new SubscribeCommand(protocol, connectionHandler, eventMap, channelId).execute();
                break;
            case Constants.ACK_TYPE:
                result = new AckMessageCommand(protocol, connectionHandler, eventMap).execute();
        }
        return result;
    }
}
