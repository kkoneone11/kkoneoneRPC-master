package org.kkoneone.rpc.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.kkoneone.rpc.common.RpcServiceNameBuilder;
import org.kkoneone.rpc.common.ServiceMeta;
import org.kkoneone.rpc.config.RpcProperties;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @Author：kkoneone11
 * @name：ZookeeperRegistry
 * @Date：2023/12/14 14:47
 */
public class ZookeeperRegistry implements RegistryService{

    //连接失败等待重试时间
    public static final int BASE_SLEEP_TIME_MS = 1000;
    //重试次数
    public static final int MAX_RETRIES = 3;
    //根路径
    public static final String ZK_BASE_PATH = "/xhy_rpc";
    //存储服务
    private final ServiceDiscovery<ServiceMeta> serviceDiscovery;

    /**
     * 启动zookeeper
     * @throws Exception
     */
    public ZookeeperRegistry() throws Exception{
        //生成RpcProperties的实例获取端口
        String registerAddr = RpcProperties.getInstance().getRegisterAddr();
        //利用Curator创建客户端 注册地址和重试策略
        CuratorFramework client = CuratorFrameworkFactory.newClient(registerAddr, new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
        //启动客户端
        client.start();
        //序列化ServiceMeta
        JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<ServiceMeta>(ServiceMeta.class );
        //利用Service Discovery构建一个服务发现实例并配置zk ZooKeeper客户端、序列化器和基本路径
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMeta.class)
                .client(client)
                .serializer(serializer)
                .basePath(ZK_BASE_PATH)
                .build();
        this.serviceDiscovery.start();
    }

    /**
     * 将服务注册进存储服务
     * @param serviceMeta
     * @throws Exception
     */
    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        //创建一个ServiceInstance
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(RpcServiceNameBuilder.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion()))
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.registerService(serviceInstance);
    }

    /**
     * 移除服务
     * @param serviceMeta
     * @throws Exception
     */
    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(serviceMeta.getServiceName())
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    /**
     * 列出所有服务
     * @param serviceName
     * @return
     * @throws Exception
     */
    private List<ServiceMeta> listServices(String serviceName) throws Exception {
        Collection<ServiceInstance<ServiceMeta>> serviceInstances = serviceDiscovery.queryForInstances(serviceName);
        List<ServiceMeta> serviceMetas = serviceInstances.stream().map(serviceMetaServiceInstance -> serviceMetaServiceInstance.getPayload()).collect(Collectors.toList());
        return serviceMetas;
    }


    @Override
    public List<ServiceMeta> discoveries(String serviceName) {
        try {
            return listServices(serviceName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }



    @Override
    public void destroy() throws IOException {
        serviceDiscovery.close();
    }
}
