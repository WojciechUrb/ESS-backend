package com.urbaniak.evacuation_support_system.components.evacuation_solver;

import com.urbaniak.evacuation_support_system.components.evacuation_solver.algorithm.EvacuationGraph;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.algorithm.EvacuationPlanner;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.algorithm.dto.CalculationsResult;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.algorithm.dto.EvacuationPlan;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.MapParser;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.RoomAssigner;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.*;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.output.dto.OutputData;
import com.urbaniak.evacuation_support_system.dto.BlockedCell;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class EvacuationSolver {
    private final MapParser mapParser;
    private final RoomAssigner roomAssigner;
    private final EvacuationPlanner evacuationPlanner;


    public OutputData solve(Set<String> maps, List<Room> rooms, List<UserLocation> usersLocation, Set<Cell> blockedCells){

        BuildingLayout buildingLayout = new BuildingLayout(rooms, mapParser.parseMap(maps, blockedCells));
        RoomAssignment roomAssignment = roomAssigner.assign(buildingLayout, usersLocation);
        EvacuationPlan evacuationPlan = evacuationPlanner.plan(buildingLayout, roomAssignment);

        EvacuationGraph evacuationGraph = new EvacuationGraph(buildingLayout, roomAssignment, evacuationPlan);
        CalculationsResult calculationsResult = evacuationGraph.simulateEvacuation();


        return OutputData.from(buildingLayout, roomAssignment, evacuationPlan, calculationsResult);

    }
}
