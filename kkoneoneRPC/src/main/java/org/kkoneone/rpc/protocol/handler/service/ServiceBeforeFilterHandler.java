package org.kkoneone.rpc.protocol.handler.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.kkoneone.rpc.Filter.FilterConfig;
import org.kkoneone.rpc.Filter.FilterData;
import org.kkoneone.rpc.common.RpcRequest;
import org.kkoneone.rpc.common.RpcResponse;
import org.kkoneone.rpc.common.constants.MsgStatus;
import org.kkoneone.rpc.protocol.MsgHeader;
import org.kkoneone.rpc.protocol.RpcProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 前置拦截器
 * @Author：kkoneone11
 * @name：ServiceBeforeFilterHandler
 * @Date：2023/12/3 14:50
 */
public class ServiceBeforeFilterHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
    private Logger logger = LoggerFactory.getLogger(ServiceBeforeFilterHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {
        final RpcRequest request = protocol.getBody();
        final FilterData filterData = new FilterData(request);
        RpcResponse response = new RpcResponse();
        MsgHeader header = protocol.getHeader();

        //将数据加入过滤链 对数据做一些处理
        try{
            FilterConfig.getServiceBeforeFilterChain().doFilter(filterData);
        }catch (Exception e){
            //若失败则重新构建一个协议
            RpcProtocol<RpcResponse> resProtocol = new RpcProtocol<>();
            //请求头中塞入失败状态
            header.setStatus((byte) MsgStatus.FAILED.ordinal());
            //Response中塞错误信息
            response.setException(e);
            //协议中设置封装好的消息头和消息体
            logger.error("before process request {} error", header.getRequestId(), e);
            resProtocol.setHeader(header);
            resProtocol.setBody(response);
            //写回信息
            ctx.writeAndFlush(resProtocol);
            return;
        }
        ctx.fireChannelRead(protocol);
    }
}
