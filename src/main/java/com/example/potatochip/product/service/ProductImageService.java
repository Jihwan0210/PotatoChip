package com.example.potatochip.product.service;


import com.example.potatochip.product.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductImageService  {

    //상품 이미지 업로드 저장
    void uploadImages(Product product, List<MultipartFile> files) throws IOException;
}
