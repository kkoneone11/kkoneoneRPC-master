package org.kkoneone.rpc.common.constants;

/**
 * 负载均衡策略
 * @Author：kkoneone11
 * @name：LoadBalancerRules
 * @Date：2023/12/2 13:04
 */
public interface LoadBalancerRules {

    String ConsistentHash = "consistentHash";
    String RoundRobin = "roundRobin";
}
