package com.aivle8.backend.global.exception;

import com.aivle8.backend.global.common.BaseResponse;
import lombok.Getter;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Getter
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * CustomException 처리
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
     * 처리하지 못한 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleException(Exception ex) {

        ex.printStackTrace(); // 개발 중에는 로그 확인

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