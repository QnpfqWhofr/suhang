package hello.dgswesport.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TeamAssignRequest(

        @NotNull(message = "팀 수는 필수입니다.")
        @Min(value = 2, message = "팀 수는 2 이상이어야 합니다.")
        Integer teamCount
) {
}
