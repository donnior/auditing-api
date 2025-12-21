-- 创建员工分析报告表
CREATE TABLE xca_employee_analysis_report (
    id varchar(36) NOT NULL,
    employee_id varchar(255) NOT NULL, -- 员工ID
    report_type varchar(255) NOT NULL, -- 报告类型
    qw_account_id varchar(255) NOT NULL, -- 企微帐号ID
    qw_account_name varchar(255) DEFAULT NULL, -- 企微帐号名称
    customer_id varchar(255) NOT NULL, -- 客户ID
    customer_name varchar(255) DEFAULT NULL, -- 客户名称
    cycle_start_time timestamptz NOT NULL, -- 周期开始时间
    cycle_end_time timestamptz NOT NULL, -- 周期结束时间
    report_summary text, -- 报告摘要
    report_rating varchar(255), -- 报告评级
    report_score integer DEFAULT 0, -- 报告评分
    report_suggestions text, -- 改进建议
    attributes JSON, -- 扩展属性，使用JSON格式存储
    create_time timestamptz DEFAULT CURRENT_TIMESTAMP,
    update_time timestamptz DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT xca_employee_analysis_report_pkey PRIMARY KEY (id)
);

-- 创建索引提高查询性能
CREATE INDEX idx_employee_analysis_report_employee ON xca_employee_analysis_report(employee_id);
CREATE INDEX idx_employee_analysis_report_qw_account ON xca_employee_analysis_report(qw_account_id);
CREATE INDEX idx_employee_analysis_report_customer ON xca_employee_analysis_report(customer_id);
CREATE INDEX idx_employee_analysis_report_type ON xca_employee_analysis_report(report_type);
CREATE INDEX idx_employee_analysis_report_cycle ON xca_employee_analysis_report(cycle_start_time, cycle_end_time);

-- 创建唯一约束避免重复分析
CREATE UNIQUE INDEX idx_employee_analysis_unique_report ON xca_employee_analysis_report(employee_id, qw_account_id, customer_id, report_type, cycle_start_time, cycle_end_time);
