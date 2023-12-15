package org.kkoneone.rpc.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author：kkoneone11
 * @name：RpcResponse
 * @Date：2023/11/27 9:28
 */
@Data
public class RpcResponse implements Serializable {
    private Object data;
    private Class dataClass;
    private String message;
    private Exception exception;
}
