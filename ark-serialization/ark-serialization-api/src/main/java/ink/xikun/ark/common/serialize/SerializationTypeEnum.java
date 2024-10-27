package ink.xikun.ark.common.serialize;

import lombok.Getter;

@Getter
public enum SerializationTypeEnum {

    hessian(0x10),
    jackson(0x20),
    kryo(0x30);

    private final int type;

    SerializationTypeEnum(int type) {
        this.type = type;
    }

    public static SerializationTypeEnum findByType(byte serializationType) {
        for (SerializationTypeEnum typeEnum : SerializationTypeEnum.values()) {
            if (typeEnum.getType() == serializationType) {
                return typeEnum;
            }
        }
        return hessian;
    }
}
