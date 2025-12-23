package com.aivle8.backend.service;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.domain.BookDetail;
import com.aivle8.backend.domain.User;
import com.aivle8.backend.domain.enums.BookSortType;
import com.aivle8.backend.dto.*;
import com.aivle8.backend.global.exception.CustomException;
import com.aivle8.backend.global.exception.ErrorCode;
import com.aivle8.backend.repository.BookDetailRepository;
import com.aivle8.backend.repository.BookRepository;
import com.aivle8.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookDetailRepository bookDetailRepository;
    private final S3Service s3Service; // S3 서비스 주입

    private final DynamoDbClient dynamoDbClient;

    @Override
    public BookResponseDto getBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 도서를 찾을 수 없습니다."));

        // 2. 기존 RDS 조회수 증가 로직은 주석 처리 (차별화 증명용)
        // book.increaseViewCount();

        // 3. DynamoDB 조회수 증가 메서드 호출
        incrementViewCount(bookId);

        BookResponseDto dto = BookResponseDto.fromEntity(book);
        // DB에 저장된 S3 Key를 접근 가능한 URL로 변환
        dto.setCoverImageUrl(s3Service.getPresignedUrl(book.getCoverImageUrl()));

        return dto;
    }

    @Override
    public void incrementViewCount(Long bookId) {
        try {
            UpdateItemRequest request = UpdateItemRequest.builder()
                    .tableName("PortfolioBookViews") // DynamoDB 테이블명
                    .key(Map.of("book_id", AttributeValue.builder().n(String.valueOf(bookId)).build()))
                    .updateExpression("SET view_count = if_not_exists(view_count, :zero) + :incr")
                    .expressionAttributeValues(Map.of(
                            ":zero", AttributeValue.builder().n("0").build(),
                            ":incr", AttributeValue.builder().n("1").build()
                    ))
                    .build();

            dynamoDbClient.updateItem(request);
        } catch (Exception e) {
            // 비즈니스 로직에 지장을 주지 않도록 에러 로그만 남김
            System.err.println("DynamoDB 업데이트 실패: " + e.getMessage());
        }
    }

    @Override
    public BookDetailResponseDto getBookDetail(Long bookId) {
        BookDetail detail = bookDetailRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 도서의 세부 정보가 없습니다."));

        return BookDetailResponseDto.fromEntity(detail);
    }

    @Override
    public BookUpdateResponseDto updateBook(Long bookId, BookUpdateRequestDto dto, Long userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));

        if (!book.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 도서만 수정 가능합니다.");
        }

        // [이미지 수정 로직]
        // 새로운 URL이 들어왔고, 기존 URL과 다르다면 처리
        if (dto.getCoverImageUrl() != null && !dto.getCoverImageUrl().equals(book.getCoverImageUrl())) {
            // 1. 기존 S3 파일 삭제
            s3Service.deleteImage(book.getCoverImageUrl());
            // 2. 새 이미지 S3 업로드
            String newS3Key = s3Service.uploadImageFromUrl(dto.getCoverImageUrl());
            book.setCoverImageUrl(newS3Key);
        }

        book.setTitle(dto.getTitle());
        book.setCategory(dto.getCategory());
        book.setCoverImageUrl(dto.getCoverImageUrl());

        BookDetail detail = book.getBookDetail();

        if (detail == null) {
            detail = new BookDetail();
            detail.setBook(book);
            detail.setBookId(bookId);
            book.setBookDetail(detail);
        }

        detail.setDescription(dto.getDescription());
        detail.setContent(dto.getContent());

        return BookUpdateResponseDto.fromEntity(book);
    }

    @Override
    public void deleteBook(Long bookId, Long userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "도서를 찾을 수 없습니다."));

        if (!book.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 도서만 삭제 가능합니다.");
        }

        // [삭제 로직] RDS 레코드 삭제 전 S3 파일 먼저 삭제
        if (book.getCoverImageUrl() != null) {
            s3Service.deleteImage(book.getCoverImageUrl());
        }

        bookRepository.delete(book);
    }

    @Override
    public Page<BookResponseDto> getBooks(BookSortType sort, int page, int size) {

        Sort sortOption = switch (sort) {
            case POPULAR -> Sort.by(Sort.Direction.DESC, "viewCount");
            case TITLE_ASC -> Sort.by(Sort.Direction.ASC, "title");
            case TITLE_DESC -> Sort.by(Sort.Direction.DESC, "title");
            case LATEST -> Sort.by(Sort.Direction.DESC, "bookCreated");
        };

        Pageable pageable = PageRequest.of(page, size, sortOption);

        return bookRepository.findAll(pageable)
                .map(BookResponseDto::fromEntity);
    }

    @Override
    public List<BookResponseDto> getPopularBooks(int limit){
        Pageable pageable = PageRequest.of(0, limit);
        return bookRepository.findTopByViewCount(pageable).stream()
                .map(BookResponseDto::fromEntity)
                .toList();
    }

    @Override
    public BookCreateResponseDto createBook(BookCreateRequestDto request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 1. 프론트에서 보낸 한 장의 임시 URL을 S3에 업로드
        String s3Key = s3Service.uploadImageFromUrl(request.getCoverImageUrl());

        Book book = Book.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .coverImageUrl(s3Key) // S3 경로 저장
                .user(user)
                .build();

        // BookDetail 설정 생략 (기존과 동일)
        BookDetail detail = BookDetail.builder().book(book).content(request.getContent()).description(request.getDescription()).build();
        book.setBookDetail(detail);

        bookRepository.save(book);
        return BookCreateResponseDto.builder().bookId(book.getBookId()).build();
    }

    @Override
    public void updateBookCoverUrl(Long bookId, BookCoverUrlUpdateRequestDto dto, Long userId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "도서 없음"));
        if (!book.getUser().getUserId().equals(userId)) throw new CustomException(ErrorCode.FORBIDDEN, "권한 없음");

        // 기존 이미지 삭제 후 새 이미지 등록
        s3Service.deleteImage(book.getCoverImageUrl());
        String newS3Key = s3Service.uploadImageFromUrl(dto.getCoverImageUrl());
        book.setCoverImageUrl(newS3Key);
    }
}
