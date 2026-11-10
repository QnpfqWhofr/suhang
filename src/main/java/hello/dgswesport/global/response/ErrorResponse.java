package hello.dgswesport.global.response;

import hello.dgswesport.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.List;

// 실패 응답의 공통 형식
public record ErrorResponse(int status, String code, String message, List<ValidationError> errors) {

    public record ValidationError(String field, String message) {
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return of(errorCode, errorCode.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getStatus().value(), errorCode.name(), message, List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, List<ValidationError> errors) {
        return new ErrorResponse(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage(), errors);
    }

    public static ErrorResponse of(HttpStatus status, String message) {
        return new ErrorResponse(status.value(), status.name(), message, List.of());
    }
}
