package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Team;
import hello.dgswesport.domain.repository.TeamRepository;
import hello.dgswesport.dto.TeamRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    public Map<String, Object> teamRegister(TeamRequest request){
        Team team = new Team(request.getName(), request.getUsers());
        Map<String,Object> map = new HashMap<>();
        teamRepository.save(team);

        map.put("code",200);
        map.put("data",team);
        map.put("message","등록 완료");
        return map;
    }

    public Map<String, Object> getAllTeams() {
        Map<String,Object> map = new HashMap<>();
        List<Team> teams = teamRepository.findAll();
        map.put("code",200);
        map.put("data",teams);
        return map;
    }

    public Map<String, Object> getOneTeam(Long id) {
        Map<String,Object> map = new HashMap<>();
        Optional<Team> teams = teamRepository.findById(id);
        map.put("code",200);
        map.put("data",teams);
        return map;
    }

    public Map<String, Object> deleteTeam(Long id) {
        Map<String,Object> map = new HashMap<>();
        teamRepository.deleteById(id);
        map.put("code",200);
        map.put("message","삭제 완료");
        return map;
    }
}
