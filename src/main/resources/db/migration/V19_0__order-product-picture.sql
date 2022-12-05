ALTER TABLE T_ORDER ADD COLUMN product_picture_url1 TEXT;
ALTER TABLE T_ORDER ADD COLUMN product_picture_url2 TEXT;
ALTER TABLE T_ORDER ADD COLUMN product_picture_url3 TEXT;

UPDATE T_ORDER O, T_ORDER_ITEM I SET product_picture_url1=I.picture_url WHERE O.id=I.order_fk;
