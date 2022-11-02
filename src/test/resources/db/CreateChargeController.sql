INSERT INTO T_BUSINESS(id, account_id, status, suspended, balance, currency)
    VALUES
        (1, 1, 2, null, 120000, 'XAF')
    ;

INSERT INTO T_PAYMENT_METHOD(id, token, account_id, number, country, owner_name, type, status, deactivated)
    VALUES
        (1001, 'token-100', 100, '+237690000100', 'CM', 'Roger Milla', 1, 1, null),
        (2001, 'token-200', 200, '+237690000200', 'CM', 'Omam Biyick', 1, 1, null)
    ;

INSERT INTO T_ORDER(id, business_fk, customer_id, customer_name, status, currency, total_price)
    VALUES
        ('order-100', 1, 100, 'Roger Milla', 1, 'XAF', 50000),
        ('order-200', 1, 100, 'Roger Milla', 1, 'XAF', 500)
    ;

INSERT INTO T_TRANSACTION(id, idempotency_key, type, status, gateway_type, payment_method_fk, business_fk, order_fk, customer_id, amount, fees, gateway_fees, net, currency, gateway_transaction_id)
    VALUES
        ('tx-200', 'idempotent-200', 1, 1, 3, 2001, 1, 'order-200', 200, 500, 0, 0, 500, 'XAF', 'TX-00000-000-1111')
    ;
