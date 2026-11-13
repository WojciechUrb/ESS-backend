package com.urbaniak.evacuation_support_system.components.evacuationsolver.output.dto;

import com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm.dto.CalculationsResult;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm.dto.EvacuationPlan;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.input.dto.BuildingLayout;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.input.dto.RoomAssignment;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.input.dto.SkippedUser;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.output.mapper.MobileRouteMapper;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public record OutputData(
        int allUsersCount,
        int assignedToEvacuationCount,
        List<MobileRoute> routes,
        List<UUID> notEvacuated,
        List<SkippedUser> skippedUsers,
        int horizon,
        int agentsEvacuated,
        int evacuationTime, // ilosc rund
        Map<String, Integer> exitsUsage,    // <wyjscie, uzycia>
        double plannerComputationTime,
        double evacuationComputationTime
) {


    public static OutputData from(BuildingLayout buildingLayout, RoomAssignment assignment, EvacuationPlan plan, CalculationsResult result){
        return OutputData.builder()
                .allUsersCount(assignment.assignedCount() + assignment.skipped().size())
                .assignedToEvacuationCount(assignment.assignedCount())
                .routes(MobileRouteMapper.toResponse(result.routes(), buildingLayout.exits()))
                .notEvacuated(result.notEvacuated())
                .skippedUsers(assignment.skipped())
                .horizon(plan.horizon())
                .agentsEvacuated(result.evacuatedCount())
                .evacuationTime(result.evacuationTime()) // <= horizon
                .exitsUsage(result.exitsUsage())
                .plannerComputationTime(plan.computationTime()) // czas planowania
                .evacuationComputationTime(result.computationTime()) // czas symulacji
                .build();



    }
}
