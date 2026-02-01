package com.xingcanai.csqe.auditing.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeGroupRepository extends JpaRepository<EmployeeGroup, String>, JpaSpecificationExecutor<EmployeeGroup> {
    
    /**
     * 根据名称查找分组
     */
    Optional<EmployeeGroup> findByNameAndIsDeletedFalse(String name);
    
    /**
     * 查找所有未删除的分组
     */
    List<EmployeeGroup> findByIsDeletedFalseOrderByCreatedAtDesc();
    
    /**
     * 根据组长ID查找分组
     */
    List<EmployeeGroup> findByLeaderIdAndIsDeletedFalse(String leaderId);
}
