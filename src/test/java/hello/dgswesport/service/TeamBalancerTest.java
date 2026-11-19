package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Tier;
import hello.dgswesport.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeamBalancerTest {

    @Test
    @DisplayName("티어 점수 합이 팀 간에 같아지도록 배정한다")
    void distributeBalancesTotalScore() {
        // given: 점수 10, 9, 8, 7, 6, 5, 4, 3 (합 52)
        List<User> users = users(Tier.CHALLENGER, Tier.GRANDMASTER, Tier.MASTER, Tier.DIAMOND,
                Tier.EMERALD, Tier.PLATINUM, Tier.GOLD, Tier.SILVER);

        // when
        List<List<User>> groups = TeamBalancer.distribute(users, 2);

        // then
        assertThat(groups).hasSize(2);
        assertThat(totalScore(groups.get(0))).isEqualTo(26);
        assertThat(totalScore(groups.get(1))).isEqualTo(26);
    }

    @Test
    @DisplayName("입력 순서와 관계없이 점수가 높은 참가자부터 배정한다")
    void distributeIgnoresInputOrder() {
        // given: 낮은 점수부터 들어와도 결과의 균형은 같아야 한다
        List<User> users = users(Tier.SILVER, Tier.GOLD, Tier.PLATINUM, Tier.EMERALD,
                Tier.DIAMOND, Tier.MASTER, Tier.GRANDMASTER, Tier.CHALLENGER);

        // when
        List<List<User>> groups = TeamBalancer.distribute(users, 2);

        // then
        assertThat(totalScore(groups.get(0))).isEqualTo(totalScore(groups.get(1)));
    }

    @Test
    @DisplayName("인원이 팀 수로 나누어떨어지지 않으면 팀 간 인원 차이는 1명 이하다")
    void distributeKeepsTeamSizesWithinOne() {
        // given: 7명을 3팀으로
        List<User> users = users(Tier.CHALLENGER, Tier.DIAMOND, Tier.DIAMOND, Tier.GOLD,
                Tier.GOLD, Tier.SILVER, Tier.IRON);

        // when
        List<List<User>> groups = TeamBalancer.distribute(users, 3);

        // then
        assertThat(groups).extracting(List::size).containsExactlyInAnyOrder(3, 2, 2);
        assertThat(groups.stream().mapToInt(List::size).sum()).isEqualTo(7);
    }

    @Test
    @DisplayName("참가자가 없으면 빈 팀만 반환한다")
    void distributeWithNoUsers() {
        // when
        List<List<User>> groups = TeamBalancer.distribute(List.of(), 2);

        // then
        assertThat(groups).hasSize(2).allSatisfy(group -> assertThat(group).isEmpty());
    }

    private List<User> users(Tier... tiers) {
        return Arrays.stream(tiers)
                .map(tier -> User.builder().tier(tier).build())
                .toList();
    }

    private int totalScore(List<User> users) {
        return users.stream().mapToInt(user -> user.getTier().getScore()).sum();
    }
}
