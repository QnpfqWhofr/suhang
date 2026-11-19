package hello.dgswesport.dto;

import hello.dgswesport.domain.entity.Team;

import java.util.List;

public record TeamResponse(
        Long id,
        String name,
        List<UserResponse> users
) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getUsers().stream()
                        .map(UserResponse::from)
                        .toList()
        );
    }
}
