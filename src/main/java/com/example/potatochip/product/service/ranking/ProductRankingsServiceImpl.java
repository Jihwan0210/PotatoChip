package com.example.potatochip.product.service.ranking;

import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.order.repository.OrderItemRepository;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.entity.ranking.PeriodType;
import com.example.potatochip.product.entity.ranking.ProductRankings;
import com.example.potatochip.product.repository.ProductRepository;
import com.example.potatochip.product.repository.ProductRankingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductRankingsServiceImpl implements ProductRankingsService {

    private final ProductRankingsRepository productRankingsRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public List<ProductRankings> getDailyRanking() {
        LocalDate today = LocalDate.now();
        return productRankingsRepository
                .findByPeriodTypeAndPeriodDateOrderByRankAsc(PeriodType.DAILY, today);
    }

    @Override
    public List<ProductRankings> getWeeklyRanking() {
        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        return productRankingsRepository
                .findByPeriodTypeAndPeriodDateOrderByRankAsc(PeriodType.WEEKLY, weekStart);
    }

    @Override
    @Transactional
    public void refreshRankings() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

        // 일간 갱신
        productRankingsRepository.deleteByPeriodTypeAndPeriodDate(PeriodType.DAILY, today);
        List<Object[]> dailyResults = orderItemRepository.findSalesCountByProductBetween(
                today.atStartOfDay(), dayEnd);
        saveRankings(dailyResults, PeriodType.DAILY, today);

        // 주간 갱신
        productRankingsRepository.deleteByPeriodTypeAndPeriodDate(PeriodType.WEEKLY, weekStart);
        List<Object[]> weeklyResults = orderItemRepository.findSalesCountByProductBetween(
                weekStart.atStartOfDay(), dayEnd);
        saveRankings(weeklyResults, PeriodType.WEEKLY, weekStart);
    }

    private void saveRankings(List<Object[]> results, PeriodType periodType, LocalDate periodDate) {
        int rank = 1;
        for (Object[] row : results) {
            Long productId = ((Number) row[0]).longValue();
            Long sellerId  = ((Number) row[1]).longValue();
            int salesCount = ((Number) row[2]).intValue();

            Product product = productRepository.findById(productId).orElse(null);
            User seller     = userRepository.findById(sellerId).orElse(null);
            if (product == null || seller == null) continue;

            ProductRankings r = new ProductRankings();
            r.setProduct(product);
            r.setSeller(seller);
            r.setPeriodType(periodType);
            r.setPeriodDate(periodDate);
            r.setSalesCount(salesCount);
            r.setRank(rank++);
            productRankingsRepository.save(r);
        }
    }
}
