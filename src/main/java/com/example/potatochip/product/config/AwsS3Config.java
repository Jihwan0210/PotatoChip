package com.example.potatochip.product.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// spring-cloud-starter-aws 2.2.6 은 Boot 2 용이라 Boot 3 에서 자동설정이 동작하지 않는다.
// SDK 클래스는 클래스패스에 있으므로 AmazonS3 빈만 직접 등록한다.
// 자격증명은 EC2 인스턴스 프로파일(IAM 역할)에서 가져온다.
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
