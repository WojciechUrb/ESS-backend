package com.urbaniak.evacuation_support_system.components.evacuationsolver;

import com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm.EvacuationGraph;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm.EvacuationPlanner;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm.dto.CalculationsResult;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm.dto.EvacuationPlan;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.input.MapParser;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.input.RoomAssigner;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.input.dto.*;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.output.dto.OutputData;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class EvacuationSolver {
    private final MapParser mapParser;
    private final RoomAssigner roomAssigner;
    private final EvacuationPlanner evacuationPlanner;


    public OutputData solve(Set<String> maps, List<Room> rooms, List<UserLocation> usersLocation){

        BuildingLayout buildingLayout = new BuildingLayout(rooms, mapParser.parseMap(maps));
        RoomAssignment roomAssignment = roomAssigner.assign(buildingLayout, usersLocation);
        EvacuationPlan evacuationPlan = evacuationPlanner.plan(buildingLayout, roomAssignment);

        EvacuationGraph evacuationGraph = new EvacuationGraph(buildingLayout, roomAssignment, evacuationPlan);
        CalculationsResult calculationsResult = evacuationGraph.simulateEvacuation();


        return OutputData.from(buildingLayout, roomAssignment, evacuationPlan, calculationsResult);

    }
}
