package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Tier;
import hello.dgswesport.domain.entity.User;
import hello.dgswesport.domain.repository.UserRepository;
import hello.dgswesport.dto.TeamAssignRequest;
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
    @DisplayName("팀을 자동 배정하면 모든 참가자의 소속 팀이 DB에 저장된다")
    void teamRegisterAssignsAllUsers() {
        // given
        saveUsers(Tier.CHALLENGER, Tier.DIAMOND, Tier.GOLD, Tier.IRON);
        flushAndClear();

        // when
        List<TeamResponse> created = teamService.teamRegister(new TeamAssignRequest(2));
        flushAndClear();

        // then
        assertThat(userRepository.findByTeamIsNullOrderById()).isEmpty();
        List<TeamResponse> found = teamService.getAllTeams(null);
        assertThat(found).hasSize(2);
        assertThat(found).allSatisfy(team -> assertThat(team.users()).hasSize(2));
        assertThat(found).extracting(TeamResponse::averageScore).containsExactly(5.5, 5.5);
        assertThat(created).extracting(TeamResponse::id).doesNotContainNull();
    }

    @Test
    @DisplayName("이미 팀에 배정된 참가자는 다시 배정하지 않는다")
    void teamRegisterSkipsAssignedUsers() {
        // given
        saveUsers(Tier.GOLD, Tier.GOLD);
        flushAndClear();
        teamService.teamRegister(new TeamAssignRequest(2));
        flushAndClear();

        // when
        saveUsers(Tier.SILVER);
        flushAndClear();

        // then
        assertThat(userRepository.findByTeamIsNullOrderById())
                .extracting(User::getTier)
                .containsExactly(Tier.SILVER);
    }

    @Test
    @DisplayName("팀 전체를 삭제하면 팀은 사라지고 유저는 미배정 상태로 남는다")
    void deleteAllTeamsKeepsUsers() {
        // given
        saveUsers(Tier.CHALLENGER, Tier.DIAMOND, Tier.GOLD, Tier.IRON);
        flushAndClear();
        teamService.teamRegister(new TeamAssignRequest(2));
        flushAndClear();

        // when
        teamService.deleteAllTeams();
        flushAndClear();

        // then
        assertThat(teamService.getAllTeams(null)).isEmpty();
        assertThat(userRepository.count()).isEqualTo(4);
        assertThat(userRepository.findByTeamIsNullOrderById()).hasSize(4);
    }

    @Test
    @DisplayName("팀 단건 조회는 소속 팀원과 팀 ID를 함께 반환한다")
    void getOneTeamReturnsMembers() {
        // given
        saveUsers(Tier.DIAMOND, Tier.GOLD);
        flushAndClear();
        Long teamId = teamService.teamRegister(new TeamAssignRequest(2)).get(0).id();
        flushAndClear();

        // when
        TeamResponse found = teamService.getOneTeam(teamId);

        // then
        assertThat(found.users()).hasSize(1);
        assertThat(found.users()).extracting(UserResponse::teamId).containsExactly(teamId);
    }

    private void saveUsers(Tier... tiers) {
        long base = userRepository.count();
        for (int i = 0; i < tiers.length; i++) {
            userRepository.save(User.builder()
                    .studentNum("S" + (base + i))
                    .name("참가자" + (base + i))
                    .gameNickname("nick" + (base + i))
                    .tier(tiers[i])
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
