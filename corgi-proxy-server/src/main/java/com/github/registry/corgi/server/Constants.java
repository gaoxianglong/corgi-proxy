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

import com.github.registry.corgi.utils.CapacityConvert;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;

/**
 * 缺省静态常量相关类
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-18 21:51
 */
public class Constants {
    public final static int DEFAULT_PORT = 9376;
    public final static int DEFAULT_BOSS_THREADS = 10;
    public final static int DEFAULT_WORKER_THREADS = Runtime.getRuntime().availableProcessors() << 2;
    public final static int DEFAULT_BACKLOG = 128;
    public final static int DEFAULT_RCVBUF = 1024;
    public final static long MAX_FRAME_LENGTH = CapacityConvert.convert(1, CapacityConvert.MB);

    public final static int DEFAULT_THREADS = 30;
    public final static int DEFAULT_QUEUES = 100;
    public final static int DEFAULT_CORES = 10;
    public final static int DEFAULT_ALIVE = 10000;
    public final static String DEFAULT_THREADPOOL = "fixed";
    public final static String DEFAULT_HOST = "127.0.0.1:2181";
    public final static int DEFAULT_SESSION_TIMEOUT_MS = 1000;
    public final static int DEFAULT_CONNECTION_TIMEOUT_MS = 1000;
    public final static String ROOT_PATH = "/corgi/nodes";
    public final static String THREADPOOL_NAME = "cokeyThreadPool";

    public final static String COPYRIGHT = "gao_xianglong@sina.com";
    public final static String IGNITE_DOCUMENTATION = "https://github.com/gaoxianglong/corgi";
    public final static String CONFIGURATION_PATH = "properties/corgi-global.properties";
    public final static String VERSION = "0.2-SNAPSHOT";
    public static final String LINE = System.getProperty("line.separator");

    public final static String OS_NAME = System.getProperty("os.name");
    public final static String JAVA_RUNTIME_TIME = System.getProperty("java.runtime.name");
    public final static String JAVA_RUNTIME_VERSION = System.getProperty("java.runtime.version");
    public final static String JAVA_VM_VENDOR = System.getProperty("java.vm.vendor");
    public final static String JAVA_VM_NAME = System.getProperty("java.vm.name");
    public final static MemoryMXBean MEMORY_BEAN = ManagementFactory.getMemoryMXBean();
    public final static long INIT_HEAP = MEMORY_BEAN.getHeapMemoryUsage().getInit();
    public final static long USE_HEAP = MEMORY_BEAN.getHeapMemoryUsage().getUsed();
    public final static long MAX_HEAP = MEMORY_BEAN.getHeapMemoryUsage().getMax();
    public final static RuntimeMXBean RUNTIME_BEAN = ManagementFactory.getRuntimeMXBean();
    public final static int PID = Integer.parseInt(RUNTIME_BEAN.getName().split("@")[0]);
    public final static int CHECK_TIMEOUT = 2;

    public final static int SLEEP_MS_BETWEEN_RETRIES = 1000;

    public final static int LENGTH_FIELD_LENGTH = 4;
    public final static int LENGTH_ADJUSTMENT = 14;
    public final static int INITIAL_BYTES_TO_STRIP = 4;
    public final static int LENGTH_FIELD_OFFSET = 0;
    public final static long READER_IDLE_TIME = 120;
    public final static long WRITE_IDLE_TIME = 0;
    public final static long ALL_IDLE_TIME = 0;

    public final static byte REGISTER_TYPE = 1;
    public final static byte UNREGISTER_TYPE = 2;
    public final static byte SUBSCRIBE_TYPE = 3;

    public final static int CAPACITY = 1000;
    public final static int INITIAL_CAPACITY = 32;

    public final static String PLUS_EVENT = "+";
    public final static String REDUCES_EVENT = "-";
}
