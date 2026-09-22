package com.example.potatochip;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * prod 프로파일로도 컨텍스트가 뜨는지 검증한다.
 *
 * local 프로파일만 테스트하면 prod 전용 빈(S3FileService, AmazonS3)이 검증되지 않아
 * 배포 후에야 기동 실패가 드러난다. 실제로 그렇게 장애가 났기 때문에 추가한 테스트다.
 *
 * AmazonS3ClientBuilder 는 build() 시점에 자격증명을 조회하지 않으므로
 * AWS 자격증명 없이도 통과한다.
 */
@SpringBootTest
@ActiveProfiles("prod")
class ProdProfileContextTest {

    @Test
    void contextLoads() {
    }
}
