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
