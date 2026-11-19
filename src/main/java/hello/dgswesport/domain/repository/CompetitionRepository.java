package hello.dgswesport.domain.repository;

import hello.dgswesport.domain.entity.Competition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {

    // 가장 최근에 만든 대회
    Optional<Competition> findTopByOrderByIdDesc();
}
