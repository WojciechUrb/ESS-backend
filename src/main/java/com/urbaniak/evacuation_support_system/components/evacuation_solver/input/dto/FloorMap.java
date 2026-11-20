package com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto;

import java.util.Map;
import java.util.Set;



public record FloorMap(int floor, Set<Cell> cells, Map<Character, Set<Cell> > stairCells) {

    public FloorMap {
        cells = Set.copyOf(cells);
        stairCells = Map.copyOf(stairCells);
    }
}
