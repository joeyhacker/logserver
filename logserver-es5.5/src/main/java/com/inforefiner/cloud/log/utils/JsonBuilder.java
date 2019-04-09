package com.inforefiner.cloud.log.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;

public class JsonBuilder {

    private ObjectMapper mapper = new ObjectMapper();

    private static com.inforefiner.cloud.log.utils.JsonBuilder _instance = new com.inforefiner.cloud.log.utils.JsonBuilder();

    public static com.inforefiner.cloud.log.utils.JsonBuilder getInstance() {
        return _instance;
    }

    public JsonBuilder() {
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public com.inforefiner.cloud.log.utils.JsonBuilder pretty() {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return this;
    }

    public <T> T fromJson(String json, Class<T> typeOfT) {
        if (json == null) {
            throw new IllegalArgumentException("json string should not be null");
        }
        try {
            return mapper.readValue(json, typeOfT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ObjectMapper buildObjectMapper(boolean prettyJson) {
        ObjectMapper m = new ObjectMapper();
        if (prettyJson) {
            m.enable(SerializationFeature.INDENT_OUTPUT);
        }
        m.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        m.setSerializationInclusion(Include.NON_NULL);
        return m;
    }

    public static ObjectMapper defaultObjectMapper = buildObjectMapper(false);

}
