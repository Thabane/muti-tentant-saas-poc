# Implementation Plan: Tenant Authentication

## Overview

This implementation plan adds tenant authentication enhancements including automatic slug generation, onboarding flow management, feature flags, and tenant configuration. The implementation builds on the existing TenantService and integrates with the JWT authentication system being implemented in the jwt-authentication-migration spec.

## Tasks

- [ ] 1. Create SlugGenerator utility
  - [ ] 1.1 Implement SlugGenerator class
    - Create SlugGenerator in service package
    - Implement generateSlug(String organizationName, TenantRepository repository)
    - Implement sanitizeSlug(String input) - lowercase, replace special chars with hyphens
    - Implement ensureUnique(String baseSlug, TenantRepository repository)
    - Use regex pattern [^a-z0-9-]+ for sanitization
    - Use regex pattern -+ to collapse consecutive hyphens
    - Maximum 10 uniqueness attempts before throwing exception
    - _Requirements: 10.2, 10.3, 10.4, 10.5, 10.6_

  - [ ]* 1.2 Write property test for slug sanitization
    - **Property 4: Slug Sanitization and Format**
    - **Validates: Requirements 10.2, 10.3, 10.4, 10.6**

  - [ ]* 1.3 Write property test for slug uniqueness
    - **Property 5: Slug Uniqueness Enforcement**
    - **Validates: Requirements 10.5**

  - [ ]* 1.4 Write unit tests for SlugGenerator
    - Test slug generation from simple names
    - Test uppercase conversion to lowercase
    - Test space replacement with hyphens
    - Test special character removal
    - Test consecutive hyphen collapse
    - Test leading/trailing hyphen removal
    - Test uniqueness with numeric suffix
    - Test exception on max attempts exceeded
    - Test null/blank input validation
    - Test only special characters input
    - _Requirements: 10.2, 10.3, 10.4, 10.5, 10.6_

- [ ] 2. Add configuration column to tenants table
  - [ ] 2.1 Create Liquibase migration
    - Create new changeset in db/changelog
    - Add configuration column as JSONB type
    - Set default value to '{}'
    - _Requirements: 8.1_

  - [ ] 2.2 Update Tenant entity
    - Add configuration field with @JdbcTypeCode(SqlTypes.JSON)
    - Initialize as new HashMap<>()
    - _Requirements: 8.1_

  - [ ] 2.3 Update TenantResponse DTO
    - Add configuration field to record
    - Update toTenantResponse() method in TenantService
    - _Requirements: 8.1_

- [ ] 3. Create OnboardingService
  - [ ] 3.1 Implement OnboardingService class
    - Create package-private class in service package
    - Constructor inject TenantRepository
    - Implement completeOnboarding() method
    - Implement isOnboardingCompleted() method
    - Use TenantContextHolder to get current tenant ID
    - Throw UnauthorizedException if tenant context not set
    - Mark methods with @Transactional annotations
    - _Requirements: 6.2, 6.3_

  - [ ]* 3.2 Write property test for onboarding completion
    - **Property 11: Onboarding Completion Update**
    - **Validates: Requirements 6.3**

  - [ ]* 3.3 Write unit tests for OnboardingService
    - Test completeOnboarding with tenant context
    - Test completeOnboarding without tenant context throws exception
    - Test isOnboardingCompleted returns correct status
    - Test onboarding completion is idempotent
    - _Requirements: 6.2, 6.3_

- [ ] 4. Create FeatureService
  - [ ] 4.1 Implement FeatureService class
    - Create package-private class in service package
    - Constructor inject TenantRepository
    - Implement isFeatureEnabled(String featureKey) method
    - Implement getAllFeatures() method
    - Return false for missing feature flags (fail-safe default)
    - Use TenantContextHolder to get current tenant ID
    - Mark methods with @Transactional(readOnly = true)
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

  - [ ]* 4.2 Write property test for feature flag retrieval
    - **Property 12: Feature Flag Retrieval**
    - **Validates: Requirements 7.3**

  - [ ]* 4.3 Write property test for missing feature flag default
    - **Property 13: Missing Feature Flag Default**
    - **Validates: Requirements 7.4**

  - [ ]* 4.4 Write unit tests for FeatureService
    - Test isFeatureEnabled returns true for enabled feature
    - Test isFeatureEnabled returns false for disabled feature
    - Test isFeatureEnabled returns false for missing feature
    - Test getAllFeatures returns complete map
    - Test methods throw UnauthorizedException without tenant context
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

