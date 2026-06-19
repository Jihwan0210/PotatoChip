-- ================================================================
-- 판매자 대시보드 더미 데이터 (변수 없이 서브쿼리 직접 참조)
-- ================================================================
USE potatochip;

-- 이전 더미 데이터 정리 (재실행 시 충돌 방지)
DELETE FROM reviews WHERE user_id IN (SELECT id FROM users WHERE email IN ('dummy_buyer1@test.com','dummy_buyer2@test.com','dummy_buyer3@test.com'));
DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE order_number LIKE 'ORD-2026%');
DELETE FROM orders WHERE order_number LIKE 'ORD-2026%';
DELETE FROM products WHERE seller_id = (SELECT id FROM users WHERE email = 'lee2@naver.com') AND name IN ('못난이 사과 3kg','못난이 당근 2kg','못난이 배 2kg');
DELETE FROM users WHERE email IN ('dummy_buyer1@test.com','dummy_buyer2@test.com','dummy_buyer3@test.com');

-- 더미 구매자 3명
INSERT INTO users (email, password, name, role, push_agree, email_agree, is_active, created_at, updated_at) VALUES
  ('dummy_buyer1@test.com', 'SOCIAL_DUMMY', '김철수', 'BUYER', false, false, true, NOW(), NOW()),
  ('dummy_buyer2@test.com', 'SOCIAL_DUMMY', '이영희', 'BUYER', false, false, true, NOW(), NOW()),
  ('dummy_buyer3@test.com', 'SOCIAL_DUMMY', '박민준', 'BUYER', false, false, true, NOW(), NOW());

-- 상품 3개
INSERT INTO products (seller_id, category, name, description, price, discount_price, origin, expiry_date, is_pickup_available, stock_quantity, created_at, updated_at, address) VALUES
  ((SELECT id FROM users WHERE email = 'lee2@naver.com'), '과일', '못난이 사과 3kg', '흠집이 있지만 맛은 최고! 충주 사과', 15000, 12000, '충청북도 충주', DATE_ADD(CURDATE(), INTERVAL 7 DAY), false, 42, NOW(), NOW(), '충청북도 충주시 농원로 123'),
  ((SELECT id FROM users WHERE email = 'lee2@naver.com'), '채소', '못난이 당근 2kg', '모양이 삐뚤어진 국내산 당근', 8000, 6500, '제주도', DATE_ADD(CURDATE(), INTERVAL 5 DAY), false, 8, NOW(), NOW(), '제주도 서귀포시'),
  ((SELECT id FROM users WHERE email = 'lee2@naver.com'), '과일', '못난이 배 2kg', '작은 배지만 달달해요', 18000, 14000, '충청남도 천안', DATE_ADD(CURDATE(), INTERVAL 3 DAY), false, 0, NOW(), NOW(), '충청남도 천안시');

