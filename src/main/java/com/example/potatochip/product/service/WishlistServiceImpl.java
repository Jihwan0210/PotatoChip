package com.example.potatochip.product.service;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.product.dto.ProductDTO;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.entity.Wishlist;
import com.example.potatochip.product.repository.ProductRepository;
import com.example.potatochip.product.repository.WishRepository;
import com.example.potatochip.product.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public boolean toggle(String email, Long productId) {
        var existing = wishlistRepository.findByUserEmailAndProductId(email, productId);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return false; // 찜 해제
        } else {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("유저 없음: " + email));
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("상품 없음: " + productId));
            Wishlist wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setProduct(product);
            wishlistRepository.save(wishlist);
            return true; // 찜 추가
        }
    }

    @Override
    public List<ProductDTO> getWishlist(String email) {
        return wishlistRepository.findByUserEmail(email).stream()
                .map(w -> modelMapper.map(w.getProduct(), ProductDTO.class))
                .collect(Collectors.toList());
    }
}