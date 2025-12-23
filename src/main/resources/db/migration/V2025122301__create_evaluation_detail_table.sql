-- 评估明细表
CREATE TABLE xca_evaluation_detail (
    id varchar(36) NOT NULL,
    employee_id varchar(255) DEFAULT NULL,
    employee_qw_id varchar(255) DEFAULT NULL,
    customer_id varchar(255) DEFAULT NULL,
    customer_name varchar(255) DEFAULT NULL,
    eval_time varchar(255) DEFAULT NULL, -- 评估时间
    eval_type varchar(255) DEFAULT NULL, -- 评估类型
    has_introduce_teacher integer NOT NULL DEFAULT 0, -- 是否有介绍老师
    has_introduce_course integer NOT NULL DEFAULT 0, -- 是否有介绍课程
    has_introduce_schedule integer NOT NULL DEFAULT 0, -- 是否有介绍课表
    has_introduce_course_time integer NOT NULL DEFAULT 0, -- 是否有介绍上课时间
    has_order_check integer NOT NULL DEFAULT 0, -- 是否有索要订单并核对
    has_material_send integer NOT NULL DEFAULT 0, -- 是否有完成资料发送
    has_course_remind integer NOT NULL DEFAULT 0, -- 是否有完成到课提醒
    has_homework_publish integer NOT NULL DEFAULT 0, -- 是否有完成课后作业发布
    has_feedback_track integer NOT NULL DEFAULT 0, -- 是否有完成课后学习感受追踪
    has_week_material_send integer NOT NULL DEFAULT 0, -- 是否有完成下周资料发送
    has_sunday_link_send integer NOT NULL DEFAULT 0, -- 是否有发送周日螳螂销转链接
    has_risk_word_trigger integer NOT NULL DEFAULT 0, -- 是否有触发风险词
    CONSTRAINT xca_evaluation_detail_pkey PRIMARY KEY (id)
);

-- 常用查询索引
CREATE INDEX idx_xca_evaluation_detail_employee_id ON xca_evaluation_detail(employee_id);
CREATE INDEX idx_xca_evaluation_detail_employee_qw_id ON xca_evaluation_detail(employee_qw_id);
CREATE INDEX idx_xca_evaluation_detail_customer_id ON xca_evaluation_detail(customer_id);
CREATE INDEX idx_xca_evaluation_detail_eval_time ON xca_evaluation_detail(eval_time);
