package com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm.dto;

import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public record CalculationsResult(int evacuatedCount,
                                 List<AgentRoute> routes,
                                 List<UUID> notEvacuated,
                                 int evacuationTime,
                                 Map<String, Integer> exitsUsage,
                                 double computationTime
                                 ) {}
