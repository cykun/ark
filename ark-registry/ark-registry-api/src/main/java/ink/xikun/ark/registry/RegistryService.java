package ink.xikun.ark.registry;

import ink.xikun.ark.common.RpcRequest;
import ink.xikun.ark.common.ServiceMeta;
import ink.xikun.ark.common.extension.SPI;

import java.io.IOException;
import java.net.URI;

@SPI
public interface RegistryService {

    void init(URI uri) throws Exception;

    void register(ServiceMeta serviceMeta) throws Exception;

    void unRegister(ServiceMeta serviceMeta) throws Exception;

    ServiceMeta discovery(String serviceName, RpcRequest request) throws Exception;

    void destroy() throws IOException;
}
