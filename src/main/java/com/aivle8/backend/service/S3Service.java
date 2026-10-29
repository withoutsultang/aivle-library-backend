package com.aivle8.backend.service;

import com.aivle8.backend.global.exception.CustomException;
import com.aivle8.backend.global.exception.ErrorCode;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URI;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static:us-west-1}") private String region;
    @Value("${app.images.allowed-source-hosts:oaidalleapiprodscus.blob.core.windows.net}")
    private String allowedSourceHosts;

    public boolean isSameImage(String key, String input) {
        if (key == null || input == null) return key == null && input == null;
        if (key.equals(input)) return true;
        try {
            URI uri = URI.create(input);
            return key.startsWith("ai-images/") && "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getUserInfo() == null && (uri.getPort() == -1 || uri.getPort() == 443)
                    && ((bucket + ".s3." + region + ".amazonaws.com").equals(uri.getHost())
                        || (bucket + ".s3.amazonaws.com").equals(uri.getHost()))
                    && ("/" + key).equals(uri.getPath());
        } catch (IllegalArgumentException e) { return false; }
    }

    public String uploadImageFromUrl(String tempUrl) {
        if (tempUrl != null && tempUrl.startsWith("data:image/png;base64,")) {
            return uploadGeneratedPng(tempUrl.substring("data:image/png;base64,".length()));
        }
        URI uri;
        try { uri = URI.create(tempUrl); }
        catch (Exception e) { throw new CustomException(ErrorCode.BAD_REQUEST, "올바른 이미지 URL을 입력해 주세요."); }
        boolean allowed = "https".equalsIgnoreCase(uri.getScheme()) && uri.getUserInfo() == null
                && (uri.getPort() == -1 || uri.getPort() == 443)
                && Arrays.stream(allowedSourceHosts.split(",")).map(String::trim)
                .anyMatch(host -> host.equalsIgnoreCase(uri.getHost()));
        if (!allowed) throw new CustomException(ErrorCode.BAD_REQUEST, "허용된 이미지 출처를 사용해 주세요.");
        try {
            // 1. 임시 URL에서 이미지 다운로드
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            try {
                if (connection.getResponseCode() != 200 || connection.getContentType() == null
                        || !connection.getContentType().startsWith("image/")) throw new IllegalArgumentException("Image response required");
                try (InputStream inputStream = connection.getInputStream()) {
                byte[] bytes = inputStream.readNBytes(10 * 1024 * 1024 + 1);
                if (bytes.length == 0 || bytes.length > 10 * 1024 * 1024) throw new IllegalArgumentException("Image size");

                // 2. 고유 파일명 생성
                String fileName = "ai-images/" + UUID.randomUUID() + ".png";

                // 3. 메타데이터 설정
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(bytes.length);
                metadata.setContentType(connection.getContentType());

                // 4. S3 업로드
                amazonS3.putObject(new PutObjectRequest(bucket, fileName,
                        new ByteArrayInputStream(bytes), metadata));

                // 5. 나중에 조회할 때 사용할 S3 Key(경로) 반환
                return fileName;
                }
            } finally { connection.disconnect(); }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 업로드 중 오류가 발생했습니다.");
        }
    }

    private String uploadGeneratedPng(String encoded) {
        if (encoded.length() > 14 * 1024 * 1024) throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 10 MiB 이하여야 합니다.");
        byte[] bytes;
        try { bytes = java.util.Base64.getDecoder().decode(encoded); }
        catch (IllegalArgumentException e) { throw new CustomException(ErrorCode.BAD_REQUEST, "이미지 인코딩을 확인해 주세요."); }
        byte[] signature = { (byte)137, 80, 78, 71, 13, 10, 26, 10 };
        if (bytes.length < signature.length || bytes.length > 10 * 1024 * 1024
                || !Arrays.equals(signature, Arrays.copyOf(bytes, signature.length))) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "PNG 이미지를 사용해 주세요.");
        }
        String key = "ai-images/" + UUID.randomUUID() + ".png";
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/png"); metadata.setContentLength(bytes.length);
        try { amazonS3.putObject(new PutObjectRequest(bucket, key, new ByteArrayInputStream(bytes), metadata)); }
        catch (Exception e) { throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 저장을 완료하지 못했습니다."); }
        return key;
    }

    // 조회 시 사용할 Presigned URL 생성 메서드 (필요 시 사용)
    public String getPresignedUrl(String key) {
        if (key == null || !key.startsWith("ai-images/")) return key; // S3 경로가 아니면 그대로 반환

        Date expiration = new Date();
        long expTimeMillis = expiration.getTime() + (1000 * 60 * 60); // 1시간 유효
        expiration.setTime(expTimeMillis);

        return amazonS3.generatePresignedUrl(bucket, key, expiration).toString();
    }
    public void deleteImage(String s3Key) {
        if (s3Key == null || !s3Key.startsWith("ai-images/") || "ai-images/default-cover.png".equals(s3Key)) {
            return; // S3에 저장된 파일이 아니면 무시
        }
        try {
            amazonS3.deleteObject(bucket, s3Key);
        } catch (Exception e) {
            // 삭제 실패가 전체 로직에 영향을 주지 않도록 로그만 남기거나 예외 처리
            System.err.println("S3 파일 삭제 실패: " + s3Key);
        }
    }
}
