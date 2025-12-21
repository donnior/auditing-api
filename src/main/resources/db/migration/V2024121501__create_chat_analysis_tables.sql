-- 聊天消息存储表
CREATE TABLE xca_qw_chat_msg (
    msg_id varchar(255) NOT NULL, -- 消息ID (主键)
    from_id varchar(255) DEFAULT NULL, -- 消息发送人ID
    from_name varchar(255) DEFAULT NULL, -- 消息发送人名称
    accept_id varchar(255) DEFAULT NULL, -- 消息接收人ID
    accept_type smallint DEFAULT NULL, -- 消息的接受类型: 1:客户或成员; 2:客群
    accept_name varchar(255) DEFAULT NULL, -- 消息接收人名称
    msg_type varchar(255) DEFAULT NULL, -- 消息类型
    content text, -- 消息内容
    data_seq bigint DEFAULT NULL, -- 当前数据对应的下标
    msg_time timestamptz DEFAULT NULL, -- 消息的发送时间
    create_time timestamptz DEFAULT NULL, -- 当前数据入库创建时间
    CONSTRAINT xca_qw_chat_msg_pkey PRIMARY KEY (msg_id)
);

-- 创建索引提高查询性能
CREATE INDEX idx_xca_qw_chat_msg_from_id ON xca_qw_chat_msg(from_id);
CREATE INDEX idx_xca_qw_chat_msg_accept_id ON xca_qw_chat_msg(accept_id);
CREATE INDEX idx_xca_qw_chat_msg_msg_time ON xca_qw_chat_msg(msg_time);
CREATE INDEX idx_xca_qw_chat_msg_data_seq ON xca_qw_chat_msg(data_seq);

-- 员工企微帐号-客户聊天分析明细表
CREATE TABLE xca_employee_customer_chat_analysis (
    id varchar(36) NOT NULL,
    qw_account_id varchar(255) NOT NULL, -- 企微帐号ID
    qw_account_name varchar(255) DEFAULT NULL, -- 企微帐号名称
    customer_id varchar(255) NOT NULL, -- 客户ID
    customer_name varchar(255) DEFAULT NULL, -- 客户名称
    cycle_start_time timestamptz NOT NULL, -- 分析周期开始时间
    cycle_end_time timestamptz NOT NULL, -- 分析周期结束时间
    report_summary text, -- 分析报告内容
    message_count integer DEFAULT 0, -- 消息总数
    response_time_avg decimal(10,2) DEFAULT 0, -- 平均响应时间(分钟)
    satisfaction_score decimal(3,2) DEFAULT 0, -- 满意度评分
    violation_count integer DEFAULT 0, -- 违规次数
    service_quality_score decimal(3,2) DEFAULT 0, -- 服务质量评分
    create_time timestamptz DEFAULT CURRENT_TIMESTAMP,
    update_time timestamptz DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT xca_employee_customer_chat_analysis_pkey PRIMARY KEY (id)
);

-- 创建索引
CREATE INDEX idx_employee_customer_analysis_qw_account ON xca_employee_customer_chat_analysis(qw_account_id);
CREATE INDEX idx_employee_customer_analysis_customer ON xca_employee_customer_chat_analysis(customer_id);
CREATE INDEX idx_employee_customer_analysis_cycle ON xca_employee_customer_chat_analysis(cycle_start_time, cycle_end_time);

-- 员工企微帐号聊天分析汇总表
CREATE TABLE xca_employee_daily_analysis (
    id varchar(36) NOT NULL,
    qw_account_id varchar(255) NOT NULL, -- 企微帐号ID
    qw_account_name varchar(255) DEFAULT NULL, -- 企微帐号名称
    cycle_start_time timestamptz NOT NULL, -- 分析周期开始时间
    cycle_end_time timestamptz NOT NULL, -- 分析周期结束时间
    report_summary text, -- 综合分析报告内容
    total_customers integer DEFAULT 0, -- 服务客户总数
    total_messages integer DEFAULT 0, -- 消息总数
    avg_response_time decimal(10,2) DEFAULT 0, -- 平均响应时间(分钟)
    overall_satisfaction decimal(3,2) DEFAULT 0, -- 整体满意度
    total_violations integer DEFAULT 0, -- 总违规次数
    service_quality_score decimal(3,2) DEFAULT 0, -- 整体服务质量评分
    performance_rating varchar(50) DEFAULT NULL, -- 绩效等级
    improvement_suggestions text, -- 改进建议
    create_time timestamptz DEFAULT CURRENT_TIMESTAMP,
    update_time timestamptz DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT xca_employee_daily_analysis_pkey PRIMARY KEY (id)
);

-- 创建索引
CREATE INDEX idx_xca_employee_daily_analysis_qw_account ON xca_employee_daily_analysis(qw_account_id);
CREATE INDEX idx_xca_employee_daily_analysis_cycle ON xca_employee_daily_analysis(cycle_start_time, cycle_end_time);

-- 创建唯一约束避免重复分析
CREATE UNIQUE INDEX idx_employee_customer_unique_analysis ON xca_employee_customer_chat_analysis(qw_account_id, customer_id, cycle_start_time, cycle_end_time);
CREATE UNIQUE INDEX idx_employee_daily_unique_analysis ON xca_employee_daily_analysis(qw_account_id, cycle_start_time, cycle_end_time);
