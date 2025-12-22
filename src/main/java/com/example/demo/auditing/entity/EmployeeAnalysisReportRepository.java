package com.example.demo.auditing.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeAnalysisReportRepository extends JpaRepository<EmployeeAnalysisReport, String> {

    /**
     * 查询员工在指定时间范围内的所有客户分析记录
     */
    List<EmployeeAnalysisReportRepository> findByQwAccountIdAndReportTypeAndCycleStartTimeAndCycleEndTime(
            String qwAccountId, String reportType, ZonedDateTime cycleStartTime, ZonedDateTime cycleEndTime);

    /**
     * 查询员工与特定客户在指定时间范围内的分析记录
     */
    Optional<EmployeeAnalysisReportRepository> findByQwAccountIdAndCustomerIdAndCycleStartTimeAndCycleEndTime(
            String qwAccountId, String customerId, ZonedDateTime cycleStartTime, ZonedDateTime cycleEndTime);

    /**
     * 查询指定时间范围内所有的分析记录
     */
    List<EmployeeAnalysisReportRepository> findByCycleStartTimeAndCycleEndTime(
            ZonedDateTime cycleStartTime, ZonedDateTime cycleEndTime);

    List<EmployeeAnalysisReportRepository> findByReportTypeAndCycleStartTimeAndCycleEndTime(String reportType,
                    ZonedDateTime cycleStartTime, ZonedDateTime cycleEndTime);

}
