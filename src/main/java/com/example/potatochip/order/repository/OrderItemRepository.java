package com.example.potatochip.order.repository;

import com.example.potatochip.order.entity.OrderItem;
import com.example.potatochip.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByStatusAndUpdatedAtBefore(OrderStatus status, LocalDateTime updatedAt);

    @Query("SELECT oi.productId, oi.sellerId, SUM(oi.quantity) " +
           "FROM OrderItem oi " +
           "WHERE oi.createdAt >= :start AND oi.createdAt < :end " +
           "GROUP BY oi.productId, oi.sellerId " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findSalesCountByProductBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
