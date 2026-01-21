-- 将在读学员表主键改为 employee_qwid + external_userid 计算得到的 id
-- 目标：同一个 external_userid 在不同员工下可以分别存在（避免主键冲突）

ALTER TABLE xca_qw_card_user
    ADD COLUMN IF NOT EXISTS employee_qwid varchar(255) DEFAULT NULL;

ALTER TABLE xca_qw_card_user
    ADD COLUMN IF NOT EXISTS id varchar(600) DEFAULT NULL;

-- 迁移历史数据：如果 employee_qwid 为空，则先用空字符串占位（不影响后续新数据写入）
UPDATE xca_qw_card_user
SET employee_qwid = COALESCE(employee_qwid, '')
WHERE employee_qwid IS NULL;

UPDATE xca_qw_card_user
SET id = CONCAT(employee_qwid, '_', external_userid)
WHERE id IS NULL;

-- 切换主键
ALTER TABLE xca_qw_card_user DROP CONSTRAINT IF EXISTS xca_qw_card_user_pkey;
ALTER TABLE xca_qw_card_user
    ADD CONSTRAINT xca_qw_card_user_pkey PRIMARY KEY (id);

-- 常用查询索引
CREATE INDEX IF NOT EXISTS idx_xca_qw_card_user_employee_qwid ON xca_qw_card_user(employee_qwid);
CREATE INDEX IF NOT EXISTS idx_xca_qw_card_user_employee_qwid_start_time ON xca_qw_card_user(employee_qwid, start_time);
