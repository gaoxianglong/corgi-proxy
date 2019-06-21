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
package com.github.registry.corgi.utils;

/**
 * 容量转换工具类
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 21:37
 */
public enum CapacityConvert {
    KB(0x400), MB(0x100000), GB(0x40000000), TB(0x10000000000L), PB(0x4000000000000L);
    public long value;

    CapacityConvert(long value) {
        this.value = value;
    }

    /**
     * 将数值根据目标单位转换为byte输出
     *
     * @param value
     * @param unit
     * @return
     */
    public static long convert(long value, CapacityConvert unit) {
        return value * unit.value;
    }
}
