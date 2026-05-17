package com.agrodrone.service;

import com.agrodrone.entity.SchedulePlan;
import com.agrodrone.mapper.DroneMapper;
import com.agrodrone.mapper.OperationTaskMapper;
import com.agrodrone.mapper.SchedulePlanMapper;
import com.agrodrone.vo.DashboardVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class DashboardService {
    private final DroneMapper droneMapper;
    private final OperationTaskMapper taskMapper;
    private final SchedulePlanMapper planMapper;

    public DashboardService(DroneMapper droneMapper, OperationTaskMapper taskMapper, SchedulePlanMapper planMapper) {
        this.droneMapper = droneMapper;
        this.taskMapper = taskMapper;
        this.planMapper = planMapper;
    }

    public DashboardVO overview() {
        DashboardVO vo = new DashboardVO();
        vo.setDroneCount(droneMapper.selectCount(null));
        vo.setIdleDroneCount(droneMapper.selectCount(new LambdaQueryWrapper<com.agrodrone.entity.Drone>()
                .eq(com.agrodrone.entity.Drone::getStatus, "IDLE")));
        vo.setPendingTaskCount(taskMapper.selectCount(new LambdaQueryWrapper<com.agrodrone.entity.OperationTask>()
                .eq(com.agrodrone.entity.OperationTask::getStatus, "PENDING")));
        vo.setScheduledTaskCount(taskMapper.selectCount(new LambdaQueryWrapper<com.agrodrone.entity.OperationTask>()
                .eq(com.agrodrone.entity.OperationTask::getStatus, "SCHEDULED")));

        LocalDate today = LocalDate.now();
        BigDecimal todayArea = planMapper.selectList(new LambdaQueryWrapper<SchedulePlan>()
                        .ge(SchedulePlan::getPlannedStartTime, today.atStartOfDay())
                        .lt(SchedulePlan::getPlannedStartTime, today.plusDays(1).atStartOfDay()))
                .stream()
                .map(SchedulePlan::getEstimatedAreaMu)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTodayAreaMu(todayArea);
        return vo;
    }
}
