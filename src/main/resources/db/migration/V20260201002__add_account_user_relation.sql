-- 为员工表添加关联账户字段
ALTER TABLE xca_employee ADD COLUMN account_user_id varchar(36) DEFAULT NULL;
CREATE INDEX idx_xca_employee_account_user_id ON xca_employee(account_user_id);

-- 为账户表添加账户类型字段
-- account_type: 1=管理员, 2=普通员工
ALTER TABLE xca_account_user ADD COLUMN account_type integer NOT NULL DEFAULT 2;
CREATE INDEX idx_xca_account_user_account_type ON xca_account_user(account_type);

-- 将现有的 admin 账号设置为管理员类型
UPDATE xca_account_user SET account_type = 1 WHERE username = 'admin';

-- 添加注释
COMMENT ON COLUMN xca_employee.account_user_id IS '关联的登录账户ID';
COMMENT ON COLUMN xca_account_user.account_type IS '账户类型: 1=管理员, 2=普通员工';
