package com.aivle8.backend.security;
import com.aivle8.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtFilterTest {
    @Test void publicHealthIsIndependentOfAnExpiredBearerToken() throws Exception {
        JwtService jwt = mock(JwtService.class); FilterChain chain = mock(FilterChain.class);
        var request = new MockHttpServletRequest("GET", "/api/v1/health");
        request.setServletPath("/api/v1/health"); request.addHeader("Authorization", "Bearer expired");
        var response = new MockHttpServletResponse();
        new JwtFilter(jwt).doFilter(request, response, chain);
        verify(chain).doFilter(request, response); verifyNoInteractions(jwt);
    }
    @Test void invalidBearerCannotAccessProtectedResources() throws Exception {
        JwtService jwt = mock(JwtService.class); FilterChain chain = mock(FilterChain.class);
        when(jwt.parseToken("invalid")).thenThrow(new IllegalArgumentException("invalid"));
        var request = new MockHttpServletRequest("GET", "/api/v1/books");
        request.setServletPath("/api/v1/books"); request.addHeader("Authorization", "Bearer invalid");
        var response = new MockHttpServletResponse();
        new JwtFilter(jwt).doFilter(request, response, chain);
        assertEquals(401, response.getStatus()); verifyNoInteractions(chain);
    }
}
