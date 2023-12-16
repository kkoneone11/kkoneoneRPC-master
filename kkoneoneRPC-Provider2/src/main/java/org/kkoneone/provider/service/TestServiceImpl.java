package org.kkoneone.provider.service;

import org.kkoneone.rpc.annotation.RpcService;
import org.kkoneone.rpc.demo.TestService;

/**
 * @Author：kkoneone11
 * @name：TestServiceImpl
 * @Date：2023/12/16 14:57
 */
@RpcService
public class TestServiceImpl implements TestService {
    @Override
    public void test(String key) {
        System.out.println("服务提供2 test 测试成功  :" + key);
    }

    @Override
    public void test2(String key) {
        System.out.println("服务提供2 tes2 测试成功  :" + key);
    }
}