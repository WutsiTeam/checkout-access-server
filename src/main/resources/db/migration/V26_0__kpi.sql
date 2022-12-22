CREATE TABLE T_KPI(
    id            SERIAL NOT NULL,

    year          INTEGER NOT NULL DEFAULT 0,
    month         INTEGER NOT NULL DEFAULT 0,
    day           INTEGER NOT NULL DEFAULT 0,
    business_id   BIGINT NOT NULL DEFAULT -1,
    product_id    BIGINT NOT NULL DEFAULT -1,

    type          INTEGER NOT NULL DEFAULT 0,
    value         BIGINT NOT NULL DEFAULT 0,
    created       DATETIME NOT NULL DEFAULT now(),

    UNIQUE(year, month, day, business_id, product_id, type),
    PRIMARY KEY (id)
);

-- CUSTOMER-COUNT BY BUSINESS
INSERT INTO T_KPI(year, month, day, business_id, type, value)
    SELECT YEAR(C.created), MONTH(C.created), DAY(C.created), C.business_fk, 1, COUNT(id)
        FROM T_CUSTOMER C
        GROUP BY YEAR(C.created), MONTH(C.created), DAY(C.created), C.business_fk;

-- ORDER-COUNT BY BUSINESS
INSERT INTO T_KPI(year, month, day, business_id, type, value)
    SELECT YEAR(O.created), MONTH(O.created), DAY(O.created), O.business_fk, 2, COUNT(id)
        FROM T_ORDER O
        WHERE O.status NOT IN (0, 1, 5)
        GROUP BY YEAR(O.created), MONTH(O.created), DAY(O.created), O.business_fk;

-- SALES BY BUSINESS
INSERT INTO T_KPI(year, month, day, business_id, type, value)
    SELECT YEAR(O.created), MONTH(O.created), DAY(O.created), O.business_fk, 2, COUNT(total_price)
        FROM T_ORDER O
        WHERE O.status NOT IN (0, 1, 5)
        GROUP BY YEAR(O.created), MONTH(O.created), DAY(O.created), O.business_fk;

-- ORDER-COUNT BY PRODUCT
INSERT INTO T_KPI(year, month, day, product_id, type, value)
    SELECT YEAR(O.created), MONTH(O.created), DAY(O.created), I.product_id, 2, COUNT(product_id)
        FROM T_ORDER O JOIN T_ORDER_ITEM I ON O.id=I.order_fk
        WHERE O.status NOT IN (0, 1, 5)
        GROUP BY YEAR(O.created), MONTH(O.created), DAY(O.created), I.product_id;

-- SALES BY PRODUCT
INSERT INTO T_KPI(year, month, day, product_id, type, value)
    SELECT YEAR(O.created), MONTH(O.created), DAY(O.created), I.product_id, 3, COUNT(I.total_price)
        FROM T_ORDER O JOIN T_ORDER_ITEM I ON O.id=I.order_fk
        WHERE O.status NOT IN (0, 1, 5)
        GROUP BY YEAR(O.created), MONTH(O.created), DAY(O.created), I.product_id;

-- AGGREGATE
INSERT INTO T_KPI(year, month, day, business_id, product_id, type, value)
    SELECT 0, 0, 0, business_id, product_id, type, SUM(value)
        FROM T_KPI
        WHERE year>0 AND month>0 AND day>0
        GROUP BY business_id, product_id, type;
