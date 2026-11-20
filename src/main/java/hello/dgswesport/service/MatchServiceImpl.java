package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Competition;
import hello.dgswesport.domain.entity.Match;
import hello.dgswesport.domain.entity.Team;
import hello.dgswesport.domain.entity.User;
import hello.dgswesport.domain.repository.CompetitionRepository;
import hello.dgswesport.domain.repository.MatchRepository;
import hello.dgswesport.domain.repository.TeamRepository;
import hello.dgswesport.domain.repository.UserRepository;
import hello.dgswesport.dto.MatchResponse;
import hello.dgswesport.global.exception.BusinessException;
import hello.dgswesport.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    // 대회는 하나만 운영한다.
    private static final String COMPETITION_NAME = "DGSW E스포츠 대회";
    // 경기를 하려면 팀이 2개 이상이어야 한다.
    private static final int MIN_TEAM_COUNT = 2;

    @Override
    @Transactional
    public List<MatchResponse> createMatches() {
        if (matchRepository.count() > 0) {
            throw new BusinessException(ErrorCode.MATCHES_ALREADY_EXIST);
        }
        List<Team> teams = new ArrayList<>(teamRepository.findAll());
        if (teams.size() < MIN_TEAM_COUNT) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_TEAMS);
        }
        // 팀 번호는 만들어진 순서일 뿐 실력과 관계가 없으므로, 순서를 무작위로 섞어 시드를 정한다.
        // 부전승은 앞선 시드부터 받으므로 어느 팀이 부전승이 될지도 편성할 때마다 달라진다.
        Collections.shuffle(teams);
        Competition competition = competitionRepository.findTopByOrderByIdDesc()
                .orElseGet(() -> competitionRepository.save(new Competition(COMPETITION_NAME)));

        List<Match> matches = BracketGenerator.generate(competition, teams);
        matchRepository.saveAll(matches);
        return toResponses(matches);
    }

    @Override
    public List<MatchResponse> getAllMatches() {
        return competitionRepository.findTopByOrderByIdDesc()
                .map(competition -> matchRepository.findMatchesByCompetitionIdOrderByMatchOrder(competition.getId()))
                .map(this::toResponses)
                .orElseGet(List::of);
    }

    @Override
    public List<MatchResponse> getMatchesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getTeam() == null) {
            return List.of();
        }
        return toResponses(matchRepository.findByTeamIdOrderByMatchOrder(user.getTeam().getId()));
    }

    private List<MatchResponse> toResponses(List<Match> matches) {
        return matches.stream()
                .map(MatchResponse::from)
                .toList();
    }
}
