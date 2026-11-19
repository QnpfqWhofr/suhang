package hello.dgswesport.controller;

import hello.dgswesport.domain.entity.Tier;
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
import static org.mockito.BDDMockito.willThrow;
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

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamService teamService;

    @Test
    @DisplayName("팀과 선수를 등록하면 201과 생성된 팀을 응답한다")
    void teamRegister() throws Exception {
        // given
        given(teamService.teamRegister(any(TeamRequest.class)))
                .willReturn(new TeamResponse(1L, "알파", List.of(new UserResponse(1L, "2101", "홍길동", null, Tier.GOLD, 1L))));

        // when & then
        mockMvc.perform(post("/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "알파", "users": [{"studentNum": "2101", "name": "홍길동", "tier": "GOLD"}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("알파"))
                .andExpect(jsonPath("$.data.users[0].studentNum").value("2101"))
                .andExpect(jsonPath("$.data.users[0].name").value("홍길동"));
    }

    @Test
    @DisplayName("팀 이름이 비어 있으면 400을 응답하고 서비스를 호출하지 않는다")
    void teamRegisterWithBlankName() throws Exception {
        mockMvc.perform(post("/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": " "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("팀 이름은 필수입니다."));

        verify(teamService, never()).teamRegister(any());
    }

    @Test
    @DisplayName("선수 이름이 비어 있으면 400과 해당 선수의 필드 위치를 응답한다")
    void teamRegisterWithBlankUserName() throws Exception {
        mockMvc.perform(post("/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "알파", "users": [{"studentNum": "2101", "name": ""}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("users[0].name"))
                .andExpect(jsonPath("$.errors[0].message").value("선수 이름은 필수입니다."));

        verify(teamService, never()).teamRegister(any());
    }

    @Test
    @DisplayName("이미 있는 팀 이름으로 등록하면 409를 응답한다")
    void teamRegisterWithDuplicateName() throws Exception {
        // given
        given(teamService.teamRegister(any(TeamRequest.class)))
                .willThrow(new BusinessException(ErrorCode.DUPLICATE_TEAM_NAME));

        // when & then
        mockMvc.perform(post("/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "알파"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_TEAM_NAME"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 팀 이름입니다."));
    }

    @Test
    @DisplayName("팀 목록을 조회하면 200과 팀 목록을 응답한다")
    void getAllTeams() throws Exception {
        // given
        given(teamService.getAllTeams("알")).willReturn(List.of(
                new TeamResponse(1L, "알파", List.of()),
                new TeamResponse(2L, "알고리즘", List.of())));

        // when & then
        mockMvc.perform(get("/team").param("name", "알"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("알파"))
                .andExpect(jsonPath("$.data[1].name").value("알고리즘"));
    }

    @Test
    @DisplayName("팀을 단건 조회하면 200과 팀을 응답한다")
    void getOneTeam() throws Exception {
        // given
        given(teamService.getOneTeam(1L)).willReturn(new TeamResponse(1L, "알파", List.of()));

        // when & then
        mockMvc.perform(get("/team/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("알파"));
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
    @DisplayName("팀을 수정하면 200과 수정된 팀을 응답한다")
    void updateTeam() throws Exception {
        // given
        given(teamService.updateTeam(eq(1L), any(TeamRequest.class)))
                .willReturn(new TeamResponse(1L, "베타", List.of(new UserResponse(2L, "2102", "임꺽정", null, Tier.SILVER, 1L))));

        // when & then
        mockMvc.perform(put("/team/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "베타", "users": [{"studentNum": "2102", "name": "임꺽정", "tier": "SILVER"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("베타"))
                .andExpect(jsonPath("$.data.users[0].name").value("임꺽정"));
    }

    @Test
    @DisplayName("팀을 삭제하면 본문 없이 204를 응답한다")
    void deleteTeam() throws Exception {
        mockMvc.perform(delete("/team/1"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        verify(teamService).deleteTeam(1L);
    }

    @Test
    @DisplayName("없는 팀을 삭제하면 404를 응답한다")
    void deleteTeamNotFound() throws Exception {
        // given
        willThrow(new BusinessException(ErrorCode.TEAM_NOT_FOUND)).given(teamService).deleteTeam(99L);

        // when & then
        mockMvc.perform(delete("/team/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEAM_NOT_FOUND"));
    }
}
