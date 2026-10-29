package com.aivle8.backend;

import com.aivle8.backend.controller.BookController;
import com.aivle8.backend.domain.Book;
import com.aivle8.backend.domain.User;
import com.aivle8.backend.dto.*;
import com.aivle8.backend.global.exception.CustomException;
import com.aivle8.backend.repository.*;
import com.aivle8.backend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookWorkflowTest {
    BookRepository books;
    S3Service storage;
    BookServiceImpl service;
    Book book;
    BookUpdateRequestDto update;
    @BeforeEach void setup() {
        books = mock(BookRepository.class);
        storage = mock(S3Service.class);
        service = new BookServiceImpl(books, mock(UserRepository.class),
                mock(BookDetailRepository.class), storage, mock(DynamoDbClient.class));
        book = Book.builder().bookId(1L).title("원고").user(User.builder().userId(7L).build())
                .coverImageUrl("ai-images/original.png").build();
        when(books.findById(1L)).thenReturn(Optional.of(book));
        update = new BookUpdateRequestDto();
        update.setTitle("완성한 원고"); update.setContent("본문"); update.setDescription("소개");
    }
    @Test void anotherAuthorCannotModifyBookOrStorage() {
        assertThrows(CustomException.class, () -> service.updateBook(1L, update, 8L));
        verifyNoInteractions(storage);
        assertEquals("원고", book.getTitle());
    }
    @Test void unchangedSignedCoverKeepsItsStoredKey() {
        String signed = "https://bucket.s3.us-west-1.amazonaws.com/ai-images/original.png?signature=example";
        update.setCoverImageUrl(signed);
        when(storage.isSameImage(book.getCoverImageUrl(), signed)).thenReturn(true);
        service.updateBook(1L, update, 7L);
        assertEquals("ai-images/original.png", book.getCoverImageUrl());
        verify(storage, never()).uploadImageFromUrl(any());
        verify(storage, never()).deleteImage(any());
    }
    @Test void failedNewUploadRetainsTheExistingCover() {
        update.setCoverImageUrl("https://image.example/new.png");
        when(storage.uploadImageFromUrl(update.getCoverImageUrl())).thenThrow(new IllegalStateException("upload"));
        assertThrows(IllegalStateException.class, () -> service.updateBook(1L, update, 7L));
        assertEquals("ai-images/original.png", book.getCoverImageUrl());
        verify(storage, never()).deleteImage(any());
    }
    @Test void replacementIsStoredBeforeTheOldImageIsRemoved() {
        update.setCoverImageUrl("https://image.example/new.png");
        when(storage.uploadImageFromUrl(update.getCoverImageUrl())).thenReturn("ai-images/new.png");
        service.updateBook(1L, update, 7L);
        var order = inOrder(storage);
        order.verify(storage).uploadImageFromUrl(update.getCoverImageUrl());
        order.verify(storage).deleteImage("ai-images/original.png");
        assertEquals("ai-images/new.png", book.getCoverImageUrl());
    }
    @Test void creatingBookReturnsHttpCreatedAndTheBookIdentifier() {
        BookService api = mock(BookService.class);
        BookCreateRequestDto request = new BookCreateRequestDto();
        when(api.createBook(request, 7L)).thenReturn(BookCreateResponseDto.builder().bookId(42L).build());
        var response = new BookController(api).createBook(request, "7");
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(42L, response.getBody().getData().getBookId());
    }
}
