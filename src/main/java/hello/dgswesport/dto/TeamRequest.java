package hello.dgswesport.dto;

import hello.dgswesport.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamRequest {
    private String name;
    private List<User> users;
}