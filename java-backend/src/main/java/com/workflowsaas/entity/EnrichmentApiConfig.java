package com.workflowsaas.entity;

import com.workflowsaas.converter.JsonbConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * EnrichmentApiConfig entity representing enrichment API configuration for an app.
 * Multiple enrichment APIs can be associated with one AppConfiguration.
 * The config_properties field uses JSONB type (converted with JsonbConverter).
 */
@Entity
@Table(name = "enrichment_api_configs",
       indexes = {
           @Index(name = "idx_enrichment_api_tenant_id", columnList = "tenant_id"),
           @Index(name = "idx_enrichment_api_config_id", columnList = "app_config_id")
       })
@Data
public class EnrichmentApiConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_config_id", nullable = false)
    private AppConfiguration appConfiguration;
    
    @Column(name = "openapi_document", nullable = false, columnDefinition = "TEXT")
    private String openApiDocument;
    
    @Column(name = "request_mappings", columnDefinition = "TEXT")
    private String requestMappings;
    
    @Column(name = "config_properties", columnDefinition = "JSONB")
    @Convert(converter = JsonbConverter.class)
    private Map<String, String> configProperties;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
