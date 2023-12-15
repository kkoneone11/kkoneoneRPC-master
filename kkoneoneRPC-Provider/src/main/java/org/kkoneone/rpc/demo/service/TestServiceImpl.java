package org.kkoneone.rpc.demo.service;

import org.kkoneone.rpc.annotation.RpcService;
import org.kkoneone.rpc.demo.TestService;

/**
 * @Author：kkoneone11
 * @name：TestServiceImpl
 * @Date：2023/12/15 23:37
 */
@RpcService
public class TestServiceImpl implements TestService {
    @Override
    public void test(String key) {
        System.out.println(1/0);
        System.out.println("服务提供1 test 测试成功  :" + key);
    }

    @Override
    public void test2(String key) {
        System.out.println("服务提供1 test2 测试成功  :" + key);
    }
}
