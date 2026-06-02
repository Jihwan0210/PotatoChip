package com.example.potatochip.product.service;

import com.example.potatochip.product.dto.ProductDTO;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;



    @Override
    public ProductDTO getProductById(Long id) {
        Optional<Product> result = productRepository.findById(id);
        Product product = result.orElseThrow();
        ProductDTO productDTO = modelMapper.map(product , ProductDTO.class);
        return productDTO;
    }



    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());
    }





    @Override
    public Long createProduct(ProductDTO productDTO) {
        Product product = modelMapper.map(productDTO , Product.class);
        product.setSellerId(1L);
        productRepository.save(product);
        return product.getId();
    }


    @Override
    public void modify(ProductDTO productDTO) {
        Optional<Product> result = productRepository.findById(productDTO.getId());
        Product product = result.orElseThrow();
        product.changeEntity(productDTO);
        productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
