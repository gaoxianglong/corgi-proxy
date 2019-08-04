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
package com.github.registry.corgi.monitor;


/**
 * 响应对象
 *
 * @author gao_xianglong@sina.com
 * @version 0.2-SNAPSHOT
 * @date created in 2019-08-04 22:03
 */
public class Result<T> {
    private int errorCode;
    private String desc;
    private T data;

    private Result(Builder builder) {
        this.errorCode = builder.errorCode;
        this.desc = builder.desc;
        this.data = (T) builder.data;
    }

    public static class Builder {
        private int errorCode;
        private String desc;
        private Object data;

        public Builder(ErrorCode errorCode) {
            this.errorCode = errorCode.code;
            this.desc = errorCode.desc;
        }

        public Builder() {
            this(ErrorCode.SUCCESS);
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Result builder() {
            return new Result(this);
        }
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getDesc() {
        return desc;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "errorCode=" + errorCode +
                ", desc='" + desc + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
