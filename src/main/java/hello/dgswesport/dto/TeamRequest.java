package hello.dgswesport.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamRequest {

    @NotBlank(message = "팀 이름은 필수입니다.")
    @Size(max = 255, message = "팀 이름은 255자 이하여야 합니다.")
    private String name;

    // 엔티티 대신 DTO로 받는다. 생략하면 선수 없이 팀만 등록한다.
    @Valid
    private List<UserRequest> users;
}
