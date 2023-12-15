package org.kkoneone.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务提供方
 * @Author：kkoneone11
 * @name：RpcService
 * @Date：2023/12/2 23:16
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {
    /**
     * 指定实现方,默认为实现接口中第一个
     * @return
     */
    Class<?> serviceInterface() default void.class;

    /**
     * 版本
     * @return
     */
    String serviceVersion() default "1.0";
}
