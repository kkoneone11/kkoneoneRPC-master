package org.kkoneone.rpc.router;

import org.kkoneone.rpc.common.ServiceMeta;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 服务节点
 * @Author：kkoneone11
 * @name：ServiceMetaRes
 * @Date：2023/12/1 19:43
 */
public class ServiceMetaRes {

    // 当前服务节点
    private ServiceMeta curServiceMeta;

    // 剩余服务节点 方便进行容错服务选择
    private Collection<ServiceMeta> otherServiceMeta;

    public Collection<ServiceMeta> getOtherServiceMeta() {
        return otherServiceMeta;
    }

    public ServiceMeta getCurServiceMeta() {
        return curServiceMeta;
    }

    public static ServiceMetaRes build(ServiceMeta curServiceMeta, Collection<ServiceMeta> otherServiceMeta){
        final ServiceMetaRes serviceMetaRes = new ServiceMetaRes();
        serviceMetaRes.curServiceMeta = curServiceMeta;
        // 如果只有一个服务
        if(otherServiceMeta.size() == 1){
            otherServiceMeta = new ArrayList<>();
        }else{
            otherServiceMeta.remove(curServiceMeta);
        }
        serviceMetaRes.otherServiceMeta = otherServiceMeta;
        return serviceMetaRes;
    }
}
