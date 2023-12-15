package org.kkoneone.rpc.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 服务元数据 服务节点
 * @Author：kkoneone11
 * @name：ServiceMeta
 * @Date：2023/11/27 14:17
 */
@Data
public class ServiceMeta implements Serializable {
    //服务名
    private String serviceName;
    //服务版本
    private String serviceVersion;
    //服务地址
    private String serviceAddr;
    //服务端口
    private int servicePort;
    /**
     * 关于redis注册中心的属性
     */
    private long endTime;

    private String UUID;

    /**
     * 故障转移需要移除不可用服务
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceMeta that = (ServiceMeta) o;
        return servicePort == that.servicePort &&
                Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(serviceVersion, that.serviceVersion) &&
                Objects.equals(serviceAddr, that.serviceAddr) &&
                Objects.equals(UUID, that.UUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, serviceVersion, serviceAddr, servicePort, UUID);
    }



}
