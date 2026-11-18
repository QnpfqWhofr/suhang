package hello.dgswesport.controller;

import hello.dgswesport.domain.entity.Team;
import hello.dgswesport.dto.TeamRequest;
import hello.dgswesport.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TeamController {
    @Autowired
    TeamService teamService;

    @PostMapping("/team")
    public ResponseEntity<Map<String, Object>> teamRegister(@RequestBody TeamRequest request){
        return ResponseEntity.ok(teamService.teamRegister(request));
    }

    @GetMapping("/team")
    public ResponseEntity<Map<String, Object>> getAllTeams(){
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/team/{id}")
    public ResponseEntity<Map<String, Object>> getOneTeam(@PathVariable Long id){
        return ResponseEntity.ok(teamService.getOneTeam(id));
    }
}
