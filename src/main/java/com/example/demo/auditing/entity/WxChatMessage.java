package com.example.demo.auditing.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

/**
 * 企业微信聊天消息实体
 */
@Entity
@Table(name = "xca_qw_chat_msg")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WxChatMessage {

    @Id
    @Column(name = "msg_id")
    private String msgId;

    @Column(name = "from_id")
    private String fromId;

    @Column(name = "from_name")
    private String fromName;

    @Column(name = "accept_id")
    private String acceptId;

    @Column(name = "accept_type")
    private Integer acceptType;

    @Column(name = "accept_name")
    private String acceptName;

    @Column(name = "msg_type")
    private String msgType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "data_seq")
    private Long dataSeq;

    @Column(name = "msg_time")
    private ZonedDateTime msgTime;

    @Column(name = "create_time")
    private ZonedDateTime createTime;

    // @PrePersist
    // protected void onCreate() {
    //     if (createTime == null) {
    //         createTime = ZonedDateTime.now();
    //     }
    // }
}
