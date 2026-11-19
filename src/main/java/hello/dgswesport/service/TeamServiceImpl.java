package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Team;
import hello.dgswesport.domain.entity.User;
import hello.dgswesport.domain.repository.TeamRepository;
import hello.dgswesport.domain.repository.UserRepository;
import hello.dgswesport.dto.TeamRequest;
import hello.dgswesport.dto.TeamResponse;
import hello.dgswesport.global.exception.BusinessException;
import hello.dgswesport.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    // 한 팀의 인원. 5대5 게임 기준이다.
    private static final int TEAM_SIZE = 5;
    // 팀 간 균형을 맞추려면 팀이 2개 이상이어야 한다.
    private static final int MIN_TEAM_COUNT = 2;

    @Override
    @Transactional
    public List<TeamResponse> teamRegister(){
        if (teamRepository.count() > 0) {
            throw new BusinessException(ErrorCode.TEAMS_ALREADY_EXIST);
        }
        List<User> unassigned = userRepository.findByTeamIsNullOrderById();
        int teamCount = unassigned.size() / TEAM_SIZE;
        if (teamCount < MIN_TEAM_COUNT) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_USERS);
        }
        // 5명 단위로 나누어떨어지는 인원까지만 등록 순서대로 배정하고, 남는 인원은 미배정으로 둔다.
        List<User> users = unassigned.subList(0, teamCount * TEAM_SIZE);

        List<Team> teams = new ArrayList<>();
        for (int i = 1; i <= teamCount; i++) {
            teams.add(new Team(i + "팀", new ArrayList<>()));
        }
        teamRepository.saveAll(teams);

        List<List<User>> groups = TeamBalancer.distribute(users, teamCount);
        for (int i = 0; i < teamCount; i++) {
            Team team = teams.get(i);
            for (User user : groups.get(i)) {
                // 연관관계의 주인인 User.team을 지정해야 team_id가 저장된다. 변경 감지로 UPDATE된다.
                user.setTeam(team);
                team.getUsers().add(user);
            }
        }
        return teams.stream()
                .map(TeamResponse::from)
                .toList();
    }

    @Override
    public List<TeamResponse> getAllTeams(String name) {
        List<Team> teams = StringUtils.hasText(name)
                ? teamRepository.findByNameContainingOrderByName(name)
                : teamRepository.findAll();
        return teams.stream()
                .map(TeamResponse::from)
                .toList();
    }

    @Override
    public TeamResponse getOneTeam(Long id) {
        return TeamResponse.from(findTeam(id));
    }

    @Override
    @Transactional
    public TeamResponse updateTeam(Long id, TeamRequest request) {
        Team team = findTeam(id);
        if (teamRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_TEAM_NAME);
        }
        team.setName(request.getName());
        return TeamResponse.from(team);
    }

    @Override
    @Transactional
    public void deleteTeam(Long id) {
        Team team = findTeam(id);
        // team_id 외래키 때문에 소속 유저를 먼저 미배정으로 되돌린 뒤 팀을 삭제한다.
        team.getUsers().forEach(user -> user.setTeam(null));
        teamRepository.delete(team);
    }

    @Override
    @Transactional
    public void deleteAllTeams() {
        // team_id 외래키 때문에 유저의 소속을 먼저 해제한 뒤 팀을 삭제한다.
        userRepository.clearAllTeams();
        teamRepository.deleteAllInBatch();
    }

    private Team findTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
    }
}
