package hello.dgswesport.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public Team(String name, List<User> users) {
        this.name = name;
        this.users = users;
    }

    // 유저는 팀과 독립적으로 등록·삭제되므로 cascade를 걸지 않는다.
    @OneToMany(mappedBy = "team")
    @BatchSize(size = 10)
    @Builder.Default
    private List<User> users = new ArrayList<>();
}