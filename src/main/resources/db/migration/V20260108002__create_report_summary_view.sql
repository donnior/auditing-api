CREATE OR REPLACE VIEW report_summary_view AS
SELECT
    r.id,
    r.employee_id,
    r.eval_period,
    r.eval_type,
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
    em.name  AS employee_name,
    em.qw_id AS employee_qw_id
FROM (
    SELECT
        ed.employee_id,
        ed.eval_period,
        ed.eval_type,
        concat(ed.employee_id, ed.eval_period, ed.eval_type) AS id,
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
        sum(
            ed.has_introduce_course
          * ed.has_introduce_teacher
          * ed.has_introduce_schedule
          * ed.has_introduce_course_time
          * ed.has_order_check
        ) AS total_introduce_completed
    FROM xca_evaluation_detail ed
    GROUP BY
        ed.employee_id,
        ed.eval_period,
        ed.eval_type
) r
JOIN xca_employee em
  ON em.id::text = r.employee_id::text;
