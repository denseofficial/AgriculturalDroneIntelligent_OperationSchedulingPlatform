package com.agrodrone.controller;

import com.agrodrone.common.ApiResponse;
import com.agrodrone.service.MapService;
import com.agrodrone.vo.MapOverviewVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map")
public class MapController {
    private final MapService mapService;

    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/overview")
    public ApiResponse<MapOverviewVO> overview() {
        return ApiResponse.ok(mapService.overview());
    }
}
