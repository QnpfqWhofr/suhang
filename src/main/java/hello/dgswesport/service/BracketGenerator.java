package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Competition;
import hello.dgswesport.domain.entity.Match;
import hello.dgswesport.domain.entity.Team;

import java.util.ArrayList;
import java.util.List;

// 팀 목록으로 싱글 엘리미네이션 토너먼트 대진을 만든다. DB에 의존하지 않는 순수 계산이다.
public final class BracketGenerator {

    private BracketGenerator() {
    }

    // 대진의 한 자리. 팀이 정해졌거나(team), 앞선 경기의 승자를 기다린다(sourceOrder).
    private record Slot(Team team, Integer sourceOrder) {
    }

    // teams의 앞쪽일수록 높은 시드다. 결승까지의 모든 경기를 경기 순서대로 반환한다.
    // 팀 수가 2의 거듭제곱이 아니면 모자란 수만큼 상위 시드가 1라운드를 부전승으로 건너뛴다.
    // 각 라운드는 남은 자리의 양 끝을 짝지어, 상위 시드끼리는 늦게 만나게 한다.
    public static List<Match> generate(Competition competition, List<Team> teams) {
        List<Slot> slots = new ArrayList<>();
        for (Team team : teams) {
            slots.add(new Slot(team, null));
        }

        List<Match> matches = new ArrayList<>();
        int order = 1;
        int round = 1;
        while (slots.size() > 1) {
            // 현재 자리 수 이상인 가장 작은 2의 거듭제곱. 모자란 만큼이 부전승이다.
            int bracketSize = Integer.highestOneBit(slots.size() - 1) * 2;
            int byeCount = bracketSize - slots.size();

            List<Slot> nextSlots = new ArrayList<>(slots.subList(0, byeCount));
            List<Slot> playing = slots.subList(byeCount, slots.size());
            for (int i = 0; i < playing.size() / 2; i++) {
                Slot first = playing.get(i);
                Slot second = playing.get(playing.size() - 1 - i);
                matches.add(new Match(order, round, competition,
                        first.team(), first.sourceOrder(), second.team(), second.sourceOrder()));
                nextSlots.add(new Slot(null, order));
                order++;
            }
            slots = nextSlots;
            round++;
        }
        return matches;
    }
}
