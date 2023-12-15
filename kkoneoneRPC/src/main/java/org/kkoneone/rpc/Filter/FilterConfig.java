package org.kkoneone.rpc.Filter;

import lombok.SneakyThrows;
import org.kkoneone.rpc.spi.ExtensionLoader;

import java.io.IOException;

/**
 * 拦截器配置类，用于统一管理拦截器
 * @Author：kkoneone11
 * @name：FilterConfig
 * @Date：2023/11/30 17:20
 */
public class FilterConfig {
    private static FilterChain serviceBeforeFilterChain = new FilterChain();
    private static FilterChain serviceAfterFilterChain = new FilterChain();
    private static FilterChain clientBeforeFilterChain = new FilterChain();
    private static FilterChain clientAfterFilterChain = new FilterChain();

    @SneakyThrows //可以让你在不使用try/catch块的情况下抛出被检查的异常。如果在此方法中有任何被检查的异常被抛出，Lombok将在编译时自动插入一个try/catch块来处理这些异常
    public static void initServiceFilter(){
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        //加载对应过滤器的实例
        extensionLoader.loadExtension(ServiceAfterFilter.class);
        extensionLoader.loadExtension(ServiceBeforeFilter.class);
        //获取对应类的实例并添加到过滤器当中
        serviceBeforeFilterChain.addFilter(extensionLoader.gets(ServiceBeforeFilter.class));
        serviceAfterFilterChain.addFilter(extensionLoader.gets(ServiceAfterFilter.class));
    }
    public static void initClientFilter() throws IOException, ClassNotFoundException {
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(ClientAfterFilter.class);
        extensionLoader.loadExtension(ClientBeforeFilter.class);
        clientBeforeFilterChain.addFilter(extensionLoader.gets(ClientBeforeFilter.class));
        clientAfterFilterChain.addFilter(extensionLoader.gets(ClientAfterFilter.class));
    }

    public static FilterChain getServiceBeforeFilterChain(){
        return serviceBeforeFilterChain;
    }
    public static FilterChain getServiceAfterFilterChain(){
        return serviceAfterFilterChain;
    }
    public static FilterChain getClientBeforeFilterChain(){
        return clientBeforeFilterChain;
    }
    public static FilterChain getClientAfterFilterChain(){
        return clientAfterFilterChain;
    }

}
