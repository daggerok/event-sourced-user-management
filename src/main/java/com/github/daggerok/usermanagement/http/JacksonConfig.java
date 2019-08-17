package com.github.daggerok.usermanagement.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Lazy
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().enable(SerializationFeature.WRAP_EXCEPTIONS)
                                 .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                 .enable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS)
                                 .enable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
                                 .enable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                                 .enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)
                                 .enable(DeserializationFeature.WRAP_EXCEPTIONS);
    }
}
