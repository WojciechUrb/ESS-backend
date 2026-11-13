package com.urbaniak.evacuation_support_system.components.evacuationsolver.input.dto;

public record Staircase(char mark, int fromFloor, int toFloor,int length, int width) {
    public static final int DEAFAULT_LENGTH = 10;
    public static final int DEFAULT_WIDTH = 1;

    public Staircase {
        if(!(length >= 1) || !(width >= 1) || fromFloor == toFloor){
            throw new IllegalArgumentException("Niepoprawny dane schodów");
        }
    }


}
