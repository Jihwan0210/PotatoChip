package com.example.potatochip.product.repository;

import com.example.potatochip.product.entity.ranking.PeriodType;
import com.example.potatochip.product.entity.ranking.ProductRankings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductRankingsRepository extends JpaRepository<ProductRankings , Long> {

    List<ProductRankings> findByPeriodTypeAndPeriodDateOrderByRankAsc(
            PeriodType periodType,
            LocalDate periodDate
    );

    Optional<ProductRankings> findTopByPeriodTypeOrderByPeriodDateDesc(PeriodType periodType);

    @Modifying
    @Query("DELETE FROM ProductRankings p WHERE p.periodType = :periodType AND p.periodDate = :periodDate")
    void deleteByPeriodTypeAndPeriodDate(@Param("periodType") PeriodType periodType,
                                         @Param("periodDate") LocalDate periodDate);
}
