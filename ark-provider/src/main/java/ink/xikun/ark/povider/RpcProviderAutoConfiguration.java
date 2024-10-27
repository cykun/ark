package ink.xikun.ark.povider;

import ink.xikun.ark.registry.RegistryFactory;
import ink.xikun.ark.registry.RegistryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class RpcProviderAutoConfiguration {

    @Bean
    public RpcProvider rpcProvider() throws Exception {
        RegistryService registryService = RegistryFactory.getInstance(new URI("nacos://localhost:8848"));
        return new RpcProvider(9711, registryService);
    }
}
