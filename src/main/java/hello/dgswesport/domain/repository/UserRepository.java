package hello.dgswesport.domain.repository;

import hello.dgswesport.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    boolean existsByStudentNum(String studentNum);

    Optional<User> findByStudentNum(String studentNum);

    // 아직 팀에 배정되지 않은 참가자
    List<User> findByTeamIsNullOrderById();

    // 팀 전체 삭제 전에 모든 유저의 소속을 해제한다. 유저 수만큼 UPDATE하지 않도록 벌크 쿼리 한 번으로 처리한다.
    // 벌크 쿼리는 영속성 컨텍스트를 거치지 않으므로 실행 전후로 flush, clear 한다.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update User u set u.team = null where u.team is not null")
    int clearAllTeams();
}
