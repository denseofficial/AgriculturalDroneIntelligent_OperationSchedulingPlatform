package com.agrodrone.controller;

import com.agrodrone.common.ApiResponse;
import com.agrodrone.entity.OperationTask;
import com.agrodrone.mapper.OperationTaskMapper;
import com.agrodrone.mapper.SchedulePlanMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class OperationTaskController {
    private final OperationTaskMapper taskMapper;
    private final SchedulePlanMapper schedulePlanMapper;

    public OperationTaskController(OperationTaskMapper taskMapper, SchedulePlanMapper schedulePlanMapper) {
        this.taskMapper = taskMapper;
        this.schedulePlanMapper = schedulePlanMapper;
    }

    @GetMapping
    public ApiResponse<List<OperationTask>> list() {
        return ApiResponse.ok(taskMapper.selectList(new LambdaQueryWrapper<OperationTask>()
                .orderByDesc(OperationTask::getPriority)
                .orderByAsc(OperationTask::getLatestEndTime)));
    }

    @PostMapping
    public ApiResponse<OperationTask> save(@RequestBody OperationTask task) {
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
