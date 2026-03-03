package com.workflowsaas.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WarehousePublisherConfig entity representing warehouse-specific publisher configuration.
 * This is associated with EEH (Event Hub) publishers and stores Avro schema and field mappings.
 * Has a one-to-one relationship with PublisherConfig.
 */
@Entity
@Table(name = "warehouse_publisher_configs",
       uniqueConstraints = @UniqueConstraint(name = "unique_warehouse_per_publisher", columnNames = "publisher_config_id"),
       indexes = {
           @Index(name = "idx_warehouse_publisher_tenant_id", columnList = "tenant_id"),
           @Index(name = "idx_warehouse_publisher_config_id", columnList = "publisher_config_id")
       })
@Data
public class WarehousePublisherConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_config_id", nullable = false)
    private PublisherConfig publisherConfig;
    
    @Column(name = "avro_schema", nullable = false, columnDefinition = "TEXT")
    private String avroSchema;
    
    @Column(columnDefinition = "TEXT")
    private String mappings;
    
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
