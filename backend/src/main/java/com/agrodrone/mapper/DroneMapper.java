package com.agrodrone.mapper;

import com.agrodrone.entity.Drone;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DroneMapper extends BaseMapper<Drone> {
    @Select("""
            SELECT id, code, model, payload_kg, spray_width_meter, battery_percent,
                   status, longitude, latitude, updated_at
            FROM drone
            WHERE status IN ('IDLE', 'CHARGING')
              AND battery_percent >= #{minBattery}
            ORDER BY battery_percent DESC, payload_kg DESC
            """)
    List<Drone> selectDispatchableDrones(@Param("minBattery") int minBattery);
}
