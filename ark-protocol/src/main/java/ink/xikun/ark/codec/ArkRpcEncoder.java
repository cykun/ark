package ink.xikun.ark.codec;

import ink.xikun.ark.common.serialize.Serialization;
import ink.xikun.ark.common.serialize.SerializationFactory;
import ink.xikun.ark.protocol.MsgHeader;
import ink.xikun.ark.protocol.RpcProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ArkRpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> {

    /*
    +---------------------------------------------------------------+
    | 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |
    +---------------------------------------------------------------+
    | 状态 1byte |        消息 ID 8byte     |      数据长度 4byte     |
    +---------------------------------------------------------------+
    |                   数据内容 （长度不定）                          |
    +---------------------------------------------------------------+
    */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcProtocol<Object> objectRpcProtocol, ByteBuf byteBuf) {
        MsgHeader header = objectRpcProtocol.getHeader();
        byteBuf.writeShort(header.getMagic());
        byteBuf.writeByte(header.getVersion());
        byteBuf.writeByte(header.getSerialization());
        byteBuf.writeByte(header.getMsgType());
        byteBuf.writeByte(header.getStatus());
        byteBuf.writeLong(header.getRequestId());
        Serialization serialization = SerializationFactory.getRpcSerialization(header.getSerialization());
        byte[] data = serialization.serialize(objectRpcProtocol.getBody());
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}
