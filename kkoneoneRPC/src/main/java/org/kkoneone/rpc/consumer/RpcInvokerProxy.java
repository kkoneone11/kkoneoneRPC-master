package org.kkoneone.rpc.consumer;

import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import org.kkoneone.rpc.Filter.FilterConfig;
import org.kkoneone.rpc.Filter.FilterData;
import org.kkoneone.rpc.common.*;
import org.kkoneone.rpc.common.constants.MsgType;
import org.kkoneone.rpc.common.constants.ProtocolConstants;
import org.kkoneone.rpc.config.RpcProperties;
import org.kkoneone.rpc.protocol.MsgHeader;
import org.kkoneone.rpc.protocol.RpcProtocol;
import org.kkoneone.rpc.router.LoadBalancer;
import org.kkoneone.rpc.router.LoadBalancerFactory;
import org.kkoneone.rpc.router.ServiceMetaRes;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.kkoneone.rpc.common.constants.FaultTolerantRules.*;

/**
 * @Author：kkoneone11
 * @name：RpcInvokerProxy
 * @Date：2023/11/25 23:03
 */
@Slf4j
public class RpcInvokerProxy implements InvocationHandler {

    private String serviceVersion;
    private long timeout;
    private String loadBalancerType;
    private String faultTolerantType;
    private long retryCount;

    public RpcInvokerProxy(){}

    public RpcInvokerProxy(String serviceVersion, long timeout,String faultTolerantType,String loadBalancerType,long retryCount) throws Exception {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.loadBalancerType = loadBalancerType;
        this.faultTolerantType = faultTolerantType;
        this.retryCount = retryCount;

    }

    /**
     * 接口调用方法的时候都会走到这个invoke方法
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //1创建协议
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();

        //2构建消息头
        MsgHeader header = new MsgHeader();
        //2.1建立连接 获取请求id
        long requestId = RpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setMsgLen(ProtocolConstants.VERSION);
        header.setRequestId(requestId);
        //2.2获得序列化的字节长度
        final byte[] serialization = RpcProperties.getInstance().getSerialization().getBytes();
        header.setSerializationLen(serialization.length);
        header.setSerializations(serialization);
        //ordinal()用来获得枚举值中的位置
        header.setMsgType((byte) MsgType.REQUEST.ordinal());
        header.setStatus((byte) 0x1);
        protocol.setHeader(header);

        //3构建请求体
        RpcRequest request = new RpcRequest();
        request.setServiceVersion(this.serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setData(ObjectUtils.isEmpty(args) ? new Object[0] : args);
        request.setDataClass(request.getData().getClass());
        request.setServiceAttachments(RpcProperties.getInstance().getServiceAttachments());
        request.setClientAttachments(RpcProperties.getInstance().getClientAttachments());
        //3.1拦截器上下文
        final FilterData filterData = new FilterData(request);
        try{
            FilterConfig.getClientBeforeFilterChain().doFilter(filterData);
        }catch (Throwable e){
            throw e;
        }
        protocol.setBody(request);

        //4获取负载均衡策略
        final LoadBalancer loadBalancer = LoadBalancerFactory.get(loadBalancerType);
        //4.1获取对应服务和参数
        String serviceName = RpcServiceNameBuilder.buildServiceKey(request.getClassName(), request.getServiceVersion());
        Object[] params = {request.getData()};
        //4.2根据策略获取对应的服务节点
        final ServiceMetaRes serviceMetaRes = loadBalancer.select(params, serviceName);
        ServiceMeta curServiceMeta = serviceMetaRes.getCurServiceMeta();
        Collection<ServiceMeta> otherServiceMeta = serviceMetaRes.getOtherServiceMeta();

        //5容错机制：重试
        long count = 1;
        long retryCount = this.retryCount;
        //服务调用者
        RpcConsumer rpcConsumer = new RpcConsumer();
        //返回数据
        RpcResponse rpcResponse = null;
        while(count <= retryCount){
            //5.1处理返回数据
            //new DefaultPromise()创建了一个DefaultPromise对象，它是Netty库中的一个类，用于表示一个异步操作的结果。
            //new DefaultEventLoop()创建了一个新的事件循环，它是Netty中处理并发操作和网络事件的组件
            RpcFuture<RpcResponse> future = new RpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()),timeout);
            //5.2存储返回结果
            RpcRequestHolder.REQUEST_MAP.put(requestId,future);
            try{
                //服务调用者发送消息
                rpcConsumer.sendRequest(protocol,curServiceMeta);
                //异步等待数据返回
                rpcResponse = future.getPromise().get(future.getTimeout(), TimeUnit.MICROSECONDS);
                //如果有异常并且没有其他服务则抛出异常
                if(rpcResponse.getException()!=null && otherServiceMeta.size() == 0){
                    throw rpcResponse.getException();
                }
                if (rpcResponse.getException()!=null){
                    throw rpcResponse.getException();
                }
                log.info("rpc 调用成功, serviceName: {}",serviceName);
                try {
                    FilterConfig.getClientAfterFilterChain().doFilter(filterData);
                }catch (Throwable e){
                    throw e;
                }
                return rpcResponse.getData();
            }catch (Throwable e){
                //调用失败则重试
                String errorMsg = e.toString();
                switch (faultTolerantType){
                    // 快速失败
                    case FailFast:
                        log.warn("rpc 调用失败,触发 FailFast 策略,异常信息: {}",errorMsg);
                        return rpcResponse.getException();
                    // 故障转移
                    case Failover:
                        log.warn("rpc 调用失败,第{}次重试,异常信息:{}",count,errorMsg);
                        count++;
                        if (!ObjectUtils.isEmpty(otherServiceMeta)){
                            final ServiceMeta next = otherServiceMeta.iterator().next();
                            curServiceMeta = next;
                            otherServiceMeta.remove(next);
                        }else {
                            final String msg = String.format("rpc 调用失败,无服务可用 serviceName: {%s}, 异常信息: {%s}", serviceName, errorMsg);
                            log.warn(msg);
                            throw new RuntimeException(msg);
                        }
                        break;
                    // 忽视这次错误
                    case Failsafe:
                        return null;
                }
            }

        }

        throw new RuntimeException("rpc 调用失败，超过最大重试次数: {}" + retryCount);
    }
}
