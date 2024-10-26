package ink.xikun.ark.common.serialize.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ink.xikun.ark.common.serialize.Serialization;
import ink.xikun.ark.common.serialize.SerializationException;

import java.text.SimpleDateFormat;

public class JacksonSerialization implements Serialization {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = generateMapper();
    }

    private static ObjectMapper generateMapper() {
        ObjectMapper customMapper = new ObjectMapper();

        customMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        customMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        customMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        customMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        return customMapper;
    }

    @Override
    public <T> byte[] serialize(T obj) {
        try {
            return obj instanceof String ? ((String) obj).getBytes() : MAPPER.writeValueAsBytes(obj);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        try {
            return MAPPER.readValue(data, clazz);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }
}
