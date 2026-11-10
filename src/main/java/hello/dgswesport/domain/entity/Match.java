package hello.dgswesport.domain.entity;

import hello.dgswesport.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "MATCH_ORDER")
    private Integer matchOrder;

    @ManyToOne
    @JoinColumn(name = "COMPETITION_ID")
    private Competition competition;

    public Match(Integer matchOrder, Competition competition) {
        this.matchOrder = matchOrder;
        this.competition = competition;
    }

}
