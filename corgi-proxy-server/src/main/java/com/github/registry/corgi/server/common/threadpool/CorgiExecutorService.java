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
package com.github.registry.corgi.server.common.threadpool;

import com.github.registry.corgi.server.common.threadpool.support.CachedThreadPool;
import com.github.registry.corgi.server.common.threadpool.support.FixedThreadPool;
import com.github.registry.corgi.server.common.threadpool.support.LimitedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * corgi-server线程池选择器
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-19 15:28
 */
public class CorgiExecutorService implements ThreadPool {
    /**
     * 线程池类型
     */
    private String threadPool;
    /**
     * 最大线程数
     */
    private int threads;
    /**
     * 核心线程数
     */
    private int cores;
    /**
     * 队列数量
     */
    private int queues;
    /**
     * 空闲时间,单位ms
     */
    private int alive;
    private String nameFormat;

    private CorgiExecutorService(Builder builder) {
        this.threadPool = builder.threadPool;
        this.threads = builder.threads;
        this.cores = builder.cores;
        this.queues = builder.queues;
        this.alive = builder.alive;
        this.nameFormat = builder.nameFormat;
    }

    public static class Builder {
        private String threadPool;
        private int threads;
        private int queues;
        private int cores;
        private int alive;
        private String nameFormat;

        public Builder(int threads, int queues, String threadPool) {
            this.threads = threads;
            this.queues = queues;
            this.threadPool = threadPool;
        }

        public Builder nameFormat(String nameFormat) {
            this.nameFormat = nameFormat;
            return this;
        }

        public Builder cores(int cores) {
            this.cores = cores;
            return this;
        }

        public Builder alive(int alive) {
            this.alive = alive;
            return this;
        }

        public CorgiExecutorService builder() {
            return new CorgiExecutorService(this);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "threadPool='" + threadPool + '\'' +
                    ", threads=" + threads +
                    ", queues=" + queues +
                    ", cores=" + cores +
                    ", alive=" + alive +
                    ", nameFormat='" + nameFormat + '\'' +
                    '}';
        }
    }

    @Override
    public ExecutorService getExecutor() {
        //如果传入的不是limit也按LimitedThreadPool返回
        return threadPool.equalsIgnoreCase("fixed") ?
                new FixedThreadPool().getExecutor(threads, queues, nameFormat) :
                (threadPool.equalsIgnoreCase("cached") ?
                        new CachedThreadPool().getExecutor(threads, queues, cores, alive, nameFormat) :
                        new LimitedThreadPool().getExecutor(threads, queues, cores, nameFormat));
    }

    public String getThreadPool() {
        return threadPool;
    }

    public int getThreads() {
        return threads;
    }

    public int getCores() {
        return cores;
    }

    public int getQueues() {
        return queues;
    }

    public int getAlive() {
        return alive;
    }

    public String getNameFormat() {
        return nameFormat;
    }
}
