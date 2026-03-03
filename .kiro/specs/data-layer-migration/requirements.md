# Requirements Document

## Introduction

This specification defines the requirements for migrating the data persistence layer from Node.js with Sequelize ORM to Java with Spring Data JPA. The migration must maintain compatibility with the existing PostgreSQL database schema, preserve data format compatibility, and ensure that both Node.js and Java backends can coexist during the transition period. The system will use Hibernate as the JPA implementation, Liquibase for schema versioning, and maintain support for PostgreSQL-specific features including JSONB columns and UUID primary keys.

## Glossary

- **JPA_Entity**: A Java class annotated with @Entity that maps to a database table
- **Repository**: A Spring Data JPA interface that provides database access methods
- **Liquibase**: A database schema migration tool that tracks and applies schema changes
- **JSONB**: PostgreSQL's binary JSON data type for efficient storage and querying of JSON documents
- **UUID**: Universally Unique Identifier used as primary key format
- **Tenant_Scope**: Query filtering mechanism that automatically restricts data access to a specific tenant
- **Entity_Lifecycle**: JPA callback methods that execute during entity state transitions
- **Cascade_Behavior**: Automatic propagation of operations from parent to child entities
- **Transaction_Boundary**: The scope within which database operations are atomic
- **Schema_Compatibility**: The ability for multiple application versions to work with the same database schema

## Requirements

### Requirement 1: JPA Entity Definitions

**User Story:** As a backend developer, I want JPA entities that match the existing database schema, so that the Java application can read and write data without schema changes.

#### Acceptance Criteria

1. THE JPA_Entity SHALL map to the existing database table using @Table annotation with exact table name
2. THE JPA_Entity SHALL define all columns with @Column annotations matching existing column names, types, and constraints
3. THE JPA_Entity SHALL use UUID type for primary key fields annotated with @Id
4. THE JPA_Entity SHALL include tenant_id field with @Column(nullable = false) for multi-tenant tables
5. THE JPA_Entity SHALL define created_at and updated_at timestamp fields matching Node.js format
6. THE JPA_Entity SHALL use @Type annotation for JSONB columns to enable PostgreSQL JSON support
7. THE JPA_Entity SHALL define foreign key relationships using @ManyToOne, @OneToMany, or @OneToOne annotations
8. THE JPA_Entity SHALL include @Index annotations for columns that have database indexes
9. FOR ALL JPA entities with JSONB columns, serialization and deserialization SHALL produce JSON equivalent to Node.js Sequelize output

### Requirement 2: Spring Data JPA Repositories

**User Story:** As a backend developer, I want Spring Data repositories with tenant-scoped queries, so that data access is automatically isolated by tenant.

#### Acceptance Criteria

1. THE Repository SHALL extend JpaRepository interface with entity type and UUID key type
2. THE Repository SHALL define custom query methods using @Query annotation for tenant-scoped operations
3. WHEN a query method includes tenantId parameter, THE Repository SHALL filter results by tenant_id column
4. THE Repository SHALL use JPQL syntax for custom queries to maintain database independence
5. THE Repository SHALL define method names following Spring Data naming conventions for automatic query generation
6. WHERE complex queries are needed, THE Repository SHALL use @Query with native SQL and nativeQuery = true
7. THE Repository SHALL include findById methods that verify tenant ownership before returning results

### Requirement 3: Liquibase Schema Management

**User Story:** As a DevOps engineer, I want Liquibase migrations for schema management, so that database changes are versioned and reproducible.

#### Acceptance Criteria

1. THE Liquibase SHALL define changesets in YAML format under db/changelog directory
2. THE Liquibase SHALL create initial schema changeset matching existing PostgreSQL schema exactly
3. WHEN the application starts, THE Liquibase SHALL execute pending changesets in order
4. THE Liquibase SHALL record executed changesets in databasechangelog table
5. THE Liquibase SHALL support rollback for reversible schema changes
6. THE Liquibase SHALL include preconditions to prevent duplicate execution
7. THE Liquibase SHALL define foreign key constraints with explicit names matching existing schema
8. THE Liquibase SHALL create indexes with names matching existing database indexes

### Requirement 4: JSONB Column Support

**User Story:** As a backend developer, I want JSONB column support for PostgreSQL, so that I can store and query JSON data efficiently.

#### Acceptance Criteria

1. THE JPA_Entity SHALL use @Type(JsonBinaryType.class) annotation for JSONB columns
2. THE JPA_Entity SHALL map JSONB columns to Java Map, List, or custom POJO types
3. WHEN an entity with JSONB column is persisted, THE Hibernate SHALL serialize Java object to JSON format
4. WHEN an entity with JSONB column is retrieved, THE Hibernate SHALL deserialize JSON to Java object
5. THE Repository SHALL support native queries with JSONB operators (@>, ->, ->>) for JSON querying
6. FOR ALL JSONB columns, NULL values SHALL be stored as database NULL not JSON null
7. THE Hibernate SHALL preserve JSON structure and data types during round-trip serialization

### Requirement 5: UUID Primary Key Generation

**User Story:** As a backend developer, I want UUID primary key generation compatible with PostgreSQL, so that primary keys match the existing format.

