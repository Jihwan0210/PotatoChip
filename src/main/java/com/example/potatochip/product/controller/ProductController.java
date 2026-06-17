package com.example.potatochip.product.controller;

import com.example.potatochip.product.dto.ProductDTO;
import com.example.potatochip.product.entity.Product;
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
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "popular") String sort,
            @RequestParam(required = false) String sellerEmail,  // Long sellerId → String sellerEmail
            Model model) {

        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, 12);
        Page<ProductDTO> products = productService.getProducts(category, keyword, sort, sellerEmail, pageable);

        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("sellerId", sellerEmail);  // 템플릿 페이지네이션 유지용 (키 이름 유지)
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
        return "product/market-edit";
    }

    @PostMapping("/market/edit/{id}")
    public String editProduct(@PathVariable Long id,
                              ProductDTO productDTO,
                              @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile) throws IOException {
        productDTO.setId(id);
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            String url = fileService.upload(thumbnailFile);
            productDTO.setThumbnailUrl(url);
        }
        productService.modify(productDTO);
        return "redirect:/market/detail?id=" + id;
    }


}