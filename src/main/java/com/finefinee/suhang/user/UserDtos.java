package com.finefinee.suhang.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** API 요청과 응답에 사용하는 DTO 모음입니다. */
public final class UserDtos {
    private UserDtos() {
    }

    /** 회원가입 요청입니다. */
    public record CreateRequest(
            @NotBlank @Size(max = 30) String studentNumber,
            @NotBlank @Size(max = 50) String name,
            @NotBlank @Size(max = 50) String gameNickname,
            @NotBlank @Size(min = 8, max = 100) String password
    ) {
    }

    /** 학번과 비밀번호를 받는 로그인 요청입니다. */
    public record LoginRequest(
            @NotBlank @Size(max = 30) String studentNumber,
            @NotBlank @Size(max = 100) String password
    ) {
    }

    /** 프로필 수정 요청이며, 비밀번호는 선택 항목입니다. */
    public record UpdateRequest(
            @NotBlank @Size(max = 50) String name,
            @NotBlank @Size(max = 50) String gameNickname,
            @Size(max = 100) String password
    ) {
    }

    /** 클라이언트에 공개할 사용자 정보로, 비밀번호는 포함하지 않습니다. */
    public record Response(Long id, String studentNumber, String name, String gameNickname) {
        /** 엔티티에서 공개 응답 DTO를 생성합니다. */
        static Response from(User user) {
            return new Response(user.getId(), user.getStudentNumber(), user.getName(), user.getGameNickname());
        }
    }

    /** API 오류를 일관된 JSON 형식으로 전달합니다. */
    public record ErrorResponse(String message) {
    }
}
