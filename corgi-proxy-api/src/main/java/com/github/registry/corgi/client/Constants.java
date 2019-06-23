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

import com.github.registry.corgi.utils.CapacityConvert;

/**
 * 缺省静态常量相关类
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-18 11:09
 */
public class Constants {
    public final static byte REGISTER_TYPE = 1;
    public final static byte UNREGISTER_TYPE = 2;
    public final static byte SUBSCRIBE_TYPE = 3;

    public final static byte REQUEST_KRYO_FLAG = 0b00100001;
    public final static byte REQUEST_FASTJSON_FLAG = 0b00100010;
    public final static byte REQUEST_FST_FLAG = 0b00100100;
    public final static byte REQUEST_PING_FLAG = 0b00010000;

    public final static int REDIRECTIONS = 2;//重试次数
    public final static int RECONNECT = 5;//重连时间间隔,单位s

    public final static int LENGTH_FIELD_LENGTH = 4;
    public final static int LENGTH_ADJUSTMENT = 14;
    public final static int INITIAL_BYTES_TO_STRIP = 4;
    public final static int LENGTH_FIELD_OFFSET = 0;
    public final static long READER_IDLE_TIME = 0;
    public final static long WRITE_IDLE_TIME = 60;
    public final static long ALL_IDLE_TIME = 0;
    public final static long MAX_FRAME_LENGTH = CapacityConvert.convert(1, CapacityConvert.MB);

    public final static String OS_NAME = System.getProperty("os.name");
    public final static String CORGI_ROOT_PATH = "/corgi/nodes";
    public final static String REQUEST_RESULT = "ok";
    public final static int CAPACITY = 1000;
    public final static int INITIAL_CAPACITY = 32;
    public final static int DEFAULT_PULL_SIZE = 1;
    public final static int DEFAULT_PULL_TIMEOUT = 10000;
    public final static Boolean DEFAULT_ISBATCH = false;
}
