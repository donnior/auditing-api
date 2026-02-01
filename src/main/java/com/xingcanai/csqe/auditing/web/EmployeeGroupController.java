package com.xingcanai.csqe.auditing.web;

import com.xingcanai.csqe.auditing.entity.Employee;
import com.xingcanai.csqe.auditing.entity.EmployeeGroup;
import com.xingcanai.csqe.auditing.entity.EmployeeGroupRepository;
import com.xingcanai.csqe.auditing.entity.EmployeeRepository;
import com.github.f4b6a3.ulid.UlidCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工分组管理接口
 */
@RestController
@RequestMapping("/employee-groups")
public class EmployeeGroupController {

    @Autowired
    private EmployeeGroupRepository employeeGroupRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * 分组列表（包含成员数量和组长信息）
     */
    @GetMapping("")
    public List<EmployeeGroupVO> listGroups() {
        List<EmployeeGroup> groups = employeeGroupRepository.findByIsDeletedFalseOrderByCreatedAtDesc();
        
        // 获取所有员工，用于统计每个分组的成员数量和组长信息
        List<Employee> allEmployees = employeeRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
                .collect(Collectors.toList());
        
        // 按分组统计成员数量
        Map<String, Long> memberCountMap = allEmployees.stream()
                .filter(e -> e.getGroupId() != null)
                .collect(Collectors.groupingBy(Employee::getGroupId, Collectors.counting()));
        
        // 构建员工ID到姓名的映射
        Map<String, String> employeeNameMap = allEmployees.stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName, (a, b) -> a));
        
        return groups.stream().map(group -> {
            EmployeeGroupVO vo = new EmployeeGroupVO();
            vo.setId(group.getId());
            vo.setName(group.getName());
            vo.setDescription(group.getDescription());
            vo.setLeaderId(group.getLeaderId());
            vo.setLeaderName(group.getLeaderId() != null ? employeeNameMap.get(group.getLeaderId()) : null);
            vo.setMemberCount(memberCountMap.getOrDefault(group.getId(), 0L).intValue());
            vo.setCreatedAt(group.getCreatedAt());
            vo.setUpdatedAt(group.getUpdatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 分组详情
     */
    @GetMapping("/{id}")
    public EmployeeGroupVO getGroup(@PathVariable String id) {
        EmployeeGroup group = employeeGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分组不存在"));
        if (Boolean.TRUE.equals(group.getIsDeleted())) {
            throw new RuntimeException("分组不存在");
        }
        
        // 获取分组成员
        List<Employee> members = employeeRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()) && id.equals(e.getGroupId()))
                .collect(Collectors.toList());
        
        EmployeeGroupVO vo = new EmployeeGroupVO();
        vo.setId(group.getId());
        vo.setName(group.getName());
        vo.setDescription(group.getDescription());
        vo.setLeaderId(group.getLeaderId());
        vo.setMemberCount(members.size());
        vo.setCreatedAt(group.getCreatedAt());
        vo.setUpdatedAt(group.getUpdatedAt());
        
        // 设置组长姓名
        if (group.getLeaderId() != null) {
            employeeRepository.findById(group.getLeaderId())
                    .ifPresent(leader -> vo.setLeaderName(leader.getName()));
        }
        
        // 设置成员列表
        vo.setMembers(members.stream().map(e -> {
            EmployeeBriefVO brief = new EmployeeBriefVO();
            brief.setId(e.getId());
            brief.setName(e.getName());
            brief.setQwId(e.getQwId());
            brief.setIsLeader(e.getId().equals(group.getLeaderId()));
            return brief;
        }).collect(Collectors.toList()));
        
        return vo;
    }

    /**
     * 新增分组
     */
    @PostMapping("")
    public EmployeeGroup createGroup(@RequestBody EmployeeGroup group) {
        if (group.getId() == null || group.getId().isBlank()) {
            group.setId(UlidCreator.getUlid().toLowerCase());
        }
        if (group.getIsDeleted() == null) {
            group.setIsDeleted(false);
        }
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        return employeeGroupRepository.save(group);
    }

    /**
     * 更新分组
     */
    @PutMapping("/{id}")
    public EmployeeGroup updateGroup(@PathVariable String id, @RequestBody EmployeeGroup update) {
        EmployeeGroup group = employeeGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分组不存在"));
        if (Boolean.TRUE.equals(group.getIsDeleted())) {
            throw new RuntimeException("分组不存在");
        }

        group.setName(update.getName());
        group.setDescription(update.getDescription());
        group.setLeaderId(update.getLeaderId());
        group.setUpdatedAt(LocalDateTime.now());
        
        return employeeGroupRepository.save(group);
    }

    /**
     * 删除分组（软删）
     */
    @DeleteMapping("/{id}")
    public EmployeeGroup deleteGroup(@PathVariable String id) {
        EmployeeGroup group = employeeGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分组不存在"));
        
        // 将分组内的员工的groupId设为null
        List<Employee> members = employeeRepository.findAll().stream()
                .filter(e -> id.equals(e.getGroupId()))
                .collect(Collectors.toList());
        for (Employee member : members) {
            member.setGroupId(null);
            employeeRepository.save(member);
        }
        
        group.setIsDeleted(true);
        group.setUpdatedAt(LocalDateTime.now());
        return employeeGroupRepository.save(group);
    }

    /**
     * 分配员工到分组
     */
    @PostMapping("/{id}/members")
    public EmployeeGroup addMembers(@PathVariable String id, @RequestBody MemberIdsRequest request) {
        EmployeeGroup group = employeeGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分组不存在"));
        if (Boolean.TRUE.equals(group.getIsDeleted())) {
            throw new RuntimeException("分组不存在");
        }
        
        System.out.println("=== 添加成员请求 ===");
        System.out.println("分组ID: " + id);
        System.out.println("请求对象: " + request);
        System.out.println("employeeIds: " + request.getEmployeeIds());
        
        if (request.getEmployeeIds() != null) {
            for (String employeeId : request.getEmployeeIds()) {
                System.out.println("处理员工ID: " + employeeId);
                var empOpt = employeeRepository.findById(employeeId);
                System.out.println("找到员工: " + empOpt.isPresent());
                empOpt.ifPresent(employee -> {
                    System.out.println("员工: " + employee.getName() + ", isDeleted: " + employee.getIsDeleted());
                    if (!Boolean.TRUE.equals(employee.getIsDeleted())) {
                        employee.setGroupId(id);
                        employeeRepository.save(employee);
                        System.out.println("已保存员工分组");
                    }
                });
            }
        }
        
        group.setUpdatedAt(LocalDateTime.now());
        return employeeGroupRepository.save(group);
    }

    /**
     * 从分组移除员工
     */
    @DeleteMapping("/{id}/members/{employeeId}")
    public void removeMember(@PathVariable String id, @PathVariable String employeeId) {
        employeeRepository.findById(employeeId).ifPresent(employee -> {
            if (id.equals(employee.getGroupId())) {
                employee.setGroupId(null);
                employeeRepository.save(employee);
            }
        });
    }

    /**
     * 设置组长
     */
    @PutMapping("/{id}/leader/{employeeId}")
    public EmployeeGroup setLeader(@PathVariable String id, @PathVariable String employeeId) {
        EmployeeGroup group = employeeGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分组不存在"));
        if (Boolean.TRUE.equals(group.getIsDeleted())) {
            throw new RuntimeException("分组不存在");
        }
        
        // 验证员工存在且属于该分组
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("员工不存在"));
        if (!id.equals(employee.getGroupId())) {
            // 如果员工不在该分组，自动加入
            employee.setGroupId(id);
            employeeRepository.save(employee);
        }
        
        group.setLeaderId(employeeId);
        group.setUpdatedAt(LocalDateTime.now());
        return employeeGroupRepository.save(group);
    }

    /**
     * 获取未分组的员工列表
     */
    @GetMapping("/unassigned-employees")
    public List<EmployeeBriefVO> getUnassignedEmployees() {
        return employeeRepository.findAll().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()) && e.getGroupId() == null)
                .map(e -> {
                    EmployeeBriefVO brief = new EmployeeBriefVO();
                    brief.setId(e.getId());
                    brief.setName(e.getName());
                    brief.setQwId(e.getQwId());
                    brief.setIsLeader(false);
                    return brief;
                })
                .collect(Collectors.toList());
    }

    // VO 类定义
    @lombok.Data
    public static class EmployeeGroupVO {
        private String id;
        private String name;
        private String description;
        private String leaderId;
        private String leaderName;
        private Integer memberCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<EmployeeBriefVO> members;
    }

    @lombok.Data
    public static class EmployeeBriefVO {
        private String id;
        private String name;
        private String qwId;
        private Boolean isLeader;
    }

    @lombok.Data
    public static class MemberIdsRequest {
        @com.fasterxml.jackson.annotation.JsonProperty("employee_ids")
        private List<String> employeeIds;
    }
}
