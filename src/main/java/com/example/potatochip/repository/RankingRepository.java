package com.example.potatochip.repository;

import com.example.potatochip.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RankingRepository extends JpaRepository<OrderItem,Long> {

    @Query(value = """
       SELECT
           product_id,
           SUM(quantity) AS total_sales
       FROM order_item
       GROUP BY product_id
       GROUP BY total_sales DESC
    """,nativeQuery = true)
    List<Object[]>getSalesRanking();
}
