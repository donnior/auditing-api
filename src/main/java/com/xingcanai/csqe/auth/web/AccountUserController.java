package com.xingcanai.csqe.auth.web;

import com.xingcanai.csqe.auth.entity.AccountUser;
import com.xingcanai.csqe.auth.entity.AccountUserRepository;
import com.xingcanai.csqe.common.XCPageRequest;
import com.github.f4b6a3.ulid.UlidCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录账号管理接口
 */
@RestController
@RequestMapping("/account-users")
public class AccountUserController {

    @Autowired
    private AccountUserRepository accountUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 账号列表（默认按创建时间倒序）
     */
    @GetMapping("")
    public Page<AccountUserVO> listAccounts(XCPageRequest pageRequest) {
        var req = pageRequest.toPageRequest().withSort(Sort.by("createTime").descending());
        Page<AccountUser> accountPage = accountUserRepository.findAll(req);

        // 转换为 VO（不返回密码）
        List<AccountUserVO> voList = accountPage.getContent().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageImpl<>(voList, accountPage.getPageable(), accountPage.getTotalElements());
    }

    /**
     * 账号详情
     */
    @GetMapping("/{id}")
    public AccountUserVO getAccount(@PathVariable String id) {
        AccountUser account = accountUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("账号不存在"));
        if (Boolean.TRUE.equals(account.getIsDeleted())) {
            throw new RuntimeException("账号不存在");
        }
        return toVO(account);
    }

    /**
     * 新增账号
     */
    @PostMapping("")
    public AccountUserVO createAccount(@RequestBody CreateAccountRequest request) {
        // 验证用户名不为空
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new RuntimeException("用户名不能为空");
        }
        // 验证密码不为空
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("密码不能为空");
        }
        // 验证用户名唯一
        if (accountUserRepository.findByUsernameAndIsDeletedFalse(request.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        AccountUser account = new AccountUser();
        account.setId(UlidCreator.getUlid().toLowerCase());
        account.setUsername(request.getUsername());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setAccountType(request.getAccountType() != null ? request.getAccountType() : AccountUser.ACCOUNT_TYPE_EMPLOYEE);
        account.setStatus(AccountUser.STATUS_ACTIVE);
        account.setIsDeleted(false);
        account.setCreateTime(ZonedDateTime.now());
        account.setUpdateTime(ZonedDateTime.now());

        return toVO(accountUserRepository.save(account));
    }

    /**
     * 更新账号
     */
    @PutMapping("/{id}")
    public AccountUserVO updateAccount(@PathVariable String id, @RequestBody UpdateAccountRequest request) {
        AccountUser account = accountUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("账号不存在"));
        if (Boolean.TRUE.equals(account.getIsDeleted())) {
            throw new RuntimeException("账号不存在");
        }

        // 如果要修改用户名，验证唯一性
        if (request.getUsername() != null && !request.getUsername().isBlank() 
                && !request.getUsername().equals(account.getUsername())) {
            if (accountUserRepository.findByUsernameAndIsDeletedFalse(request.getUsername()).isPresent()) {
                throw new RuntimeException("用户名已存在");
            }
            account.setUsername(request.getUsername());
        }

        // 如果提供了新密码，则更新密码
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        // 更新账户类型
        if (request.getAccountType() != null) {
            account.setAccountType(request.getAccountType());
        }

        // 更新状态
        if (request.getStatus() != null) {
            account.setStatus(request.getStatus());
        }

        account.setUpdateTime(ZonedDateTime.now());
        return toVO(accountUserRepository.save(account));
    }

    /**
     * 删除账号（软删）
     */
    @DeleteMapping("/{id}")
    public AccountUserVO deleteAccount(@PathVariable String id) {
        AccountUser account = accountUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("账号不存在"));
        
        // 不允许删除 admin 账号
        if ("admin".equals(account.getUsername())) {
            throw new RuntimeException("不能删除管理员账号");
        }

        account.setIsDeleted(true);
        account.setUpdateTime(ZonedDateTime.now());
        return toVO(accountUserRepository.save(account));
    }

    /**
     * 重置密码
     */
    @PutMapping("/{id}/reset-password")
    public AccountUserVO resetPassword(@PathVariable String id, @RequestBody ResetPasswordRequest request) {
        AccountUser account = accountUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("账号不存在"));
        if (Boolean.TRUE.equals(account.getIsDeleted())) {
            throw new RuntimeException("账号不存在");
        }

        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new RuntimeException("新密码不能为空");
        }

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        account.setUpdateTime(ZonedDateTime.now());
        return toVO(accountUserRepository.save(account));
    }

    private AccountUserVO toVO(AccountUser account) {
        AccountUserVO vo = new AccountUserVO();
        vo.setId(account.getId());
        vo.setUsername(account.getUsername());
        vo.setAccountType(account.getAccountType());
        vo.setStatus(account.getStatus());
        vo.setCreateTime(account.getCreateTime());
        vo.setUpdateTime(account.getUpdateTime());
        return vo;
    }

    // VO 和 Request 类定义
    @lombok.Data
    public static class AccountUserVO {
        private String id;
        private String username;
        private Integer accountType;
        private Integer status;
        private ZonedDateTime createTime;
        private ZonedDateTime updateTime;
    }

    @lombok.Data
    public static class CreateAccountRequest {
        private String username;
        private String password;
        private Integer accountType;
    }

    @lombok.Data
    public static class UpdateAccountRequest {
        private String username;
        private String password;
        private Integer accountType;
        private Integer status;
    }

    @lombok.Data
    public static class ResetPasswordRequest {
        private String newPassword;
    }
}
