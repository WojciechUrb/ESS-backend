package com.urbaniak.evacuation_support_system.dto.Request;

import com.urbaniak.evacuation_support_system.dto.BlockedCell;
import lombok.Builder;

import java.util.List;

@Builder
public record BlockedCellsRequest(List<BlockedCell> blockedCells) {
}
