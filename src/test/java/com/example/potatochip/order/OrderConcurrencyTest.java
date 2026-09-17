package com.example.potatochip.order;

import com.example.potatochip.auth.entity.Role;
import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 재고 차감 동시성 검증.
 * @Transactional(NOT_SUPPORTED): 테스트 자동 트랜잭션을 꺼서
 *   준비 데이터가 실제로 커밋되어 여러 스레드가 볼 수 있게 함.
 */
@DataJpaTest
@TestPropertySource(properties = "spring.sql.init.mode=never") // 더미 데이터 로딩 끔 (테스트는 빈 H2에서 시작)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OrderConcurrencyTest {

    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;
    @Autowired PlatformTransactionManager txManager;

    private TransactionTemplate tx;

    /** 재고 stock개짜리 상품 1개를 만들고 커밋, id 반환 */
    private Long createProductWithStock(int stock) {
        tx = new TransactionTemplate(txManager);
        return tx.execute(status -> {
            User seller = new User();
            seller.setEmail("seller" + System.nanoTime() + "@test.com");
            seller.setPassword("pw");
            seller.setName("판매자");
            seller.setRole(Role.SELLER);
            seller.setPushAgree(true);
            seller.setEmailAgree(true);
            seller.setIsActive(true);
            seller.setPoints(0);
            userRepository.save(seller);

            Product p = new Product();
            p.setSeller(seller);
            p.setCategory("채소");
            p.setName("테스트감자");
            p.setPrice(BigDecimal.valueOf(1000));
            p.setOrigin("강원");
            p.setExpiryDate(LocalDate.now().plusDays(7));
            p.setIsPickupAvailable(false);
            p.setStockQuantity(stock);
            productRepository.save(p);
            return p.getId();
        });
    }

    @Test
    void 개선_조건부UPDATE_오버셀_0() throws InterruptedException {
        int stock = 100;
        int threads = 300;
        Long productId = createProductWithStock(stock);

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    Integer updated = tx.execute(s -> productRepository.decreaseStock(productId, 1));
                    if (updated != null && updated == 1) success.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();   // 모든 스레드 동시 출발
        done.await();
        pool.shutdown();

        int finalStock = tx.execute(s ->
                productRepository.findById(productId).orElseThrow().getStockQuantity());
        System.out.println("[개선] 재고=" + stock + " 동시요청=" + threads
                + " 주문성공=" + success.get() + " 최종재고=" + finalStock);

        assertThat(success.get()).isEqualTo(stock); // 딱 재고만큼만 성공
        assertThat(finalStock).isEqualTo(0);        // 음수 절대 안 됨
    }

    @Test
    void 기존_readModifyWrite_오버셀_발생() throws InterruptedException {
        int stock = 100;
        int threads = 300;
        Long productId = createProductWithStock(stock);

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    Boolean ok = tx.execute(s -> {
                        Product p = productRepository.findById(productId).orElseThrow();
                        if (p.getStockQuantity() >= 1) {          // 확인
                            p.setStockQuantity(p.getStockQuantity() - 1); // 자바에서 빼고
                            productRepository.save(p);            // 저장 (read-modify-write)
                            return true;
                        }
                        return false;
                    });
                    if (Boolean.TRUE.equals(ok)) success.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        pool.shutdown();

        int finalStock = tx.execute(s ->
                productRepository.findById(productId).orElseThrow().getStockQuantity());
        int oversell = Math.max(success.get() - stock, 0);
        System.out.println("[기존] 재고=" + stock + " 동시요청=" + threads
                + " 주문성공=" + success.get() + " 오버셀=" + oversell + " 최종재고=" + finalStock);
        // 데모: 기존 방식은 오버셀/락경합 발생. 수치는 환경마다 달라 하드 단언은 하지 않음.
    }
}
