package com.example.potatochip.product.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * AmazonS3 빈 수동 등록.
 *
 * spring-cloud-starter-aws 2.2.6 은 Spring Boot 2 용이라 자동 설정을 spring.factories 로
 * 등록한다. Boot 3 는 그 파일을 자동 설정 용도로 읽지 않으므로 AmazonS3 빈이 만들어지지
 * 않는다. SDK 클래스 자체는 클래스패스에 있으므로 여기서 직접 만든다.
 *
 * 자격증명은 DefaultAWSCredentialsProviderChain 이 EC2 인스턴스 프로파일(IAM 역할)에서
 * 가져온다. 액세스 키를 설정에 두지 않는다.
 */
@Configuration
@Profile("prod")
public class AwsS3Config {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }
}
