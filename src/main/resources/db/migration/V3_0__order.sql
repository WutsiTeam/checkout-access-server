CREATE TABLE T_ORDER(
    id              VARCHAR(36) NOT NULL,

    store_id        BIGINT NOT NULL,

    customer_id     BIGINT NOT NULL,
    customer_name   VARCHAR(100) NOT NULL,
    customer_email  VARCHAR(100),
    device_id       TEXT,
    device_ip       VARCHAR(30),
    channel_type    INT,
    device_type     INT,
    status          INT NOT NULL DEFAULT 0,
    sub_total_price BIGINT DEFAULT 0,
    total_discount  BIGINT DEFAULT 0,
    total_price     BIGINT DEFAULT 0,
    currency        VARCHAR(3) NOT NULL,
    notes           TEXT,

    created         DATETIME NOT NULL DEFAULT now(),
    updated         DATETIME NOT NULL DEFAULT now() ON UPDATE now(),
    cancelled       DATETIME,
    closed          DATETIME,
    cancellation_reason TEXT,

    PRIMARY KEY (id)
);

CREATE TABLE T_ORDER_ITEM(
    id              SERIAL NOT NULL,

    order_fk        VARCHAR(36) NOT NULL REFERENCES T_ORDER(id),
    offer_id        BIGINT NOT NULL,
    offer_type      INT DEFAULT 0,

    unit_price      BIGINT DEFAULT 0,
    sub_total_price BIGINT DEFAULT 0,
    total_discount  BIGINT DEFAULT 0,
    total_price     BIGINT DEFAULT 0,
    quantity        INT NOT NULL DEFAULT 1,
    title           VARCHAR(100) NOT NULL,
    picture_url     TEXT,

    UNIQUE(order_fk, offer_id, offer_type),
    PRIMARY KEY (id)
);

CREATE TABLE T_ORDER_DISCOUNT(
    id              SERIAL NOT NULL,

    order_fk        VARCHAR(36) NOT NULL REFERENCES T_ORDER(id),

    code            VARCHAR(255),
    amount          BIGINT NOT NULL DEFAULT 0,
    type            INT NOT NULL DEFAULT 0,

    PRIMARY KEY (id)
);

CREATE TABLE T_ORDER_ITEM_DISCOUNT(
    id              SERIAL NOT NULL,

    order_item_fk   BIGINT NOT NULL REFERENCES T_ORDER_ITEM(id),

    code            VARCHAR(255),
    amount          BIGINT NOT NULL DEFAULT 0,
    type            INT NOT NULL DEFAULT 0,

    PRIMARY KEY (id)
);
