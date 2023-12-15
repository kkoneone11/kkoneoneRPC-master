package org.kkoneone.rpc.protocol.serialization;

import java.io.IOException;

/**
 * 序列化接口
 * @Author：kkoneone11
 * @name：RpcSerialization
 * @Date：2023/11/28 18:03
 */
public interface RpcSerialization {
    //序列化方法
    <T> byte[] serialize(T obj) throws IOException;

    //反序列化方法
    <T> T deserialize(byte[] data,Class<T> clz) throws IOException;

}
