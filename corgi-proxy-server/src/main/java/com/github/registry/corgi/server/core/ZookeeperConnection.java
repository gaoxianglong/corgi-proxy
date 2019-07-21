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
import com.github.registry.corgi.server.exceptions.StartingException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zookeeper会话连接类
 *
 * @author gao_xianglong@sina.com
 * @version 0.2-SNAPSHOT
 * @date created in 2019-07-21 14:47
 */
public class ZookeeperConnection {
    /**
     * zookeeper host
     */
    private String host;

    /**
     * zookeeper的租约时间
     */
    private int sessionTimeoutMs;

    /**
     * zookeeper的连接超时时间
     */
    private int connectionTimeoutMs;
    private Logger log = LoggerFactory.getLogger("");

    public CuratorFramework getFramework() {
        return framework;
    }

    private CuratorFramework framework;

    public ZookeeperConnection(String host, int sessionTimeoutMs, int connectionTimeoutMs) {
        this.host = host;
        this.sessionTimeoutMs = sessionTimeoutMs;
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    /**
     * 初始化Zookeeper会话连接
     *
     * @return
     * @throws StartingException
     */
    public void conn() throws StartingException {
        try {
            framework = CuratorFrameworkFactory.builder().connectString(host).sessionTimeoutMs(sessionTimeoutMs)
                    .connectionTimeoutMs(connectionTimeoutMs).retryPolicy(new RetryNTimes(Integer.MAX_VALUE,
                            Constants.SLEEP_MS_BETWEEN_RETRIES))
                    .build();
            framework.start();
        } catch (Throwable e) {
            throw new StartingException(e);
        }
    }

    /**
     * 资源释放
     */
    public void close() {
        if (null != framework && framework.isStarted()) {
            framework.close();
        }
    }
}
