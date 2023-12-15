package org.kkoneone.rpc.registry;

import org.kkoneone.rpc.common.ServiceMeta;

import java.io.IOException;
import java.util.List;

/**
 * 注册服务接口
 * @Author：kkoneone11
 * @name：RegistryService
 * @Date：2023/12/3 9:51
 */
public interface RegistryService {

    /**
     * 服务注册
     * @param serviceMeta
     * @throws Exception
     */
    void register(ServiceMeta serviceMeta) throws Exception;
    /**
     * 服务注销
     * @param serviceMeta
     * @throws Exception
     */
    void unRegister(ServiceMeta serviceMeta) throws Exception;
    /**
     * 获取 serviceName 下的所有服务
     * @param serviceName
     * @return
     */
    List<ServiceMeta> discoveries(String serviceName);
    /**
     * 关闭
     * @throws IOException
     */
    void destroy() throws IOException;
}
