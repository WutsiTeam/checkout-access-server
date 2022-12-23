DELETE FROM T_KPI_SALES;
ALTER TABLE T_KPI_SALES DROP COLUMN customer_fk;

INSERT INTO T_KPI_SALES(date, business_fk, product_id, total_orders, total_units, total_value)
    SELECT DATE(O.created), O.business_fk, I.product_id, COUNT(I.product_id), SUM(I.quantity), SUM(I.total_price)
        FROM T_ORDER O
            JOIN T_ORDER_ITEM I ON I.order_fk=O.id
        WHERE
            O.status NOT IN (0, 1, 5)
        GROUP BY DATE(O.created), O.business_fk, I.product_id;
