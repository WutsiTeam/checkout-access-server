INSERT INTO T_BUSINESS(id, account_id, status, deactivated,currency, country)
    VALUES
        (1, 1, 1, null, 'XAF', 'CM');

INSERT INTO T_ORDER(id, business_fk, customer_account_id, customer_name, status, currency, customer_email, expires)
    VALUES
        (100, 1, 111, 'Roger Milla', 1, 'XAF', 'roger.milla@gmail.com', now()),
        (101, 1, 111, 'Roger Milla', 1, 'XAF', 'roger.milla@gmail.com', now()),
        (102, 1, 111, 'Roger Milla', 2, 'XAF', 'roger.milla@gmail.com', now()),

        (200, 1, 111, 'Roger Milla', 2, 'XAF', 'roger.milla@gmail.com', now()),
        (300, 1, 111, 'Roger Milla', 3, 'XAF', 'roger.milla@gmail.com', now())
    ;
