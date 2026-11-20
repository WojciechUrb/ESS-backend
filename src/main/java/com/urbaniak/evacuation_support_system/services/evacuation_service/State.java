package com.urbaniak.evacuation_support_system.services.evacuation_service;

import com.urbaniak.evacuation_support_system.components.evacuation_solver.output.dto.OutputData;

public record State(EvacuationState evacuationState, OutputData outputData) {
}
