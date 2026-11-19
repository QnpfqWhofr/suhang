package com.finefinee.suhang.user;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** PBKDF2 기반 비밀번호 해시 생성과 검증을 제공합니다. */
@Component
public class PasswordHasher {
    /** 비밀번호마다 생성할 임의 salt 길이입니다. */
    private static final int SALT_LENGTH = 16;
    /** 해시 계산 반복 횟수로 비밀번호 대입 비용을 높입니다. */
    private static final int ITERATIONS = 120_000;
    /** 생성할 비밀번호 해시의 비트 길이입니다. */
    private static final int KEY_LENGTH = 256;

    private final SecureRandom secureRandom = new SecureRandom();

    /** 예측할 수 없는 사용자별 salt를 생성해 Base64 문자열로 반환합니다. */
    public String newSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** 비밀번호와 salt를 PBKDF2-HMAC-SHA256으로 해시합니다. */
    public String hash(char[] password, String salt) {
        PBEKeySpec keySpec = new PBEKeySpec(
                password, Base64.getDecoder().decode(salt), ITERATIONS, KEY_LENGTH);
        try {
            // 비밀번호 원문이 아닌 해시 결과만 영속화할 수 있도록 문자열로 반환합니다.
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(keySpec)
                    .getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException exception) {
            // 암호화 알고리즘 사용 실패를 정상 응답으로 숨기지 않습니다.
            throw new IllegalStateException("비밀번호를 안전하게 처리하지 못했습니다.", exception);
        } finally {
            // 키 사양 내부에 복사된 비밀번호를 제거합니다.
            keySpec.clearPassword();
        }
    }

    /** 입력한 비밀번호의 해시를 상수 시간 비교로 저장된 해시와 대조합니다. */
    public boolean matches(char[] password, String salt, String expectedHash) {
        byte[] actual = Base64.getDecoder().decode(hash(password, salt));
        byte[] expected = Base64.getDecoder().decode(expectedHash);
        return MessageDigest.isEqual(actual, expected);
    }
}
