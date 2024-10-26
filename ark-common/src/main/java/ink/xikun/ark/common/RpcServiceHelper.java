package ink.xikun.ark.common;

public final class RpcServiceHelper {

    public static String buildServiceKey(String serviceName, String serviceVersion) {
        return String.format("%s#%s", serviceName, serviceVersion);
    }
}
