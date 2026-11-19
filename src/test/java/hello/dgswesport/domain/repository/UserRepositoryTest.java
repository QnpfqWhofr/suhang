package hello.dgswesport.domain.repository;

import hello.dgswesport.domain.entity.Team;
import hello.dgswesport.domain.entity.Tier;
import hello.dgswesport.domain.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("학번으로 유저 존재 여부를 확인한다")
    void existsByStudentNum() {
        // given
        userRepository.save(user("2101", null));

        // when & then
        assertThat(userRepository.existsByStudentNum("2101")).isTrue();
        assertThat(userRepository.existsByStudentNum("9999")).isFalse();
    }

    @Test
    @DisplayName("학번으로 유저를 조회하고 없으면 빈 값을 반환한다")
    void findByStudentNum() {
        // given
        userRepository.save(user("2101", null));

        // when & then
        assertThat(userRepository.findByStudentNum("2101"))
                .get()
                .extracting(User::getTier)
                .isEqualTo(Tier.GOLD);
        assertThat(userRepository.findByStudentNum("9999")).isEmpty();
    }

    @Test
    @DisplayName("같은 학번의 유저를 저장하면 유니크 제약조건 위반 예외가 발생한다")
    void saveDuplicateStudentNum() {
        // given
        userRepository.saveAndFlush(user("2101", null));

        // when & then
        assertThatThrownBy(() -> userRepository.saveAndFlush(user("2101", null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("팀에 배정되지 않은 유저만 조회한다")
    void findByTeamIsNullOrderById() {
        // given
        Team team = teamRepository.save(new Team("1팀", new ArrayList<>()));
        userRepository.save(user("2101", team));
        userRepository.save(user("2102", null));

        // when & then
        assertThat(userRepository.findByTeamIsNullOrderById())
                .extracting(User::getStudentNum)
                .containsExactly("2102");
    }

    @Test
    @DisplayName("벌크 쿼리로 모든 유저의 소속 팀을 해제하고 변경 건수를 반환한다")
    void clearAllTeams() {
        // given
        Team team = teamRepository.save(new Team("1팀", new ArrayList<>()));
        userRepository.save(user("2101", team));
        userRepository.save(user("2102", team));
        userRepository.save(user("2103", null));
        entityManager.flush();

        // when
        int updated = userRepository.clearAllTeams();

        // then
        assertThat(updated).isEqualTo(2);
        assertThat(userRepository.findByTeamIsNullOrderById()).hasSize(3);
    }

    private User user(String studentNum, Team team) {
        return User.builder()
                .studentNum(studentNum)
                .name("홍길동")
                .gameNickname("Hong")
                .tier(Tier.GOLD)
                .password("ENCODED")
                .team(team)
                .build();
    }
}
