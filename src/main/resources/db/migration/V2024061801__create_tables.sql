CREATE TABLE xc_qwaccount (
	id varchar(36) NOT NULL,
	name varchar(255) NULL,
	description text NULL,
	qwid text NULL,
	create_time timestamptz NULL,
	update_time timestamptz NULL,
	CONSTRAINT xc_qwaccount_pkey PRIMARY KEY (id)
);