- [ ] 5. Create ConfigurationService
  - [ ] 5.1 Implement ConfigurationService class
    - Create package-private class in service package
    - Constructor inject TenantRepository
    - Implement getConfiguration() method
    - Implement updateConfiguration(Map<String, Object> configuration) method
    - Implement validateConfiguration(Map<String, Object> configuration) private method
    - Validate required keys, value types, and ranges
    - Throw BadRequestException for invalid configuration
    - Use TenantContextHolder to get current tenant ID
    - Mark methods with appropriate @Transactional annotations
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

  - [ ]* 5.2 Write property test for configuration schema validation
    - **Property 14: Configuration Schema Validation**
    - **Validates: Requirements 8.3, 8.4**

  - [ ]* 5.3 Write unit tests for ConfigurationService
    - Test getConfiguration returns tenant configuration
    - Test updateConfiguration with valid data succeeds
    - Test updateConfiguration with invalid schema throws exception
    - Test validation of required keys
    - Test validation of value types
    - Test validation of value ranges
    - Test methods throw UnauthorizedException without tenant context
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [ ] 6. Update TenantService
  - [ ] 6.1 Inject SlugGenerator dependency
    - Add SlugGenerator to constructor parameters
    - Store as final field
    - _Requirements: 10.1_

  - [ ] 6.2 Update RegisterRequest DTO
    - Remove slug field from record
    - Keep name, email, password fields
    - Update validation annotations
    - _Requirements: 1.3, 10.1_

  - [ ] 6.3 Update register() method
    - Remove slug uniqueness check
    - Call slugGenerator.generateSlug(request.name(), tenantRepository)
    - Initialize default features map
    - Initialize default configuration map
    - Set onboarding_completed to false
    - _Requirements: 1.5, 1.6, 8.2, 10.1_

  - [ ] 6.4 Remove completeOnboarding() method
    - Delete method from TenantService
    - Logic moved to OnboardingService
    - _Requirements: 6.2_

  - [ ]* 6.5 Write property test for registration initialization
    - **Property 2: Registration Initialization**
    - **Validates: Requirements 1.5, 1.6, 8.2**

  - [ ]* 6.6 Write property test for email uniqueness
    - **Property 1: Email Uniqueness Validation**
    - **Validates: Requirements 1.1, 1.2**

  - [ ]* 6.7 Write property test for password hash exclusion
    - **Property 3: Password Hash Exclusion**
    - **Validates: Requirements 1.7, 9.4**

  - [ ]* 6.8 Update unit tests for TenantService
    - Update register tests to not provide slug
    - Verify slug is auto-generated
    - Verify default features are initialized
    - Verify default configuration is initialized
    - Verify onboarding_completed is false
    - Remove completeOnboarding tests (moved to OnboardingService)
    - _Requirements: 1.5, 1.6, 8.2, 10.1_

- [ ] 7. Update TenantController
  - [ ] 7.1 Inject new services
    - Add OnboardingService to constructor
    - Add FeatureService to constructor
    - Add ConfigurationService to constructor
    - _Requirements: 6.1, 7.1, 8.1_

  - [ ] 7.2 Add onboarding endpoints
    - Implement POST /api/auth/onboarding/complete
    - Delegate to OnboardingService.completeOnboarding()
    - Return 200 OK with no body
    - _Requirements: 6.1, 6.3_

  - [ ] 7.3 Add feature flag endpoints
    - Implement GET /api/auth/features
    - Implement GET /api/auth/features/{featureKey}
    - Delegate to FeatureService methods
    - Return appropriate response types
    - _Requirements: 7.1, 7.2_

  - [ ] 7.4 Add configuration endpoints
    - Implement GET /api/auth/configuration
    - Implement PUT /api/auth/configuration
    - Create ConfigurationRequest DTO with @NotNull validation
    - Delegate to ConfigurationService methods
    - Return appropriate response types
    - _Requirements: 8.1, 8.2_

  - [ ]* 7.5 Write integration tests for TenantController
    - Test POST /api/auth/register without slug field
    - Test POST /api/auth/onboarding/complete
    - Test GET /api/auth/features
    - Test GET /api/auth/features/{key}
    - Test GET /api/auth/configuration
    - Test PUT /api/auth/configuration with valid data
    - Test PUT /api/auth/configuration with invalid data
    - Test authentication requirements for protected endpoints
    - _Requirements: 1.3, 6.1, 6.3, 7.1, 7.2, 8.1, 8.2_

- [ ] 8. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 9. Create property-based tests for tenant isolation
  - [ ]* 9.1 Write property test for thread-local tenant context isolation
    - **Property 6: Thread-Local Tenant Context Isolation**
    - **Validates: Requirements 4.2**

  - [ ]* 9.2 Write property test for repository query tenant filtering
    - **Property 7: Repository Query Tenant Filtering**
    - **Validates: Requirements 5.2**

  - [ ]* 9.3 Write property test for automatic tenant ID assignment
    - **Property 8: Automatic Tenant ID Assignment**
    - **Validates: Requirements 5.3**

  - [ ]* 9.4 Write property test for cross-tenant ownership verification
    - **Property 9: Cross-Tenant Ownership Verification**
    - **Validates: Requirements 5.4, 5.5, 5.6, 6.4, 8.5**

  - [ ]* 9.5 Write property test for onboarding status in login response
    - **Property 10: Onboarding Status in Login Response**
    - **Validates: Requirements 6.1**

  - [ ]* 9.6 Write property test for cross-tenant access logging
    - **Property 15: Cross-Tenant Access Logging**
    - **Validates: Requirements 12.5**

- [ ] 10. Integration testing
  - [ ]* 10.1 Write end-to-end registration and onboarding test
    - Test complete registration flow with auto-generated slug
    - Test login returns onboarding_completed status
    - Test onboarding completion flow
    - Test subsequent login redirects to dashboard
    - _Requirements: 1.1, 1.5, 1.6, 6.1, 6.3_

  - [ ]* 10.2 Write end-to-end feature flag test
    - Test feature flag initialization during registration
    - Test feature flag retrieval
    - Test feature flag checks
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

  - [ ]* 10.3 Write end-to-end configuration test
    - Test configuration initialization during registration
    - Test configuration retrieval
    - Test configuration update with validation
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [ ] 11. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties with minimum 100 iterations
- Unit tests validate specific examples, edge cases, and error conditions
- Integration tests validate end-to-end flows
- The implementation assumes JWT authentication is handled by jwt-authentication-migration spec
- Security is currently disabled - password hashing will be added by jwt-authentication-migration spec
