package org.kkoneone.rpc.consumer;

import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * @Author：kkoneone11
 * @name：RpcReferenceBean
 * @Date：2023/12/13 21:30
 */
@Deprecated //注解用于标记一个类、方法或字段已经过时，不推荐使用。这可以帮助其他开发者知道哪些部分的代码不建议再使用，以便进行更新或替换
public class RpcReferenceBean implements FactoryBean<Object> {
    private Class<?> interfaceClass;

    private String serviceVersion;

    private long timeout;

    private Object object;

    private String loadBalancerType;

    private String faultTolerantType;

    private long retryCount;

    @Override
    public Object getObject() throws Exception {
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    public void init() throws Exception {

        Object object = Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcInvokerProxy(serviceVersion,timeout,faultTolerantType,loadBalancerType,retryCount));
        this.object = object;
    }

    public void setRetryCount(long retryCount) {
        this.retryCount = retryCount;
    }

    public void setFaultTolerantType(String faultTolerantType) {
        this.faultTolerantType = faultTolerantType;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setLoadBalancerType(String loadBalancerType) {
        this.loadBalancerType = loadBalancerType;
    }
}
