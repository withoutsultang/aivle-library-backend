// src/main/java/com/aivle8/backend/config/DynamoDbConfig.java
package com.aivle8.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfig {
    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.US_WEST_1)
                .httpClientBuilder(software.amazon.awssdk.http.apache.ApacheHttpClient.builder()) // v2 전용 클라이언트 명시
                .build();
    }
}