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

import java.util.concurrent.ExecutorService;

/**
 * corgi-server线程池接口,使用default方法实现适配器模式
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-19 14:53
 */
public interface ThreadPool {
    /**
     * 适配于FixedThreadPool
     *
     * @param threads
     * @param queues
     * @param nameFormat
     * @return
     */
    default ExecutorService getExecutor(int threads, int queues, String nameFormat) {
        return null;
    }

    /**
     * 适配于LimitedThreadPool
     *
     * @param threads
     * @param queues
     * @param cores
     * @param nameFormat
     * @return
     */
    default ExecutorService getExecutor(int threads, int queues, int cores, String nameFormat) {
        return null;
    }

    /**
     * 适配于CachedThreadPool
     *
     * @param threads
     * @param queues
     * @param cores
     * @param alive
     * @param nameFormat
     * @return
     */
    default ExecutorService getExecutor(int threads, int queues, int cores, int alive, String nameFormat) {
        return null;
    }

    default ExecutorService getExecutor() {
        return null;
    }
}
