DROP INDEX order_fk ON T_ORDER_ITEM;
ALTER TABLE T_ORDER_ITEM DROP COLUMN offer_type;
ALTER TABLE T_ORDER_ITEM DROP COLUMN offer_id;

ALTER TABLE T_ORDER_ITEM ADD COLUMN product_id BIGINT NOT NULL;
CREATE UNIQUE INDEX I_ORDR_ITEM_product ON T_ORDER_ITEM(order_fk, product_id);

