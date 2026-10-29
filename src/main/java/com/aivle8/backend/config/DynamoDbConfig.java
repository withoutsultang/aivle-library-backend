// src/main/java/com/aivle8/backend/config/DynamoDbConfig.java
package com.aivle8.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfig {
    @Value("${cloud.aws.region.static:us-west-1}") private String region;
    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .httpClientBuilder(software.amazon.awssdk.http.apache.ApacheHttpClient.builder()) // v2 전용 클라이언트 명시
                .build();
    }
}