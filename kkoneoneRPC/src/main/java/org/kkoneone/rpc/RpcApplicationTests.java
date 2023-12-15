package org.kkoneone.rpc;

import java.util.ServiceLoader;

/**
 * @Author：kkoneone11
 * @name：RpcApplicationTests
 * @Date：2023/12/12 11:59
 */
public class RpcApplicationTests {
    public static void main(String[] args) {
        final ServiceLoader<ServiceLoader> load = ServiceLoader.load(ServiceLoader.class);

    }
}
