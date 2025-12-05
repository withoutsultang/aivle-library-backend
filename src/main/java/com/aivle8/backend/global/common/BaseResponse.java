package com.aivle8.backend.global.common;

import com.aivle8.backend.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
@JsonPropertyOrder({"status", "message", "data"})
public class BaseResponse<T> {

    private int status;
    private String message;
    private T data;

    private BaseResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // 200 OK
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(
                HttpStatus.OK.value(),
                ResponseMessage.SUCCESS.name(),
                data
        );
    }

    // 201 CREATED
    public static <T> BaseResponse<T> created(T data) {
        return new BaseResponse<>(
                HttpStatus.CREATED.value(),
                ResponseMessage.CREATED.name(),
                data
        );
    }

    // Error - ErrorCode 기반
    public static <T> BaseResponse<T> error(HttpStatus status, String message) {
        return new BaseResponse<>(
                status.value(),
                message,
                null
        );
    }
}
