package com.aivle8.backend.global.exception;

import com.aivle8.backend.global.common.BaseResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * [CustomException 처리]
     * 비즈니스 로직에서 throw new CustomException(...)으로 발생시킨 예외를 처리합니다.
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseResponse<?>> handleCustomException(CustomException ex) {
        ErrorCode code = ex.getErrorCode();

        BaseResponse<?> response =
                BaseResponse.error(code.getStatus(), ex.getMessage());

        return ResponseEntity
                .status(code.getStatus())
                .body(response);
    }

    /*
     * [그 외 모든 예외 처리]
     * 예상치 못한 서버 에러(NullPointerException 등)를 처리합니다.
     * 보안을 위해 상세 에러 내용은 로그에만 남기고, 클라이언트에는 "서버 내부 오류"로 숨깁니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleException(Exception ex) {

        log.error("Unhandled Exception: ", ex);

        BaseResponse<?> response =
                BaseResponse.error(
                        ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
                );

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(response);
    }
}