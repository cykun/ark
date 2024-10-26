package ink.xikun.ark.common.serialize;

import ink.xikun.ark.common.extension.SPI;

@SPI
public interface Serialization {

    <T> byte[] serialize(T obj) throws SerializationException;

    <T> T deserialize(byte[] data, Class<T> clazz) throws SerializationException;
}
