package com.finefinee.suhang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Spring Boot 애플리케이션의 시작점입니다. */
@SpringBootApplication
public class SuhangApplication {
    /** 내장 웹 서버와 애플리케이션 컨텍스트를 실행합니다. */
    public static void main(String[] args) {
        SpringApplication.run(SuhangApplication.class, args);
    }
}
