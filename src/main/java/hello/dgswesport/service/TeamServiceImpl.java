package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Team;
import hello.dgswesport.domain.entity.User;
import hello.dgswesport.domain.repository.TeamRepository;
import hello.dgswesport.domain.repository.UserRepository;
import hello.dgswesport.dto.TeamAssignRequest;
import hello.dgswesport.dto.TeamRequest;
import hello.dgswesport.dto.TeamResponse;
import hello.dgswesport.global.exception.BusinessException;
import hello.dgswesport.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public List<TeamResponse> teamRegister(TeamAssignRequest request){
        int teamCount = request.teamCount();
        if (teamRepository.count() > 0) {
            throw new BusinessException(ErrorCode.TEAMS_ALREADY_EXIST);
        }
        List<User> users = userRepository.findByTeamIsNullOrderById();
        if (users.size() < teamCount) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_USERS);
        }

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
