package org.kkoneone.rpc.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author：kkoneone11
 * @name：RpcConsumerDemoApplication
 * @Date：2023/12/15 21:57
 */
@SpringBootApplication
//@EnableConsumerRpc //注入消费者
public class RpcConsumerDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpcConsumerDemoApplication.class, args);
    }
}
