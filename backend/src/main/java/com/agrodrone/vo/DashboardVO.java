package com.agrodrone.vo;

import java.math.BigDecimal;

public class DashboardVO {
    private long droneCount;
    private long idleDroneCount;
    private long pendingTaskCount;
    private long scheduledTaskCount;
    private BigDecimal todayAreaMu;

    public long getDroneCount() {
        return droneCount;
    }

    public void setDroneCount(long droneCount) {
        this.droneCount = droneCount;
    }

    public long getIdleDroneCount() {
        return idleDroneCount;
    }

    public void setIdleDroneCount(long idleDroneCount) {
        this.idleDroneCount = idleDroneCount;
    }

    public long getPendingTaskCount() {
        return pendingTaskCount;
    }

    public void setPendingTaskCount(long pendingTaskCount) {
        this.pendingTaskCount = pendingTaskCount;
    }

    public long getScheduledTaskCount() {
        return scheduledTaskCount;
    }

    public void setScheduledTaskCount(long scheduledTaskCount) {
        this.scheduledTaskCount = scheduledTaskCount;
    }

    public BigDecimal getTodayAreaMu() {
        return todayAreaMu;
    }

    public void setTodayAreaMu(BigDecimal todayAreaMu) {
        this.todayAreaMu = todayAreaMu;
    }
}
