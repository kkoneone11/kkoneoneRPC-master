package org.kkoneone.rpc.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * RPC请求类
 * @Author：kkoneone11
 * @name：RpcRequest
 * @Date：2023/11/26 9:39
 */
@Data
public class RpcRequest implements Serializable {
    //服务版本号，用于处理同一个接口有多个实现类的情况
    private String serviceVersion;
    //要调用的远程服务的类名
    private String className;
    //要调用的远程服务的方法名
    private String methodName;
    //调用远程方法时传递的参数
    private Object[] params;
    //这些参数的类型，这是必要的，因为要通过反射来调用方法，需要知道参数的确切类型
    private Class<?>[] parameterTypes;
    //请求数据
    private Object data;
    //请求数据的类
    private Class dataClass;
    //服务端额外配置数据
    private Map<String,Object> serviceAttachments;
    //客户端额外配置数据
    private Map<String,Object> clientAttachments;
}
