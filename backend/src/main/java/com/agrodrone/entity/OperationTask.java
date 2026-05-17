package com.agrodrone.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("operation_task")
public class OperationTask {
    private Long id;
    private String taskNo;
    private Long fieldId;
    private String operationType;
    private BigDecimal requiredAreaMu;
    private Integer priority;
    private LocalDateTime earliestStartTime;
    private LocalDateTime latestEndTime;
    private String status;
    private String remark;

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

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public BigDecimal getRequiredAreaMu() {
        return requiredAreaMu;
    }

    public void setRequiredAreaMu(BigDecimal requiredAreaMu) {
        this.requiredAreaMu = requiredAreaMu;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public LocalDateTime getEarliestStartTime() {
        return earliestStartTime;
    }

    public void setEarliestStartTime(LocalDateTime earliestStartTime) {
        this.earliestStartTime = earliestStartTime;
    }

    public LocalDateTime getLatestEndTime() {
        return latestEndTime;
    }

    public void setLatestEndTime(LocalDateTime latestEndTime) {
        this.latestEndTime = latestEndTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
