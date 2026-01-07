-- 为评估明细表添加评估周期字段
ALTER TABLE xca_evaluation_detail ADD COLUMN eval_period varchar(255) DEFAULT NULL; -- 评估周期
