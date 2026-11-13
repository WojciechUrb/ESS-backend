package com.urbaniak.evacuation_support_system.components.evacuationsolver.input.dto;

public record Cell(int x, int y, int floor) {
    // klatki schodowe nie są przypisane do pięter
    public static final int STAIRS = -1;

    // x - oznaczenie klatki, y - numer segmentu klatki schodowej
    public static Cell stairSegment(int staircaseMark, int segment){ return  new Cell(staircaseMark, segment, STAIRS); }
    public boolean isStairSegment(){ return floor == STAIRS; }
}
