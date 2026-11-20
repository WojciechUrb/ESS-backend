package com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto;

import java.util.*;

/**
 * Wynik przypisania userów do pokoi - "migawka" stanu budynku w chwili wywołania ewakuacji.
 *
 * Obiekt jest niezmienny (wszystkie kolekcje to kopie tylko do odczytu). Dzięki temu graf
 * budowany z tego przypisania nie zobaczy zmian, które backend w międzyczasie wprowadzi
 * w bazie albo w innym wątku - kolejna zmiana położenia userów = nowe przypisanie
 * i nowe obliczenie.
 *
 * @param usersByRoom id pokoju -> userzy, którzy w nim są (tylko pokoje z co najmniej jednym userem)
 * @param skipped     userzy pominięci przy przypisaniu, razem z powodem
 */
public record RoomAssignment(Map<String, List<UUID>> usersByRoom, List<SkippedUser> skipped) {

    public RoomAssignment {
        Map<String, List<UUID>> copy = new LinkedHashMap<>();
        usersByRoom.forEach((roomId, users) -> copy.put(roomId, List.copyOf(users)));
        usersByRoom = Collections.unmodifiableMap(copy);
        skipped = List.copyOf(skipped);
    }

    /** uzytkownicy przypisani do danego pokoju. pusta lista, jeśli nikogo nie ma w pomieszczeniu */
    public List<UUID> usersIn(String roomId) {
        return usersByRoom.getOrDefault(roomId, List.of());
    }

    public Integer usersCountInRoom(String roomId){ return usersByRoom.getOrDefault(roomId, List.of()).size();   }

    /** liczba uzytkownikow, ktorzy wezma udzial w obliczeniach algorytmu (bez pominietych). */
    public int assignedCount() {
        int count = 0;
        for (List<UUID> users : usersByRoom.values()) {
            count += users.size();
        }
        return count;
    }
}