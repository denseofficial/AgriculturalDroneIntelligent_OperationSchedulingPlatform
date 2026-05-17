package com.agrodrone.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("field_plot")
public class FieldPlot {
    private Long id;
    private String name;
    private String cropType;
    private BigDecimal areaMu;
    private String soilMoistureLevel;
    private String pestRiskLevel;
    private BigDecimal longitude;
    private BigDecimal latitude;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCropType() {
        return cropType;
    }

    public void setCropType(String cropType) {
        this.cropType = cropType;
    }

    public BigDecimal getAreaMu() {
        return areaMu;
    }

    public void setAreaMu(BigDecimal areaMu) {
        this.areaMu = areaMu;
    }

    public String getSoilMoistureLevel() {
        return soilMoistureLevel;
    }

    public void setSoilMoistureLevel(String soilMoistureLevel) {
        this.soilMoistureLevel = soilMoistureLevel;
    }

    public String getPestRiskLevel() {
        return pestRiskLevel;
    }

    public void setPestRiskLevel(String pestRiskLevel) {
        this.pestRiskLevel = pestRiskLevel;
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
}
