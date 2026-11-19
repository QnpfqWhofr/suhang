package hello.dgswesport.service;

import hello.dgswesport.dto.TeamRequest;
import hello.dgswesport.dto.TeamResponse;

import java.util.List;

public interface TeamService {

    // 팀에 배정되지 않은 참가자를 5명씩, 티어 점수가 균등하도록 나눠 팀을 생성한다.
    // 5명 단위에 못 미쳐 남는 참가자는 미배정으로 남는다.
    List<TeamResponse> teamRegister();

    // name이 비어 있으면 전체 조회, 있으면 이름에 해당 문자열이 포함된 팀만 조회한다.
    List<TeamResponse> getAllTeams(String name);

    TeamResponse getOneTeam(Long id);

    TeamResponse updateTeam(Long id, TeamRequest request);

    // 팀 하나만 삭제하고 소속 유저는 미배정 상태로 남긴다.
    void deleteTeam(Long id);

    // 팀 전체를 삭제하고 유저는 미배정 상태로 남긴다.
    void deleteAllTeams();
}
