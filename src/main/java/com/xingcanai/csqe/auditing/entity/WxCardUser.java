package com.xingcanai.csqe.auditing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * 在读学员课程周序（卡片用户）实体
 */
@Entity
@Table(name = "xca_qw_card_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WxCardUser {

    @Id
    @Column(name = "external_userid")
    private String externalUserid;

    @Column(name = "external_name")
    private String externalName;

    @Column(name = "card_name")
    private String cardName;

    @Column(name = "start_time")
    private ZonedDateTime startTime;

    @Column(name = "create_time")
    private ZonedDateTime createTime;

    @Column(name = "update_time")
    private ZonedDateTime updateTime;

    @Column(name = "week_number")
    private Integer weekNumber;
}

