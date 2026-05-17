package com.agrodrone.controller;

import com.agrodrone.common.ApiResponse;
import com.agrodrone.entity.Drone;
import com.agrodrone.mapper.DroneMapper;
import com.agrodrone.vo.DroneLocationRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/drones")
public class DroneController {
    private final DroneMapper droneMapper;

    public DroneController(DroneMapper droneMapper) {
        this.droneMapper = droneMapper;
    }

    @GetMapping
    public ApiResponse<List<Drone>> list() {
        return ApiResponse.ok(droneMapper.selectList(new LambdaQueryWrapper<Drone>().orderByAsc(Drone::getCode)));
    }

    @PostMapping
    public ApiResponse<Drone> save(@RequestBody Drone drone) {
        drone.setUpdatedAt(LocalDateTime.now());
        if (drone.getId() == null) {
            droneMapper.insert(drone);
        } else {
            droneMapper.updateById(drone);
        }
        return ApiResponse.ok(drone);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.ok(droneMapper.deleteById(id) > 0);
    }

    @PostMapping("/{id}/location")
    public ApiResponse<Boolean> updateLocation(@PathVariable Long id, @RequestBody DroneLocationRequest request) {
        Drone drone = droneMapper.selectById(id);
        if (drone == null) {
            throw new IllegalArgumentException("无人机不存在");
        }
        drone.setLongitude(request.getLongitude());
        drone.setLatitude(request.getLatitude());
        if (request.getBatteryPercent() != null) {
            drone.setBatteryPercent(request.getBatteryPercent());
        }
        drone.setUpdatedAt(LocalDateTime.now());
        return ApiResponse.ok(droneMapper.updateById(drone) > 0);
    }
}
