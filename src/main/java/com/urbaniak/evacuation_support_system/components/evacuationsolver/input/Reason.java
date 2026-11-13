package com.urbaniak.evacuation_support_system.components.evacuationsolver.input;

import com.urbaniak.evacuation_support_system.components.evacuationsolver.input.dto.SkippedUser;

/** Powód, dla którego {@link RoomAssigner} pominął usera - zapisywany w {@link SkippedUser}. */
public enum Reason {
    /** User nie ma w bazie zapisanego pokoju (roomId == null). */
    NO_ROOM,
    /** Zapisany pokój nie istnieje w układzie budynku przekazanym do algorytmu. */
    UNKNOWN_ROOM,
    /** Brak możliwości osiągnięcia wyjścia */
    UNREACHABLE
}