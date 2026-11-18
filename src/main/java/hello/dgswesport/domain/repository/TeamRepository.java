package hello.dgswesport.domain.repository;

import hello.dgswesport.domain.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team,Long> {
}
