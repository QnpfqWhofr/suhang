package hello.dgswesport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "학번은 필수입니다.")
        @Size(max = 30, message = "학번은 30자 이하여야 합니다.")
        String studentNum,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(max = 100, message = "비밀번호는 100자 이하여야 합니다.")
        String password
) {
}
