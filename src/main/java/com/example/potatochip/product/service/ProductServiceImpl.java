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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.example.potatochip.notification.service.NotificationService;
import com.example.potatochip.product.entity.Wishlist;
import com.example.potatochip.product.repository.WishRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final FileService fileService;
    private final UserRepository userRepository;
    private final WishRepository wishRepository;
    private final NotificationService notificationService;



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

        BigDecimal oldPrice = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();

        product.changeEntity(productDTO);
        productRepository.save(product);

        BigDecimal newPrice = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();

        if (oldPrice != null && newPrice != null && oldPrice.compareTo(newPrice) != 0) {
            List<Wishlist> wishlists = wishRepository.findByProductId(product.getId());

            for (Wishlist wishlist : wishlists) {
                notificationService.createPriceChangedNotification(
                        wishlist.getUser().getId(),
                        product.getId(),
                        product.getName(),
                        oldPrice,
                        newPrice
                );
            }
        }
    }

    @Override
    public Product getProductEntity(Long id) {
        return productRepository.findById(id).orElseThrow();
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }




    @Override
    public Page<ProductDTO> getProducts(String category, String keyword, String searchType, String sort, String sellerEmail, Pageable pageable) {

        LocalDate today = LocalDate.now();
        LocalDate expireLimit = today.plusDays(4); // 기한임박 기준: 오늘 + 4일

        Page<Product> products;

        if (sellerEmail != null) {
            // 내 상품 필터: 정렬은 price/newest/기본만 지원 (popular/discount 제외)
            Sort sorting = switch (sort) {
                case "price"  -> Sort.by("price").ascending();
                case "newest" -> Sort.by("createdAt").descending();
                default       -> Sort.by("id").descending();
            };
            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sorting);
            products = productRepository.searchProductsBySellerEmail(
                    sellerEmail, category, keyword, searchType, today, expireLimit, sortedPageable
            );
        } else if ("popular".equals(sort)) {
            // 인기순: WEEKLY salesCount 기준 (이번 주 월요일 날짜로 필터)
            LocalDate weekStart = today.with(DayOfWeek.MONDAY);
            products = productRepository.searchProductsByPopular(
                    category, keyword, searchType, today, expireLimit, weekStart, pageable
            );
        } else if ("discount".equals(sort)) {
            // 할인율순: 할인율 높은 순, 할인 없는 상품 맨 뒤
            products = productRepository.searchProductsByDiscount(
                    category, keyword, searchType, today, expireLimit, pageable
            );
        } else {
            // 가격순 / 최신순 / 기본(id 내림차순)
            Sort sorting = switch (sort) {
                case "price"  -> Sort.by("price").ascending();
                case "newest" -> Sort.by("createdAt").descending();
                default       -> Sort.by("id").descending();
            };
            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sorting);
            products = productRepository.searchProducts(
                    category, keyword, searchType, today, expireLimit, sortedPageable
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


