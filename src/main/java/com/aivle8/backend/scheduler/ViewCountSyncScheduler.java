package com.aivle8.backend.scheduler;

import com.aivle8.backend.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewCountSyncScheduler {

    private final DynamoDbClient dynamoDbClient;
    private final BookRepository bookRepository;

    // 테스트를 위해 우선 1분마다 실행되도록 설정했습니다 (실제 서비스는 0 0/5 * * * *)
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void syncViewCount() {
        log.info("조회수 동기화 시작 (DynamoDB -> RDS)");

        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName("PortfolioBookViews")
                    .build();

            ScanResponse response = dynamoDbClient.scan(scanRequest);

            response.items().forEach(item -> {
                // n()은 숫자 타입을 의미합니다.
                Long bookId = Long.parseLong(item.get("book_id").n());
                Long viewCount = Long.parseLong(item.get("view_count").n());

                // BookRepository에 아까 만든 updateViewCount 호출
                bookRepository.updateViewCount(bookId, viewCount);
            });

            log.info("조회수 동기화 완료! 총 {}개 항목 처리", response.count());
        } catch (Exception e) {
            log.error("동기화 중 에러 발생: {}", e.getMessage());
        }
    }
}