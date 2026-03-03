package com.workflowsaas.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * App entity representing a logical container for BPMN and DMN resources.
 * Each app has a unique API key for authentication.
 */
@Entity
@Table(name = "apps",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name"}),
       indexes = {
           @Index(name = "idx_apps_tenant_id", columnList = "tenant_id"),
           @Index(name = "idx_apps_api_key_hash", columnList = "api_key_hash")
       })
@Data
public class App {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "api_key_hash", nullable = false, unique = true)
    private String apiKeyHash;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore  // Prevent circular reference during JSON serialization
    private List<Workflow> workflows = new ArrayList<>();
    
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
