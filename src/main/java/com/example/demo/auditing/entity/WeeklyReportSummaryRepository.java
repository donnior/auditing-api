package com.example.demo.auditing.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyReportSummaryRepository extends JpaRepository<WeeklyReportSummary, String>, JpaSpecificationExecutor<WeeklyReportSummary> {
}
