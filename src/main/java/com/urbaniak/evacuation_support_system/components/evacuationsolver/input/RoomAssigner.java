package com.urbaniak.evacuation_support_system.components.evacuationsolver.input;

import com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm.EvacuationGraph;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.input.dto.*;

import java.util.*;

/**
 * Pierwszy krok ewakuacji: przypisanie każdego usera (jego źródła w grafie) do pokoju.
 *
 * Przepływ w backendzie:
 *   1. pobranie userów z bazy i zamiana na {@link UserLocation} (userId + roomId),
 *   2. {@link #assign} - dopasowanie roomId do pokoi z układu budynku,
 *   3. dopiero z gotowego {@link RoomAssignment} budowany jest {@link EvacuationGraph}
 *      i uruchamiany algorytm Dinica.
 *
 * Klasa nie trzyma stanu, więc można ją bezpiecznie wołać z wielu wątków naraz.
 */
public final class RoomAssigner {
    /**
     * @param buildingLayout układ budynku (pokoje z drzwiami na siatce korytarza)
     * @param users położenia userów pobrane z bazy
     * @return przypisanie userów do pokoi oraz lista pominiętych
     * @throws IllegalArgumentException gdy userId jest null albo powtarza się - to błąd danych,
     *         którego nie da się sensownie pominąć (dwa wpisy dla jednej osoby = dwa agenty w grafie)
     */
    public RoomAssignment assign(BuildingLayout buildingLayout, List<UserLocation> users) {
        if(buildingLayout == null || users == null){
            throw new IllegalArgumentException("null na buildingLayout/user do przypisywania pomieszczen");
        }

        Set<String> reachableRooms =  ReachabilityChecker.reachableRooms(buildingLayout);

        Map<String, Room> roomsById = new HashMap<>();
        for (Room room : buildingLayout.rooms()) {
            roomsById.put(room.id(), room);
        }

        Map<String, List<UUID>> usersByRoom = new LinkedHashMap<>();
        List<SkippedUser> skipped = new ArrayList<>();
        Set<UUID> seen = new HashSet<>();

        for (UserLocation user : users) {
            if(user == null){
                throw new IllegalArgumentException("null na liscie uzytkownikow");
            }

            //uzytkownik bez id
            if (user.userId() == null) {
                throw new IllegalArgumentException("User bez id (roomId=" + user.roomId() + ")");
            }

            // zduplikowany uzytkownik
            if (!seen.add(user.userId())) {
                throw new IllegalArgumentException("Zduplikowane id usera: " + user.userId());
            }

            // brak pokoju w bazie - uzytkownik pomijany
            if (user.roomId() == null) {
                skipped.add(new SkippedUser(user.userId(), null, Reason.NO_ROOM));
                continue;
            }

            // pokoj zapisany w bazie, ale nie ma go w układzie budynku - pomijany i zglaszany
            if (!roomsById.containsKey(user.roomId())) {
                skipped.add(new SkippedUser(user.userId(), user.roomId(), Reason.UNKNOWN_ROOM));
                continue;
            }

            // brak mozliwosci osiagniecia wyjscia z budynku z pokoju uzytkownika - pomijany
            if(!reachableRooms.contains(user.roomId())){
                skipped.add(new SkippedUser(user.userId(), user.roomId(), Reason.UNREACHABLE));
                continue;
            }

            List<UUID> roomUsers = usersByRoom.get(user.roomId());
            if(roomUsers == null){
                roomUsers = new ArrayList<>();
                usersByRoom.put(user.roomId(), roomUsers);
            }
            roomUsers.add(user.userId());

        }

        return new RoomAssignment(usersByRoom, skipped);
    }
}