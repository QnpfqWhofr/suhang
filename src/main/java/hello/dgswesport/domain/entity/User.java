package hello.dgswesport.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인에 쓰는 값이라 중복을 허용하지 않는다.
    @Column(name = "student_num", nullable = false, unique = true)
    private String studentNum;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String gameNickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tier tier;

    // 암호화된 값만 저장한다. 로그에 출력되지 않도록 toString에서 제외한다.
    @ToString.Exclude
    @Column(nullable = false)
    private String password;

    // 팀 배정 전에는 null이다.
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
}
