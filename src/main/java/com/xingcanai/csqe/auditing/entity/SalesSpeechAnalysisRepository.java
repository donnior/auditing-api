package com.xingcanai.csqe.auditing.entity;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SalesSpeechAnalysisRepository extends JpaRepository<SalesSpeechAnalysis, String> {

    Optional<SalesSpeechAnalysis> findFirstByEmployeeIdAndEvalPeriodOrderByUpdateTimeDescCreateTimeDesc(
            String employeeId,
            LocalDate evalPeriod);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select a
        from SalesSpeechAnalysis a
        where a.employeeId = :employeeId
          and a.evalPeriod = :evalPeriod
          and a.promptVersion = :promptVersion
        """)
    Optional<SalesSpeechAnalysis> findForUpdate(
            @Param("employeeId") String employeeId,
            @Param("evalPeriod") LocalDate evalPeriod,
            @Param("promptVersion") String promptVersion);
}
