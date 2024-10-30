package ink.xikun.ark.povider;

import ink.xikun.ark.common.ArkProperties;
import ink.xikun.ark.registry.RegistryFactory;
import ink.xikun.ark.registry.RegistryService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(ArkProperties.class)
public class RpcProviderAutoConfiguration {

    @Bean
    public RpcProvider rpcProvider(ArkProperties arkProperties) throws Exception {
        RegistryService registryService = RegistryFactory.getInstance(new URI(arkProperties.getRegistry().getAddress()));
        return new RpcProvider(arkProperties.getProtocol().getPort(), registryService);
    }
}
