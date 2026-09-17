package com.example.potatochip.product.controller;

import com.example.potatochip.product.dto.ProductDTO;
import com.example.potatochip.product.dto.ai.AnalyzeImageResult;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.entity.ranking.ProductRankings;
import com.example.potatochip.product.file.FileService;
import com.example.potatochip.product.service.ProductImageService;
import com.example.potatochip.product.service.ai.ProductImageAnalysisService;
import com.example.potatochip.product.service.ranking.ProductRankingsService;
import com.example.potatochip.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductRankingsService productRankingsService;
    private final ProductImageService productImageService;
    private final FileService fileService;
    private final ProductImageAnalysisService productImageAnalysisService;


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

        List<ProductRankings> weeklyRankings = productRankingsService.getWeeklyRanking();

        boolean isRealCategory = category != null && !category.isBlank()
                && !"전체".equals(category) && !"기한임박".equals(category);

        List<ProductRankings> categoryTopRankings = isRealCategory
                ? weeklyRankings.stream()
                .filter(r -> category.equals(r.getProduct().getCategory()))
                .limit(2)
                .collect(Collectors.toList())
                : Collections.emptyList();

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
        model.addAttribute("categoryTopRankings", categoryTopRankings);
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
                                Authentication authentication) throws IOException {

        // 판매자는 로그인한 유저로 고정 (클라이언트가 보낸 값 신뢰하지 않음)
        String sellerEmail = requireLogin(authentication);
        Product product = productService.createProduct(productDTO, thumbnailFile, sellerEmail);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            productImageService.uploadImages(product, imageFiles);
        }

        return "redirect:/market";
    }


    @DeleteMapping("/market/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, Authentication authentication) {
        if (!isOwner(id, authentication)) {
            return ResponseEntity.status(403).build();
        }
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/market/edit/{id}")
    public String marketEdit(@PathVariable Long id, Model model, Authentication authentication) {
        if (!isOwner(id, authentication)) {
            return "redirect:/market/detail?id=" + id;
        }
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
                              @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
                              Authentication authentication) throws IOException {
        if (!isOwner(id, authentication)) {
            return "redirect:/market/detail?id=" + id;
        }
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

    @GetMapping("/market/api/category-ranking")
    @ResponseBody
    public List<CategoryRankingItem> categoryRankingApi(@RequestParam String category) {
        return productRankingsService.getWeeklyRanking().stream()
                .filter(r -> category.equals(r.getProduct().getCategory()))
                .limit(2)
                .map(r -> new CategoryRankingItem(
                        r.getProduct().getId(),
                        r.getProduct().getName(),
                        r.getProduct().getOrigin(),
                        r.getProduct().getPrice(),
                        r.getProduct().getDiscountPrice(),
                        r.getProduct().getThumbnailUrl()))
                .collect(Collectors.toList());
    }

    public record CategoryRankingItem(Long id, String name, String origin, BigDecimal price, BigDecimal discountPrice, String thumbnailUrl) {}


    @PostMapping("/market/api/analyze-image")
    @ResponseBody
    public AnalyzeImageResult analyzeImage(@RequestParam("image") MultipartFile image) throws IOException {
        return productImageAnalysisService.analyze(image);
    }

    // 로그인한 유저의 이메일 반환, 미로그인 시 예외
    private String requireLogin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("로그인이 필요합니다.");
        }
        return authentication.getName();
    }

    // 해당 상품의 판매자가 현재 로그인한 유저인지 확인
    private boolean isOwner(Long productId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        ProductDTO product = productService.getProductById(productId);
        return product != null && authentication.getName().equals(product.getSellerEmail());
    }

}