package com.aivle8.backend;
import com.aivle8.backend.repository.BookRepository;
import com.aivle8.backend.scheduler.ViewCountSyncScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.paginators.ScanIterable;
import java.util.Map;
import static org.mockito.Mockito.*;

class ViewCountSyncTest {
    @Test void synchronizesRecordsFromEveryDynamoDbPage() {
        DynamoDbClient dynamo = mock(DynamoDbClient.class);
        BookRepository books = mock(BookRepository.class);
        var request = ScanRequest.builder().tableName("test-views").build();
        var first = Map.of("book_id", AttributeValue.builder().n("1").build(),
                "view_count", AttributeValue.builder().n("12").build());
        var second = Map.of("book_id", AttributeValue.builder().n("2").build(),
                "view_count", AttributeValue.builder().n("8").build());
        when(dynamo.scan(any(ScanRequest.class))).thenReturn(
                ScanResponse.builder().items(first).lastEvaluatedKey(Map.of("book_id", first.get("book_id"))).build(),
                ScanResponse.builder().items(second).build());
        when(dynamo.scanPaginator(any(ScanRequest.class))).thenReturn(new ScanIterable(dynamo, request));
        var scheduler = new ViewCountSyncScheduler(dynamo, books);
        ReflectionTestUtils.setField(scheduler, "table", "test-views");
        scheduler.syncViewCount();
        verify(books).updateViewCount(1L, 12L); verify(books).updateViewCount(2L, 8L);
        verify(dynamo, times(2)).scan(any(ScanRequest.class));
    }
}
