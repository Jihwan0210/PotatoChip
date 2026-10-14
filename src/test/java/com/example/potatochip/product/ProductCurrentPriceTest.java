package com.example.potatochip.product;

import com.example.potatochip.product.entity.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCurrentPriceTest {

    private static final LocalDate TODAY = LocalDate.now(ZoneId.of("Asia/Seoul"));

    private Product product(LocalDate start, LocalDate end) {
        Product p = new Product();
        p.setPrice(BigDecimal.valueOf(5000));
        p.setDiscountPrice(BigDecimal.valueOf(3000));
        p.setDiscountStartAt(start);
        p.setDiscountEndAt(end);
        return p;
    }

    @Test
    void 할인_기간_중이면_할인가() {
        assertThat(product(TODAY.minusDays(1), TODAY.plusDays(1)).currentPrice()).isEqualByComparingTo("3000");
    }

    @Test
    void 할인_시작_전이면_정가() {
        assertThat(product(TODAY.plusDays(1), TODAY.plusDays(3)).currentPrice()).isEqualByComparingTo("5000");
    }

    @Test
    void 할인_종료_후면_정가() {
        assertThat(product(TODAY.minusDays(3), TODAY.minusDays(1)).currentPrice()).isEqualByComparingTo("5000");
    }

    @Test
    void 종료일_당일까지는_할인가() {
        assertThat(product(TODAY.minusDays(1), TODAY).currentPrice()).isEqualByComparingTo("3000");
    }

    @Test
    void 할인_기간_밖이면_적용_할인가는_없음() {
        assertThat(product(TODAY.minusDays(3), TODAY.minusDays(1)).getActiveDiscountPrice()).isNull();
        assertThat(product(TODAY.minusDays(1), TODAY.plusDays(1)).getActiveDiscountPrice()).isEqualByComparingTo("3000");
    }

    @Test
    void 할인_상태() {
        assertThat(product(TODAY.plusDays(1), TODAY.plusDays(3)).getDiscountStatus()).isEqualTo("할인 예정");
        assertThat(product(TODAY.minusDays(1), TODAY.plusDays(1)).getDiscountStatus()).isEqualTo("할인 중");
        assertThat(product(TODAY.minusDays(3), TODAY.minusDays(1)).getDiscountStatus()).isEqualTo("할인 종료");
        Product noDiscount = product(null, null);
        noDiscount.setDiscountPrice(null);
        assertThat(noDiscount.getDiscountStatus()).isNull();
    }

    @Test
    void 할인가가_없으면_정가() {
        Product p = product(null, null);
        p.setDiscountPrice(null);
        assertThat(p.currentPrice()).isEqualByComparingTo("5000");
    }
}
