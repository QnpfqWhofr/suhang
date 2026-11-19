package hello.dgswesport.controller;

import hello.dgswesport.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Mock 없이 Controller → Service → Repository → H2까지 실제로 연결해 유저 API의 요청-응답 흐름을 검증한다.
@SpringBootTest
@AutoConfigureMockMvc
class UserApiIntegrationTest {

    private static final String CREATE_REQUEST = """
            {
              "studentNum": "20230001",
              "name": "김민준",
              "gameNickname": "finefinee",
              "tier": "GOLD",
              "password": "password123"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    // 각 테스트가 독립적으로 실행되도록 기존 유저를 삭제한다.
    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입, 로그인, 조회, 수정, 비밀번호 변경, 삭제가 순서대로 동작한다")
    void userCanRegisterLoginReadUpdateAndDelete() throws Exception {
        mockMvc.perform(post("/user").contentType(APPLICATION_JSON).content(CREATE_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.studentNum").value("20230001"))
                .andExpect(jsonPath("$.data.tier").value("GOLD"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
        long id = userRepository.findByStudentNum("20230001").orElseThrow().getId();

        mockMvc.perform(post("/user/login").contentType(APPLICATION_JSON).content(loginRequest("password123")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        // 비밀번호를 보내지 않으면 프로필만 바뀌고 기존 비밀번호로 계속 로그인된다.
        mockMvc.perform(put("/user/{id}", id).contentType(APPLICATION_JSON).content("""
                        {"name": "김민준", "gameNickname": "new-name", "tier": "DIAMOND"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gameNickname").value("new-name"))
                .andExpect(jsonPath("$.data.tier").value("DIAMOND"));
        mockMvc.perform(post("/user/login").contentType(APPLICATION_JSON).content(loginRequest("password123")))
                .andExpect(status().isOk());

        // 비밀번호를 보내면 새 비밀번호로만 로그인된다.
        mockMvc.perform(put("/user/{id}", id).contentType(APPLICATION_JSON).content("""
                        {"name": "김민준", "gameNickname": "new-name", "tier": "DIAMOND", "password": "newpassword456"}
                        """))
                .andExpect(status().isOk());
        mockMvc.perform(post("/user/login").contentType(APPLICATION_JSON).content(loginRequest("newpassword456")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/user/login").contentType(APPLICATION_JSON).content(loginRequest("password123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"));

        mockMvc.perform(delete("/user/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/user/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("이미 등록된 학번으로 회원가입하면 409를 응답한다")
    void duplicateStudentNumberIsRejected() throws Exception {
        mockMvc.perform(post("/user").contentType(APPLICATION_JSON).content(CREATE_REQUEST))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/user").contentType(APPLICATION_JSON).content(CREATE_REQUEST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_STUDENT_NUM"));
    }

    @Test
    @DisplayName("8자 미만의 새 비밀번호로 수정하면 400을 응답하고 기존 비밀번호가 유지된다")
    void shortNewPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/user").contentType(APPLICATION_JSON).content(CREATE_REQUEST))
                .andExpect(status().isCreated());
        long id = userRepository.findByStudentNum("20230001").orElseThrow().getId();

        mockMvc.perform(put("/user/{id}", id).contentType(APPLICATION_JSON).content("""
                        {"name": "김민준", "gameNickname": "finefinee", "tier": "GOLD", "password": "short"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상이어야 합니다."));

        mockMvc.perform(post("/user/login").contentType(APPLICATION_JSON).content(loginRequest("password123")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("8자 미만의 비밀번호로 회원가입하면 400과 필드 오류를 응답한다")
    void shortPasswordIsRejectedOnRegister() throws Exception {
        mockMvc.perform(post("/user").contentType(APPLICATION_JSON).content("""
                        {"studentNum": "20230002", "name": "김민준", "gameNickname": "finefinee",
                         "tier": "GOLD", "password": "short"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("password"));
    }

    private String loginRequest(String password) {
        return """
                {"studentNum": "20230001", "password": "%s"}
                """.formatted(password);
    }
}
