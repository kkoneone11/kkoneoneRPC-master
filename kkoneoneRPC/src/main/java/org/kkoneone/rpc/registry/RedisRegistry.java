package org.kkoneone.rpc.registry;

import com.alibaba.fastjson.JSON;
import org.kkoneone.rpc.common.RpcServiceNameBuilder;
import org.kkoneone.rpc.common.ServiceMeta;
import org.kkoneone.rpc.config.RpcProperties;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * redis注册中心
 * @Author：kkoneone11
 * @name：RedisRegistry
 * @Date：2023/12/13 21:43
 *
 *
 * 思路：
 *  *      使用集合保存所有服务节点信息
 *  * 服务启动：节点使用了redis作为注册中心后，将自身信息注册到redis当中(ttl：10秒)，并开启定时任务，ttl/2。
 *  * 定时任务用于检测各个节点的信息，如果发现节点的时间 < 当前时间，则将节点踢出，如果没有发现，则续签自身节点
 *  * 将节点踢出后，从服务注册表中找到对应key删除该节点的下的服务数据信息
 *  *
 *  * ttl :10秒
 *  * 定时任务为ttl/2
 *  *节点注册后启动心跳检测，检测服务注册的key集合，如果有服务到期，则删除,自身的服务则续签
 *  * 服务注册后将服务注册到redis以及保存到自身的服务注册key集合，供心跳检测
 *  *
 *  * 如果有节点宕机，则其他服务会检测的，如果服务都宕机，则ttl会进行管理
 */
public class RedisRegistry implements RegistryService{

    private JedisPool jedispool;

    //唯一标识当前服务节点
    private String UUID;

    //ttl 10s
    private static final int ttl = 10 * 1000;

    //存储服务 一个key由ServiceMetaName和version组成 通常有多个ServiceMeta
    private Set<String> serviceMap =  new HashSet<>();

    //新建一个定时任务方便心跳检测调用
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * 注册当前服务,将当前服务ip，端口，时间注册到redis当中，并且开启定时任务
     * 使用集合存储服务节点信息
     */
    public RedisRegistry(){
        //获取一个RpcProperties实例
        RpcProperties properties = RpcProperties.getInstance();
        //从RpcProperties实例中获取注册地址参数
        String[] split = properties.getRegisterAddr().split("-");
        //创建poolConfig
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        //设置一些参数
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        //new一个Jedis
        this.jedispool = new JedisPool(poolConfig, split[0], Integer.valueOf(split[1]));
        //设置uuid
        this.UUID = java.util.UUID.randomUUID().toString();
        //开始心跳检测
        heartbeat();

    }

    public Jedis getJedis(){
        //从池中获取jedis
        Jedis jedis = jedispool.getResource();
        RpcProperties properties = RpcProperties.getInstance();
        //如果RpcProperties配置中属性不为空则使用RpcProperties配置中提供的密码对连接到Redis服务器用auth方法进行身份验证
        if(properties != null){
            jedis.auth(properties.getRegisterPsw());
        }
        return jedis;
    }


    /**
     * 服务节点轮询检测服务信息 心跳检测定时任务
     */
    public void heartbeat(){
        int sch = 5;
        scheduledExecutorService.scheduleWithFixedDelay(()->{
            //遍历存储服务
            for (String key : serviceMap) {
                //根据key获取服务节点
                List<ServiceMeta> serviceNodes = listServices(key);
                //获取服务节点的迭代器
                Iterator<ServiceMeta> iterator = serviceNodes.iterator();
                //查询服务及诶点的过期时间是否<当前时间，如果小于则有权将节点下的服务信息都删除然后给自己续签
                while (iterator.hasNext()){
                    ServiceMeta node = iterator.next();
                    //删除过期服务
                    if(node.getEndTime() < new Date().getTime()){
                        iterator.remove();
                    }
                    //续签
                    if(node.getUUID().equals(this.UUID)){
                        node.setEndTime(node.getEndTime()+ttl/2);
                    }
                }
                //重新加载服务
                if(!ObjectUtils.isEmpty(serviceNodes)){
                    loadService(key,serviceNodes);
                }
            }
        },sch,sch, TimeUnit.SECONDS);
    }

    /**
     * 重新加载服务
     * @param serviceMetas
     */
    private void loadService(String key,List<ServiceMeta> serviceMetas) {
        //原子性操作
        String script = "redis.call('DEL', KEYS[1])\n" +
                "for i = 1, #ARGV do\n" +
                "   redis.call('RPUSH', KEYS[1], ARGV[i])\n" +
                "end \n"+
                "redis.call('EXPIRE', KEYS[1],KEYS[2])";
        List<String> keys = new ArrayList<>();
        keys.add(key);
        keys.add(String.valueOf(10));
        List<String> values = serviceMetas.stream().map(o -> JSON.toJSONString(o)).collect(Collectors.toList());
        Jedis jedis = getJedis();
        jedis.eval(script,keys,values);
        jedis.close();
    }

    private List<ServiceMeta> listServices(String key) {
        //获取当前redis
        Jedis jedis = getJedis();
        //根据key列出所有服务
        List<String> list = jedis.lrange(key, 0, -1);
        List<ServiceMeta> serviceMetas = null;
        if (!list.isEmpty()){
            //利用JSON工具将String转化为ServiceMeta
           serviceMetas = list.stream().map(o -> JSON.parseObject(o, ServiceMeta.class)).collect(Collectors.toList());
        }
        //关闭redis
        jedis.close();

        return serviceMetas;
    }


    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        //组装key
        String key = RpcServiceNameBuilder.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion());
        //添加key到ServicMap中
        if(!serviceMap.contains(key)){
            serviceMap.add(key);
        }
        //对元服务再进行封装UUID和结束时间
        serviceMeta.setUUID(UUID);
        serviceMeta.setEndTime(new Date().getTime());
        //创建jedis将服务加到redis中
        Jedis jedis = new Jedis();
        //编写script
        String script = "redis.call('RPUSH', KEYS[1], ARGV[1])\n" +
                "redis.call('EXPIRE', KEYS[1], ARGV[2])";
        //组装values
        List<String> value = new ArrayList<>();
        value.add(JSON.toJSONString(serviceMeta));
        value.add(String.valueOf(10));
        jedis.eval(script,Collections.singletonList(key),value);
        //关闭redis
        jedis.close();
    }

    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {

    }

    @Override
    public List<ServiceMeta> discoveries(String serviceName) {
        return listServices(serviceName);
    }

    @Override
    public void destroy() throws IOException {

    }


    // TODO:
    //  拓展：服务调用方要调用服务提供方都得找注册中心的话就容易形成大io流
    //  因此可以设置一个本地缓存，注册中心并且需要实现一个钩子函数，当服务挂掉的时候也要去本地缓存删除对应缓存

}
