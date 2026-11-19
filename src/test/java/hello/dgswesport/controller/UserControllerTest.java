package hello.dgswesport.controller;

import hello.dgswesport.domain.entity.Tier;
import hello.dgswesport.dto.LoginRequest;
import hello.dgswesport.dto.UserCreateRequest;
import hello.dgswesport.dto.UserResponse;
import hello.dgswesport.dto.UserUpdateRequest;
import hello.dgswesport.global.exception.BusinessException;
import hello.dgswesport.global.exception.ErrorCode;
import hello.dgswesport.service.UserService;
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

@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final UserResponse HONG = new UserResponse(1L, "2101", "홍길동", "Hong", Tier.GOLD, null);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("유저를 등록하면 201과 생성된 유저를 응답하고 비밀번호는 응답에 없다")
    void createUser() throws Exception {
        // given
        given(userService.createUser(any(UserCreateRequest.class))).willReturn(HONG);

        // when & then
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "홍길동", "studentNum": "2101", "gameNickname": "Hong",
                                 "tier": "GOLD", "password": "pass1234"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.studentNum").value("2101"))
                .andExpect(jsonPath("$.data.tier").value("GOLD"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("필수 값이 비어 있으면 400을 응답하고 서비스를 호출하지 않는다")
    void createUserWithBlankName() throws Exception {
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "studentNum": "2101", "gameNickname": "Hong",
                                 "tier": "GOLD", "password": "pass1234"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("이름은 필수입니다."));

        verify(userService, never()).createUser(any());
    }

    @Test
    @DisplayName("존재하지 않는 티어를 보내면 400을 응답한다")
    void createUserWithUnknownTier() throws Exception {
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "홍길동", "studentNum": "2101", "gameNickname": "Hong",
                                 "tier": "없는티어", "password": "pass1234"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(userService, never()).createUser(any());
    }

    @Test
    @DisplayName("이미 등록된 학번으로 등록하면 409를 응답한다")
    void createUserWithDuplicateStudentNum() throws Exception {
        // given
        given(userService.createUser(any(UserCreateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.DUPLICATE_STUDENT_NUM));

        // when & then
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "홍길동", "studentNum": "2101", "gameNickname": "Hong",
                                 "tier": "GOLD", "password": "pass1234"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_STUDENT_NUM"));
    }

    @Test
    @DisplayName("로그인에 성공하면 200과 유저 정보를 응답한다")
    void login() throws Exception {
        // given
        given(userService.login(any(LoginRequest.class))).willReturn(HONG);

        // when & then
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"studentNum": "2101", "password": "pass1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("홍길동"));
    }

    @Test
    @DisplayName("학번이나 비밀번호가 틀리면 401을 응답한다")
    void loginFailed() throws Exception {
        // given
        given(userService.login(any(LoginRequest.class)))
                .willThrow(new BusinessException(ErrorCode.LOGIN_FAILED));

        // when & then
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"studentNum": "2101", "password": "wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"))
                .andExpect(jsonPath("$.message").value("학번 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("유저 목록을 조회하면 200과 유저 목록을 응답한다")
    void getAllUsers() throws Exception {
        // given
        given(userService.getAllUsers()).willReturn(List.of(
                HONG, new UserResponse(2L, "2102", "임꺽정", "Lim", Tier.SILVER, 3L)));

        // when & then
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[1].teamId").value(3));
    }

    @Test
    @DisplayName("없는 유저를 조회하면 404를 응답한다")
    void getOneUserNotFound() throws Exception {
        // given
        given(userService.getOneUser(99L)).willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/user/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("유저를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("유저를 수정하면 200과 수정된 유저를 응답한다")
    void updateUser() throws Exception {
        // given
        given(userService.updateUser(eq(1L), any(UserUpdateRequest.class)))
                .willReturn(new UserResponse(1L, "2101", "홍길동", "NewHong", Tier.DIAMOND, null));

        // when & then
        mockMvc.perform(put("/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "홍길동", "gameNickname": "NewHong", "tier": "DIAMOND"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gameNickname").value("NewHong"))
                .andExpect(jsonPath("$.data.tier").value("DIAMOND"));
    }

    @Test
    @DisplayName("유저를 삭제하면 본문 없이 204를 응답한다")
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/user/1"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("없는 유저를 삭제하면 404를 응답한다")
    void deleteUserNotFound() throws Exception {
        // given
        willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND)).given(userService).deleteUser(99L);

        // when & then
        mockMvc.perform(delete("/user/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }
}
