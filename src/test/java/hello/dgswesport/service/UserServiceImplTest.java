package hello.dgswesport.service;

import hello.dgswesport.domain.entity.Tier;
import hello.dgswesport.domain.entity.User;
import hello.dgswesport.domain.repository.UserRepository;
import hello.dgswesport.dto.LoginRequest;
import hello.dgswesport.dto.UserCreateRequest;
import hello.dgswesport.dto.UserResponse;
import hello.dgswesport.dto.UserUpdateRequest;
import hello.dgswesport.global.exception.BusinessException;
import hello.dgswesport.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// DB 없이 Repository와 PasswordEncoder를 Mock으로 대체해 Service의 비즈니스 규칙만 검증한다.
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("유저를 등록하면 비밀번호를 암호화해 저장한다")
    void createUser() {
        // given
        given(userRepository.existsByStudentNum("2101")).willReturn(false);
        given(passwordEncoder.encode("pass1234")).willReturn("ENCODED");
        UserCreateRequest request = new UserCreateRequest("홍길동", "2101", "Hong", Tier.GOLD, "pass1234");

        // when
        UserResponse response = userService.createUser(request);

        // then
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPassword()).isEqualTo("ENCODED");
        assertThat(response.studentNum()).isEqualTo("2101");
        assertThat(response.tier()).isEqualTo(Tier.GOLD);
        assertThat(response.teamId()).isNull();
    }

    @Test
    @DisplayName("이미 등록된 학번이면 예외가 발생하고 저장하지 않는다")
    void createUserWithDuplicateStudentNum() {
        // given
        given(userRepository.existsByStudentNum("2101")).willReturn(true);
        UserCreateRequest request = new UserCreateRequest("홍길동", "2101", "Hong", Tier.GOLD, "pass1234");

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_STUDENT_NUM);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("학번과 비밀번호가 일치하면 로그인에 성공한다")
    void login() {
        // given
        given(userRepository.findByStudentNum("2101")).willReturn(Optional.of(user()));
        given(passwordEncoder.matches("pass1234", "ENCODED")).willReturn(true);

        // when
        UserResponse response = userService.login(new LoginRequest("2101", "pass1234"));

        // then
        assertThat(response.name()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인에 실패한다")
    void loginWithWrongPassword() {
        // given
        given(userRepository.findByStudentNum("2101")).willReturn(Optional.of(user()));
        given(passwordEncoder.matches("wrong", "ENCODED")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(new LoginRequest("2101", "wrong")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOGIN_FAILED);
    }

    @Test
    @DisplayName("등록되지 않은 학번이면 비밀번호가 틀린 경우와 같은 오류로 실패한다")
    void loginWithUnknownStudentNum() {
        // given
        given(userRepository.findByStudentNum("9999")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(new LoginRequest("9999", "pass1234")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOGIN_FAILED);
    }

    @Test
    @DisplayName("없는 유저를 조회하면 예외가 발생한다")
    void getOneUserNotFound() {
        // given
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getOneUser(99L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("유저를 수정하면 이름, 게임 닉네임, 티어가 변경되고 비밀번호는 그대로다")
    void updateUser() {
        // given
        User user = user();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.updateUser(1L, new UserUpdateRequest("홍길순", "NewHong", Tier.DIAMOND, null));

        // then
        assertThat(response.name()).isEqualTo("홍길순");
        assertThat(user.getGameNickname()).isEqualTo("NewHong");
        assertThat(user.getTier()).isEqualTo(Tier.DIAMOND);
        assertThat(user.getPassword()).isEqualTo("ENCODED");
    }

    @Test
    @DisplayName("없는 유저를 삭제하면 예외가 발생한다")
    void deleteUserNotFound() {
        // given
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(userRepository, never()).delete(any());
    }

    private User user() {
        return User.builder()
                .studentNum("2101")
                .name("홍길동")
                .gameNickname("Hong")
                .tier(Tier.GOLD)
                .password("ENCODED")
                .build();
    }
}
