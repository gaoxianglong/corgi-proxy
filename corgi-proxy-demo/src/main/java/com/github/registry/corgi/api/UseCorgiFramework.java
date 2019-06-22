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
package com.github.registry.corgi.api;

import com.github.registry.corgi.client.CorgiCommands;
import com.github.registry.corgi.client.CorgiFramework;
import com.github.registry.corgi.client.HostAndPort;
import com.github.registry.corgi.client.exceptions.CommandException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 直接使用Corgi原生客户端实现服务注册/订阅
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-22 15:17
 */
public class UseCorgiFramework {
    /**
     * 缺省的hostname
     */
    private final String hostName = "127.0.0.1";
    /**
     * 缺省corgi-server端口
     */
    private final int port = 9376;
    private final int redirections = 2;
    private CorgiFramework.SerializationType serializationType = CorgiFramework.SerializationType.FST;
    private final String[] rootPath = {"/dubbo/com.test.service1", "/dubbo/com.test.service2", "/dubbo/com.test.service3"};
    private CorgiFramework framework;
    private Logger log = LoggerFactory.getLogger(UseCorgiFramework.class);

    public static void main(String[] agrs) {
        new UseCorgiFramework().run();
    }

    private UseCorgiFramework() {
        //初始化corgi客户端
        //framework = new CorgiFramework.Builder(new HostAndPort(hostName, port)).builder().init();
        //List<HostAndPort> hostAndPortList = Arrays.asList(new HostAndPort(hostName, port));
        framework = new CorgiFramework.Builder(new HostAndPort(hostName, port)).redirections(redirections).
                serialization(serializationType).builder().init();
    }

    /**
     * 命令执行
     */
    private void run() {
        Stream.of(rootPath).filter(x -> !StringUtils.isEmpty(x)).forEach(path -> {
            for (int i = 0; i < (int) (Math.random() * rootPath.length); i++) {
                String result = register(path, UUID.randomUUID().toString());//服务注册操作
                log.info("Register result:{}", result);
            }
        });
        try {
            this.register(rootPath[0], null);//服务注册操作
        } catch (CommandException e) {
            log.error("{}", e);
        }
        log.info(subscribe(rootPath[0]).toString());//单次订阅
        Stream.of(rootPath).filter(x -> !StringUtils.isEmpty(x)).forEach(path -> {
            Executors.newSingleThreadScheduledExecutor().execute(() -> {
                while (true) {
                    log.info("Result:{}", subscribe(rootPath[0]).toString());//持续订阅
                }
            });
        });
        try {
            TimeUnit.SECONDS.sleep(5);
            register(rootPath[0], "192.168.1.1:21999");//服务注册操作
            TimeUnit.SECONDS.sleep(5);
            log.info("Unregister result:{}", unRegister(rootPath[0], "192.168.1.1:21999"));
        } catch (Throwable e) {
            log.error("{}", e);
        }
    }

    /**
     * Provider/Comsumer注册
     *
     * @param persistentNode
     * @param ephemeralNode
     * @return
     */
    private String register(String persistentNode, String ephemeralNode) {
        return framework.register(persistentNode, ephemeralNode);
    }

    /**
     * 取消注册
     *
     * @param persistentNode
     * @param ephemeralNode
     * @return
     */
    private String unRegister(String persistentNode, String ephemeralNode) {
        return framework.unRegister(persistentNode, ephemeralNode);
    }

    /**
     * 订阅
     *
     * @param persistentNode
     * @return
     */
    private CorgiCommands.NodeBo subscribe(String persistentNode) {
        return framework.subscribe(persistentNode);
    }
}
