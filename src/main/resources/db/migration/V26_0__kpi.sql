CREATE TABLE T_KPI_SALES(
    id            SERIAL NOT NULL,

    date          DATE NOT NULL,
    business_fk   BIGINT NOT NULL REFERENCES T_BUSINESS(id),
    customer_fk   BIGINT NOT NULL REFERENCES T_CUSTOMER(id),
    product_id    BIGINT NOT NULL,

    total_orders  BIGINT NOT NULL DEFAULT 0,
    total_units   BIGINT NOT NULL DEFAULT 0,
    total_value  BIGINT NOT NULL DEFAULT 0,

    UNIQUE(date, business_fk, customer_fk, product_id),
    PRIMARY KEY (id)
);

INSERT INTO T_KPI_SALES(date, business_fk, customer_fk, product_id, total_orders, total_units, total_value)
    SELECT DATE(O.created), O.business_fk, C.id, I.product_id, COUNT(I.product_id), SUM(I.quantity), SUM(I.total_price)
        FROM T_ORDER O
            JOIN T_ORDER_ITEM I ON I.order_fk=O.id
            JOIN T_CUSTOMER C ON O.customer_email=C.email
        WHERE
            O.status NOT IN (0, 1, 5)
        GROUP BY DATE(O.created), O.business_fk, C.id, I.product_id;
