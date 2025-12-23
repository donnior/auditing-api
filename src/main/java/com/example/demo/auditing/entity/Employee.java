package com.example.demo.auditing.entity;

import com.github.f4b6a3.ulid.UlidCreator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Table(name = "xca_employee")
@Data
public class Employee {

    @Id
    @Column(name = "id")
    private String id = UlidCreator.getUlid().toLowerCase();

    @Column(name = "qw_id")
    private String qwId;

    @Column(name = "name")
    private String name;

    @Column(name = "status")
    private Integer status;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

}
