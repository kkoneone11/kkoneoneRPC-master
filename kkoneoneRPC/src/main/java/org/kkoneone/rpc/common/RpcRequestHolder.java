package org.kkoneone.rpc.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RPC请求连接
 * @Author：kkoneone11
 * @name：RpcRequestHolder
 * @Date：2023/11/27 9:23
 */
public class RpcRequestHolder {
    // 请求id
    //用于生成唯一的请求ID。AtomicLong是一个线程安全的类，可以在多线程环境下安全地进行操作。REQUEST_ID_GEN被初始化为0，每次需要新的请求ID时，可以调用其incrementAndGet()方法，以原子方式将当前值加1，然后返回更新后的值
    public final static AtomicLong REQUEST_ID_GEN = new AtomicLong(0);

    // 绑定并存储返回值
    //ConcurrentHashMap是一个线程安全的类，可以在多线程环境下安全地进行操作。键是请求ID，值是对应的RpcFuture对象
    public static final Map<Long, RpcFuture<RpcResponse>> REQUEST_MAP = new ConcurrentHashMap<>();
}
