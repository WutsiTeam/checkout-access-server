CREATE TABLE T_PAYMENT_PROVIDER(
    id                  SERIAL NOT NULL,
    code                VARCHAR(30) NOT NULL,
    name                VARCHAR(100) NOT NULL,
    logo_url            TEXT,
    type                INT NOT NULL DEFAULT 0,

    UNIQUE (code),
    PRIMARY KEY (id)
);

CREATE TABLE T_PAYMENT_PROVIDER_PREFIX(
    id                  SERIAL NOT NULL,

    payment_provider_fk BIGINT REFERENCES T_PAYMENT_PREFIX(id),
    number_prefix       VARCHAR(10) NOT NULL,
    country             VARCHAR(2) NOT NULL
);

ALTER TABLE T_PAYMENT_METHOD ADD COLUMN payment_provider_fk BIGINT NOT NULL REFERENCES T_PAYMENT_PROVIDER(id);

INSERT INTO T_PAYMENT_PROVIDER(id, code, name, logo_url, type)
    VALUES
        (1000, 'MTN', 'MTN', 'https://prod-wutsi.s3.amazonaws.com/static/checkout-access-server/logos/mtn.png', 1),
        (1001, 'Orange', 'Orange', 'https://prod-wutsi.s3.amazonaws.com/static/checkout-access-server/logos/mtn.png', 1)
    ;

INSERT INTO T_PAYMENT_PROVIDER_PREFIX(payment_provider_fk, country, number_prefix)
    VALUES
        (1000, 'CM', '+23767'),
        (1000, 'CM', '+237650'),
        (1000, 'CM', '+237651'),
        (1000, 'CM', '+237652'),
        (1000, 'CM', '+237653'),
        (1000, 'CM', '+237654'),
        (1000, 'CM', '+237675'),
        (1000, 'CM', '+237676'),
        (1000, 'CM', '+237677'),
        (1000, 'CM', '+237678'),
        (1000, 'CM', '+237679'),
        (1000, 'CM', '+23768'),

        (1001, 'CM', '+23769'),
        (1001, 'CM', '+237655'),
        (1001, 'CM', '+237656'),
        (1001, 'CM', '+237657'),
        (1001, 'CM', '+237658'),
        (1001, 'CM', '+237659')
    ;
