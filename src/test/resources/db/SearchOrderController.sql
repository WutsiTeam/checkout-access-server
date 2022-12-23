INSERT INTO T_BUSINESS(id, account_id, status, deactivated,currency, country)
    VALUES
        (1, 1, 1, null, 'XAF', 'CM'),
        (2, 2, 1, null, 'XAF', 'CM')
    ;

INSERT INTO T_ORDER(id, business_fk, customer_account_id, customer_name, customer_email, device_id, channel_type, device_type, status, sub_total_price, total_discount, total_price, currency, notes, created, updated, cancelled, closed, cancellation_reason, expires)
    VALUES
        (100, 1, 11, 'Ray Sponsible', 'ray.sponsible@gmail.com', '0000-1111', 3, 1, 3, 5000, 1000, 4000, 'XAF', 'Thanks', '2010-01-01', '2010-01-10', '2010-01-10', null, 'Not available', DATE_ADD(now(), INTERVAL -1 DAY)),
        (200, 1, 11, 'Ray Sponsible', 'ray.sponsible@gmail.com', '0000-1111', 3, 1, 1, 5000, 0, 5000, 'XAF', 'Thanks', '2010-02-01', '2010-01-10', '2010-01-10', null, null, DATE_ADD(now(), INTERVAL 2 DAY )),
        (300, 2, 55, 'Roger Milla', 'roger.milla@gmail.com',     '0000-2222', 1, 1, 1, 5000, 0, 5000, 'XAF', 'Thanks', DATE_ADD(NOW(), INTERVAL 1 DAY), '2010-01-10', '2010-01-10', null, null, DATE_ADD(now(), INTERVAL 1 DAY))
    ;

INSERT INTO T_ORDER_ITEM(order_fk, product_id, product_type, title, picture_url, quantity, unit_price, sub_total_price, total_discount, total_price)
    VALUE
        ('100', 10, 1, 'Product 555', 'https://www.img.com/555.png', 2, 1500, 3000, 900, 2100),
        ('100', 11, 2, 'Product 666', 'https://www.img.com/666.png', 1, 2000, 2000, 0, 2000),
        ('200', 10, 1, 'Product 555', 'https://www.img.com/555.png', 2, 1500, 3000, 900, 2100),
        ('200', 20, 2, 'Product 666', 'https://www.img.com/666.png', 1, 2000, 2000, 0, 2000),
        ('300', 30, 2, 'Product 666', 'https://www.img.com/666.png', 1, 2000, 2000, 0, 2000)
    ;
