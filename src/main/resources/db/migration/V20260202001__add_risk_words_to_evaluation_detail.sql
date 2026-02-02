-- 添加风险词字段到评估详情表
ALTER TABLE xca_evaluation_detail ADD COLUMN IF NOT EXISTS risk_words VARCHAR(500) DEFAULT '';
