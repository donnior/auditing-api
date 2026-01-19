-- 在读学员课程周序列表（卡片用户）存储表
CREATE TABLE xca_qw_card_user (
    external_userid varchar(255) NOT NULL, -- 学员 externalUserid (主键)
    external_name varchar(255) DEFAULT NULL, -- 学员名称
    card_name varchar(255) DEFAULT NULL, -- 卡片名称
    start_time timestamptz DEFAULT NULL, -- 开始时间
    create_time timestamptz DEFAULT NULL, -- 创建时间
    update_time timestamptz DEFAULT NULL, -- 更新时间（用于增量同步）
    week_number integer DEFAULT NULL, -- 课程周序
    CONSTRAINT xca_qw_card_user_pkey PRIMARY KEY (external_userid)
);

-- 索引：按更新时间增量查询/排序
CREATE INDEX idx_xca_qw_card_user_update_time ON xca_qw_card_user(update_time);
CREATE INDEX idx_xca_qw_card_user_week_number ON xca_qw_card_user(week_number);

