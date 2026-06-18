package com.example.potatochip.product.controller;

import com.example.potatochip.product.dto.ProductDTO;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.entity.ranking.ProductRankings;
import com.example.potatochip.product.file.FileService;
import com.example.potatochip.product.service.ProductImageService;
import com.example.potatochip.product.service.ranking.ProductRankingsService;
import com.example.potatochip.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductRankingsService productRankingsService;
    private final ProductImageService productImageService;
    private final FileService fileService;



    @GetMapping("/market")
    public String market(
            @RequestParam(defaultValue = "") String category,       // 카테고리 필터
            @RequestParam(defaultValue = "") String keyword,        // 검색 키워드
            @RequestParam(defaultValue = "all") String searchType,  // 검색 타입 (all/name/farm/region)
            @RequestParam(defaultValue = "0") int page,             // 현재 페이지
            @RequestParam(defaultValue = "popular") String sort,    // 정렬 기준
            @RequestParam(required = false) String sellerEmail,     // 내 상품 필터용 판매자 이메일
            Model model) {

        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, 12); // 한 페이지 12개
        Page<ProductDTO> products = productService.getProducts(category, keyword, searchType, sort, sellerEmail, pageable);

        model.addAttribute("products", products.getContent());       // 상품 목록
        model.addAttribute("currentPage", page);                     // 현재 페이지
        model.addAttribute("totalPages", products.getTotalPages());  // 전체 페이지 수
        model.addAttribute("category", category);                    // 선택된 카테고리
        model.addAttribute("keyword", keyword);                      // 검색어
        model.addAttribute("searchType", searchType);                // 검색 타입
        model.addAttribute("sort", sort);                            // 정렬 기준
        model.addAttribute("sellerId", sellerEmail);                 // 페이지네이션 유지용
        model.addAttribute("dailyRankings", productRankingsService.getDailyRanking());   // 일간 랭킹
        model.addAttribute("weeklyRankings", productRankingsService.getWeeklyRanking()); // 주간 랭킹
        model.addAttribute("totalElements", products.getTotalElements()); // 전체 상품 수
        return "product/market";
    }

    @GetMapping("/market/detail")
    public String marketDetail(@RequestParam Long id, Model model) {
        ProductDTO product = productService.getProductById(id); //
        model.addAttribute("product", product);
        Integer weekelyRanking = productRankingsService.getWeeklyRanking()
                .stream()
                .filter(r -> r.getProduct().getId().equals(id))
                .map(ProductRankings::getRank)
                .findFirst()
                .orElse(null);
        model.addAttribute("weekelyRanking", weekelyRanking);
        return "product/market-detail";
    }

    @GetMapping("/market/create")
    public String marketCreate() {
        return "product/create";
    }

    @PostMapping("/market/create")
    public String createProduct(ProductDTO productDTO,
                                @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
                                @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                                @RequestParam("sellerEmail") String sellerEmail) throws IOException {

        Product product = productService.createProduct(productDTO, thumbnailFile, sellerEmail);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            productImageService.uploadImages(product, imageFiles);
        }

        return "redirect:/market";
    }


    @DeleteMapping("/market/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/market/edit/{id}")
    public String marketEdit(@PathVariable Long id, Model model) {
        ProductDTO product = productService.getProductById(id);
        model.addAttribute("product", product);

        Integer discountRate = null;
        if (product.getDiscountPrice() != null && product.getPrice() != null
                && product.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            discountRate = product.getPrice().subtract(product.getDiscountPrice())
                    .divide(product.getPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
        }
        model.addAttribute("discountRate", discountRate);


        return "product/market-edit";
    }

    @PostMapping("/market/edit/{id}")
    public String editProduct(@PathVariable Long id,
                              ProductDTO productDTO,
                              @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
                              @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                              @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds) throws IOException {
        productDTO.setId(id);
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            String url = fileService.upload(thumbnailFile);
            productDTO.setThumbnailUrl(url);
        } else {
            ProductDTO existing = productService.getProductById(id);
            productDTO.setThumbnailUrl(existing.getThumbnailUrl());
        }
        productService.modify(productDTO);

        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            productImageService.deleteImages(deleteImageIds);
        }

        if (imageFiles != null && !imageFiles.isEmpty()) {
            productImageService.uploadImages(productService.getProductEntity(id), imageFiles);
        }

        return "redirect:/market/detail?id=" + id;
    }


}