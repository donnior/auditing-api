-- 为 xc_qwaccount 表添加 auto_analyze 字段
ALTER TABLE xc_qwaccount
ADD COLUMN auto_analyze BOOLEAN DEFAULT FALSE;

-- 为现有记录设置默认值
UPDATE xc_qwaccount SET auto_analyze = FALSE WHERE auto_analyze IS NULL;

-- 添加非空约束
ALTER TABLE xc_qwaccount
ALTER COLUMN auto_analyze SET NOT NULL;
