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
package com.github.registry.corgi.server.common.threadpool.support;

import com.github.registry.corgi.server.Constants;
import com.github.registry.corgi.server.common.threadpool.ThreadPool;
import org.apache.curator.shaded.com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Optional;
import java.util.concurrent.*;

/**
 * Cached线程池，可回收缓存线程池，空闲线程允许进行回收
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-19 15:19
 */
public class CachedThreadPool implements ThreadPool {
    @Override
    public ExecutorService getExecutor(int threads, int queues, int cores, int alive, String nameFormat) {
        nameFormat = Optional.ofNullable(nameFormat).orElseGet(() -> Constants.THREADPOOL_NAME + "-%d");
        return new ThreadPoolExecutor(cores, threads, alive, TimeUnit.MILLISECONDS,
                queues == 0 ? new SynchronousQueue<Runnable>() :
                        (queues < 0 ? new LinkedBlockingQueue<Runnable>()
                                : new LinkedBlockingQueue<Runnable>(queues)),
                new ThreadFactoryBuilder().setNameFormat(nameFormat).setDaemon(true).build(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
