package com.xingcanai.csqe.auditing.web;

import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.EmployeeGroup;
import com.xingcanai.csqe.auditing.entity.EmployeeGroupRepository;
import com.xingcanai.csqe.auditing.entity.EmployeeRepository;
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

    // 员工 VO，包含分组信息
    @lombok.Data
    public static class EmployeeVO {
        private String id;
        private String qwId;
        private String name;
        private Integer status;
        private Boolean isDeleted;
        private String groupId;
        private String groupName;
    }
}
