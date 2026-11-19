package hello.dgswesport.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 게임 티어. score는 팀 실력 균형을 계산할 때 쓰는 점수다.
@Getter
@RequiredArgsConstructor
public enum Tier {

    IRON(1),
    BRONZE(2),
    SILVER(3),
    GOLD(4),
    PLATINUM(5),
    EMERALD(6),
    DIAMOND(7),
    MASTER(8),
    GRANDMASTER(9),
    CHALLENGER(10);

    private final int score;
}
