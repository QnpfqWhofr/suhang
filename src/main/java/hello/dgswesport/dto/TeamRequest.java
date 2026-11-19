package hello.dgswesport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamRequest {

    @NotBlank(message = "팀 이름은 필수입니다.")
    @Size(max = 255, message = "팀 이름은 255자 이하여야 합니다.")
    private String name;
}
