package org.kkoneone.rpc.consumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.kkoneone.rpc.common.RpcRequest;
import org.kkoneone.rpc.common.ServiceMeta;
import org.kkoneone.rpc.protocol.RpcProtocol;
import org.kkoneone.rpc.protocol.codec.RpcDecoder;
import org.kkoneone.rpc.protocol.codec.RpcEncoder;
import org.kkoneone.rpc.protocol.handler.consumer.RpcResponseHandler;

/**
 * 消费方发送数据
 * @Author：kkoneone11
 * @name：RpcConsumer
 * @Date：2023/11/28 10:40
 */
@Slf4j
public class RpcConsumer {

    //帮助设置客户端的辅助类
    private final Bootstrap bootstrap;
    //处理 I/O 操作的多线程事件循环
    private final EventLoopGroup eventLoopGroup;

    public RpcConsumer(){
        bootstrap = new Bootstrap();
        //创建一个4线程的Nio事件循环组
        eventLoopGroup = new NioEventLoopGroup(4);
        //将 eventLoopGroup 分配给 bootstrap并指定客户端将使用 NioSocketChannel 连接到服务器
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                   @Override
                   protected void initChannel(SocketChannel socketChannel) throws Exception{
                       //向每个SocketChannel的ChannelPipeline添加三个处理器
                       socketChannel.pipeline()
                               .addLast(new RpcEncoder())
                               .addLast(new RpcDecoder())
                               .addLast(new RpcResponseHandler());
                   }
                });
    }

    public void sendRequest(RpcProtocol<RpcRequest> protocol, ServiceMeta serviceMetadata) throws Exception {
        if(serviceMetadata != null){
            //和服务建立连接异步数据
            ChannelFuture future = bootstrap.connect(serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort()).sync();
            //设置事件监听器
            future.addListener((ChannelFutureListener) arg0 -> {
                if(future.isSuccess()){
                    log.info("连接 rpc server {} 端口 {} 成功.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                }else {
                    log.error("连接 rpc server {} 端口 {} 失败.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                    future.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully();
                }
            });

            //向管道异步写入数据传递给消息提供方
            future.channel().writeAndFlush(protocol);
        }

    }

}
