package org.kkoneone.rpc.utils;

import org.kkoneone.rpc.annotation.PropertiesField;
import org.kkoneone.rpc.annotation.PropertiesPrefix;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;

/**
 * @Author：kkoneone11
 * @name：PropertiesUtils
 * @Date：2023/12/2 13:16
 */
public class PropertiesUtils {
    /**
     * 根据对象中的配置匹配配置文件
     * @param o 对应类
     * @param environment 配置参数
     */
    public static void init(Object o, Environment environment){
        //通过反射获取对应类
        final Class<?> aClass = o.getClass();
        //获取类的注解前缀
        PropertiesPrefix prefixAnnotation = aClass.getAnnotation(PropertiesPrefix.class);
        if(prefixAnnotation == null){
            throw new NullPointerException(aClass + "@PropertiesPrefix 不存在");
        }
        String prefix = prefixAnnotation.value();
        // 前缀参数矫正
        if (!prefix.contains(".")){
            prefix += ".";
        }
        //遍历对象中的字段
        for(Field field : aClass.getDeclaredFields()){
            final PropertiesField fieldAnnotation = field.getAnnotation(PropertiesField.class);
            if(fieldAnnotation == null) continue;
            String fieldValue = fieldAnnotation.value();
            if(fieldValue == null || fieldValue.equals("")){
                fieldValue = convertToHyphenCase(field.getName());
            }
            try{
                //当字段访问权限为private时候设置true为告诉jvm我想访问
                field.setAccessible(true);
                final Class<?> type = field.getType();
                //拦截对应类对其塞入额外的参数
                final Object value = PropertyUtil.handle(environment, prefix + fieldValue, type);
                if(value == null)continue;
                //填充字段
                field.set(o,value);
            }catch (IllegalAccessException e){
                e.printStackTrace();
            }
            field.setAccessible(false);
        }

    }

    /**
     * 将输入字符串转换为连字符如"HelloWorld" -> "-hello-world"
     * @param input
     * @return
     */
    public static String convertToHyphenCase(String input){
        StringBuilder output = new StringBuilder();
        for(int i = 0; i < input.length(); i++){
            char c = input.charAt(i);
            if (Character.isUpperCase(c)) {
                output.append('-');
                output.append(Character.toLowerCase(c));
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }


}
