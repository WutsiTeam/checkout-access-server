INSERT INTO T_BUSINESS(id, account_id, status, deactivated, currency, country, total_orders, total_sales)
    VALUES
        (201, 200, 1, null, 'XAF', 'CM', 201, 20100),
        (202, 200, 3, now(), 'XAF', 'CM', 202, 20200),

        (399, 300, 3, now(), 'XAF', 'CM', 0, 0)
    ;
