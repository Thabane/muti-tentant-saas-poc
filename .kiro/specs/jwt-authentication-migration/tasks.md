# Implementation Plan: JWT Authentication Migration

## Overview

This plan implements JWT-based authentication for the Spring Boot backend, replacing plain text password storage with BCrypt hashing and integrating with Spring Security. The implementation maintains backward compatibility with existing JWT tokens from the Node.js backend while following Spring Security best practices.

## Tasks

- [ ] 1. Create JwtTokenProvider component
  - [ ] 1.1 Implement token generation with HS256 signing
    - Create JwtTokenProvider class in security package
    - Inject JWT_SECRET and expiration from configuration
    - Implement generateToken(UUID tenantId, String email) method
    - Use JJWT library Jwts.builder() to create tokens
    - Set claims: tenantId, email, iat, exp
    - Sign with HS256 algorithm using secret key
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

  - [ ]* 1.2 Write property test for token generation
    - **Property 1: Token Generation Contains Required Claims**
    - **Validates: Requirements 1.1**

  - [ ]* 1.3 Write property test for token structure conformance
    - **Property 2: Token Structure Conformance**
    - **Validates: Requirements 1.2, 1.4, 1.5, 3.2**

  - [ ]* 1.4 Write property test for token expiration
    - **Property 3: Token Expiration is Seven Days**
    - **Validates: Requirements 1.3, 3.3**

  - [ ] 1.5 Implement token validation
    - Implement validateToken(String token) method
    - Use Jwts.parser() with signing key
    - Catch and handle JJWT exceptions (ExpiredJwtException, MalformedJwtException, SignatureException)
    - Return boolean indicating validation success
    - Log validation failures at DEBUG level
    - _Requirements: 2.1, 2.2, 2.3, 7.1, 7.2, 7.3, 10.1_

  - [ ] 1.6 Implement claim extraction methods
    - Implement getTenantIdFromToken(String token) method
    - Implement getEmailFromToken(String token) method
    - Parse token and extract claims from payload
    - Convert tenantId string claim to UUID
    - _Requirements: 2.4, 2.5, 3.4, 3.5_

  - [ ]* 1.7 Write property test for token round-trip
    - **Property 4: Token Round-Trip Preserves Data**
    - **Validates: Requirements 2.4, 2.5, 3.4, 3.5**

  - [ ]* 1.8 Write property test for invalid signature rejection
    - **Property 5: Invalid Signature Rejection**
    - **Validates: Requirements 2.1, 2.2**

  - [ ]* 1.9 Write property test for expired token rejection
    - **Property 6: Expired Token Rejection**
    - **Validates: Requirements 2.3**

  - [ ]* 1.10 Write unit tests for JwtTokenProvider
    - Test token generation with valid inputs
    - Test claim extraction from valid tokens
    - Test validation with malformed tokens
    - Test exception types for different error conditions
    - _Requirements: 2.1, 2.2, 2.3, 7.1, 7.2, 7.3_

- [ ] 2. Create JwtAuthenticationFilter
  - [ ] 2.1 Implement filter class extending OncePerRequestFilter
    - Create JwtAuthenticationFilter in security package
    - Inject JwtTokenProvider via constructor
    - Override doFilterInternal method
    - Implement try-finally block for SecurityContext cleanup
    - _Requirements: 4.1, 6.5_

  - [ ] 2.2 Implement token extraction from Authorization header
    - Create extractTokenFromRequest(HttpServletRequest) method
    - Check for "Authorization" header presence
    - Verify header starts with "Bearer "
    - Extract and trim token substring
    - Return null if header missing or malformed
    - _Requirements: 4.2, 5.1, 5.2, 5.3, 5.4, 5.5_

  - [ ]* 2.3 Write property test for Bearer token extraction
    - **Property 7: Bearer Token Extraction**
    - **Validates: Requirements 4.2, 5.1, 5.5**

  - [ ]* 2.4 Write property test for skip validation on missing headers
    - **Property 10: Skip Validation for Missing or Malformed Headers**
    - **Validates: Requirements 5.2, 5.3, 5.4**

  - [ ] 2.5 Implement SecurityContext population on successful validation
    - Validate extracted token using JwtTokenProvider
    - Extract tenant ID and email from token
    - Create UsernamePasswordAuthenticationToken with tenant ID as principal
    - Set authentication in SecurityContextHolder
    - Log successful authentication at DEBUG level
    - _Requirements: 4.3, 6.1, 6.2, 6.3, 10.2_

  - [ ]* 2.6 Write property test for SecurityContext population
    - **Property 8: Security Context Population on Success**
    - **Validates: Requirements 4.3, 6.1, 6.2, 6.3**

  - [ ] 2.7 Implement graceful error handling
    - Catch all exceptions from token validation
    - Log errors at DEBUG level without exposing token contents
    - Do NOT throw exceptions or write error responses
    - Always call filterChain.doFilter() to continue request
    - _Requirements: 4.4, 7.4, 7.5, 10.1, 10.3_

  - [ ]* 2.8 Write property test for request continuation on failure
    - **Property 9: Request Continues on Validation Failure**
    - **Validates: Requirements 4.4, 7.4, 7.5**

  - [ ] 2.9 Implement SecurityContext cleanup in finally block
    - Clear SecurityContextHolder after request completion
    - Ensure cleanup happens even if exceptions occur
    - _Requirements: 6.5_

  - [ ]* 2.10 Write property test for SecurityContext cleanup
    - **Property 11: Security Context Cleanup**
    - **Validates: Requirements 6.5**

  - [ ]* 2.11 Write unit tests for JwtAuthenticationFilter
    - Test token extraction with valid Bearer headers
    - Test token extraction with whitespace trimming
    - Test skip validation for missing Authorization header
    - Test skip validation for non-Bearer headers
    - Test SecurityContext not set on validation failure
    - Test request continues on validation failure
    - Test logging at DEBUG level for validation failures
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 7.4, 7.5, 10.1_

