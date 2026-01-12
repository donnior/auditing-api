package com.example.demo.auth.entity;

import java.time.ZonedDateTime;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "xca_account_user")
@Data
public class AccountUser {

    public static final Integer STATUS_ACTIVE = 1;
    public static final Integer STATUS_INACTIVE = 0;

    @Id
    @Column(name = "id")
    private String id = UlidCreator.getUlid().toLowerCase();

    @Column(name = "username")
    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "status")
    private Integer status = STATUS_ACTIVE;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "create_time")
    private ZonedDateTime createTime = ZonedDateTime.now();

    @Column(name = "update_time")
    private ZonedDateTime updateTime = ZonedDateTime.now();
}
