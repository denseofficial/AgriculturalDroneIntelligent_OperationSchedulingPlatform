package com.agrodrone.controller;

import com.agrodrone.common.ApiResponse;
import com.agrodrone.entity.SysUser;
import com.agrodrone.service.SysUserService;
import com.agrodrone.service.TokenService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class SysUserController {
    private final SysUserService sysUserService;
    private final TokenService tokenService;

    public SysUserController(SysUserService sysUserService, TokenService tokenService) {
        this.sysUserService = sysUserService;
        this.tokenService = tokenService;
    }

    @GetMapping
    public ApiResponse<List<SysUser>> list(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
        com.agrodrone.entity.SysUser current = tokenService.parseToken(token);
        if (current != null && "ADMIN".equals(current.getRole())) {
            return ApiResponse.ok(sysUserService.listAll());
        }
        if (current != null) {
            SysUser self = sysUserService.findByUsername(current.getUsername());
            return ApiResponse.ok(List.of(self));
        }
        return ApiResponse.ok(List.of());
    }

    @GetMapping("/{id}")
    public ApiResponse<SysUser> get(@PathVariable Long id, HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
        com.agrodrone.entity.SysUser current = tokenService.parseToken(token);
        SysUser target = sysUserService.getById(id);
        if (current != null && ("ADMIN".equals(current.getRole()) || current.getId().equals(id))) {
            return ApiResponse.ok(target);
        }
        return ApiResponse.fail("没有权限");
    }

    public static class CreateUserRequest {
        public String username;
        public String password;
        public String realName;
        public String role;
        public String status;
    }

    @PostMapping
    public ApiResponse<SysUser> create(@RequestBody CreateUserRequest req, HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
        com.agrodrone.entity.SysUser current = tokenService.parseToken(token);
        if (current == null || !"ADMIN".equals(current.getRole())) {
            return ApiResponse.fail("没有权限");
        }
        SysUser user = new SysUser();
        user.setUsername(req.username);
        user.setRealName(req.realName);
        user.setRole(req.role == null ? "OPERATOR" : req.role);
        user.setStatus(req.status == null ? "ACTIVE" : req.status);
        SysUser created = sysUserService.create(user, req.password);
        return ApiResponse.ok(created);
    }

    @PutMapping("/{id}")
    public ApiResponse<SysUser> update(@PathVariable Long id, @RequestBody SysUser user, HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
        com.agrodrone.entity.SysUser current = tokenService.parseToken(token);
        if (current == null || !"ADMIN".equals(current.getRole())) {
            return ApiResponse.fail("没有权限");
        }
        user.setId(id);
        SysUser updated = sysUserService.update(user);
        return ApiResponse.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
        com.agrodrone.entity.SysUser current = tokenService.parseToken(token);
        if (current == null || !"ADMIN".equals(current.getRole())) {
            return ApiResponse.fail("没有权限");
        }
        return ApiResponse.ok(sysUserService.delete(id));
    }

    public static class ChangePasswordRequest {
        public String oldPassword;
        public String newPassword;
    }

    @PostMapping("/{id}/password")
    public ApiResponse<Boolean> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest req, HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
        com.agrodrone.entity.SysUser current = tokenService.parseToken(token);
        if (current == null) return ApiResponse.fail("未认证");
        boolean isAdmin = "ADMIN".equals(current.getRole());
        // users can change their own password by providing oldPassword; admins can change without oldPassword
        if (!isAdmin && !current.getId().equals(id)) {
            return ApiResponse.fail("没有权限");
        }
        boolean ok = sysUserService.changePassword(id, req.oldPassword, req.newPassword, isAdmin);
        if (!ok) return ApiResponse.fail("密码修改失败（旧密码错误或用户不存在）");
        return ApiResponse.ok(true);
    }
}

