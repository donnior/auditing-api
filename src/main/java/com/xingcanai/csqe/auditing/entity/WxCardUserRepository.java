package com.xingcanai.csqe.auditing.entity;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WxCardUserRepository extends JpaRepository<WxCardUser, String> {

    WxCardUser findTopByOrderByUpdateTimeDesc();

    WxCardUser findTopByOrderByCreateTimeDesc();

    //TODO: 根据员工qwid和startTime在指定时间区间内查询
    @Query("""
        select u
        from WxCardUser u
        where u.employeeQwid = :employeeQwid
          and u.startTime between :startTime and :endTime
        order by u.startTime asc
        """)
    List<WxCardUser> findByEmployeeQwidAndTimeRange(
        @Param("employeeQwid") String employeeQwid,
        @Param("startTime") ZonedDateTime startTime,
        @Param("endTime") ZonedDateTime endTime
    );
}
