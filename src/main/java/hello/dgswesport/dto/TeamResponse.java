package hello.dgswesport.dto;

import hello.dgswesport.domain.entity.Team;

public record TeamResponse(
        Long id,
        String name
) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName()
        );
    }
}