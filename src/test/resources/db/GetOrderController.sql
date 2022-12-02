INSERT INTO T_BUSINESS(id, account_id, status, suspended, currency, country)
    VALUES
        (1, 11, 1, null, 'XAF', 'CM');

INSERT INTO T_ORDER(id, business_fk, customer_id, customer_name, customer_email, device_id, channel_type, device_type, status, sub_total_price, total_discount, total_price, total_paid, currency, notes, created, updated, cancelled, closed, cancellation_reason, expires)
    VALUES
        ('100-AEF01-1111', 1, 11, 'Ray Sponsible', 'ray.sponsible@gmail.com', '0000-1111', 3, 1, 3, 5000, 1000, 4000, 1500, 'XAF', 'Thanks', '2010-01-01', '2010-01-10', '2010-01-10', null, 'Not available', now());

INSERT INTO T_ORDER_DISCOUNT(id, order_fk, type, code, amount)
    VALUES
        (1001, '100-AEF01-1111', 1, 'C-100', 100)
    ;

INSERT INTO T_ORDER_ITEM(id, order_fk, product_id, title, picture_url, quantity, unit_price, sub_total_price, total_discount, total_price)
    VALUE
        (1001, '100-AEF01-1111', 555, 'Product 555', 'https://www.img.com/555.png', 2, 1500, 3000, 900, 2100),
        (1002, '100-AEF01-1111', 666, 'Product 666', 'https://www.img.com/666.png', 1, 2000, 2000, 0, 2000)
    ;

INSERT INTO T_ORDER_ITEM_DISCOUNT(id, order_item_fk, type, code, amount)
    VALUES
        (10011, 1001, 1, 'MERCHANT', 500),
        (10012, 1001, 3, 'MOBILE', 400)
    ;
