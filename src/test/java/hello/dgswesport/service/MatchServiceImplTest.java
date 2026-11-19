package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Competition;
import hello.dgswesport.domain.entity.Team;
import hello.dgswesport.domain.entity.User;
import hello.dgswesport.domain.repository.CompetitionRepository;
import hello.dgswesport.domain.repository.MatchRepository;
import hello.dgswesport.domain.repository.TeamRepository;
import hello.dgswesport.domain.repository.UserRepository;
import hello.dgswesport.dto.MatchResponse;
import hello.dgswesport.global.exception.BusinessException;
import hello.dgswesport.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// DB 없이 Repository를 Mock으로 대체해 Service의 비즈니스 규칙만 검증한다.
@ExtendWith(MockitoExtension.class)
class MatchServiceImplTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MatchServiceImpl matchService;

    @Test
    @DisplayName("팀 3개로 대진을 편성하면 1팀이 부전승으로 결승에 올라간다")
    void createMatches() {
        // given
        given(matchRepository.count()).willReturn(0L);
        given(teamRepository.findAll(any(Sort.class))).willReturn(teams(3));
        given(competitionRepository.findTopByOrderByIdDesc()).willReturn(Optional.empty());
        given(competitionRepository.save(any(Competition.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        List<MatchResponse> responses = matchService.createMatches();

        // then
        assertThat(responses).extracting(MatchResponse::matchOrder).containsExactly(1, 2);
        assertThat(responses).extracting(MatchResponse::round).containsExactly(1, 2);

        MatchResponse first = responses.get(0);
        assertThat(first.team1().teamName()).isEqualTo("2팀");
        assertThat(first.team2().teamName()).isEqualTo("3팀");
        assertThat(first.team1().bye()).isFalse();

        MatchResponse finalMatch = responses.get(1);
        assertThat(finalMatch.team1().teamName()).isEqualTo("1팀");
        assertThat(finalMatch.team1().bye()).isTrue();
        assertThat(finalMatch.team2().teamId()).isNull();
        assertThat(finalMatch.team2().winnerOfMatchOrder()).isEqualTo(1);

        verify(competitionRepository).save(any(Competition.class));
        verify(matchRepository).saveAll(any());
    }

    @Test
    @DisplayName("이미 대회가 있으면 새로 만들지 않고 그 대회에 대진을 편성한다")
    void createMatchesReusesCompetition() {
        // given
        given(matchRepository.count()).willReturn(0L);
        given(teamRepository.findAll(any(Sort.class))).willReturn(teams(2));
        given(competitionRepository.findTopByOrderByIdDesc()).willReturn(Optional.of(new Competition("대회")));

        // when
        List<MatchResponse> responses = matchService.createMatches();

        // then
        assertThat(responses).hasSize(1);
        verify(competitionRepository, never()).save(any(Competition.class));
    }

    @Test
    @DisplayName("이미 대진이 있으면 예외가 발생하고 경기를 만들지 않는다")
    void createMatchesWhenMatchesExist() {
        // given
        given(matchRepository.count()).willReturn(3L);

        // when & then
        assertThatThrownBy(() -> matchService.createMatches())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MATCHES_ALREADY_EXIST);

        verify(matchRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("팀이 2개보다 적으면 예외가 발생하고 경기를 만들지 않는다")
    void createMatchesWithNotEnoughTeams() {
        // given
        given(matchRepository.count()).willReturn(0L);
        given(teamRepository.findAll(any(Sort.class))).willReturn(teams(1));

        // when & then
        assertThatThrownBy(() -> matchService.createMatches())
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_ENOUGH_TEAMS);

        verify(matchRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("대회가 없으면 전체 일정은 빈 목록이다")
    void getAllMatchesWithoutCompetition() {
        // given
        given(competitionRepository.findTopByOrderByIdDesc()).willReturn(Optional.empty());

        // when
        List<MatchResponse> responses = matchService.getAllMatches();

        // then
        assertThat(responses).isEmpty();
        verify(matchRepository, never()).findMatchesByCompetitionIdOrderByMatchOrder(any());
    }

    @Test
    @DisplayName("없는 유저의 일정을 조회하면 예외가 발생한다")
    void getMatchesByUserNotFound() {
        // given
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> matchService.getMatchesByUser(99L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("팀에 배정되지 않은 유저의 일정은 빈 목록이다")
    void getMatchesByUserWithoutTeam() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(User.builder().id(1L).build()));

        // when
        List<MatchResponse> responses = matchService.getMatchesByUser(1L);

        // then
        assertThat(responses).isEmpty();
        verify(matchRepository, never()).findByTeamIdOrderByMatchOrder(anyLong());
    }

    // ID가 1부터 순서대로인 "1팀", "2팀", ... 을 만든다.
    private List<Team> teams(int count) {
        List<Team> teams = new ArrayList<>();
        for (long i = 1; i <= count; i++) {
            teams.add(Team.builder().id(i).name(i + "팀").build());
        }
        return teams;
    }
}
