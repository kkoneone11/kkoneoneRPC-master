package org.kkoneone.rpc.consumer;

import org.kkoneone.rpc.Filter.FilterConfig;
import org.kkoneone.rpc.Filter.client.ClientLogFilter;
import org.kkoneone.rpc.annotation.RpcReference;
import org.kkoneone.rpc.config.RpcProperties;
import org.kkoneone.rpc.protocol.serialization.SerializationFactory;
import org.kkoneone.rpc.registry.RegistryFactory;
import org.kkoneone.rpc.router.LoadBalancerFactory;
import org.kkoneone.rpc.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * @Author：kkoneone11
 * @name：ConsumerPostProcessor
 * @Date：2023/12/5 23:58
 */
public class ConsumerPostProcessor implements BeanPostProcessor, EnvironmentAware, InitializingBean {

    private Logger logger = LoggerFactory.getLogger(ClientLogFilter.class);
    RpcProperties rpcProperties;
    /**
     * 初始化一些bean
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        SerializationFactory.init();
        RegistryFactory.init();
        LoadBalancerFactory.init();
        FilterConfig.initClientFilter();
    }

    /**
     * 从配置文件中读取配置
     * @param environment
     */
    @Override
    public void setEnvironment(Environment environment) {
        RpcProperties properties = RpcProperties.getInstance();
        PropertiesUtils.init(properties,environment);
        rpcProperties = properties;
        logger.info("读取配置文件成功");
    }


    /**
     * 服务发现 代理层注入
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //通过反射获取所有字段
        final Field[] fields = bean.getClass().getDeclaredFields();
        //查找字段上标有@RpcReference注解的字段
        for(Field field : fields){
            if(field.isAnnotationPresent(RpcReference.class)){
                RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                //获取字段的类类型
                Class<?> aClass = field.getType();
                //设置字段为可访问
                field.setAccessible(true);
                Object object = null;
                try{
                    //在运行时期创建代理对象
                    object = Proxy.newProxyInstance(
                            aClass.getClassLoader(),
                            new Class<?>[]{aClass},
                            new RpcInvokerProxy(rpcReference.serviceVersion(),rpcReference.timeout(),rpcReference.faultTolerant(),
                                    rpcReference.loadBalancer(),rpcReference.retryCount()));
                }catch (Exception e){
                    e.printStackTrace();
                }
                try{
                    //代理对象创建成功则赋值给@RpcReference进行动态代理
                    field.set(bean,object);
                    field.setAccessible(false);
                    logger.info(beanName + " field:" + field.getName() + "注入成功");
                }catch (Exception e){
                    e.printStackTrace();
                    logger.info(beanName + " field:" + field.getName() + "注入失败");
                }
            }
        }
        return bean;
    }


}
