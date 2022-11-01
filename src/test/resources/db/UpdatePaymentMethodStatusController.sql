INSERT INTO T_PAYMENT_METHOD(id, token, account_id, number, country, owner_name, type, status, deactivated)
    VALUES
        (299, 'token-299', 200, '+237690000200', 'CM', 'Roger Milla', 1, 2, now()),

        (300, 'token-300', 300, '+237690000300', 'CM', 'Roger Milla', 1, 1, null),
        (301, 'token-301', 300, '+237690000301', 'CM', 'Roger Milla', 1, 1, null),
        (399, 'token-399', 300, '+237690000300', 'CM', 'Roger Milla', 1, 2, now())
    ;

