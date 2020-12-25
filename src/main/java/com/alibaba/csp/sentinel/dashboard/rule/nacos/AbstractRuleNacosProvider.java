/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * 从 Nacos 配置中心加载 Sentinel Rules 的通用类
 *
 * @author Kelvin Gui
 */
public abstract class AbstractRuleNacosProvider<T> implements DynamicRuleProvider<List<T>> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigService configService;
    @Autowired
    private Converter<String, List<T>> converter;

    /**
     * 1）通过 ConfigService 的 getConfig() 方法从 Nacos Config Server 读取指定配置信息
     * 2）通过转为 converter 转化为 Rule 规则
     */
    @Override
    public List<T> getRules(String appName) throws Exception {
        String dataId = appName + getRuleDataIdPostfix();
        String rules = configService.getConfig(dataId, NacosConfigUtil.GROUP_ID, 3000);
        log.info("Sentinel dashboard pull rules from nacos, dataId: {}, rules: {}", dataId, rules);
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return converter.convert(rules);
    }

    abstract String getRuleDataIdPostfix();

}
