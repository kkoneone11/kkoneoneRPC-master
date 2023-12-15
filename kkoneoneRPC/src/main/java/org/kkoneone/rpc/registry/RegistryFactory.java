package org.kkoneone.rpc.registry;

import org.kkoneone.rpc.spi.ExtensionLoader;

/**
 * 注册工厂
 * @Author：kkoneone11
 * @name：RegistryFactory
 * @Date：2023/12/3 9:45
 */
public class RegistryFactory {

    //都从这个工厂中获取对应的注册bean
    public static RegistryService get(String registryService) throws Exception {
        return ExtensionLoader.getInstance().get(registryService);
    }

    /**
     * 加载SPI扩展
     * @throws Exception
     */
    public static void init() throws Exception {
        ExtensionLoader.getInstance().loadExtension(RegistryService.class);
    }
}
