ALTER TABLE roles
    ADD CONSTRAINT uc_roles_name UNIQUE (name);

ALTER TABLE user_roles
    ADD CONSTRAINT uc_user_roles_user_role UNIQUE (user_id, role_id);

ALTER TABLE client
    ADD CONSTRAINT uc_client_client_id UNIQUE (client_id);
