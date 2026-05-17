package com.agrodrone.service;

import com.agrodrone.entity.Drone;
import com.agrodrone.entity.FieldBoundary;
import com.agrodrone.entity.OperationTrack;
import com.agrodrone.mapper.DroneMapper;
import com.agrodrone.mapper.FieldBoundaryMapper;
import com.agrodrone.mapper.FieldPlotMapper;
import com.agrodrone.mapper.OperationTrackMapper;
import com.agrodrone.vo.MapOverviewVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MapService {
    private final FieldPlotMapper fieldPlotMapper;
    private final FieldBoundaryMapper boundaryMapper;
    private final DroneMapper droneMapper;
    private final OperationTrackMapper trackMapper;

    public MapService(FieldPlotMapper fieldPlotMapper, FieldBoundaryMapper boundaryMapper,
                      DroneMapper droneMapper, OperationTrackMapper trackMapper) {
        this.fieldPlotMapper = fieldPlotMapper;
        this.boundaryMapper = boundaryMapper;
        this.droneMapper = droneMapper;
        this.trackMapper = trackMapper;
    }

    public MapOverviewVO overview() {
        MapOverviewVO vo = new MapOverviewVO();
        vo.setFields(fieldPlotMapper.selectList(null));
        vo.setDrones(droneMapper.selectList(null));
        List<FieldBoundary> boundaries = boundaryMapper.selectList(new LambdaQueryWrapper<FieldBoundary>()
                .orderByAsc(FieldBoundary::getFieldId)
                .orderByAsc(FieldBoundary::getPointOrder));
        Map<Long, List<FieldBoundary>> boundaryMap = boundaries.stream()
                .collect(Collectors.groupingBy(FieldBoundary::getFieldId));
        vo.setBoundaries(boundaryMap);
        vo.setTracks(trackMapper.selectList(new LambdaQueryWrapper<OperationTrack>()
                .orderByDesc(OperationTrack::getReportedAt)
                .last("LIMIT 200"))
                .stream()
                .sorted(java.util.Comparator.comparing(OperationTrack::getReportedAt))
                .toList());
        return vo;
    }
}
