package com.agrodrone.controller;

import com.agrodrone.common.ApiResponse;
import com.agrodrone.entity.FieldPlot;
import com.agrodrone.mapper.FieldPlotMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fields")
public class FieldPlotController {
    private final FieldPlotMapper fieldPlotMapper;

    public FieldPlotController(FieldPlotMapper fieldPlotMapper) {
        this.fieldPlotMapper = fieldPlotMapper;
    }

    @GetMapping
    public ApiResponse<List<FieldPlot>> list() {
        return ApiResponse.ok(fieldPlotMapper.selectList(new LambdaQueryWrapper<FieldPlot>().orderByAsc(FieldPlot::getName)));
    }

    @PostMapping
    public ApiResponse<FieldPlot> save(@RequestBody FieldPlot fieldPlot) {
        if (fieldPlot.getId() == null) {
            fieldPlotMapper.insert(fieldPlot);
        } else {
            fieldPlotMapper.updateById(fieldPlot);
        }
        return ApiResponse.ok(fieldPlot);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        return ApiResponse.ok(fieldPlotMapper.deleteById(id) > 0);
    }
}
