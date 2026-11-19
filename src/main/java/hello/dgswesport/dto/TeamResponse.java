package hello.dgswesport.dto;

import hello.dgswesport.domain.entity.Team;

import java.util.List;

public record TeamResponse(
        Long id,
        String name,
        double averageScore,
        List<UserResponse> users
) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                averageScore(team),
                team.getUsers().stream()
                        .map(UserResponse::from)
                        .toList()
        );
    }

    // 소속 팀원의 티어 점수 평균. 소수 둘째 자리까지 반올림하고 팀원이 없으면 0이다.
    private static double averageScore(Team team) {
        double average = team.getUsers().stream()
                .mapToInt(user -> user.getTier().getScore())
                .average()
                .orElse(0.0);
        return Math.round(average * 100) / 100.0;
    }
}