#### Acceptance Criteria

1. THE JPA_Entity SHALL use @GeneratedValue(strategy = GenerationType.AUTO) for UUID primary keys
2. THE Hibernate SHALL generate UUID values using PostgreSQL uuid_generate_v4() function
3. WHEN a new entity is persisted, THE Hibernate SHALL assign UUID before database insertion
4. THE JPA_Entity SHALL use java.util.UUID type for primary key fields
5. THE Hibernate SHALL format UUID as lowercase hyphenated string (xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)
6. FOR ALL entities, UUID generation SHALL be compatible with existing Node.js UUID format

### Requirement 6: Timestamp Handling

**User Story:** As a backend developer, I want timestamp handling matching Node.js format, so that timestamps are consistent across both backends.

#### Acceptance Criteria

1. THE JPA_Entity SHALL define created_at field with @Column(nullable = false, updatable = false)
2. THE JPA_Entity SHALL define updated_at field with @Column(nullable = false)
3. THE JPA_Entity SHALL use @CreationTimestamp annotation for created_at field
4. THE JPA_Entity SHALL use @UpdateTimestamp annotation for updated_at field
5. THE Hibernate SHALL store timestamps in UTC timezone
6. THE Hibernate SHALL use TIMESTAMP WITHOUT TIME ZONE column type for timestamp fields
7. WHEN an entity is created, THE Hibernate SHALL set created_at to current UTC timestamp
8. WHEN an entity is updated, THE Hibernate SHALL update updated_at to current UTC timestamp
9. FOR ALL timestamp fields, the format SHALL match ISO 8601 format used by Node.js

### Requirement 7: Foreign Key Constraints and Cascade Behavior

**User Story:** As a backend developer, I want foreign key constraints with proper cascade behavior, so that referential integrity is maintained automatically.

#### Acceptance Criteria

1. THE JPA_Entity SHALL define @ManyToOne relationships with @JoinColumn specifying foreign key column name
2. THE JPA_Entity SHALL use cascade = CascadeType.REMOVE for parent-child relationships requiring deletion propagation
3. THE JPA_Entity SHALL use orphanRemoval = true for one-to-many relationships where children cannot exist independently
4. THE Liquibase SHALL create foreign key constraints with ON DELETE CASCADE or ON DELETE RESTRICT matching existing schema
5. WHEN a parent entity is deleted, THE Hibernate SHALL cascade delete to child entities if configured
6. THE JPA_Entity SHALL use @OnDelete(action = OnDeleteAction.CASCADE) for database-level cascade
7. THE Repository SHALL not allow deletion of entities with dependent records unless cascade is configured

### Requirement 8: Database Connection Configuration

**User Story:** As a DevOps engineer, I want database connection configuration, so that the application can connect to PostgreSQL with optimal settings.

#### Acceptance Criteria

1. THE Application SHALL read database URL from spring.datasource.url property
2. THE Application SHALL read database credentials from spring.datasource.username and spring.datasource.password
3. THE Application SHALL configure HikariCP connection pool with spring.datasource.hikari properties
4. THE Application SHALL set maximum pool size to 10 connections for production environments
5. THE Application SHALL set connection timeout to 30 seconds
6. THE Application SHALL enable connection validation with spring.datasource.hikari.connection-test-query
7. THE Application SHALL set spring.jpa.database-platform to org.hibernate.dialect.PostgreSQLDialect
8. THE Application SHALL disable spring.jpa.open-in-view to prevent lazy loading issues
9. WHERE environment variables are provided, THE Application SHALL override property file values

### Requirement 9: Transaction Management

**User Story:** As a backend developer, I want declarative transaction management, so that database operations are atomic and consistent.

#### Acceptance Criteria

1. THE Service SHALL annotate query methods with @Transactional(readOnly = true)
2. THE Service SHALL annotate data-modifying methods with @Transactional
3. WHEN a @Transactional method completes successfully, THE Spring SHALL commit the transaction
4. IF an exception occurs in @Transactional method, THEN THE Spring SHALL rollback the transaction
5. THE Service SHALL use default transaction propagation (REQUIRED) unless explicitly specified
6. THE Service SHALL set transaction isolation level to READ_COMMITTED for PostgreSQL
7. THE Service SHALL keep transaction scope minimal to reduce lock duration
8. WHERE multiple repository operations are needed, THE Service SHALL group them in single @Transactional method

### Requirement 10: Entity Lifecycle Callbacks

**User Story:** As a backend developer, I want entity lifecycle callbacks, so that I can execute logic during entity state transitions.

#### Acceptance Criteria

1. THE JPA_Entity SHALL use @PrePersist annotation for methods executing before entity insertion
2. THE JPA_Entity SHALL use @PreUpdate annotation for methods executing before entity update
3. THE JPA_Entity SHALL use @PostLoad annotation for methods executing after entity retrieval
4. WHEN @PrePersist is triggered, THE JPA_Entity SHALL validate required fields are set
5. WHEN @PreUpdate is triggered, THE JPA_Entity SHALL update the updated_at timestamp
6. THE JPA_Entity SHALL use lifecycle callbacks for audit logging if required
7. THE JPA_Entity SHALL not perform database operations within lifecycle callback methods

