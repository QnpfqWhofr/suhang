package com.finefinee.suhang.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.finefinee.suhang.user.UserDtos.*;

/** 사용자 기능의 검증과 비즈니스 로직을 담당합니다. */
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    /** 저장소와 비밀번호 해시 도구를 주입받습니다. */
    public UserService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    /** 학번 중복 여부를 검사하고 비밀번호를 해시해 회원을 등록합니다. */
    public Response create(CreateRequest request) {
        if (userRepository.existsByStudentNumber(request.studentNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 학번입니다.");
        }

        // 사용자별 salt를 적용해 원본 비밀번호는 저장하지 않습니다.
        String salt = passwordHasher.newSalt();
        String hash = passwordHasher.hash(request.password().toCharArray(), salt);
        User user = new User(request.studentNumber(), request.name(), request.gameNickname(), salt, hash);
        return Response.from(userRepository.save(user));
    }

    /** 학번과 비밀번호를 확인하고 공개 사용자 정보를 반환합니다. */
    @Transactional(readOnly = true)
    public Response login(LoginRequest request) {
        User user = userRepository.findByStudentNumber(request.studentNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "학번 또는 비밀번호를 확인해 주세요."));
        // 학번과 비밀번호 오류는 동일한 메시지로 응답합니다.
        if (!passwordHasher.matches(request.password().toCharArray(),
                user.getPasswordSalt(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "학번 또는 비밀번호를 확인해 주세요.");
        }
        return Response.from(user);
    }

    /** 저장된 전체 사용자를 공개 응답 형식으로 변환해 반환합니다. */
    @Transactional(readOnly = true)
    public List<Response> findAll() {
        return userRepository.findAll().stream().map(Response::from).toList();
    }

    /** ID로 사용자를 조회해 공개 응답 형식으로 반환합니다. */
    @Transactional(readOnly = true)
    public Response findById(Long id) {
        return Response.from(getUser(id));
    }

    /** 이름과 게임 닉네임을 수정하고, 입력된 경우에만 비밀번호를 변경합니다. */
    public Response update(Long id, UpdateRequest request) {
        User user = getUser(id);
        user.updateProfile(request.name(), request.gameNickname());
        if (request.password() != null && !request.password().isBlank()) {
            // 공백은 비밀번호 변경 요청이 아닌 것으로 처리하고, 새 비밀번호는 최소 길이를 확인합니다.
            if (request.password().length() < 8) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 8자 이상이어야 합니다.");
            }
            String salt = passwordHasher.newSalt();
            user.updatePassword(salt, passwordHasher.hash(request.password().toCharArray(), salt));
        }
        return Response.from(user);
    }

    /** 사용자를 삭제합니다. 존재하지 않는 ID는 404 오류로 처리합니다. */
    public void delete(Long id) {
        User user = getUser(id);
        userRepository.delete(user);
    }

    /** 사용자를 조회하고 존재하지 않으면 404 오류를 발생시킵니다. */
    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
