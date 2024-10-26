package ink.xikun.ark.common;

import lombok.Data;

@Data
public class ServiceMeta {

    private String serviceName;

    private String serviceVersion;

    private String serviceAddress;

    private int servicePort;
}
