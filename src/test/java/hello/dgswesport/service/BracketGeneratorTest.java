package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Competition;
import hello.dgswesport.domain.entity.Match;
import hello.dgswesport.domain.entity.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BracketGeneratorTest {

    private final Competition competition = new Competition("대회");

    @Test
    @DisplayName("팀이 2개면 결승 한 경기만 만든다")
    void generateWithTwoTeams() {
        // when
        List<Match> matches = BracketGenerator.generate(competition, teams(2));

        // then
        assertThat(matches).hasSize(1);
        Match finalMatch = matches.get(0);
        assertThat(finalMatch.getMatchOrder()).isEqualTo(1);
        assertThat(finalMatch.getRoundNumber()).isEqualTo(1);
        assertThat(finalMatch.getTeam1().getName()).isEqualTo("1팀");
        assertThat(finalMatch.getTeam2().getName()).isEqualTo("2팀");
        assertThat(finalMatch.getCompetition()).isSameAs(competition);
    }

    @Test
    @DisplayName("팀이 4개면 1라운드는 양 끝 시드끼리 붙고 결승은 두 경기의 승자가 만난다")
    void generateWithFourTeams() {
        // when
        List<Match> matches = BracketGenerator.generate(competition, teams(4));

        // then
        assertThat(matches).extracting(Match::getMatchOrder).containsExactly(1, 2, 3);
        assertThat(matches).extracting(Match::getRoundNumber).containsExactly(1, 1, 2);
        assertThat(teamNames(matches.get(0))).containsExactly("1팀", "4팀");
        assertThat(teamNames(matches.get(1))).containsExactly("2팀", "3팀");

        Match finalMatch = matches.get(2);
        assertThat(finalMatch.getTeam1()).isNull();
        assertThat(finalMatch.getTeam2()).isNull();
        assertThat(finalMatch.getTeam1SourceOrder()).isEqualTo(1);
        assertThat(finalMatch.getTeam2SourceOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("팀이 5개면 상위 3개 팀이 부전승으로 2라운드에 진출한다")
    void generateWithFiveTeamsGivesByes() {
        // when
        List<Match> matches = BracketGenerator.generate(competition, teams(5));

        // then: 1라운드 1경기, 2라운드 2경기, 결승 1경기
        assertThat(matches).extracting(Match::getRoundNumber).containsExactly(1, 2, 2, 3);
        assertThat(teamNames(matches.get(0))).containsExactly("4팀", "5팀");

        // 1팀은 1경기 승자와, 2팀과 3팀은 서로 2라운드에서 만난다
        Match second = matches.get(1);
        assertThat(second.getTeam1().getName()).isEqualTo("1팀");
        assertThat(second.getTeam1SourceOrder()).isNull();
        assertThat(second.getTeam2()).isNull();
        assertThat(second.getTeam2SourceOrder()).isEqualTo(1);
        assertThat(teamNames(matches.get(2))).containsExactly("2팀", "3팀");

        Match finalMatch = matches.get(3);
        assertThat(finalMatch.getTeam1SourceOrder()).isEqualTo(2);
        assertThat(finalMatch.getTeam2SourceOrder()).isEqualTo(3);
    }

    @Test
    @DisplayName("팀 수와 관계없이 경기는 팀 수보다 하나 적고 모든 팀이 한 번씩 배정된다")
    void generateKeepsBracketConsistent() {
        for (int teamCount = 2; teamCount <= 16; teamCount++) {
            // when
            List<Team> teams = teams(teamCount);
            List<Match> matches = BracketGenerator.generate(competition, teams);

            // then: 경기 순서는 1부터 빠짐없이 이어진다
            assertThat(matches).hasSize(teamCount - 1);
            assertThat(matches).extracting(Match::getMatchOrder)
                    .containsExactlyElementsOf(IntStream.rangeClosed(1, teamCount - 1).boxed().toList());

            // 모든 팀은 정확히 한 자리에 배정된다
            List<Team> assigned = matches.stream()
                    .flatMap(match -> Stream.of(match.getTeam1(), match.getTeam2()))
                    .filter(Objects::nonNull)
                    .toList();
            assertThat(assigned).containsExactlyInAnyOrderElementsOf(teams);

            // 결승을 뺀 모든 경기의 승자는 정확히 한 번씩 뒤 경기로 올라간다
            List<Integer> sources = matches.stream()
                    .flatMap(match -> Stream.of(match.getTeam1SourceOrder(), match.getTeam2SourceOrder()))
                    .filter(Objects::nonNull)
                    .toList();
            assertThat(sources)
                    .containsExactlyInAnyOrderElementsOf(IntStream.rangeClosed(1, teamCount - 2).boxed().toList());

            // 마지막 경기가 결승이고 라운드 수는 팀 수를 담는 대진표 크기로 정해진다
            int expectedRounds = 32 - Integer.numberOfLeadingZeros(teamCount - 1);
            assertThat(matches.get(matches.size() - 1).getRoundNumber()).isEqualTo(expectedRounds);
        }
    }

    // ID가 1부터 순서대로인 "1팀", "2팀", ... 을 만든다.
    private List<Team> teams(int count) {
        List<Team> teams = new ArrayList<>();
        for (long i = 1; i <= count; i++) {
            teams.add(Team.builder().id(i).name(i + "팀").build());
        }
        return teams;
    }

    private List<String> teamNames(Match match) {
        return List.of(match.getTeam1().getName(), match.getTeam2().getName());
    }
}
