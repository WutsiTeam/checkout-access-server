INSERT INTO T_PAYMENT_METHOD(id, payment_provider_fk, token, account_id, number, country, owner_name, type, status, deactivated)
    VALUES
        (299, 1000, 'token-299', 200, '+237690000200', 'CM', 'Roger Milla', 1, 2, now()),

        (300, 1000, 'token-300', 300, '+237690000300', 'CM', 'Roger Milla', 1, 1, null),
        (301, 1000, 'token-301', 300, '+237690000301', 'CM', 'Roger Milla', 1, 1, null),
        (399, 1000, 'token-399', 300, '+237690000300', 'CM', 'Roger Milla', 1, 2, now())
    ;
