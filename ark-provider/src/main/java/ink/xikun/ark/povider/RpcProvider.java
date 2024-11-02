package ink.xikun.ark.povider;

import ink.xikun.ark.codec.ArkRpcDecoder;
import ink.xikun.ark.codec.ArkRpcEncoder;
import ink.xikun.ark.common.RpcServiceHelper;
import ink.xikun.ark.common.ServiceMeta;
import ink.xikun.ark.handler.RpcRequestHandler;
import ink.xikun.ark.povider.annotation.RpcService;
import ink.xikun.ark.registry.RegistryService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcProvider implements InitializingBean, DisposableBean, BeanPostProcessor {

    private String serverAddress;
    private final int serverPort;
    private final RegistryService registryService;
    private final Map<String, Object> rpcServiceMap = new HashMap<>();
    private final List<ServiceMeta> registeredServiceList = new LinkedList<>();

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public RpcProvider(int serverPort, RegistryService registryService) {
        this.serverPort = serverPort;
        this.registryService = registryService;
    }

    private void startServer() throws Exception {
        this.serverAddress = InetAddress.getLocalHost().getHostAddress();

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline()
                                    .addLast("decoder", new ArkRpcDecoder())
                                    .addLast("encoder", new ArkRpcEncoder())
                                    .addLast("server-idle-handler", new IdleStateHandler(0, 0, 3000, TimeUnit.MILLISECONDS))
                                    .addLast("handler", new RpcRequestHandler(rpcServiceMap));
                        }
                    }).childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(this.serverAddress, serverPort).sync();
            log.info("Ark started with port {}", serverPort);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void afterPropertiesSet() {
        new Thread(() -> {
            try {
                startServer();
            } catch (Exception e) {
                log.error("start server error", e);
            }
        }).start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> registeredServiceList.forEach(serviceMeta -> {
            try {
                registryService.unRegister(serviceMeta);
            } catch (Exception e) {
                log.error("unregister service error", e);
            }
        })));
    }

    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
        if (rpcService != null) {
            String serviceName = rpcService.serviceInterface().getName();
            String serviceVersion = rpcService.serviceVersion();

            ServiceMeta serviceMeta = new ServiceMeta();
            serviceMeta.setServiceName(serviceName);
            serviceMeta.setServiceVersion(serviceVersion);
            serviceMeta.setServiceAddress(this.serverAddress);
            serviceMeta.setServicePort(this.serverPort);

            try {
                registryService.register(serviceMeta);
                rpcServiceMap.put(RpcServiceHelper.buildServiceKey(serviceName, serviceVersion), bean);
                registeredServiceList.add(serviceMeta);
            } catch (Exception e) {
                log.error("register service error", e);
            }
        }
        return bean;
    }

    public void close() throws IOException {
        registryService.destroy();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public void destroy() throws Exception {
        close();
    }
}
