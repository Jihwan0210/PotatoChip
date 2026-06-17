package com.example.potatochip.product.service;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.product.dto.ProductDTO;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.file.FileService;
import com.example.potatochip.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final FileService fileService;
    private final UserRepository userRepository;



    @Override
    public ProductDTO getProductById(Long id) {
        Optional<Product> result = productRepository.findById(id);
        Product product = result.orElseThrow();
        ProductDTO productDTO = modelMapper.map(product , ProductDTO.class);
        productDTO.setImages(product.getImages());
        productDTO.setSellerPhone(product.getSeller().getPhone());
        productDTO.setSellerEmail(product.getSeller().getEmail());
        return productDTO;
    }


    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());
    }





    @Override
    public Product createProduct(ProductDTO productDTO, MultipartFile file, String sellerEmail) {

        if (file != null && !file.isEmpty()) {
            try {
                String url = fileService.upload(file);
                productDTO.setThumbnailUrl(url);
            } catch (IOException e) {
                throw new RuntimeException("저장 실패", e);
            }
        }

        Product product = modelMapper.map(productDTO, Product.class);

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new RuntimeException("유저 없음: " + sellerEmail));

        product.setSeller(seller);
        return productRepository.save(product);
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

    @Override
    public Page<ProductDTO> getProducts(String category, String keyword, String sort, String sellerEmail, Pageable pageable) {

        LocalDate today = LocalDate.now();
        LocalDate expireLimit = today.plusDays(4);

        Page<Product> products;

        if (sellerEmail != null) {
            Sort sorting = switch (sort) {
                case "price"  -> Sort.by("price").ascending();
                case "newest" -> Sort.by("createdAt").descending();
                default       -> Sort.by("id").descending();
            };
            Pageable sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    sorting
            );
            products = productRepository.searchProductsBySellerEmail(
                    sellerEmail, category, keyword, today, expireLimit, sortedPageable
            );
        } else if ("popular".equals(sort)) {
            products = productRepository.searchProductsByPopular(
                    category, keyword, today, expireLimit, pageable
            );
        } else if ("discount".equals(sort)) {
            products = productRepository.searchProductsByDiscount(
                    category, keyword, today, expireLimit, pageable
            );
        } else {
            Sort sorting = switch (sort) {
                case "price"  -> Sort.by("price").ascending();
                case "newest" -> Sort.by("createdAt").descending();
                default       -> Sort.by("id").descending();
            };
            Pageable sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    sorting
            );
            products = productRepository.searchProducts(
                    category, keyword, today, expireLimit, sortedPageable
            );
        }

        return products.map(product -> toDTO(product, today, expireLimit));
    }

    private ProductDTO toDTO(Product product, LocalDate today, LocalDate expireLimit) {
        ProductDTO dto = modelMapper.map(product, ProductDTO.class);
        if (product.getDiscountEndAt() != null) {
            LocalDate endAt = product.getDiscountEndAt();
            dto.setSoonExpired(!endAt.isBefore(today) && endAt.isBefore(expireLimit));
        } else {
            dto.setSoonExpired(false);
        }
        return dto;
    }



}


