package org.kkoneone.rpc.protocol.serialization;

import org.kkoneone.rpc.spi.ExtensionLoader;

/**
 * 序列化工厂
 * @Author：kkoneone11
 * @name：SerializationFactory
 * @Date：2023/11/28 17:52
 */
public class SerializationFactory {
    //先生成一个SPI机制实例然后根据提供serializationr来决定对应的序列化方式
    public static RpcSerialization get(String serialization) throws Exception {

        return ExtensionLoader.getInstance().get(serialization);

    }

    public static void init() throws Exception {
        ExtensionLoader.getInstance().loadExtension(RpcSerialization.class);
    }

}
