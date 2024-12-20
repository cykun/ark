package ink.xikun.ark.consumer;

import ink.xikun.ark.codec.ArkRpcDecoder;
import ink.xikun.ark.codec.ArkRpcEncoder;
import ink.xikun.ark.common.RpcRequest;
import ink.xikun.ark.common.RpcServiceHelper;
import ink.xikun.ark.common.ServiceMeta;
import ink.xikun.ark.handler.RpcResponseHandler;
import ink.xikun.ark.protocol.RpcProtocol;
import ink.xikun.ark.registry.RegistryService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcConsumer {

    private static final RpcConsumer INSTANCE = new RpcConsumer();
    private static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private static final Bootstrap bootstrap = new Bootstrap();
    public static AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool> poolMap;

    static {
        bootstrap.group(eventLoopGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(NioSocketChannel.class);
        poolMap = new AbstractChannelPoolMap<>() {
            @Override
            protected FixedChannelPool newPool(InetSocketAddress inetSocketAddress) {
                ChannelPoolHandler handler = new AbstractChannelPoolHandler() {
                    @Override
                    public void channelCreated(Channel ch) {
                        ch.pipeline()
                                .addLast("decoder", new ArkRpcDecoder())
                                .addLast("encoder", new ArkRpcEncoder())
                                .addLast("client-idle-handler", new IdleStateHandler(3000, 30, 0, TimeUnit.MILLISECONDS))
                                .addLast(new RpcResponseHandler());
                    }
                };
                return new FixedChannelPool(bootstrap.remoteAddress(inetSocketAddress), handler, 5);
            }
        };
        Runtime.getRuntime().addShutdownHook(new Thread(RpcConsumer::close));
    }

    public static RpcConsumer getInstance() {
        return INSTANCE;
    }

    public void sendRequest(RpcProtocol<RpcRequest> requestProtocol, RegistryService registryService) throws Exception {
        RpcRequest request = requestProtocol.getBody();
        String serviceIdentifier = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getServiceVersion());

        ServiceMeta serviceMetadata = registryService.discovery(serviceIdentifier, request);

        if (serviceMetadata != null) {
            InetSocketAddress serviceAddress = new InetSocketAddress(serviceMetadata.getServiceAddress(), serviceMetadata.getServicePort());
            final SimpleChannelPool channelPool = poolMap.get(serviceAddress);
            final Future<Channel> channelFuture = channelPool.acquire();
            channelFuture.addListener((FutureListener<Channel>) futureListener -> {
                if (channelFuture.isSuccess()) {
                    Channel connectionChannel = channelFuture.getNow();
                    connectionChannel.writeAndFlush(requestProtocol);
                    channelPool.release(connectionChannel);
                } else {
                    log.error("get channel from pool failed.");
                }
            });
        } else {
            log.error("can not find service metadata.");
        }
    }

    public static void close() {
        poolMap.close();
    }
}
