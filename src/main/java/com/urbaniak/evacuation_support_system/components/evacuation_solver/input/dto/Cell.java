package com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto;

public record Cell(int x, int y, int floor) {
    public static final int STAIRS = -1;

    public static Cell stairSegment(int staircaseMark, int segment){ return  new Cell(staircaseMark, segment, STAIRS); }
    public boolean isStairSegment(){ return floor == STAIRS; }
}