-- 주문 5개
INSERT INTO orders (order_number, buyer_id, shipping_address, total_amount, total_shipping_fee, payment_method, delivery_type, status, created_at, updated_at) VALUES
  ('ORD-20260618-001', (SELECT id FROM users WHERE email = 'dummy_buyer1@test.com'), '서울시 강남구 테헤란로 123', 15000, 3000, 'CARD', 'delivery', 'PAYMENT_COMPLETE', NOW(), NOW()),
  ('ORD-20260617-002', (SELECT id FROM users WHERE email = 'dummy_buyer2@test.com'), '부산시 해운대구 해변로 456', 8000,  3000, 'CARD', 'delivery', 'PREPARING',        DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
  ('ORD-20260616-003', (SELECT id FROM users WHERE email = 'dummy_buyer3@test.com'), '인천시 남동구 논현로 789',   27000, 3000, 'CARD', 'delivery', 'SHIPPING',         DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
  ('ORD-20260615-004', (SELECT id FROM users WHERE email = 'dummy_buyer1@test.com'), '서울시 강남구 테헤란로 123', 14000, 3000, 'CARD', 'delivery', 'DELIVERED',        DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
  ('ORD-20260614-005', (SELECT id FROM users WHERE email = 'dummy_buyer2@test.com'), '부산시 해운대구 해변로 456', 15000, 3000, 'CARD', 'delivery', 'DELIVERED',        DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY));

-- 주문 아이템
INSERT INTO order_items (order_id, product_id, seller_id, quantity, price, shipping_fee, status, created_at, updated_at) VALUES
  (
    (SELECT id FROM orders WHERE order_number = 'ORD-20260618-001'),
    (SELECT id FROM products WHERE name = '못난이 사과 3kg' AND seller_id = (SELECT id FROM users WHERE email = 'lee2@naver.com') LIMIT 1),
    (SELECT id FROM users WHERE email = 'lee2@naver.com'),
    1, 15000, 3000, 'PAYMENT_COMPLETE', NOW(), NOW()
  ),
  (
    (SELECT id FROM orders WHERE order_number = 'ORD-20260617-002'),
    (SELECT id FROM products WHERE name = '못난이 당근 2kg' AND seller_id = (SELECT id FROM users WHERE email = 'lee2@naver.com') LIMIT 1),
    (SELECT id FROM users WHERE email = 'lee2@naver.com'),
    1, 8000, 3000, 'PREPARING', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)
  ),
  (
    (SELECT id FROM orders WHERE order_number = 'ORD-20260616-003'),
    (SELECT id FROM products WHERE name = '못난이 사과 3kg' AND seller_id = (SELECT id FROM users WHERE email = 'lee2@naver.com') LIMIT 1),
    (SELECT id FROM users WHERE email = 'lee2@naver.com'),
    2, 15000, 3000, 'SHIPPING', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)
  ),
  (
    (SELECT id FROM orders WHERE order_number = 'ORD-20260615-004'),
    (SELECT id FROM products WHERE name = '못난이 배 2kg' AND seller_id = (SELECT id FROM users WHERE email = 'lee2@naver.com') LIMIT 1),
    (SELECT id FROM users WHERE email = 'lee2@naver.com'),
    1, 14000, 3000, 'DELIVERED', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)
  ),
  (
    (SELECT id FROM orders WHERE order_number = 'ORD-20260614-005'),
    (SELECT id FROM products WHERE name = '못난이 사과 3kg' AND seller_id = (SELECT id FROM users WHERE email = 'lee2@naver.com') LIMIT 1),
    (SELECT id FROM users WHERE email = 'lee2@naver.com'),
    1, 15000, 3000, 'DELIVERED', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)
  );

-- 리뷰 3개
INSERT INTO reviews (product_id, user_id, rating, content, repurchase_intent, is_anonymous, is_active, created_at, updated_at, is_hidden) VALUES
  (
    (SELECT id FROM products WHERE name = '못난이 사과 3kg' AND seller_id = (SELECT id FROM users WHERE email = 'lee2@naver.com') LIMIT 1),
    (SELECT id FROM users WHERE email = 'dummy_buyer3@test.com'),
    5, '맛있어요! 모양은 좀 이상하지만 맛은 최고', true, false, true, NOW(), NOW(), false
  ),
  (
    (SELECT id FROM products WHERE name = '못난이 배 2kg' AND seller_id = (SELECT id FROM users WHERE email = 'lee2@naver.com') LIMIT 1),
    (SELECT id FROM users WHERE email = 'dummy_buyer1@test.com'),
    4, '달달하고 좋아요. 다음에도 구매할게요', true, false, true, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), false
  ),
  (
    (SELECT id FROM products WHERE name = '못난이 사과 3kg' AND seller_id = (SELECT id FROM users WHERE email = 'lee2@naver.com') LIMIT 1),
    (SELECT id FROM users WHERE email = 'dummy_buyer2@test.com'),
    5, '가격 대비 최고! 못난이라도 맛은 일품', true, false, true, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), false
  );


SELECT id, email FROM users WHERE role = 'SELLER';
SELECT id, name, price FROM products WHERE seller_id = 9;
SELECT id, name, price FROM products WHERE seller_id = 10;
SELECT id, name, price FROM products WHERE seller_id = 11;




-- ── 1월 ──────────────────────────────────────────
INSERT INTO orders (order_number, buyer_id, shipping_address, total_amount, total_shipping_fee, payment_method, delivery_type, status, created_at, updated_at)
VALUES ('ORD-2601-001', 9, '서울시 강남구 테헤란로 1', 203000, 3000, 'card', 'delivery', 'delivered', '2026-01-05 10:30:00', '2026-01-05 10:30:00');
SET @o1 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, seller_id, quantity, price, shipping_fee, status, created_at, updated_at)
VALUES (@o1, 1, 10, 1, 200000, 3000, 'delivered', '2026-01-05 10:30:00', '2026-01-05 10:30:00');

