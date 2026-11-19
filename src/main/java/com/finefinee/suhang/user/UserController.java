package com.finefinee.suhang.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.finefinee.suhang.user.UserDtos.*;

/** 사용자 REST API 요청을 서비스 계층에 연결합니다. */
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    /** 사용자 서비스 계층을 주입받습니다. */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** POST /users: 회원가입을 처리하고 201 Created를 반환합니다. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response create(@Valid @RequestBody CreateRequest request) {
        return userService.create(request);
    }

    /** POST /users/login: 학번과 비밀번호를 확인합니다. */
    @PostMapping("/login")
    public Response login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    /** GET /users: 사용자 전체 목록을 조회합니다. */
    @GetMapping
    public List<Response> findAll() {
        return userService.findAll();
    }

    /** GET /users/{id}: 사용자 한 명을 조회합니다. */
    @GetMapping("/{id}")
    public Response findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    /** PUT /users/{id}: 이름, 게임 닉네임과 선택적 비밀번호를 수정합니다. */
    @PutMapping("/{id}")
    public Response update(@PathVariable Long id, @Valid @RequestBody UpdateRequest request) {
        return userService.update(id, request);
    }

    /** DELETE /users/{id}: 사용자를 삭제하고 본문 없는 204 응답을 반환합니다. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
