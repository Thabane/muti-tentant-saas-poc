package com.workflowsaas.config;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson configuration for JSON serialization/deserialization.
 * Configures stream read constraints to handle large CSV file uploads.
 */
@Configuration
class JacksonConfig {

    /**
     * Configure ObjectMapper with increased string length limit.
     * Default is 20MB, we increase to 50MB to handle large CSV files.
     */
    @Bean
    ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.build();
        
        // Increase max string length from 20MB to 50MB
        StreamReadConstraints constraints = StreamReadConstraints.builder()
                .maxStringLength(50_000_000) // 50MB
                .build();
        
        mapper.getFactory().setStreamReadConstraints(constraints);
        
        return mapper;
    }
}
