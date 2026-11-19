package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Team;
import hello.dgswesport.domain.entity.Tier;
import hello.dgswesport.domain.entity.User;
import hello.dgswesport.domain.repository.MatchRepository;
import hello.dgswesport.domain.repository.TeamRepository;
import hello.dgswesport.domain.repository.UserRepository;
import hello.dgswesport.dto.MatchResponse;
import hello.dgswesport.global.config.JpaAuditingConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 실제 DB(H2)와 연결해 대진 저장, 팀 기준 조회 쿼리, 팀 삭제 시 대진 삭제를 검증한다.
// Match가 상속한 BaseEntity의 생성·수정 시각이 채워지도록 JpaAuditingConfig를 함께 불러온다.
@DataJpaTest
@Import({MatchServiceImpl.class, TeamServiceImpl.class, JpaAuditingConfig.class})
class MatchServiceImplJpaTest {

    @Autowired
    private MatchService matchService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("팀 3개로 편성한 대진이 DB에 저장되고 전체 일정이 경기 순서대로 조회된다")
    void createMatchesAndGetAll() {
        // given
        saveTeams(3);
        flushAndClear();

        // when
        List<MatchResponse> created = matchService.createMatches();
        flushAndClear();

        // then
        assertThat(created).extracting(MatchResponse::id).doesNotContainNull();
        List<MatchResponse> found = matchService.getAllMatches();
        assertThat(found).extracting(MatchResponse::matchOrder).containsExactly(1, 2);
        assertThat(found.get(0).team1().teamName()).isEqualTo("2팀");
        assertThat(found.get(0).team2().teamName()).isEqualTo("3팀");
        assertThat(found.get(1).team1().teamName()).isEqualTo("1팀");
        assertThat(found.get(1).team1().bye()).isTrue();
        assertThat(found.get(1).team2().winnerOfMatchOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("유저 일정 조회는 그 유저의 팀이 배정된 경기만 반환한다")
    void getMatchesByUser() {
        // given: 1팀은 부전승으로 2경기에, 3팀은 1경기에 배정된다
        List<Team> teams = saveTeams(3);
        User firstTeamUser = saveUser("S1", teams.get(0));
        User thirdTeamUser = saveUser("S3", teams.get(2));
        User unassignedUser = saveUser("S9", null);
        flushAndClear();
        matchService.createMatches();
        flushAndClear();

        // when & then
        assertThat(matchService.getMatchesByUser(firstTeamUser.getId()))
                .extracting(MatchResponse::matchOrder).containsExactly(2);
        assertThat(matchService.getMatchesByUser(thirdTeamUser.getId()))
                .extracting(MatchResponse::matchOrder).containsExactly(1);
        assertThat(matchService.getMatchesByUser(unassignedUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("팀 전체를 삭제하면 편성된 대진도 함께 삭제된다")
    void deleteAllTeamsRemovesMatches() {
        // given
        saveTeams(4);
        flushAndClear();
        matchService.createMatches();
        flushAndClear();

        // when
        teamService.deleteAllTeams();
        flushAndClear();

        // then
        assertThat(matchRepository.count()).isZero();
        assertThat(teamRepository.count()).isZero();
        assertThat(matchService.getAllMatches()).isEmpty();
    }

    @Test
    @DisplayName("팀 하나를 삭제하면 대진이 삭제되고 남은 팀으로 다시 편성할 수 있다")
    void deleteTeamRemovesMatchesAndAllowsRecreate() {
        // given
        List<Team> teams = saveTeams(3);
        flushAndClear();
        matchService.createMatches();
        flushAndClear();

        // when
        teamService.deleteTeam(teams.get(0).getId());
        flushAndClear();

        // then
        assertThat(matchRepository.count()).isZero();
        List<MatchResponse> recreated = matchService.createMatches();
        assertThat(recreated).hasSize(1);
        assertThat(recreated.get(0).team1().teamName()).isEqualTo("2팀");
        assertThat(recreated.get(0).team2().teamName()).isEqualTo("3팀");
    }

    // "1팀", "2팀", ... 순서로 count개를 저장한다.
    private List<Team> saveTeams(int count) {
        List<Team> teams = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            teams.add(teamRepository.save(new Team(i + "팀", new ArrayList<>())));
        }
        return teams;
    }

    private User saveUser(String studentNum, Team team) {
        return userRepository.save(User.builder()
                .studentNum(studentNum)
                .name("참가자" + studentNum)
                .gameNickname("nick" + studentNum)
                .tier(Tier.GOLD)
                .password("ENCODED")
                .team(team)
                .build());
    }

    // 영속성 컨텍스트를 비워 이후 조회가 실제 DB 값을 읽게 한다.
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
