package org.kkoneone.rpc.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI机制
 * @Author：kkoneone11
 * @name：ExtensionLoader
 * @Date：2023/11/27 15:04
 */
public class ExtensionLoader {

    private Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    //系统端配置SPI
    private static String SYS_EXTENSION_LOADER_DIR_PREFIX = "META-INF/xrpc/";

    //客户配置SPI
    private static String DIY_EXTENSION_LOADER_DIR_PREFIX = "META-INF/rpc/";

    private static String[] prefixs = {SYS_EXTENSION_LOADER_DIR_PREFIX, DIY_EXTENSION_LOADER_DIR_PREFIX};

    //存放bean定义信息 key: 接口 value：子类
    private static Map<String, Class> extensionClassCache = new ConcurrentHashMap<>();
    //存放bean定义信息 key：接口 value：接口子类s
    private static Map<String, Map<String,Class>> extensionClassCaches = new ConcurrentHashMap<>();

    // 存放实例化的bean 是当需要用到这个bean才会进行加载到map中如果不需要的话则先不加载
    private static Map<String, Object> singletonsObject = new ConcurrentHashMap<>();


    private static ExtensionLoader extensionLoader;

    static {
        extensionLoader = new ExtensionLoader();
    }

    public static ExtensionLoader getInstance(){
        return extensionLoader;
    }

    private ExtensionLoader(){

    }
    /**
     * 根据传入的策略来获取实例bean
     * @param name
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public <V> V get(String name) {
        if (!singletonsObject.containsKey(name)) {
            try {
                singletonsObject.put(name, extensionClassCache.get(name).newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return (V) singletonsObject.get(name);
    }

    /**
     * 获取给定类的实例列表。当你需要管理不同类的单例对象缓存时，它允许你从缓存中获取特定类的所有实例
     *
     * @param clazz
     * @return
     */
    public List<Object> gets(Class clazz){
        final String name = clazz.getName();
        if(!extensionClassCaches.containsKey(name)){
            try{
                throw new ClassNotFoundException(clazz + "未找到");
            }catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }
        final Map<String, Class> stringClassMap = extensionClassCaches.get(name);
        List<Object> objects = new ArrayList<>();
        if(stringClassMap.size() > 0){
            stringClassMap.forEach((k,v)->{
                try{
                    objects.add(singletonsObject.getOrDefault(k,v.newInstance()));
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        }
        return objects;
    }


    /**
     * 根据spi机制加载bean的信息放入map  从SPI配置文件中加载指定类的
     * @param clazz
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void loadExtension(Class clazz) throws IOException, ClassNotFoundException {
        if (clazz == null) {
            throw new IllegalArgumentException("class 没找到");
        }
        //1.获取当前类的ClassLoader
        ClassLoader classLoader = this.getClass().getClassLoader();
        HashMap<String, Class> classMap = new HashMap<>();
        //2.遍历前缀列表，将每个前缀添加到类名（clazz.getName()）后面，形成SPI配置文件的路径
        for (String prefix : prefixs) {
            String spiFilePath = prefix + clazz.getName();
            //3.根据路径获得SPI配置文件的URL枚举
            Enumeration<URL> enumeration = classLoader.getResources(spiFilePath);
            //4.对于每个URL，打开一个BufferedReader来逐行读取文件
            while (enumeration.hasMoreElements()) {
                URL url = enumeration.nextElement();
                InputStreamReader inputStreamReader = null;
                inputStreamReader = new InputStreamReader(url.openStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    // SPI配置文件中的每一行都应该是key=value的格式
                    String[] lineArr = line.split("=");
                    String key = lineArr[0];
                    String name = lineArr[1];
                    final Class<?> aClass = Class.forName(name);
                    extensionClassCache.put(key, aClass);
                    classMap.put(key,aClass);
                    logger.info("加载bean key:{} , value:{}",key,name);
                }
            }
        }
        extensionClassCaches.put(clazz.getName(),classMap);
    }



}
