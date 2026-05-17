package com.agrodrone.service;

import com.agrodrone.entity.Drone;
import com.agrodrone.entity.FieldPlot;
import com.agrodrone.entity.OperationRecord;
import com.agrodrone.entity.OperationTask;
import com.agrodrone.entity.OperationTrack;
import com.agrodrone.entity.SchedulePlan;
import com.agrodrone.mapper.DroneMapper;
import com.agrodrone.mapper.FieldPlotMapper;
import com.agrodrone.mapper.OperationRecordMapper;
import com.agrodrone.mapper.OperationTaskMapper;
import com.agrodrone.mapper.OperationTrackMapper;
import com.agrodrone.mapper.SchedulePlanMapper;
import com.agrodrone.vo.CompleteOperationRequest;
import com.agrodrone.vo.OperationRecordVO;
import com.agrodrone.vo.ScheduleDetailVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SchedulingService {
    private final DroneMapper droneMapper;
    private final FieldPlotMapper fieldPlotMapper;
    private final OperationTaskMapper taskMapper;
    private final SchedulePlanMapper planMapper;
    private final OperationRecordMapper recordMapper;
    private final OperationTrackMapper trackMapper;
    private final OperationLogService operationLogService;

    public SchedulingService(DroneMapper droneMapper, FieldPlotMapper fieldPlotMapper,
                             OperationTaskMapper taskMapper, SchedulePlanMapper planMapper,
                             OperationRecordMapper recordMapper, OperationTrackMapper trackMapper,
                             OperationLogService operationLogService) {
        this.droneMapper = droneMapper;
        this.fieldPlotMapper = fieldPlotMapper;
        this.taskMapper = taskMapper;
        this.planMapper = planMapper;
        this.recordMapper = recordMapper;
        this.trackMapper = trackMapper;
        this.operationLogService = operationLogService;
    }

    @Transactional
    public List<SchedulePlan> generatePlans() {
        List<Drone> availableDrones = droneMapper.selectDispatchableDrones(35);
        if (availableDrones.isEmpty()) {
            throw new IllegalStateException("没有电量充足的可用无人机");
        }

        List<OperationTask> tasks = taskMapper.selectPendingTasksForSchedule();
        if (tasks.isEmpty()) {
            return List.of();
        }

        List<Long> fieldIds = tasks.stream()
                .map(OperationTask::getFieldId)
                .distinct()
                .toList();
        Map<Long, FieldPlot> fields = fieldPlotMapper.selectList(new LambdaQueryWrapper<FieldPlot>()
                        .in(FieldPlot::getId, fieldIds))
                .stream()
                .collect(Collectors.toMap(FieldPlot::getId, Function.identity()));

        Map<Long, LocalDateTime> droneAvailableTime = new HashMap<>();
        availableDrones.forEach(drone -> droneAvailableTime.put(drone.getId(), LocalDateTime.now().plusMinutes(30)));

        List<SchedulePlan> plans = new ArrayList<>();
        for (OperationTask task : tasks) {
            FieldPlot field = fields.get(task.getFieldId());
            if (field == null) {
                continue;
            }
            Optional<DroneScore> best = availableDrones.stream()
                    .map(drone -> score(task, field, drone, droneAvailableTime.get(drone.getId())))
                    .max(Comparator.comparing(DroneScore::score));

            if (best.isEmpty()) {
                continue;
            }

            Drone drone = best.get().drone();
            BigDecimal hourlyCapacity = drone.getSprayWidthMeter().multiply(BigDecimal.valueOf(8));
            long minutes = task.getRequiredAreaMu()
                    .divide(hourlyCapacity, 2, RoundingMode.UP)
                    .multiply(BigDecimal.valueOf(60))
                    .max(BigDecimal.valueOf(30))
                    .longValue();

            LocalDateTime availableAt = droneAvailableTime.get(drone.getId());
            LocalDateTime start = task.getEarliestStartTime().isAfter(availableAt) ? task.getEarliestStartTime() : availableAt;
            LocalDateTime end = start.plusMinutes(minutes);

            SchedulePlan plan = new SchedulePlan();
            plan.setTaskId(task.getId());
            plan.setDroneId(drone.getId());
            plan.setPlannedStartTime(start);
            plan.setPlannedEndTime(end);
            plan.setEstimatedAreaMu(task.getRequiredAreaMu());
            plan.setScore(BigDecimal.valueOf(best.get().score()).setScale(2, RoundingMode.HALF_UP));
            plan.setDecisionReason(best.get().reason());
            plan.setStatus("GENERATED");
            plan.setCreatedAt(LocalDateTime.now());
            planMapper.insert(plan);
            operationLogService.record("调度管理", "生成方案", "schedule_plan", plan.getId(),
                    "为任务" + task.getTaskNo() + "分配无人机" + drone.getCode());

            task.setStatus("SCHEDULED");
            taskMapper.updateById(task);
            drone.setStatus("WORKING");
            drone.setUpdatedAt(LocalDateTime.now());
            droneMapper.updateById(drone);

            droneAvailableTime.put(drone.getId(), end.plusMinutes(15));
            plans.add(plan);
        }
        return plans;
    }

    public List<ScheduleDetailVO> detailList() {
        return planMapper.selectScheduleDetails();
    }

    @Transactional
    public boolean confirmPlan(Long id) {
        SchedulePlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new IllegalArgumentException("调度计划不存在");
        }
        plan.setStatus("CONFIRMED");
        operationLogService.record("调度管理", "确认方案", "schedule_plan", plan.getId(), "确认调度方案");
        return planMapper.updateById(plan) > 0;
    }

    @Transactional
    public boolean startOperation(Long planId) {
        SchedulePlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("调度计划不存在");
        }
        if (!"CONFIRMED".equals(plan.getStatus())) {
            throw new IllegalStateException("只有已确认的计划才能开始作业");
        }
        Long count = recordMapper.selectCount(new LambdaQueryWrapper<OperationRecord>()
                .eq(OperationRecord::getSchedulePlanId, planId));
        if (count > 0) {
            throw new IllegalStateException("该计划已开始作业");
        }

        OperationRecord record = new OperationRecord();
        record.setSchedulePlanId(plan.getId());
        record.setTaskId(plan.getTaskId());
        record.setDroneId(plan.getDroneId());
        record.setActualStartTime(LocalDateTime.now());
        record.setResult("RUNNING");
        record.setCreatedAt(LocalDateTime.now());
        recordMapper.insert(record);
        Drone drone = droneMapper.selectById(plan.getDroneId());
        if (drone != null) {
            insertTrack(record.getId(), drone);
        }

        plan.setStatus("RUNNING");
        planMapper.updateById(plan);
        operationLogService.record("作业执行", "开始作业", "schedule_plan", plan.getId(), "开始执行调度计划");

        OperationTask task = taskMapper.selectById(plan.getTaskId());
        if (task != null) {
            task.setStatus("RUNNING");
            taskMapper.updateById(task);
        }
        return true;
    }

    @Transactional
    public boolean completeOperation(Long planId, CompleteOperationRequest request) {
        SchedulePlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("调度计划不存在");
        }
        OperationRecord record = recordMapper.selectOne(new LambdaQueryWrapper<OperationRecord>()
                .eq(OperationRecord::getSchedulePlanId, planId)
                .last("LIMIT 1"));
        if (record == null) {
            throw new IllegalStateException("该计划还未开始作业");
        }
        if (record.getActualEndTime() != null) {
            throw new IllegalStateException("该计划已完成作业");
        }

        LocalDateTime endTime = LocalDateTime.now();
        record.setActualEndTime(endTime);
        record.setActualAreaMu(request.getActualAreaMu() == null ? plan.getEstimatedAreaMu() : request.getActualAreaMu());
        record.setDurationMinutes((int) Duration.between(record.getActualStartTime(), endTime).toMinutes());
        record.setResult(request.getResult() == null || request.getResult().isBlank() ? "FINISHED" : request.getResult());
        record.setExceptionRemark(request.getExceptionRemark());
        recordMapper.updateById(record);

        plan.setStatus("FINISHED");
        planMapper.updateById(plan);
        operationLogService.record("作业执行", "完成作业", "schedule_plan", plan.getId(), "完成作业并记录实际面积");

        OperationTask task = taskMapper.selectById(plan.getTaskId());
        if (task != null) {
            task.setStatus("FINISHED");
            taskMapper.updateById(task);
        }

        Drone drone = droneMapper.selectById(plan.getDroneId());
        if (drone != null) {
            OperationTask taskForTrack = taskMapper.selectById(plan.getTaskId());
            FieldPlot targetField = taskForTrack == null ? null : fieldPlotMapper.selectById(taskForTrack.getFieldId());
            if (targetField != null) {
                insertRouteTracks(record.getId(), drone, targetField);
                drone.setLongitude(targetField.getLongitude());
                drone.setLatitude(targetField.getLatitude());
            } else {
                insertTrack(record.getId(), drone);
            }
            drone.setStatus("IDLE");
            drone.setUpdatedAt(LocalDateTime.now());
            droneMapper.updateById(drone);
        }
        return true;
    }

    public List<OperationRecordVO> recordList() {
        return recordMapper.selectRecordDetails();
    }

    @Transactional
    public boolean cancelPlan(Long id) {
        SchedulePlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new IllegalArgumentException("调度计划不存在");
        }

        OperationTask task = taskMapper.selectById(plan.getTaskId());
        if (task != null) {
            task.setStatus("PENDING");
            taskMapper.updateById(task);
        }

        Drone drone = droneMapper.selectById(plan.getDroneId());
        if (drone != null) {
            drone.setStatus("IDLE");
            drone.setUpdatedAt(LocalDateTime.now());
            droneMapper.updateById(drone);
        }

        plan.setStatus("CANCELED");
        operationLogService.record("调度管理", "取消方案", "schedule_plan", plan.getId(), "取消调度方案，任务恢复待调度");
        return planMapper.updateById(plan) > 0;
    }

    private DroneScore score(OperationTask task, FieldPlot field, Drone drone, LocalDateTime availableAt) {
        if (task == null || drone == null) {
            return new DroneScore(drone, 0.0, "任务或无人机数据不完整，无法计算调度分数。");
        }
        LocalDateTime start = task.getEarliestStartTime().isAfter(availableAt) ? task.getEarliestStartTime() : availableAt;
        long hoursToDeadline = Math.max(1, Duration.between(start, task.getLatestEndTime()).toHours());
        double urgencyScore = Math.min(28.0, 90.0 / hoursToDeadline);
        double priorityScore = task.getPriority() * 20.0;
        String pestRiskLevel = field == null ? "MEDIUM" : field.getPestRiskLevel();
        double riskScore = switch (pestRiskLevel) {
            case "HIGH" -> 22.0;
            case "MEDIUM" -> 12.0;
            default -> 5.0;
        };
        double batteryScore = drone.getBatteryPercent() * 0.28;
        double capacityScore = drone.getPayloadKg().doubleValue() * 1.3 + drone.getSprayWidthMeter().doubleValue() * 1.8;
        double distanceKm = distanceKm(drone, field);
        double distancePenalty = Math.min(25.0, distanceKm * 1.8);
        double latePenalty = start.isAfter(task.getLatestEndTime()) ? 35.0 : 0.0;
        double rawScore = priorityScore + riskScore + urgencyScore + batteryScore + capacityScore - distancePenalty - latePenalty;
        double score = Math.max(0.0, Math.min(100.0, rawScore / 2.4));
        String reason = "优先级较高，作业窗口匹配；无人机电量充足，距离地块约"
                + BigDecimal.valueOf(distanceKm).setScale(2, RoundingMode.HALF_UP)
                + "km，当前排班无时间冲突。";
        return new DroneScore(drone, score, reason);
    }

    private double distanceKm(Drone drone, FieldPlot field) {
        if (drone == null || field == null || drone.getLongitude() == null || drone.getLatitude() == null
                || field.getLongitude() == null || field.getLatitude() == null) {
            return 0.0;
        }
        double lonDistance = drone.getLongitude().subtract(field.getLongitude()).doubleValue();
        double latDistance = drone.getLatitude().subtract(field.getLatitude()).doubleValue();
        return Math.sqrt(lonDistance * lonDistance + latDistance * latDistance) * 111;
    }

    private void insertTrack(Long recordId, Drone drone) {
        if (drone == null || drone.getLongitude() == null || drone.getLatitude() == null) {
            return;
        }
        OperationTrack track = new OperationTrack();
        track.setRecordId(recordId);
        track.setDroneId(drone.getId());
        track.setLongitude(drone.getLongitude());
        track.setLatitude(drone.getLatitude());
        track.setAltitude(BigDecimal.valueOf(3.5));
        track.setSpeed(BigDecimal.valueOf(6.0));
        track.setBatteryPercent(drone.getBatteryPercent());
        track.setReportedAt(LocalDateTime.now());
        trackMapper.insert(track);
    }

    private void insertRouteTracks(Long recordId, Drone drone, FieldPlot targetField) {
        if (drone == null || drone.getLongitude() == null || drone.getLatitude() == null
                || targetField == null || targetField.getLongitude() == null || targetField.getLatitude() == null) {
            insertTrack(recordId, drone);
            return;
        }
        double startLon = drone.getLongitude().doubleValue();
        double startLat = drone.getLatitude().doubleValue();
        double endLon = targetField.getLongitude().doubleValue();
        double endLat = targetField.getLatitude().doubleValue();
        LocalDateTime now = LocalDateTime.now();
        for (int index = 1; index <= 6; index++) {
            double ratio = index / 6.0;
            OperationTrack track = new OperationTrack();
            track.setRecordId(recordId);
            track.setDroneId(drone.getId());
            track.setLongitude(BigDecimal.valueOf(startLon + (endLon - startLon) * ratio).setScale(6, RoundingMode.HALF_UP));
            track.setLatitude(BigDecimal.valueOf(startLat + (endLat - startLat) * ratio).setScale(6, RoundingMode.HALF_UP));
            track.setAltitude(BigDecimal.valueOf(3.5));
            track.setSpeed(BigDecimal.valueOf(6.0));
            track.setBatteryPercent(Math.max(0, drone.getBatteryPercent() - index));
            track.setReportedAt(now.plusSeconds(index * 10L));
            trackMapper.insert(track);
        }
    }

    private record DroneScore(Drone drone, double score, String reason) {
    }
}
