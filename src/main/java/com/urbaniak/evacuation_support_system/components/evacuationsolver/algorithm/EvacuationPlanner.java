package com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm;

import com.urbaniak.evacuation_support_system.components.evacuationsolver.algorithm.dto.EvacuationPlan;
import com.urbaniak.evacuation_support_system.components.evacuationsolver.input.dto.*;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class EvacuationPlanner {

    /** Domyślny limit horyzontu */
    public static final int DEFAULT_MAX_HORIZON = 500;


    private final int maxHorizon;
    private final Cell OUTSIDE = new Cell(-1, -1, -10);

    public EvacuationPlanner(){
        this.maxHorizon = DEFAULT_MAX_HORIZON;
    }

    /** @param maxHorizon największe T, jakie może zostać przekazane do symulacji */
    public EvacuationPlanner(int maxHorizon) {
        if (maxHorizon < 1) {
            throw new IllegalArgumentException("Limit horyzontu musi być dodatni");
        }
        this.maxHorizon = maxHorizon;
    }

    private HashMap<Cell, Integer> distances(BuildingLayout buildingLayout){
        Graph<Cell, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        for(Cell cell : buildingLayout.cells()){
            graph.addVertex(cell);
        }
        graph.addVertex(OUTSIDE);

        for(Cell cell : buildingLayout.cells()){
            for(Cell neighbour : buildingLayout.getNeighbours(cell)){
                var edge = graph.addEdge(cell, neighbour);
                graph.setEdgeWeight(edge, 1);
                }
        }

        for(Exit exit : buildingLayout.exits()){
            Cell exitCell = new Cell(exit.x(), exit.y(), exit.floor());
            DefaultWeightedEdge edge = graph.addEdge(exitCell, OUTSIDE);
            graph.setEdgeWeight(edge, 0);
        }

        Graph<Cell, DefaultWeightedEdge> reversedGraph = new EdgeReversedGraph<>(graph);
        var paths = new DijkstraShortestPath<>(reversedGraph).getPaths(OUTSIDE);

        HashMap<Cell, Integer> distance = new HashMap<Cell, Integer>();
        
        // jesli infiniy, to znaczy brak drogi do wyjscia
        for(Cell cell : buildingLayout.cells() ){
            var w = paths.getWeight(cell);
            if(w != Double.POSITIVE_INFINITY){
                distance.put(cell, (int) w);
            }
        }
        
        return  distance;
    }

    private void releaseFromRooms(BuildingLayout buildingLayout,
                                  HashMap<String, Integer> waiting,
                                  ArrayList<Cell> onCorridor,
                                  HashSet<Cell> reserved)
    {
        for(Room room : buildingLayout.rooms()){
            if(waiting.get(room.id()) == 0){
                continue;
            }

            var door = new Cell(room.doorX(), room.doorY(), room.floor());

            if(reserved.contains(door)){ continue; }

            onCorridor.add(door);
            reserved.add(door);
            waiting.put(room.id(), waiting.get(room.id()) -1);

        }

    }

    public EvacuationPlan plan(BuildingLayout buildingLayout, RoomAssignment roomAssignment){
        long startTime, endTime;

        startTime = System.nanoTime();

        if(buildingLayout == null){
            throw new IllegalArgumentException("BuildingLayout jest NULL");
        }

        if(roomAssignment == null){
            throw new IllegalArgumentException("rommAssigmnet jest NULL");
        }

        int assignedAgents = roomAssignment.assignedCount();
        if(assignedAgents == 0){
            return new EvacuationPlan(1, false, 0);
        }

        HashMap<Cell, Integer> distances = distances(buildingLayout);
        HashMap<String, Integer> waiting = new HashMap<>();

        for(Room room : buildingLayout.rooms()){
            waiting.put(room.id(), roomAssignment.usersCountInRoom(room.id()));
        }

        ArrayList<Cell> onCorridor = new ArrayList<>();
        HashSet<Cell> reserved = new HashSet<>();
        int evacuated = 0;

        releaseFromRooms(buildingLayout, waiting, onCorridor, reserved);
        for(int t=0; t <= maxHorizon; t++){

            int wait  = onCorridor.size();
            onCorridor.removeIf(cell -> distances.get(cell) == 0);
            evacuated += wait - onCorridor.size();

            if (evacuated == assignedAgents){
                endTime = System.nanoTime();
                return  new EvacuationPlan(Math.max(t, 1), false, (double) (endTime - startTime)/1_000_000.0);
            }

            onCorridor.sort(Comparator.comparingInt(distances::get));

            var next = new ArrayList<Cell>();
            reserved = new HashSet<>();

            for(Cell position : onCorridor){
                var target = position;

                for(Cell neighbour : buildingLayout.getNeighbours(position)){
                        if(distances.containsKey(neighbour) &&
                                distances.get(neighbour) == distances.get(position) - 1 &&
                                !reserved.contains(neighbour)){
                            target = neighbour;
                            break;
                        }

                }

                reserved.add(target);
                next.add(target);
            }
            onCorridor = next;

            releaseFromRooms(buildingLayout, waiting, onCorridor, reserved);
        }
        endTime = System.nanoTime();
        return new EvacuationPlan(maxHorizon, true, (double) (endTime - startTime)/1_000_000.0 );
    }


}
