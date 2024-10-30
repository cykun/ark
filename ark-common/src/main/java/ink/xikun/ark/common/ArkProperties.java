package ink.xikun.ark.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ark.provider")
public class ArkProperties {

    private Application application;

    private Registry registry;

    private Protocol protocol;

    @Getter
    @Setter
    public static class Application {
        private String name;
    }

    @Getter
    @Setter
    public static class Registry {
        private String address;
    }

    @Getter
    @Setter
    public static class Protocol {

        private String name;

        private Integer port;
    }
}
