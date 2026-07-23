CREATE TABLE xca_sales_speech_analysis (
    id varchar(36) NOT NULL,
    employee_id varchar(36) NOT NULL,
    employee_qw_id varchar(255) NOT NULL,
    eval_period date NOT NULL,
    period_start_time timestamptz NOT NULL,
    period_end_time timestamptz NOT NULL,
    status varchar(16) NOT NULL,
    prompt_version varchar(64) NOT NULL,
    report_markdown text,
    model_name varchar(128),
    finish_reason varchar(64),
    prompt_tokens bigint,
    completion_tokens bigint,
    total_tokens bigint,
    error_code varchar(64),
    error_message text,
    requested_by varchar(64) NOT NULL,
    create_time timestamptz NOT NULL DEFAULT now(),
    update_time timestamptz NOT NULL DEFAULT now(),
    completed_time timestamptz,
    CONSTRAINT xca_sales_speech_analysis_pkey PRIMARY KEY (id),
    CONSTRAINT uk_sales_speech_analysis_employee_period_prompt
        UNIQUE (employee_id, eval_period, prompt_version),
    CONSTRAINT ck_sales_speech_analysis_status
        CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED'))
);

