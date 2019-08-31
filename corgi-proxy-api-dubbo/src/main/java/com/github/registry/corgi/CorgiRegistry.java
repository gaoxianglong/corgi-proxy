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
package com.github.registry.corgi;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.support.FailbackRegistry;
import com.github.registry.corgi.client.CorgiCommands;
import com.github.registry.corgi.client.CorgiFramework;
import com.github.registry.corgi.client.HostAndPort;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Corgi注册服务
 *
 * @author gao_xianglong@sina.com
 * @version 0.1-SNAPSHOT
 * @date created in 2019-06-20 18:47
 */
public class CorgiRegistry extends FailbackRegistry {
    /**
     * 序列化类型
     */
    private CorgiFramework.SerializationType serializationType;
    /**
     * 全量服务地址列表
     */
    private final List<URL> urls = new Vector<>(com.github.registry.corgi.client.Constants.INITIAL_CAPACITY);
    private int redirections;
    private int pullTimeOut;
    private int pullSize;
    private CorgiFramework framework;
    private String root;
    private Logger log = LoggerFactory.getLogger(CorgiRegistry.class);

    public CorgiRegistry(URL url) {
        super(url);
        String group = url.getParameter(Constants.GROUP_KEY, "dubbo");
        if (!group.startsWith(Constants.PATH_SEPARATOR)) {
            group = Constants.PATH_SEPARATOR + group;
        }
        this.root = group;
        redirections = url.getParameter("redirections", com.github.registry.corgi.client.Constants.REDIRECTIONS);
        pullTimeOut = url.getParameter("pullTimeOut", com.github.registry.corgi.client.Constants.DEFAULT_PULL_TIMEOUT);
        pullSize = url.getParameter("pullSize", com.github.registry.corgi.client.Constants.DEFAULT_PULL_SIZE);
        init(url.getBackupAddress());
    }

    /**
     * 相关初始化操作
     */
    private void init(String address) {
        if (StringUtils.isEmpty(address)) {
            return;
        }
        List<HostAndPort> hostAndPorts = new Vector<>(com.github.registry.corgi.client.Constants.INITIAL_CAPACITY);
        Stream.of(address.split("\\,")).forEach(x -> {
            String[] temp = x.split("\\:");
            final String HOST = temp[0];
            final int PORT = Integer.parseInt(temp[1]);
            hostAndPorts.add(new HostAndPort(HOST, PORT));
        });
        if (!hostAndPorts.isEmpty()) {
            serializationType = CorgiFramework.SerializationType.FST;
            framework = new CorgiFramework.Builder(hostAndPorts).serialization(serializationType).
                    redirections(redirections).pullSize(pullSize).pullTimeOut(pullTimeOut).builder().init();
        }
    }

    private String toUrlPath(URL url) {
        return toCategoryPath(url) + Constants.PATH_SEPARATOR + URL.encode(url.toFullString());
    }

    private String toCategoryPath(URL url) {
        return toServicePath(url) + Constants.PATH_SEPARATOR + url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
    }

    private String toServicePath(URL url) {
        String name = url.getServiceInterface();
        if (Constants.ANY_VALUE.equals(name)) {
            return toRootPath();
        }
        return toRootDir() + URL.encode(name);
    }

    private String toRootPath() {
        return root;
    }

    private String toRootDir() {
        if (root.equals(Constants.PATH_SEPARATOR)) {
            return root;
        }
        return root + Constants.PATH_SEPARATOR;
    }

    private String[] toCategoriesPath(URL url) {
        String[] categroies;
        if (Constants.ANY_VALUE.equals(url.getParameter(Constants.CATEGORY_KEY))) {
            categroies = new String[]{Constants.PROVIDERS_CATEGORY, Constants.CONSUMERS_CATEGORY,
                    Constants.ROUTERS_CATEGORY, Constants.CONFIGURATORS_CATEGORY};
        } else {
            categroies = url.getParameter(Constants.CATEGORY_KEY, new String[]{Constants.DEFAULT_CATEGORY});
        }
        String[] paths = new String[categroies.length];
        for (int i = 0; i < categroies.length; i++) {
            paths[i] = toServicePath(url) + Constants.PATH_SEPARATOR + categroies[i];
        }
        return paths;
    }

    @Override
    protected void doRegister(URL url) {
        final String urlPath = toUrlPath(url);
        final String persistentNode = getPersistentNode(urlPath);
        final String ephemeralNode = getEphemeralNode(urlPath);
        if (!StringUtils.isEmpty(persistentNode) && !StringUtils.isEmpty(ephemeralNode)) {
            framework.register(persistentNode, ephemeralNode);
        }
    }

    @Override
    protected void doUnregister(URL url) {
        final String urlPath = toUrlPath(url);
        final String persistentNode = getPersistentNode(urlPath);
        final String ephemeralNode = getEphemeralNode(urlPath);
        if (!StringUtils.isEmpty(persistentNode) && !StringUtils.isEmpty(ephemeralNode)) {
            framework.unRegister(persistentNode, ephemeralNode);
        }
    }

    @Override
    protected void doSubscribe(URL url, NotifyListener listener) {
        for (String path : toCategoriesPath(url)) {
            if (path.contains("providers")) {//暂时只订阅providers目录
                CorgiCommands.NodeBo nodeBo = framework.subscribe(path);//第一次全量拉取
                String[] plusNodes = nodeBo.getPlusNodes();
                if (null != plusNodes) {
                    Stream.of(plusNodes).forEach(x -> {
                        urls.add(URL.valueOf(URL.decode(x)));
                    });
                }
                Executors.newSingleThreadScheduledExecutor().execute(() -> {
                    while (true) {
                        CorgiCommands.NodeBo nodeBo_ = framework.subscribe(path);
                        if (null == nodeBo_ || (null == nodeBo_.getPlusNodes() && null == nodeBo_.getReducesNodes())) {
                            continue;
                        }
//                        if (null == nodeBo_) {
//                            continue;
//                        }
                        String[] plusNodes_ = nodeBo_.getPlusNodes();
                        if (null != plusNodes_) {
                            Stream.of(plusNodes_).filter(x -> !urls.contains(x)).forEach(x -> {
                                urls.add(URL.valueOf(URL.decode(x)));
                                log.debug("plusNodes:{}", plusNodes_);
                            });
                        }
                        String[] reducesNodes = nodeBo_.getReducesNodes();
                        if (null != reducesNodes) {
                            Stream.of(reducesNodes).forEach(x -> {
                                urls.remove(URL.valueOf(URL.decode(x)));
                                log.debug("reducesNodes:{}", reducesNodes);
                            });
                        }
                        listener.notify(urls);
                    }
                });
            }
        }
        notify(url, listener, urls);
    }

    private String getPersistentNode(String urlPath) {
        String result = null;
        int index = urlPath.lastIndexOf('/');
        if (index > 0) {
            result = urlPath.substring(0, index);
        }
        return result;
    }

    private String getEphemeralNode(String urlPath) {
        String result = null;
        int index = urlPath.lastIndexOf('/');
        if (index > 0) {
            result = urlPath.substring(++index, urlPath.length());
        }
        return result;
    }

    @Override
    protected void doUnsubscribe(URL url, NotifyListener listener) {

    }

    @Override
    public boolean isAvailable() {
        if (null != framework) {
            return framework.isActive();
        }
        return false;
    }
}
