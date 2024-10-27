package ink.xikun.ark.codec;

import ink.xikun.ark.common.serialize.Serialization;
import ink.xikun.ark.common.RpcRequest;
import ink.xikun.ark.common.RpcResponse;
import ink.xikun.ark.common.serialize.SerializationFactory;
import ink.xikun.ark.protocol.MsgHeader;
import ink.xikun.ark.protocol.MsgType;
import ink.xikun.ark.protocol.ProtocolConstants;
import ink.xikun.ark.protocol.RpcProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class ArkRpcDecoder extends ByteToMessageDecoder {

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
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        if (byteBuf.readableBytes() < 16) {
            return;
        }
        byteBuf.markReaderIndex();
        short magic = byteBuf.readShort();
        if (magic != ProtocolConstants.MAGIC) {
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }
        byte version = byteBuf.readByte();
        byte serializationType = byteBuf.readByte();
        byte messageType = byteBuf.readByte();
        byte status = byteBuf.readByte();
        long requestId = byteBuf.readLong();
        int dataLength = byteBuf.readInt();

        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);

        MsgType msgType = MsgType.findByType(messageType);
        if (msgType == null) {
            return;
        }

        MsgHeader msgHeader = new MsgHeader();
        msgHeader.setMagic(magic);
        msgHeader.setVersion(version);
        msgHeader.setSerialization(serializationType);
        msgHeader.setMsgType(messageType);
        msgHeader.setStatus(status);
        msgHeader.setRequestId(requestId);
        msgHeader.setDataLength(dataLength);

        Serialization rpcSerialization = SerializationFactory.getRpcSerialization(serializationType);
        switch (msgType) {
            case REQUEST:
                RpcRequest rpcRequest = rpcSerialization.deserialize(data, RpcRequest.class);
                if (rpcRequest != null) {
                    RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
                    protocol.setHeader(msgHeader);
                    protocol.setBody(rpcRequest);
                    list.add(protocol);
                }
                break;
            case RESPONSE:
                RpcResponse rpcResponse = rpcSerialization.deserialize(data, RpcResponse.class);
                if (rpcResponse != null) {
                    RpcProtocol<RpcResponse> protocol = new RpcProtocol<>();
                    protocol.setHeader(msgHeader);
                    protocol.setBody(rpcResponse);
                    list.add(protocol);
                }
                break;
        }
    }
}
