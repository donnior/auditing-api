package com.xingcanai.csqe.auditing.entity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    Optional<Employee> findFirstByAccountUserIdAndIsDeletedFalse(String accountUserId);

    List<Employee> findByGroupIdInAndIsDeletedFalse(List<String> groupIds);
}
