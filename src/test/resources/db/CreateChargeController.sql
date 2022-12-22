INSERT INTO T_BUSINESS(id, account_id, status, deactivated,balance, currency, country)
    VALUES
        (1, 1, 2, null, 120000, 'XAF', 'CM')
    ;

INSERT INTO T_PAYMENT_METHOD(id, payment_provider_fk, token, account_id, number, country, owner_name, type, status, deactivated)
    VALUES
        (1001, 1000, 'token-100', 100, '+237690000100', 'CM', 'Roger Milla', 1, 1, null),
        (2001, 1000, 'token-200', 200, '+237690000200', 'CM', 'Omam Biyick', 1, 1, null)
    ;

INSERT INTO T_ORDER(id, business_fk, customer_account_id, customer_name, status, currency, total_price, customer_email, expires)
    VALUES
        ('order-100', 1, 100, 'Roger Milla', 1, 'XAF', 50000, 'roger.milla@gmail.com', now()),
        ('order-200', 1, 100, 'Roger Milla', 1, 'XAF', 500, 'roger.milla@gmail.com', now())
    ;

INSERT INTO T_TRANSACTION(id, idempotency_key, type, status, gateway_type, payment_method_fk, business_fk, order_fk, customer_account_id, amount, fees, gateway_fees, net, currency, gateway_transaction_id, payment_method_type, payment_method_number, payment_method_owner_name, payment_method_country, payment_provider_fk)
    VALUES
        ('tx-200', 'idempotent-200', 1, 1, 3, 2001, 1, 'order-200', 200, 500, 0, 0, 500, 'XAF', 'TX-00000-000-1111', 1, '+237690000200', 'Roger Milla', 'CM', 1000)
    ;
