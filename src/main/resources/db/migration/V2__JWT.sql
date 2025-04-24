CREATE TABLE jwt
(
    id               BINARY(16)   NOT NULL,
    created_at       datetime NULL,
    last_modified_at datetime NULL,
    is_deleted       BIT(1) NULL,
    created_by       BINARY(16)   NULL,
    last_modified_by BINARY(16)   NULL,
    version          INT NULL,
    secret           VARCHAR(255) NULL,
    CONSTRAINT pk_jwt PRIMARY KEY (id)
);