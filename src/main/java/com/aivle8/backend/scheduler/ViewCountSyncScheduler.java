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


    @org.springframework.beans.factory.annotation.Value("${app.dynamodb.table:PortfolioBookViews}")
    private String table;
    @Scheduled(cron = "${app.dynamodb.sync-cron:0 * * * * *}")
    @Transactional
    public void syncViewCount() {
        ScanRequest request = ScanRequest.builder().tableName(table).build();
        int count = 0;
        for (ScanResponse page : dynamoDbClient.scanPaginator(request)) {
            for (var item : page.items()) {
                bookRepository.updateViewCount(Long.parseLong(item.get("book_id").n()),
                        Long.parseLong(item.get("view_count").n()));
                count++;
            }
        }
        log.info("View counts synchronized: {} records", count);
    }
}
