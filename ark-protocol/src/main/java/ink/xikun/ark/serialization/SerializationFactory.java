package ink.xikun.ark.serialization;

import ink.xikun.ark.common.serialize.Serialization;
import ink.xikun.ark.common.extension.ExtensionLoader;

public final class SerializationFactory {

    public static Serialization getRpcSerialization(byte serializationType) {
        SerializationTypeEnum typeEnum = SerializationTypeEnum.findByType(serializationType);
        return ExtensionLoader.getExtension(Serialization.class, typeEnum.name());
    }
}
