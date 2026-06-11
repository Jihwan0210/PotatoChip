package com.example.potatochip.product.controller;

import com.example.potatochip.product.dto.ProductDTO;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.service.ProductImageService;
import com.example.potatochip.product.service.ranking.ProductRankingsService;
import com.example.potatochip.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductRankingsService productRankingsService;
    private final ProductImageService productImageService;



    @GetMapping("/market")
    public String market(Model model) {
        model.addAttribute("products" , productService.getAllProducts());
        model.addAttribute("dailyRankings", productRankingsService.getDailyRanking());
        model.addAttribute("weeklyRankings", productRankingsService.getWeeklyRanking());
        return "product/market";
    }

    @GetMapping("/market/detail")
    public String marketDetail(
            @RequestParam Long id, Model model) {
        ProductDTO product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product/market-detail";
    }

    @GetMapping("/market/create")
    public String marketCreate() {
        return "product/create";
    }

    @PostMapping("/market/create")
    public String createProduct(ProductDTO productDTO,
                                @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
                                @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles) throws IOException {

        Product product = productService.createProduct(productDTO, thumbnailFile);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            productImageService.uploadImages(product, imageFiles);
        }

        return "redirect:/market";
    }

}