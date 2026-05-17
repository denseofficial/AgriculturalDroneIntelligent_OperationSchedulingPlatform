package com.agrodrone.controller;

import com.agrodrone.common.ApiResponse;
import com.agrodrone.service.DashboardService;
import com.agrodrone.vo.DashboardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public ApiResponse<DashboardVO> overview() {
        return ApiResponse.ok(dashboardService.overview());
    }
}
