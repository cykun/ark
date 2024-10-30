package ink.xikun.ark.consumer;

import ink.xikun.ark.common.ArkProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ArkProperties.class)
public class RpcConsumerAutoConfiguration {

    @Bean
    public RpcConsumerPostProcessor rpcConsumerPostProcessor(ArkProperties arkProperties) {
        return new RpcConsumerPostProcessor(arkProperties);
    }
}
