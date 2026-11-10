package hello.dgswesport.domain.repository;

import hello.dgswesport.domain.entity.Competition;
import hello.dgswesport.domain.entity.Match;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class MatchRepositoryTest {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Test
    @DisplayName("대회 ID로 조회 후 경기 순서대로 정렬한다")
    void findMatchesByCompetitionIdOrderByOrder() {
        // given
        Competition competition = new Competition("대회");
        Match matchA = new Match(0, competition);
        Match matchB = new Match(1, competition);
        Match matchD = new Match(3, competition);
        Match matchC = new Match(2, competition);

        competitionRepository.save(competition);
        matchRepository.save(matchA);
        matchRepository.save(matchB);
        matchRepository.save(matchC);
        matchRepository.save(matchD);

        // when
        List<Match> matches = matchRepository.findMatchesByCompetitionIdOrderByMatchOrder(1L);

        // then
        assertThat(matches.get(0).getMatchOrder()).isEqualTo(0);
        assertThat(matches.get(1).getMatchOrder()).isEqualTo(1);
        assertThat(matches.get(2).getMatchOrder()).isEqualTo(2);
        assertThat(matches.get(3).getMatchOrder()).isEqualTo(3);
    }
}