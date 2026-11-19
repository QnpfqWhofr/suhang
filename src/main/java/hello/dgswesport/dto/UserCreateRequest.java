package hello.dgswesport.dto;

import hello.dgswesport.domain.entity.Tier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(

        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "학번은 필수입니다.")
        String studentNum,

        @NotBlank(message = "게임 닉네임은 필수입니다.")
        String gameNickname,

        @NotNull(message = "티어는 필수입니다.")
        Tier tier,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 4, max = 50, message = "비밀번호는 4자 이상 50자 이하여야 합니다.")
        String password
) {
}
