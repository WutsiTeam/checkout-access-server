INSERT INTO T_BUSINESS(id, account_id, status, deactivated,currency, country)
    VALUES
        (1, 1, 1, null, 'XAF', 'CM'),
        (2, 2, 1, null, 'XAF', 'CM')
    ;

INSERT INTO T_CUSTOMER(business_fk, email) VALUES (1, 'ray.sponsible30@gmail.com');

INSERT INTO T_ORDER(id, business_fk, customer_name, customer_email, device_id, channel_type, device_type, status, sub_total_price, total_discount, total_price, currency, notes, created, cancelled, closed, cancellation_reason, expires)
    VALUES
        (1000, 1, 'Ray Sponsible', 'ray.sponsible00@gmail.com', '0000-1111', 3, 1, 0, 5000, 1000, 4000, 'XAF', 'Thanks', now(), null, null, 'Not available', DATE_ADD(now(), INTERVAL 1 DAY)),
        (1010, 1, 'Ray Sponsible', 'ray.sponsible10@gmail.com', '0000-1111', 3, 1, 1, 5000, 1000, 4000, 'XAF', 'Thanks', now(), null, null, 'Not available', DATE_ADD(now(), INTERVAL 1 DAY)),
        (1020, 1, 'Ray Sponsible', 'ray.sponsible20@gmail.com', '0000-1111', 3, 1, 2, 5000, 1000, 4000, 'XAF', 'Thanks', now(), null, null, 'Not available', DATE_ADD(now(), INTERVAL 1 DAY)),
        (1030, 1, 'Ray Sponsible', 'ray.sponsible30@gmail.com', '0000-1111', 3, 1, 3, 5000, 1000, 4000, 'XAF', 'Thanks', now(), null, null, 'Not available', DATE_ADD(now(), INTERVAL 1 DAY)),
        (1031, 1, 'Ray Sponsible', 'ray.sponsible31@gmail.com', '0000-1111', 3, 1, 3, 5000, 1000, 4000, 'XAF', 'Thanks', DATE_ADD(now(), INTERVAL -10 DAY), null, null, 'Not available', DATE_ADD(now(), INTERVAL 1 DAY)),
        (1040, 1, 'Ray Sponsible', 'ray.sponsible40@gmail.com', '0000-1111', 3, 1, 4, 5000, 1000, 4000, 'XAF', 'Thanks', now(), null, null, 'Not available', DATE_ADD(now(), INTERVAL 1 DAY)),
        (1050, 1, 'Ray Sponsible', 'ray.sponsible50@gmail.com', '0000-1111', 3, 1, 5, 5000, 1000, 4000, 'XAF', 'Thanks', now(), null, null, 'Not available', DATE_ADD(now(), INTERVAL 1 DAY)),
        (1060, 1, 'Ray Sponsible', 'ray.sponsible60@gmail.com', '0000-1111', 3, 1, 6, 5000, 1000, 4000, 'XAF', 'Thanks', now(), null, null, 'Not available', DATE_ADD(now(), INTERVAL 1 DAY)),

        (200, 2, 'Ray Sponsible', 'ray.sponsible10@gmail.com', '0000-1111', 3, 1, 3, 5000, 0, 5000, 'XAF', 'Thanks', now(), null, null, null, DATE_ADD(now(), INTERVAL 2 DAY )),
        (201, 2, 'Roger Milla',   'roger.milla@gmail.com',     '0000-2222', 3, 1, 3, 5000, 0, 5000, 'XAF', 'Thanks', now(), null, null, null, DATE_ADD(now(), INTERVAL 1 DAY))
    ;
