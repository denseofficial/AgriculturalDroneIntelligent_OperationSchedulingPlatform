package com.agrodrone.mapper;

import com.agrodrone.entity.OperationRecord;
import com.agrodrone.vo.OperationRecordVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface OperationRecordMapper extends BaseMapper<OperationRecord> {
    List<OperationRecordVO> selectRecordDetails();
}
