package ink.xikun.ark.povider;

import ink.xikun.ark.registry.RegistryFactory;
import ink.xikun.ark.registry.RegistryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RpcProviderAutoConfiguration {

    @Bean
    public RpcProvider rpcProvider() throws Exception {
        RegistryService registryService = RegistryFactory.getInstance("127.0.0.1:2181");
        return new RpcProvider(9711, registryService);
    }
}
