package com.aivle8.backend.global.common;

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

    /**
     * [성공 응답 - 200 OK]
     * 데이터를 포함하여 성공 응답을 반환합니다.
     * @param data 클라이언트에게 전달할 데이터
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(
                HttpStatus.OK.value(),
                ResponseMessage.SUCCESS.name(),
                data
        );
    }

    /**
     * [성공 응답 - 201 Created]
     * 리소스 생성 성공 시 사용합니다.
     * @param data 생성된 리소스 데이터 등
     */
    public static <T> BaseResponse<T> created(T data) {
        return new BaseResponse<>(
                HttpStatus.CREATED.value(),
                ResponseMessage.CREATED.name(),
                data
        );
    }

    /**
     * [에러 응답]
     * 예외 발생 시 GlobalExceptionHandler에서 호출합니다.
     * data는 null로 설정됩니다.
     * @param status HTTP 상태 코드 (Enum)
     * @param message 에러 메시지
     */
    public static <T> BaseResponse<T> error(HttpStatus status, String message) {
        return new BaseResponse<>(
                status.value(),
                message,
                null
        );
    }
}
