INSERT INTO T_BUSINESS(id, account_id, status, suspended, currency)
    VALUES
        (1, 1, 1, null, 'XAF');

INSERT INTO T_ORDER(id, business_fk, customer_id, customer_name, status, currency)
    VALUES
        (100, 1, 111, 'Roger Milla', 1, 'XAF'),
        (101, 1, 111, 'Roger Milla', 1, 'XAF'),
        (102, 1, 111, 'Roger Milla', 1, 'XAF'),

        (200, 1, 111, 'Roger Milla', 2, 'XAF'),
        (300, 1, 111, 'Roger Milla', 3, 'XAF')
    ;
