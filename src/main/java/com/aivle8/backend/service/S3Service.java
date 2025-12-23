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
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadImageFromUrl(String tempUrl) {
        try {
            // 1. 임시 URL에서 이미지 다운로드
            URL url = new URL(tempUrl);
            try (InputStream inputStream = url.openStream()) {
                byte[] bytes = inputStream.readAllBytes();

                // 2. 고유 파일명 생성
                String fileName = "ai-images/" + UUID.randomUUID() + ".png";

                // 3. 메타데이터 설정
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(bytes.length);
                metadata.setContentType("image/png");

                // 4. S3 업로드
                amazonS3.putObject(new PutObjectRequest(bucket, fileName,
                        new ByteArrayInputStream(bytes), metadata));

                // 5. 나중에 조회할 때 사용할 S3 Key(경로) 반환
                return fileName;
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 업로드 중 오류가 발생했습니다.");
        }
    }

    // 조회 시 사용할 Presigned URL 생성 메서드 (필요 시 사용)
    public String getPresignedUrl(String key) {
        if (key == null || !key.contains("covers/")) return key; // S3 경로가 아니면 그대로 반환

        Date expiration = new Date();
        long expTimeMillis = expiration.getTime() + (1000 * 60 * 60); // 1시간 유효
        expiration.setTime(expTimeMillis);

        return amazonS3.generatePresignedUrl(bucket, key, expiration).toString();
    }
    public void deleteImage(String s3Key) {
        if (s3Key == null || !s3Key.contains("ai-images/")) {
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