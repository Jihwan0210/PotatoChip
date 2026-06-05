package com.example.potatochip.controller;


import com.example.potatochip.dto.request.ProductRequestDTO;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.potatochip.dto.response.ProductResponseDTO;
import com.example.potatochip.service.ProductService;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {

        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponseDTO> getProducts(){
        return productService.getProducts();
    }

    @GetMapping("/{id}")
    public ProductResponseDTO getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @PostMapping
    public ProductResponseDTO createProduct(@RequestBody ProductRequestDTO request) {
        return productService.createProduct(request);
    }
}
