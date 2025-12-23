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
        // 현재 사용 중인 리전(예: us-west-1)으로 설정하십시오.
        return DynamoDbClient.builder()
                .region(Region.US_WEST_1)
                .build();
    }
}