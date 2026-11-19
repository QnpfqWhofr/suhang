package com.finefinee.suhang.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** H2 데이터베이스와 MockMvc를 사용해 사용자 API의 요청-응답 흐름을 검증합니다. */
@SpringBootTest
@AutoConfigureMockMvc
class UserApiIntegrationTest {
    /** HTTP 요청을 실제 컨트롤러에 전달하는 테스트 클라이언트입니다. */
    @Autowired
    private MockMvc mockMvc;

    /** 테스트 데이터 정리와 생성된 ID 확인에 사용하는 저장소입니다. */
    @Autowired
    private UserRepository userRepository;

    /** 각 테스트가 독립적으로 실행되도록 기존 사용자를 삭제합니다. */
    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    /** 회원가입, 로그인, 전체 조회, 수정, 삭제 및 삭제 후 404를 검증합니다. */
    @Test
    void userCanRegisterLoginReadUpdateAndDelete() throws Exception {
        String createRequest = """
                {
                  "studentNumber": "20230001",
                  "name": "김민준",
                  "gameNickname": "finefinee",
                  "password": "password123"
                }
                """;
        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentNumber").value("20230001"))
                .andExpect(jsonPath("$.password").doesNotExist());
        long id = userRepository.findByStudentNumber("20230001").orElseThrow().getId();

        mockMvc.perform(post("/users/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"studentNumber":"20230001","password":"password123"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(put("/users/{id}", id)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"김민준","gameNickname":"new-name"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameNickname").value("new-name"));

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    /** 이미 등록된 학번으로 회원가입할 때 409 Conflict를 반환하는지 검증합니다. */
    @Test
    void duplicateStudentNumberIsRejected() throws Exception {
        String request = """
                {
                  "studentNumber": "20230002",
                  "name": "김민준",
                  "gameNickname": "finefinee",
                  "password": "password123"
                }
                """;
        mockMvc.perform(post("/users").contentType(APPLICATION_JSON).content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/users").contentType(APPLICATION_JSON).content(request))
                .andExpect(status().isConflict());
    }
}
