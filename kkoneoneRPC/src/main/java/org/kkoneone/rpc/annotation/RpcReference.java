package org.kkoneone.rpc.annotation;

import org.kkoneone.rpc.common.constants.FaultTolerantRules;
import org.kkoneone.rpc.common.constants.LoadBalancerRules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务调用方注解 表明接口要被服务发现或者服务注册使用
 * @Author：kkoneone11
 * @name：RpcReference
 * @Date：2023/12/2 12:59
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RpcReference {
    /**
     * 版本
     * @return
     */
    String serviceVersion() default "1.0";

    /**
     * 超时时间
     * @return
     */
    long timeout() default 5000;

    /**
     * 可选的负载均衡:consistentHash,roundRobin...
     * {@link LoadBalancerRules}
     * @return
     */
    String loadBalancer() default LoadBalancerRules.RoundRobin;

    /**可选的容错策略:failover,failFast,failsafe...
     * {@link FaultTolerantRules}
     * @return
     */
    String faultTolerant() default FaultTolerantRules.FailFast;

    /**
     * 重试次数
     * @return
     */
    long retryCount() default 3;
}
