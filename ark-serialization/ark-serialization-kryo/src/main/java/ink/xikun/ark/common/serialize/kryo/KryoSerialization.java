package ink.xikun.ark.common.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import ink.xikun.ark.common.serialize.Serialization;
import ink.xikun.ark.common.serialize.SerializationException;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSerialization implements Serialization {

    public static ThreadLocal<Kryo> runtimeSerializationKryo = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
            kryo.register(java.sql.Timestamp.class, new DefaultSerializers.TimestampSerializer());
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
        }
    };

    @Override
    public <T> byte[] serialize(T obj) throws SerializationException {
        if (obj == null) {
            throw new NullPointerException("obj is null");
        }

        Kryo kryo = runtimeSerializationKryo.get();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Output output = new Output(bos);
            kryo.writeObject(output, obj);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize object", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws SerializationException {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("data is null or empty");
        }

        Kryo kryo = runtimeSerializationKryo.get();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
            Input input = new Input(bis);
            return kryo.readObject(input, clazz);
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize object", e);
        }
    }
}
