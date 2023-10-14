package org.laboration3.resource.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;


@Provider
public class ObjectMapperConvertDate implements ContextResolver<ObjectMapper> {
    public static ObjectMapper configureObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false);
        return objectMapper;
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return configureObjectMapper();
    }
}