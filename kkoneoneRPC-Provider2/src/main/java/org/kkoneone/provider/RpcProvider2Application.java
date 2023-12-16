package org.kkoneone.provider;

import org.kkoneone.rpc.annotation.EnableProviderRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author：kkoneone11
 * @name：RpcProvider2Application
 * @Date：2023/12/16 14:58
 */
@SpringBootApplication
@EnableProviderRpc
public class RpcProvider2Application {

    public static void main(String[] args) {
        SpringApplication.run(RpcProvider2Application.class, args);
    }

}