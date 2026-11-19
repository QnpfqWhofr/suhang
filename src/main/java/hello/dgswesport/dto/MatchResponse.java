package hello.dgswesport.dto;

import hello.dgswesport.domain.entity.Match;

public record MatchResponse(
        Long id,
        Integer matchOrder,
        Integer round,
        MatchSlotResponse team1,
        MatchSlotResponse team2
) {
    public static MatchResponse from(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getMatchOrder(),
                match.getRoundNumber(),
                MatchSlotResponse.of(match.getTeam1(), match.getTeam1SourceOrder(), match.getRoundNumber()),
                MatchSlotResponse.of(match.getTeam2(), match.getTeam2SourceOrder(), match.getRoundNumber())
        );
    }
}
