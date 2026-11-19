package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Tier;
import hello.dgswesport.dto.TeamRequest;
import hello.dgswesport.dto.TeamResponse;
import hello.dgswesport.dto.UserRequest;
import hello.dgswesport.dto.UserResponse;
import hello.dgswesport.global.exception.BusinessException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 실제 DB(H2)와 연결해 Mock으로는 확인할 수 없는 팀-선수 연관관계 저장을 검증한다.
@DataJpaTest
@Import(TeamServiceImpl.class)
class TeamServiceImplJpaTest {

    @Autowired
    private TeamService teamService;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("팀과 선수를 함께 등록하면 선수가 팀에 소속되어 저장된다")
    void teamRegisterWithUsers() {
        // given
        TeamRequest request = new TeamRequest("알파", List.of(
                new UserRequest("2101", "홍길동", Tier.GOLD),
                new UserRequest("2102", "임꺽정", Tier.SILVER)));

        // when
        TeamResponse created = teamService.teamRegister(request);
        flushAndClear();

        // then
        TeamResponse found = teamService.getOneTeam(created.id());
        assertThat(found.users()).extracting(UserResponse::name).containsExactlyInAnyOrder("홍길동", "임꺽정");
        assertThat(found.users()).allSatisfy(user -> assertThat(user.id()).isNotNull());
    }

    @Test
    @DisplayName("팀을 수정하면 기존 선수는 삭제되고 새 선수가 ID와 함께 응답된다")
    void updateTeamReplacesUsers() {
        // given
        TeamResponse created = teamService.teamRegister(
                new TeamRequest("알파", List.of(new UserRequest("2101", "홍길동", Tier.GOLD))));
        flushAndClear();

        // when
        TeamResponse updated = teamService.updateTeam(created.id(),
                new TeamRequest("베타", List.of(new UserRequest("2102", "임꺽정", Tier.SILVER))));
        flushAndClear();

        // then
        assertThat(updated.name()).isEqualTo("베타");
        assertThat(updated.users()).extracting(UserResponse::name).containsExactly("임꺽정");
        assertThat(updated.users().get(0).id()).isNotNull();
        assertThat(countUsers()).isEqualTo(1);
    }

    @Test
    @DisplayName("팀을 삭제하면 소속 선수도 함께 삭제된다")
    void deleteTeamRemovesUsers() {
        // given
        TeamResponse created = teamService.teamRegister(
                new TeamRequest("알파", List.of(new UserRequest("2101", "홍길동", Tier.GOLD))));
        flushAndClear();

        // when
        teamService.deleteTeam(created.id());
        flushAndClear();

        // then
        assertThat(countUsers()).isZero();
        assertThatThrownBy(() -> teamService.getOneTeam(created.id()))
                .isInstanceOf(BusinessException.class);
    }

    // 영속성 컨텍스트를 비워 이후 조회가 실제 DB 값을 읽게 한다.
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private long countUsers() {
        return entityManager.createQuery("select count(u) from User u", Long.class).getSingleResult();
    }
}
