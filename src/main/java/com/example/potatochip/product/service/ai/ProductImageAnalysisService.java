package com.example.potatochip.product.service.ai;

import com.example.potatochip.product.dto.ai.AnalyzeImageResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductImageAnalysisService {

    AnalyzeImageResult analyze(MultipartFile image) throws IOException;

}