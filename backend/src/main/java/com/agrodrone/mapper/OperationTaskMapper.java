package com.agrodrone.mapper;

import com.agrodrone.entity.OperationTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OperationTaskMapper extends BaseMapper<OperationTask> {
    @Select("""
            SELECT id, task_no, field_id, operation_type, required_area_mu,
                   priority, earliest_start_time, latest_end_time, status, remark
            FROM operation_task
            WHERE status = 'PENDING'
            ORDER BY priority DESC, latest_end_time ASC
            """)
    List<OperationTask> selectPendingTasksForSchedule();
}
