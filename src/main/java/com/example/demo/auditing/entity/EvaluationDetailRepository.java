package com.example.demo.auditing.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationDetailRepository extends JpaRepository<EvaluationDetail, String> {
}
