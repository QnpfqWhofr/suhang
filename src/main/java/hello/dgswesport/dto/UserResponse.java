package hello.dgswesport.dto;

import hello.dgswesport.domain.entity.Tier;
import hello.dgswesport.domain.entity.User;

// 비밀번호는 응답에 포함하지 않는다.
public record UserResponse(
        Long id,
        String studentNum,
        String name,
        String gameNickname,
        Tier tier,
        Long teamId
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getStudentNum(),
                user.getName(),
                user.getGameNickname(),
                user.getTier(),
                user.getTeam() == null ? null : user.getTeam().getId()
        );
    }
}
