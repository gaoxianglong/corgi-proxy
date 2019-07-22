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

import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class CorgiExecutorServiceTest {
    @Test
    public void testFixedThreadPool() {
        CorgiExecutorService corgiExecutorService = new CorgiExecutorService.Builder(10, 10, "fixed").builder();
        ExecutorService executorService = null;
        CountDownLatch latch = new CountDownLatch(corgiExecutorService.getThreads());
        executorService = corgiExecutorService.getExecutor();
        for (int i = 0; i < corgiExecutorService.getThreads(); i++) {
            executorService.execute(() -> {
                for (int j = 0; j < 10000; j++) {
                    UUID.randomUUID().toString();
                }
            });
            latch.countDown();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
