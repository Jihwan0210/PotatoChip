package com.example.potatochip.product.service.ranking;


import com.example.potatochip.product.entity.ranking.ProductRankings;

import java.util.List;

public interface ProductRankingsService {
    List<ProductRankings> getWeeklyRanking();
    List<ProductRankings> getDailyRanking();
    void refreshRankings();
}
