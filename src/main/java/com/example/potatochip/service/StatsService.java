package com.example.potatochip.service;

import com.example.potatochip.dto.response.StatsResponseDTO;
import com.example.potatochip.repository.StatsRepository;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    private final StatsRepository statsRepository;

    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public StatsResponseDTO getStats() {

        StatsResponseDTO dto = new StatsResponseDTO();

        dto.setTotalProducts(statsRepository.countProducts());
        dto.setTotalOrders(statsRepository.countOrders());
        dto.setTotalSales(statsRepository.totalSales());

        return dto;
    }
}
