package com.agrodrone.controller;

import com.agrodrone.common.ApiResponse;
import com.agrodrone.entity.SchedulePlan;
import com.agrodrone.service.SchedulingService;
import com.agrodrone.vo.CompleteOperationRequest;
import com.agrodrone.vo.OperationRecordVO;
import com.agrodrone.vo.ScheduleDetailVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {
    private final SchedulingService schedulingService;

    public ScheduleController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @GetMapping
    public ApiResponse<List<ScheduleDetailVO>> list() {
        return ApiResponse.ok(schedulingService.detailList());
    }

    @PostMapping("/generate")
    public ApiResponse<List<SchedulePlan>> generate() {
        return ApiResponse.ok(schedulingService.generatePlans());
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<Boolean> confirm(@PathVariable Long id) {
        return ApiResponse.ok(schedulingService.confirmPlan(id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Boolean> cancel(@PathVariable Long id) {
        return ApiResponse.ok(schedulingService.cancelPlan(id));
    }

    @PostMapping("/{id}/start")
    public ApiResponse<Boolean> start(@PathVariable Long id) {
        return ApiResponse.ok(schedulingService.startOperation(id));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<Boolean> complete(@PathVariable Long id, @RequestBody CompleteOperationRequest request) {
        return ApiResponse.ok(schedulingService.completeOperation(id, request));
    }

    @GetMapping("/records")
    public ApiResponse<List<OperationRecordVO>> records() {
        return ApiResponse.ok(schedulingService.recordList());
    }
}
