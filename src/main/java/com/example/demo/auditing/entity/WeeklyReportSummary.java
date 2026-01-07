package com.example.demo.auditing.entity;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Data;

@Entity
@Table(name = "view_report_full")
@Data
@Immutable
public class WeeklyReportSummary {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "employee_id")
    private String employeeId;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "employee_qw_id")
    private String employeeQwId;

    @Column(name = "eval_time")
    private String evalTime;

    @Column(name = "eval_type")
    private String evalType;

    @Column(name = "total_customers")
    private int totalCustomers;

    // //TODO to deprecate
    // @Column(name = "has_introduce_course_ratio")
    // private float hasIntroduceCourseRatio;

    // // TODO to deprecate
    // @Column(name = "full_completed_1_ratio")
    // private float fullCompleted1Ratio;

    @Column(name = "total_material_send")
    private int totalMaterialSend; // 总资料发送次数

    @Column(name = "total_course_remind")
    private int totalCourseRemind; // 总到课提醒次数

    @Column(name = "total_homework_publish")
    private int totalHomeworkPublish; // 总课后作业发布次数

    @Column(name = "total_week_material_send")
    private int totalWeekMaterialSend; // 总下周资料发送次数

    @Column(name="total_sunday_link_send")
    private int totalSundayLinkSend; // 总周日螳螂销转链接发送次数

    @Column(name="total_feedback_track")
    private int totalFeedbackTrack; // 总课后学习感受追踪次数

    @Column(name="total_risk_word_trigger")
    private int totalRiskWordTrigger; // 总触发风险词次数


    @Column(name="total_introduce_teacher")
    private int totalIntroduceTeacher; // 总介绍老师次数

    @Column(name="total_introduce_course")
    private int totalIntroduceCourse; // 总介绍课程次数

    @Column(name="total_introduce_schedule")
    private int totalIntroduceSchedule; // 总介绍课表次数

    @Column(name="total_introduce_course_time")
    private int totalIntroduceCourseTime; // 总介绍上课时间次数

    @Column(name="total_order_check")
    private int totalOrderCheck; // 总索要订单并核对次数

    @Column(name="total_introduce_completed")
    private int totalIntroduceCompleted; // 总介绍完成次数

}
