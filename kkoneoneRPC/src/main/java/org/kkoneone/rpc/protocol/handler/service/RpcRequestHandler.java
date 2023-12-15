package org.kkoneone.rpc.protocol.handler.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.kkoneone.rpc.common.RpcRequest;
import org.kkoneone.rpc.poll.ThreadPollFactory;
import org.kkoneone.rpc.protocol.RpcProtocol;

/**
 * 处理消费方发送数据并且调用方法
 * @Author：kkoneone11
 * @name：RpcRequestHandler
 * @Date：2023/12/4 14:50
 */
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>>{

    public RpcRequestHandler() {}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        ThreadPollFactory.submitRequest(ctx,protocol);
    }
}
