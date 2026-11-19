package com.finefinee.suhang.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 학번, 이름, 게임 닉네임과 해시된 비밀번호를 저장하는 사용자 엔티티입니다. */
@Entity
@Table(name = "users")
public class User {
    /** 데이터베이스가 발급하는 사용자 ID입니다. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 학번은 로그인 ID이므로 중복될 수 없습니다. */
    @Column(nullable = false, unique = true, length = 30)
    private String studentNumber;

    /** 사용자의 실명 또는 표시 이름입니다. */
    @Column(nullable = false, length = 50)
    private String name;

    /** 대회 참가에 사용하는 게임 닉네임입니다. */
    @Column(nullable = false, length = 50)
    private String gameNickname;

    /** 비밀번호 해시에 사용된 salt를 Base64로 저장합니다. */
    @Column(nullable = false, length = 32)
    private String passwordSalt;

    /** 원본 비밀번호가 아닌 PBKDF2 해시를 Base64로 저장합니다. */
    @Column(nullable = false, length = 64)
    private String passwordHash;

    /** JPA가 조회 결과를 생성할 때 사용하는 기본 생성자입니다. */
    protected User() {
    }

    /** 회원가입 정보와 이미 계산된 비밀번호 정보를 받아 엔티티를 생성합니다. */
    User(String studentNumber, String name, String gameNickname, String passwordSalt, String passwordHash) {
        this.studentNumber = studentNumber;
        this.name = name;
        this.gameNickname = gameNickname;
        this.passwordSalt = passwordSalt;
        this.passwordHash = passwordHash;
    }

    /** 사용자 ID를 반환합니다. */
    public Long getId() {
        return id;
    }

    /** 학번을 반환합니다. */
    public String getStudentNumber() {
        return studentNumber;
    }

    /** 이름을 반환합니다. */
    public String getName() {
        return name;
    }

    /** 게임 닉네임을 반환합니다. */
    public String getGameNickname() {
        return gameNickname;
    }

    /** 비밀번호 검증에 필요한 salt를 서비스 내부에서 반환합니다. */
    String getPasswordSalt() {
        return passwordSalt;
    }

    /** 비밀번호 검증에 필요한 해시를 서비스 내부에서 반환합니다. */
    String getPasswordHash() {
        return passwordHash;
    }

    /** 사용자의 공개 프로필 정보를 수정합니다. */
    void updateProfile(String name, String gameNickname) {
        this.name = name;
        this.gameNickname = gameNickname;
    }

    /** 새 비밀번호의 salt와 해시를 저장합니다. */
    void updatePassword(String salt, String hash) {
        this.passwordSalt = salt;
        this.passwordHash = hash;
    }
}
