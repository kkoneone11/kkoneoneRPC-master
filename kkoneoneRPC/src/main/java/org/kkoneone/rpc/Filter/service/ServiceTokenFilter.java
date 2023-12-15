package org.kkoneone.rpc.Filter.service;

import org.kkoneone.rpc.Filter.FilterData;
import org.kkoneone.rpc.Filter.ServiceBeforeFilter;
import org.kkoneone.rpc.config.RpcProperties;

import java.util.Map;

/**
 * token拦截器
 * @Author：kkoneone11
 * @name：ServiceTokenFilter
 * @Date：2023/12/11 23:25
 */
public class ServiceTokenFilter implements ServiceBeforeFilter {
    @Override
    public void doFilter(FilterData filterData) {
        final Map<String, Object> attachments = filterData.getClientAttachments();
        final Map<String, Object> serviceAttachments = RpcProperties.getInstance().getServiceAttachments();
        if (!attachments.getOrDefault("token","").equals(serviceAttachments.getOrDefault("token",""))){
            throw new IllegalArgumentException("token不正确");
        }
    }
}
