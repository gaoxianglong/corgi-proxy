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
package com.github.registry.corgi.server.exceptions;

/**
 * 启动失败相关异常类
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-18 22:26
 */
public class StartingException extends CorgiException {
    private static final long serialVersionUID = 3219625587720584949L;

    public StartingException(Throwable cause) {
        super(cause);
    }

    public StartingException(String message, Throwable cause) {
        super(message, cause);
    }

    public StartingException(String message) {
        super(message);
    }
}
