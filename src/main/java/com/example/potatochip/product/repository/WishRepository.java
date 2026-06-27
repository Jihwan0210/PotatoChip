package com.example.potatochip.product.repository;

import com.example.potatochip.product.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserEmail(String email);
    Optional<Wishlist> findByUserEmailAndProductId(String email, Long productId);
    List<Wishlist> findByProductId(Long productId);
}