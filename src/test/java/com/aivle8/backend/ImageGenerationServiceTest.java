package com.aivle8.backend;

import com.aivle8.backend.global.exception.CustomException;
import com.aivle8.backend.service.ImageGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ImageGenerationServiceTest {
    MockRestServiceServer server;
    ImageGenerationService service;
    @BeforeEach void setup() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        service = new ImageGenerationService(builder.build());
        ReflectionTestUtils.setField(service, "apiKey", "test-fixture");
        ReflectionTestUtils.setField(service, "model", "gpt-image-2.5-sunburst");
    }
    @Test void requestsPngAndReturnsBrowserCompatibleDataUrl() {
        server.expect(requestTo("https://api.openai.com/v1/images/generations"))
                .andExpect(header("Authorization", "Bearer test-fixture"))
                .andExpect(jsonPath("$.size").value("1024x1536"))
                .andExpect(jsonPath("$.output_format").value("png"))
                .andExpect(jsonPath("$.n").value(1))
                .andRespond(withSuccess("{\"data\":[{\"b64_json\":\"fixture\"}]}", MediaType.APPLICATION_JSON));
        assertEquals("data:image/png;base64,fixture", service.generate("book cover", "1024x1536"));
        server.verify();
    }
    @Test void providerFailureReturnsSafeError() {
        server.expect(requestTo("https://api.openai.com/v1/images/generations")).andRespond(withServerError());
        assertThrows(CustomException.class, () -> service.generate("book", "1024x1024"));
        server.verify();
    }
    @Test void missingConfigurationNeverCallsProvider() {
        ReflectionTestUtils.setField(service, "apiKey", "");
        assertThrows(CustomException.class, () -> service.generate("book", "1024x1024"));
        server.verify();
    }
    @Test void emptyResponseCannotBecomeCover() {
        server.expect(requestTo("https://api.openai.com/v1/images/generations"))
                .andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));
        assertThrows(CustomException.class, () -> service.generate("book", "1024x1024"));
        server.verify();
    }
}
