package com.xingcanai.csqe.auditing.entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationDetailRepository extends JpaRepository<EvaluationDetail, String>, JpaSpecificationExecutor<EvaluationDetail> {

    Optional<EvaluationDetail> findByEmployeeIdAndCustomerIdAndEvalTypeAndEvalPeriod(
        String employeeId, String customerId, String evalType, String evalPeriod);
}
