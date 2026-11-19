package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Tier;
import hello.dgswesport.domain.entity.User;
import hello.dgswesport.domain.repository.UserRepository;
import hello.dgswesport.dto.TeamResponse;
import hello.dgswesport.dto.UserResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 실제 DB(H2)와 연결해 Mock으로는 확인할 수 없는 팀-유저 연관관계 저장과 벌크 쿼리를 검증한다.
@DataJpaTest
@Import(TeamServiceImpl.class)
class TeamServiceImplJpaTest {

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("참가자 10명을 자동 배정하면 5명씩 두 팀의 소속이 DB에 저장된다")
    void teamRegisterAssignsFivePerTeam() {
        // given
        saveUsers(10);
        flushAndClear();

        // when
        List<TeamResponse> created = teamService.teamRegister();
        flushAndClear();

        // then
        assertThat(created).extracting(TeamResponse::id).doesNotContainNull();
        assertThat(userRepository.findByTeamIsNullOrderById()).isEmpty();
        List<TeamResponse> found = teamService.getAllTeams(null);
        assertThat(found).hasSize(2);
        assertThat(found).allSatisfy(team -> assertThat(team.users()).hasSize(5));
    }

    @Test
    @DisplayName("참가자가 12명이면 나중에 등록된 2명은 미배정으로 DB에 남는다")
    void teamRegisterLeavesRemainderUnassigned() {
        // given
        saveUsers(12);
        flushAndClear();

        // when
        teamService.teamRegister();
        flushAndClear();

        // then
        assertThat(userRepository.findByTeamIsNullOrderById())
                .extracting(User::getStudentNum)
                .containsExactly("S10", "S11");
    }

    @Test
    @DisplayName("팀 전체를 삭제하면 팀은 사라지고 유저는 미배정 상태로 남는다")
    void deleteAllTeamsKeepsUsers() {
        // given
        saveUsers(10);
        flushAndClear();
        teamService.teamRegister();
        flushAndClear();

        // when
        teamService.deleteAllTeams();
        flushAndClear();

        // then
        assertThat(teamService.getAllTeams(null)).isEmpty();
        assertThat(userRepository.count()).isEqualTo(10);
        assertThat(userRepository.findByTeamIsNullOrderById()).hasSize(10);
    }

    @Test
    @DisplayName("팀 단건 조회는 소속 팀원 5명과 팀 ID를 함께 반환한다")
    void getOneTeamReturnsMembers() {
        // given
        saveUsers(10);
        flushAndClear();
        Long teamId = teamService.teamRegister().get(0).id();
        flushAndClear();

        // when
        TeamResponse found = teamService.getOneTeam(teamId);

        // then
        assertThat(found.users()).hasSize(5);
        assertThat(found.users()).extracting(UserResponse::teamId).containsOnly(teamId);
    }

    // 학번 S0, S1, ... 순서로 count명을 저장한다. 티어는 전체 티어를 돌아가며 배정한다.
    private void saveUsers(int count) {
        Tier[] tiers = Tier.values();
        for (int i = 0; i < count; i++) {
            userRepository.save(User.builder()
                    .studentNum("S" + i)
                    .name("참가자" + i)
                    .gameNickname("nick" + i)
                    .tier(tiers[i % tiers.length])
                    .password("ENCODED")
                    .build());
        }
    }

    // 영속성 컨텍스트를 비워 이후 조회가 실제 DB 값을 읽게 한다.
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
