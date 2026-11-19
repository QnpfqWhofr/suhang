package hello.dgswesport.controller;

import hello.dgswesport.dto.TeamRequest;
import hello.dgswesport.dto.TeamResponse;
import hello.dgswesport.global.response.ApiResponse;
import hello.dgswesport.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TeamController {
    @Autowired
    TeamService teamService;

    @PostMapping("/team")
    public ResponseEntity<ApiResponse<TeamResponse>> teamRegister(@Valid @RequestBody TeamRequest request){
        TeamResponse response = teamService.teamRegister(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping("/team")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getAllTeams(@RequestParam(required = false) String name){
        return ResponseEntity.ok(ApiResponse.ok(teamService.getAllTeams(name)));
    }

    @GetMapping("/team/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> getOneTeam(@PathVariable Long id){
        return ResponseEntity.ok(ApiResponse.ok(teamService.getOneTeam(id)));
    }

    @PutMapping("/team/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @PathVariable Long id, @Valid @RequestBody TeamRequest request){
        return ResponseEntity.ok(ApiResponse.ok(teamService.updateTeam(id, request)));
    }

    // 삭제 성공 시 본문 없이 204를 응답한다.
    @DeleteMapping("/team/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id){
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }
}
