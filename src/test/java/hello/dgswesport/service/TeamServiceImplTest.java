package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Team;
import hello.dgswesport.domain.repository.TeamRepository;
import hello.dgswesport.dto.TeamRequest;
import hello.dgswesport.dto.TeamResponse;
import hello.dgswesport.dto.UserRequest;
import hello.dgswesport.dto.UserResponse;
import hello.dgswesport.global.exception.BusinessException;
import hello.dgswesport.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// DB 없이 Repository를 Mock으로 대체해 Service의 비즈니스 규칙만 검증한다.
@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    @Test
    @DisplayName("팀과 선수를 등록하면 등록된 팀 정보를 반환한다")
    void teamRegister() {
        // given
        given(teamRepository.existsByName("알파")).willReturn(false);
        TeamRequest request = new TeamRequest("알파", List.of(new UserRequest("2101", "홍길동", "골드")));

        // when
        TeamResponse response = teamService.teamRegister(request);

        // then
        assertThat(response.name()).isEqualTo("알파");
        assertThat(response.users()).extracting(UserResponse::name).containsExactly("홍길동");
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    @DisplayName("선수 목록 없이 등록하면 선수가 없는 팀이 등록된다")
    void teamRegisterWithoutUsers() {
        // given
        given(teamRepository.existsByName("알파")).willReturn(false);

        // when
        TeamResponse response = teamService.teamRegister(new TeamRequest("알파", null));

        // then
        assertThat(response.users()).isEmpty();
    }

    @Test
    @DisplayName("이미 있는 이름으로 등록하면 예외가 발생하고 저장하지 않는다")
    void teamRegisterWithDuplicateName() {
        // given
        given(teamRepository.existsByName("알파")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> teamService.teamRegister(new TeamRequest("알파", null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_TEAM_NAME);

        verify(teamRepository, never()).save(any());
    }

    @Test
    @DisplayName("검색어가 없으면 전체 팀을 조회한다")
    void getAllTeamsWithoutName() {
        // given
        given(teamRepository.findAll()).willReturn(List.of(team("알파"), team("베타")));

        // when
        List<TeamResponse> responses = teamService.getAllTeams(null);

        // then
        assertThat(responses).extracting(TeamResponse::name).containsExactly("알파", "베타");
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
    @DisplayName("팀을 수정하면 이름과 선수 목록이 변경된다")
    void updateTeam() {
        // given
        Team team = team("알파");
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamRepository.existsByNameAndIdNot("베타", 1L)).willReturn(false);
        TeamRequest request = new TeamRequest("베타", List.of(new UserRequest("2102", "임꺽정", "실버")));

        // when
        TeamResponse response = teamService.updateTeam(1L, request);

        // then
        assertThat(response.name()).isEqualTo("베타");
        assertThat(team.getName()).isEqualTo("베타");
        assertThat(team.getUsers()).hasSize(1);
        assertThat(team.getUsers().get(0).getTeam()).isSameAs(team);
    }

    @Test
    @DisplayName("다른 팀이 쓰는 이름으로 수정하면 예외가 발생하고 변경되지 않는다")
    void updateTeamWithDuplicateName() {
        // given
        Team team = team("알파");
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamRepository.existsByNameAndIdNot("베타", 1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> teamService.updateTeam(1L, new TeamRequest("베타", null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_TEAM_NAME);

        assertThat(team.getName()).isEqualTo("알파");
    }

    @Test
    @DisplayName("팀을 삭제한다")
    void deleteTeam() {
        // given
        Team team = team("알파");
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));

        // when
        teamService.deleteTeam(1L);

        // then
        verify(teamRepository).delete(team);
    }

    @Test
    @DisplayName("없는 팀을 삭제하면 예외가 발생한다")
    void deleteTeamNotFound() {
        // given
        given(teamRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> teamService.deleteTeam(99L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEAM_NOT_FOUND);

        verify(teamRepository, never()).delete(any());
    }

    private Team team(String name) {
        return new Team(name, new ArrayList<>());
    }
}
