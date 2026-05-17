package com.agrodrone.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("operation_record")
public class OperationRecord {
    private Long id;
    private Long schedulePlanId;
    private Long taskId;
    private Long droneId;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private BigDecimal actualAreaMu;
    private Integer durationMinutes;
    private String result;
    private String exceptionRemark;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSchedulePlanId() {
        return schedulePlanId;
    }

    public void setSchedulePlanId(Long schedulePlanId) {
        this.schedulePlanId = schedulePlanId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getDroneId() {
        return droneId;
    }

    public void setDroneId(Long droneId) {
        this.droneId = droneId;
    }

    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(LocalDateTime actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public BigDecimal getActualAreaMu() {
        return actualAreaMu;
    }

    public void setActualAreaMu(BigDecimal actualAreaMu) {
        this.actualAreaMu = actualAreaMu;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getExceptionRemark() {
        return exceptionRemark;
    }

    public void setExceptionRemark(String exceptionRemark) {
        this.exceptionRemark = exceptionRemark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
