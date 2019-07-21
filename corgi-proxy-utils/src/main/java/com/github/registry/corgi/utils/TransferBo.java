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

import java.io.Serializable;
import java.util.Arrays;

/**
 * 传输对象，封装有request/response所需的相关参数
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-17 14:18
 */
public class TransferBo implements Serializable {
    private static final long serialVersionUID = 5959656478729258518L;
    /**
     * 持久节点
     */
    private String persistentNode;
    /**
     * 瞬时节点
     */
    private String ephemeralNode;
    /**
     * 每次增量拉取的数量
     */
    private int pullSize;
    /**
     * 客户端记录的拉取位点
     */
    private int index;
    /**
     * 拉取超时时间
     */
    private int pullTimeOut;
    /**
     * 批量拉取开关
     */
    private Boolean isBatch;
    /**
     * response结果集
     */
    private Content content;

    public static class Content implements Serializable {
        /**
         * 响应结果，操作成功(ok)或者失败(fail:异常),缺省ok
         */
        private String result = "ok";
        /**
         * 新增节点
         */
        private String[] plusNodes;
        /**
         * 删除节点
         */
        private String[] reducesNodes;
        /**
         * 第一次订阅时返回给客户端初始的位点
         */
        private int initIndex;

        public int getInitIndex() {
            return initIndex;
        }

        public void setInitIndex(int initIndex) {
            this.initIndex = initIndex;
        }

        public String getResult() {
            return result;
        }

        public String[] getPlusNodes() {
            return plusNodes;
        }

        public String[] getReducesNodes() {
            return reducesNodes;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public void setPlusNodes(String[] plusNodes) {
            this.plusNodes = plusNodes;
        }

        public void setReducesNodes(String[] reducesNodes) {
            this.reducesNodes = reducesNodes;
        }

        @Override
        public String toString() {
            return "Content{" +
                    "result='" + result + '\'' +
                    ", plusNodes=" + Arrays.toString(plusNodes) +
                    ", reducesNodes=" + Arrays.toString(reducesNodes) +
                    ", initIndex=" + initIndex +
                    '}';
        }
    }

    public String getPersistentNode() {
        return persistentNode;
    }

    public void setPersistentNode(String persistentNode) {
        this.persistentNode = persistentNode;
    }

    public String getEphemeralNode() {
        return ephemeralNode;
    }

    public void setEphemeralNode(String ephemeralNode) {
        this.ephemeralNode = ephemeralNode;
    }

    public int getPullSize() {
        return pullSize;
    }

    public void setPullSize(int pullSize) {
        this.pullSize = pullSize;
    }

    public int getPullTimeOut() {
        return pullTimeOut;
    }

    public void setPullTimeOut(int pullTimeOut) {
        this.pullTimeOut = pullTimeOut;
    }

    public Boolean isBatch() {
        return isBatch;
    }

    public void setBatch(Boolean batch) {
        isBatch = batch;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "TransferBo{" +
                "persistentNode='" + persistentNode + '\'' +
                ", ephemeralNode='" + ephemeralNode + '\'' +
                ", pullSize=" + pullSize +
                ", index=" + index +
                ", pullTimeOut=" + pullTimeOut +
                ", isBatch=" + isBatch +
                ", content=" + content.toString() +
                '}';
    }
}