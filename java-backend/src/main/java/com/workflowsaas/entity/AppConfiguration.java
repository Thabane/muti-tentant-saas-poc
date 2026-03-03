package com.workflowsaas.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AppConfiguration entity representing configuration settings for an app.
 * Includes source selection, parameters, model files, enrichment APIs, and publisher configuration.
 */
@Entity
@Table(name = "app_configurations",
       uniqueConstraints = @UniqueConstraint(name = "unique_config_per_app", columnNames = "app_id"),
       indexes = {
           @Index(name = "idx_app_config_tenant_id", columnList = "tenant_id"),
           @Index(name = "idx_app_config_app_id", columnList = "app_id")
       })
@Data
public class AppConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "app_id", nullable = false)
    private UUID appId;
    
    @Column(nullable = false, length = 10)
    private String source;
    
    @Column(name = "parameters_filename")
    private String parametersFilename;
    
    @Column(name = "parameters_content", columnDefinition = "TEXT")
    private String parametersContent;
    
    @Column(name = "model_filename")
    private String modelFilename;
    
    @Column(name = "model_content", columnDefinition = "TEXT")
    private String modelContent;
    
    @OneToMany(mappedBy = "appConfiguration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnrichmentApiConfig> enrichmentApis = new ArrayList<>();
    
    @OneToOne(mappedBy = "appConfiguration", cascade = CascadeType.ALL, orphanRemoval = true)
    private PublisherConfig publisher;
    
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
