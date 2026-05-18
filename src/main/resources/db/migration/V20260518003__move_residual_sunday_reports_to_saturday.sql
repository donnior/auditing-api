UPDATE xca_evaluation_detail ed
SET
    eval_period = to_char(to_date(ed.eval_period, 'YYYY-MM-DD') - 1, 'YYYY-MM-DD'),
    biz_date = to_char(to_date(ed.eval_period, 'YYYY-MM-DD') - 1, 'YYYY-MM-DD')
WHERE ed.eval_period ~ '^[0-9]{4}-[0-9]{2}-[0-9]{2}$'
  AND extract(dow from to_date(ed.eval_period, 'YYYY-MM-DD')) = 0;
