package org.kkoneone.rpc.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.kkoneone.rpc.protocol.MsgHeader;
import org.kkoneone.rpc.protocol.RpcProtocol;
import org.kkoneone.rpc.protocol.serialization.RpcSerialization;
import org.kkoneone.rpc.protocol.serialization.SerializationFactory;

/**
 * 编码器
 * @Author：kkoneone11
 * @name：RpcEncoder
 * @Date：2023/11/28 14:35
 */
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol<Object> msg, ByteBuf byteBuf) throws Exception {
        // 获取消息头类型
        MsgHeader header = msg.getHeader();
        //往byteBuf流里写入数据
        // 写入魔数(安全校验，可以参考java中的CAFEBABE)
        byteBuf.writeShort(header.getMagic());
        // 写入版本号
        byteBuf.writeByte(header.getVersion());
        // 写入消息类型(接收放根据不同的消息类型进行不同的处理方式)
        byteBuf.writeByte(header.getMsgType());
        // 写入状态
        byteBuf.writeByte(header.getStatus());
        // 写入请求id(请求id可以用于记录异步回调标识,具体需要回调给哪个请求)
        byteBuf.writeLong(header.getRequestId());
        // 写入序列化方式(接收方需要依靠具体哪个序列化进行序列化)
        byteBuf.writeInt(header.getSerializationLen());
        final byte[] ser = header.getSerializations();
        byteBuf.writeBytes(ser);
        final String serialization = new String(ser);
        //获取序列化策略 序列化消息体
        RpcSerialization rpcSerialization = SerializationFactory.get(serialization);
        byte[] data = rpcSerialization.serialize(msg.getBody());
        // 写入数据长度(接收方根据数据长度读取数据内容)
        byteBuf.writeInt(data.length);
        // 写入数据
        byteBuf.writeBytes(data);
    }
}
