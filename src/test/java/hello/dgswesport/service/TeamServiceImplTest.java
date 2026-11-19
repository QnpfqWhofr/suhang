package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Team;
import hello.dgswesport.domain.entity.Tier;
import hello.dgswesport.domain.entity.User;
import hello.dgswesport.domain.repository.MatchRepository;
import hello.dgswesport.domain.repository.TeamRepository;
import hello.dgswesport.domain.repository.UserRepository;
import hello.dgswesport.dto.TeamRequest;
import hello.dgswesport.dto.TeamResponse;
import hello.dgswesport.global.exception.BusinessException;
import hello.dgswesport.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// DB 없이 Repository를 Mock으로 대체해 Service의 비즈니스 규칙만 검증한다.
@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    @Test
    @DisplayName("미배정 참가자 10명을 5명씩 두 팀으로 나누고 팀별 평균 점수를 반환한다")
    void teamRegister() {
        // given: 점수 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 (합 55)
        List<User> users = users(10);
        given(teamRepository.count()).willReturn(0L);
        given(userRepository.findByTeamIsNullOrderById()).willReturn(users);

        // when
        List<TeamResponse> responses = teamService.teamRegister();

        // then: 합 28과 27로 나뉜다
        assertThat(responses).extracting(TeamResponse::name).containsExactly("1팀", "2팀");
        assertThat(responses).allSatisfy(team -> assertThat(team.users()).hasSize(5));
        assertThat(responses).extracting(TeamResponse::averageScore).containsExactly(5.6, 5.4);
        assertThat(users).allSatisfy(user -> assertThat(user.getTeam()).isNotNull());
        verify(teamRepository).saveAll(any());
    }

    @Test
    @DisplayName("5명 단위에 못 미쳐 남는 참가자는 나중에 등록된 순서대로 미배정으로 남는다")
    void teamRegisterLeavesRemainderUnassigned() {
        // given: 12명이면 2팀(10명)만 배정된다
        List<User> users = users(12);
        given(teamRepository.count()).willReturn(0L);
        given(userRepository.findByTeamIsNullOrderById()).willReturn(users);

        // when
        List<TeamResponse> responses = teamService.teamRegister();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).allSatisfy(team -> assertThat(team.users()).hasSize(5));
        assertThat(users.subList(0, 10)).allSatisfy(user -> assertThat(user.getTeam()).isNotNull());
        assertThat(users.subList(10, 12)).allSatisfy(user -> assertThat(user.getTeam()).isNull());
    }

    @Test
    @DisplayName("이미 팀이 있으면 예외가 발생하고 팀을 만들지 않는다")
    void teamRegisterWhenTeamsExist() {
        // given
        given(teamRepository.count()).willReturn(3L);

        // when & then
        assertThatThrownBy(() -> teamService.teamRegister())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEAMS_ALREADY_EXIST);

        verify(teamRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("미배정 참가자가 10명보다 적으면 예외가 발생하고 팀을 만들지 않는다")
    void teamRegisterWithNotEnoughUsers() {
        // given: 9명이면 5명 팀을 하나밖에 만들 수 없다
        given(teamRepository.count()).willReturn(0L);
        given(userRepository.findByTeamIsNullOrderById()).willReturn(users(9));

        // when & then
        assertThatThrownBy(() -> teamService.teamRegister())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_ENOUGH_USERS);

        verify(teamRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("검색어가 없으면 전체 팀을 조회한다")
    void getAllTeamsWithoutName() {
        // given
        given(teamRepository.findAll()).willReturn(List.of(team("1팀"), team("2팀")));

        // when
        List<TeamResponse> responses = teamService.getAllTeams(null);

        // then
        assertThat(responses).extracting(TeamResponse::name).containsExactly("1팀", "2팀");
    }

    @Test
    @DisplayName("검색어가 있으면 이름으로 검색한다")
    void getAllTeamsWithName() {
        // given
        given(teamRepository.findByNameContainingOrderByName("알")).willReturn(List.of(team("알파")));

        // when
        List<TeamResponse> responses = teamService.getAllTeams("알");

        // then
        assertThat(responses).extracting(TeamResponse::name).containsExactly("알파");
        verify(teamRepository, never()).findAll();
    }

    @Test
    @DisplayName("팀원이 없는 팀의 평균 점수는 0이다")
    void getOneTeamWithoutUsers() {
        // given
        given(teamRepository.findById(1L)).willReturn(Optional.of(team("1팀")));

        // when
        TeamResponse response = teamService.getOneTeam(1L);

        // then
        assertThat(response.averageScore()).isZero();
        assertThat(response.users()).isEmpty();
    }

    @Test
    @DisplayName("없는 팀을 조회하면 예외가 발생한다")
    void getOneTeamNotFound() {
        // given
        given(teamRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> teamService.getOneTeam(99L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEAM_NOT_FOUND);
    }

    @Test
    @DisplayName("팀 이름을 수정한다")
    void updateTeam() {
        // given
        Team team = team("1팀");
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamRepository.existsByNameAndIdNot("알파", 1L)).willReturn(false);

        // when
        TeamResponse response = teamService.updateTeam(1L, new TeamRequest("알파"));

        // then
        assertThat(response.name()).isEqualTo("알파");
        assertThat(team.getName()).isEqualTo("알파");
    }

    @Test
    @DisplayName("다른 팀이 쓰는 이름으로 수정하면 예외가 발생하고 변경되지 않는다")
    void updateTeamWithDuplicateName() {
        // given
        Team team = team("1팀");
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamRepository.existsByNameAndIdNot("2팀", 1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> teamService.updateTeam(1L, new TeamRequest("2팀")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_TEAM_NAME);

        assertThat(team.getName()).isEqualTo("1팀");
    }

    @Test
    @DisplayName("팀 전체 삭제는 대진을 지우고 유저의 소속을 해제한 뒤 팀을 삭제한다")
    void deleteAllTeams() {
        // when
        teamService.deleteAllTeams();

        // then
        InOrder order = inOrder(matchRepository, userRepository, teamRepository);
        order.verify(matchRepository).deleteAllInBatch();
        order.verify(userRepository).clearAllTeams();
        order.verify(teamRepository).deleteAllInBatch();
    }

    @Test
    @DisplayName("팀 하나를 삭제하면 대진을 지우고 소속 유저를 미배정으로 되돌린 뒤 팀을 삭제한다")
    void deleteTeam() {
        // given
        Team team = team("1팀");
        List<User> members = users(2);
        members.forEach(user -> {
            user.setTeam(team);
            team.getUsers().add(user);
        });
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));

        // when
        teamService.deleteTeam(1L);

        // then
        assertThat(members).allSatisfy(user -> assertThat(user.getTeam()).isNull());
        InOrder order = inOrder(matchRepository, teamRepository);
        order.verify(matchRepository).deleteAllInBatch();
        order.verify(teamRepository).delete(team);
    }

    @Test
    @DisplayName("없는 팀을 삭제하면 예외가 발생하고 삭제하지 않는다")
    void deleteTeamNotFound() {
        // given
        given(teamRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> teamService.deleteTeam(99L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEAM_NOT_FOUND);

        verify(teamRepository, never()).delete(any(Team.class));
        verify(matchRepository, never()).deleteAllInBatch();
    }

    private Team team(String name) {
        return new Team(name, new ArrayList<>());
    }

    // 점수가 10(CHALLENGER)부터 1씩 낮아지는 참가자 count명. 10명을 넘으면 다시 10점부터 반복한다.
    private List<User> users(int count) {
        Tier[] tiers = Tier.values();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(User.builder().tier(tiers[tiers.length - 1 - (i % tiers.length)]).build());
        }
        return users;
    }
}
