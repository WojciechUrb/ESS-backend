package com.urbaniak.evacuation_support_system.components.evacuation_solver.input;

import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.Cell;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.Exit;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.FloorMap;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.ParseMapResult;
import com.urbaniak.evacuation_support_system.dto.BlockedCell;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

public class MapParser {
    @Value("${evacuation.building-id}")
    private String buildingId;

    public ParseMapResult parseMap(Set<String> maps, Set<Cell> blockedCells){

        int floor;


        Set<Cell> corridorCells = new HashSet<>();
        Map<Character, Set<Cell>> stairCells = new HashMap<>(); // char - oznaczenie klatki
        List<Exit> exits = new ArrayList<>();

        Set<FloorMap> floorMaps = new HashSet<>();

        if(maps == null){
            throw new IllegalArgumentException("Brak map budynku (błąd argumentu).");
        }

        for(String map : maps) {
            String[] lines = map.split("\\R");
            corridorCells.clear();
            stairCells.clear();

            if(!lines[0].trim().equals(buildingId)){
                throw new IllegalArgumentException("Mapa nienależąca do budynku: " + lines[0].trim() + ", oczekiwana mapa: " + buildingId);
            }

            floor = Integer.parseInt(lines[1].trim());

            for (int y = 2; y < lines.length; y++) {
                for (int x = 0; x < lines[y].length(); x++) {
                    char c = lines[y].charAt(x);
                    if (c == 'o') continue;

                    Cell cell = new Cell(x, y-2, floor);
                    if (c == 'x'){
                        if(!blockedCells.contains(cell)) corridorCells.add(cell);
                    }
                    if (c == 'e') {
                        if(!blockedCells.contains(cell)) {
                            corridorCells.add(cell);
                            exits.add(new Exit("exit_f" + floor + "_x" + x + "_y" + (y - 2), x, y - 2, floor));
                        }
                    }
                    if ( (c >= 'A' && c <= 'Z') ){
                        if(!blockedCells.contains(cell)) {
                            corridorCells.add(cell);
                            stairCells.computeIfAbsent(c, k -> new HashSet<>()).add(new Cell(x, y - 2, floor));
                        }
                    }
                }
            }
            if(corridorCells.isEmpty()){
                throw new IllegalArgumentException("Pusta mapa korytarza");
            }
            floorMaps.add(new FloorMap(floor, corridorCells, stairCells));
        }


        return new ParseMapResult(floorMaps, exits);
    }


}
