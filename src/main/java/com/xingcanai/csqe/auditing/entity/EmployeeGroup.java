package com.xingcanai.csqe.auditing.entity;

import com.github.f4b6a3.ulid.UlidCreator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工分组实体
 */
@Entity
@Table(name = "xca_employee_group")
@Data
public class EmployeeGroup {

    @Id
    @Column(name = "id")
    private String id = UlidCreator.getUlid().toLowerCase();

    /**
     * 分组名称
     */
    @Column(name = "name")
    private String name;

    /**
     * 分组描述
     */
    @Column(name = "description")
    private String description;

    /**
     * 组长ID（关联员工表）
     */
    @Column(name = "leader_id")
    private String leaderId;

    /**
     * 是否删除
     */
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
