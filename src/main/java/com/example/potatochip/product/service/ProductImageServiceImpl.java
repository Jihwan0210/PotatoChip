package com.example.potatochip.product.service;


import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.entity.ProductImage;
import com.example.potatochip.product.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService{
    private final ProductImageRepository productImageRepository;

    // 이미지 저장 경로
    private final String uploadDir = "uploads/products/";

    @Override
    @Transactional
    public void uploadImages(Product product, List<MultipartFile> files) throws IOException {

        for (MultipartFile file : files) {

            // UUID 파일명 생성
            String uuid = UUID.randomUUID().toString();
            String originalName = file.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String savedName = uuid + "_" + originalName.replaceAll("[^a-zA-Z0-9.]", "") + ext;

            // 파일 저장
            File dest = new File(uploadDir + savedName);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);

            // DB 저장
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setUuid("/uploads/products/" + savedName);
            productImageRepository.save(image);
        }
    }
}

