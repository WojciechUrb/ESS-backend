package com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto;

import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.RoomAssigner;

import java.util.UUID;

/**
 * Położenie jednego usera - dokładnie to, co backend wyciąga z bazy przed ewakuacją.
 *
 * @param userId id usera w bazie; staje się identyfikatorem agenta w grafie i kluczem jego trasy
 * @param roomId id pokoju w formacie piętro + nr pokoju na piętrze (np. 101, 242, 333);
 *               może być null, jeśli user nie ma zapisanego pokoju - wtedy zostanie
 *               pominięty przy przypisaniu (patrz {@link RoomAssigner})
 */
public record UserLocation(UUID userId, String roomId) {
}