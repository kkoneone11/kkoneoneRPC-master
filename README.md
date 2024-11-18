以SPI为基础架构搭建高拓展高可用的RPC。

模块有： 注册中心 代理层 路由层 容错层 协议层 拦截器层 SPI 业务线程池

软件架构
SpringBoot + Netty

使用说明
将kkoneone-Rpc 进行install，再将interface进行install
在consumer和provider 中进行配置注册中心相关配置
启动consumer和provider1/2 模块，进行http请求进行测试就行了
如需进行拓展，参考consumer中的SPI机制即可


演示使用教程：https://www.yuque.com/kkoneone11/kgms6n/gefnf769ccv92rcz?singleDoc# 《RPC项目演示》
