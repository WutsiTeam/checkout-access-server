CREATE TABLE T_TRANSACTION(
    id                          VARCHAR(36) NOT NULL,
    idempotency_key             VARCHAR(36) NOT NULL,

    type                        INT NOT NULL,
    status                      INT NOT NULL,
    gateway_type                INT NOT NULL,

    payment_method_fk           BIGINT NOT NULL REFERENCES T_PAYMENT_METHOD(id),
    business_fk                 BIGINT NOT NULL REFERENCES T_BUSINESS(id),
    order_fk                    VARCHAR(36) REFERENCES T_ORDER(id),

    customer_id                 BIGINT,
    amount                      BIGINT NOT NULL DEFAULT 0,
    fees                        BIGINT NOT NULL DEFAULT 0,
    gateway_fees                BIGINT NOT NULL DEFAULT 0,
    net                         BIGINT NOT NULL DEFAULT 0,
    currency                    VARCHAR(3),
    description                 VARCHAR(100),
    gateway_transaction_id      VARCHAR(100),
    financial_transaction_id    VARCHAR(100),
    error_code                  VARCHAR(100),
    supplier_error_code         VARCHAR(100),
    created                     DATETIME NOT NULL DEFAULT now(),
    updated                     DATETIME NOT NULL DEFAULT now() ON UPDATE now(),

    UNIQUE(idempotency_key),
    PRIMARY KEY(id)
);
