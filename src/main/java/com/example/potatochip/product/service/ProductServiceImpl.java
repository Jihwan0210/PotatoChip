package com.example.potatochip.product.service;

import com.example.potatochip.product.dto.ProductDTO;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.file.FileService;
import com.example.potatochip.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final FileService fileService;



    @Override
    public ProductDTO getProductById(Long id) {
        Optional<Product> result = productRepository.findById(id);
        Product product = result.orElseThrow();
        ProductDTO productDTO = modelMapper.map(product , ProductDTO.class);
        productDTO.setImages(product.getImages());
        return productDTO;
    }



    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());
    }





    @Override
    public Product createProduct(ProductDTO productDTO , MultipartFile file) {

        if (file != null && !file.isEmpty()) {
            try {
                String url = fileService.upload(file);
                productDTO.setThumbnailUrl(url);
            } catch (IOException e) {
                throw new RuntimeException("저장 실패" , e);
            }
        }

        Product product = modelMapper.map(productDTO , Product.class);
        product.setSellerId(1L);
        productRepository.save(product);
        return product;
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


