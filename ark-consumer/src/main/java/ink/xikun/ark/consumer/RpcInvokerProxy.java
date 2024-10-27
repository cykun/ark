package ink.xikun.ark.consumer;

import ink.xikun.ark.common.RpcFuture;
import ink.xikun.ark.common.RpcRequest;
import ink.xikun.ark.common.RpcRequestHolder;
import ink.xikun.ark.common.RpcResponse;
import ink.xikun.ark.common.serialize.SerializationTypeEnum;
import ink.xikun.ark.protocol.*;
import ink.xikun.ark.registry.RegistryService;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class RpcInvokerProxy implements InvocationHandler {

    private final String serviceVersion;

    private final long timeout;

    private final RegistryService registryService;

    public RpcInvokerProxy(String serviceVersion, long timeout, RegistryService registryService) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.registryService = registryService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        MsgHeader header = new MsgHeader();
        long requestId = RpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setSerialization((byte) SerializationTypeEnum.jackson.getType());
        header.setMsgType((byte) MsgType.REQUEST.getType());
        header.setStatus((byte) 0x1);
        header.setRequestId(requestId);
        protocol.setHeader(header);

        RpcRequest request = new RpcRequest();
        request.setServiceVersion(serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        protocol.setBody(request);

        RpcConsumer rpcConsumer = new RpcConsumer();
        RpcFuture<RpcResponse> future = new RpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);
        RpcRequestHolder.REQUEST_MAP.put(requestId, future);
        rpcConsumer.sendRequest(protocol, registryService);

        return future.promise().get(future.timeout(), TimeUnit.MILLISECONDS).getData();
    }
}
