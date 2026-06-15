package com.example.potatochip.product.service.ranking;


import com.example.potatochip.product.entity.ranking.PeriodType;
import com.example.potatochip.product.entity.ranking.ProductRankings;
import com.example.potatochip.product.repository.ProductRankingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductRankingsServiceImpl implements ProductRankingsService {

    private final ProductRankingsRepository productRankingsRepository;

    @Override
    public List<ProductRankings> getWeeklyRanking() {
        LocalDate latest = productRankingsRepository
                .findTopByPeriodTypeOrderByPeriodDateDesc(PeriodType.WEEKLY)
                .map(r -> r.getPeriodDate())
                .orElse(LocalDate.now());
        return productRankingsRepository
                .findByPeriodTypeAndPeriodDateOrderByRankAsc(PeriodType.WEEKLY, latest);
    }

    @Override
    public List<ProductRankings> getDailyRanking() {
        LocalDate latest = productRankingsRepository
                .findTopByPeriodTypeOrderByPeriodDateDesc(PeriodType.DAILY)
                .map(r -> r.getPeriodDate())
                .orElse(LocalDate.now());
        return productRankingsRepository
                .findByPeriodTypeAndPeriodDateOrderByRankAsc(PeriodType.DAILY, latest);
    }
}
