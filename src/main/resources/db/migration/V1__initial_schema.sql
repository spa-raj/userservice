CREATE TABLE roles
(
    id               BINARY(16)   NOT NULL,
    created_at       datetime NULL,
    last_modified_at datetime NULL,
    is_deleted       BIT(1) NULL,
    name             VARCHAR(255) NULL,
    `description`    VARCHAR(1024) NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

CREATE TABLE sessions
(
    id               BINARY(16)   NOT NULL,
    created_at       datetime NULL,
    last_modified_at datetime NULL,
    is_deleted       BIT(1) NULL,
    user_id          BINARY(16)   NULL,
    token            VARCHAR(1024) NULL,
    device           VARCHAR(255) NULL,
    ip_address       VARCHAR(255) NULL,
    expired_at       datetime NULL,
    CONSTRAINT pk_sessions PRIMARY KEY (id)
);

CREATE TABLE users
(
    id               BINARY(16)   NOT NULL,
    created_at       datetime NULL,
    last_modified_at datetime NULL,
    is_deleted       BIT(1) NULL,
    first_name       VARCHAR(255) NULL,
    last_name        VARCHAR(255) NULL,
    email            VARCHAR(255) NULL,
    password         VARCHAR(255) NULL,
    phone_number     VARCHAR(255) NULL,
    address          VARCHAR(1024) NULL,
    city             VARCHAR(255) NULL,
    state            VARCHAR(255) NULL,
    country          VARCHAR(255) NULL,
    zip_code         VARCHAR(255) NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE users_roles
(
    roles_id BINARY(16) NOT NULL,
    users_id BINARY(16) NOT NULL
);

ALTER TABLE sessions
    ADD CONSTRAINT FK_SESSIONS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE users_roles
    ADD CONSTRAINT fk_user_roles_on_role FOREIGN KEY (roles_id) REFERENCES roles (id);

ALTER TABLE users_roles
    ADD CONSTRAINT fk_user_roles_on_user FOREIGN KEY (users_id) REFERENCES users (id);