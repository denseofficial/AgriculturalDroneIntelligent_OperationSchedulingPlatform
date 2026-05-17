package com.agrodrone.vo;

import com.agrodrone.entity.Drone;
import com.agrodrone.entity.FieldBoundary;
import com.agrodrone.entity.FieldPlot;
import com.agrodrone.entity.OperationTrack;

import java.util.List;
import java.util.Map;

public class MapOverviewVO {
    private List<FieldPlot> fields;
    private Map<Long, List<FieldBoundary>> boundaries;
    private List<Drone> drones;
    private List<OperationTrack> tracks;

    public List<FieldPlot> getFields() {
        return fields;
    }

    public void setFields(List<FieldPlot> fields) {
        this.fields = fields;
    }

    public Map<Long, List<FieldBoundary>> getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(Map<Long, List<FieldBoundary>> boundaries) {
        this.boundaries = boundaries;
    }

    public List<Drone> getDrones() {
        return drones;
    }

    public void setDrones(List<Drone> drones) {
        this.drones = drones;
    }

    public List<OperationTrack> getTracks() {
        return tracks;
    }

    public void setTracks(List<OperationTrack> tracks) {
        this.tracks = tracks;
    }
}
