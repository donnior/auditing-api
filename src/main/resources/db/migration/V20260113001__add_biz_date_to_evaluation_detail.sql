-- 为 xca_evaluation_detail 表添加业务日期字段
ALTER TABLE xca_evaluation_detail
    ADD COLUMN biz_date varchar(255) DEFAULT NULL;

-- 添加列注释
COMMENT ON COLUMN xca_evaluation_detail.biz_date IS '业务日期';
