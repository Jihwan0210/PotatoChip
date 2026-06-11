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
        return productRankingsRepository.findByPeriodTypeAndPeriodDateOrderByRankAsc (
            PeriodType.WEEKLY, //기간 유형
                LocalDate.now()  //조회 날짜
        );
    }


    @Override
    public List<ProductRankings> getDailyRanking() {
        return productRankingsRepository.findByPeriodTypeAndPeriodDateOrderByRankAsc (
                PeriodType.DAILY, //기간 유형
                LocalDate.now() //조회 날짜
        );
    }
}
