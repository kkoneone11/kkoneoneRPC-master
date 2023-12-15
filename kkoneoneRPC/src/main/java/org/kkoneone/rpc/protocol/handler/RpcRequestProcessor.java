package org.kkoneone.rpc.protocol.handler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 业务线程池
 * @Author：kkoneone11
 * @name：RpcRequestProcessor
 * @Date：2023/12/13 17:21
 */
public class RpcRequestProcessor {
    private static ThreadPoolExecutor threadPoolExecutor;

    public static void submitRequest(Runnable task){
        if (threadPoolExecutor == null) {
            synchronized (RpcRequestProcessor.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor =
                            new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000));
                }
            }
        }
        threadPoolExecutor.submit(task);
    }
}
