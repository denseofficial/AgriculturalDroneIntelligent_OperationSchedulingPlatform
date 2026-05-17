package com.agrodrone.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ScheduleDetailVO {
    private Long id;
    private String taskNo;
    private String operationType;
    private String fieldName;
    private String cropType;
    private String droneCode;
    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private BigDecimal estimatedAreaMu;
    private BigDecimal score;
    private String decisionReason;
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getCropType() {
        return cropType;
    }

    public void setCropType(String cropType) {
        this.cropType = cropType;
    }

    public String getDroneCode() {
        return droneCode;
    }

    public void setDroneCode(String droneCode) {
        this.droneCode = droneCode;
    }

    public LocalDateTime getPlannedStartTime() {
        return plannedStartTime;
    }

    public void setPlannedStartTime(LocalDateTime plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
    }

    public LocalDateTime getPlannedEndTime() {
        return plannedEndTime;
    }

    public void setPlannedEndTime(LocalDateTime plannedEndTime) {
        this.plannedEndTime = plannedEndTime;
    }

    public BigDecimal getEstimatedAreaMu() {
        return estimatedAreaMu;
    }

    public void setEstimatedAreaMu(BigDecimal estimatedAreaMu) {
        this.estimatedAreaMu = estimatedAreaMu;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
