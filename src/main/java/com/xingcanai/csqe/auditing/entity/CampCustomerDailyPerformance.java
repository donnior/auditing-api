package com.xingcanai.csqe.auditing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * 学员每日业绩记录。
 */
@Entity
@Table(name = "xca_camp_customer_daily_performance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampCustomerDailyPerformance {

    /**
     * 组合主键：statDate + sysUserId + externalUserid + campTag。
     */
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "stat_date")
    private LocalDate statDate;

    @Column(name = "camp_tag")
    private String campTag;

    @Column(name = "sys_user_id")
    private String sysUserId;

    @Column(name = "sales_name")
    private String salesName;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "external_userid")
    private String externalUserid;

    @Column(name = "external_name")
    private String externalName;

    @Column(name = "gmv_amount")
    private BigDecimal gmvAmount;

    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    @Column(name = "sync_time")
    private ZonedDateTime syncTime;
}
