package org.kkoneone.rpc.protocol.handler.consumer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.kkoneone.rpc.common.RpcFuture;
import org.kkoneone.rpc.common.RpcRequestHolder;
import org.kkoneone.rpc.common.RpcResponse;
import org.kkoneone.rpc.protocol.RpcProtocol;

/**
 * 响应
 * @Author：kkoneone11
 * @name：RpcResponseHandler
 * @Date：2023/11/29 9:47
 */
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    /**
     * 处理传入的RPC响应，将它们与相应的请求匹配，并将表示请求的future的结果设置为响应的主体表示结果处理情况
     * @param ctx 关于网络通信的上下文
     * @param msg Rpc协议
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> msg) throws Exception {
        long requestId = msg.getHeader().getRequestId();
        RpcFuture<RpcResponse> future = RpcRequestHolder.REQUEST_MAP.remove(requestId);
        //将RpcFuture的结果设置为传入消息的主体。解除任何等待此future结果的线程的阻塞
        future.getPromise().setSuccess(msg.getBody());
    }
}
