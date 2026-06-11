//package com.example.potatochip.product.file;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.ObjectMetadata;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.UUID;
//
//@Service
//@Profile("prod") // Prod에서만 동작하는 S3 업로드
//@RequiredArgsConstructor
//public class S3FileService implements FileService {
//
//    private final AmazonS3 amazonS3;
//
//    @Value("${cloud.aws.s3.bucket}")
//    private String bucket; //업로드 대상 S3 버킷명
//
//    @Override
//    public String upload(MultipartFile file) throws IOException {
//
//        //파일명 중복 방지를 위해 UUID 강아지 -> 123456_강아지
//        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//
//        //파일 크기 및 MIME 타입 정보
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentLength(file.getSize());
//        metadata.setContentType(file.getContentType());
//
//       //AWS S3 버킷에 파일을 업로드
//        amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
//
//       //업로드된 파일의 접근 URL
//        return amazonS3.getUrl(bucket, fileName).toString();
//    }
//}

//S3를 이용한 사진 저장 방식 배포할 때 쓸 예정