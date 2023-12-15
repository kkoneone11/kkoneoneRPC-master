package org.kkoneone.rpc.provider;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.kkoneone.rpc.annotation.RpcService;
import org.kkoneone.rpc.common.RpcServiceNameBuilder;
import org.kkoneone.rpc.common.ServiceMeta;
import org.kkoneone.rpc.config.RpcProperties;
import org.kkoneone.rpc.protocol.codec.RpcDecoder;
import org.kkoneone.rpc.protocol.codec.RpcEncoder;
import org.kkoneone.rpc.protocol.handler.service.RpcRequestHandler;
import org.kkoneone.rpc.protocol.handler.service.ServiceAfterFilterHandler;
import org.kkoneone.rpc.protocol.handler.service.ServiceBeforeFilterHandler;
import org.kkoneone.rpc.registry.RegistryFactory;
import org.kkoneone.rpc.registry.RegistryService;
import org.kkoneone.rpc.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务提供方后置处理器
 * @Author：kkoneone11
 * @name：ProviderPostProcessor
 * @Date：2023/12/2 12:45
 */
public class ProviderPostProcessor implements InitializingBean, BeanPostProcessor, EnvironmentAware {
    private Logger logger = LoggerFactory.getLogger(ProviderPostProcessor.class);
    RpcProperties rpcProperties;
    // 此处在linux环境下改为0.0.0.0
    private static String serverAddress = "127.0.0.1";
    private final Map<String, Object> rpcServiceMap = new HashMap<>();


    /**
     * 启动RPC服务
     * @throws InterruptedException
     */
    private void startRpcServer() throws InterruptedException {
        Integer serverPort = rpcProperties.getPort();
        //设置上级事件循环组和下级事件循环组
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try{
            //创建客户端
            ServerBootstrap bootstrap = new ServerBootstrap();
            //传入必要参数
            bootstrap.group(boss, worker)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new RpcEncoder())
                                    .addLast(new RpcDecoder())
                                    .addLast(new ServiceBeforeFilterHandler())
                                    .addLast(new RpcRequestHandler())
                                    .addLast(new ServiceAfterFilterHandler());
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            //开启管道异步获取结果
            ChannelFuture channelFuture = bootstrap.bind(this.serverAddress, serverPort).sync();
            logger.info("server addr {} started on port {}", this.serverAddress, serverPort);
            //阻塞当前线程并保持应用程序运行，直到服务器通道关闭
            channelFuture.channel().closeFuture().sync();
            //添加一个钩子函数
            Runtime.getRuntime().addShutdownHook(new Thread(() ->
            {
                logger.info("ShutdownHook execute start...");
                logger.info("Netty NioEventLoopGroup shutdownGracefully...");
                logger.info("Netty NioEventLoopGroup shutdownGracefully2...");
                boss.shutdownGracefully();
                worker.shutdownGracefully();
                logger.info("ShutdownHook execute end...");
            }, "Allen-thread"));
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {

    }


    /**
     * 服务注册
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        //通过反射获取bean的对应类
        Class<?> beanClass = bean.getClass();
        //找到bean上带有 RpcService 注解标识的类（即需要注册的类）
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if(rpcService!=null){
            //可能会有多个接口,默认选择第一个接口的接口名用作服务调用方和服务提供方找到对应接口调用
            String serviceName = beanClass.getInterfaces()[0].getName();
            if(!rpcService.serviceInterface().equals(void.class)){
                //说明rpcService对象定义了特定的服务接口则不使用第一个接口
                serviceName = rpcService.serviceInterface().getName();
            }
            String serviceVersion = rpcService.serviceVersion();
            try{
                // 服务注册
                Integer servicePort = rpcProperties.getPort();
                // 从配置文件获取注册中心来创建一个注册中心实例 ioc
                RegistryService registryService = RegistryFactory.get(rpcProperties.getRegisterType());
                ServiceMeta serviceMeta = new ServiceMeta();
                // 服务提供方地址 将服务注册到注册中心上
                   //服务端口
                serviceMeta.setServicePort(servicePort);
                   //服务地址
                serviceMeta.setServiceAddr("127.0.0.1");
                   //服务版本
                serviceMeta.setServiceVersion(serviceVersion);
                   //服务名字
                serviceMeta.setServiceName(serviceName);
                registryService.register(serviceMeta);
                // 缓存
                rpcServiceMap.put(RpcServiceNameBuilder.buildServiceKey(serviceMeta.getServiceName(),serviceMeta.getServiceVersion()), bean);
                logger.info("register server {} version {}",serviceName,serviceVersion);
            }catch (Exception e){
                logger.error("failed to register service {}",  serviceVersion, e);
            }
        }
        return bean;
    }



    /**
     * 设置配置文件
     * @param environment
     */
    @Override
    public void setEnvironment(Environment environment) {
        RpcProperties properties = RpcProperties.getInstance();
        //通过init方法将environment中的参数配置到RpcProperties中方便全局使用
        PropertiesUtils.init(properties,environment);
        rpcProperties = properties;
    }
}
