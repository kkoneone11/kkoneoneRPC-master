package org.kkoneone.rpc.poll;


import com.sun.org.slf4j.internal.LoggerFactory;
import io.netty.channel.ChannelHandlerContext;
import org.kkoneone.rpc.common.RpcRequest;
import org.kkoneone.rpc.common.RpcResponse;
import org.kkoneone.rpc.common.RpcServiceNameBuilder;
import org.kkoneone.rpc.common.constants.MsgStatus;
import org.kkoneone.rpc.common.constants.MsgType;
import org.kkoneone.rpc.protocol.MsgHeader;
import org.kkoneone.rpc.protocol.RpcProtocol;
import org.slf4j.Logger;

import org.springframework.cglib.reflect.FastClass;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 线程池工厂
 * @Author：kkoneone11
 * @name：ThreadPollFactory
 * @Date：2023/12/4 15:03
 */
public class ThreadPollFactory {

    private static Logger logger = (Logger) LoggerFactory.getLogger(ThreadPollFactory.class);

    //快请求
    private static ThreadPoolExecutor fastPoll;

    //慢请求
    private static ThreadPoolExecutor slowPoll;

    //慢请求映射
    private static volatile ConcurrentHashMap<String, AtomicInteger> slowTaskMap = new ConcurrentHashMap<>();

    //目前可执行的核数
    private static int corSize = Runtime.getRuntime().availableProcessors();

    //缓存服务 该缓存放这里不太好,应该作一个统一 Config 进行管理
    private static Map<String, Object> rpcServiceMap;

    //静态代码块初始化数据
    static{
        slowPoll = new ThreadPoolExecutor(corSize / 2, corSize , 60L,
                TimeUnit.SECONDS,
                //线程池的任务队列，用于存放待执行的任务
                new LinkedBlockingDeque<>(2000),
                //线程工厂，用于创建新线程并且设置为守护线程
                r->{
                    Thread thread = new Thread(r);
                    thread.setName("slow poll-"+r.hashCode());
                    thread.setDaemon(true);
                    return thread;
                });
        fastPoll = new ThreadPoolExecutor(corSize, corSize*2, 60L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000),
                r->{
                    Thread thread = new Thread(r);
                    thread.setName("fast poll-"+r.hashCode());
                    thread.setDaemon(true);
                    return thread;
                });
        startClearMonitor();
    }

    private ThreadPollFactory(){}

    public static void setRpcServiceMap(Map<String, Object> rpcMap){
        rpcServiceMap = rpcMap;
    }

    /**
     * 清理慢请求
     */
    private static void startClearMonitor(){
        //创建了一个单线程的定时任务执行器 5分钟后执行，然后每隔5分钟执行一次
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()->{
            slowTaskMap.clear();
        },5,5,TimeUnit.MINUTES);
    }

    public static void submitRequest(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol){
        //取出协议体
        final RpcRequest request  = protocol.getBody();
        //拼装key 类名+方法+服务版本
        String key = request.getClassName() + request.getMethodName() + request.getServiceVersion();
        //快请求赋值给poll
        ThreadPoolExecutor poll = fastPoll;
        //看慢请求映射中是否有缓存对应的key且初始值 存在则将表明是个慢请求则poll传给slowPoll
        if(slowTaskMap.containsKey(key) && slowTaskMap.get(key).intValue() >= 10){
            poll = slowPoll;
        }
        //线程池执行任务
        poll.submit(()->{
            //组装一个新的rpc协议
            RpcProtocol<RpcResponse> resProtocol = new RpcProtocol<>();
            //取出协议头
            final MsgHeader header = protocol.getHeader();
            //新建RpcResponse
            RpcResponse response = new RpcResponse();
            long startTime = System.currentTimeMillis();
            //发送请求
            try{
                //处理返回结果
                final Object result = submit(ctx, protocol);
                //设置返回体
                response.setData(result);
                //返回数据类别
                response.setDataClass(result.getClass());
                //返回状态
                header.setStatus((byte) MsgStatus.SUCCESS.ordinal());
            }catch (Exception e){
                header.setStatus((byte) MsgStatus.FAILED.ordinal());
                response.setException(e);
                logger.error("process request {} error", header.getRequestId(), e);
            }finally {
                //计算请求耗费时长 超过1000的则加入慢请求映射中
                long cost = System.currentTimeMillis() - startTime;
                System.out.println("cost time:" + cost);
                if(cost > 1000){
                    final AtomicInteger timeOutCount = slowTaskMap.putIfAbsent(key, new AtomicInteger(1));
                    if (timeOutCount!=null){
                        timeOutCount.incrementAndGet();
                    }
                }
            }
            resProtocol.setHeader(header);
            resProtocol.setBody(response);
            logger.info("执行成功: {},{},{},{}",Thread.currentThread().getName(),request.getClassName(),request.getMethodName(),request.getServiceVersion());
            //将协议写到管道里
            ctx.fireChannelRead(resProtocol);
        });

    }

    /**
     *
     * @param ctx
     * @param protocol
     * @return
     * @throws Exception
     */
    private static Object submit(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception{
        MsgHeader header = protocol.getHeader();
        header.setMsgType((byte) MsgType.RESPONSE.ordinal());
        final RpcRequest request = protocol.getBody();
        // 执行具体业务
        return handle(request);
    }

    /**
     * 调用invoke方法处理请求
     * @param request
     * @return
     * @throws Exception
     */
    private static Object handle(RpcRequest request) throws Exception {
        //组装服务key
        String serviceKey = RpcServiceNameBuilder.buildServiceKey(request.getClassName(), request.getServiceVersion());
        //从缓存中获取服务信息
        Object serviceBean = rpcServiceMap.get(serviceKey);
        if(serviceBean == null){
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }
        //获取服务提供方信息并创建
        //通过反射获取类实例
        Class<?> serviceClass = serviceBean.getClass();
        //获取请求名
        String methodName = request.getMethodName();
        //获取请求的参数
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = {request.getData()};
        FastClass fastClass = FastClass.create(serviceClass);
        int methodIndex = fastClass.getIndex(methodName, parameterTypes);

        // 调用invoke方法并返回结果
        return fastClass.invoke(methodIndex, serviceBean, parameters);
    }

}
