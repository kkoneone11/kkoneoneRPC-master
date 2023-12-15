package org.kkoneone.rpc.common;

import io.netty.util.concurrent.Promise;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author：kkoneone11
 * @name：RpcFuture
 * @Date：2023/11/27 9:37
 */
@Data //自动为类的所有字段生成getter和setter方法，以及equals(), hashCode(), toString()等方法
@NoArgsConstructor //生成一个无参的构造函数
@AllArgsConstructor //生成一个全参的构造函数
public class RpcFuture<T> {
    //用于处理异步操作，它代表一个尚未完成但预期会完成的操作
    private Promise<T> promise;
    //等待RPC响应的最长时间
    private long timeout;
}
