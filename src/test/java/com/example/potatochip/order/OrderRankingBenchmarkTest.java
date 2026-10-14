package com.example.potatochip.order;

import com.example.potatochip.auth.entity.Role;
import com.example.potatochip.auth.entity.User;
import com.example.potatochip.auth.repository.UserRepository;
import com.example.potatochip.cart.entity.Cart;
import com.example.potatochip.cart.repository.CartRepository;
import com.example.potatochip.cartitem.entity.CartItem;
import com.example.potatochip.order.dto.OrderRequestDTO;
import com.example.potatochip.order.entity.Order;
import com.example.potatochip.order.entity.OrderItem;
import com.example.potatochip.order.entity.OrderStatus;
import com.example.potatochip.order.repository.OrderRepository;
import com.example.potatochip.order.service.OrderService;
import com.example.potatochip.product.entity.Product;
import com.example.potatochip.product.repository.ProductRepository;
import com.example.potatochip.product.service.ranking.ProductRankingsService;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 주문 흐름 전체(장바구니 → 주문 → 랭킹)의 처리 시간을 이번 주 판매 상품 수별로 측정한다.
 * 실제 MariaDB가 필요해 BENCH_DB_URL 이 있을 때만 실행된다. BENCH_LABEL 로 결과 파일 이름을 구분한다.
 * 예) BENCH_DB_URL=jdbc:mariadb://127.0.0.1:13306/bench BENCH_DB_PASSWORD=bench BENCH_LABEL=before
 *     ./gradlew cleanTest test --tests "*OrderRankingBenchmarkTest"
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "BENCH_DB_URL", matches = ".+")
class OrderRankingBenchmarkTest {

    private static final int SEQUENTIAL_ORDERS = 20;
    private static final int WARMUP = 3;
    private static final int CONCURRENT_ORDERS = 200;
    private static final int THREADS = 20;
    private static final int RUNS = 3;

