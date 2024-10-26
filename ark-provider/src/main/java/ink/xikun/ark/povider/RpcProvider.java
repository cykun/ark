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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class RpcProvider implements InitializingBean, BeanPostProcessor {

    private String serverAddress;
    private final int serverPort;
    private final RegistryService registryService;
    private final Map<String, Object> rpcServiceMap = new HashMap<>();
    private final List<ServiceMeta> registeredServiceList = new LinkedList<>();

    public RpcProvider(int serverPort, RegistryService registryService) {
        this.serverPort = serverPort;
        this.registryService = registryService;
    }

    private void startServer() throws Exception {
        this.serverAddress = InetAddress.getLocalHost().getHostAddress();

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new ArkRpcEncoder())
                                    .addLast(new ArkRpcDecoder())
                                    .addLast(new RpcRequestHandler(rpcServiceMap));
                        }
                    }).childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(this.serverAddress, serverPort).sync();
            log.info("server started at port {}", serverPort);
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
}
