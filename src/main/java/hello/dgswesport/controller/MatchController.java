package hello.dgswesport.controller;

import hello.dgswesport.dto.MatchResponse;
import hello.dgswesport.global.response.ApiResponse;
import hello.dgswesport.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    // 생성된 팀으로 토너먼트 대진을 편성한다. 요청 본문은 받지 않는다.
    @PostMapping("/match")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> createMatches() {
        List<MatchResponse> response = matchService.createMatches();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping("/match")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getAllMatches() {
        return ResponseEntity.ok(ApiResponse.ok(matchService.getAllMatches()));
    }

    @GetMapping("/match/user/{userId}")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getMatchesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(matchService.getMatchesByUser(userId)));
    }
}
