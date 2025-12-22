package com.aivle8.backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookCreateRequestDtoTest {

    private final Validator validator;

    public BookCreateRequestDtoTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    @DisplayName("모든 필수 값이 정상일 경우 BookCreateRequestDto는 유효함을 보장한다")
    void validDto_passesValidation() {
        // given
        BookCreateRequestDto dto = new BookCreateRequestDto();
        dto.setTitle("클린 코드");
        dto.setCategory("개발");
        dto.setDescription("깨끗한 코드를 작성하는 방법을 설명하는 책");
        dto.setContent("이 책은 좋은 코드에 대한 철학을 담고 있다.");
        dto.setCoverImageUrl(null);

        // when
        Set<ConstraintViolation<BookCreateRequestDto>> violations =
                validator.validate(dto);

        // then
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("제목이 비어 있으면 BookCreateRequestDto는 유효하지 않음을 보장한다")
    void blankTitle_failsValidation() {
        // given
        BookCreateRequestDto dto = new BookCreateRequestDto();
        dto.setTitle("");
        dto.setCategory("개발");
        dto.setDescription("요약");
        dto.setContent("내용");

        // when
        Set<ConstraintViolation<BookCreateRequestDto>> violations =
                validator.validate(dto);

        // then
        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("설명(description)이 200자를 초과하면 유효성 검증에 실패함을 보장한다")
    void descriptionTooLong_failsValidation() {
        // given
        BookCreateRequestDto dto = new BookCreateRequestDto();
        dto.setTitle("클린 코드");
        dto.setCategory("개발");
        dto.setDescription("a".repeat(201));
        dto.setContent("내용");

        // when
        Set<ConstraintViolation<BookCreateRequestDto>> violations =
                validator.validate(dto);

        // then
        assertEquals(1, violations.size());
    }
}
