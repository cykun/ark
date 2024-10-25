package ink.xikun.ark.serialization;

public final class SerializationFactory {

    public static RpcSerialization getRpcSerialization(byte serializationType) {
        SerializationTypeEnum typeEnum = SerializationTypeEnum.findByType(serializationType);

        return switch (typeEnum) {
            case HESSIAN -> new HessianSerialization();
            case JSON -> new JsonSerialization();
        };
    }
}
