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
package com.github.registry.corgi.server.core;

import com.github.registry.corgi.server.Constants;
import com.github.registry.corgi.server.exceptions.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于存储目标服务接口的上/下线事件流
 *
 * @author gao_xianglong@sina.com
 * @version 0.2-SNAPSHOT
 * @date created in 2019-07-29 15:03
 */
public class ServiceEvents {
    private AtomicInteger position = new AtomicInteger();
    private Map<Integer, EventBO> events = new ConcurrentHashMap<>(Constants.INITIAL_CAPACITY);
    private Logger log = LoggerFactory.getLogger(ServiceEvents.class);

    /**
     * 添加事件
     *
     * @param event
     */
    public void addEvent(String event) {
        events.put(position.getAndIncrement(),
                new EventBO(event, getExpire(System.currentTimeMillis())));
    }

    /**
     * 获取末尾位点
     *
     * @return
     */
    public int getLastPosition() {
        return position.get();
    }

    /**
     * 如果事件流map中存在数据，返回起始位点，反之返回末尾位点
     * 正常情况下，不会调用此方法获取位点，只有当事件流map中的过期数据被清理后，无效位点访问时才会调用
     *
     * @return
     */
    public int getInitPosition() {
        return events.isEmpty() ? getLastPosition() :
                events.keySet().stream().min(Comparator.comparing(x -> x)).get();
    }

    public String getEvent(int position) throws CommandException {
        EventBO eventBO = events.get(position);
        if (null == eventBO) {
            throw new CommandException("Invalid position!!!");
        }
        return events.get(position).getEvent();
    }

    protected void cleanPeriodically(long timestamp) {
        events.forEach((x, y) -> {
            if (timestamp >= y.getExpire()) {
                EventBO result = null;
                do {
                    result = events.remove(x);
                } while (null == result);
                log.debug("Cleanup event:{}", y.toString());
            }
        });
    }

    /**
     * 获取过期时间
     *
     * @return
     */
    private long getExpire(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.add(Calendar.DAY_OF_MONTH, Constants.AMOUNT);//缺省1天后过期
        return calendar.getTimeInMillis();
    }

    public static class EventBO {
        /**
         * 具体的上/下线事件
         */
        private String event;

        /**
         * 过期时间
         */
        private long expire;

        public EventBO(String event, long expire) {
            this.event = event;
            this.expire = expire;
        }

        public String getEvent() {
            return event;
        }

        public long getExpire() {
            return expire;
        }

        @Override
        public String toString() {
            return "EventBO{" +
                    "event='" + event + '\'' +
                    ", expire=" + expire +
                    '}';
        }
    }
}
