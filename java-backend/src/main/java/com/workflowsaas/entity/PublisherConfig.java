package com.workflowsaas.entity;

import com.workflowsaas.converter.JsonbConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * PublisherConfig entity representing publisher configuration for an app.
 * Supports two types: 'api' (API-based publishing) and 'eeh' (Event Hub publishing).
 * For EEH type, an optional WarehousePublisherConfig can be associated.
 * The config_properties field uses JSONB type (converted with JsonbConverter).
 */
@Entity
@Table(name = "publisher_configs",
       uniqueConstraints = @UniqueConstraint(name = "unique_publisher_per_app_config", columnNames = "app_config_id"),
       indexes = {
           @Index(name = "idx_publisher_config_tenant_id", columnList = "tenant_id"),
           @Index(name = "idx_publisher_config_app_config_id", columnList = "app_config_id")
       })
@Data
public class PublisherConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_config_id", nullable = false)
    private AppConfiguration appConfiguration;
    
    @Column(nullable = false, length = 10)
    private String type;
    
    @Column(name = "openapi_document", columnDefinition = "TEXT")
    private String openApiDocument;
    
    @Column(name = "request_mappings", columnDefinition = "TEXT")
    private String requestMappings;
    
    @Column(name = "config_properties", columnDefinition = "JSONB")
    @Convert(converter = JsonbConverter.class)
    private Map<String, String> configProperties;
    
    @OneToOne(mappedBy = "publisherConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    private WarehousePublisherConfig warehousePublisher;
    
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
