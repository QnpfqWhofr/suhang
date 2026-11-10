package hello.dgswesport.domain.repository;

import hello.dgswesport.domain.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findMatchesByCompetitionIdOrderByMatchOrder(Long competitionId);
}
