package com.example.potatochip.product.controller;

import com.example.potatochip.product.dto.ProductDTO;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;



    @GetMapping("/market")
    public String market(Model model) {
        model.addAttribute("products" , productService.getAllProducts());
        return "product/market";
    }

    @GetMapping("/market/detail")
    public String marketDetail(@RequestParam Long id, Model model) {
        ProductDTO product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product/market-detail";
    }

    @GetMapping("/market/create")
    public String marketCreate() {
        return "product/create";
    }

    @PostMapping("/market/create")
    public String createProduct(ProductDTO productDTO) {

        productService.createProduct(productDTO);

        return "redirect:/market";
    }

}