package com.example.potatochip.service;

import com.example.potatochip.dto.response.RankingResponseDTO;
import com.example.potatochip.repository.RankingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class RankingService {

    private final RankingRepository rankingRepository;

    public RankingService(RankingRepository rankingRepository) {
        this.rankingRepository = rankingRepository;
    }

    public List<RankingResponseDTO> getRanking() {
        AtomicInteger rank = new AtomicInteger(1);

        return rankingRepository.getSalesRanking()
                .stream()
                .map(obj -> {
                    RankingResponseDTO dto = new RankingResponseDTO();

                    dto.setProductName((String) obj[0]);
                    dto.setTotalSales(((Number) obj[1]).longValue());
                    dto.setRank(rank.getAndIncrement());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<RankingResponseDTO> getTopRanking(int count) {
        return getRanking()
                .stream()
                .limit(count)
                .collect(Collectors.toList());
    }
}

