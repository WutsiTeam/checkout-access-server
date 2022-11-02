CREATE TABLE T_BUSINESS(
    id              SERIAL NOT NULL,

    account_id      BIGINT NOT NULL,
    status          INT NOT NULL DEFAULT 0,

    created         DATETIME NOT NULL DEFAULT now(),
    updated         DATETIME NOT NULL DEFAULT now() ON UPDATE now(),
    suspended       DATETIME,

    PRIMARY KEY (id)
);
