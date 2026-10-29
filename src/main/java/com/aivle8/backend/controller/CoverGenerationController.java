package com.aivle8.backend.controller;
import com.aivle8.backend.global.common.BaseResponse;
import com.aivle8.backend.service.ImageGenerationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/covers")
public class CoverGenerationController {
    private final ImageGenerationService images;
    public record Request(
            @NotBlank @Size(max = 4000) String prompt,
            @NotBlank @Pattern(regexp = "1024x1024|1024x1536|1536x1024") String size) {}
    @PostMapping("/generate")
    public BaseResponse<Map<String, String>> generate(@RequestBody @Valid Request request) {
        return BaseResponse.success(Map.of("imageUrl", images.generate(request.prompt(), request.size())));
    }
}
