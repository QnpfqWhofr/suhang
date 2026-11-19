package hello.dgswesport.dto;

import hello.dgswesport.domain.entity.Tier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserUpdateRequest(

        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "게임 닉네임은 필수입니다.")
        String gameNickname,

        @NotNull(message = "티어는 필수입니다.")
        Tier tier
) {
}
