CREATE TABLE xca_camp_customer_daily_performance (
    id varchar(800) NOT NULL,
    stat_date date NOT NULL,
    camp_tag varchar(64) NOT NULL DEFAULT '',
    sys_user_id varchar(255) NOT NULL,
    sales_name varchar(255) DEFAULT NULL,
    group_name varchar(255) DEFAULT NULL,
    external_userid varchar(255) NOT NULL,
    external_name varchar(255) DEFAULT NULL,
    gmv_amount numeric(14,2) NOT NULL DEFAULT 0,
    refund_amount numeric(14,2) NOT NULL DEFAULT 0,
    sync_time timestamptz DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_camp_customer_daily_performance_unique
    ON xca_camp_customer_daily_performance (stat_date, sys_user_id, external_userid, camp_tag);

CREATE INDEX idx_camp_customer_daily_performance_stat_date
    ON xca_camp_customer_daily_performance (stat_date);

CREATE INDEX idx_camp_customer_daily_performance_sys_user_stat_date
    ON xca_camp_customer_daily_performance (sys_user_id, stat_date);
