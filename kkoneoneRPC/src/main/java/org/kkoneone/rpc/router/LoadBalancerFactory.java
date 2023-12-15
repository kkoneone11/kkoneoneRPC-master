package org.kkoneone.rpc.router;

import org.kkoneone.rpc.spi.ExtensionLoader;

/**
 * @Author：kkoneone11
 * @name：LoadBalancerFactory
 * @Date：2023/11/27 14:06
 */
public class LoadBalancerFactory {
    //先生成一个SPI机制实例然后根据提供的serviceLoadBalancer来决定对应负载均衡策略
    public static LoadBalancer get(String serviceLoadBalancer) throws Exception {
        return ExtensionLoader.getInstance().get(serviceLoadBalancer);

    }

    public static void init() throws Exception {
        ExtensionLoader.getInstance().loadExtension(LoadBalancer.class);
    }
}
