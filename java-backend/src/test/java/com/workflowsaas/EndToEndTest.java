package com.workflowsaas;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test that verifies the entire application stack.
 * This test starts the full Spring Boot application and makes real HTTP requests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EndToEndTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testApplicationStartsSuccessfully() {
        // Verify the application context loads
        assertNotNull(restTemplate, "TestRestTemplate should be autowired");
        assertTrue(port > 0, "Server port should be assigned");
    }

    @Test
    void testHealthEndpointReturnsHealthyStatus() {
        // Make HTTP GET request to /health endpoint
        String url = "http://localhost:" + port + "/health";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Health endpoint should return 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");

        Map<String, Object> body = response.getBody();
        assertEquals("healthy", body.get("status"), "Status should be 'healthy'");
        assertNotNull(body.get("timestamp"), "Timestamp should be present");
        assertEquals("workflow-saas", body.get("service"), "Service name should be 'workflow-saas'");
    }

    @Test
    void testHealthEndpointResponseStructure() {
        String url = "http://localhost:" + port + "/health";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        Map<String, Object> body = response.getBody();
        assertNotNull(body, "Response body should not be null");

        // Verify all expected fields are present
        assertTrue(body.containsKey("status"), "Response should contain 'status' field");
        assertTrue(body.containsKey("timestamp"), "Response should contain 'timestamp' field");
        assertTrue(body.containsKey("service"), "Response should contain 'service' field");

        // Verify field types
        assertTrue(body.get("status") instanceof String, "'status' should be a String");
        assertTrue(body.get("timestamp") instanceof String, "'timestamp' should be a String");
        assertTrue(body.get("service") instanceof String, "'service' should be a String");
    }

    @Test
    void testActuatorHealthEndpoint() {
        // Test Spring Boot Actuator health endpoint
        String url = "http://localhost:" + port + "/actuator/health";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Actuator health endpoint should return 200 OK");
        assertNotNull(response.getBody(), "Response body should not be null");

        Map<String, Object> body = response.getBody();
        assertEquals("UP", body.get("status"), "Actuator status should be 'UP'");
    }

    @Test
    void testServerRunsOnConfiguredPort() {
        // Verify server is accessible on the configured port
        String url = "http://localhost:" + port + "/health";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), 
                "Server should be accessible on port " + port);
    }
}
