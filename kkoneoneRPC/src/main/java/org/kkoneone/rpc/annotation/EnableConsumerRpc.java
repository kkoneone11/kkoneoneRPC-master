package org.kkoneone.rpc.annotation;

import org.kkoneone.rpc.consumer.ConsumerPostProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启调用方自动装配
 * @Author：kkoneone11
 * @name：EnableConsumerRpc
 * @Date：2023/12/13 19:31
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(ConsumerPostProcessor.class)
public @interface EnableConsumerRpc {
}
