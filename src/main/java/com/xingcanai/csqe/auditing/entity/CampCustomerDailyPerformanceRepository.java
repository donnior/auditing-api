package com.xingcanai.csqe.auditing.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CampCustomerDailyPerformanceRepository extends JpaRepository<CampCustomerDailyPerformance, String> {

    CampCustomerDailyPerformance findTopByOrderByStatDateDesc();

    @Query("""
        select p
        from CampCustomerDailyPerformance p
        where p.sysUserId = :sysUserId
          and p.statDate between :startDate and :endDate
        order by p.statDate asc, p.externalUserid asc, p.campTag asc, p.id asc
        """)
    List<CampCustomerDailyPerformance> findRawBySysUserIdAndStatDateRange(
            @Param("sysUserId") String sysUserId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        select
            p.campTag as campTag,
            sum(p.gmvAmount) as gmvAmount,
            sum(p.refundAmount) as refundAmount,
            count(p.id) as recordCount
        from CampCustomerDailyPerformance p
        where p.sysUserId = :sysUserId
          and p.statDate between :startDate and :endDate
        group by p.campTag
        order by p.campTag asc
        """)
    List<CampCustomerDailyPerformanceSummary> summarizeBySysUserIdAndStatDateRange(
            @Param("sysUserId") String sysUserId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
