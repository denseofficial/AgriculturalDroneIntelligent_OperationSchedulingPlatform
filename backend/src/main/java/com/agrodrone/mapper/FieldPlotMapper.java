package com.agrodrone.mapper;

import com.agrodrone.entity.FieldPlot;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FieldPlotMapper extends BaseMapper<FieldPlot> {
    @Select("""
            SELECT id, name, crop_type, area_mu, soil_moisture_level,
                   pest_risk_level, longitude, latitude
            FROM field_plot
            WHERE pest_risk_level = #{riskLevel}
            ORDER BY area_mu DESC
            """)
    List<FieldPlot> selectByPestRisk(@Param("riskLevel") String riskLevel);
}