INSERT INTO orders (order_number, buyer_id, shipping_address, total_amount, total_shipping_fee, payment_method, delivery_type, status, created_at, updated_at)
VALUES ('ORD-2601-002', 9, '서울시 마포구 홍대입구 22', 249246, 3000, 'card', 'delivery', 'delivered', '2026-01-16 14:00:00', '2026-01-16 14:00:00');
SET @o2 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, seller_id, quantity, price, shipping_fee, status, created_at, updated_at)
VALUES (@o2, 2, 10, 2, 123123, 3000, 'delivered', '2026-01-16 14:00:00', '2026-01-16 14:00:00');

INSERT INTO orders (order_number, buyer_id, shipping_address, total_amount, total_shipping_fee, payment_method, delivery_type, status, created_at, updated_at)
VALUES ('ORD-2601-003', 9, '부산시 해운대구 마린시티 5', 203000, 3000, 'card', 'delivery', 'delivered', '2026-01-22 09:15:00', '2026-01-22 09:15:00');
SET @o3 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, seller_id, quantity, price, shipping_fee, status, created_at, updated_at)
VALUES (@o3, 1, 10, 1, 200000, 3000, 'delivered', '2026-01-22 09:15:00', '2026-01-22 09:15:00');

-- ── 2월 ──────────────────────────────────────────
INSERT INTO orders (order_number, buyer_id, shipping_address, total_amount, total_shipping_fee, payment_method, delivery_type, status, created_at, updated_at)
VALUES ('ORD-2602-001', 9, '서울시 서초구 반포대로 10', 126123, 3000, 'card', 'delivery', 'delivered', '2026-02-10 11:00:00', '2026-02-10 11:00:00');
SET @o4 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, seller_id, quantity, price, shipping_fee, status, created_at, updated_at)
VALUES (@o4, 2, 10, 1, 123123, 3000, 'delivered', '2026-02-10 11:00:00', '2026-02-10 11:00:00');

INSERT INTO orders (order_number, buyer_id, shipping_address, total_amount, total_shipping_fee, payment_method, delivery_type, status, created_at, updated_at)
VALUES ('ORD-2602-002', 9, '인천시 연수구 송도대로 7', 403000, 3000, 'card', 'delivery', 'delivered', '2026-02-15 16:30:00', '2026-02-15 16:30:00');
SET @o5 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, seller_id, quantity, price, shipping_fee, status, created_at, updated_at)
VALUES (@o5, 1, 10, 2, 200000, 3000, 'delivered', '2026-02-15 16:30:00', '2026-02-15 16:30:00');

-- ── 3월 ──────────────────────────────────────────
INSERT INTO orders (order_number, buyer_id, shipping_address, total_amount, total_shipping_fee, payment_method, delivery_type, status, created_at, updated_at)
VALUES ('ORD-2603-001', 9, '대전시 유성구 대학로 99', 372369, 3000, 'card', 'delivery', 'delivered', '2026-03-11 13:20:00', '2026-03-11 13:20:00');
SET @o6 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, seller_id, quantity, price, shipping_fee, status, created_at, updated_at)
VALUES (@o6, 2, 10, 3, 123123, 3000, 'delivered', '2026-03-11 13:20:00', '2026-03-11 13:20:00');

INSERT INTO orders (order_number, buyer_id, shipping_address, total_amount, total_shipping_fee, payment_method, delivery_type, status, created_at, updated_at)
VALUES ('ORD-2603-002', 9, '서울시 강남구 삼성로 300', 203000, 3000, 'card', 'delivery', 'delivered', '2026-03-25 10:00:00', '2026-03-25 10:00:00');
SET @o7 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, seller_id, quantity, price, shipping_fee, status, created_at, updated_at)
VALUES (@o7, 1, 10, 1, 200000, 3000, 'delivered', '2026-03-25 10:00:00', '2026-03-25 10:00:00');

-- ── 4월 ──────────────────────────────────────────
INSERT INTO orders (order_number, buyer_id, shipping_address, total_amount, total_shipping_fee, payment_method, delivery_type, status, created_at, updated_at)
VALUES ('ORD-2604-001', 9, '광주시 서구 상무대로 55', 249246, 3000, 'card', 'delivery', 'shipping', '2026-04-07 15:45:00', '2026-04-07 15:45:00');
SET @o8 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, seller_id, quantity, price, shipping_fee, status, created_at, updated_at)
VALUES (@o8, 2, 10, 2, 123123, 3000, 'shipping', '2026-04-07 15:45:00', '2026-04-07 15:45:00');

