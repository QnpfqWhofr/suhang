package hello.dgswesport.service;

import hello.dgswesport.domain.entity.User;
import hello.dgswesport.domain.repository.UserRepository;
import hello.dgswesport.dto.LoginRequest;
import hello.dgswesport.dto.UserCreateRequest;
import hello.dgswesport.dto.UserResponse;
import hello.dgswesport.dto.UserUpdateRequest;
import hello.dgswesport.global.exception.BusinessException;
import hello.dgswesport.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    // 회원가입 DTO의 @Size(min = 8)과 같은 기준이다.
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByStudentNum(request.studentNum())) {
            throw new BusinessException(ErrorCode.DUPLICATE_STUDENT_NUM);
        }
        // 원본 비밀번호는 저장하지 않고 암호화한 값만 저장한다.
        User user = User.builder()
                .studentNum(request.studentNum())
                .name(request.name())
                .gameNickname(request.gameNickname())
                .tier(request.tier())
                .password(passwordEncoder.encode(request.password()))
                .build();
        userRepository.save(user);
        return UserResponse.from(user);
    }

    @Override
    public UserResponse login(LoginRequest request) {
        // 학번이 없는 경우와 비밀번호가 틀린 경우를 같은 오류로 응답해 가입 여부가 드러나지 않게 한다.
        User user = userRepository.findByStudentNum(request.studentNum())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        return UserResponse.from(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll(Sort.by("id")).stream()
                .map(UserResponse::from)
                .toList();
    }

    @Override
    public UserResponse getOneUser(Long id) {
        return UserResponse.from(findUser(id));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findUser(id);
        // 변경 감지로 트랜잭션 종료 시점에 UPDATE가 실행된다.
        user.updateProfile(request.name(), request.gameNickname(), request.tier());
        // 비밀번호는 값이 들어온 경우에만 바꾼다. 비어 있으면 변경 요청이 아닌 것으로 본다.
        if (StringUtils.hasText(request.password())) {
            if (request.password().length() < MIN_PASSWORD_LENGTH) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "비밀번호는 8자 이상이어야 합니다.");
            }
            user.updatePassword(passwordEncoder.encode(request.password()));
        }
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.delete(findUser(id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
