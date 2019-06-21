///*
// * Copyright 2019-2119 gao_xianglong@sina.com
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.github.test.registry.corgi.client;
//
//import com.github.registry.corgi.client.CorgiFramework;
//import com.github.registry.corgi.client.HostAndPort;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
///**
// * @author gao_xianglong@sina.com
// * @version 0.1-SNAPSHOT
// * @date created in 2019-06-18 00:42
// */
//public class CorgiFrameworkTest {
//    private Logger log = LoggerFactory.getLogger(CorgiFrameworkTest.class);
//
//    @Test
//    public void testConnection() {
//        CorgiFramework framework = new CorgiFramework.Builder(new HostAndPort("127.0.0.1", 9376)).
//                serialization(CorgiFramework.SerializationType.FST).builder().init();
//        log.info("result:{}", framework.register("/dubbo/com.gxl.test.service.user.UserService/providers",
//                UUID.randomUUID().toString()));
//        log.info("result:{}", framework.register("/dubbo/com.gxl.test.service.user.UserService2/providers",
//                UUID.randomUUID().toString()));
////        while (true) {
////            try {
////                log.info(framework.subscribe("/dubbo/com.gxl.test.service.user.UserService/providers").toString());
////                log.info(framework.subscribe("/dubbo/com.gxl.test.service.user.UserService2/providers").toString());
////            } catch (Exception e) {
////                //e.printStackTrace();
////            }
////        }
//
//        log.info(framework.subscribe("/dubbo/com.gxl.test.service.user.UserService/providers").toString());
//        log.info(framework.subscribe("/dubbo/com.gxl.test.service.user.UserService2/providers").toString());
//        //log.info(framework.subscribe("dubbo/com.gxl.test.service.user.UserService/configurators").toString());
/////dubbo/com.gxl.test.service.user.UserService/configurators
////        try {
////            System.in.read();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//
//
//        //log.info("result:{}",framework.unRegister("/dubbo/com.gxl.test.service.user.UserService/providers","4ac32d53-2afe-4ff1-a22e-43ed85b862d4"));
//        //framework.close();
//    }
//}
