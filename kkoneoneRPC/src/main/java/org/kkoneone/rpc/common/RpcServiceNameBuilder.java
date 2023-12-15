package org.kkoneone.rpc.common;

/**
 * 服务名拼接
 * @Author：kkoneone11
 * @name：RpcServiceNameBuilder
 * @Date：2023/11/27 13:54
 */
public class RpcServiceNameBuilder {
    // key: 服务名 value: 服务提供方s
    public static String buildServiceKey(String serviceName, String serviceVersion) {
        return String.join("$", serviceName, serviceVersion);
    }


}
