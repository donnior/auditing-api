ALTER TABLE xca_evaluation_detail
    ADD COLUMN IF NOT EXISTS camp_tag varchar(64) DEFAULT NULL;

UPDATE xca_evaluation_detail ed
SET camp_tag = COALESCE(cu.camp_tag, '')
FROM xca_qw_card_user cu
WHERE ed.customer_id = cu.external_userid
  AND ed.employee_qw_id = cu.employee_qwid
  AND ed.camp_tag IS NULL;

WITH weekly_details AS (
    SELECT
        ed.id,
        to_date(ed.eval_period, 'YYYY-MM-DD') - 1 AS business_period_end,
        to_date(ed.camp_tag, 'YYYYMMDD') AS camp_date
    FROM xca_evaluation_detail ed
    WHERE ed.eval_type <> 'WITHIN_48_HOURS'
      AND ed.eval_period ~ '^[0-9]{4}-[0-9]{2}-[0-9]{2}$'
      AND ed.camp_tag ~ '^[0-9]{8}$'
      AND extract(dow from to_date(ed.eval_period, 'YYYY-MM-DD')) = 0
)
UPDATE xca_evaluation_detail ed
SET
    eval_period = to_char(w.business_period_end, 'YYYY-MM-DD'),
    biz_date = to_char(w.business_period_end, 'YYYY-MM-DD'),
    eval_type = CASE (w.business_period_end - w.camp_date)
        WHEN 0 THEN 'FIRST_WEEK'
        WHEN 7 THEN 'SECOND_WEEK'
        WHEN 14 THEN 'THIRD_WEEK'
        WHEN 21 THEN 'FOURTH_WEEK'
        ELSE ed.eval_type
    END
FROM weekly_details w
WHERE ed.id = w.id
  AND (w.business_period_end - w.camp_date) IN (0, 7, 14, 21);

CREATE INDEX IF NOT EXISTS idx_xca_evaluation_detail_camp_tag
    ON xca_evaluation_detail(camp_tag);
