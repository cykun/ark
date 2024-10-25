package ink.xikun.ark.protocol;

import lombok.Data;

/*
+---------------------------------------------------------------+
| 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |
+---------------------------------------------------------------+
| 状态 1byte |        消息 ID 8byte     |      数据长度 4byte     |
+---------------------------------------------------------------+
|                   数据内容 （长度不定）                          |
+---------------------------------------------------------------+
*/
@Data
public class MsgHeader {

    private short magic;

    private byte version;

    private byte serialization;

    private byte msgType;

    private byte status;

    private long requestId;

    private int dataLength;
}
