package com.urbaniak.evacuation_support_system.models.qr;

import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.Room;

public record QrPoint(String id, QrPointType type, int floor, int x, int y, String label) {
    public Room toSolverRoom() {
        return new Room(id, x, y, floor);
    }
}
