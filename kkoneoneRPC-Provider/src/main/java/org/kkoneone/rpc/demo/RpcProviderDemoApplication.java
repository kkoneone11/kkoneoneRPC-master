package org.kkoneone.rpc.demo;

import org.kkoneone.rpc.annotation.EnableProviderRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author：kkoneone11
 * @name：RpcProviderDemoApplication
 * @Date：2023/12/15 23:49
 */
@SpringBootApplication
@EnableProviderRpc
public class RpcProviderDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpcProviderDemoApplication.class, args);
    }
}
