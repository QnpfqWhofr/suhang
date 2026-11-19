package hello.dgswesport.service;

import hello.dgswesport.dto.MatchResponse;

import java.util.List;

public interface MatchService {

    // 생성된 팀으로 토너먼트 대진을 편성하고 결승까지의 경기 순서를 배정한다.
    // 팀 수가 2의 거듭제곱이 아니면 팀 번호가 빠른 팀부터 부전승으로 다음 라운드에 진출한다.
    List<MatchResponse> createMatches();

    // 전체 일정을 경기 순서대로 조회한다. 대진이 없으면 빈 목록이다.
    List<MatchResponse> getAllMatches();

    // 유저가 소속된 팀이 배정된 경기만 경기 순서대로 조회한다. 팀 미배정 유저는 빈 목록이다.
    List<MatchResponse> getMatchesByUser(Long userId);
}
