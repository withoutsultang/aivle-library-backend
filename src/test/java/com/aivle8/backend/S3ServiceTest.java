package com.aivle8.backend;
import com.aivle8.backend.global.exception.CustomException;
import com.aivle8.backend.service.S3Service;
import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.net.URL;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class S3ServiceTest {
    AmazonS3 s3; S3Service service;
    @BeforeEach void setup() {
        s3 = mock(AmazonS3.class); service = new S3Service(s3);
        ReflectionTestUtils.setField(service, "bucket", "library-test");
        ReflectionTestUtils.setField(service, "region", "us-west-1");
        ReflectionTestUtils.setField(service, "allowedSourceHosts", "images.example.com");
    }
    @Test void resolvesStoredCoverToASignedUrl() throws Exception {
        when(s3.generatePresignedUrl(eq("library-test"), eq("ai-images/book.png"), any(Date.class)))
                .thenReturn(new URL("https://library-test.s3.us-west-1.amazonaws.com/ai-images/book.png?expires=example"));
        assertTrue(service.getPresignedUrl("ai-images/book.png").contains("expires=example"));
    }
    @Test void defaultCoverCannotBeDeleted() {
        service.deleteImage("ai-images/default-cover.png"); verifyNoInteractions(s3);
    }
    @Test void generatedPngIsStoredWithMatchingMetadata() {
        String png = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jSugAAAAASUVORK5CYII=";
        String key = service.uploadImageFromUrl("data:image/png;base64," + png);
        var request = org.mockito.ArgumentCaptor.forClass(com.amazonaws.services.s3.model.PutObjectRequest.class);
        verify(s3).putObject(request.capture());
        assertTrue(key.startsWith("ai-images/"));
        assertEquals(key, request.getValue().getKey());
        assertEquals("image/png", request.getValue().getMetadata().getContentType());
        assertEquals(java.util.Base64.getDecoder().decode(png).length, request.getValue().getMetadata().getContentLength());
    }
    @Test void malformedGeneratedImageNeverReachesStorage() {
        assertThrows(CustomException.class, () -> service.uploadImageFromUrl("data:image/png;base64,%%%"));
        assertThrows(CustomException.class, () -> service.uploadImageFromUrl("data:image/png;base64,aGVsbG8="));
        verifyNoInteractions(s3);
    }
    @Test void recognizesOwnSignedCoverButNotAnotherBucket() {
        assertTrue(service.isSameImage("ai-images/book.png", "https://library-test.s3.us-west-1.amazonaws.com/ai-images/book.png?expires=example"));
        assertFalse(service.isSameImage("ai-images/book.png", "https://other.s3.us-west-1.amazonaws.com/ai-images/book.png"));
    }
    @Test void rejectsLocalNetworkAndUnapprovedImageSourcesBeforeDownloading() {
        assertThrows(CustomException.class, () -> service.uploadImageFromUrl("http://127.0.0.1/private"));
        assertThrows(CustomException.class, () -> service.uploadImageFromUrl("file:///tmp/image.png"));
        assertThrows(CustomException.class, () -> service.uploadImageFromUrl("https://unapproved.example.com/image.png"));
        verifyNoInteractions(s3);
    }
}
