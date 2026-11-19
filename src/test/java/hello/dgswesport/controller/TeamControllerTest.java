package hello.dgswesport.controller;

import hello.dgswesport.domain.entity.Tier;
import hello.dgswesport.dto.TeamAssignRequest;
import hello.dgswesport.dto.TeamRequest;
import hello.dgswesport.dto.TeamResponse;
import hello.dgswesport.dto.UserResponse;
import hello.dgswesport.global.exception.BusinessException;
import hello.dgswesport.global.exception.ErrorCode;
import hello.dgswesport.service.TeamService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeamController.class)
class TeamControllerTest {

    private static final TeamResponse TEAM_1 = new TeamResponse(1L, "1팀", 5.5, List.of(
            new UserResponse(1L, "3101", "김하늘", "SkyKim", Tier.DIAMOND, 1L),
            new UserResponse(2L, "2103", "박서준", "SeoJunP", Tier.GOLD, 1L)));
    private static final TeamResponse TEAM_2 = new TeamResponse(2L, "2팀", 5.0, List.of(
            new UserResponse(3L, "3102", "이도윤", "DoYoon2", Tier.PLATINUM, 2L)));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamService teamService;

    @Test
    @DisplayName("팀 자동 배정을 요청하면 201과 생성된 팀 목록을 응답한다")
    void teamRegister() throws Exception {
        // given
        given(teamService.teamRegister(any(TeamAssignRequest.class))).willReturn(List.of(TEAM_1, TEAM_2));

        // when & then
        mockMvc.perform(post("/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"teamCount": 2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("1팀"))
                .andExpect(jsonPath("$.data[0].averageScore").value(5.5))
                .andExpect(jsonPath("$.data[0].users[0].name").value("김하늘"));
    }

    @Test
    @DisplayName("팀 수가 2보다 작으면 400을 응답하고 서비스를 호출하지 않는다")
    void teamRegisterWithTooFewTeams() throws Exception {
        mockMvc.perform(post("/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"teamCount": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("teamCount"))
                .andExpect(jsonPath("$.errors[0].message").value("팀 수는 2 이상이어야 합니다."));

        verify(teamService, never()).teamRegister(any());
    }

    @Test
    @DisplayName("이미 팀이 있으면 409를 응답한다")
    void teamRegisterWhenTeamsExist() throws Exception {
        // given
        given(teamService.teamRegister(any(TeamAssignRequest.class)))
                .willThrow(new BusinessException(ErrorCode.TEAMS_ALREADY_EXIST));

        // when & then
        mockMvc.perform(post("/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"teamCount": 2}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TEAMS_ALREADY_EXIST"));
    }

    @Test
    @DisplayName("참가자가 팀 수보다 적으면 400을 응답한다")
    void teamRegisterWithNotEnoughUsers() throws Exception {
        // given
        given(teamService.teamRegister(any(TeamAssignRequest.class)))
                .willThrow(new BusinessException(ErrorCode.NOT_ENOUGH_USERS));

        // when & then
        mockMvc.perform(post("/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"teamCount": 8}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOT_ENOUGH_USERS"));
    }

    @Test
    @DisplayName("팀 목록을 조회하면 200과 팀별 평균 실력 점수를 응답한다")
    void getAllTeams() throws Exception {
        // given
        given(teamService.getAllTeams(null)).willReturn(List.of(TEAM_1, TEAM_2));

        // when & then
        mockMvc.perform(get("/team"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].averageScore").value(5.5))
                .andExpect(jsonPath("$.data[1].averageScore").value(5.0));
    }

    @Test
    @DisplayName("팀을 단건 조회하면 200과 소속 팀원 목록을 응답한다")
    void getOneTeam() throws Exception {
        // given
        given(teamService.getOneTeam(1L)).willReturn(TEAM_1);

        // when & then
        mockMvc.perform(get("/team/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.users.length()").value(2))
                .andExpect(jsonPath("$.data.users[1].tier").value("GOLD"));
    }

    @Test
    @DisplayName("없는 팀을 조회하면 404를 응답한다")
    void getOneTeamNotFound() throws Exception {
        // given
        given(teamService.getOneTeam(99L)).willThrow(new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/team/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEAM_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("팀을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("팀 이름을 수정하면 200과 수정된 팀을 응답한다")
    void updateTeam() throws Exception {
        // given
        given(teamService.updateTeam(eq(1L), any(TeamRequest.class)))
                .willReturn(new TeamResponse(1L, "알파", 5.5, List.of()));

        // when & then
        mockMvc.perform(put("/team/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "알파"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("알파"));
    }

    @Test
    @DisplayName("다른 팀이 쓰는 이름으로 수정하면 409를 응답한다")
    void updateTeamWithDuplicateName() throws Exception {
        // given
        given(teamService.updateTeam(eq(1L), any(TeamRequest.class)))
                .willThrow(new BusinessException(ErrorCode.DUPLICATE_TEAM_NAME));

        // when & then
        mockMvc.perform(put("/team/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "2팀"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_TEAM_NAME"));
    }

    @Test
    @DisplayName("팀 전체를 삭제하면 본문 없이 204를 응답한다")
    void deleteAllTeams() throws Exception {
        mockMvc.perform(delete("/team"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        verify(teamService).deleteAllTeams();
    }
}
