package hello.dgswesport.controller;

import hello.dgswesport.dto.TeamAssignRequest;
import hello.dgswesport.dto.TeamRequest;
import hello.dgswesport.dto.TeamResponse;
import hello.dgswesport.global.response.ApiResponse;
import hello.dgswesport.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    // 팀에 배정되지 않은 참가자를 teamCount개의 팀으로 자동 배정한다.
    @PostMapping("/team")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> teamRegister(@Valid @RequestBody TeamAssignRequest request){
        List<TeamResponse> response = teamService.teamRegister(request);
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

    // 팀 전체를 삭제한다. 성공 시 본문 없이 204를 응답한다.
    @DeleteMapping("/team")
    public ResponseEntity<Void> deleteAllTeams(){
        teamService.deleteAllTeams();
        return ResponseEntity.noContent().build();
    }
}
