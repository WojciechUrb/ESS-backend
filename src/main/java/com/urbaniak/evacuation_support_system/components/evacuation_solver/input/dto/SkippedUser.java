package com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto;

import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.Reason;

import java.util.UUID;

/**
 * User, którego nie dało się przypisać do żadnego pokoju, więc nie bierze udziału w obliczeniach.
 * Algorytm go pomija i tylko zgłasza - co z tym zrobić (log, alert) decyduje backend.
 *
 * @param roomId id pokoju zapisane w bazie (null dla powodu NO_ROOM)
 */
public record SkippedUser(UUID userId, String roomId, Reason reason) {

}