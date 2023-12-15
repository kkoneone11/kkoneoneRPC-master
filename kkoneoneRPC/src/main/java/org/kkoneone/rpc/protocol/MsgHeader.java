package org.kkoneone.rpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息头
 * @Author：kkoneone11
 * @name：MsgHeader
 * @Date：2023/11/26 9:28
 */
@Data
public class MsgHeader implements Serializable {
    private short magic; // 魔数
    private byte version; // 协议版本号
    private byte msgType; // 数据类型
    private byte status; // 状态
    private long requestId; // 请求 ID
    private int serializationLen;
    private byte[] serializations;
    private int msgLen; // 数据长度
}
