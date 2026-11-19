package hello.dgswesport.domain.repository;

import hello.dgswesport.domain.entity.Match;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    // 응답에 팀 이름이 필요하므로 두 팀을 함께 조회해 경기마다 팀을 따로 조회하지 않게 한다.
    @EntityGraph(attributePaths = {"team1", "team2"})
    List<Match> findMatchesByCompetitionIdOrderByMatchOrder(Long competitionId);

    // 두 자리 중 한 곳이라도 해당 팀이 배정된 경기를 경기 순서대로 조회한다.
    @EntityGraph(attributePaths = {"team1", "team2"})
    @Query("select m from Match m where m.team1.id = :teamId or m.team2.id = :teamId order by m.matchOrder")
    List<Match> findByTeamIdOrderByMatchOrder(@Param("teamId") Long teamId);
}
