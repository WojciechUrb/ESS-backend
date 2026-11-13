package com.urbaniak.evacuation_support_system.components.evacuationsolver.output.dto;

import java.util.List;
import java.util.UUID;

public record MobileRoute(UUID userId, String roomId, String exitName, List<GridPoint> path) {
}
