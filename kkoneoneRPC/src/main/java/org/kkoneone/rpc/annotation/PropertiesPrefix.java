package org.kkoneone.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 前缀
 * @Author：kkoneone11
 * @name：PropertiesPrefix
 * @Date：2023/11/30 13:18
 */

@Retention(RetentionPolicy.RUNTIME) //指定注解应在运行时保留。这意味着可以在运行时使用反射来检查它。
@Target(ElementType.TYPE) //指定该注解可以应用于类型（类，接口，枚举）
public @interface PropertiesPrefix {
    String value();
}
