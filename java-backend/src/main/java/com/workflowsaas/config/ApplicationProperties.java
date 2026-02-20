package com.workflowsaas.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Application configuration properties.
 * Centralizes all application-specific configuration with validation.
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {
    
    /**
     * Active Spring profile (development, production, etc.)
     */
    @NotBlank
    private String profile = "production";
    
    /**
     * CORS configuration
     */
    private final Cors cors = new Cors();
    
    @Data
    public static class Cors {
        /**
         * Comma-separated list of allowed origins for CORS
         */
        @NotBlank
        private String allowedOrigins = "http://localhost:5173";
    }
}
