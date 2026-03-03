import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import AppDetail from './AppDetail';
import { appAPI } from '../services/api';

// Mock the API module
vi.mock('../services/api', () => ({
  appAPI: {
    getById: vi.fn(),
    getConfig: vi.fn(),
    createConfig: vi.fn(),
    updateConfig: vi.fn(),
    createWorkflow: vi.fn(),
    regenerateKey: vi.fn(),
  },
}));

// Mock react-router-dom hooks
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useParams: () => ({ appId: 'test-app-id' }),
    useNavigate: () => vi.fn(),
    useLocation: () => ({ state: null }),
  };
});

// Mock child components
vi.mock('../components/ApiKeyDisplay', () => ({
  default: () => <div data-testid="api-key-display">API Key Display</div>,
}));

vi.mock('../components/SourceSelector', () => ({
  default: ({ value, onChange, error }) => (
    <div data-testid="source-selector">
      <select value={value} onChange={(e) => onChange(e.target.value)}>
        <option value="">Select source...</option>
        <option value="eeh">EEH</option>
        <option value="api">API</option>
      </select>
      {error && <span data-testid="source-error">{error}</span>}
    </div>
  ),
}));

vi.mock('../components/EnrichmentApiSection', () => ({
  default: () => <div data-testid="enrichment-api-section">Enrichment API Section</div>,
}));

vi.mock('../components/FileUpload', () => ({
  default: () => <div data-testid="file-upload">File Upload</div>,
}));

vi.mock('../components/PublisherSection', () => ({
  default: () => <div data-testid="publisher-section">Publisher Section</div>,
}));

