
张三 2026-01-11 第一周检测报告
张三 2026-01-11 第二周检测报告
张三 2026-01-11 第三周检测报告
张三 2026-01-11 第四周检测报告

李四 2026-01-11 第一周检测报告
李四 2026-01-11 第二周检测报告
李四 2026-01-11 第三周检测报告
李四 2026-01-11 第四周检测报告

张三 2026-01-04 第一周检测报告
张三 2026-01-04 第二周检测报告
张三 2026-01-04 第三周检测报告
张三 2026-01-04 第四周检测报告

李四 2026-01-04 第一周检测报告
李四 2026-01-04 第二周检测报告
李四 2026-01-04 第三周检测报告
李四 2026-01-04 第四周检测报告



report_view_test
```
 SELECT ed.employee_id,
    ed.eval_time,
		ed.eval_period,
    ed.eval_type,
    concat(ed.employee_id, ed.eval_type, ed.eval_time, ed.eval_period) AS id,
    count(*) AS total_customers,
    sum(ed.has_material_send) AS total_material_send,
    sum(ed.has_course_remind) AS total_course_remind,
    sum(ed.has_homework_publish) AS total_homework_publish,
    sum(ed.has_week_material_send) AS total_week_material_send,
    sum(ed.has_sunday_link_send) AS total_sunday_link_send,
    sum(ed.has_feedback_track) AS total_feedback_track,
    sum(ed.has_risk_word_trigger) AS total_risk_word_trigger,
    sum(ed.has_introduce_teacher) AS total_introduce_teacher,
    sum(ed.has_introduce_course) AS total_introduce_course,
    sum(ed.has_introduce_schedule) AS total_introduce_schedule,
    sum(ed.has_introduce_course_time) AS total_introduce_course_time,
    sum(ed.has_order_check) AS total_order_check,
    sum(ed.has_introduce_course * ed.has_introduce_teacher * ed.has_introduce_schedule * ed.has_introduce_course_time * ed.has_order_check) AS total_introduce_completed
   FROM xca_evaluation_detail ed
  GROUP BY ed.employee_id, ed.eval_time, ed.eval_type, ed.eval_period

```

view_report_full
```
 SELECT r.employee_id,
    r.eval_time,
    r.eval_type,
    r.id,
    r.total_customers,
    r.total_material_send,
    r.total_course_remind,
    r.total_homework_publish,
    r.total_week_material_send,
    r.total_sunday_link_send,
    r.total_feedback_track,
    r.total_risk_word_trigger,
    r.total_introduce_teacher,
    r.total_introduce_course,
    r.total_introduce_schedule,
    r.total_introduce_course_time,
    r.total_order_check,
    r.total_introduce_completed,
    em.name AS employee_name,
    em.qw_id AS employee_qw_id
   FROM report_view_test r
     JOIN xca_employee em ON em.id::text = r.employee_id::text
```

```
select * from xca_qw_chat_msg where from_id = 'LiBingXin' order by msg_time desc


select * from xca_qw_chat_msg  order by msg_time desc

select count(*) from xca_qw_chat_msg

select m.msg_time from xca_qw_chat_msg m  order by msg_time desc limit 1

select DISTINCT from_id from xca_qw_chat_msg m where m.from_id not like 'wmy%'

select count(DISTINCT from_id) from xca_qw_chat_msg

select DISTINCT accept_id from xca_qw_chat_msg m where m.from_id = 'QingHuaShaShaLaoShi' and m.accept_type = 1 order by accept_id asc

01-07 16:35

01-05  16:35
01-04  16:35

select accept_id, min(m.msg_time) AS first_msg_time from xca_qw_chat_msg m where m.from_id = 'QingHuaShaShaLaoShi' and m.accept_type = 1 group by accept_id ORDER BY first_msg_time DESC;

```
