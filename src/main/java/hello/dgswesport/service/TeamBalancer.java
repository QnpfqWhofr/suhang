package hello.dgswesport.service;

import hello.dgswesport.domain.entity.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// 참가자를 팀 간 티어 점수 합이 비슷해지도록 나눈다. DB에 의존하지 않는 순수 계산이다.
public final class TeamBalancer {

    private TeamBalancer() {
    }

    // 점수가 높은 참가자부터, 인원이 가장 적은 팀 중 점수 합이 가장 낮은 팀에 배정한다.
    // 인원이 적은 팀을 먼저 채우므로 팀 간 인원 차이는 최대 1명이다.
    public static List<List<User>> distribute(List<User> users, int teamCount) {
        List<List<User>> groups = new ArrayList<>();
        int[] totals = new int[teamCount];
        for (int i = 0; i < teamCount; i++) {
            groups.add(new ArrayList<>());
        }

        List<User> sorted = users.stream()
                .sorted(Comparator.comparingInt(TeamBalancer::score).reversed())
                .toList();

        for (User user : sorted) {
            int target = 0;
            for (int i = 1; i < teamCount; i++) {
                int sizeDiff = groups.get(i).size() - groups.get(target).size();
                if (sizeDiff < 0 || (sizeDiff == 0 && totals[i] < totals[target])) {
                    target = i;
                }
            }
            groups.get(target).add(user);
            totals[target] += score(user);
        }
        return groups;
    }

    private static int score(User user) {
        return user.getTier().getScore();
    }
}
