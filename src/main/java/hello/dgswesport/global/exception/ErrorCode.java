package hello.dgswesport.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 오류 코드.
 * 도메인별 오류(예: TEAM_NOT_FOUND)는 이 enum에 추가해서 사용한다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "이미 존재하는 리소스입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."),
    DUPLICATE_TEAM_NAME(HttpStatus.CONFLICT, "이미 사용 중인 팀 이름입니다."),
    TEAMS_ALREADY_EXIST(HttpStatus.CONFLICT, "이미 팀이 생성되어 있습니다. 팀 전체 삭제 후 다시 생성하세요."),
    NOT_ENOUGH_USERS(HttpStatus.BAD_REQUEST, "팀을 2개 이상 만들려면 팀에 배정되지 않은 참가자가 10명 이상 필요합니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    DUPLICATE_STUDENT_NUM(HttpStatus.CONFLICT, "이미 등록된 학번입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "학번 또는 비밀번호가 올바르지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
