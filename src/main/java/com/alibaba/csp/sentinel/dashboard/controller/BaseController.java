package com.alibaba.csp.sentinel.dashboard.controller;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BaseController<T extends RuleEntity,
        D extends DynamicRuleProvider<List<T>>,
        S extends DynamicRulePublisher<List<T>>> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected SentinelApiClient sentinelApiClient;
    @Autowired
    protected InMemoryRuleRepositoryAdapter<T> repository;

    @Autowired
    protected D ruleProvider;
    @Autowired
    protected S rulePublisher;

    public void publishRules(String app, String ip, Integer port) throws Exception {
        List<T> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));
        rulePublisher.publish(app, rules);
    }

}
