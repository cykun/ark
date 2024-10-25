package ink.xikun.ark.registry;

public final class RegistryFactory {

    private static volatile RegistryService registryService;

    public static RegistryService getInstance(String registryAddress) throws Exception {
        if (registryService == null) {
            synchronized (RegistryService.class) {
                if (registryService == null) {
                    registryService = new ZookeeperRegisterService(registryAddress);
                }
            }
        }
        return registryService;
    }
}
