package com.aivle8.backend.service;
import com.aivle8.backend.global.exception.CustomException;
import com.aivle8.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageGenerationService {
    private final RestClient imageApiClient;
    @Value("${app.images.api-key:}") private String apiKey;
    @Value("${app.images.model:gpt-image-2.5-sunburst}") private String model;
    public record ImageData(String b64_json) {}
    public record ImageResponse(List<ImageData> data) {}

    public String generate(String prompt, String size) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 표지 서비스 설정을 확인해 주세요.");
        }
        try {
            ImageResponse result = imageApiClient.post().uri("/images/generations")
                    .header("Authorization", "Bearer " + apiKey).contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("model", model, "prompt", prompt, "size", size, "n", 1,
                            "quality", "medium", "output_format", "png"))
                    .retrieve().body(ImageResponse.class);
            if (result == null || result.data() == null || result.data().isEmpty()) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "표지 이미지 응답을 확인해 주세요.");
            }
            String image = result.data().get(0).b64_json();
            if (image == null || image.isBlank() || image.length() > 14 * 1024 * 1024) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "표지 이미지 응답을 확인해 주세요.");
            }
            return "data:image/png;base64," + image;
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "표지 생성 요청을 완료하지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }
}
