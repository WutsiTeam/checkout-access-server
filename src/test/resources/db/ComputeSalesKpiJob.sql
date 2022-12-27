INSERT INTO T_BUSINESS(id, account_id, status, deactivated,currency, country)
    VALUES
        (1, 1, 1, null, 'XAF', 'CM'),
        (2, 2, 1, null, 'XAF', 'CM')
    ;

INSERT INTO T_ORDER(id, business_fk, status, total_price, created, customer_name, customer_email, currency, expires)
    VALUES
        (1000, 1, 3, 4000, now(), 'Ray Sponsible', 'ray.sponsible10@gmail.com', 'XAF', now()),
        (1001, 1, 4, 3000, now(), 'Ray Sponsible', 'ray.sponsible11@gmail.com', 'XAF', now()),
        (1002, 1, 4, 1500, now(), 'Ray Sponsible', 'ray.sponsible11@gmail.com', 'XAF', now()),
        (1003, 1, 4, 1500, DATE_ADD(now(), INTERVAL -10 DAY), 'Ray Sponsible', 'ray.sponsible11@gmail.com', 'XAF', now()),

        (2000, 2, 3, 1500, now(), 'Ray Sponsible', 'ray.sponsible20@gmail.com', 'XAF', now()),

        (9997, 1, 0, 5000, now(), 'Ray Sponsible', 'ray.sponsible11@gmail.com', 'XAF', now()),
        (9998, 1, 1, 5000, now(), 'Ray Sponsible', 'ray.sponsible11@gmail.com', 'XAF', now()),
        (9999, 1, 5, 5000, now(), 'Ray Sponsible', 'ray.sponsible11@gmail.com', 'XAF', now())
    ;

INSERT INTO T_ORDER_ITEM(order_fk, product_id, quantity, total_price, title)
    VALUES
        (1000, 100, 3, 4500, 'Product'),
        (1000, 101, 1, 0500, 'Product'),
        (1001, 100, 2, 3000, 'Product'),
        (1002, 100, 1, 1500, 'Product'),
        (1003, 100, 10, 15000, 'Product'),
        (2000, 200, 1, 1500, 'Product'),

        (9997, 100, 1, 1500, 'Product'),
        (9998, 100, 1, 1500, 'Product'),
        (9999, 100, 1, 1500, 'Product')
    ;
