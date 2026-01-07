-- 为 xca_evaluation_detail 表添加聊天时间字段
ALTER TABLE xca_evaluation_detail
    ADD COLUMN chat_start_time timestamptz DEFAULT NULL,
    ADD COLUMN chat_end_time timestamptz DEFAULT NULL;

-- 添加列注释
COMMENT ON COLUMN xca_evaluation_detail.chat_start_time IS '聊天开始时间';
COMMENT ON COLUMN xca_evaluation_detail.chat_end_time IS '聊天结束时间';
