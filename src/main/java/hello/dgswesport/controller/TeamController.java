package hello.dgswesport.controller;

import hello.dgswesport.domain.entity.Team;
import hello.dgswesport.dto.TeamRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TeamController {

    @PostMapping("/team")
    public ResponseEntity<Map<String, Object>> teamRegister(@RequestBody TeamRequest request){
        Team team = new Team(
                request.getName(),
                request.getUsers()
        );
        return ResponseEntity.ok();
    }
}
