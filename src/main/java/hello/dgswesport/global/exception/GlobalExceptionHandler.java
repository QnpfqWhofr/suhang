package hello.dgswesport.global.exception;

import hello.dgswesport.global.response.ErrorResponse;
import hello.dgswesport.global.response.ErrorResponse.ValidationError;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Service 계층에서 던진 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, ex.getMessage()));
    }

    // 어디에서도 처리하지 못한 예외. 내부 정보가 노출되지 않도록 메시지를 고정한다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("처리되지 않은 예외가 발생했습니다.", ex);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    // Valid 검증 실패: 필드별 오류 목록을 함께 내려준다.
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
                .toList();
        return handleExceptionInternal(ex, ErrorResponse.of(ErrorCode.INVALID_INPUT, errors), headers, status, request);
    }

    // Spring MVC 표준 예외의 응답 본문을 ErrorResponse로 교체한다.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        Object errorBody = (body instanceof ErrorResponse) ? body : toErrorResponse(body, statusCode);
        return super.handleExceptionInternal(ex, errorBody, headers, statusCode, request);
    }

    private ErrorResponse toErrorResponse(Object body, HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = (body instanceof ProblemDetail problemDetail && problemDetail.getDetail() != null)
                ? problemDetail.getDetail()
                : status.getReasonPhrase();
        return ErrorResponse.of(status, message);
    }
}
