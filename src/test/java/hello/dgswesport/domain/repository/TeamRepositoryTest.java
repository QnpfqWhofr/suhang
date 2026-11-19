package hello.dgswesport.domain.repository;

import hello.dgswesport.domain.entity.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TeamRepositoryTest {

    @Autowired
    private TeamRepository teamRepository;

    @Test
    @DisplayName("이름으로 팀 존재 여부를 확인한다")
    void existsByName() {
        // given
        teamRepository.save(team("알파"));

        // when & then
        assertThat(teamRepository.existsByName("알파")).isTrue();
        assertThat(teamRepository.existsByName("베타")).isFalse();
    }

    @Test
    @DisplayName("자기 자신을 제외하고 같은 이름의 팀이 있는지 확인한다")
    void existsByNameAndIdNot() {
        // given
        Team alpha = teamRepository.save(team("알파"));
        Team beta = teamRepository.save(team("베타"));

        // when & then
        assertThat(teamRepository.existsByNameAndIdNot("알파", alpha.getId())).isFalse();
        assertThat(teamRepository.existsByNameAndIdNot("알파", beta.getId())).isTrue();
    }

    @Test
    @DisplayName("이름에 검색어가 포함된 팀을 이름순으로 조회한다")
    void findByNameContainingOrderByName() {
        // given
        teamRepository.save(team("코드 브레이커"));
        teamRepository.save(team("알고리즘 코드"));
        teamRepository.save(team("세그폴트"));

        // when
        List<Team> teams = teamRepository.findByNameContainingOrderByName("코드");

        // then
        assertThat(teams).extracting(Team::getName).containsExactly("알고리즘 코드", "코드 브레이커");
    }

    @Test
    @DisplayName("검색어와 일치하는 팀이 없으면 빈 목록을 반환한다")
    void findByNameContainingNoMatch() {
        // given
        teamRepository.save(team("알파"));

        // when
        List<Team> teams = teamRepository.findByNameContainingOrderByName("없는이름");

        // then
        assertThat(teams).isEmpty();
    }

    private Team team(String name) {
        return new Team(name, new ArrayList<>());
    }
}
