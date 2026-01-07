package com.example.demo.auditing.entity;

import lombok.Data;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

/**
 * 企业微信聊天消息实体
 */
// @Entity
// @Table(name = "xca_qw_customer")
@Data
public class WxChatCustomer {

    @Id
    @Column(name = "external_user_id")
    private String externalUserid;

    @Column(name = "external_name")
    private String externalName;

    @Column(name = "card_name")
    private String cardName;

    @Column(name = "start_time")
    private String startTime;

    @Column(name = "week_number")
    private Integer weekNumber;

    @Column(name = "update_time")
    private ZonedDateTime updateTime;

    @Column(name = "create_time")
    private ZonedDateTime createTime;
}
