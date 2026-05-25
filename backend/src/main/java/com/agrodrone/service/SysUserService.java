package com.agrodrone.service;

import com.agrodrone.entity.SysUser;
import com.agrodrone.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysUserService {
    private final SysUserMapper sysUserMapper;

    public SysUserService(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    public List<SysUser> listAll() {
        return sysUserMapper.selectList(null);
    }

    public SysUser getById(Long id) {
        return sysUserMapper.selectById(id);
    }

    public SysUser findByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    public SysUser create(SysUser user, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        user.setPasswordHash(sha256(rawPassword));
        user.setCreatedAt(LocalDateTime.now());
        sysUserMapper.insert(user);
        return user;
    }

    public SysUser update(SysUser user) {
        sysUserMapper.updateById(user);
        return user;
    }

    public boolean delete(Long id) {
        return sysUserMapper.deleteById(id) > 0;
    }

    public boolean changePassword(Long id, String oldPassword, String newPassword, boolean isAdmin) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) return false;
        if (!isAdmin) {
            if (oldPassword == null || !sha256(oldPassword).equals(user.getPasswordHash())) {
                return false;
            }
        }
        user.setPasswordHash(sha256(newPassword));
        sysUserMapper.updateById(user);
        return true;
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

