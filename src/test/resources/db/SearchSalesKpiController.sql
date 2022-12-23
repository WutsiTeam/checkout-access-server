INSERT INTO T_BUSINESS(id, account_id, status, deactivated,currency, country)
    VALUES
        (1, 1, 1, null, 'XAF', 'CM'),
        (2, 2, 1, null, 'XAF', 'CM')
    ;

INSERT INTO T_CUSTOMER(id, business_fk, email)
    VALUES
        (10, 1, 'ray.sponsible10@gmail.com'),
        (11, 1, 'ray.sponsible11@gmail.com'),
        (20, 2, 'ray.sponsible20@gmail.com')
;

INSERT INTO T_KPI_SALES(business_fk, product_id, date, total_orders, total_units, total_value)
    VALUES
        (1, 100, now(), 1, 5, 5000),
        (1, 100, DATE_ADD(now(), INTERVAL -1 DAY), 1, 2, 2000),
        (1, 100, DATE_ADD(now(), INTERVAL -2 DAY), 3, 3, 3000),
        (1, 110, DATE_ADD(now(), INTERVAL -10 DAY), 11, 33, 50000),
        (2, 200, DATE_ADD(now(), INTERVAL -2 DAY), 1, 1, 10000)
    ;
