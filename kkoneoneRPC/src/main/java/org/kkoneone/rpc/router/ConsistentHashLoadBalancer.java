package org.kkoneone.rpc.router;

import org.kkoneone.rpc.common.ServiceMeta;
import org.kkoneone.rpc.config.RpcProperties;
import org.kkoneone.rpc.registry.RegistryService;
import org.kkoneone.rpc.spi.ExtensionLoader;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性哈希
 * @Author：kkoneone11
 * @name：ConsistentHashLoadBalancer
 * @Date：2023/12/6 23:10
 */
public class ConsistentHashLoadBalancer implements LoadBalancer{

    //物理节点映射的虚拟节点,为了解决哈希倾斜
    private final static int VIRTUAL_NODE_SIZE = 10;
    private final static String VIRTUAL_NODE_SPLIT = "$";

    @Override
    public ServiceMetaRes select(Object[] params, String serviceName) {
        //获取注册中心
        RegistryService registryService = ExtensionLoader.getInstance().get(RpcProperties.getInstance().getRegisterType());
        //根据注册中心获取服务
        List<ServiceMeta> discoveries = registryService.discoveries(serviceName);
        final ServiceMeta curServiceMeta = allocateNode(makeConsistentHashRing(discoveries), params[0].hashCode());
        return ServiceMetaRes.build(curServiceMeta,discoveries);
    }


    /**
     * 选择节点
     * @param ring
     * @param hashCode
     * @return
     */
    private ServiceMeta allocateNode(TreeMap<Integer, ServiceMeta> ring, int hashCode){
        //获取最近哈希环上节点位置
        Map.Entry<Integer, ServiceMeta> entry = ring.ceilingEntry(hashCode);
        if(entry == null){
            //如果没有则找最小节点
            entry = ring.firstEntry();
        }
        return entry.getValue();
    }

    /**
     * 将所有服务实例添加到一致性哈希环上，并生成虚拟节点
     * 这里每次调用都需要构建哈希环是为了扩展(服务提供方)
     * @param servers 服务实例列表
     * @return 一致性哈希环
     */
    private TreeMap<Integer, ServiceMeta> makeConsistentHashRing(List<ServiceMeta> servers){
        TreeMap<Integer, ServiceMeta> ring = new TreeMap<>();
        for(ServiceMeta instance : servers){
            for(int i = 0; i < VIRTUAL_NODE_SIZE; i++){
                ring.put((buildServiceInstanceKey(instance) + VIRTUAL_NODE_SPLIT + i).hashCode(), instance);
            }
        }
        return ring;
    }

    /**
     * 根据服务实例信息构建缓存键
     * @param serviceMeta
     * @return
     */
    private String buildServiceInstanceKey(ServiceMeta serviceMeta){
        return String.join(":",serviceMeta.getServiceAddr(),String.valueOf(serviceMeta.getServicePort()));
    }
}
