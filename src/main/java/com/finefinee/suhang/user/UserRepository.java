package com.finefinee.suhang.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** 사용자 엔티티의 데이터베이스 조회와 저장을 담당합니다. */
public interface UserRepository extends JpaRepository<User, Long> {
    /** 회원가입 시 학번 중복 여부를 확인합니다. */
    boolean existsByStudentNumber(String studentNumber);

    /** 로그인할 사용자를 학번으로 조회합니다. */
    Optional<User> findByStudentNumber(String studentNumber);
}
