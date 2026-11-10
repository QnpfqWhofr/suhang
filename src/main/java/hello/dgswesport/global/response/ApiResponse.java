package hello.dgswesport.global.response;

import org.springframework.http.HttpStatus;

// 성공 응답의 공통 형식
public record ApiResponse<T>(int status, String message, T data) {

    private static final String DEFAULT_SUCCESS_MESSAGE = "요청이 성공했습니다.";
    private static final String DEFAULT_CREATED_MESSAGE = "리소스가 생성되었습니다.";

    public static <T> ApiResponse<T> ok(T data) {
        return of(HttpStatus.OK, DEFAULT_SUCCESS_MESSAGE, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return of(HttpStatus.CREATED, DEFAULT_CREATED_MESSAGE, data);
    }

    public static <T> ApiResponse<T> of(HttpStatus status, String message, T data) {
        return new ApiResponse<>(status.value(), message, data);
    }
}
