package com.workflowsaas.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for cross-origin requests.
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {
    
    private final ApplicationProperties applicationProperties;
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        String activeProfile = applicationProperties.getProfile();
        if ("development".equals(activeProfile) || "dev".equals(activeProfile)) {
            config.addAllowedOriginPattern("*");
        } else {
            String allowedOrigins = applicationProperties.getCors().getAllowedOrigins();
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            config.setAllowedOrigins(origins);
        }
        
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
