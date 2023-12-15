package org.kkoneone.rpc.router;

import org.kkoneone.rpc.common.ServiceMeta;
import org.kkoneone.rpc.config.RpcProperties;
import org.kkoneone.rpc.registry.RegistryService;
import org.kkoneone.rpc.spi.ExtensionLoader;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询算法
 * @Author：kkoneone11
 * @name：RoundRobinLoadBalancer
 * @Date：2023/12/6 21:40
 */
public class RoundRobinLoadBalancer implements LoadBalancer{
    //原子类 基于内存即可，因为是服务调用方所调用的，而服务提供方可能会有多个
    private static AtomicInteger roundRobinId = new AtomicInteger(0);
    @Override
    public ServiceMetaRes select(Object[] params, String serviceName) {
        //传入注册中心实现类型来获取注册中心
        RegistryService registryService = ExtensionLoader.getInstance().get(RpcProperties.getInstance().getRegisterType());

        // 1.获取所有服务
        List<ServiceMeta> discoveries = registryService.discoveries(serviceName);
        int size = discoveries.size();
        // 2.根据当前轮询ID取余服务长度得到具体服务 每次都加一
        roundRobinId.addAndGet(1);
        //处理Integer最大值的问题
        if(roundRobinId.get() == Integer.MAX_VALUE){
            roundRobinId.set(0);
        }
        //取余然后获取当前的服务
        return ServiceMetaRes.build(discoveries.get(roundRobinId.get() % size),discoveries);
    }
}
