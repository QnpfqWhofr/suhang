package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Team;
import hello.dgswesport.domain.entity.User;
import hello.dgswesport.domain.repository.TeamRepository;
import hello.dgswesport.dto.TeamRequest;
import hello.dgswesport.dto.TeamResponse;
import hello.dgswesport.dto.UserRequest;
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

    @Override
    @Transactional
    public TeamResponse teamRegister(TeamRequest request){
        if (teamRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_TEAM_NAME);
        }
        Team team = new Team(request.getName(), new ArrayList<>());
        team.getUsers().addAll(toUsers(request.getUsers(), team));
        // cascade 설정으로 팀을 저장하면 선수도 함께 저장된다.
        teamRepository.save(team);
        return TeamResponse.from(team);
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
        // orphanRemoval 설정으로 목록에서 빠진 기존 선수는 삭제된다.
        team.getUsers().clear();
        team.getUsers().addAll(toUsers(request.getUsers(), team));
        // 새로 추가된 선수의 ID가 응답에 담기도록 INSERT를 먼저 실행한다.
        teamRepository.flush();
        return TeamResponse.from(team);
    }

    @Override
    @Transactional
    public void deleteTeam(Long id) {
        teamRepository.delete(findTeam(id));
    }

    private Team findTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
    }

    // 선수의 team을 채워야 team_id가 저장된다.
    private List<User> toUsers(List<UserRequest> requests, Team team) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .map(request -> User.builder()
                        .student_num(request.studentNum())
                        .name(request.name())
                        .tier(request.tier())
                        .team(team)
                        .build())
                .toList();
    }
}
