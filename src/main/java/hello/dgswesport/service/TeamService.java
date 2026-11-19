package hello.dgswesport.service;

import hello.dgswesport.dto.TeamRequest;
import hello.dgswesport.dto.TeamResponse;

import java.util.List;

public interface TeamService {

    TeamResponse teamRegister(TeamRequest request);

    // name이 비어 있으면 전체 조회, 있으면 이름에 해당 문자열이 포함된 팀만 조회한다.
    List<TeamResponse> getAllTeams(String name);

    TeamResponse getOneTeam(Long id);

    TeamResponse updateTeam(Long id, TeamRequest request);

    void deleteTeam(Long id);
}
