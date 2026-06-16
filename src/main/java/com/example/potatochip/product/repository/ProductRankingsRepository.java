package com.example.potatochip.product.repository;

import com.example.potatochip.product.entity.ranking.PeriodType;
import com.example.potatochip.product.entity.ranking.ProductRankings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductRankingsRepository extends JpaRepository<ProductRankings , Long> {

    // 기간 유형(daily/weekly)과 날짜로 조회 후 순위 오름차순 정렬
    List<ProductRankings> findByPeriodTypeAndPeriodDateOrderByRankAsc(
            PeriodType periodType , // 기간 유형 (daily or weekly)
            LocalDate periodDate //조회 기간 날짜
    );

    Optional<ProductRankings> findTopByPeriodTypeOrderByPeriodDateDesc(PeriodType periodType);
}
