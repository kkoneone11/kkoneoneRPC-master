package org.kkoneone.rpc.common.constants;

/**
 * @Author：kkoneone11
 * @name：MsgType
 * @Date：2023/11/27 11:54
 */
public enum MsgType {
    REQUEST,
    RESPONSE,
    HEARTBEAT;

    public static MsgType findByType(int type) {
        return MsgType.values()[type];
    }
}
