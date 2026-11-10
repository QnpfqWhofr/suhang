package hello.dgswesport.domain.repository;

import hello.dgswesport.domain.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {
}
