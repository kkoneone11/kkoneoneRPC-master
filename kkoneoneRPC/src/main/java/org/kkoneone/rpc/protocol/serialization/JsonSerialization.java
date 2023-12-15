package org.kkoneone.rpc.protocol.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kkoneone.rpc.common.RpcRequest;
import org.kkoneone.rpc.common.RpcResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @Author：kkoneone11
 * @name：JsonSerialization
 * @Date：2023/12/7 16:35
 */
public class JsonSerialization implements RpcSerialization{

    private final static ObjectMapper MAPPER;

    static{
        //JsonInclude.Include 是 Jackson 库中的一个枚举类型，用于指定在序列化 Java 对象为 JSON 时，哪些属性应该包含在输出的 JSON 中
        //- ALWAYS: 总是包含该属性
        //- NON_NULL: 只有当属性值不为 null 时才包含
        //- NON_ABSENT: 只有当属性值不为 Optional.empty() 时才包含
        //- NON_EMPTY: 只有当属性值不为 null 且不为空时才包含
        //- NON_DEFAULT: 只有当属性值不等于默认值时才包含
        //- USE_DEFAULTS: 使用默认的包含规则
        MAPPER = generateMapper(JsonInclude.Include.ALWAYS);
    }

    private static ObjectMapper generateMapper(JsonInclude.Include include){
        com.fasterxml.jackson.databind.ObjectMapper customMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        customMapper.setSerializationInclusion(include);
        customMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        customMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        customMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        return customMapper;
    }


    /**
     * 序列化
     * @param obj
     * @param <T>
     * @return
     * @throws IOException
     */
    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        //如果是string类型则直接转化为byte流
        //如果不是则按照utf-8的格式转化成byte流
        return obj instanceof String ? ((String) obj).getBytes() : MAPPER.writeValueAsString(obj).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 反序列化 解决jackson在反序列化对象时为LinkedHashMap
     * @param data
     * @param clz
     * @param <T>
     * @return
     * @throws IOException
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) throws IOException {
        //将byte数组反序列化为指定类型的对象
        final T t = MAPPER.readValue(data, clz);
        //先判断类型
        //为RpcRequest
        if(clz.equals(RpcRequest.class)){
            //将t转化
            RpcRequest rpcRequest = (RpcRequest) t;
            rpcRequest.setData(convertRes(rpcRequest.getData(),rpcRequest.getDataClass()));
            return (T) rpcRequest;
         //否则为RpcResponse
        }else{
            RpcResponse rpcResponse = (RpcResponse) t;
            rpcResponse.setData(convertRes(rpcResponse.getData(),rpcResponse.getDataClass()));
            return (T) rpcResponse;
        }

    }

    public Object convertReq(Object data,Class clazz){
        final LinkedHashMap map = (LinkedHashMap)((ArrayList) data).get(0);
        return convert(clazz,map);
    }

    public Object convertRes(Object data,Class clazz){
        final  LinkedHashMap map = (LinkedHashMap) ((ArrayList)data).get(0);
        return convert(clazz,map);
    }

    public Object convert(Class clazz,LinkedHashMap map){
        //额外处理对象
        final Class dataClass = clazz;
        try{
            //用类建一个实例
            Object o = dataClass.newInstance();
            //循环遍历map
            map.forEach((k,v)->{
                //动态地设置对象 o 的字段值
                try {
                    final Field field = dataClass.getDeclaredField(String.valueOf(k));
                    if (v!=null && v.getClass().equals(LinkedHashMap.class)){
                        v = convert(field.getType(),(LinkedHashMap) v);
                    }
                    field.setAccessible(true);
                    field.set(o,v);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            });
            return o;
        }catch (InstantiationException | IllegalAccessException e){
            e.printStackTrace();
        }
        return null;
    }
}