    @DynamicPropertySource
    static void db(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> System.getenv("BENCH_DB_URL"));
        r.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
        r.add("spring.datasource.username", () -> System.getenv().getOrDefault("BENCH_DB_USER", "root"));
        r.add("spring.datasource.password", () -> System.getenv().getOrDefault("BENCH_DB_PASSWORD", ""));
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.sql.init.mode", () -> "never");
        r.add("spring.jpa.properties.hibernate.generate_statistics", () -> "true");
        r.add("logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener", () -> "WARN");
        // 측정 중에 백그라운드 랭킹 갱신이 끼어들지 않게 한다
        r.add("ranking.refresh-delay-ms", () -> "3600000");
    }

    @Autowired OrderService orderService;
    @Autowired ProductRankingsService productRankingsService;
    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;
    @Autowired CartRepository cartRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManagerFactory emf;
    @Autowired PlatformTransactionManager txManager;

    private TransactionTemplate tx;
    private Statistics stats;
    private final AtomicLong userSeq = new AtomicLong();

    record Sequential(int products, double avgMs, double p95Ms, double queriesPerOrder) {}

    record Concurrent(double tps, double avgMs, double p95Ms, int success, int errors, Set<String> errorTypes) {}

    @Test
    void 주문_처리_성능() throws Exception {
        tx = new TransactionTemplate(txManager);
        stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);

        String label = System.getenv().getOrDefault("BENCH_LABEL", "result");
        String version = jdbc.queryForObject("select version()", String.class);
        StringBuilder out = new StringBuilder();
        out.append("# ").append(label).append("\n\n")
                .append("MariaDB ").append(version).append(", 커넥션 풀 10\n\n");

        out.append("### 주문 1건 처리 (순차 ").append(SEQUENTIAL_ORDERS).append("건, 이번 주 판매 상품 수별)\n\n")
                .append("| 이번 주 판매 상품 수 | 평균(ms) | p95(ms) | 주문 1건당 쿼리 수 |\n|---|---|---|---|\n");
        for (int n : new int[]{10, 100, 300}) {
            Sequential s = sequential(n);
            out.append(String.format("| %d | %.1f | %.1f | %.0f |%n", n, s.avgMs(), s.p95Ms(), s.queriesPerOrder()));
        }

        out.append("\n### 인기 상품 동시 주문 (판매 상품 100개, ").append(THREADS).append("스레드, ")
                .append(CONCURRENT_ORDERS).append("건, ").append(RUNS).append("회 중앙값)\n\n")
                .append("| 처리량(건/초) | 평균(ms) | p95(ms) | 성공 | 오류로 실패 |\n|---|---|---|---|---|\n");
        List<Concurrent> runs = new ArrayList<>();
        for (int i = 0; i < RUNS; i++) runs.add(concurrent(100));
        Concurrent m = runs.stream().sorted(Comparator.comparingDouble(Concurrent::tps)).toList().get(RUNS / 2);
        out.append(String.format("| %.1f | %.1f | %.1f | %d/%d | %d |%n",
                m.tps(), m.avgMs(), m.p95Ms(), m.success(), CONCURRENT_ORDERS, m.errors()));
        Set<String> allErrors = ConcurrentHashMap.newKeySet();
        runs.forEach(r -> allErrors.addAll(r.errorTypes()));
        out.append("\n오류 종류: ").append(allErrors.isEmpty() ? "없음" : String.join(" / ", allErrors)).append("\n");

        out.append("\n### 랭킹 갱신 1회 비용\n\n| 이번 주 판매 상품 수 | 소요(ms) | 쿼리 수 |\n|---|---|---|\n");
        for (int n : new int[]{10, 100, 300}) {
            double[] c = refreshCost(n);
            out.append(String.format("| %d | %.1f | %.0f |%n", n, c[0], c[1]));
        }

        System.out.println(out);
        Path file = Path.of("build", "bench-order-ranking-" + label + ".md");
        Files.createDirectories(file.getParent());
        Files.writeString(file, out.toString());
    }

    private Sequential sequential(int n) {
        reset();
        List<Long> products = seedProducts(n);
        List<Long> pids = new ArrayList<>();
        for (int i = 0; i < SEQUENTIAL_ORDERS + WARMUP; i++) pids.add(products.get(i % n));
        List<Long> buyers = prepareCarts(pids);

        long[] lat = new long[SEQUENTIAL_ORDERS];
        long queries = 0;
        for (int i = 0; i < buyers.size(); i++) {
            long q0 = stats.getPrepareStatementCount();
            long t0 = System.nanoTime();
            orderService.OrderFromCart(buyers.get(i), request());
            long dt = System.nanoTime() - t0;
            long dq = stats.getPrepareStatementCount() - q0;
            if (i >= WARMUP) {
                lat[i - WARMUP] = dt;
                queries += dq;
            }
        }
        Arrays.sort(lat);
        return new Sequential(n, avgMs(lat), p95Ms(lat), (double) queries / SEQUENTIAL_ORDERS);
    }

    private Concurrent concurrent(int n) throws InterruptedException {
        reset();
        List<Long> products = seedProducts(n);
        Long hot = products.get(0);
        List<Long> pids = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_ORDERS; i++) pids.add(hot);
        List<Long> buyers = prepareCarts(pids);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger errors = new AtomicInteger();
        AtomicInteger idx = new AtomicInteger();
        Set<String> errorTypes = ConcurrentHashMap.newKeySet();
        long[] latency = new long[CONCURRENT_ORDERS];

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(CONCURRENT_ORDERS);
        for (Long buyer : buyers) {
            pool.submit(() -> {
                long t0 = 0;
                try {
                    start.await();
                    t0 = System.nanoTime();
                    orderService.OrderFromCart(buyer, request());
                    success.incrementAndGet();
                } catch (Exception e) {
                    errors.incrementAndGet();
                    Throwable root = e;
                    while (root.getCause() != null) root = root.getCause();
                    String msg = String.valueOf(root.getMessage()).replaceAll("\\(conn=\\d+\\) ", "");
                    errorTypes.add(msg.substring(0, Math.min(70, msg.length())));
                } finally {
                    if (t0 > 0) latency[idx.getAndIncrement()] = System.nanoTime() - t0;
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
        return new Concurrent(CONCURRENT_ORDERS / (elapsed / 1_000_000_000.0), avgMs(lat), p95Ms(lat),
                success.get(), errors.get(), errorTypes);
    }

    private double[] refreshCost(int n) {
        reset();
        seedProducts(n);
        productRankingsService.refreshRankings();
        int times = 5;
        long q0 = stats.getPrepareStatementCount();
        long t0 = System.nanoTime();
        for (int i = 0; i < times; i++) productRankingsService.refreshRankings();
        long elapsed = System.nanoTime() - t0;
        long dq = stats.getPrepareStatementCount() - q0;
        return new double[]{elapsed / 1_000_000.0 / times, (double) dq / times};
    }

    /** 주문·랭킹·장바구니를 비워 이번 주 판매 상품 수를 정확히 맞춘다 */
    private void reset() {
        jdbc.execute((ConnectionCallback<Void>) con -> {
            try (Statement st = con.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS=0");
                for (String t : List.of("order_items", "orders", "product_rankings", "cart_items", "carts")) {
                    st.execute("TRUNCATE TABLE " + t);
                }
                st.execute("SET FOREIGN_KEY_CHECKS=1");
            }
            return null;
        });
    }

    /** 상품 n개를 만들고, 이번 주에 각각 1개씩 팔린 주문을 넣는다 */
    private List<Long> seedProducts(int n) {
        return tx.execute(s -> {
            User seller = newUser(Role.SELLER);
            List<Product> products = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                Product p = new Product();
                p.setSeller(seller);
                p.setCategory("채소");
                p.setName("벤치상품" + i);
                p.setPrice(BigDecimal.valueOf(1000));
                p.setOrigin("강원");
                p.setExpiryDate(LocalDate.now().plusDays(7));
                p.setIsPickupAvailable(false);
                p.setStockQuantity(1_000_000);
                products.add(productRepository.save(p));
            }
            Order past = Order.builder()
                    .buyerId(seller.getId())
                    .orderNumber("SEED-" + System.nanoTime())
                    .totalAmount(BigDecimal.ZERO)
                    .status(OrderStatus.PAYMENT_COMPLETE)
                    .build();
            for (Product p : products) {
                past.addOrderItem(OrderItem.builder()
                        .productId(p.getId())
                        .sellerId(seller.getId())
                        .quantity(1)
                        .price(BigDecimal.valueOf(1000))
                        .build());
            }
            orderRepository.save(past);
            return products.stream().map(Product::getId).toList();
        });
    }

    /** 주문 1건마다 구매자 1명과 장바구니 1개를 미리 만든다 (측정 시간에는 포함하지 않음) */
    private List<Long> prepareCarts(List<Long> productIds) {
        return tx.execute(s -> {
            List<Long> buyers = new ArrayList<>();
            for (Long pid : productIds) {
                User buyer = newUser(Role.BUYER);
                Cart cart = Cart.builder().buyerId(buyer.getId()).build();
                cart.addItem(CartItem.builder().productId(pid).quantity(1).build());
                cartRepository.save(cart);
                buyers.add(buyer.getId());
            }
            return buyers;
        });
    }

    private User newUser(Role role) {
        User u = new User();
        u.setEmail("bench" + userSeq.incrementAndGet() + "_" + System.nanoTime() + "@test.com");
        u.setPassword("pw");
        u.setName("벤치");
        u.setRole(role);
        u.setPushAgree(true);
        u.setEmailAgree(true);
        u.setIsActive(true);
        u.setPoints(0);
        return userRepository.save(u);
    }

    private static OrderRequestDTO request() {
        OrderRequestDTO r = new OrderRequestDTO();
        r.setShippingAddress("서울");
        r.setPaymentMethod("card");
        r.setDeliveryType("delivery");
        r.setShippingFee(BigDecimal.ZERO);
        r.setPointUsed(0);
        return r;
    }

    private static double avgMs(long[] lat) {
        return Arrays.stream(lat).average().orElse(0) / 1_000_000.0;
    }

    private static double p95Ms(long[] sorted) {
        return sorted.length == 0 ? 0 : sorted[(int) Math.ceil(sorted.length * 0.95) - 1] / 1_000_000.0;
    }
}
