package hello.dgswesport.dto;

import hello.dgswesport.domain.entity.Team;

// 경기의 한 자리. 팀이 정해졌으면 teamId·teamName이, 아직이면 winnerOfMatchOrder(승자가 올라올 경기 순서)가 채워진다.
public record MatchSlotResponse(
        Long teamId,
        String teamName,
        Integer winnerOfMatchOrder,
        boolean bye
) {
    public static MatchSlotResponse of(Team team, Integer sourceOrder, Integer round) {
        if (team == null) {
            return new MatchSlotResponse(null, null, sourceOrder, false);
        }
        // 2라운드 이후인데 앞선 경기 없이 팀이 정해져 있으면 부전승으로 올라온 팀이다.
        boolean bye = round != null && round > 1 && sourceOrder == null;
        return new MatchSlotResponse(team.getId(), team.getName(), null, bye);
    }
}
