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
package com.github.registry.corgi.monitor.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.registry.corgi.monitor.ErrorCode;
import com.github.registry.corgi.monitor.Result;
import com.github.registry.corgi.monitor.service.CorgiMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * monitor controller
 *
 * @author gao_xianglong@sina.com
 * @version 0.2-SNAPSHOT
 * @date created in 2019-08-04 17:08
 */
@Controller
@RequestMapping("/corgi/monitor")
public class MonitorController {
    @Resource
    private CorgiMonitor corgiMonitor;
    private Logger log = LoggerFactory.getLogger(MonitorController.class);

    /**
     * 获取corgi列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getCorgiNodes.do", method = RequestMethod.POST)
    public String getCorgiNodes() {
        String result = null;
        try {
            List<String> temp = corgiMonitor.getNodes();
            result = toJSON(new Result.Builder().data(temp).builder());
        } catch (Throwable e) {
            log.error("Get corgi-nodes failure!!!", e);
            result = toJSON(new Result.Builder(ErrorCode.GET_NODES_ERROR).builder());
        }
        return result;
    }

    /**
     * 序列化
     *
     * @param result
     * @return
     */
    private String toJSON(Result result) {
        return JSONObject.toJSONString(result);
    }
}
