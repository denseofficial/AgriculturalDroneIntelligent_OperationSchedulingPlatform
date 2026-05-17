package com.agrodrone.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OperationRecordVO {
    private Long id;
    private String taskNo;
    private String fieldName;
    private String droneCode;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private BigDecimal actualAreaMu;
    private Integer durationMinutes;
    private String result;
    private String exceptionRemark;

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

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDroneCode() {
        return droneCode;
    }

    public void setDroneCode(String droneCode) {
        this.droneCode = droneCode;
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
}
