package org.kkoneone.provider.service;

import org.kkoneone.rpc.annotation.RpcService;
import org.kkoneone.rpc.demo.Test2Service;

/**
 * @Author：kkoneone11
 * @name：Test2ServiceImpl
 * @Date：2023/12/16 14:57
 */
@RpcService
public class Test2ServiceImpl implements Test2Service {
    @Override
    public String test(String key) {
        System.out.println("服务提供2 test2 测试成功 :" + key);
        return key;
    }

    @Override
    public void test2(String key) {

    }
}