package com.xingcanai.csqe.auditing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.ToString;

import java.time.ZonedDateTime;

import com.github.f4b6a3.ulid.UlidCreator;

@Data
@Entity
@Table(name = "xca_evaluation_detail")
@ToString
public class EvaluationDetail {

    @Id
    @Column(name = "id")
    private String id = UlidCreator.getUlid().toLowerCase();

    @Column(name = "employee_id")
    private String employeeId;

    @Column(name = "employee_qw_id")
    private String employeeQwId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "eval_time")
    private String evalTime;   //评估时间

    @Column(name = "eval_period")
    private String evalPeriod; // 评估周期

    @Column(name = "eval_type")
    private String evalType;   //评估类型

    @Column(name = "has_introduce_teacher")
    private int hasIntroduceTeacher;   //是否有介绍老师

    @Column(name = "has_introduce_course")
    private int hasIntroduceCourse;   //是否有介绍课程

    @Column(name = "has_introduce_schedule")
    private int hasIntroduceSchedule;   //是否有介绍课表

    @Column(name = "has_introduce_course_time")
    private int hasIntroduceCourseTime;   //是否有介绍上课时间

    @Column(name = "has_order_check")
    private int hasOrderCheck;   //是否有索要订单并核对

    @Column(name = "has_material_send")
    private int hasMaterialSend;   //是否有完成资料发送

    @Column(name = "has_course_remind")
    private int hasCourseRemind;   //是否有完成到课提醒

    @Column(name = "has_homework_publish")
    private int hasHomeworkPublish;   //是否有完成课后作业发布

    @Column(name = "has_feedback_track")
    private int hasFeedbackTrack;   //是否有完成课后学习感受追踪

    @Column(name = "has_week_material_send")
    private int hasWeekMaterialSend;   //是否有完成下周资料发送

    @Column(name = "has_sunday_link_send")
    private int hasSundayLinkSend;   //是否有发送周日螳螂销转链接

    @Column(name = "has_risk_word_trigger")
    private int hasRiskWordTrigger;   //是否有触发风险词

    @Column(name = "chat_start_time")
    private ZonedDateTime chatStartTime;   //聊天开始时间

    @Column(name = "chat_end_time")
    private ZonedDateTime chatEndTime;   //聊天结束时间

    @Column(name = "biz_date")
    private String bizDate;   //业务日期

}
