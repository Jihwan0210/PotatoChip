package com.example.potatochip.product.repository;

import com.example.potatochip.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    long countByStockQuantityLessThanEqual(Integer stock);

    long countByIsPickupAvailableTrue();

    List<Product> findByStockQuantityLessThanEqual(Integer stock);

    List<Product> findBySellerId(Long sellerId);

    Page<Product> findByCategory(String category, Pageable pageable);

    // 기존 쿼리
    @Query("SELECT p FROM Product p " +
            "WHERE (:category = '' OR :category = '전체' OR p.category = :category " +
            "OR (:category = '기한임박' AND p.discountEndAt IS NOT NULL " +
            "AND p.discountEndAt >= :today AND p.discountEndAt < :expireLimit)) " +
            "AND (:keyword = '' OR p.name LIKE %:keyword% " +
            "OR p.origin LIKE %:keyword% OR p.address LIKE %:keyword%)")
    Page<Product> searchProducts(@Param("category") String category,
                                 @Param("keyword") String keyword,
                                 @Param("today") LocalDate today,
                                 @Param("expireLimit") LocalDate expireLimit,
                                 Pageable pageable);

    // 인기순 - 랭킹 salesCount 높은 순
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN ProductRankings r ON r.product = p AND r.periodType = 'WEEKLY' " +
            "WHERE (:category = '' OR :category = '전체' OR p.category = :category " +
            "OR (:category = '기한임박' AND p.discountEndAt IS NOT NULL " +
            "AND p.discountEndAt >= :today AND p.discountEndAt < :expireLimit)) " +
            "AND (:keyword = '' OR p.name LIKE %:keyword% " +
            "OR p.origin LIKE %:keyword% OR p.address LIKE %:keyword%) " +
            "ORDER BY COALESCE(r.salesCount, 0) DESC")
    Page<Product> searchProductsByPopular(@Param("category") String category,
                                          @Param("keyword") String keyword,
                                          @Param("today") LocalDate today,
                                          @Param("expireLimit") LocalDate expireLimit,
                                          Pageable pageable);

    // 할인율순 - 할인율 높은 순, 할인 없으면 맨 뒤
    @Query("SELECT p FROM Product p " +
            "WHERE (:category = '' OR :category = '전체' OR p.category = :category " +
            "OR (:category = '기한임박' AND p.discountEndAt IS NOT NULL " +
            "AND p.discountEndAt >= :today AND p.discountEndAt < :expireLimit)) " +
            "AND (:keyword = '' OR p.name LIKE %:keyword% " +
            "OR p.origin LIKE %:keyword% OR p.address LIKE %:keyword%) " +
            "ORDER BY CASE WHEN p.discountPrice IS NULL THEN 0 " +
            "ELSE (p.price - p.discountPrice) * 1.0 / p.price END DESC")
    Page<Product> searchProductsByDiscount(@Param("category") String category,
                                           @Param("keyword") String keyword,
                                           @Param("today") LocalDate today,
                                           @Param("expireLimit") LocalDate expireLimit,
                                           Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "WHERE p.seller.email = :sellerEmail " +
            "AND (:category = '' OR :category = '전체' OR p.category = :category " +
            "OR (:category = '기한임박' AND p.discountEndAt IS NOT NULL " +
            "AND p.discountEndAt >= :today AND p.discountEndAt < :expireLimit)) " +
            "AND (:keyword = '' OR p.name LIKE %:keyword% " +
            "OR p.origin LIKE %:keyword% OR p.address LIKE %:keyword%)")
    Page<Product> searchProductsBySellerEmail(@Param("sellerEmail") String sellerEmail,
                                              @Param("category") String category,
                                              @Param("keyword") String keyword,
                                              @Param("today") LocalDate today,
                                              @Param("expireLimit") LocalDate expireLimit,
                                              Pageable pageable);
}