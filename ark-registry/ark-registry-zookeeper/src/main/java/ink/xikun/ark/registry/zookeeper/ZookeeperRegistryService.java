package ink.xikun.ark.registry.zookeeper;

import ink.xikun.ark.common.RpcRequest;
import ink.xikun.ark.common.RpcServiceHelper;
import ink.xikun.ark.common.ServiceMeta;
import ink.xikun.ark.registry.RegistryService;
import ink.xikun.ark.registry.loadbalance.RandomLoadBalancer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;

public class ZookeeperRegistryService implements RegistryService {

    public static final int BASE_SLEEP_TIME = 1000;
    public static final int MAX_RETRY_TIMES = 3;
    public static final String ZK_REGISTRY_PATH = "/ark";

    private ServiceDiscovery<ServiceMeta> serviceDiscovery;

    @Override
    public void init(URI uri) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(uri.getHost() + ":" + uri.getPort(), new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRY_TIMES));
        client.start();
        JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<>(ServiceMeta.class);
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMeta.class)
                .client(client)
                .serializer(serializer)
                .basePath(ZK_REGISTRY_PATH)
                .build();
        this.serviceDiscovery.start();
    }

    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance.<ServiceMeta>builder()
                .name(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion()))
                .address(serviceMeta.getServiceAddress())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance.<ServiceMeta>builder()
                .name(serviceMeta.getServiceName())
                .address(serviceMeta.getServiceAddress())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    @Override
    public ServiceMeta discovery(String serviceName, RpcRequest request) throws Exception {
        Collection<ServiceInstance<ServiceMeta>> serviceInstances = serviceDiscovery.queryForInstances(serviceName);
        ServiceInstance<ServiceMeta> instance = new RandomLoadBalancer().select((List<ServiceInstance<ServiceMeta>>) serviceInstances, request);
        if (instance != null) {
            return instance.getPayload();
        }
        return null;
    }

    @Override
    public void destroy() throws IOException {
        serviceDiscovery.close();
    }
}
