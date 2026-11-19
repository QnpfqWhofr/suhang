package hello.dgswesport.controller;

import hello.dgswesport.dto.MatchResponse;
import hello.dgswesport.dto.MatchSlotResponse;
import hello.dgswesport.global.exception.BusinessException;
import hello.dgswesport.global.exception.ErrorCode;
import hello.dgswesport.service.MatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchController.class)
class MatchControllerTest {

    // 팀 3개의 대진: 1경기는 2팀 대 3팀, 2경기(결승)는 부전승인 1팀 대 1경기 승자
    private static final MatchResponse MATCH_1 = new MatchResponse(1L, 1, 1,
            new MatchSlotResponse(2L, "2팀", null, false),
            new MatchSlotResponse(3L, "3팀", null, false));
    private static final MatchResponse MATCH_2 = new MatchResponse(2L, 2, 2,
            new MatchSlotResponse(1L, "1팀", null, true),
            new MatchSlotResponse(null, null, 1, false));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatchService matchService;

    @Test
    @DisplayName("대진 편성을 요청하면 본문 없이도 201과 생성된 경기 목록을 응답한다")
    void createMatches() throws Exception {
        // given
        given(matchService.createMatches()).willReturn(List.of(MATCH_1, MATCH_2));

        // when & then
        mockMvc.perform(post("/match"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].matchOrder").value(1))
                .andExpect(jsonPath("$.data[0].team1.teamName").value("2팀"))
                .andExpect(jsonPath("$.data[1].round").value(2))
                .andExpect(jsonPath("$.data[1].team1.bye").value(true))
                .andExpect(jsonPath("$.data[1].team2.winnerOfMatchOrder").value(1));
    }

    @Test
    @DisplayName("팀이 2개보다 적으면 400을 응답한다")
    void createMatchesWithNotEnoughTeams() throws Exception {
        // given
        given(matchService.createMatches()).willThrow(new BusinessException(ErrorCode.NOT_ENOUGH_TEAMS));

        // when & then
        mockMvc.perform(post("/match"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOT_ENOUGH_TEAMS"))
                .andExpect(jsonPath("$.message").value("대진을 편성하려면 팀이 2개 이상 필요합니다."));
    }

    @Test
    @DisplayName("이미 대진이 있으면 409를 응답한다")
    void createMatchesWhenMatchesExist() throws Exception {
        // given
        given(matchService.createMatches()).willThrow(new BusinessException(ErrorCode.MATCHES_ALREADY_EXIST));

        // when & then
        mockMvc.perform(post("/match"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MATCHES_ALREADY_EXIST"));
    }

    @Test
    @DisplayName("전체 일정을 조회하면 200과 경기 순서대로 정렬된 목록을 응답한다")
    void getAllMatches() throws Exception {
        // given
        given(matchService.getAllMatches()).willReturn(List.of(MATCH_1, MATCH_2));

        // when & then
        mockMvc.perform(get("/match"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].matchOrder").value(1))
                .andExpect(jsonPath("$.data[1].matchOrder").value(2));
    }

    @Test
    @DisplayName("유저 일정을 조회하면 200과 그 유저 팀의 경기만 응답한다")
    void getMatchesByUser() throws Exception {
        // given
        given(matchService.getMatchesByUser(7L)).willReturn(List.of(MATCH_2));

        // when & then
        mockMvc.perform(get("/match/user/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].team1.teamId").value(1));
    }

    @Test
    @DisplayName("없는 유저의 일정을 조회하면 404를 응답한다")
    void getMatchesByUserNotFound() throws Exception {
        // given
        given(matchService.getMatchesByUser(99L)).willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/match/user/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }
}
