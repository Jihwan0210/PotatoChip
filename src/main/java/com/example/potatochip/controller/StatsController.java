package com.example.potatochip.controller;


import com.example.potatochip.dto.response.StatsResponseDTO;
import com.example.potatochip.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "통계 API", description = "관리자 통계 조회")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @Operation(
            summary = "통계 조회",
            description = "상품 수, 주문 수, 총 판매량을 조회합니다."
    )
    @GetMapping("/stats")
    public StatsResponseDTO getStats() {
        return statsService.getStats();
    }
}
