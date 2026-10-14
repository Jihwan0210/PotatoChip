package com.example.potatochip.order;

import com.example.potatochip.auth.entity.Role;
import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 재고 차감 방식별 성능 비교. 실제 MariaDB가 필요해 BENCH_DB_URL 이 있을 때만 실행된다.
 * 예) BENCH_DB_URL=jdbc:mariadb://127.0.0.1:13306/bench BENCH_DB_PASSWORD=bench ./gradlew test --tests "*StockStrategyBenchmarkTest"
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@EnabledIfEnvironmentVariable(named = "BENCH_DB_URL", matches = ".+")
class StockStrategyBenchmarkTest {

    private static final int THREADS = 50;
    private static final int RUNS = 3;
    private static final int MAX_RETRY = 10;

    @DynamicPropertySource
    static void db(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> System.getenv("BENCH_DB_URL"));
        r.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
        r.add("spring.datasource.username", () -> System.getenv().getOrDefault("BENCH_DB_USER", "root"));
        r.add("spring.datasource.password", () -> System.getenv().getOrDefault("BENCH_DB_PASSWORD", ""));
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.sql.init.mode", () -> "never");
    }

    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager txManager;
    @Autowired DataSource dataSource;

    private TransactionTemplate tx;

    interface Strategy {
        boolean order(Long productId, int holdMs, AtomicInteger retries, AtomicInteger giveUps);
    }

    record Result(String name, int requests, int success, int errors, int giveUps, int retries,
                  double tps, double avgMs, double p95Ms, int stockLeft, int stock) {
        int oversell() { return success - (stock - stockLeft); }
    }

    private final java.util.Set<String> errorTypes = java.util.concurrent.ConcurrentHashMap.newKeySet();

    @Test
    void 재고_차감_방식별_성능_비교() throws Exception {
        tx = new TransactionTemplate(txManager);

        Strategy conditional = (id, hold, retries, giveUps) -> Boolean.TRUE.equals(tx.execute(s -> {
            int updated = productRepository.decreaseStock(id, 1);
            sleep(hold);
            return updated == 1;
        }));

        Strategy pessimistic = (id, hold, retries, giveUps) -> Boolean.TRUE.equals(tx.execute(s -> {
            Product p = em.find(Product.class, id, LockModeType.PESSIMISTIC_WRITE);
            if (p.getStockQuantity() < 1) return false;
            p.setStockQuantity(p.getStockQuantity() - 1);
            sleep(hold);
            return true;
        }));

        // @Version 과 같은 원리: 읽은 값이 그대로일 때만 갱신하고, 바뀌었으면 처음부터 다시 시도
        // MariaDB 11.6+ 는 충돌 시 0행 대신 OptimisticLockException 을 던지므로 둘 다 충돌로 본다
        Strategy optimistic = (id, hold, retries, giveUps) -> {
            for (int attempt = 0; attempt <= MAX_RETRY; attempt++) {
                Integer r;
                try {
                    r = tx.execute(s -> {
                        Integer cur = em.createQuery("select p.stockQuantity from Product p where p.id = :id", Integer.class)
                                .setParameter("id", id).getSingleResult();
                        if (cur < 1) return -1;
                        sleep(hold);
                        return em.createQuery("update Product p set p.stockQuantity = :next where p.id = :id and p.stockQuantity = :cur")
                                .setParameter("next", cur - 1).setParameter("id", id).setParameter("cur", cur)
                                .executeUpdate();
                    });
                } catch (jakarta.persistence.OptimisticLockException
                         | org.springframework.dao.ConcurrencyFailureException conflict) {
                    r = 0;
                }
                if (r == -1) return false;
                if (r == 1) return true;
                retries.incrementAndGet();
            }
            giveUps.incrementAndGet();
            return false;
        };

        // 개선 전 코드: 락 없이 읽고 애플리케이션에서 빼서 저장
        Strategy readModifyWrite = (id, hold, retries, giveUps) -> Boolean.TRUE.equals(tx.execute(s -> {
            Product p = em.find(Product.class, id);
            if (p.getStockQuantity() < 1) return false;
            p.setStockQuantity(p.getStockQuantity() - 1);
            sleep(hold);
            return true;
        }));

        String[] names = {"개선 전 (락 없음)", "조건부 UPDATE", "비관적 락", "낙관적 락"};
        Strategy[] strategies = {readModifyWrite, conditional, pessimistic, optimistic};

        StringBuilder out = new StringBuilder();
        String version = tx.execute(s -> (String) em.createNativeQuery("select version()").getSingleResult());
        out.append("MariaDB ").append(version)
                .append(", 동시 요청 스레드 ").append(THREADS)
                .append(", 커넥션 풀 ").append(((HikariDataSource) dataSource).getMaximumPoolSize())
                .append(", 낙관적 락 최대 재시도 ").append(MAX_RETRY)
                .append(", 각 ").append(RUNS).append("회 측정 중앙값\n\n");

        // 워밍업
        for (Strategy s : strategies) run("warmup", s, 200, 200, 0);

        scenario(out, "A. 재고 차감만 (재고 2000개, 2000건 동시 요청)", names, strategies, 2000, 2000, 0);
        scenario(out, "B. 같은 트랜잭션에서 5ms 추가 작업 가정 (재고 300개, 300건 동시 요청)", names, strategies, 300, 300, 5);

        out.append("발생한 오류 종류: ").append(errorTypes.isEmpty() ? "없음" : String.join(" / ", errorTypes)).append("\n");
        System.out.println(out);
        Path file = Path.of("build", "bench-stock-strategy.md");
        Files.createDirectories(file.getParent());
        Files.writeString(file, out.toString());
    }

    private void scenario(StringBuilder out, String title, String[] names, Strategy[] strategies,
                          int stock, int requests, int holdMs) throws InterruptedException {
        List<List<Result>> byStrategy = new ArrayList<>();
        for (int i = 0; i < strategies.length; i++) byStrategy.add(new ArrayList<>());

        for (int run = 0; run < RUNS; run++) {
            for (int k = 0; k < strategies.length; k++) {
                int i = (k + run) % strategies.length; // 실행 순서에 따른 편향을 줄이려고 순서를 돌린다
                byStrategy.get(i).add(run(names[i], strategies[i], stock, requests, holdMs));
            }
        }

        out.append("### ").append(title).append("\n\n")
                .append("| 방식 | 처리량(건/초) | 평균 응답(ms) | p95 응답(ms) | 성공 | 초과 판매 | 오류로 실패 | 재시도 | 재시도 초과 실패 | 남은 재고 |\n")
                .append("|---|---|---|---|---|---|---|---|---|---|\n");
        for (int i = 0; i < strategies.length; i++) {
            List<Result> rs = byStrategy.get(i);
            Result m = rs.stream().sorted((a, b) -> Double.compare(a.tps(), b.tps())).toList().get(rs.size() / 2);
            out.append(String.format("| %s | %.0f | %.1f | %.1f | %d/%d | %d | %d | %d | %d | %d |%n",
                    names[i], m.tps(), m.avgMs(), m.p95Ms(), m.success(), m.requests(),
                    m.oversell(), m.errors(), m.retries(), m.giveUps(), m.stockLeft()));
        }
        out.append("\n");
    }

    private Result run(String name, Strategy strategy, int stock, int requests, int holdMs) throws InterruptedException {
        Long productId = createProduct(stock);
        AtomicInteger retries = new AtomicInteger();
        AtomicInteger giveUps = new AtomicInteger();
        AtomicInteger success = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();
        AtomicInteger idx = new AtomicInteger();
        long[] latency = new long[requests];

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(requests);
        for (int i = 0; i < requests; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    long t0 = System.nanoTime();
                    boolean ok = strategy.order(productId, holdMs, retries, giveUps);
                    latency[idx.getAndIncrement()] = System.nanoTime() - t0;
                    if (ok) success.incrementAndGet();
                } catch (Exception e) {
                    errors.incrementAndGet();
                    Throwable root = e;
                    while (root.getCause() != null) root = root.getCause();
                    String msg = String.valueOf(root.getMessage()).replaceAll("\\(conn=\\d+\\) ", "");
                    errorTypes.add(name + ": " + msg.substring(0, Math.min(70, msg.length())));
                } finally {
                    done.countDown();
                }
            });
        }
        long t0 = System.nanoTime();
        start.countDown();
        done.await();
        long elapsed = System.nanoTime() - t0;
        pool.shutdown();

        long[] lat = Arrays.copyOf(latency, idx.get());
        Arrays.sort(lat);
        double avgMs = Arrays.stream(lat).average().orElse(0) / 1_000_000.0;
        double p95Ms = lat.length == 0 ? 0 : lat[(int) Math.ceil(lat.length * 0.95) - 1] / 1_000_000.0;
        Integer left = tx.execute(s -> em.find(Product.class, productId).getStockQuantity());

        return new Result(name, requests, success.get(), errors.get(), giveUps.get(), retries.get(),
                requests / (elapsed / 1_000_000_000.0), avgMs, p95Ms, left, stock);
    }

    private Long createProduct(int stock) {
        return tx.execute(status -> {
            User seller = new User();
            seller.setEmail("bench" + System.nanoTime() + "@test.com");
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
            p.setName("벤치감자");
            p.setPrice(BigDecimal.valueOf(1000));
            p.setOrigin("강원");
            p.setExpiryDate(LocalDate.now().plusDays(7));
            p.setIsPickupAvailable(false);
            p.setStockQuantity(stock);
            productRepository.save(p);
            return p.getId();
        });
    }

    private static void sleep(int ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
