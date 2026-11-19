package com.finefinee.suhang.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import static com.finefinee.suhang.user.UserDtos.ErrorResponse;

/** 컨트롤러에서 발생한 예외를 일관된 JSON 오류 응답으로 변환합니다. */
@RestControllerAdvice
public class ApiExceptionHandler {
    /** DTO 입력값 검증 오류를 400 응답으로 변환합니다. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " 입력값을 확인해 주세요.")
                .orElse("요청값을 확인해 주세요.");
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    /** 서비스에서 지정한 HTTP 상태 코드와 메시지를 유지해 반환합니다. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return ResponseEntity.status(status)
                .body(new ErrorResponse(exception.getReason() == null ? "요청을 처리할 수 없습니다." : exception.getReason()));
    }
}
