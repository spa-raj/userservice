CREATE TABLE addresses
(
    id               BINARY(16)   NOT NULL,
    created_at       datetime NULL,
    last_modified_at datetime NULL,
    is_deleted       BIT(1) NULL,
    created_by       BINARY(16)   NULL,
    last_modified_by BINARY(16)   NULL,
    version          INT NULL,
    street           VARCHAR(255) NULL,
    city             VARCHAR(255) NULL,
    state            VARCHAR(255) NULL,
    country          VARCHAR(255) NULL,
    zip_code         VARCHAR(255) NULL,
    user_id          BINARY(16)   NULL,
    CONSTRAINT pk_addresses PRIMARY KEY (id)
);

CREATE TABLE roles
(
    id               BINARY(16)   NOT NULL,
    created_at       datetime NULL,
    last_modified_at datetime NULL,
    is_deleted       BIT(1) NULL,
    created_by       BINARY(16)   NULL,
    last_modified_by BINARY(16)   NULL,
    version          INT NULL,
    name             VARCHAR(255) NULL,
    `description`    VARCHAR(255) NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

CREATE TABLE sessions
(
    id               BINARY(16)   NOT NULL,
    created_at       datetime NULL,
    last_modified_at datetime NULL,
    is_deleted       BIT(1) NULL,
    created_by       BINARY(16)   NULL,
    last_modified_by BINARY(16)   NULL,
    version          INT NULL,
    user_id          BINARY(16)   NULL,
    token            VARCHAR(255) NULL,
    device           VARCHAR(255) NULL,
    ip_address       VARCHAR(255) NULL,
    expired_at       datetime NULL,
    status           SMALLINT NULL,
    CONSTRAINT pk_sessions PRIMARY KEY (id)
);

CREATE TABLE sessions_role
(
    role_id     BINARY(16) NOT NULL,
    sessions_id BINARY(16) NOT NULL,
    CONSTRAINT pk_sessions_role PRIMARY KEY (role_id, sessions_id)
);

CREATE TABLE user_profiles
(
    id               BINARY(16)   NOT NULL,
    created_at       datetime NULL,
    last_modified_at datetime NULL,
    is_deleted       BIT(1) NULL,
    created_by       BINARY(16)   NULL,
    last_modified_by BINARY(16)   NULL,
    version          INT NULL,
    user_id          BINARY(16)   NULL,
    bio              VARCHAR(255) NULL,
    profile_picture  VARCHAR(255) NULL,
    CONSTRAINT pk_user_profiles PRIMARY KEY (id)
);

CREATE TABLE user_roles
(
    id               BINARY(16) NOT NULL,
    created_at       datetime NULL,
    last_modified_at datetime NULL,
    is_deleted       BIT(1) NULL,
    created_by       BINARY(16) NULL,
    last_modified_by BINARY(16) NULL,
    version          INT NULL,
    user_id          BINARY(16) NULL,
    role_id          BINARY(16) NULL,
    assigned_at      datetime NULL,
    assigned_by_id   BINARY(16) NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (id)
);

CREATE TABLE users
(
    id               BINARY(16)   NOT NULL,
    created_at       datetime NULL,
    last_modified_at datetime NULL,
    is_deleted       BIT(1) NULL,
    created_by       BINARY(16)   NULL,
    last_modified_by BINARY(16)   NULL,
    version          INT NULL,
    first_name       VARCHAR(255) NULL,
    last_name        VARCHAR(255) NULL,
    email            VARCHAR(255) NOT NULL,
    password         VARCHAR(255) NOT NULL,
    phone_number     VARCHAR(255) NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

CREATE INDEX idx_user_email ON users (email);

CREATE INDEX idx_user_phonenumber ON users (phone_number);

ALTER TABLE addresses
    ADD CONSTRAINT FK_ADDRESSES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE sessions
    ADD CONSTRAINT FK_SESSIONS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE user_profiles
    ADD CONSTRAINT FK_USER_PROFILES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE user_roles
    ADD CONSTRAINT FK_USER_ROLES_ON_ASSIGNEDBY FOREIGN KEY (assigned_by_id) REFERENCES users (id);

ALTER TABLE user_roles
    ADD CONSTRAINT FK_USER_ROLES_ON_ROLE FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE user_roles
    ADD CONSTRAINT FK_USER_ROLES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE sessions_role
    ADD CONSTRAINT fk_sesrol_on_role FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE sessions_role
    ADD CONSTRAINT fk_sesrol_on_session FOREIGN KEY (sessions_id) REFERENCES sessions (id);