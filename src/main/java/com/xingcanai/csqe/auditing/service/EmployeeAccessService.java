package com.xingcanai.csqe.auditing.service;

import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.EmployeeGroup;
import com.xingcanai.csqe.auditing.entity.EmployeeGroupRepository;
import com.xingcanai.csqe.auditing.entity.EmployeeRepository;
import com.xingcanai.csqe.auth.config.JwtProperties;
import com.xingcanai.csqe.auth.entity.AccountUser;
import com.xingcanai.csqe.auth.entity.AccountUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeAccessService {

    private final AccountUserRepository accountUserRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeGroupRepository employeeGroupRepository;
    private final JwtProperties jwtProperties;

    public EmployeeAccessService(
            AccountUserRepository accountUserRepository,
            EmployeeRepository employeeRepository,
            EmployeeGroupRepository employeeGroupRepository,
            JwtProperties jwtProperties) {
        this.accountUserRepository = accountUserRepository;
        this.employeeRepository = employeeRepository;
        this.employeeGroupRepository = employeeGroupRepository;
        this.jwtProperties = jwtProperties;
    }

    public boolean canViewAllEmployees(String username) {
        if (jwtProperties.isDevBypass() && jwtProperties.getDevBypassUser().equals(username)) {
            return true;
        }

        return accountUserRepository.findByUsernameAndIsDeletedFalse(username)
                .map(account -> AccountUser.ACCOUNT_TYPE_ADMIN.equals(account.getAccountType()))
                .orElse(false);
    }

    public List<Employee> findManagedEmployees(String username) {
        AccountUser account = accountUserRepository.findByUsernameAndIsDeletedFalse(username)
                .orElse(null);
        if (account == null) {
            return List.of();
        }

        Employee leaderEmployee = employeeRepository.findFirstByAccountUserIdAndIsDeletedFalse(account.getId())
                .orElse(null);
        if (leaderEmployee == null) {
            return List.of();
        }

        List<String> groupIds = employeeGroupRepository.findByLeaderIdAndIsDeletedFalse(leaderEmployee.getId())
                .stream()
                .map(EmployeeGroup::getId)
                .toList();
        if (groupIds.isEmpty()) {
            return List.of();
        }

        return employeeRepository.findByGroupIdInAndIsDeletedFalse(groupIds);
    }

    public List<String> findManagedEmployeeIds(String username) {
        return findManagedEmployees(username).stream()
                .map(Employee::getId)
                .toList();
    }
}
