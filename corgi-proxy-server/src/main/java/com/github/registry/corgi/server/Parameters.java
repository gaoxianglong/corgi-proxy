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
package com.github.registry.corgi.server;

/**
 * corgi-server启动所需的各种相关参数，如果没有从配置文件中加载到目标项，则直接使用Constants中定义的缺省值
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-18 22:15
 */
public class Parameters {
    /**
     * corgi-server端口,缺省为9376
     */
    private int port = Constants.DEFAULT_PORT;

    /**
     * boss线程数,缺省为10
     */
    private int bossThreadSize = Constants.DEFAULT_BOSS_THREADS;

    /**
     * worker线程数,缺省为为CPU核心的4倍
     */
    private int workerThreadSize = Constants.DEFAULT_WORKER_THREADS;

    /**
     * corgi-server接受连接的队列长度,缺省为128
     */
    private int backLog = Constants.DEFAULT_BACKLOG;

    /**
     * TCP数据接收缓冲区大小,缺省为1024,单位KB
     */
    private int rcvbuf = Constants.DEFAULT_RCVBUF;

    /**
     * 业务线程池最大线程数,缺省为30
     */
    private int threads = Constants.DEFAULT_THREADS;

    /**
     * 业务线程池队列长度,缺省为100
     */
    private int queues = Constants.DEFAULT_QUEUES;

    /**
     * 业务线程池模式(fixed\limited\cached),缺省为fixed
     */
    private String threadPool = Constants.DEFAULT_THREADPOOL;

    /**
     * 业务线程池核心线程数,缺省10
     */
    private int cores = Constants.DEFAULT_CORES;

    /**
     * 业务线程池空闲线程的存活时间,缺省为10s
     */
    private int alive = Constants.DEFAULT_ALIVE;

    /**
     * zookeeper地址,缺省为127.0.0.1:2181
     */
    private String host = Constants.DEFAULT_HOST;

    /**
     * zookeeper的租约时间
     */
    private int sessionTimeoutMs = Constants.DEFAULT_SESSION_TIMEOUT_MS;

    /**
     * zookeeper的连接超时时间
     */
    private int connectionTimeoutMs = Constants.DEFAULT_CONNECTION_TIMEOUT_MS;

    public int getPort() {
        return port;
    }

    public int getBossThreadSize() {
        return bossThreadSize;
    }

    public int getWorkerThreadSize() {
        return workerThreadSize;
    }


    public int getBackLog() {
        return backLog;
    }

    public int getRcvbuf() {
        return rcvbuf;
    }

    public int getThreads() {
        return threads;
    }

    public int getQueues() {
        return queues;
    }

    public String getThreadPool() {
        return threadPool;
    }

    public int getCores() {
        return cores;
    }

    public int getAlive() {
        return alive;
    }

    public String getHost() {
        return host;
    }

    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    @Override
    public String toString() {
        return "Parameters{" +
                "port=" + port +
                ", bossThreadSize=" + bossThreadSize +
                ", workerThreadSize=" + workerThreadSize +
                ", backLog=" + backLog +
                ", rcvbuf=" + rcvbuf +
                ", threads=" + threads +
                ", queues=" + queues +
                ", threadPool='" + threadPool + '\'' +
                ", cores=" + cores +
                ", alive=" + alive +
                ", host='" + host + '\'' +
                ", sessionTimeoutMs=" + sessionTimeoutMs +
                ", connectionTimeoutMs=" + connectionTimeoutMs +
                '}';
    }
}
