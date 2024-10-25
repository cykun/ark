package ink.xikun.ark.handler;

import ink.xikun.ark.core.RpcRequest;
import ink.xikun.ark.core.RpcResponse;
import ink.xikun.ark.core.RpcServiceHelper;
import ink.xikun.ark.protocol.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    private final Map<String, Object> rpcServiceMap;

    public RpcRequestHandler(Map<String, Object> rpcServiceMap) {
        this.rpcServiceMap = rpcServiceMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> reqProtocol) {
        RpcRequestProcessor.submitRequest(() -> {
            RpcProtocol<RpcResponse> resProtocol = new RpcProtocol<>();
            RpcResponse response = new RpcResponse();
            MsgHeader header = buildResponseHeader(reqProtocol.getHeader());
            try {
                RpcRequest req = reqProtocol.getBody();
                Object result = handler(req);
                response.setData(result);

                header.setStatus((byte) MsgStatus.SUCCESS.getCode());
            } catch (Throwable e) {
                header.setStatus((byte) MsgStatus.FAIL.getCode());
                response.setMessage(e.toString());
            }

            resProtocol.setHeader(header);
            resProtocol.setBody(response);
            ctx.writeAndFlush(resProtocol);
        });
    }

    private static MsgHeader buildResponseHeader(MsgHeader reqHeader) {
        MsgHeader header = new MsgHeader();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(reqHeader.getVersion());
        header.setSerialization(reqHeader.getSerialization());
        header.setMsgType((byte) MsgType.RESPONSE.getType());
        header.setRequestId(reqHeader.getRequestId());
        return header;
    }

    private Object handler(RpcRequest request) throws Throwable {
        String serviceName = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getServiceVersion());
        Object serviceBean = rpcServiceMap.get(serviceName);

        if (serviceBean == null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getServiceVersion()));
        }

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        return serviceClass.getMethod(methodName, parameterTypes).invoke(serviceBean, parameters);
    }
}
