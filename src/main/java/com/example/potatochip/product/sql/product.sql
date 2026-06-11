INSERT INTO product_rankings (product_id, seller_id, period_type, period_date, sales_count, rank)
VALUES
    (1, 1, 'weekly', CURDATE(), 150, 1),
    (2, 1, 'weekly', CURDATE(), 120, 2),
    (3, 2, 'weekly', CURDATE(), 90, 3);