package com.workflowsaas.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Camunda embedded engine.
 * The engine and services are automatically configured by Camunda Spring Boot Starter.
 * No manual bean definitions needed - they're provided by auto-configuration.
 */
@Configuration
public class CamundaConfig {
    // Camunda beans (ProcessEngine, RuntimeService, RepositoryService, etc.) 
    // are auto-configured by camunda-bpm-spring-boot-starter
}
