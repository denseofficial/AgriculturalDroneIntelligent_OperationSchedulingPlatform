package com.agrodrone.service;

import com.agrodrone.entity.SysUser;
import com.agrodrone.mapper.SysUserMapper;
import com.agrodrone.vo.LoginRequest;
import com.agrodrone.vo.LoginVO;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class AuthService {
    private final SysUserMapper sysUserMapper;
    private final TokenService tokenService;
    private final OperationLogService operationLogService;

    public AuthService(SysUserMapper sysUserMapper, TokenService tokenService, OperationLogService operationLogService) {
        this.sysUserMapper = sysUserMapper;
        this.tokenService = tokenService;
        this.operationLogService = operationLogService;
    }

    public LoginVO login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException("用户不存在或已禁用");
        }
        if (!sha256(request.getPassword()).equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        LoginVO vo = new LoginVO();
        vo.setToken(tokenService.createToken(user));
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        operationLogService.record("系统登录", "登录", "sys_user", user.getId(), user.getRealName() + "登录系统");
        return vo;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : bytes) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("密码加密失败", exception);
        }
    }
}
