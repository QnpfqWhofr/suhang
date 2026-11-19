package hello.dgswesport.dto;

import hello.dgswesport.domain.entity.Tier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
        String name,

        @NotBlank(message = "게임 닉네임은 필수입니다.")
        @Size(max = 50, message = "게임 닉네임은 50자 이하여야 합니다.")
        String gameNickname,

        @NotNull(message = "티어는 필수입니다.")
        Tier tier,

        // 선택 항목. 비어 있으면 비밀번호를 바꾸지 않는다. 최소 길이는 Service에서 확인한다.
        @Size(max = 100, message = "비밀번호는 100자 이하여야 합니다.")
        String password
) {
}
