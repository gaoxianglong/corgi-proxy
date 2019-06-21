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
package com.github.registry.corgi.server.launcher;

import com.github.registry.corgi.server.Parameters;
import com.github.registry.corgi.server.common.threadpool.CorgiExecutorService;
import com.github.registry.corgi.server.core.CorgiServer;
import com.github.registry.corgi.server.core.ZookeeperConnectionHandler;
import com.github.registry.corgi.server.exceptions.CorgiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * corgi-server启动器
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-18 21:42
 */
public class CoreLauncher {
    private long beginTime;
    private Logger log = LoggerFactory.getLogger("");

    public CoreLauncher(long beginTime) {
        this.beginTime = beginTime;
    }

    public void start() {
        CorgiInformation.print();//启动时输出启动信息
        Parameters parameters = new Parameters();
        try {
            PropertiesConfiguration.loadProperties(parameters);//加载并获取配置文件中的相关参数对缺省值进行替换
            ExecutorService executorService = initThreadPool(parameters);//初始化业务线程池
            //初始化Zookeeper会话连接
            ZookeeperConnectionHandler connectionHandler = new ZookeeperConnectionHandler(
                    parameters.getHost(), parameters.getSessionTimeoutMs(), parameters.getSessionTimeoutMs()).init();
            new CorgiServer(beginTime, parameters, executorService, connectionHandler).start();//启动corgi-server
        } catch (CorgiException e) {
            log.error("Corgi-server startup failed!!!", e);
        }
    }

    /**
     * 根据threadPool参数返回目标线程池
     *
     * @param parameters
     * @return
     */
    private ExecutorService initThreadPool(Parameters parameters) {
        final int cores = parameters.getCores();
        final int queues = parameters.getQueues();
        final String threadPool = parameters.getThreadPool();
        final int alive = parameters.getAlive();
        final int threads = parameters.getThreads();
        CorgiExecutorService executorService = "fixed".equalsIgnoreCase(threadPool) ?
                new CorgiExecutorService.Builder(threads, queues, threadPool).builder() :
                ("cached".equalsIgnoreCase(threadPool) ?
                        new CorgiExecutorService.Builder(threads, queues, threadPool).cores(cores).alive(alive).builder() :
                        new CorgiExecutorService.Builder(threads, queues, threadPool).cores(cores).builder());
        return executorService.getExecutor();
    }
}
