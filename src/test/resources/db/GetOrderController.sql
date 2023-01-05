INSERT INTO T_BUSINESS(id, account_id, status, deactivated,currency, country)
    VALUES
        (1, 11, 1, null, 'XAF', 'CM');

INSERT INTO T_ORDER(id, business_fk, customer_account_id, customer_name, customer_email, device_id, channel_type, device_type, status, sub_total_price, total_discount, total_price, total_paid, currency, notes, created, updated, cancelled, closed, cancellation_reason, expires)
    VALUES
        ('100-AEF01-1111', 1, 11, 'Ray Sponsible', 'ray.sponsible@gmail.com', '0000-1111', 3, 1, 3, 5000, 1000, 4000, 1500, 'XAF', 'Thanks', '2010-01-01', '2010-01-10', '2010-01-10', null, 'Not available', now());

INSERT INTO T_ORDER_DISCOUNT(id, order_fk, type, name, amount, discount_id)
    VALUES
        (1001, '100-AEF01-1111', 1, 'C-100', 100, 1)
    ;

INSERT INTO T_ORDER_ITEM(id, order_fk, product_id, product_type, title, picture_url, quantity, unit_price, sub_total_price, total_discount, total_price)
    VALUE
        (1001, '100-AEF01-1111', 555, 1, 'Product 555', 'https://www.img.com/555.png', 2, 1500, 3000, 900, 2100),
        (1002, '100-AEF01-1111', 666, 2, 'Product 666', 'https://www.img.com/666.png', 1, 2000, 2000, 0, 2000)
    ;

INSERT INTO T_ORDER_ITEM_DISCOUNT(id, order_item_fk, type, name, amount, discount_id)
    VALUES
        (10011, 1001, 1, 'MERCHANT', 500, 11),
        (10012, 1001, 2, 'MOBILE', 400, 12)
    ;

INSERT INTO T_PAYMENT_METHOD(id, payment_provider_fk, token, account_id, number, country, owner_name, type, status, deactivated)
    VALUES
        (1001, 1000, 'token-100', 100, '+237690000100', 'CM', 'Roger Milla', 1, 1, null),
        (2001, 1000, 'token-200', 200, '+237690000200', 'CM', 'Omam Biyick', 1, 1, null)
    ;

INSERT INTO T_TRANSACTION(id, idempotency_key, type, status, gateway_type, payment_method_fk, business_fk, order_fk, customer_account_id, amount, fees, gateway_fees, net, currency, gateway_transaction_id, financial_transaction_id, error_code, supplier_error_code, description, payment_method_type, payment_method_number, payment_method_owner_name, payment_method_country, payment_provider_fk)
    VALUES
        ('tx-100', 'idempotent-100', 1, 1, 3, 2001, 1, '100-AEF01-1111', 100, 2100, 5, 10, 2095, 'XAF', 'TX-00000-000-1111', 'FIN-00000-000-1111', 'NOT_ENOUGH_FUNDS', '00000', 'Hello world', 1, '+237690000200', 'Roger Milla', 'CM', 1000),
        ('tx-101', 'idempotent-101', 1, 1, 3, 2001, 2, null, 100, 1500, 5, 10, 495, 'XAF', 'TX-00000-000-1111', 'FIN-00000-000-1111', 'NOT_ENOUGH_FUNDS', '00000', 'Hello world', 1, '+237690000200', 'Roger Milla', 'CM', 1000)
    ;
