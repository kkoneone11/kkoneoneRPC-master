package org.kkoneone.rpc.common.constants;

/**
 * @Author：kkoneone11
 * @name：FaultTolerantRules
 * @Date：2023/12/1 23:45
 */
public interface FaultTolerantRules {
    //故障转移
    String Failover = "failover";
    //快速失败
    String FailFast = "failFast";
    //忽视这次错误
    String Failsafe = "failsafe";
}
