package com.urbaniak.evacuation_support_system.components.evacuation_solver.input;

import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.BuildingLayout;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.Cell;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.Exit;
import com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto.Room;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class ReachabilityChecker {

    public static Set<String> reachableRooms(BuildingLayout buildingLayout){
        Set<String> reachable = new HashSet<>();

        Set<Cell> visited = new HashSet<>();
        ArrayDeque<Cell> queue = new ArrayDeque<>();

        for(Exit exit : buildingLayout.exits()){
            Cell start = new Cell(exit.x(), exit.y(), exit.floor());
            if( visited.add(start)){
                queue.add(start);
            }
        }

        while (!queue.isEmpty()){
            Cell cell = queue.poll();
            for(Cell next : buildingLayout.getNeighbours(cell)){
                if(visited.add(next)){
                    queue.add(next);
                }
            }
        }


        for(Room room: buildingLayout.rooms()){
            if(visited.contains(new Cell(room.doorX(), room.doorY(), room.floor()))){
                reachable.add(room.id());
            }
        }

        return reachable;
    }
}
