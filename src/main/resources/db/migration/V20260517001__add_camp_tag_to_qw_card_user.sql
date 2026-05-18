-- 为在读学员表增加期数字段，比如 0305、0312、0319
ALTER TABLE xca_qw_card_user
    ADD COLUMN IF NOT EXISTS camp_tag varchar(64) DEFAULT NULL;

COMMENT ON COLUMN xca_qw_card_user.camp_tag IS '学员期数标签，例如 0305、0312、0319';

CREATE INDEX IF NOT EXISTS idx_xca_qw_card_user_employee_qwid_camp_tag
    ON xca_qw_card_user(employee_qwid, camp_tag);
