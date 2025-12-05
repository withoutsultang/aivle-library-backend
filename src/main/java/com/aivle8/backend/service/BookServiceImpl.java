package com.aivle8.backend.service;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.global.exception.CustomException;
import com.aivle8.backend.global.exception.ErrorCode;
import com.aivle8.backend.repository.BookRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    @Override
    public Book getBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
}