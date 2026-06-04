package com.example.potatochip.product.file;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@Profile("local")
public class LocalFileService implements  FileService{

    @Override
    public String upload(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path savePath = Paths.get("uploads/products/", fileName);
        Files.createDirectories(savePath.getParent());
        Files.copy(file.getInputStream(), savePath);
        return "/uploads/products/" + fileName;
    }
}