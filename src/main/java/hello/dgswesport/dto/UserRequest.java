package hello.dgswesport.dto;

import jakarta.validation.constraints.NotBlank;

public record UserRequest(

        @NotBlank(message = "학번은 필수입니다.")
        String studentNum,

        @NotBlank(message = "선수 이름은 필수입니다.")
        String name,

        String tier
) {
}
