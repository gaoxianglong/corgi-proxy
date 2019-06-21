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

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接实际处理类
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-18 12:13
 */
public class CorgiConnectionHandler implements CommandOperation {
    private CorgiClient client;
    private String hostName;
    private int port;
    private volatile static AtomicInteger index = new AtomicInteger(-1);

    public CorgiConnectionHandler(List<HostAndPort> hostAndPorts) {
        if (null == hostAndPorts || hostAndPorts.isEmpty()) {
            return;
        }
        HostAndPort hostAndPort = null;
        if (hostAndPorts.size() < 2) {
            hostAndPort = hostAndPorts.get(0);
        } else {
            Collections.shuffle(hostAndPorts);//对地址列表进行乱序排列操作
            int temp = index.incrementAndGet();
            if (temp >= hostAndPorts.size()) {
                int source = 0;
                do {
                    source = index.get();
                } while (!(index.compareAndSet(source, -1)));
                temp = index.incrementAndGet();
            }
            hostAndPort = hostAndPorts.get(temp);
        }
        this.hostName = hostAndPort.getHostName();
        this.port = hostAndPort.getPort();
    }

    public CorgiConnectionHandler init() {
        client = new CorgiClient(new InetSocketAddress(hostName, port)).init();//建立corgi-client与corgi-server的会话连接
        return this;
    }

    /**
     * 资源释放
     */
    public void close() {
        client.close();
    }

    /**
     * 判断连接是否可用
     *
     * @return
     */
    protected boolean isActive() {
        return client.isActive();
    }

    @Override
    public void sendCommand(CorgiProtocol protocol, CorgiCommands template) {
        client.sendCommand(protocol, template);
    }

    @Override
    public CorgiProtocol getResult(long msgId) {
        return client.getResult(msgId);
    }
}
