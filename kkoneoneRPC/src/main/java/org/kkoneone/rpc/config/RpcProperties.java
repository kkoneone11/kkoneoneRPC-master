package org.kkoneone.rpc.config;

import lombok.Data;
import org.kkoneone.rpc.annotation.PropertiesField;
import org.kkoneone.rpc.annotation.PropertiesPrefix;
import org.kkoneone.rpc.common.constants.RegistryRules;
import org.kkoneone.rpc.common.constants.SerializationRules;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置信息
 * @Author：kkoneone11
 * @name：RpcProperties
 * @Date：2023/11/27 10:56
 */
@PropertiesPrefix("rpc")
@Data
public class RpcProperties {
    /**
     * netty端口  两个服务就是两个进程，进程间通信就需要端口
     */
    @PropertiesField
    private Integer port;

    /**
     * 注册中心地址
     */
    @PropertiesField
    private String registerAddr;

    /**
     * 注册中心类型
     */
    @PropertiesField
    private String registerType = RegistryRules.ZOOKEEPER;

    /**
     * 注册中心密码
     */
    @PropertiesField
    private String registerPsw;

    /**
     * 序列化
     */
    @PropertiesField
    private String serialization = SerializationRules.JSON;

    /**
     * 服务端额外配置数据
     */
    @PropertiesField("service")
    private Map<String,Object> serviceAttachments = new HashMap<>();

    /**
     * 客户端额外配置数据
     */
    @PropertiesField("client")
    private Map<String,Object> clientAttachments = new HashMap<>();

    static RpcProperties rpcProperties;

    public static RpcProperties getInstance(){
        if (rpcProperties == null){
            rpcProperties = new RpcProperties();
        }
        return rpcProperties;
    }
    private RpcProperties(){}

    public void setRegisterType(String registerType) {
        if(registerType == null || registerType.equals("")){
            registerType = RegistryRules.ZOOKEEPER;
        }
        this.registerType = registerType;
    }

    public void setSerialization(String serialization) {
        if(serialization == null || serialization.equals("")){
            serialization = SerializationRules.JSON;
        }
        this.serialization = serialization;
    }

    /**
     * 做一个能够解析任意对象属性的工具类
     * @param environment
     */
    public static void init(Environment environment){

    }

}
