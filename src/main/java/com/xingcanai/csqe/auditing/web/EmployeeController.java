package com.xingcanai.csqe.auditing.web;

import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.EmployeeRepository;
import com.xingcanai.csqe.common.XCPageRequest;
import com.github.f4b6a3.ulid.UlidCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 员工管理接口
 */
@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * 员工列表（默认按id倒序）
     */
    @GetMapping("")
    public Page<Employee> listEmployees(XCPageRequest pageRequest) {
        var req = pageRequest.toPageRequest().withSort(Sort.by("id").descending());
        return employeeRepository.findAll(req);
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
}
