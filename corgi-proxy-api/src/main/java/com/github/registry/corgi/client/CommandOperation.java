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

import com.github.registry.corgi.utils.CorgiProtocol;

/**
 * 命令操作接口
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-18 12:28
 */
public interface CommandOperation {
    /**
     * 向corgi-server发送命令请求
     *
     * @param protocol
     * @param template
     */
    void sendCommand(CorgiProtocol protocol, CorgiCommands template);

    /**
     * 获取corgi-server响应结果
     *
     * @param msgId
     * @return
     */
    CorgiProtocol getResult(long msgId);
}
