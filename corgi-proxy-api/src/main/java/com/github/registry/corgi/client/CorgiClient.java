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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 处理Channel的消息发送/请求接收
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 22:25
 */
public class CorgiClient extends CorgiConnection implements CommandOperation {
    private Logger log = LoggerFactory.getLogger(CorgiClient.class);

    public CorgiClient(InetSocketAddress address) {
        super(address);
    }

    /**
     * 调用CorgiConnection的conn()方法建立会话连接
     *
     * @return
     */
    public CorgiClient init() {
        super.conn();
        return this;
    }

    @Override
    public void sendCommand(CorgiProtocol protocol, CorgiCommands template) {
        super.getChannel().writeAndFlush(protocol).addListener(x -> {
            if (x.isSuccess()) {
                log.debug("Command sent successfully,protocol({})", protocol.toString());
            } else {
                x.cause();
            }
        });
        synchronized (template) {
            try {
                template.wait();//阻塞当前业务线程，等待corgi-server响应对等消息时被唤醒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public CorgiProtocol getResult(long msgId) {
        return CorgiFramework.getResultMap().get(msgId);
    }
}