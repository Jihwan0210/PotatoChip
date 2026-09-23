package com.example.potatochip;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// prod 전용 빈(S3FileService, AmazonS3)은 local 프로파일 테스트로는 검증되지 않는다.
@SpringBootTest
@ActiveProfiles("prod")
class ProdProfileContextTest {

    @Test
    void contextLoads() {
    }
}
