package ink.xikun.ark.consumer;

import ink.xikun.ark.registry.RegistryFactory;
import ink.xikun.ark.registry.RegistryService;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;
import java.net.URI;

@Setter
public class RpcReferenceBean implements FactoryBean<Object> {

    private Class<?> interfaceClass;

    private String serviceVersion;

    private String registryAddress;

    private long timeout;

    private Object proxy;

    @Override
    public Object getObject() {
        return proxy;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    public void init() throws Exception {
        RegistryService registryService = RegistryFactory.getInstance(new URI(registryAddress));
        this.proxy = Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcInvokerProxy(serviceVersion, timeout, registryService));
    }
}
