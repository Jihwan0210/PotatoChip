package com.example.potatochip.product.service;


import com.example.potatochip.product.entity.ranking.ProductRankings;

import java.util.List;

public interface ProductRankingsService {
    List<ProductRankings> getWeeklyRanking();
    List<ProductRankings> getDailyRanking();
}
