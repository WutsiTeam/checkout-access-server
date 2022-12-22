UPDATE T_ORDER set customer_email = LOWER (customer_email);
CREATE INDEX T_ORDER__customer_email ON T_ORDER(customer_email);

CREATE TABLE T_CUSTOMER(
    id            SERIAL NOT NULL,

    business_fk   BIGINT NOT NULL REFERENCES T_BUSINESS(id),

    email         VARCHAR(36) NOT NULL,
    created       DATETIME NOT NULL DEFAULT now(),

    UNIQUE(business_fk, email),
    PRIMARY KEY (id)
);
INSERT INTO T_CUSTOMER(business_fk, email) SELECT DISTINCT business_fk, customer_email FROM T_ORDER;
