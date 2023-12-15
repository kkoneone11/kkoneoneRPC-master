package org.kkoneone.rpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * 自定义RPC协议,使用泛型来自定义传入的请求
 * @Author：kkoneone11
 * @name：RpcProtocol
 * @Date：2023/11/26 9:13
 */
@Data
public class RpcProtocol<T> implements Serializable {
    //协议头
    private MsgHeader header;
    //协议体
    private T body;
}
