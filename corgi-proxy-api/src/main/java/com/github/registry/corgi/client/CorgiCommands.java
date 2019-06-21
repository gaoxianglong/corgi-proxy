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

import java.util.Arrays;

/**
 * Registry Proxy基本命令接口
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 22:19
 */
public interface CorgiCommands {
    /**
     * Provider/Comsumer注册接口
     *
     * @param persistentNode
     * @param ephemeralNode
     * @return
     */
    String register(String persistentNode, String ephemeralNode);

    /**
     * Provider/Comsumer注册取消注册接口
     *
     * @param persistentNode
     * @param ephemeralNode
     * @return
     */
    String unRegister(String persistentNode, String ephemeralNode);

    /**
     * 增量订阅接口,如果是第一次订阅,则返回一个服务接口下的全量地址列表
     *
     * @param persistentNod
     * @return
     */
    NodeBo subscribe(String persistentNod);

    class NodeBo {
        /**
         * 增加的节点
         */
        private String[] plusNodes;
        /**
         * 减少的节点
         */
        private String[] reducesNodes;

        public NodeBo() {
        }

        public NodeBo(String[] plusNodes, String[] reducesNodes) {
            this.plusNodes = plusNodes;
            this.reducesNodes = reducesNodes;
        }

        public String[] getPlusNodes() {
            return plusNodes;
        }

        public void setPlusNodes(String[] plusNodes) {
            this.plusNodes = plusNodes;
        }

        public String[] getReducesNodes() {
            return reducesNodes;
        }

        public void setReducesNodes(String[] reducesNodes) {
            this.reducesNodes = reducesNodes;
        }

        @Override
        public String toString() {
            return "NodeBean{" +
                    "plusNodes=" + Arrays.toString(plusNodes) +
                    ", reducesNodes=" + Arrays.toString(reducesNodes) +
                    '}';
        }
    }
}