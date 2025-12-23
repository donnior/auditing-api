-- 员工表
CREATE TABLE xca_employee (
    id varchar(36) NOT NULL,
    qw_id varchar(255) DEFAULT NULL,
    name varchar(255) DEFAULT NULL,
    status integer DEFAULT NULL,
    is_deleted boolean DEFAULT NULL,
    CONSTRAINT xca_employee_pkey PRIMARY KEY (id)
);

-- 常用查询索引
CREATE INDEX idx_xca_employee_qw_id ON xca_employee(qw_id);
CREATE INDEX idx_xca_employee_status ON xca_employee(status);
CREATE INDEX idx_xca_employee_is_deleted ON xca_employee(is_deleted);
CREATE INDEX idx_xca_employee_status_is_deleted ON xca_employee(status, is_deleted);
