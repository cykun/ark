package ink.xikun.ark.handler;

import ink.xikun.ark.common.RpcFuture;
import ink.xikun.ark.common.RpcRequestHolder;
import ink.xikun.ark.common.RpcResponse;
import ink.xikun.ark.protocol.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> resProtocol) {
        long requestId = resProtocol.getHeader().getRequestId();
        RpcFuture<RpcResponse> future = RpcRequestHolder.REQUEST_MAP.remove(requestId);
        future.promise().setSuccess(resProtocol.getBody());
    }
}
