package com.agrodrone.mapper;

import com.agrodrone.entity.SchedulePlan;
import com.agrodrone.vo.ScheduleDetailVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface SchedulePlanMapper extends BaseMapper<SchedulePlan> {
    List<ScheduleDetailVO> selectScheduleDetails();
}
