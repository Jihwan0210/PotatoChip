ALTER TABLE ai_review_summaries AUTO_INCREMENT = 1;
ALTER TABLE board AUTO_INCREMENT = 1;
ALTER TABLE cart_items AUTO_INCREMENT = 1;
ALTER TABLE carts AUTO_INCREMENT = 1;
ALTER TABLE chatbot_logs AUTO_INCREMENT = 1;
ALTER TABLE inquiries AUTO_INCREMENT = 1;
ALTER TABLE order_items AUTO_INCREMENT = 1;
ALTER TABLE orders AUTO_INCREMENT = 1;
ALTER TABLE pickup_locations AUTO_INCREMENT = 1;
ALTER TABLE product_images AUTO_INCREMENT = 1;
ALTER TABLE product_rankings AUTO_INCREMENT = 1;
ALTER TABLE product_recommendations AUTO_INCREMENT = 1;
ALTER TABLE products AUTO_INCREMENT = 1;
ALTER TABLE review_helpfuls AUTO_INCREMENT = 1;
ALTER TABLE reviews AUTO_INCREMENT = 1;
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE wishlists AUTO_INCREMENT = 1;



INSERT INTO products
(seller_id, category, name, description, price, discount_price, discount_start_at, discount_end_at,
 thumbnail_url, origin, expiry_date, is_pickup_available, stock_quantity,
 created_at, updated_at, address, latitude, longitude, operating_hours)
VALUES

    (2, '과일', '못난이 사과', '모양은 별로지만 맛은 그대로인 사과입니다.', 8000, 6000, '2026-06-18', '2026-06-25',
     NULL, '경북 영주', '2026-07-15', true, 50,
     NOW(), NOW(), '서울특별시 강남구 테헤란로 123', 37.498095, 127.027610, '09:00 ~ 18:00'),

    (2, '채소', '못난이 감자', '크기가 제각각이지만 신선한 강원도 감자입니다.', 5000, NULL, NULL, NULL,
     NULL, '강원 평창', '2026-08-01', false, 120,
     NOW(), NOW(), '서울특별시 마포구 월드컵북로 396', 37.566826, 126.901480, '08:30 ~ 17:30'),

    (2, '과일', '못난이 배', '흠집은 있지만 당도 높은 나주 배입니다.', 12000, 9000, '2026-06-20', '2026-06-30',
     NULL, '전남 나주', '2026-07-20', true, 30,
     NOW(), NOW(), '서울특별시 송파구 올림픽로 300', 37.513272, 127.105827, '09:00 ~ 19:00'),

    (2, '채소', '못난이 양파', '크기 불균일 양파, 맛은 동일합니다.', 4000, NULL, NULL, NULL,
     NULL, '경남 창녕', '2026-09-10', true, 200,
     NOW(), NOW(), '서울특별시 영등포구 여의대로 24', 37.525500, 126.926700, '08:00 ~ 17:00'),

    (2, '과일', '못난이 복숭아', '모양은 울퉁불퉁해도 향과 맛은 그대로.', 15000, 11000, '2026-06-19', '2026-06-26',
     NULL, '충북 음성', '2026-07-10', false, 40,
     NOW(), NOW(), '서울특별시 서초구 서초대로 250', 37.491870, 127.013820, '09:00 ~ 18:00'),

    (2, '채소', '못난이 당근', '모양 불균일, 일반 당근과 영양 동일.', 3500, 2800, '2026-06-18', '2026-06-24',
     NULL, '제주', '2026-08-05', true, 150,
     NOW(), NOW(), '서울특별시 광진구 능동로 120', 37.547610, 127.085800, '08:30 ~ 18:00'),

    (2, '과일', '못난이 귤', '껍질 흠집 있는 제주 감귤입니다.', 9000, NULL, NULL, NULL,
     NULL, '제주', '2026-12-01', true, 80,
     NOW(), NOW(), '서울특별시 노원구 동일로 1000', 37.654850, 127.060000, '09:00 ~ 18:00'),

    (2, '채소', '못난이 무', '모양은 못나도 단단하고 신선합니다.', 4500, NULL, NULL, NULL,
     NULL, '강원 평창', '2026-08-20', false, 90,
     NOW(), NOW(), '서울특별시 강서구 화곡로 200', 37.541260, 126.840000, '08:00 ~ 17:30'),

    (2, '과일', '못난이 포도', '알이 작거나 모양이 불균일한 포도.', 13000, 10000, '2026-06-18', '2026-06-23',
     NULL, '경북 영천', '2026-07-05', true, 60,
     NOW(), NOW(), '서울특별시 동작구 보라매로 15', 37.495400, 126.934000, '09:00 ~ 19:00'),

    (2, '채소', '못난이 고구마', '모양 불균일, 당도는 그대로인 해남 고구마.', 6000, 4800, '2026-06-18', '2026-06-28',
     NULL, '전남 해남', '2026-09-15', true, 100,
     NOW(), NOW(), '서울특별시 은평구 통일로 1000', 37.602800, 126.929300, '08:30 ~ 18:00');



INSERT INTO product_rankings
(product_id, seller_id, period_type, period_date, sales_count, ranking)
VALUES
    (3, 2, 'DAILY', '2026-06-18', 58, 1),
    (9, 2, 'DAILY', '2026-06-18', 51, 2),
    (1, 2, 'DAILY', '2026-06-18', 47, 3),
    (10, 2, 'DAILY', '2026-06-18', 44, 4),
    (6, 2, 'DAILY', '2026-06-18', 40, 5),
    (5, 2, 'DAILY', '2026-06-18', 36, 6),
    (2, 2, 'DAILY', '2026-06-18', 31, 7),
    (7, 2, 'DAILY', '2026-06-18', 27, 8),
    (4, 2, 'DAILY', '2026-06-18', 22, 9),
    (8, 2, 'DAILY', '2026-06-18', 18, 10);


INSERT INTO product_rankings
(product_id, seller_id, period_type, period_date, sales_count, ranking)
VALUES
    (9, 2, 'WEEKLY', '2026-06-15', 320, 1),
    (3, 2, 'WEEKLY', '2026-06-15', 298, 2),
    (10, 2, 'WEEKLY', '2026-06-15', 275, 3),
    (1, 2, 'WEEKLY', '2026-06-15', 250, 4),
    (6, 2, 'WEEKLY', '2026-06-15', 230, 5),
    (5, 2, 'WEEKLY', '2026-06-15', 210, 6),
    (2, 2, 'WEEKLY', '2026-06-15', 190, 7),
    (7, 2, 'WEEKLY', '2026-06-15', 165, 8),
    (4, 2, 'WEEKLY', '2026-06-15', 140, 9),
    (8, 2, 'WEEKLY', '2026-06-15', 120, 10);