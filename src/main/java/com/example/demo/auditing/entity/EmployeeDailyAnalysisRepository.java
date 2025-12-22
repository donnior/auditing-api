package com.example.demo.auditing.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeDailyAnalysisRepository extends JpaRepository<EmployeeDailyAnalysis, String> {

    /**
     * 查询员工在指定时间范围内的分析记录
     */
    Optional<EmployeeDailyAnalysis> findByQwAccountIdAndCycleStartTimeAndCycleEndTime(
            String qwAccountId, ZonedDateTime cycleStartTime, ZonedDateTime cycleEndTime);

    /**
     * 分页查询指定时间范围内的员工分析记录
     */
    Page<EmployeeDailyAnalysis> findByCycleStartTimeAndCycleEndTimeOrderByServiceQualityScoreDesc(
            ZonedDateTime cycleStartTime, ZonedDateTime cycleEndTime, Pageable pageable);

    /**
     * 查询指定时间范围内所有员工分析记录
     */
    List<EmployeeDailyAnalysis> findByCycleStartTimeAndCycleEndTime(
            ZonedDateTime cycleStartTime, ZonedDateTime cycleEndTime);

    /**
     * 检查是否已存在分析记录
     */
    boolean existsByQwAccountIdAndCycleStartTimeAndCycleEndTime(
            String qwAccountId, ZonedDateTime cycleStartTime, ZonedDateTime cycleEndTime);

    /**
     * 查询最新的分析记录
     */
    @Query("SELECT e FROM EmployeeDailyAnalysis e " +
           "ORDER BY e.cycleEndTime DESC, e.serviceQualityScore DESC")
    Page<EmployeeDailyAnalysis> findLatestAnalysis(Pageable pageable);

    /**
     * 根据员工ID查询历史分析记录
     */
    @Query("SELECT e FROM EmployeeDailyAnalysis e " +
           "WHERE e.qwAccountId = :qwAccountId " +
           "ORDER BY e.cycleEndTime DESC")
    Page<EmployeeDailyAnalysis> findByQwAccountIdOrderByCycleEndTimeDesc(
            @Param("qwAccountId") String qwAccountId, Pageable pageable);

    /**
     * 统计指定时间范围内的员工表现概况
     */
    @Query("SELECT " +
           "COUNT(e) as totalEmployees, " +
           "AVG(e.serviceQualityScore) as avgQualityScore, " +
           "SUM(e.totalViolations) as totalViolations, " +
           "AVG(e.overallSatisfaction) as avgSatisfaction " +
           "FROM EmployeeDailyAnalysis e " +
           "WHERE e.cycleStartTime = :cycleStartTime " +
           "AND e.cycleEndTime = :cycleEndTime")
    Object[] getOverviewStatistics(@Param("cycleStartTime") ZonedDateTime cycleStartTime,
                                   @Param("cycleEndTime") ZonedDateTime cycleEndTime);

    /**
     * 统计所有员工表现概况
     */
    @Query("SELECT " +
           "COUNT(e) as totalEmployees, " +
           "AVG(e.serviceQualityScore) as avgQualityScore, " +
           "SUM(e.totalViolations) as totalViolations, " +
           "AVG(e.overallSatisfaction) as avgSatisfaction " +
           "FROM EmployeeDailyAnalysis e")
    Object[] getAllOverviewStatistics();

    /**
     * 分页查询所有员工分析记录，按服务质量分数降序排列
     */
    @Query("SELECT e FROM EmployeeDailyAnalysis e " +
           "ORDER BY e.serviceQualityScore DESC, e.cycleEndTime DESC")
    Page<EmployeeDailyAnalysis> findAllOrderByServiceQualityScoreDesc(Pageable pageable);
}
