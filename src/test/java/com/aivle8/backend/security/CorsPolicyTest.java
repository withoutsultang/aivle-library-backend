package com.aivle8.backend.security;
import com.aivle8.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CorsPolicyTest {
    @Test void acceptsJsonAndBearerHeadersFromConfiguredFrontend() {
        var config = new SecurityConfig(mock(JwtService.class));
        ReflectionTestUtils.setField(config, "allowedOrigins", "http://localhost:5173,https://library.example.com");
        var request = new MockHttpServletRequest("OPTIONS", "/api/v1/books");
        var policy = config.corsConfigurationSource().getCorsConfiguration(request);
        assertEquals("https://library.example.com", policy.checkOrigin("https://library.example.com"));
        assertNull(policy.checkOrigin("https://other.example.com"));
        assertEquals(2, policy.checkHeaders(java.util.List.of("Authorization", "Content-Type")).size());
    }
}
