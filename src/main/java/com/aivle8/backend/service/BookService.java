package com.aivle8.backend.service;

import com.aivle8.backend.domain.Book;
import com.aivle8.backend.repository.BookRepository;

import java.util.List;

public interface BookService {
    Book getBook(Long id);

    List<Book> getAllBooks();
}