INSERT INTO orders (order_number, buyer_id, shipping_address, total_amount, total_shipping_fee, payment_method, delivery_type, status, created_at, updated_at)
VALUES ('ORD-2604-002', 9, '서울시 용산구 이태원로 88', 203000, 3000, 'card', 'delivery', 'shipping', '2026-04-17 09:30:00', '2026-04-17 09:30:00');
SET @o9 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, seller_id, quantity, price, shipping_fee, status, created_at, updated_at)
VALUES (@o9, 1, 10, 1, 200000, 3000, 'shipping', '2026-04-17 09:30:00', '2026-04-17 09:30:00');

-- ── 5월 ──────────────────────────────────────────
INSERT INTO orders (order_number, buyer_id, shipping_address, total_amount, total_shipping_fee, payment_method, delivery_type, status, created_at, updated_at)
VALUES ('ORD-2605-001', 9, '서울시 성동구 왕십리로 3', 126123, 3000, 'card', 'delivery', 'preparing', '2026-05-06 12:00:00', '2026-05-06 12:00:00');
SET @o10 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, seller_id, quantity, price, shipping_fee, status, created_at, updated_at)
VALUES (@o10, 2, 10, 1, 123123, 3000, 'preparing', '2026-05-06 12:00:00', '2026-05-06 12:00:00');

INSERT INTO orders (order_number, buyer_id, shipping_address, total_amount, total_shipping_fee, payment_method, delivery_type, status, created_at, updated_at)
VALUES ('ORD-2605-002', 9, '경기도 성남시 분당구 판교로 7', 403000, 3000, 'card', 'delivery', 'payment_complete', '2026-05-20 17:10:00', '2026-05-20 17:10:00');
SET @o11 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, seller_id, quantity, price, shipping_fee, status, created_at, updated_at)
VALUES (@o11, 1, 10, 2, 200000, 3000, 'payment_complete', '2026-05-20 17:10:00', '2026-05-20 17:10:00');

-- ── 6월 ──────────────────────────────────────────
INSERT INTO orders (order_number, buyer_id, shipping_address, total_amount, total_shipping_fee, payment_method, delivery_type, status, created_at, updated_at)
VALUES ('ORD-2606-001', 9, '서울시 종로구 율곡로 10', 126123, 3000, 'card', 'delivery', 'payment_complete', '2026-06-02 10:20:00', '2026-06-02 10:20:00');
SET @o12 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, seller_id, quantity, price, shipping_fee, status, created_at, updated_at)
VALUES (@o12, 2, 10, 1, 123123, 3000, 'payment_complete', '2026-06-02 10:20:00', '2026-06-02 10:20:00');

INSERT INTO orders (order_number, buyer_id, shipping_address, total_amount, total_shipping_fee, payment_method, delivery_type, status, created_at, updated_at)
VALUES ('ORD-2606-002', 9, '대구시 수성구 달구벌대로 200', 203000, 3000, 'card', 'delivery', 'payment_complete', '2026-06-13 14:50:00', '2026-06-13 14:50:00');
SET @o13 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, seller_id, quantity, price, shipping_fee, status, created_at, updated_at)
VALUES (@o13, 1, 10, 1, 200000, 3000, 'payment_complete', '2026-06-13 14:50:00', '2026-06-13 14:50:00');

INSERT INTO orders (order_number, buyer_id, shipping_address, total_amount, total_shipping_fee, payment_method, delivery_type, status, created_at, updated_at)
VALUES ('ORD-2606-003', 9, '서울시 마포구 와우산로 5', 249246, 3000, 'card', 'delivery', 'payment_complete', '2026-06-19 09:00:00', '2026-06-19 09:00:00');
SET @o14 = LAST_INSERT_ID();
INSERT INTO order_items (order_id, product_id, seller_id, quantity, price, shipping_fee, status, created_at, updated_at)
VALUES (@o14, 2, 10, 2, 123123, 3000, 'payment_complete', '2026-06-19 09:00:00', '2026-06-19 09:00:00');





