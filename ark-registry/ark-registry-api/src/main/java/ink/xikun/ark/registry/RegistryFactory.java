package ink.xikun.ark.registry;

import ink.xikun.ark.common.extension.ExtensionLoader;

import java.net.URI;

public final class RegistryFactory {

    public static RegistryService getInstance(URI uri) {
        RegistryService registryService = ExtensionLoader.getExtension(RegistryService.class, uri.getScheme());
        try {
            registryService.init(uri);
            return registryService;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
