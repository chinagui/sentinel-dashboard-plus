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

import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class AbstractRuleNacosPublisher<T> implements DynamicRulePublisher<List<T>> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigService configService;
    @Autowired
    private Converter<List<T>, String> converter;

    /**
     * 通过 configService 的 publishConfig() 方法将 rules 发布到 nacos
     */
    @Override
    public void publish(String app, List<T> rules) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        String dataId = app + getRuleDataIdPostfix();
        log.info("Sentinel dashboard push rules to nacos, dataId {}, rules: {}", dataId, rules);
        configService.publishConfig(dataId, NacosConfigUtil.GROUP_ID, converter.convert(rules));
    }

    abstract String getRuleDataIdPostfix();
}
