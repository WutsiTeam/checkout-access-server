CREATE TABLE T_PAYMENT_METHOD(
    id            SERIAL NOT NULL,
    token         VARCHAR(36) NOT NULL,
    account_id    BIGINT NOT NULL,
    number        VARCHAR(30) NOT NULL,
    country       VARCHAR(2) NOT NULL,
    owner_name    VARCHAR(100) NOT NULL,
    type          INT NOT NULL DEFAULT 0,
    status        INT NOT NULL DEFAULT 0,
    created       DATETIME NOT NULL DEFAULT now(),
    updated       DATETIME NOT NULL DEFAULT now() ON UPDATE now(),
    deactivated   DATETIME,

    UNIQUE (token),
    PRIMARY KEY (id)
);

