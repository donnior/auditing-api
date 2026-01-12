-- 账号员登录账号表
CREATE TABLE xca_account_user (
    id varchar(36) NOT NULL,
    username varchar(64) NOT NULL,
    password_hash varchar(255) NOT NULL,
    status integer NOT NULL DEFAULT 1,
    is_deleted boolean NOT NULL DEFAULT FALSE,
    create_time timestamptz NOT NULL DEFAULT now(),
    update_time timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT xca_account_user_pkey PRIMARY KEY (id),
    CONSTRAINT uk_xca_account_user_username UNIQUE (username)
);

CREATE INDEX idx_xca_account_user_username ON xca_account_user(username);
CREATE INDEX idx_xca_account_user_status ON xca_account_user(status);
CREATE INDEX idx_xca_account_user_is_deleted ON xca_account_user(is_deleted);

-- 初始化 admin 账号（密码为 BCrypt hash；不落明文）
-- 注意：password_hash 将在实现阶段用生成的 BCrypt 值回填
INSERT INTO xca_account_user (id, username, password_hash, status, is_deleted, create_time, update_time)
VALUES ('01jexw2q1r9q5b2n3y5q8w6t1a', 'admin', '$2a$10$rjB1ljhGLf85Lce/cUfUlO8Sp8FCpdnvlkm1ZIuqSRXP5OSMrVI8u', 1, FALSE, now(), now());
