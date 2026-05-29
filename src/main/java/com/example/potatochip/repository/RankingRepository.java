package com.example.potatochip.repository;

import com.example.potatochip.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RankingRepository extends JpaRepository<OrderItem,Long> {

    @Query(value = """
       SELECT
           p.product_name,
           SUM(quantity) AS total_sales
       FROM order_item o
       JOIN product p 
       ON o.product_id = p.product_id
       GROUP BY p.product_name
       ORDER BY total_sales DESC
       """,nativeQuery = true)
    List<Object[]> getSalesRanking();
}
