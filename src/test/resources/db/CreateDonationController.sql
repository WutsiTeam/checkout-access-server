INSERT INTO T_BUSINESS(id, account_id, status, deactivated,balance, currency, country)
    VALUES
        (1, 1, 2, null, 120000, 'XAF', 'CM')
    ;

INSERT INTO T_PAYMENT_METHOD(id, payment_provider_fk, token, account_id, number, country, owner_name, type, status, deactivated)
    VALUES
        (1001, 1000, 'token-100', 100, '+237690000100', 'CM', 'Roger Milla', 1, 1, null),
        (2001, 1000, 'token-200', 200, '+237690000200', 'CM', 'Omam Biyick', 1, 1, null)
    ;

INSERT INTO T_TRANSACTION(id, business_fk, idempotency_key, type, status, gateway_type, payment_method_fk, order_fk, customer_account_id, amount, fees, gateway_fees, net, currency, gateway_transaction_id, payment_method_type, payment_method_number, payment_method_owner_name, payment_method_country, payment_provider_fk, created)
    VALUES
        ('tx-200', 1, 'idempotent-200', 3, 1, 3, 2001, null, 200, 500, 0, 0, 500, 'XAF', 'TX-00000-000-1111', 1, '+237690000200', 'Roger Milla', 'CM', 1000, now()),
        ('tx-201', 1, 'idempotent-201', 1, 1, 3, 2001, null, null, 1000, 0, 0, 1000, 'XAF', 'TX-00000-000-1111', 1, '+237690000200', 'Roger Milla', 'CM', 1000, now()),
        ('tx-202', 1, 'idempotent-202', 1, 1, 3, 2001, null, null, 2000, 0, 0, 2000, 'XAF', 'TX-00000-000-1111', 1, '+237690000200', 'Roger Milla', 'CM', 1000, now()),
        ('tx-203', 1, 'idempotent-203', 1, 1, 3, 2001, null, null, 3000, 0, 0, 3000, 'XAF', 'TX-00000-000-1111', 1, '+237690000200', 'Roger Milla', 'CM', 1000, DATE_ADD(now(), interval -1 hour )),
        ('tx-204', 1, 'idempotent-204', 1, 1, 3, 2001, null, null, 4000, 0, 0, 4000, 'XAF', 'TX-00000-000-1111', 1, '+237690000200', 'Roger Milla', 'CM', 1000, DATE_ADD(now(), interval -10 hour )),
        ('tx-298', 1, 'idempotent-298', 2, 1, 3, 2001, null, null, 4008, 0, 0, 4000, 'XAF', 'TX-00000-000-1111', 1, '+237690000200', 'Roger Milla', 'CM', 1000, DATE_ADD(now(), interval -10 hour )),
        ('tx-299', 1, 'idempotent-299', 1, 2, 3, 2001, null, null, 4009, 0, 0, 4000, 'XAF', 'TX-00000-000-1111', 1, '+237690000200', 'Roger Milla', 'CM', 1000, DATE_ADD(now(), interval -10 hour )),
        ('tx-205', 1, 'idempotent-205', 1, 1, 3, 2001, null, null, 5000, 0, 0, 5000, 'XAF', 'TX-00000-000-1111', 1, '+237690000200', 'Roger Milla', 'CM', 1000, DATE_ADD(now(), interval -2 day))
    ;
