package com.urbaniak.evacuation_support_system.dto;

import lombok.Builder;

@Builder
public record BlockedCell(int floor, int x, int y) {
}