- [ ] 3. Configure Spring Security
  - [ ] 3.1 Create SecurityConfig class
    - Create SecurityConfig in config package
    - Annotate with @Configuration and @EnableWebSecurity
    - _Requirements: 4.5, 8.5_

  - [ ] 3.2 Define JwtTokenProvider bean with configuration validation
    - Create @Bean method for JwtTokenProvider
    - Inject JWT_SECRET from @Value("${jwt.secret}")
    - Inject expiration from @Value("${jwt.expiration:604800000}")
    - Validate JWT_SECRET is not null or blank
    - Validate JWT_SECRET length >= 32 characters
    - Throw IllegalStateException with clear message if invalid
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 10.4_

  - [ ]* 3.3 Write property test for secret length validation
    - **Property 13: Secret Length Validation**
    - **Validates: Requirements 8.4**

  - [ ] 3.4 Define PasswordEncoder bean
    - Create @Bean method returning BCryptPasswordEncoder
    - Use default strength (10 rounds)
    - _Requirements: (implicit for password hashing)_

  - [ ] 3.5 Configure HTTP security with filter chain
    - Create @Bean method for SecurityFilterChain
    - Disable CSRF (stateless JWT authentication)
    - Set session management to STATELESS
    - Configure CORS to allow frontend origin
    - Define public endpoints: /api/auth/login, /api/auth/register, /actuator/health, /actuator/info
    - Require authentication for /api/**
    - Add JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter
    - _Requirements: 4.1, 4.5_

  - [ ]* 3.6 Write unit tests for SecurityConfig
    - Test JwtTokenProvider bean creation with valid configuration
    - Test application startup failure with missing JWT_SECRET
    - Test application startup failure with short JWT_SECRET
    - Test filter chain includes JwtAuthenticationFilter
    - _Requirements: 8.1, 8.3, 8.4_

- [ ] 4. Update TenantService for JWT integration
  - [ ] 4.1 Inject JwtTokenProvider and PasswordEncoder
    - Add JwtTokenProvider and PasswordEncoder to constructor parameters
    - Store as final fields
    - _Requirements: 1.1, (implicit for password hashing)_

  - [ ] 4.2 Update register method to hash passwords and generate JWT
    - Hash password with passwordEncoder.encode() before saving
    - Generate JWT token after successful tenant creation
    - Return AuthResponse with tenant and JWT token
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

  - [ ] 4.3 Update login method to validate passwords and generate JWT
    - Retrieve tenant by email
    - Validate password with passwordEncoder.matches()
    - Throw UnauthorizedException("Invalid credentials") if validation fails
    - Generate JWT token after successful validation
    - Return AuthResponse with tenant and JWT token
    - _Requirements: 1.1, 2.1, 2.2, 2.3, 7.1_

  - [ ] 4.4 Update getCurrentTenant to require authentication
    - Remove fallback to default tenant
    - Get tenant ID from SecurityContextHolder
    - Throw UnauthorizedException if authentication missing
    - _Requirements: 6.4_

  - [ ] 4.5 Update completeOnboarding to require authentication
    - Remove fallback to default tenant
    - Get tenant ID from SecurityContextHolder
    - Throw UnauthorizedException if authentication missing
    - _Requirements: 6.4_

  - [ ]* 4.6 Write integration tests for TenantService
    - Test register creates tenant with hashed password
    - Test register returns JWT token in AuthResponse
    - Test login validates password with BCrypt
    - Test login returns JWT token in AuthResponse
    - Test login fails with invalid email
    - Test login fails with invalid password
    - Test getCurrentTenant requires authentication
    - Test completeOnboarding requires authentication
    - _Requirements: 1.1, 2.1, 2.2, 2.3, 6.4, 7.1_

- [ ] 5. Add configuration properties
  - [ ] 5.1 Update application.properties with JWT configuration
    - Add jwt.secret=${JWT_SECRET} property
    - Add jwt.expiration=604800000 property (7 days in milliseconds)
    - Add comment explaining expiration value
    - _Requirements: 8.1, 8.2_

  - [ ] 5.2 Update .env.example with JWT_SECRET
    - Add JWT_SECRET=your-secret-key-at-least-32-characters-long
    - Add comment about minimum length requirement
    - _Requirements: 8.1, 8.4_

- [ ] 6. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 7. Write property-based tests for thread safety
  - [ ]* 7.1 Write property test for thread safety and isolation
    - **Property 14: Thread Safety and Isolation**
    - **Validates: Requirements 9.1, 9.3, 9.5**
    - Generate tokens for multiple tenants
    - Validate concurrently from multiple threads using ExecutorService
    - Verify each thread gets correct tenant ID without cross-contamination

  - [ ]* 7.2 Write property test for no sensitive data in logs
    - **Property 15: No Sensitive Data in Logs**
    - **Validates: Requirements 10.3**
    - Configure log capture in test
    - Perform token operations with various inputs
    - Assert logs never contain JWT_SECRET or full token contents

- [ ] 8. Write integration tests for end-to-end authentication flow
  - [ ]* 8.1 Write integration test for authenticated request access
    - Use @SpringBootTest with RANDOM_PORT and @AutoConfigureMockMvc
    - Register user and extract JWT token from response
    - Make authenticated request to /api/tenant/me with Bearer token
    - Assert 200 OK response with correct tenant data
    - _Requirements: 4.1, 4.2, 4.3, 6.1, 6.2, 6.3_

  - [ ]* 8.2 Write integration test for unauthenticated request rejection
    - Make request to /api/tenant/me without Authorization header
    - Assert 401 Unauthorized response
    - _Requirements: 4.4, 7.5_

  - [ ]* 8.3 Write integration test for invalid token rejection
    - Make request to /api/tenant/me with invalid Bearer token
    - Assert 401 Unauthorized response
    - _Requirements: 2.2, 2.3, 4.4, 7.4, 7.5_

  - [ ]* 8.4 Write integration test for public endpoint access
    - Make request to /actuator/health without authentication
    - Assert 200 OK response
    - _Requirements: (implicit for public endpoints)_

  - [ ]* 8.5 Write integration test for backward compatibility
    - Generate token using Node.js-compatible format
    - Validate token with JwtTokenProvider
    - Assert successful validation and correct claim extraction
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 9. Update TenantContextHolder integration
  - [ ] 9.1 Update JwtAuthenticationFilter to populate TenantContextHolder
    - After successful token validation, call TenantContextHolder.setTenantId()
    - Clear TenantContextHolder in finally block alongside SecurityContext
    - _Requirements: 6.5, 9.2_

  - [ ] 9.2 Update service methods to use TenantContextHolder
    - Verify TenantContextHolder.getTenantId() returns correct value
    - Ensure existing tenant-scoped queries continue to work
    - _Requirements: 6.4, 9.3_

  - [ ]* 9.3 Write unit tests for TenantContextHolder integration
    - Test TenantContextHolder populated after authentication
    - Test TenantContextHolder cleared after request completion
    - Test service methods access correct tenant context
    - _Requirements: 6.5, 9.2, 9.3_

- [ ] 10. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties
- Unit tests validate specific examples and edge cases
- Integration tests validate end-to-end authentication flow
- The implementation uses Java 17 with Spring Boot 3.1.5 and JJWT 0.12.5
- BCrypt password hashing replaces plain text storage (security improvement)
- Existing plain text passwords will not validate - recommend clearing test data
