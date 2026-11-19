package hello.dgswesport.dto;

import hello.dgswesport.domain.entity.User;

public record UserResponse(
        Long id,
        String studentNum,
        String name,
        String tier
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getStudent_num(),
                user.getName(),
                user.getTier()
        );
    }
}
