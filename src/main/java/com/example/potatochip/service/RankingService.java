package com.example.potatochip.service;


import com.example.potatochip.repository.RankingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RankingService {

    private final RankingRepository rankingRepository;

    public RankingService(RankingRepository rankingRepository) {
        this.rankingRepository = rankingRepository;
    }

    public List<Object[]> getRanking() {
        return rankingRepository.getSalesRanking();
    }
}
