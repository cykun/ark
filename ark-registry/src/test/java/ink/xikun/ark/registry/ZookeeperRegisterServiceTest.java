package ink.xikun.ark.registry;

import ink.xikun.ark.core.ServiceMeta;
import org.junit.jupiter.api.Test;

public class ZookeeperRegisterServiceTest {

    @Test
    public void testRegister() throws Exception {
        RegistryService registryService = RegistryFactory.getInstance("127.0.0.1:2181");
        ServiceMeta serviceMeta = new ServiceMeta();
        serviceMeta.setServiceName("test");
        serviceMeta.setServiceVersion("1.0");
        serviceMeta.setServiceAddress("127.0.0.1");
        serviceMeta.setServicePort(8080);
        registryService.register(serviceMeta);
    }
}
