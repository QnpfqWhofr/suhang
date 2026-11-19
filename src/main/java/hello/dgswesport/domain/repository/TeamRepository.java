package hello.dgswesport.domain.repository;

import hello.dgswesport.domain.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team,Long> {

    // 수정 시 자기 자신을 제외하고 같은 이름이 있는지 확인한다.
    boolean existsByNameAndIdNot(String name, Long id);

    List<Team> findByNameContainingOrderByName(String name);
}
