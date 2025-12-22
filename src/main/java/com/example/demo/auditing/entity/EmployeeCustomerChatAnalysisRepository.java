package com.example.demo.auditing.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeCustomerChatAnalysisRepository extends JpaRepository<EmployeeCustomerChatAnalysis, String> {

    /**
     * 查询员工在指定时间范围内的所有客户分析记录
     */
    List<EmployeeCustomerChatAnalysis> findByQwAccountIdAndCycleStartTimeAndCycleEndTime(
            String qwAccountId, ZonedDateTime cycleStartTime, ZonedDateTime cycleEndTime);

    /**
     * 查询员工与特定客户在指定时间范围内的分析记录
     */
    Optional<EmployeeCustomerChatAnalysis> findByQwAccountIdAndCustomerIdAndCycleStartTimeAndCycleEndTime(
            String qwAccountId, String customerId, ZonedDateTime cycleStartTime, ZonedDateTime cycleEndTime);

    /**
     * 查询指定时间范围内所有的分析记录
     */
    List<EmployeeCustomerChatAnalysis> findByCycleStartTimeAndCycleEndTime(
            ZonedDateTime cycleStartTime, ZonedDateTime cycleEndTime);

    /**
     * 检查是否已存在分析记录
     */
    boolean existsByQwAccountIdAndCustomerIdAndCycleStartTimeAndCycleEndTime(
            String qwAccountId, String customerId, ZonedDateTime cycleStartTime, ZonedDateTime cycleEndTime);

    /**
     * 根据员工ID和时间范围查询分析统计
     */
    @Query("SELECT " +
           "COUNT(e) as totalCustomers, " +
           "SUM(e.messageCount) as totalMessages, " +
           "AVG(e.responseTimeAvg) as avgResponseTime, " +
           "AVG(e.satisfactionScore) as avgSatisfaction, " +
           "SUM(e.violationCount) as totalViolations, " +
           "AVG(e.serviceQualityScore) as avgQualityScore " +
           "FROM EmployeeCustomerChatAnalysis e " +
           "WHERE e.qwAccountId = :qwAccountId " +
           "AND e.cycleStartTime = :cycleStartTime " +
           "AND e.cycleEndTime = :cycleEndTime")
    Object[] getEmployeeStatistics(@Param("qwAccountId") String qwAccountId,
                                   @Param("cycleStartTime") ZonedDateTime cycleStartTime,
                                   @Param("cycleEndTime") ZonedDateTime cycleEndTime);
}
