package com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto;

import java.util.List;
import java.util.Set;

public record ParseMapResult(Set<FloorMap> floorMaps, List<Exit> exits) {

}
