package com.urbaniak.evacuation_support_system.components.evacuationsolver;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "evacuation.solver")
public record EvacuationSolverProperties(
        @Min(1) @Max(5000) int maxHorizon,
        @NotBlank String mapsLocation
) {
}
