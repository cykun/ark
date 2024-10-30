package ink.xikun.ark.registry.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import ink.xikun.ark.common.RpcRequest;
import ink.xikun.ark.common.RpcServiceHelper;
import ink.xikun.ark.common.ServiceMeta;
import ink.xikun.ark.registry.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@Slf4j
public class NacosRegistryService implements RegistryService {

    private NamingService namingService;

    @Override
    public void init(URI uri) throws Exception {
        namingService = NacosFactory.createNamingService(uri.getHost() + ":" + uri.getPort());
    }

    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        String serviceName = RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion());
        Instance instance = createInstance(serviceMeta);
        namingService.registerInstance(serviceName, Constants.DEFAULT_GROUP, instance);
        log.info("Register nacos service: {}", serviceName);
    }

    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {
        String serviceName = RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion());
        namingService.deregisterInstance(serviceName, serviceMeta.getServiceAddress(), serviceMeta.getServicePort());
        log.info("Unregister nacos service: {}", serviceName);
    }

    @Override
    public ServiceMeta discovery(String serviceName, RpcRequest request) throws Exception {
        List<Instance> instances = namingService.getAllInstances(serviceName);
        if (instances.isEmpty()) {
            return null;
        }
        Instance instance = instances.getFirst();
        ServiceMeta serviceMeta = new ServiceMeta();
        serviceMeta.setServiceAddress(instance.getIp());
        serviceMeta.setServicePort(instance.getPort());
        return serviceMeta;
    }

    @Override
    public void destroy() throws IOException {

    }

    private static Instance createInstance(ServiceMeta serviceMeta) {
        Instance instance = new Instance();
        instance.setServiceName(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion()));
        instance.setIp(serviceMeta.getServiceAddress());
        instance.setPort(serviceMeta.getServicePort());
        return instance;
    }
}
