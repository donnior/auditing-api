package com.example.demo.staff.entity;

import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.github.f4b6a3.ulid.UlidCreator;

import lombok.Data;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "xc_qwaccount")
@Data
@AllArgsConstructor
public class QWAccount {
    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "qwid")
    private String qwid;

    @Column(name = "auto_analyze")
    private Boolean autoAnalyze = false;

    @Column(name = "create_time")
    private ZonedDateTime createTime;

    @Column(name = "update_time")
    private ZonedDateTime updateTime;

    public QWAccount() {
        this.id = UlidCreator.getUlid().toLowerCase();
        this.autoAnalyze = false;
        this.createTime = ZonedDateTime.now();
        this.updateTime = ZonedDateTime.now();
    }

    public void update() {
        this.updateTime = ZonedDateTime.now();
    }

}
