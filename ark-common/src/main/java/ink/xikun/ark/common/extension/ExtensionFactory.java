package ink.xikun.ark.common.extension;

@SPI
public interface ExtensionFactory {

    <T> T getExtension(String key, Class<T> clazz);
}
