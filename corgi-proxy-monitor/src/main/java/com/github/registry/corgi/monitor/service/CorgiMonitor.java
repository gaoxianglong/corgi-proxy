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
package com.github.registry.corgi.monitor.service;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 监控服务处理
 *
 * @author gao_xianglong@sina.com
 * @version 0.2-SNAPSHOT
 * @date created in 2019-08-04 18:15
 */
@Service
public class CorgiMonitor {
    private final static String CORGI_ROOT_PATH = "/corgi/nodes";
    @Resource
    private CuratorFramework framework;

    public List<String> getNodes() throws Throwable {
        return framework.getChildren().forPath(CORGI_ROOT_PATH);
    }
}
