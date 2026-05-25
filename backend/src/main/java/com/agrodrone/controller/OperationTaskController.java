package com.agrodrone.controller;

import com.agrodrone.common.ApiResponse;
import com.agrodrone.entity.OperationTask;
import com.agrodrone.mapper.OperationTaskMapper;
import com.agrodrone.mapper.SchedulePlanMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class OperationTaskController {
    private final OperationTaskMapper taskMapper;
    private final SchedulePlanMapper schedulePlanMapper;
    private final com.agrodrone.service.TokenService tokenService;

    public OperationTaskController(OperationTaskMapper taskMapper, SchedulePlanMapper schedulePlanMapper, com.agrodrone.service.TokenService tokenService) {
        this.taskMapper = taskMapper;
        this.schedulePlanMapper = schedulePlanMapper;
        this.tokenService = tokenService;
    }

    @GetMapping
    public ApiResponse<List<OperationTask>> list(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
        com.agrodrone.entity.SysUser current = tokenService.parseToken(token);
        LambdaQueryWrapper<OperationTask> wrapper = new LambdaQueryWrapper<OperationTask>()
                .orderByDesc(OperationTask::getPriority)
                .orderByAsc(OperationTask::getLatestEndTime);
        if (current != null && "ADMIN".equals(current.getRole())) {
            // admin: see all
        } else if (current != null) {
            // operator: only tasks created by themselves
            wrapper.eq(OperationTask::getCreatedBy, current.getUsername());
        } else {
            // unauthenticated (should be rejected by interceptor), but return empty
            return ApiResponse.ok(List.of());
        }
        return ApiResponse.ok(taskMapper.selectList(wrapper));
    }

    @PostMapping
    public ApiResponse<OperationTask> save(@RequestBody OperationTask task, HttpServletRequest request) {
        if (task.getTaskNo() == null || task.getTaskNo().isBlank()) {
            task.setTaskNo("TASK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        }
        if (task.getStatus() == null || task.getStatus().isBlank()) {
            task.setStatus("PENDING");
        }
        if (task.getEarliestStartTime() == null) {
            task.setEarliestStartTime(LocalDateTime.now().plusMinutes(30));
        }
        if (task.getLatestEndTime() == null) {
            task.setLatestEndTime(task.getEarliestStartTime().plusHours(12));
        }
        if (task.getId() == null) {
            String authorization = request.getHeader("Authorization");
            String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
            com.agrodrone.entity.SysUser current = tokenService.parseToken(token);
            if (current != null) {
                task.setCreatedBy(current.getUsername());
            }
            taskMapper.insert(task);
        } else {
            taskMapper.updateById(task);
        }
        return ApiResponse.ok(task);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        schedulePlanMapper.delete(new LambdaQueryWrapper<com.agrodrone.entity.SchedulePlan>()
                .eq(com.agrodrone.entity.SchedulePlan::getTaskId, id));
        return ApiResponse.ok(taskMapper.deleteById(id) > 0);
    }
}
