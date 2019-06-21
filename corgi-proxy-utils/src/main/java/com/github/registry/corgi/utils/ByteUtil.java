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

import org.apache.commons.lang3.StringUtils;

/**
 * byte和bit之间的转换工具类
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 21:27
 */
public class ByteUtil {
    /**
     * 将byte转换为bit后以字符串输出
     *
     * @param b
     * @return
     */
    public static String byteToBitString(byte b) {
        StringBuffer strBuffer = new StringBuffer();
        for (int i = 7; i >= 0; i--) {
            strBuffer.append((byte) ((b >> i) & 0x1));
        }
        return strBuffer.toString();
    }

    /**
     * 根据索引获取字节的第n位
     *
     * @param b
     * @param index
     * @return
     */
    public static byte get(byte b, int index) {
        if (index >= 0 && index <= 7) {
            return (byte) ((b >> index) & 0x1);
        }
        return (byte) 0;
    }

    /**
     * 获取指定区间的字节位
     *
     * @param b
     * @param begin
     * @param end
     * @return
     */
    public static byte[] getBitRange(byte b, int begin, int end) {
        byte[] result = null;
        if (begin <= end) {
            if (end >= 0 && end <= 7) {
                result = new byte[end - begin + 1];
                int i = 0;
                for (int j = begin; j <= end; i++, j++) {
                    result[i] = get(b, j);
                }
            }
        }
        return result;
    }

    /**
     * 字符串转字节
     *
     * @param str
     * @return
     */
    public static byte bitStringToByte(String str) {
        if (!StringUtils.isEmpty(str)) {
            //判断最高位，决定正负
            if (str.charAt(0) == '0') {
                return (byte) Integer.parseInt(str, 2);
            } else if (str.charAt(0) == '1') {
                return (byte) (Integer.parseInt(str, 2) - 256);
            }
        }
        return 0;
    }
}