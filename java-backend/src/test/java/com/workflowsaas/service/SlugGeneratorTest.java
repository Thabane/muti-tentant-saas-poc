package com.workflowsaas.service;

import com.workflowsaas.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SlugGenerator.
 * Tests slug generation, sanitization, uniqueness, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class SlugGeneratorTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private SlugGenerator slugGenerator;

    @BeforeEach
    void setUp() {
        // Default: all slugs are unique (not found in database)
        // Use lenient() to avoid UnnecessaryStubbingException in tests that throw exceptions early
        lenient().when(tenantRepository.existsBySlug(anyString())).thenReturn(false);
    }

    @Test
    void testGenerateSlug_SimpleOrganizationName() {
        String result = slugGenerator.generateSlug("Acme Corp", tenantRepository);

        assertEquals("acme-corp", result);
        verify(tenantRepository, times(1)).existsBySlug("acme-corp");
    }

    @Test
    void testGenerateSlug_UppercaseConversion() {
        String result = slugGenerator.generateSlug("ACME CORPORATION", tenantRepository);

        assertEquals("acme-corporation", result);
    }

    @Test
    void testGenerateSlug_SpaceReplacement() {
        String result = slugGenerator.generateSlug("My Company Name", tenantRepository);

        assertEquals("my-company-name", result);
    }

    @Test
    void testGenerateSlug_SpecialCharacterRemoval() {
        String result = slugGenerator.generateSlug("Acme@Corp#123!", tenantRepository);

        assertEquals("acme-corp-123", result);
    }

    @Test
    void testGenerateSlug_ConsecutiveHyphenCollapse() {
        String result = slugGenerator.generateSlug("Acme   Corp", tenantRepository);

        assertEquals("acme-corp", result);
    }

    @Test
    void testGenerateSlug_LeadingTrailingHyphenRemoval() {
        String result = slugGenerator.generateSlug("  Acme Corp  ", tenantRepository);

        assertEquals("acme-corp", result);
    }

    @Test
    void testGenerateSlug_WithExistingHyphens() {
        String result = slugGenerator.generateSlug("acme-corp-inc", tenantRepository);

        assertEquals("acme-corp-inc", result);
    }

    @Test
    void testGenerateSlug_WithNumbers() {
        String result = slugGenerator.generateSlug("Company123", tenantRepository);

        assertEquals("company123", result);
    }

    @Test
    void testGenerateSlug_MixedSpecialCharacters() {
        String result = slugGenerator.generateSlug("Acme & Co. (2024)", tenantRepository);

        assertEquals("acme-co-2024", result);
    }

    @Test
    void testGenerateSlug_UniquenessWithNumericSuffix() {
        // First slug exists, second is unique
        when(tenantRepository.existsBySlug("acme-corp")).thenReturn(true);
        when(tenantRepository.existsBySlug("acme-corp-2")).thenReturn(false);

        String result = slugGenerator.generateSlug("Acme Corp", tenantRepository);

        assertEquals("acme-corp-2", result);
        verify(tenantRepository, times(1)).existsBySlug("acme-corp");
        verify(tenantRepository, times(1)).existsBySlug("acme-corp-2");
    }

    @Test
    void testGenerateSlug_MultipleConflicts() {
        // First three slugs exist, fourth is unique
        when(tenantRepository.existsBySlug("acme-corp")).thenReturn(true);
        when(tenantRepository.existsBySlug("acme-corp-2")).thenReturn(true);
        when(tenantRepository.existsBySlug("acme-corp-3")).thenReturn(true);
        when(tenantRepository.existsBySlug("acme-corp-4")).thenReturn(false);

        String result = slugGenerator.generateSlug("Acme Corp", tenantRepository);

        assertEquals("acme-corp-4", result);
        verify(tenantRepository, times(4)).existsBySlug(anyString());
    }

    @Test
    void testGenerateSlug_MaxAttemptsExceeded() {
        // All slugs exist (simulate max attempts exceeded)
        when(tenantRepository.existsBySlug(anyString())).thenReturn(true);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> slugGenerator.generateSlug("Acme Corp", tenantRepository)
        );

        assertTrue(exception.getMessage().contains("Unable to generate unique slug"));
        assertTrue(exception.getMessage().contains("10 attempts"));
        assertTrue(exception.getMessage().contains("acme-corp"));
    }

    @Test
    void testGenerateSlug_NullOrganizationName() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> slugGenerator.generateSlug(null, tenantRepository)
        );

        assertEquals("Organization name cannot be null or blank", exception.getMessage());
    }

    @Test
    void testGenerateSlug_BlankOrganizationName() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> slugGenerator.generateSlug("   ", tenantRepository)
        );

        assertEquals("Organization name cannot be null or blank", exception.getMessage());
    }

    @Test
    void testGenerateSlug_EmptyOrganizationName() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> slugGenerator.generateSlug("", tenantRepository)
        );

        assertEquals("Organization name cannot be null or blank", exception.getMessage());
    }

    @Test
    void testGenerateSlug_OnlySpecialCharacters() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> slugGenerator.generateSlug("@#$%^&*()", tenantRepository)
        );

        assertEquals("Organization name must contain at least one alphanumeric character", exception.getMessage());
    }

    @Test
    void testGenerateSlug_OnlySpaces() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> slugGenerator.generateSlug("     ", tenantRepository)
        );

        assertEquals("Organization name cannot be null or blank", exception.getMessage());
    }

    @Test
    void testGenerateSlug_SingleCharacter() {
        String result = slugGenerator.generateSlug("A", tenantRepository);

        assertEquals("a", result);
    }

    @Test
    void testGenerateSlug_SingleNumber() {
        String result = slugGenerator.generateSlug("1", tenantRepository);

        assertEquals("1", result);
    }

    @Test
    void testGenerateSlug_AlphanumericOnly() {
        String result = slugGenerator.generateSlug("abc123xyz", tenantRepository);

        assertEquals("abc123xyz", result);
    }

    @Test
    void testGenerateSlug_ComplexRealWorldExample() {
        String result = slugGenerator.generateSlug("Smith & Johnson LLC (Est. 2020)", tenantRepository);

        assertEquals("smith-johnson-llc-est-2020", result);
    }

    @Test
    void testGenerateSlug_UnicodeCharacters() {
        // Unicode characters should be replaced with hyphens
        String result = slugGenerator.generateSlug("Café Münchën", tenantRepository);

        assertEquals("caf-m-nch-n", result);
    }

    @Test
    void testGenerateSlug_LeadingSpecialCharacters() {
        String result = slugGenerator.generateSlug("!!!Acme Corp", tenantRepository);

        assertEquals("acme-corp", result);
    }

    @Test
    void testGenerateSlug_TrailingSpecialCharacters() {
        String result = slugGenerator.generateSlug("Acme Corp!!!", tenantRepository);

        assertEquals("acme-corp", result);
    }

    @Test
    void testGenerateSlug_MiddleSpecialCharacters() {
        String result = slugGenerator.generateSlug("Acme!!!Corp", tenantRepository);

        assertEquals("acme-corp", result);
    }

    @Test
    void testGenerateSlug_AlreadyValidSlug() {
        String result = slugGenerator.generateSlug("acme-corp", tenantRepository);

        assertEquals("acme-corp", result);
    }

    @Test
    void testGenerateSlug_WithDots() {
        String result = slugGenerator.generateSlug("Acme.Corp.Inc", tenantRepository);

        assertEquals("acme-corp-inc", result);
    }

    @Test
    void testGenerateSlug_WithUnderscores() {
        String result = slugGenerator.generateSlug("Acme_Corp_Inc", tenantRepository);

        assertEquals("acme-corp-inc", result);
    }
}
