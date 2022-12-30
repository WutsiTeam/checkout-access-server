INSERT INTO T_BUSINESS(id, account_id, status, deactivated,currency, country)
    VALUES
        (1, 1, 1, null, 'XAF', 'CM'),
        (2, 2, 1, null, 'XAF', 'CM')
    ;

INSERT INTO T_KPI_SALES(business_fk, product_id, date, total_orders, total_units, total_value, total_views)
    VALUES
        (1, 100, now(), 1, 5, 5000, 30000),
        (1, 100, DATE_ADD(now(), INTERVAL -1 DAY), 1, 2, 2000, 10000),
        (1, 100, DATE_ADD(now(), INTERVAL -2 DAY), 3, 3, 3000, 20000),
        (1, 101, DATE_ADD(now(), INTERVAL -2 DAY), 1, 10, 5000, 1000),
        (1, 110, DATE_ADD(now(), INTERVAL -10 DAY), 11, 33, 50000, 30000),
        (2, 200, DATE_ADD(now(), INTERVAL -2 DAY), 1, 1, 10000, 40000)
    ;
