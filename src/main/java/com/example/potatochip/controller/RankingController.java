package com.example.potatochip.controller;

import com.example.potatochip.dto.response.RankingResponseDTO;
import com.example.potatochip.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ranking")
@Tag(name = "판매 랭킹 API", description = "상품 판매 랭킹 조회 기능")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @Operation(
            summary = "판매 랭킹 조회",
            description = "판매량 기준으로 상품 랭킹을 조회합니다."
    )

    @GetMapping
    public List<RankingResponseDTO> getRanking() {
        return rankingService.getRanking();
    }

    @Operation(
            summary = "상위 N개 판매 랭킹 조회",
            description = "판매량 기준 상위 N개 상품을 조회합니다."
    )
    @GetMapping("/top/{count}")
    public List<RankingResponseDTO> getTopRanking(
            @PathVariable int count
    ) {
        return rankingService.getTopRanking(count);
    }
}
