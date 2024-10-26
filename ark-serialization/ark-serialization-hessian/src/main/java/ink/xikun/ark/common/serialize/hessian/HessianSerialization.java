package ink.xikun.ark.common.serialize.hessian;

import com.caucho.hessian.io.HessianSerializerInput;
import com.caucho.hessian.io.HessianSerializerOutput;
import ink.xikun.ark.common.serialize.Serialization;
import ink.xikun.ark.common.serialize.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianSerialization implements Serialization {

    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null) {
            throw new NullPointerException("obj is null");
        }

        HessianSerializerOutput hessianOutput;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            hessianOutput = new HessianSerializerOutput(bos);
            hessianOutput.writeObject(obj);
            hessianOutput.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (data == null) {
            throw new NullPointerException("data is null");
        }

        try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
            return (T) new HessianSerializerInput(bis).readObject(clazz);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }
}
