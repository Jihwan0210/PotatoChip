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

    // 재고가 stock 이하인 상품 수 조회
    long countByStockQuantityLessThanEqual(Integer stock);

    // 픽업 가능한 상품 수 조회
    long countByIsPickupAvailableTrue();

    // 재고가 stock 이하인 상품 목록 조회
    List<Product> findByStockQuantityLessThanEqual(Integer stock);

    // 판매자 ID로 상품 목록 조회
    List<Product> findBySellerId(Long sellerId);

    // 카테고리로 상품 페이징 조회
    Page<Product> findByCategory(String category, Pageable pageable);

    // 카테고리 + 키워드(검색타입별) 필터 검색 (기본)
    @Query("SELECT p FROM Product p " +
            "WHERE (:category = '' OR :category = '전체' OR p.category = :category " +
            "OR (:category = '기한임박' AND p.discountEndAt IS NOT NULL " +
            "AND p.discountEndAt >= :today AND p.discountEndAt < :expireLimit)) " +
            "AND (:keyword = '' OR (" +
            "(p.name LIKE %:keyword% AND (:searchType = 'all' OR :searchType = 'name')) OR " +
            "(p.origin LIKE %:keyword% AND (:searchType = 'all' OR :searchType = 'farm')) OR " +
            "(p.address LIKE %:keyword% AND (:searchType = 'all' OR :searchType = 'region'))))")
    Page<Product> searchProducts(@Param("category") String category,
                                 @Param("keyword") String keyword,
                                 @Param("searchType") String searchType,
                                 @Param("today") LocalDate today,
                                 @Param("expireLimit") LocalDate expireLimit,
                                 Pageable pageable);

    // 인기순 검색 - ProductRankings의 WEEKLY salesCount 기준 내림차순
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN ProductRankings r ON r.product = p AND r.periodType = 'WEEKLY' AND r.periodDate = :weekStart " +
            "WHERE (:category = '' OR :category = '전체' OR p.category = :category " +
            "OR (:category = '기한임박' AND p.discountEndAt IS NOT NULL " +
            "AND p.discountEndAt >= :today AND p.discountEndAt < :expireLimit)) " +
            "AND (:keyword = '' OR (" +
            "(p.name LIKE %:keyword% AND (:searchType = 'all' OR :searchType = 'name')) OR " +
            "(p.origin LIKE %:keyword% AND (:searchType = 'all' OR :searchType = 'farm')) OR " +
            "(p.address LIKE %:keyword% AND (:searchType = 'all' OR :searchType = 'region'))))"+
            "ORDER BY COALESCE(r.salesCount, 0) DESC")
    Page<Product> searchProductsByPopular(@Param("category") String category,
                                          @Param("keyword") String keyword,
                                          @Param("searchType") String searchType,
                                          @Param("today") LocalDate today,
                                          @Param("expireLimit") LocalDate expireLimit,
                                          @Param("weekStart") LocalDate weekStart,
                                          Pageable pageable);

    // 할인율순 검색 - 할인율 높은 순, 할인 없는 상품은 맨 뒤에 배치
    @Query("SELECT p FROM Product p " +
            "WHERE (:category = '' OR :category = '전체' OR p.category = :category " +
            "OR (:category = '기한임박' AND p.discountEndAt IS NOT NULL " +
            "AND p.discountEndAt >= :today AND p.discountEndAt < :expireLimit)) " +
            "AND (:keyword = '' OR (" +
            "(p.name LIKE %:keyword% AND (:searchType = 'all' OR :searchType = 'name')) OR " +
            "(p.origin LIKE %:keyword% AND (:searchType = 'all' OR :searchType = 'farm')) OR " +
            "(p.address LIKE %:keyword% AND (:searchType = 'all' OR :searchType = 'region'))))"+
            "ORDER BY CASE WHEN p.discountPrice IS NULL THEN 0 " +
            "ELSE (p.price - p.discountPrice) * 1.0 / p.price END DESC")
    Page<Product> searchProductsByDiscount(@Param("category") String category,
                                           @Param("keyword") String keyword,
                                           @Param("searchType") String searchType,
                                           @Param("today") LocalDate today,
                                           @Param("expireLimit") LocalDate expireLimit,
                                           Pageable pageable);

    // 특정 판매자 이메일 기준 상품 검색 (내 상품 필터용)
    @Query("SELECT p FROM Product p " +
            "WHERE p.seller.email = :sellerEmail " +
            "AND (:category = '' OR :category = '전체' OR p.category = :category " +
            "OR (:category = '기한임박' AND p.discountEndAt IS NOT NULL " +
            "AND p.discountEndAt >= :today AND p.discountEndAt < :expireLimit)) " +
            "AND (:keyword = '' OR (" +
            "(p.name LIKE %:keyword% AND (:searchType = 'all' OR :searchType = 'name')) OR " +
            "(p.origin LIKE %:keyword% AND (:searchType = 'all' OR :searchType = 'farm')) OR " +
            "(p.address LIKE %:keyword% AND (:searchType = 'all' OR :searchType = 'region'))))")
    Page<Product> searchProductsBySellerEmail(@Param("sellerEmail") String sellerEmail,
                                              @Param("category") String category,
                                              @Param("keyword") String keyword,
                                              @Param("searchType") String searchType,
                                              @Param("today") LocalDate today,
                                              @Param("expireLimit") LocalDate expireLimit,
                                              Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "WHERE p.category = :category " +
            "AND p.id <> :productId " +
            "ORDER BY p.createdAt DESC")
    List<Product> findRelatedByCategory(@Param("category") String category,
                                        @Param("productId") Long productId,
                                        Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "WHERE p.seller.id = :sellerId " +
            "AND p.id <> :productId " +
            "ORDER BY p.createdAt DESC")
    List<Product> findRelatedBySeller(@Param("sellerId") Long sellerId,
                                      @Param("productId") Long productId,
                                      Pageable pageable);

}
