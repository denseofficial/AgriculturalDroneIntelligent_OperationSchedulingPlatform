package com.agrodrone.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("drone")
public class Drone {
    private Long id;
    private String code;
    private String model;
    private BigDecimal payloadKg;
    private BigDecimal sprayWidthMeter;
    private Integer batteryPercent;
    private String status;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public BigDecimal getPayloadKg() {
        return payloadKg;
    }

    public void setPayloadKg(BigDecimal payloadKg) {
        this.payloadKg = payloadKg;
    }

    public BigDecimal getSprayWidthMeter() {
        return sprayWidthMeter;
    }

    public void setSprayWidthMeter(BigDecimal sprayWidthMeter) {
        this.sprayWidthMeter = sprayWidthMeter;
    }

    public Integer getBatteryPercent() {
        return batteryPercent;
    }

    public void setBatteryPercent(Integer batteryPercent) {
        this.batteryPercent = batteryPercent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
