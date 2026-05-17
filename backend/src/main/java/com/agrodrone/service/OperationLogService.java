package com.agrodrone.service;

import com.agrodrone.entity.OperationLog;
import com.agrodrone.mapper.OperationLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OperationLogService {
    private final OperationLogMapper operationLogMapper;

    public OperationLogService(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    public void record(String moduleName, String actionName, String targetType, Long targetId, String content) {
        OperationLog log = new OperationLog();
        log.setModuleName(moduleName);
        log.setActionName(actionName);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setContent(content);
        log.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    public List<OperationLog> latest() {
        return operationLogMapper.selectList(new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getCreatedAt)
                .last("LIMIT 100"));
    }
}
