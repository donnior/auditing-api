-- 员工分组表
CREATE TABLE xca_employee_group (
    id varchar(36) NOT NULL,
    name varchar(255) NOT NULL,
    description varchar(500) DEFAULT NULL,
    leader_id varchar(36) DEFAULT NULL,
    is_deleted boolean DEFAULT false,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT xca_employee_group_pkey PRIMARY KEY (id)
);

-- 常用查询索引
CREATE INDEX idx_xca_employee_group_name ON xca_employee_group(name);
CREATE INDEX idx_xca_employee_group_leader_id ON xca_employee_group(leader_id);
CREATE INDEX idx_xca_employee_group_is_deleted ON xca_employee_group(is_deleted);

-- 添加员工表的分组字段
ALTER TABLE xca_employee ADD COLUMN group_id varchar(36) DEFAULT NULL;
CREATE INDEX idx_xca_employee_group_id ON xca_employee(group_id);

-- 添加备注：每个员工属于一个分组（可选），每个分组有一个组长（可选）
COMMENT ON TABLE xca_employee_group IS '员工分组表';
COMMENT ON COLUMN xca_employee_group.name IS '分组名称';
COMMENT ON COLUMN xca_employee_group.description IS '分组描述';
COMMENT ON COLUMN xca_employee_group.leader_id IS '组长员工ID';
COMMENT ON COLUMN xca_employee.group_id IS '所属分组ID';
