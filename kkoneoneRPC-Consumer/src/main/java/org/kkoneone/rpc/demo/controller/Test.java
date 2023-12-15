package org.kkoneone.rpc.demo.controller;

import org.kkoneone.rpc.annotation.RpcReference;
import org.kkoneone.rpc.common.constants.FaultTolerantRules;
import org.kkoneone.rpc.common.constants.LoadBalancerRules;
import org.kkoneone.rpc.demo.Test2Service;
import org.kkoneone.rpc.demo.TestService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author：kkoneone11
 * @name：Test
 * @Date：2023/12/15 22:00
 */
@RestController
public class Test {

    @RpcReference(timeout = 10000L,faultTolerant = FaultTolerantRules.Failover,loadBalancer = LoadBalancerRules.RoundRobin)
    TestService testService;

    @RpcReference(loadBalancer = LoadBalancerRules.ConsistentHash)
    Test2Service test2Service;

    /**
     * 轮询
     * 会触发故障转移,提供方模拟异常
     * @param key
     * @return
     */
    @RequestMapping("test/{key}")
    public String test(@PathVariable String key){
        testService.test(key);
        return "test1 ok";
    }

    /**
     * 一致性哈希
     * @param key
     * @return
     */
    @RequestMapping("test2/{key}")
    public String test2(@PathVariable String key){

        return test2Service.test(key);
    }

    /**
     * 轮询,无如何异常
     * @param key
     * @return
     */
    @RequestMapping("test3/{key}")
    public String test3(@PathVariable String key){
        testService.test2(key);
        return "test2 ok";
    }
}
