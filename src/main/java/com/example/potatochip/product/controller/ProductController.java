package com.example.potatochip.product.controller;

import com.example.potatochip.product.dto.ProductDTO;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.service.ProductImageService;
import com.example.potatochip.product.service.ranking.ProductRankingsService;
import com.example.potatochip.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public String market(
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "popular") String sort,
            Model model) {

        if (page < 0) page = 0; // 추가

        Pageable pageable = PageRequest.of(page, 12); // 한 페이지 12개
        // ProductController
        Page<ProductDTO> products = productService.getProducts(category, keyword, sort, pageable);

        model.addAttribute("products", products.getContent()); // 이걸로 교체
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("dailyRankings", productRankingsService.getDailyRanking());
        model.addAttribute("weeklyRankings", productRankingsService.getWeeklyRanking());
        model.addAttribute("totalElements", products.getTotalElements());
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