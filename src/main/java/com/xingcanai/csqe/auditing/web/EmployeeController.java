package com.xingcanai.csqe.auditing.web;

import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.EmployeeGroup;
import com.xingcanai.csqe.auditing.entity.EmployeeGroupRepository;
import com.xingcanai.csqe.auditing.entity.EmployeeRepository;
import com.xingcanai.csqe.auth.entity.AccountUser;
import com.xingcanai.csqe.auth.entity.AccountUserRepository;
import com.xingcanai.csqe.common.XCPageRequest;
import com.github.f4b6a3.ulid.UlidCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工管理接口
 */
@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeGroupRepository employeeGroupRepository;

    @Autowired
    private AccountUserRepository accountUserRepository;

    /**
     * 员工列表（默认按id倒序，包含分组信息）
     */
    @GetMapping("")
    public Page<EmployeeVO> listEmployees(XCPageRequest pageRequest) {
        var req = pageRequest.toPageRequest().withSort(Sort.by("id").descending());
        Page<Employee> employeePage = employeeRepository.findAll(req);
        
        // 获取所有分组，用于映射分组名称
        Map<String, String> groupNameMap = employeeGroupRepository.findByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .collect(Collectors.toMap(EmployeeGroup::getId, EmployeeGroup::getName, (a, b) -> a));
        
        // 获取所有账户，用于映射账户名
        Map<String, String> accountUsernameMap = accountUserRepository.findAll()
                .stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                .collect(Collectors.toMap(AccountUser::getId, AccountUser::getUsername, (a, b) -> a));
        
        // 转换为 VO
        List<EmployeeVO> voList = employeePage.getContent().stream().map(employee -> {
            EmployeeVO vo = new EmployeeVO();
            vo.setId(employee.getId());
            vo.setQwId(employee.getQwId());
            vo.setName(employee.getName());
            vo.setStatus(employee.getStatus());
            vo.setIsDeleted(employee.getIsDeleted());
            vo.setGroupId(employee.getGroupId());
            vo.setGroupName(employee.getGroupId() != null ? groupNameMap.get(employee.getGroupId()) : null);
            vo.setAccountUserId(employee.getAccountUserId());
            vo.setAccountUsername(employee.getAccountUserId() != null ? accountUsernameMap.get(employee.getAccountUserId()) : null);
            return vo;
        }).collect(Collectors.toList());
        
        return new PageImpl<>(voList, employeePage.getPageable(), employeePage.getTotalElements());
    }

    /**
     * 员工详情
     */
    @GetMapping("/{id}")
    public Employee getEmployee(@PathVariable String id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));
        if (Boolean.TRUE.equals(employee.getIsDeleted())) {
            throw new RuntimeException("员工不存在");
        }
        return employee;
    }

    /**
     * 新增员工
     */
    @PostMapping("")
    public Employee createEmployee(@RequestBody Employee employee) {
        if (employee.getId() == null || employee.getId().isBlank()) {
            employee.setId(UlidCreator.getUlid().toLowerCase());
        }
        if (employee.getIsDeleted() == null) {
            employee.setIsDeleted(false);
        }
        return employeeRepository.save(employee);
    }

    /**
     * 更新员工
     */
    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable String id, @RequestBody Employee update) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));
        if (Boolean.TRUE.equals(employee.getIsDeleted())) {
            throw new RuntimeException("员工不存在");
        }

        employee.setQwId(update.getQwId());
        employee.setName(update.getName());
        employee.setStatus(update.getStatus());
        if (update.getIsDeleted() != null) {
            employee.setIsDeleted(update.getIsDeleted());
        }
        return employeeRepository.save(employee);
    }

    /**
     * 删除员工（软删）
     */
    @DeleteMapping("/{id}")
    public Employee deleteEmployee(@PathVariable String id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));
        employee.setIsDeleted(true);
        return employeeRepository.save(employee);
    }

    /**
     * 为员工分配登录账户
     */
    @PutMapping("/{id}/account/{accountUserId}")
    public Employee assignAccount(@PathVariable String id, @PathVariable String accountUserId) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));
        if (Boolean.TRUE.equals(employee.getIsDeleted())) {
            throw new RuntimeException("员工不存在");
        }
        
        // 验证账户存在
        AccountUser account = accountUserRepository.findById(accountUserId)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        if (Boolean.TRUE.equals(account.getIsDeleted())) {
            throw new RuntimeException("账户不存在");
        }
        
        employee.setAccountUserId(accountUserId);
        return employeeRepository.save(employee);
    }

    /**
     * 解除员工的登录账户关联
     */
    @DeleteMapping("/{id}/account")
    public Employee removeAccount(@PathVariable String id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));
        if (Boolean.TRUE.equals(employee.getIsDeleted())) {
            throw new RuntimeException("员工不存在");
        }
        
        employee.setAccountUserId(null);
        return employeeRepository.save(employee);
    }

    /**
     * 获取组长管理的员工列表
     * 根据登录用户名，查找该用户关联的员工，再查找该员工作为组长的所有分组，返回这些分组内的所有员工
     */
    @GetMapping("/managed-by-leader")
    public List<EmployeeVO> getManagedByLeader(@RequestParam String username) {
        // 1. 根据用户名找到账户
        AccountUser account = accountUserRepository.findByUsernameAndIsDeletedFalse(username)
                .orElse(null);
        if (account == null) {
            return List.of();
        }

        // 2. 找到关联该账户的员工
        Employee leaderEmployee = employeeRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()) 
                        && account.getId().equals(e.getAccountUserId()))
                .findFirst()
                .orElse(null);
        if (leaderEmployee == null) {
            return List.of();
        }

        // 3. 找到该员工作为组长的所有分组
        List<EmployeeGroup> leadingGroups = employeeGroupRepository.findByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .filter(g -> leaderEmployee.getId().equals(g.getLeaderId()))
                .collect(Collectors.toList());
        if (leadingGroups.isEmpty()) {
            return List.of();
        }

        // 4. 获取这些分组的所有成员
        List<String> groupIds = leadingGroups.stream()
                .map(EmployeeGroup::getId)
                .collect(Collectors.toList());

        // 获取分组名称映射
        Map<String, String> groupNameMap = leadingGroups.stream()
                .collect(Collectors.toMap(EmployeeGroup::getId, EmployeeGroup::getName, (a, b) -> a));

        // 获取账户名映射
        Map<String, String> accountUsernameMap = accountUserRepository.findAll()
                .stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                .collect(Collectors.toMap(AccountUser::getId, AccountUser::getUsername, (a, b) -> a));

        // 返回分组内的所有员工
        return employeeRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()) 
                        && e.getGroupId() != null 
                        && groupIds.contains(e.getGroupId()))
                .map(employee -> {
                    EmployeeVO vo = new EmployeeVO();
                    vo.setId(employee.getId());
                    vo.setQwId(employee.getQwId());
                    vo.setName(employee.getName());
                    vo.setStatus(employee.getStatus());
                    vo.setIsDeleted(employee.getIsDeleted());
                    vo.setGroupId(employee.getGroupId());
                    vo.setGroupName(groupNameMap.get(employee.getGroupId()));
                    vo.setAccountUserId(employee.getAccountUserId());
                    vo.setAccountUsername(employee.getAccountUserId() != null ? accountUsernameMap.get(employee.getAccountUserId()) : null);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取可分配的账户列表（未被其他员工关联的账户）
     */
    @GetMapping("/available-accounts")
    public List<AccountBriefVO> getAvailableAccounts() {
        // 获取已被关联的账户ID
        List<String> assignedAccountIds = employeeRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()) && e.getAccountUserId() != null)
                .map(Employee::getAccountUserId)
                .collect(Collectors.toList());
        
        // 返回未被关联的账户
        return accountUserRepository.findAll().stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()) 
                        && !assignedAccountIds.contains(a.getId()))
                .map(a -> {
                    AccountBriefVO vo = new AccountBriefVO();
                    vo.setId(a.getId());
                    vo.setUsername(a.getUsername());
                    vo.setAccountType(a.getAccountType());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    // 员工 VO，包含分组信息和账户信息
    @lombok.Data
    public static class EmployeeVO {
        private String id;
        private String qwId;
        private String name;
        private Integer status;
        private Boolean isDeleted;
        private String groupId;
        private String groupName;
        private String accountUserId;
        private String accountUsername;
    }

    // 账户简要信息
    @lombok.Data
    public static class AccountBriefVO {
        private String id;
        private String username;
        private Integer accountType;
    }
}
