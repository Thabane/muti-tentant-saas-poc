package com.workflowsaas.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Workflow entity representing a BPMN or DMN process definition.
 * Workflows are versioned and tenant-scoped.
 */
@Entity
@Table(name = "workflows", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name"}),
       indexes = {
           @Index(name = "idx_workflows_app_id", columnList = "app_id"),
           @Index(name = "idx_workflows_parent_id", columnList = "parent_workflow_id"),
           @Index(name = "idx_workflows_api_path", columnList = "api_path")
       })
@Data
public class Workflow {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_id")
    private App app;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "workflow_type", nullable = false, length = 50)
    private String type;
    
    @Column(name = "bpmn_xml", columnDefinition = "TEXT")
    private String bpmnXml;
    
    // Note: version and status columns don't exist in database yet
    // Keeping these as transient until migration is added
    @Transient
    private Integer version = 1;
    
    @Transient
    private String status = "draft";
    
    @Column(name = "parent_workflow_id")
    private UUID parentWorkflowId;
    
    @Column(name = "api_path", length = 500)
    private String apiPath;
    
    @Column(name = "sub_service")
    private String subService;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
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