describe('AppDetail Component', () => {
  const mockApp = {
    id: 'test-app-id',
    name: 'Test App',
    apiKey: 'app_test123',
    workflows: [],
  };

  const mockConfig = {
    id: 'config-id',
    source: 'api',
    enrichmentApis: [],
    parametersFile: null,
    modelFile: null,
    publisher: null,
  };

  beforeEach(() => {
    vi.clearAllMocks();
    appAPI.getById.mockResolvedValue({ data: mockApp });
    appAPI.getConfig.mockResolvedValue({ data: mockConfig });
  });

  const renderComponent = () => {
    return render(
      <BrowserRouter>
        <AppDetail />
      </BrowserRouter>
    );
  };

  describe('Source Validation', () => {
    it('should show error when saving config without selecting a source', async () => {
      // Mock config with empty source
      const emptySourceConfig = { ...mockConfig, source: '' };
      appAPI.getConfig.mockResolvedValue({ data: emptySourceConfig });

      renderComponent();

      // Wait for component to load
      await waitFor(() => {
        expect(screen.getByText('Test App')).toBeInTheDocument();
      });

      // Switch to configuration tab
      const configTab = screen.getByText('Configuration');
      fireEvent.click(configTab);

      // Wait for config to load
      await waitFor(() => {
        expect(screen.getByTestId('source-selector')).toBeInTheDocument();
      });

      // Try to save without selecting source
      const saveButton = screen.getByText('Save Configuration');
      fireEvent.click(saveButton);

      // Verify error message is displayed
      await waitFor(() => {
        const errorElement = screen.getByTestId('source-error');
        expect(errorElement).toBeInTheDocument();
        expect(errorElement).toHaveTextContent('Please select a data source (EEH or API)');
      });

      // Verify API was not called
      expect(appAPI.createConfig).not.toHaveBeenCalled();
      expect(appAPI.updateConfig).not.toHaveBeenCalled();
    });

    it('should show error when saving config with whitespace-only source', async () => {
      // Mock config with whitespace source
      const whitespaceSourceConfig = { ...mockConfig, source: '   ' };
      appAPI.getConfig.mockResolvedValue({ data: whitespaceSourceConfig });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Test App')).toBeInTheDocument();
      });

      // Switch to configuration tab
      const configTab = screen.getByText('Configuration');
      fireEvent.click(configTab);

      await waitFor(() => {
        expect(screen.getByTestId('source-selector')).toBeInTheDocument();
      });

      // Try to save with whitespace source
      const saveButton = screen.getByText('Save Configuration');
      fireEvent.click(saveButton);

      // Verify error message
      await waitFor(() => {
        const errorElement = screen.getByTestId('source-error');
        expect(errorElement).toHaveTextContent('Please select a data source (EEH or API)');
      });

      // Verify API was not called
      expect(appAPI.createConfig).not.toHaveBeenCalled();
      expect(appAPI.updateConfig).not.toHaveBeenCalled();
    });

    it('should allow saving when source is selected', async () => {
      appAPI.updateConfig.mockResolvedValue({ data: mockConfig });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Test App')).toBeInTheDocument();
      });

      // Switch to configuration tab
      const configTab = screen.getByText('Configuration');
      fireEvent.click(configTab);

      await waitFor(() => {
        expect(screen.getByTestId('source-selector')).toBeInTheDocument();
      });

      // Save with valid source
      const saveButton = screen.getByText('Save Configuration');
      fireEvent.click(saveButton);

      // Verify API was called
      await waitFor(() => {
        expect(appAPI.updateConfig).toHaveBeenCalledWith(
          'test-app-id',
          expect.objectContaining({
            source: 'api',
          })
        );
      });

      // Verify no error is shown
      expect(screen.queryByTestId('source-error')).not.toBeInTheDocument();
    });

    it('should clear previous errors when source is selected', async () => {
      // Start with empty source
      const emptySourceConfig = { ...mockConfig, source: '' };
      appAPI.getConfig.mockResolvedValue({ data: emptySourceConfig });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Test App')).toBeInTheDocument();
      });

      // Switch to configuration tab
      const configTab = screen.getByText('Configuration');
      fireEvent.click(configTab);

      await waitFor(() => {
        expect(screen.getByTestId('source-selector')).toBeInTheDocument();
      });

      // Try to save without source - should show error
      const saveButton = screen.getByText('Save Configuration');
      fireEvent.click(saveButton);

      await waitFor(() => {
        expect(screen.getByTestId('source-error')).toBeInTheDocument();
      });

      // Now select a source
      const sourceSelect = screen.getByRole('combobox');
      fireEvent.change(sourceSelect, { target: { value: 'eeh' } });

      // Try to save again
      appAPI.updateConfig.mockResolvedValue({ data: { ...mockConfig, source: 'eeh' } });
      fireEvent.click(saveButton);

      // Error should be cleared and API should be called
      await waitFor(() => {
        expect(screen.queryByTestId('source-error')).not.toBeInTheDocument();
        expect(appAPI.updateConfig).toHaveBeenCalled();
      });
    });
  });

  describe('Configuration Loading', () => {
    it('should load app and config on mount', async () => {
      renderComponent();

      await waitFor(() => {
        expect(appAPI.getById).toHaveBeenCalledWith('test-app-id');
        expect(screen.getByText('Test App')).toBeInTheDocument();
      });
    });

    it('should handle 404 when config does not exist', async () => {
      appAPI.getConfig.mockRejectedValue({
        response: { status: 404 },
      });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Test App')).toBeInTheDocument();
      });

      // Switch to configuration tab
      const configTab = screen.getByText('Configuration');
      fireEvent.click(configTab);

      // Should initialize empty config
      await waitFor(() => {
        expect(screen.getByTestId('source-selector')).toBeInTheDocument();
      });
    });

    it('should show error for non-404 config load failures', async () => {
      appAPI.getConfig.mockRejectedValue({
        response: { status: 500 },
      });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Test App')).toBeInTheDocument();
      });

      // Switch to configuration tab
      const configTab = screen.getByText('Configuration');
      fireEvent.click(configTab);

      await waitFor(() => {
        // Use getAllByText since there are multiple elements with this text
        const errorElements = screen.getAllByText(/Failed to load configuration/i);
        expect(errorElements.length).toBeGreaterThan(0);
      });
    });
  });

  describe('Configuration Save', () => {
    it('should call createConfig when config has no id', async () => {
      const newConfig = { ...mockConfig };
      delete newConfig.id;
      appAPI.getConfig.mockResolvedValue({ data: newConfig });
      appAPI.createConfig.mockResolvedValue({ data: mockConfig });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Test App')).toBeInTheDocument();
      });

      // Switch to configuration tab
      const configTab = screen.getByText('Configuration');
      fireEvent.click(configTab);

      await waitFor(() => {
        expect(screen.getByTestId('source-selector')).toBeInTheDocument();
      });

      // Save configuration
      const saveButton = screen.getByText('Save Configuration');
      fireEvent.click(saveButton);

      await waitFor(() => {
        expect(appAPI.createConfig).toHaveBeenCalledWith(
          'test-app-id',
          expect.any(Object)
        );
        expect(appAPI.updateConfig).not.toHaveBeenCalled();
      });
    });

    it('should call updateConfig when config has id', async () => {
      appAPI.updateConfig.mockResolvedValue({ data: mockConfig });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Test App')).toBeInTheDocument();
      });

      // Switch to configuration tab
      const configTab = screen.getByText('Configuration');
      fireEvent.click(configTab);

      await waitFor(() => {
        expect(screen.getByTestId('source-selector')).toBeInTheDocument();
      });

      // Save configuration
      const saveButton = screen.getByText('Save Configuration');
      fireEvent.click(saveButton);

      await waitFor(() => {
        expect(appAPI.updateConfig).toHaveBeenCalledWith(
          'test-app-id',
          expect.any(Object)
        );
        expect(appAPI.createConfig).not.toHaveBeenCalled();
      });
    });

    it('should show success message after successful save', async () => {
      appAPI.updateConfig.mockResolvedValue({ data: mockConfig });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Test App')).toBeInTheDocument();
      });

      // Switch to configuration tab
      const configTab = screen.getByText('Configuration');
      fireEvent.click(configTab);

      await waitFor(() => {
        expect(screen.getByTestId('source-selector')).toBeInTheDocument();
      });

      // Save configuration
      const saveButton = screen.getByText('Save Configuration');
      fireEvent.click(saveButton);

      // Verify success message
      await waitFor(() => {
        expect(screen.getByText('Configuration saved successfully!')).toBeInTheDocument();
      });
    });

    it('should handle save errors with field-specific errors', async () => {
      appAPI.updateConfig.mockRejectedValue({
        response: {
          data: {
            message: 'Validation failed',
            errors: { source: 'Invalid source value' },
          },
        },
      });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Test App')).toBeInTheDocument();
      });

      // Switch to configuration tab
      const configTab = screen.getByText('Configuration');
      fireEvent.click(configTab);

      await waitFor(() => {
        expect(screen.getByTestId('source-selector')).toBeInTheDocument();
      });

      // Save configuration
      const saveButton = screen.getByText('Save Configuration');
      fireEvent.click(saveButton);

      // Verify error is displayed
      await waitFor(() => {
        expect(screen.getByTestId('source-error')).toHaveTextContent('Invalid source value');
      });
    });

    it('should handle save errors with only error field', async () => {
      appAPI.updateConfig.mockRejectedValue({
        response: {
          data: {
            error: 'Configuration validation failed',
          },
        },
      });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Test App')).toBeInTheDocument();
      });

      // Switch to configuration tab
      const configTab = screen.getByText('Configuration');
      fireEvent.click(configTab);

      await waitFor(() => {
        expect(screen.getByTestId('source-selector')).toBeInTheDocument();
      });

      // Save configuration
      const saveButton = screen.getByText('Save Configuration');
      fireEvent.click(saveButton);

      // Verify general error is displayed
      await waitFor(() => {
        const errorElements = screen.getAllByText(/Configuration validation failed/i);
        expect(errorElements.length).toBeGreaterThan(0);
      });
    });

    it('should handle save errors with both error and errors fields', async () => {
      appAPI.updateConfig.mockRejectedValue({
        response: {
          data: {
            error: 'Validation failed',
            errors: { source: 'Source is required' },
          },
        },
      });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Test App')).toBeInTheDocument();
      });

      // Switch to configuration tab
      const configTab = screen.getByText('Configuration');
      fireEvent.click(configTab);

      await waitFor(() => {
        expect(screen.getByTestId('source-selector')).toBeInTheDocument();
      });

      // Save configuration
      const saveButton = screen.getByText('Save Configuration');
      fireEvent.click(saveButton);

      // Verify both general and field-specific errors are handled
      await waitFor(() => {
        expect(screen.getByTestId('source-error')).toHaveTextContent('Source is required');
      });
    });
  });

  describe('Tab Navigation', () => {
    it('should switch between workflows and configuration tabs', async () => {
      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Test App')).toBeInTheDocument();
      });

      // Initially on workflows tab - check for the tab button
      const workflowsTabButton = screen.getAllByText('Workflows').find(el => el.classList.contains('tab-button'));
      expect(workflowsTabButton).toBeInTheDocument();

      // Switch to configuration tab
      const configTab = screen.getByRole('button', { name: 'Configuration' });
      fireEvent.click(configTab);

      await waitFor(() => {
        expect(screen.getByTestId('source-selector')).toBeInTheDocument();
      });

      // Switch back to workflows tab
      const workflowsTab = screen.getByRole('button', { name: 'Workflows' });
      fireEvent.click(workflowsTab);

      await waitFor(() => {
        expect(screen.getByText(/No workflows yet/i)).toBeInTheDocument();
      });
    });
  });
});
