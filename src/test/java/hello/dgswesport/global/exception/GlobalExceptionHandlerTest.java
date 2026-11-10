package hello.dgswesport.global.exception;

import hello.dgswesport.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 테스트 전용 컨트롤러로 예외를 발생시켜 공통 응답 형식과 상태코드를 검증한다.
 * controllers를 지정해 다른 도메인 컨트롤러가 이 테스트에 로드되지 않게 한다.
 */
@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandlerTest.TestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("정상 응답은 ApiResponse 형식으로 내려간다")
    void success() throws Exception {
        mockMvc.perform(get("/test/success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("요청이 성공했습니다."))
                .andExpect(jsonPath("$.data").value("pong"));
    }

    @Test
    @DisplayName("BusinessException은 ErrorCode의 상태코드와 코드로 응답한다")
    void businessException() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("팀을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("입력값 검증에 실패하면 400과 필드별 오류를 응답한다")
    void validationFailure() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("이름은 필수입니다."));
    }

    @Test
    @DisplayName("JSON 형식이 잘못되면 400을 공통 형식으로 응답한다")
    void malformedJson() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드는 405를 공통 형식으로 응답한다")
    void methodNotAllowed() throws Exception {
        mockMvc.perform(delete("/test/success"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    @DisplayName("존재하지 않는 경로는 404를 공통 형식으로 응답한다")
    void unknownPath() throws Exception {
        mockMvc.perform(get("/test/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("예상하지 못한 예외는 500으로 응답하고 내부 메시지를 노출하지 않는다")
    void unexpectedException() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/success")
        ApiResponse<String> success() {
            return ApiResponse.ok("pong");
        }

        @GetMapping("/business")
        void business() {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "팀을 찾을 수 없습니다.");
        }

        @PostMapping("/validation")
        ApiResponse<String> validation(@Valid @RequestBody TestRequest request) {
            return ApiResponse.ok(request.name());
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException("노출되면 안 되는 내부 메시지");
        }
    }

    record TestRequest(@NotBlank(message = "이름은 필수입니다.") String name) {
    }
}
