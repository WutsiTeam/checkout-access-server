INSERT INTO T_BUSINESS(id, account_id, status, suspended, currency)
    VALUES
        (1, 1, 1, null, 'XAF'),
        (2, 2, 1, null, 'XAF')
    ;

INSERT INTO T_ORDER(id, business_fk, customer_id, customer_name, customer_email, device_id, device_ip, channel_type, device_type, status, sub_total_price, total_discount, total_price, currency, notes, created, updated, cancelled, closed, cancellation_reason)
    VALUES
        (100, 1, 11, 'Ray Sponsible', 'ray.sponsible@gmail.com', '0000-1111', '10.0.0.1', 3, 1, 3, 5000, 1000, 4000, 'XAF', 'Thanks', '2010-01-01', '2010-01-10', '2010-01-10', null, 'Not available'),
        (200, 1, 11, 'Ray Sponsible', 'ray.sponsible@gmail.com', '0000-1111', '10.0.0.2', 3, 1, 1, 5000, 0, 5000, 'XAF', 'Thanks', '2010-02-01', '2010-01-10', '2010-01-10', null, null),
        (300, 2, 55, 'Roger Milla', 'roger.milla@gmail.com',     '0000-2222', '10.0.0.3', 1, 1, 1, 5000, 0, 5000, 'XAF', 'Thanks', DATE_ADD(NOW(), INTERVAL 1 DAY), '2010-01-10', '2010-01-10', null, null)
    ;