### Requirement 11: Tenant Entity Migration

**User Story:** As a backend developer, I want Tenant entity matching the existing schema, so that tenant authentication works with both backends.

#### Acceptance Criteria

1. THE Tenant SHALL map to tenants table with columns: id, name, slug, email, password_hash, features, onboarding_completed, created_at, updated_at
2. THE Tenant SHALL use UUID type for id field with @GeneratedValue
3. THE Tenant SHALL define email field with @Column(unique = true, nullable = false)
4. THE Tenant SHALL define slug field with @Column(unique = true, nullable = false)
5. THE Tenant SHALL use @Type(JsonBinaryType.class) for features JSONB column
6. THE Tenant SHALL map features to Map<String, Object> Java type
7. THE Tenant SHALL define password_hash field with @Column(nullable = false)
8. THE Tenant SHALL define onboarding_completed as Boolean with default false

### Requirement 12: Workflow Entity Migration

**User Story:** As a backend developer, I want Workflow entity matching the existing schema, so that workflow definitions are accessible from Java backend.

#### Acceptance Criteria

1. THE Workflow SHALL map to workflows table with columns: id, tenant_id, app_id, name, type, bpmn_xml, dmn_xml, version, status, created_at, updated_at
2. THE Workflow SHALL use UUID type for id, tenant_id, and app_id fields
3. THE Workflow SHALL define @ManyToOne relationship to Tenant entity with @JoinColumn(name = "tenant_id")
4. THE Workflow SHALL define @ManyToOne relationship to App entity with @JoinColumn(name = "app_id")
5. THE Workflow SHALL use @Column(columnDefinition = "TEXT") for bpmn_xml and dmn_xml fields
6. THE Workflow SHALL define type field as String with @Column(nullable = false)
7. THE Workflow SHALL define version field as Integer with default value 1
8. THE Workflow SHALL define status field as String with default value "draft"
9. THE Workflow SHALL include foreign key constraint on tenant_id with ON DELETE CASCADE

### Requirement 13: Deployment Entity Migration

**User Story:** As a backend developer, I want Deployment entity matching the existing schema, so that deployment configurations are managed consistently.

#### Acceptance Criteria

1. THE Deployment SHALL map to deployments table with columns: id, tenant_id, workflow_id, environment, deployment_key, status, rollout_percentage, metadata, created_at, updated_at
2. THE Deployment SHALL use UUID type for id, tenant_id, and workflow_id fields
3. THE Deployment SHALL define @ManyToOne relationship to Tenant with @JoinColumn(name = "tenant_id")
4. THE Deployment SHALL define @ManyToOne relationship to Workflow with @JoinColumn(name = "workflow_id")
5. THE Deployment SHALL use @Type(JsonBinaryType.class) for metadata JSONB column
6. THE Deployment SHALL define rollout_percentage as Integer with default value 0
7. THE Deployment SHALL define environment field with @Column(nullable = false)
8. THE Deployment SHALL define deployment_key field with @Column(unique = true)
9. THE Deployment SHALL include composite unique constraint on (tenant_id, workflow_id, environment)

### Requirement 14: WorkflowExecution Entity Migration

**User Story:** As a backend developer, I want WorkflowExecution entity matching the existing schema, so that execution history is tracked consistently.

#### Acceptance Criteria

1. THE WorkflowExecution SHALL map to workflow_executions table with columns: id, tenant_id, workflow_id, input_data, output_data, status, error_message, started_at, completed_at, created_at, updated_at
2. THE WorkflowExecution SHALL use UUID type for id, tenant_id, and workflow_id fields
3. THE WorkflowExecution SHALL define @ManyToOne relationship to Tenant with @JoinColumn(name = "tenant_id")
4. THE WorkflowExecution SHALL define @ManyToOne relationship to Workflow with @JoinColumn(name = "workflow_id")
5. THE WorkflowExecution SHALL use @Type(JsonBinaryType.class) for input_data and output_data JSONB columns
6. THE WorkflowExecution SHALL define status field with @Column(nullable = false)
7. THE WorkflowExecution SHALL define started_at and completed_at as nullable timestamp fields
8. THE WorkflowExecution SHALL define error_message as nullable TEXT field

### Requirement 15: Schema Compatibility Validation

**User Story:** As a DevOps engineer, I want schema compatibility validation, so that I can verify both backends work with the same database.

#### Acceptance Criteria

1. THE Application SHALL validate JPA entity mappings against database schema on startup
2. WHEN spring.jpa.hibernate.ddl-auto is set to validate, THE Hibernate SHALL verify schema matches entity definitions
3. IF schema mismatch is detected, THEN THE Application SHALL fail to start with descriptive error message
4. THE Application SHALL log all entity mappings during startup in debug mode
5. THE Application SHALL provide integration test that verifies CRUD operations for all entities
6. THE Application SHALL include test that compares JSON output format with Node.js Sequelize format
7. FOR ALL entities, integration tests SHALL verify foreign key constraints are enforced
