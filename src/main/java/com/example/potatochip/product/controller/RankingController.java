package com.example.potatochip.product.controller;

import com.example.potatochip.product.dto.ProductDTO;
import com.example.potatochip.product.service.ranking.ProductRankingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class RankingController {

    private final ProductRankingsService productRankingsService;

    @GetMapping("/ranking")
    public String ranking(Model model) {
        model.addAttribute("weeklyRankings", productRankingsService.getWeeklyRanking());
        model.addAttribute("dailyRankings", productRankingsService.getDailyRanking());
        return "product/ranking";
    }


}