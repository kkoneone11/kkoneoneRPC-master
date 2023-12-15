package org.kkoneone.rpc.protocol.handler.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.kkoneone.rpc.Filter.FilterConfig;
import org.kkoneone.rpc.Filter.FilterData;
import org.kkoneone.rpc.Filter.client.ClientLogFilter;
import org.kkoneone.rpc.common.RpcResponse;
import org.kkoneone.rpc.common.constants.MsgStatus;
import org.kkoneone.rpc.protocol.MsgHeader;
import org.kkoneone.rpc.protocol.RpcProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author：kkoneone11
 * @name：ServiceAfterFilterHandler
 * @Date：2023/12/5 17:28
 */
public class ServiceAfterFilterHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    private Logger logger = LoggerFactory.getLogger(ClientLogFilter.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> protocol) {
        final FilterData filterData = new FilterData();
        filterData.setData(protocol.getBody());
        RpcResponse response = new RpcResponse();
        MsgHeader header = protocol.getHeader();
        try {
            FilterConfig.getServiceAfterFilterChain().doFilter(filterData);
        } catch (Exception e) {
            header.setStatus((byte) MsgStatus.FAILED.ordinal());
            response.setException(e);
            logger.error("after process request {} error", header.getRequestId(), e);
        }
        ctx.writeAndFlush(protocol);
    }
}
