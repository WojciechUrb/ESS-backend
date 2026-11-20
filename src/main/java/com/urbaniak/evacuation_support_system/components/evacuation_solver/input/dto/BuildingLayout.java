package com.urbaniak.evacuation_support_system.components.evacuation_solver.input.dto;


import java.util.*;

public class BuildingLayout{

    private final List<Room> rooms;
    private final List<Exit> exits;

    private final List<Staircase> staircases = new ArrayList<>();
    private final Set<Cell> cells = new HashSet<>();
    private final Map<Cell, ArrayList<Cell>> neighbours = new HashMap<>();
    private final Map<Integer, FloorMap> numberedFloorMapsMap = new HashMap<>();



    public BuildingLayout(List<Room> rooms, ParseMapResult parseMapResult) {
        if(rooms == null || parseMapResult.floorMaps() == null || parseMapResult.exits().isEmpty()){
            throw new IllegalArgumentException("Niepełny uklad budynku - rooms/cells/exits jest null");
        }

        validateInput(rooms, parseMapResult.floorMaps(), parseMapResult.exits());

        this.rooms = List.copyOf(rooms);
        this.exits = List.copyOf(parseMapResult.exits());

        // mapowanie pieter
        for(FloorMap floorMap : parseMapResult.floorMaps()){
            numberedFloorMapsMap.put(floorMap.floor(), floorMap);
        }

        // klatkowanie
        for(FloorMap lower : numberedFloorMapsMap.values()){
            FloorMap upper = numberedFloorMapsMap.get(lower.floor()+1);
            if(upper == null)continue;

            for(Character mark : lower.stairCells().keySet()){
                if(upper.stairCells().containsKey(mark)){
                    staircases.add(new Staircase(mark, upper.floor(), lower.floor(), Staircase.DEAFAULT_LENGTH, Staircase.DEFAULT_WIDTH));
                }
            }

        }


        // sumowanie cells
        for(Map.Entry<Integer, FloorMap> floorMap : numberedFloorMapsMap.entrySet()){
            cells.addAll(floorMap.getValue().cells());
        }

        // wyszukiwanie sasiadow
        for(Cell cell1 : cells){
            for(Cell cell2: cells){
                if(cell1 == cell2) continue;
                if( (cell1.x()-1 == cell2.x() && cell1.y() == cell2.y() && cell1.floor() == cell2.floor()) ||
                        (cell1.x()+1 == cell2.x() && cell1.y() == cell2.y() && cell1.floor() == cell2.floor()) ||
                        (cell1.x() == cell2.x() && cell1.y()-1 == cell2.y() && cell1.floor() == cell2.floor()) ||
                        (cell1.x() == cell2.x() && cell1.y()+1 == cell2.y() && cell1.floor() == cell2.floor())
                ){
                    neighbours.computeIfAbsent(cell1, list -> new ArrayList<Cell>()).add(cell2);
                }
            }
        }

        for (int i = 0; i < staircases.size(); i++) {
            Staircase stairs = staircases.get(i);

            // pola z literą klatki na górnym i dolnym piętrze (z mapy)
            Set<Cell> upperCells = numberedFloorMapsMap.get(stairs.fromFloor()).stairCells().get(stairs.mark());
            Set<Cell> lowerCells = numberedFloorMapsMap.get(stairs.toFloor()).stairCells().get(stairs.mark());

            // tworzenie segmentów i laczenie ich w lancuch
            for(int k = 0; k < stairs.length(); k++){
                Cell segment = Cell.stairSegment(i, k);
                cells.add(segment);
                if(k > 0){
                    link(Cell.stairSegment(i, k-1), segment);
                }
            }

            // podlaczanie do pieter
            Cell cell1 = Cell.stairSegment(i, 0);
            for(Cell cell : upperCells){
                link(cell, cell1);
            }

            Cell cell2 = Cell.stairSegment(i, stairs.length()-1);
            for(Cell cell : lowerCells){
                link(cell2, cell);
            }

        }


    }

    public List<Room> rooms() {
        return rooms;
    }

    public List<Exit> exits() {
        return exits;
    }

    public Map<Cell, ArrayList<Cell>> getNeighboursMap() {
        return neighbours;
    }


    private static void validateInput(List<Room> rooms, Set<FloorMap> floorMaps, List<Exit> exits){
        Set<String> roomsId = new HashSet<>();

        for(Room room : rooms){
            if(!roomsId.add(room.id())){
                throw new IllegalArgumentException("RoomId zdublowane!");
            }
        }


    }

    private void link(Cell a, Cell b){
        neighbours.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
        neighbours.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
    }

    public Set<Cell> cells() {
        return cells;
    }

    public List<Cell> getNeighbours(Cell cell) {
        return neighbours.getOrDefault(cell, new ArrayList<>());
    }
}
