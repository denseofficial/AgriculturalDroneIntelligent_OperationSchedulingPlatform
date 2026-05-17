package com.agrodrone.controller;

import com.agrodrone.common.ApiResponse;
import com.agrodrone.entity.OperationLog;
import com.agrodrone.service.OperationLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class OperationLogController {
    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    public ApiResponse<List<OperationLog>> list() {
        return ApiResponse.ok(operationLogService.latest());
    }
}
