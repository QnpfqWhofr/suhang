package hello.dgswesport.domain.entity;

import hello.dgswesport.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 대회 전체에서의 경기 순서. 1경기부터 시작한다.
    @Column(name = "MATCH_ORDER")
    private Integer matchOrder;

    // 토너먼트 라운드. 1라운드부터 시작하고 마지막 라운드가 결승이다.
    @Column(name = "ROUND_NUMBER")
    private Integer roundNumber;

    @ManyToOne
    @JoinColumn(name = "COMPETITION_ID")
    private Competition competition;

    // 대진의 두 자리. 앞선 경기의 승자를 기다리는 자리는 팀이 null이다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM1_ID")
    private Team team1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM2_ID")
    private Team team2;

    // 팀이 정해지지 않은 자리에 올라올 승자의 경기 순서. 팀이 처음부터 정해진 자리는 null이다.
    @Column(name = "TEAM1_SOURCE_ORDER")
    private Integer team1SourceOrder;

    @Column(name = "TEAM2_SOURCE_ORDER")
    private Integer team2SourceOrder;

    public Match(Integer matchOrder, Competition competition) {
        this.matchOrder = matchOrder;
        this.competition = competition;
    }

    public Match(Integer matchOrder, Integer roundNumber, Competition competition,
                 Team team1, Integer team1SourceOrder, Team team2, Integer team2SourceOrder) {
        this.matchOrder = matchOrder;
        this.roundNumber = roundNumber;
        this.competition = competition;
        this.team1 = team1;
        this.team1SourceOrder = team1SourceOrder;
        this.team2 = team2;
        this.team2SourceOrder = team2SourceOrder;
    }
}
