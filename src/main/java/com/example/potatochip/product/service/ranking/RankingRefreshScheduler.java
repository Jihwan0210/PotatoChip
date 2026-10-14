package com.example.potatochip.product.service.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankingRefreshScheduler {

    private final ProductRankingsService productRankingsService;

    // 주문마다 다시 계산하던 판매 랭킹을 1분마다 한 번만 갱신한다
    @Scheduled(fixedDelayString = "${ranking.refresh-delay-ms:60000}",
               initialDelayString = "${ranking.refresh-delay-ms:60000}")
    public void refresh() {
        productRankingsService.refreshRankings();
    }
}
