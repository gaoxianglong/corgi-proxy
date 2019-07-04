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

import com.github.registry.corgi.server.Constants;
import com.github.registry.corgi.server.Parameters;
import com.github.registry.corgi.server.exceptions.PropertiesException;
import com.github.registry.corgi.server.exceptions.StartingException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * 配置文件加载工具类,加载目标目录下的cokey-global.properties文件
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-18 22:20
 */
public class PropertiesConfiguration {
    private static Properties properties = new Properties();
    private static Logger log = LoggerFactory.getLogger("");

    /**
     * 加载cokey-global.properties文件，缺省路径为properties/corgi-global.properties
     *
     * @throws PropertiesException
     */
    protected static void loadProperties(Parameters parameters) throws PropertiesException {
        //从classpath下加载配置文件,打包成jar后当前classpath为conf
        try (BufferedInputStream inputStream = new BufferedInputStream(Thread.currentThread().
                getContextClassLoader().getResource(Constants.CONFIGURATION_PATH).openStream())) {
            properties.load(inputStream);
            StringBuffer strBuffer = new StringBuffer();
            properties.forEach((x, y) -> {
                strBuffer.append(String.format("%s=%s%s", x, y, Constants.LINE));
            });
            log.info("Corgi configuration file was loaded successfully(\n{})", strBuffer.toString());
            reset(parameters);
            log.info(parameters.toString());
        } catch (Throwable e) {
            throw new PropertiesException("Failed to load configuration file!!!", e);
        }
    }

    /**
     * 成功加载目标配置文件后，对启动所需相关参数的值进行重设操作
     *
     * @param target
     * @throws StartingException
     */
    private static void reset(Parameters target) throws StartingException {
        if (null == target) {
            throw new StartingException("Parameters cannot be null!!!");
        }
        properties.forEach((key, value) -> {
            Field field = null;
            final String FILED_NAME = key.toString();
            final String FIELD_VALUE = value.toString();
            try {
                int index = FILED_NAME.lastIndexOf(".");
                if (-1 != index) {
                    field = target.getClass().getDeclaredField(FILED_NAME.substring(++index));
                }
            } catch (NoSuchFieldException e) {//如果找不到的字段则不赋值
                log.debug("The target field({}) could not be found", FILED_NAME);
            }
            if (null != field) {
                field.setAccessible(true);
                if (!StringUtils.isEmpty(FIELD_VALUE)) {
                    try {
                        switch (field.getGenericType().getTypeName()) {
                            case "byte":
                                field.setByte(target, Byte.parseByte(FIELD_VALUE));
                                break;
                            case "short":
                                field.setShort(target, Short.parseShort(FIELD_VALUE));
                                break;
                            case "int":
                                field.setInt(target, Integer.parseInt(FIELD_VALUE));
                                break;
                            case "long":
                                field.setLong(target, Long.parseLong(FIELD_VALUE));
                                break;
                            case "float":
                                field.setFloat(target, Float.parseFloat(FIELD_VALUE));
                                break;
                            case "double":
                                field.setDouble(target, Double.parseDouble(FIELD_VALUE));
                                break;
                            case "boolean":
                                field.setBoolean(target, Boolean.parseBoolean(FIELD_VALUE));
                                break;
                            default:
                                field.set(target, value);//找不到对应的数据类型，则按引用类型处理
                        }
                    } catch (Throwable e) {//类型转换错误
                        log.error("Data type mismatch?", e);
                    }
                }
            }
        });
    }
}
