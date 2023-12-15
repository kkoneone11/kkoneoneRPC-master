package org.kkoneone.rpc.annotation;

import org.kkoneone.rpc.provider.ProviderPostProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启服务提供方自动装配
 * @Author：kkoneone11
 * @name：EnableProviderRpc
 * @Date：2023/12/13 18:06
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ProviderPostProcessor.class)
public @interface EnableProviderRpc {
}
