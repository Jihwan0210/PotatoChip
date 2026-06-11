package com.example.potatochip.product.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    String upload(MultipartFile file) throws IOException;
}
